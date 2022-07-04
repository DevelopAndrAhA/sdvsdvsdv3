package com.humdet;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by altynbek.kochkonbaev on 19.05.2020.
 */

public class OkHttpUtils {
    public  OkHttpClient okHttpClient = new OkHttpClient();

    public  OkHttpClient getInstance() {
        okHttpClient.setConnectTimeout(2, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(2, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(24, TimeUnit.SECONDS);
        return okHttpClient;
    }
}