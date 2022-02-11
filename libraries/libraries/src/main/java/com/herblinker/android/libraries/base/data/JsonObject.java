package com.herblinker.android.libraries.base.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class JsonObject {
    private JSONObject jsonObject;

    public JsonObject(String rawJson) throws JsonDataException{
        try {
            this.jsonObject = new JSONObject(rawJson);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }
    public JsonObject(JSONObject jsonObject) {
        this.jsonObject=jsonObject;
    }

    public boolean isNull(String name) {
        return jsonObject.isNull(name);
    }

    public boolean has(String name) {
        return jsonObject.has(name);
    }

    public JsonObject getJsonObject(String key) throws JsonDataException{
        try {
            JSONObject jsonObject = this.jsonObject.getJSONObject(key);
            return jsonObject==null?null:new JsonObject(jsonObject);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }

    public JsonObjectArray getJsonObjectArray(String key) throws JsonDataException{
        try {
            JSONArray jsonArray = this.jsonObject.getJSONArray(key);
            return jsonArray==null?null:new JsonObjectArray(jsonArray);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }

    public Byte getByte(String key, Byte ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                int value = jsonObject.getInt(key);
                return new Byte((byte)(value&0xFF));
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Short getShort(String key, Short ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                int value = jsonObject.getInt(key);
                return new Short((byte)(value&0xFFFF));
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Integer getInteger(String key, Integer ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                return jsonObject.getInt(key);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Long getLong(String key, Long ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                return jsonObject.getLong(key);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Float getFloat(String key, Float ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                return (float)jsonObject.getDouble(key);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Double getDouble(String key, Double ifNull) throws JsonDataWrongFormatException {
        if(has(key)){
            try {
                return jsonObject.getDouble(key);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }
    public String getString(String key) throws JsonDataWrongFormatException {
        try {
            return jsonObject.getString(key);
        }catch(Exception e) {
            throw new JsonDataWrongFormatException(e);
        }
    }

    public String getString(String key, String ifNull) throws JsonDataWrongFormatException {
        if(has(key))
            try {
                return jsonObject.getString(key);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        return ifNull;
    }

    public Set<String> keySet(){
        Set<String> keySet = new HashSet<>();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext())
            keySet.add(iterator.next());
        return keySet;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        Set<String> keys = keySet();
        Iterator<String> iterator = keys.iterator();
        String key;
        String value;
        while(iterator.hasNext()) {
            key=iterator.next();
            stringBuilder.append('"');
            stringBuilder.append(key);
            stringBuilder.append("\":");
            JsonObject jsonObject = null;
            JsonObjectArray jsonObjectArray = null;
            try {
                jsonObject = getJsonObject(key);
            } catch (JsonDataException e){
                try {
                    jsonObjectArray = getJsonObjectArray(key);
                } catch (JsonDataException e1){
                }
            }
            if(isNull(key)) {
                stringBuilder.append("null");
            } if(jsonObject!=null) {
                stringBuilder.append(jsonObject);
            } else if(jsonObjectArray!=null) {
                stringBuilder.append(jsonObjectArray);
            } else {
                try {
                    value = getString(key);
                    while(true) {
                        try {
                            stringBuilder.append(Byte.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stringBuilder.append(Short.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stringBuilder.append(Integer.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stringBuilder.append(Long.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stringBuilder.append(Float.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        try {
                            stringBuilder.append(Double.valueOf(value));
                            break;
                        } catch (NumberFormatException e) {
                        }
                        if(value.equalsIgnoreCase("true"))
                            stringBuilder.append("true");
                        else if(value.equalsIgnoreCase("false"))
                            stringBuilder.append("false");
                        else {
                            stringBuilder.append('"');
                            stringBuilder.append(value);
                            stringBuilder.append('"');
                        }
                        break;
                    }
                } catch (JsonDataWrongFormatException e){
                    stringBuilder.append("error");
                }
            }
            if(iterator.hasNext())
                stringBuilder.append(',');
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
