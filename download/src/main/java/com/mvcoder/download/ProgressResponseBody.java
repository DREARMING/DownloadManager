package com.mvcoder.download;

import android.util.Log;

import com.mvcoder.rxbus.RxBus;

import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by miya95 on 2016/12/5.
 */
public class ProgressResponseBody extends ResponseBody {
    private ResponseBody responseBody;
    private String tag;

    private BufferedSource bufferedSource;

    public ProgressResponseBody(ResponseBody responseBody, String tag) {
        this.responseBody = responseBody;
        this.tag = tag;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        /*return new ForwardingSource(source) {
            long bytesReaded = 0;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                bytesReaded += bytesRead == -1 ? 0 : bytesRead;
               //实时发送当前已读取(上传/下载)的字节

                RxBus.getDefault().post(FileManager.RXBUS_CODE, new FileLoadEvent(contentLength(), bytesReaded, tag));
                return bytesRead;
            }
        };*/
        return new MyForwardingSource(source);
    }

    class MyForwardingSource extends ForwardingSource implements FlowableOnSubscribe<FileLoadEvent> {

        long bytesReaded = 0;
        private FlowableEmitter<FileLoadEvent> emitter;
        private Flowable<FileLoadEvent> flowable;
        private FileLoadEvent fileLoadEvent = new FileLoadEvent(contentLength(), 0, tag);
        private Subscription subscription;

        public MyForwardingSource(Source delegate) {
            super(delegate);
            initFlowable();
        }

        private void initFlowable() {
            flowable = Flowable.create(this, BackpressureStrategy.LATEST);
            flowable.sample(500, TimeUnit.MILLISECONDS, true).subscribe(new FlowableSubscriber<FileLoadEvent>() {
                @Override
                public void onSubscribe(Subscription s) {
                     subscription = s;
                     s.request(1);
                }

                @Override
                public void onNext(FileLoadEvent fileLoadEvent) {
                    RxBus.getDefault().post(FileManager.RXBUS_CODE, fileLoadEvent);
                    subscription.request(1);
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onComplete() {

                }
            });
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            bytesReaded += bytesRead == -1 ? 0 : bytesRead;
            if(!emitter.isCancelled()) {
                fileLoadEvent.setBytesLoaded(bytesReaded);
                if (bytesReaded >= contentLength()) {
                    emitter.onNext(fileLoadEvent);
                    emitter.onComplete();
                } else {
                    emitter.onNext(fileLoadEvent);
                }
            }
            //RxBus.getDefault().post(FileManager.RXBUS_CODE, new FileLoadEvent(contentLength(), bytesReaded, tag));
            return bytesRead;
        }

        @Override
        public void subscribe(FlowableEmitter<FileLoadEvent> emitter) throws Exception {
            this.emitter = emitter;
        }
    }
}
