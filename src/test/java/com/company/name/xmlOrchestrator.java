package com.company.name;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.*;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockLauncher;
import org.openhim.mediator.engine.testing.TestingUtils;


import static org.junit.Assert.*;

public class xmlOrchestrator {

    static ActorSystem system;
    static MediatorConfig testConfig = new MediatorConfig("java-mediator", "localhost", 3000);

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();

        List<MockLauncher.ActorToLaunch> actorsToLaunch = new LinkedList<>();
        actorsToLaunch.add(new MockLauncher.ActorToLaunch("http-connector", xmlDestTest.class));
        TestingUtils.launchActors(system, testConfig.getName(), actorsToLaunch);
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMediatorHTTPRequest() throws Exception {
        new JavaTestKit(system) {{
            testConfig.getDynamicConfig().put("XmlConfigs", new JSONObject("{\n" +
                    "    \"url2\":\"http://173.255.211.86:5001/sample-gpg-destination\"\n" +
                    "}"));
            final ActorRef XmlOrchestration = system.actorOf(Props.create(XmlOrchestration.class, testConfig));
            String sampleData = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<Gepg>\n" +
                    "  <gepgBillSubResp>\n" +
                    "    <BillTrxInf>\n" +
                    "      <BillId>3953590</BillId>\n" +
                    "      <PayCntrNum>998490333721</PayCntrNum>\n" +
                    "      <TrxSts>GS</TrxSts>\n" +
                    "      <TrxStsCode>7101</TrxStsCode>\n" +
                    "    </BillTrxInf>\n" +
                    "  </gepgBillSubResp>\n" +
                    "  <gepgSignature>NA7QivLOweFW0YW9o05AWRckXNCsU0haPPxnvAEkHkaW0xxNCaXRrqvAxsXRm+Qrg==</gepgSignature>\n" +
                    "</Gepg>";
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/xmlMediator",
                    sampleData,
                    Collections.<String, String>singletonMap("Content-Type", "text/plain"),
                    Collections.<Pair<String, String>>emptyList()
            );
            XmlOrchestration.tell(POST_Request, getRef());
            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("5 second")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            if (msg instanceof FinishRequest) {
                                return msg;
                            }
                            throw noMatch();
                        }
                    }.get();

            boolean foundResponse = false;

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                }
            }

            assertTrue("Must send FinishRequest", foundResponse);
        }};
    }
}
