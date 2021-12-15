package com.company.name;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;


import org.json.*;

public class XmlOrchestration extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final MediatorConfig config;
    public XmlOrchestration(MediatorConfig config) {
        this.config = config;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            String body = ((MediatorHTTPRequest) msg).getBody();
            JSONObject HFRConfigs = new JSONObject(config.getDynamicConfig()).getJSONObject("XmlConfigs");
            String hfrUri = HFRConfigs.getString("url2");
            MediatorHTTPRequest hfrRequest = new MediatorHTTPRequest(
                    ((MediatorHTTPRequest) msg).getRequestHandler(),
                    getSelf(),
                    "Sending Request to",
                    "POST",
                    hfrUri, body, null, null, null);
            //Getting instance of the http connector
            ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
            //firing request
            httpConnector.tell(hfrRequest, getSelf());
        } else if (msg instanceof MediatorHTTPResponse) {
                JSONObject json = XML.toJSONObject(((MediatorHTTPResponse) msg).getBody());
                String jsonString = json.toString(4);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject aObj= (JSONObject) jsonObject.get("Gepg");


                JSONObject aObj1 = (JSONObject) aObj.get("gepgBillSubRespAck");
                Integer statusCode= (Integer) aObj1.get("TrxStsCode");
                int newCode;
                if(statusCode==7101){
                    newCode=200;
                }else if(statusCode==7201 || statusCode==7400 ||  statusCode==7640){
                    newCode=400;
                }else {
                    newCode=500;
                }
                MediatorHTTPResponse resp = new MediatorHTTPResponse((MediatorHTTPRequest) ((MediatorHTTPResponse) msg).getOriginalRequest(),
                        ((MediatorHTTPResponse) msg).getBody(), newCode, ((MediatorHTTPResponse) msg).getHeaders());
                ((MediatorHTTPResponse) msg).getOriginalRequest().getRequestHandler().tell((resp).toFinishRequest(), getSelf());

        } else {
            unhandled(msg);
        }
    }
}
