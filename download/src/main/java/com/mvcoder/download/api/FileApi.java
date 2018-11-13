package com.mvcoder.download.api;

import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileApi {

    @Streaming
    @GET
    Flowable<ResponseBody> download(@Url String url);//直接使用网址下载
}
