package com.mvcoder.download;


import android.support.annotation.NonNull;
import android.util.Log;

import com.mvcoder.download.api.FileApi;
import com.mvcoder.download.interceptor.ProgressInterceptor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class FileManager {

    public final static int RXBUS_CODE = 100;

    public volatile static FileManager instance;

    private WeakReference<Retrofit> mRetrofit;

    private Map<String, Subscription> downloadMap = new HashMap<>();

    private FileManager() {
        mRetrofit = new WeakReference<>(getRetrofit());
    }

    private Retrofit getRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addNetworkInterceptor(new ProgressInterceptor());
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://192.168.2.110:8080/")
                .client(client)
                .build();
        return retrofit;
    }

    public static FileManager getInstance() {
        if (instance == null) {
            synchronized (FileManager.class) {
                if (instance == null) {
                    instance = new FileManager();
                }
            }
        }
        return instance;
    }

    private FileApi fileApi() {
        Retrofit retrofit = mRetrofit.get();
        if (retrofit == null) {
            mRetrofit = new WeakReference<>(getRetrofit());
            retrofit = mRetrofit.get();
        }
        return retrofit.create(FileApi.class);
    }


    public void download(@NonNull final String url, @NonNull final FileCallBack<ResponseBody> callBack) {
        callBack.setTag(url);
        fileApi().download(url)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        Log.d("Download","doOnNext");
                        callBack.saveFile(responseBody);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //在主线程中更新ui
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        downloadMap.remove(url);
                        Log.d("Download", "doOnCancel");
                    }
                })
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        downloadMap.put(url,s);
                        callBack.onStart();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        callBack.onSuccess(responseBody);
                        onComplete();
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (downloadMap.get(url) != null) {
                            downloadMap.remove(url);
                        }
                        callBack.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        if (downloadMap.get(url) != null) {
                            downloadMap.remove(url);
                        }
                        callBack.onCompleted();
                    }
                });
        //.subscribe(new FileSubscriber<ResponseBody>(callBack));
    }

    public void stopDownload(String url) {
        Subscription disposable = downloadMap.get(url);
        if (disposable != null) {
            disposable.cancel();
        }
    }
}
