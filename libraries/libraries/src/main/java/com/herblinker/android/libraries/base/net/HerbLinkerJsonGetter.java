package com.herblinker.android.libraries.base.net;

import android.os.AsyncTask;
import android.util.Log;

import com.herblinker.android.libraries.base.data.JsonDataException;
import com.herblinker.android.libraries.base.data.JsonObject;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class HerbLinkerJsonGetter {
    public static final int CONNECTION_FAIL = -1;
    private static final String JSON_RESPONSE = "response";
    private static final String JSON_RESPONSE_HEADER = "header";
    private static final String JSON_RESPONSE_HEADER_RESPONSE_TIME = "responseTime";
    private static final String JSON_RESPONSE_HEADER_RESULT_CODE = "resultCode";
    private static final String JSON_RESPONSE_BODY = "body";
    private static final int DEFAULT_TIMEOUT_MILLIS = 60_000;

    private CookieData cookieData;
    private boolean useMultipart;
    private Object object;

    public static class WebTask extends AsyncTask<Void, Void, Void>{
        private HerbLinkerJsonGetter webGetter;
        private OnGetterListener onGetterListener;
        private String urlAddr;
        private List<NameValuePair> parameters;
        private int timeoutMillis;

        private volatile boolean networkFail;
        private volatile boolean invalidJson;
        private volatile boolean invalidHLJson;
        private volatile boolean isSuccess;
        private volatile int code;
        private volatile String rawJson;

        private volatile String responseTime;
        private volatile String resultCode;
        private volatile JsonObject json;

        private WebTask(HerbLinkerJsonGetter webGetter, OnGetterListener onGetterListener, String urlAddr, List<NameValuePair> parameters, int timeoutMillis){
            this.webGetter=webGetter;
            this.onGetterListener=onGetterListener;
            this.urlAddr=urlAddr;
            this.parameters=parameters;
            this.timeoutMillis=timeoutMillis;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            networkFail=true;
            invalidJson=true;
            invalidHLJson=true;
            isSuccess=false;
            try {
                url = new URL(urlAddr);
                OkHttpClient okHttpClient;
                if(webGetter.cookieData==null)
                    okHttpClient = new OkHttpClient.Builder().connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).writeTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                            .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build();
                else {
                    CookieManager cookieManager = new CookieManager(webGetter.cookieData, CookiePolicy.ACCEPT_ALL);
                    okHttpClient = new OkHttpClient.Builder().connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).writeTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                            .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS).cookieJar(new JavaNetCookieJar(cookieManager)).build();
                }
                Request.Builder requestBuilder = new Request.Builder();
                if(parameters!=null) {
                    if (webGetter.useMultipart) {
                        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
                        multipartBodyBuilder.setType(MultipartBody.FORM);
                        for (NameValuePair parameter : parameters){
                            Log.e("CHECK", parameter.name);
                            if(parameter.isFile) {
                                if(parameter.file!=null){
                                    multipartBodyBuilder.addFormDataPart(parameter.name, parameter.fileName, RequestBody.create(MediaType.parse(parameter.contentType), parameter.file));
                                } else if(parameter.bytes!=null){
                                    multipartBodyBuilder.addFormDataPart(parameter.name, parameter.fileName, RequestBody.create(MediaType.parse(parameter.contentType), parameter.bytes));
                                } else {
                                    multipartBodyBuilder.addFormDataPart(parameter.name, parameter.fileName);
                                }
                            } else {
                                multipartBodyBuilder.addFormDataPart(parameter.name, parameter.value);
                            }
                        }
                        requestBuilder.post(multipartBodyBuilder.build());
                    } else {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        for (NameValuePair parameter : parameters)
                            formBodyBuilder.addEncoded(parameter.name, URLEncoder.encode(parameter.value, "UTF-8"));
                        requestBuilder.post(formBodyBuilder.build());
                    }
                }
                requestBuilder.url(url);
                Response response = okHttpClient.newCall(requestBuilder.build()).execute();
                code=response.code();
                if(response.isSuccessful()){
                    rawJson=response.body().string();
                    networkFail=false;
                    try {
                        JsonObject result = new JsonObject(rawJson);
                        invalidJson = false;
                        if(!result.isNull(JSON_RESPONSE)){
                            JsonObject responseObject = result.getJsonObject(JSON_RESPONSE);
                            if (!responseObject.isNull(JSON_RESPONSE_HEADER)){
                                JsonObject header = responseObject.getJsonObject(JSON_RESPONSE_HEADER);
                                if (!header.isNull(JSON_RESPONSE_HEADER_RESPONSE_TIME))
                                    if (!header.isNull(JSON_RESPONSE_HEADER_RESULT_CODE)){
                                        responseTime = header.getString(JSON_RESPONSE_HEADER_RESPONSE_TIME);
                                        resultCode = header.getString(JSON_RESPONSE_HEADER_RESULT_CODE);
                                        invalidHLJson=false;
                                        isSuccess = true;
                                        if(!responseObject.isNull(JSON_RESPONSE_BODY))
                                            json = responseObject.getJsonObject(JSON_RESPONSE_BODY);
                                    }
                            }
                        }
                    } catch (JsonDataException e){
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (ConnectException e){
                e.printStackTrace();
                code=CONNECTION_FAIL;
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void oVoid) {
            if(onGetterListener==null)
                return;
            try {
                if (isSuccess)
                    onGetterListener.onSuccess(responseTime, resultCode, json);
                else
                    onGetterListener.onError(networkFail, invalidJson, invalidHLJson, rawJson);
            } finally {
                if(onGetterListener.onComplete(code))
                    return;
                webGetter.responseTo(webGetter.object, code);
            }
        }
    }

    public HerbLinkerJsonGetter(){this(null, false, null);}
    public HerbLinkerJsonGetter(boolean useMultipart){this(null, useMultipart, null);}
    public HerbLinkerJsonGetter(Object object){this(null, false, object);}
    public HerbLinkerJsonGetter(boolean useMultipart, Object object){this(null, useMultipart, object);}
    public HerbLinkerJsonGetter(CookieData cookieData){this(cookieData, false, null);}
    public HerbLinkerJsonGetter(CookieData cookieData, boolean useMultipart){this(cookieData, useMultipart, null);}
    public HerbLinkerJsonGetter(CookieData cookieData, Object object){this(cookieData, false, object);}
    public HerbLinkerJsonGetter(CookieData cookieData, boolean useMultipart, Object object){
        this.cookieData=cookieData;
        this.useMultipart=useMultipart;
        this.object=object;
    }

    protected abstract void responseTo(Object object, int code);


    public void execute(String urlAddr) {
        execute(null, urlAddr, null, DEFAULT_TIMEOUT_MILLIS);
    }
    public void execute(String urlAddr, int timeoutMillis) {
        execute(null, urlAddr, null, timeoutMillis);
    }
    public void execute(String urlAddr, List<NameValuePair> parameters) {
        execute(null, urlAddr, parameters, DEFAULT_TIMEOUT_MILLIS);
    }
    public void execute(String urlAddr, List<NameValuePair> parameters, int timeoutMillis) {
        execute(null, urlAddr, parameters, timeoutMillis);
    }
    public void execute(OnGetterListener onGetterListener, String urlAddr) {
        execute(onGetterListener, urlAddr, null, DEFAULT_TIMEOUT_MILLIS);
    }
    public void execute(OnGetterListener onGetterListener, String urlAddr, int timeoutMillis) {
        execute(onGetterListener, urlAddr, null, timeoutMillis);
    }
    public void execute(OnGetterListener onGetterListener, String urlAddr, List<NameValuePair> parameters) {
        execute(onGetterListener, urlAddr, parameters, DEFAULT_TIMEOUT_MILLIS);
    }

    public void execute(OnGetterListener onGetterListener, String urlAddr, List<NameValuePair> parameters, int timeoutMillis) {
        WebTask webTask = new WebTask(this, onGetterListener, urlAddr, parameters, timeoutMillis);
        webTask.execute();
    }
}
