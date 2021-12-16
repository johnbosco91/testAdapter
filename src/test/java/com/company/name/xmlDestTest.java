package com.company.name;

import org.json.JSONObject;
import org.junit.Assert;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;

import java.util.Map;

public class xmlDestTest extends MockHTTPConnector {
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
//test here
    }
}
