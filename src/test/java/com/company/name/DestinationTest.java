package com.company.name;

import org.json.JSONObject;
import org.junit.Assert;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;

import java.util.Map;

public class DestinationTest extends MockHTTPConnector {
    @Override
    public String getResponse() {
        return null;
    }

    @Override
    public Integer getStatus() {
        return 200;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public void executeOnReceive(MediatorHTTPRequest mediatorHTTPRequest) {
        String Iliyopokelewa = mediatorHTTPRequest.getBody();
        JSONObject objectIliyopokelewa = new JSONObject(Iliyopokelewa);
        Assert.assertEquals("jamatini", objectIliyopokelewa.getString("name"));
        Assert.assertEquals("120321-5", objectIliyopokelewa.getString("code"));
        Assert.assertEquals("jamatini", objectIliyopokelewa.getString("shortName"));
        Assert.assertEquals("2021-04-08", objectIliyopokelewa.getString("openingDate"));
        Assert.assertEquals("[-6.721521,39.2428263]", objectIliyopokelewa.getString("coordinates"));
        Assert.assertEquals(true, objectIliyopokelewa.getBoolean("active"));
    }
}
