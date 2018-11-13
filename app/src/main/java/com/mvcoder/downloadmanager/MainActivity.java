package com.mvcoder.downloadmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Button btTest;
    private Button btDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btTest = findViewById(R.id.btTest);
        btDisposable = findViewById(R.id.btDisposable);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testRxJava();
            }
        });

        btDisposable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }*/
               Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
               startActivity(intent);
            }
        });
    }

    private Disposable disposable;

    private void testRxJava() {
        /*Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                for (int i = 0; i < 10; i++) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext("Hello");
                        try {
                            Thread.sleep(1000);
                        }catch (Exception e){

                        }
                    }
                }
                if(!emitter.isDisposed())
                    emitter.onError(new Throwable("error.."));
            }
        });*/
        disposable = Observable.just("hello onnext")
                .delay(1, TimeUnit.SECONDS)
                .repeat(10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        log("onSubscribe");
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        log("onDispose");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        log("on complete");
                    }
                }).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        log("onnext : " + s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log("onError : " + throwable.getMessage());
                    }
                });


    }

    private final String TAG = MainActivity.class.getSimpleName();

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
