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

public class DefaultOrchestratorTest {

    static ActorSystem system;
    static MediatorConfig testConfig = new MediatorConfig("java-mediator", "localhost", 3000);

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();

        List<MockLauncher.ActorToLaunch> actorsToLaunch = new LinkedList<>();
        actorsToLaunch.add(new MockLauncher.ActorToLaunch("http-connector", DestinationTest.class));
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
           testConfig.getDynamicConfig().put("HfrConfigs", new JSONObject("{\n" +
                    "    \"url\":\"https://reqbin.com/echo/post/json\"\n" +
                    "}"));
            final ActorRef defaultOrchestrator = system.actorOf(Props.create(DefaultOrchestrator.class, testConfig));
            String sampleData="{\n" +
                    "    \"Fac_IDNumber\": \"120321-5\",\n" +
                    "    \"Name\": \"jamatini\",\n" +
                    "    \"Comm_FacName\": \"jamatini\",\n" +
                    "    \"Zone\": \"Central \",\n" +
                    "    \"Region_Code\": \"TZ.CL.DO\",\n" +
                    "    \"Region\": \"Dodoma \",\n" +
                    "    \"District_Code\": \"TZ.CL.DO.DO\",\n" +
                    "    \"District\": \"Dodoma \",\n" +
                    "    \"Council_Code\": \"TZ.CL.DO.DO.5\",\n" +
                    "    \"Council\": \"Dodoma MC\",\n" +
                    "    \"Ward\": \"Uhuru\",\n" +
                    "    \"Village\": \"Michese\",\n" +
                    "    \"FacilityTypeGroupCode\": \"HLCTR\",\n" +
                    "    \"FacilityTypeGroup\": \"Health Center\",\n" +
                    "    \"FacilityTypeCode\": \"HLCTR\",\n" +
                    "    \"FacilityType\": \"Health Center\",\n" +
                    "    \"OwnershipGroupCode\": \"Publ\",\n" +
                    "    \"OwnershipGroup\": \"Public\",\n" +
                    "    \"OwnershipCode\": \"LGA\",\n" +
                    "    \"Ownership\": \"LGA\",\n" +
                    "    \"OperatingStatus\": \"Operating\",\n" +
                    "    \"Latitude\": -6.721521,\n" +
                    "    \"Longitude\": 39.2428263,\n" +
                    "    \"RegistrationStatus\": \"Registered\",\n" +
                    "    \"OpenedDate\": \"2021-04-08\",\n" +
                    "    \"CreatedAt\": \"2021-04-08 16:30:59\",\n" +
                    "    \"UpdatedAt\": \"2021-06-30 09:01:38\",\n" +
                    "    \"ClosedDate\": \"\",\n" +
                    "    \"OSchangeOpenedtoClose\": \"N\",\n" +
                    "    \"OSchangeClosedtoOperational\": \"N\",\n" +
                    "    \"PostorUpdate\": \"U\"\n" +
                    "}";
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/mediator",
                    sampleData,
                    Collections.<String, String>singletonMap("Content-Type", "text/plain"),
                    Collections.<Pair<String, String>>emptyList()
            );
            defaultOrchestrator.tell(POST_Request, getRef());
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
