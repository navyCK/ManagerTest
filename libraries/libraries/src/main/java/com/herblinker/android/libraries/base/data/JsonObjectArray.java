package com.herblinker.android.libraries.base.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractCollection;
import java.util.Iterator;

public class JsonObjectArray extends AbstractCollection<JsonObject> {
    private JSONArray jsonArray;
    public JsonObjectArray(JSONArray jsonArray) {
        this.jsonArray=jsonArray;
    }
    public JsonObjectArray(String jsonArray) throws JsonDataException{
        try {
            this.jsonArray = new JSONArray(jsonArray);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }


    @Override
    public Iterator<JsonObject> iterator() {
        return new Iterator<JsonObject>() {
            int length = jsonArray.length();
            int index = 0;
            @Override
            public JsonObject next() {
                try {
                    return getJsonObject(index++);
                } catch (JsonDataException e) {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {
                return index<length;
            }
        };
    }

    public int size() {
        return jsonArray.length();
    }

    public boolean isNull(int index) {
        return !has(index);
    }

    public boolean has(int index) {
        return index<0?false:index<size();
    }

    public JsonObject getJsonObject(int index) throws JsonDataException{
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            return jsonObject == null ? null : new JsonObject(jsonObject);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }

    public JsonObjectArray getJsonObjectArray(int index) throws JsonDataException{
        try {
            JSONArray jsonArray = this.jsonArray.getJSONArray(index);
            return jsonArray == null ? null : new JsonObjectArray(jsonArray);
        } catch (JSONException e){
            throw new JsonDataException(e);
        }
    }

    public Byte getByte(int index, Byte ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                int value = jsonArray.getInt(index);
                return new Byte((byte)(value&0xFF));
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Short getShort(int index, Short ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                int value = jsonArray.getInt(index);
                return new Short((short)(value&0xFFFFFF));
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Integer getInteger(int index, Integer ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                return jsonArray.getInt(index);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Long getLong(int index, Long ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                return jsonArray.getLong(index);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Float getFloat(int index, Float ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                return (float)jsonArray.getDouble(index);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public Double getDouble(int index, Double ifNull) throws JsonDataWrongFormatException {
        if(has(index)){
            try {
                return jsonArray.getDouble(index);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        }
        return ifNull;
    }

    public String getString(int index, String ifNull) throws JsonDataWrongFormatException {
        if(has(index))
            try {
                return jsonArray.getString(index);
            }catch(Exception e) {
                throw new JsonDataWrongFormatException(e);
            }
        return ifNull;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");
        int length = size();
        JsonObject jsonObject;
        JsonObjectArray jsonObjectArray;
        String value;
        if(length>0){
            for(int index=0;index<length;++index) {
                jsonObject = null;
                jsonObjectArray = null;
                try {
                    jsonObject = getJsonObject(index);
                } catch (JsonDataException e) {
                    try {
                        jsonObjectArray = getJsonObjectArray(index);
                    } catch (JsonDataException e1) {
                    }
                }

                if (isNull(index)) {
                    stringBuilder.append("null");
                }
                if (jsonObject != null) {
                    stringBuilder.append(jsonObject);
                } else if (jsonObjectArray != null) {
                    stringBuilder.append(jsonObjectArray);
                } else {
                    try {
                        value = getString(index, null);
                        if (value == null)
                            stringBuilder.append("null");
                        else
                            while (true) {
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
                                if (value.equalsIgnoreCase("true"))
                                    stringBuilder.append("true");
                                else if (value.equalsIgnoreCase("false"))
                                    stringBuilder.append("false");
                                else {
                                    stringBuilder.append('"');
                                    stringBuilder.append(value);
                                    stringBuilder.append('"');
                                }
                                break;
                            }
                    } catch (JsonDataWrongFormatException e) {
                        stringBuilder.append("error");
                    }
                }
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
