package com.mvcoder.download;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mvcoder.rxbus.RxBus;
import com.mvcoder.rxbus.Subscribe;
import com.mvcoder.rxbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * Created by miya95 on 2016/12/5.
 */
public abstract class FileCallBack<T> {

    private String destFileDir;
    private String destFileName;
    private String tag;

    public FileCallBack(@NonNull String destFileDir,@NonNull String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        //subscribeLoadProgress();
        RxBus.getDefault().register(this);
    }

    public void setTag(@NonNull String tag){
        this.tag = tag;
    }

    public abstract void onSuccess(T t);

    public abstract void progress(long progress, long total);

    public abstract void onStart();

    public abstract void onCompleted();

    public void onDispose(){}

    public abstract void onError(Throwable e);

    public void saveFile(ResponseBody body) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = body.byteStream();
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            unsubscribe();
            //onCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }
    }


    /**
     * 取消订阅，防止内存泄漏
     */
    public void unsubscribe() {
        RxBus.getDefault().unRegister(this);
    }

    @Subscribe(code = FileManager.RXBUS_CODE, threadMode = ThreadMode.MAIN)
    public void updateProgress(@NonNull FileLoadEvent fileLoadEvent){
        if(!tag.equals(fileLoadEvent.getTag())) return;
        progress(fileLoadEvent.getBytesLoaded(), fileLoadEvent.getTotal());
    }

}
