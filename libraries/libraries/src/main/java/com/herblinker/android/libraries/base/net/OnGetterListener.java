package com.herblinker.android.libraries.base.net;

import com.herblinker.android.libraries.base.data.JsonObject;

public interface OnGetterListener {
    public void onSuccess(String responseTime, String resultCode, JsonObject jsonObject);
    public void onError(boolean networkFail, boolean invalidJson, boolean invalidHLJson, String rawJson);
    public boolean onComplete(int code);
}
