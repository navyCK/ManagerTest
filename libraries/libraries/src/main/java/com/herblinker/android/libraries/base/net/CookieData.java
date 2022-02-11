package com.herblinker.android.libraries.base.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.herblinker.libraries.base.data.BytesEncoding;
import com.herblinker.libraries.base.data.DataTransfer;
import com.herblinker.libraries.base.data.DataTransferException;

import java.io.InvalidClassException;
import java.io.Serializable;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CookieData implements CookieStore, Serializable {
    private static final String[] SESSION_NAMES = {"JSESSIONID"};
    private static final String COOKIE_SHARED_PREFERENCES_NAME = "cookie_store_for_okhttp";
    private static final String DEFAULT_ITEM_NAME = "default_cookie_name";
    private static final String DATA_SPLITTER = ":";

    private HashMap<String, HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>> schemeDomainsPathsNames;

    public CookieData(){
        this(new HashMap<String, HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>>());
    }
    public CookieData(HashMap<String, HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>> schemeDomainsPathsNames){
        this.schemeDomainsPathsNames=schemeDomainsPathsNames;
        constructionCheck();
    }
    public CookieData(Context context){
        this.schemeDomainsPathsNames = new HashMap<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(COOKIE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String data = sharedPreferences.getString(DEFAULT_ITEM_NAME, null);
        //Log.e("CHECK", "불러옴: "+data);
        if(data!=null)
            try {
                schemeDomainsPathsNames = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>>)DataTransfer.bytesToObject(BytesEncoding.HEXA.decode(data));
            }catch (Exception e){
                e.printStackTrace();
            }
        //Log.e("CHECK", "불러옴: "+schemeDomainsPathsNames);
        constructionCheck();
    }

    public CookieData(String data){
        if(data!=null)
            try {
                schemeDomainsPathsNames = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>>)DataTransfer.bytesToObject(BytesEncoding.HEXA.decode(data));
            }catch (Exception e){
                e.printStackTrace();
            }
        constructionCheck();
    }
    private void constructionCheck(){
        if(schemeDomainsPathsNames==null)
            schemeDomainsPathsNames = new HashMap<>();
        else {
            Collection<HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>>> domains = schemeDomainsPathsNames.values();
            if(domains!=null) {
                for (HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>> domain : domains) {
                    if (domain != null) {
                        Collection<HashMap<String, HashMap<String, SerializableHttpCookie>>> paths = domain.values();
                        for(HashMap<String, HashMap<String, SerializableHttpCookie>> path: paths){
                            if(path!=null){
                                Collection<HashMap<String, SerializableHttpCookie>> names = path.values();
                                for(HashMap<String, SerializableHttpCookie> name: names){
                                    if(name!=null){
                                        SerializableHttpCookie cookie;
                                        for(String key: name.keySet()){
                                            cookie = name.get(key);
                                            if(cookie!=null) {
                                                if (cookie.name != null) {
                                                    for (String session : SESSION_NAMES) {
                                                        if (session.equals(cookie.name))
                                                            name.remove(key);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public boolean saveToSharedPreferences(Context context){
        //Log.e("CHECK", "저장함: "+schemeDomainsPathsNames);
        //Log.e("CHECK", "저장함: "+toString());
        SharedPreferences sharedPreferences = context.getSharedPreferences(COOKIE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putString(DEFAULT_ITEM_NAME, toString());
        if(editor.commit())
            return true;
        editor.apply();
        return false;
    }

    @Override
    public String toString(){
        try {
            return BytesEncoding.HEXA.encode(DataTransfer.objectToBytes(schemeDomainsPathsNames));
        } catch (DataTransferException | InvalidClassException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized void add(URI uri, HttpCookie cookie) {
        if(uri==null)
            return;
        if(cookie==null)
            return;
        String host = uri.getHost();
        String domain = cookie.getDomain();
        if(host.equals(domain)) {
            String scheme = uri.getScheme();
            String path = cookie.getPath();
            String name = cookie.getName();
            HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>> domainsPathsNames = schemeDomainsPathsNames.get(scheme);
            //Log.e("CHECK", "Add: scheme: "+uri.getScheme());
            //Log.e("CHECK", "Add: domain: "+domain);
            //Log.e("CHECK", "Add: name: "+name);
            //Log.e("CHECK", "Add: U path: "+uri.getPath());
            //Log.e("CHECK", "Add: C path: "+cookie.getPath());
            if (domainsPathsNames == null) {
                domainsPathsNames = new HashMap<>();
                schemeDomainsPathsNames.put(scheme, domainsPathsNames);
            }
            if (cookie.hasExpired()) {
                HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames = domainsPathsNames.get(domain);
                if (pathsNames == null)
                    return;
                HashMap<String, SerializableHttpCookie> names = pathsNames.get(path);
                //Log.e("CHECK", "Add: path: "+path);
                if (names == null)
                    return;
                names.remove(name);
            } else {
                HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames = domainsPathsNames.get(domain);
                if (pathsNames == null) {
                    pathsNames = new HashMap<>();
                    domainsPathsNames.put(domain, pathsNames);
                }
                HashMap<String, SerializableHttpCookie> names = pathsNames.get(path);
                if (names == null) {
                    names = new HashMap<>();
                    pathsNames.put(path, names);
                }
                //Log.e("CHECK", "Add: name: "+name);
                //Log.e("CHECK", "Add: "+cookie.toString());
                names.put(name, new SerializableHttpCookie(cookie));
            }
        }
    }

    @Override
    public synchronized List<HttpCookie> get(URI uri) {
        HashMap<String, HttpCookie> result = new HashMap<>();
        //Log.e("CHECK", "Get: data: "+schemeDomainsPathsNames);
        //Log.e("CHECK", "Get: scheme: "+uri.getScheme());
        HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>> domainsPathsNames = schemeDomainsPathsNames.get(uri.getScheme());
        if(domainsPathsNames!=null){
            //Log.e("CHECK", "Get: domain: "+uri.getHost());
            HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames = domainsPathsNames.get(uri.getHost());
            if(pathsNames!=null){
                String path = uri.getPath();
                //Log.e("CHECK", "Get: path: "+path);
                int length = path.length();
                String subPath;
                HashMap<String, SerializableHttpCookie> names;
                //TODO 추후에 "/" 단위로 끊어지게 주소를 부르게 변경 해야함. 저렇게 죄다 부르면 안됌
                for(int i=length;i>0;--i){
                    subPath=path.substring(0, i);
                    //Log.e("CHECK", "Get: path: "+subPath);
                    names=pathsNames.get(subPath);
                    //Log.e("CHECK", "Get: names: "+names);
                    if(names!=null)
                        for(SerializableHttpCookie sCookie: names.values()) {
                            //Log.e("CHECK", "Get: name: "+sCookie.name);
                            if (!result.containsKey(sCookie.name))
                                result.put(sCookie.name, sCookie.getHttpCookie());
                        }
                }
            }
        }
        //Log.e("CHECK", "Get: "+new LinkedList<>(result.values()).toString());
        return new LinkedList<>(result.values());
    }

    @Override
    public synchronized List<HttpCookie> getCookies() {
        LinkedList<HttpCookie> result = new LinkedList<>();
        for(HashMap<String, HashMap<String ,HashMap<String, SerializableHttpCookie>>> domainsPathsNames: schemeDomainsPathsNames.values())
            for(HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames: domainsPathsNames.values())
                for(HashMap<String, SerializableHttpCookie> names : pathsNames.values())
                    for(SerializableHttpCookie sCookie: names.values())
                        if(sCookie!=null)
                            result.add(sCookie.getHttpCookie());
        return result;
    }

    @Override
    public synchronized List<URI> getURIs() {
        LinkedList<URI> result = new LinkedList<>();
        HashMap<String, HashMap<String ,HashMap<String, SerializableHttpCookie>>> domainsPathsNames;
        HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames;
        HashMap<String, SerializableHttpCookie> names;

        for(String scheme : schemeDomainsPathsNames.keySet()) {
            domainsPathsNames=schemeDomainsPathsNames.get(scheme);
            if(domainsPathsNames==null)
                continue;
            for (String domain : domainsPathsNames.get(scheme).keySet()) {
                pathsNames=domainsPathsNames.get(domain);
                if(pathsNames==null)
                    continue;
                for (String path : pathsNames.get(scheme).keySet()) {
                    names=pathsNames.get(path);
                    if(names==null)
                        continue;
                    try {
                        result.add(new URI(scheme, domain, path, null));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        if(uri==null)
            return false;
        if(cookie==null)
            return false;
        String host = uri.getHost();
        String domain = cookie.getDomain();
        SerializableHttpCookie sCookie = null;
        if(host.equals(domain)) {
            String path = cookie.getPath();
            String name = cookie.getName();
            HashMap<String, HashMap<String, HashMap<String, SerializableHttpCookie>>> domainsPathsNames = schemeDomainsPathsNames.get(uri.getScheme());
            if(domainsPathsNames!=null){
                HashMap<String, HashMap<String, SerializableHttpCookie>> pathsNames = domainsPathsNames.get(domain);
                if (pathsNames != null){
                    HashMap<String, SerializableHttpCookie> names = pathsNames.get(path);
                    if (names != null)
                        sCookie=names.remove(name);
                }
            }
        }
        return sCookie!=null;
    }

    @Override
    public synchronized boolean removeAll() {
        schemeDomainsPathsNames.clear();
        return true;
    }

    public static void clearCookieData(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(COOKIE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private static class SerializableHttpCookie implements Serializable {
        private int version;
        private long maxAge;
        private boolean secure;
        private boolean discard;
        private String comment;
        private String commentURL;
        private String domain;
        private String path;
        private String portlist;
        private String name;
        private String value;

        private SerializableHttpCookie(HttpCookie cookie){
            version = cookie.getVersion();
            maxAge = cookie.getMaxAge();
            discard = cookie.getDiscard();
            secure = cookie.getSecure();
            comment = cookie.getComment();
            commentURL= cookie.getCommentURL();
            domain = cookie.getDomain();
            path = cookie.getPath();
            portlist = cookie.getPortlist();
            name = cookie.getName();
            value = cookie.getValue();
        }
        private HttpCookie getHttpCookie(){
            HttpCookie cookie = new HttpCookie(name, value);
            cookie.setVersion(version);
            cookie.setMaxAge(maxAge);
            cookie.setDiscard(discard);
            cookie.setSecure(secure);
            cookie.setComment(comment);
            cookie.setCommentURL(commentURL);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setPortlist(portlist);
            return cookie;
        }
    }
}
