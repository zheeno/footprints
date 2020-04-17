package com.cluster.footprints.interfaces;

import org.json.JSONObject;

public interface JsonResponseCallback {
    void done(JSONObject res, Exception e);
}
