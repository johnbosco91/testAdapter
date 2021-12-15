package com.company.name;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;


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

            JSONObject orgUnit = new JSONObject();
            String coordinates = "[" + jsonObject.get("Latitude") + "," + jsonObject.get("Longitude") + "]";
            boolean isActive;
            isActive = jsonObject.getString("OperatingStatus").equals("Operating");

            orgUnit.put("code", jsonObject.get("FacilityTypeGroupCode"));
            JSONArray OrgUnits = new JSONArray();
            OrgUnits.put(orgUnit);
            orgUnit.put("code", jsonObject.get("FacilityTypeCode"));
            OrgUnits.put(orgUnit);
            orgUnit.put("code", jsonObject.get("Ownership"));
            OrgUnits.put(orgUnit);
            orgUnit.put("code", jsonObject.get("OwnershipGroupCode"));
            OrgUnits.put(orgUnit);
            String jsonString = new JSONObject()
                    .put("name", jsonObject.get("Name"))
                    .put("code", jsonObject.get("Fac_IDNumber"))
                    .put("shortName", jsonObject.get("Comm_FacName"))
                    .put("openingDate", jsonObject.get("OpenedDate"))
                    .put("coordinates", coordinates)
                    .put("active", isActive)
                    .put("parent", new JSONObject().put("code", jsonObject.get("Council_Code")))
                    .put("organisationUnitGroups", OrgUnits)
                    .toString();
            JSONObject HFRConfigs = new JSONObject(config.getDynamicConfig()).getJSONObject("HfrConfigs");
            String hfrUri = HFRConfigs.getString("url");
            MediatorHTTPRequest hfrRequest = new MediatorHTTPRequest(
                    ((MediatorHTTPRequest) msg).getRequestHandler(),
                    getSelf(),
                    "Sending Request to",
                    "POST",
                    hfrUri, jsonString, null, null, null);
            //Getting instance of the http connector
            ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));

            //firing request
            httpConnector.tell(hfrRequest, getSelf());

//            httpConnector.tell(jsonString, getSelf());
        } else if (msg instanceof MediatorHTTPResponse) {
            ((MediatorHTTPResponse) msg).getOriginalRequest().getRequestHandler().tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            unhandled(msg);
        }
    }
}
