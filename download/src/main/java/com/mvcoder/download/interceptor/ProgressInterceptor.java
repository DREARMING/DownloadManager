package com.mvcoder.download.interceptor;

import com.mvcoder.download.ProgressResponseBody;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by mvcoder on 2018/11/13.
 */
public class ProgressInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        String tag = originalResponse.request().url().toString();

        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), tag))
                .build();
    }
}
