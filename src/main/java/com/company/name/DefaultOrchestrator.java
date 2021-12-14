package com.company.name;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.http.HttpStatus;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import org.json.JSONObject;


public class DefaultOrchestrator extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final MediatorConfig config;


    public DefaultOrchestrator(MediatorConfig config) {
        this.config = config;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            String body = ((MediatorHTTPRequest) msg).getBody();
             JSONObject jsonObject = new JSONObject(body);


//            JSONObject Fac_IDNumber = new JSONObject(body.getJSONObject("HfrConfigs"));
            System.out.println(jsonObject.get("Fac_IDNumber"));

            JSONObject HFRConfigs = new JSONObject(config.getDynamicConfig()).getJSONObject("HfrConfigs");
            String hfrUri = HFRConfigs.getString("url");
            MediatorHTTPRequest hfrRequest = new MediatorHTTPRequest(
                    ((MediatorHTTPRequest) msg).getRequestHandler(),
                    getSelf(),
                    "Sending Request to",
                    "POST",
                    hfrUri);

            String jsonString = new JSONObject()
                    .put("name", "Hello World!")
                    .put("JSON2", "Hello my World!")
                    .put("JSON3", new JSONObject().put("key1", "value1"))
                    .toString();

//            {
//                "name": "jamatini Health Center",
//                    "code": "120321-5",
//                    "shortName": "jamatini",
//                    "openingDate": "2021-04-08",
//                    "coordinates": "[-6.721521,39.2428263]",
//                    "active": true,
//                    "parent": {
//                "code": "TZ.CL.DO.DO.5"
//            },
//                "organisationUnitGroups": [
//                {
//                    "code": "HLCTR"
//                },
//                {
//                    "code": "HLCTR"
//                },
//                {
//                    "code": "LGA"
//                },
//                {
//                    "code": "Publ"
//                }
//    ]
//            }

            System.out.println(jsonString);
            //Getting instance of the http connector
            ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));

            //firing request
//            httpConnector.tell(hfrRequest, getSelf());

            httpConnector.tell(jsonString, getSelf());
        }else if(msg instanceof MediatorHTTPResponse){
            ((MediatorHTTPResponse) msg).getOriginalRequest().getRequestHandler().tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            unhandled(msg);
        }
    }
}
