package com.github.timeloveboy.moehttpclient;

import com.github.timeloveboy.moehttpclient.cachecenter.domain_cookie;
import com.github.timeloveboy.utils.CookieUtil;
import com.github.timeloveboy.utils.Log;
import okhttp3.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by timeloveboy on 17-3-15.
 */
public class MoeHttpClient {
    Request.Builder requestbuilder = new Request.Builder();
    domain_cookie cookiecenter=new domain_cookie();
    OkHttpClient client;
    URL u;

    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.followRedirects(false);
        builder.followSslRedirects(false);
        client = builder.build();
    }

    public MoeHttpClient() {

    }


    public MoeHttpClient(domain_cookie cookiecenter) {
        this.cookiecenter = cookiecenter;
    }

    public URL getlastReqUrl() {
        return u;
    }

    public Request.Builder getRequestbuilder() {
        return requestbuilder;
    }

    public domain_cookie getCookiecenter() {
        return cookiecenter;
    }

    public MoeHttpClient GET(String url)throws MalformedURLException{

        requestbuilder.get().url(url);
        u = new URL(url);
        return this;
    }
    public MoeHttpClient header(String key,String value){
        requestbuilder.header(key, value);
        return this;
    }

    public Response execute() {
        Set<Cookie> cs=cookiecenter.GetSiteCookies(u.getHost());
        requestbuilder.header("Cookie", CookieUtil.cookieraw_fromcookie(cs));
        Request request = requestbuilder
                .build();
        Response response;
        try {
            response= client.newCall(request).execute();
        }catch (Exception e){
            e.printStackTrace();
            response=null;
        }
        return response;
    }
    public Response execute_andsavecookies() {
        Response response=execute();
        Map<String,String> addcookie= CookieUtil.parse(response.headers());
        if(u!=null){
            for(String key:addcookie.keySet()) {
                cookiecenter.AddCookie(u.getHost(),key,addcookie.get(key));
            }
        }
        return response;
    }

    public Response execute_andsavecookies_location() throws Exception {
        Response response=execute_andsavecookies();
        if(response.header("Location")!=null){
            String ur = response.header("Location");
            Log.v(u, " 重定向 ", ur);
            requestbuilder.url(ur).get();
            u = new URL(ur);
            response=execute_andsavecookies_location();
        }
        return response;
    }
    //HttpMethod method,
    public MoeHttpClient POST(String url, Map<String,String> form)throws MalformedURLException{

        FormBody.Builder reqbuilder = new FormBody.Builder();
        for (String key : form.keySet()) {
            String value = form.get(key);
            reqbuilder.add(key,value);
        }
        RequestBody formBody = reqbuilder.build();
        requestbuilder.url(url).post(formBody);
        u = new URL(url);
        return this;
    }
}