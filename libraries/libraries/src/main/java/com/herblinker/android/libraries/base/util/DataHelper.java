package com.herblinker.android.libraries.base.util;

import org.json.JSONException;
import org.json.JSONObject;

public class DataHelper {
    public static boolean objectExists(JSONObject parent, String name){
        if(parent.has(name)){
            if(parent.isNull(name))
                return false;
            return true;
        } else
            return false;
    }
    public static boolean arrayExists(JSONObject parent, String name) throws JSONException {
        if(parent.has(name)){
            if(parent.isNull(name))
                return false;
            return parent.getJSONArray(name).length()!=0;
        } else
            return false;
    }
}
