package com.mvcoder.downloadmanager;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.blankj.utilcode.constant.MemoryConstants;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.mvcoder.download.FileCallBack;
import com.mvcoder.download.FileManager;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DownloadActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
    }

    private boolean isFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
        if(isFirst){
            isFirst = false;
            Observable.just(0).delay(1,TimeUnit.SECONDS).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    downloadFile();
                }
            });
        }
    }

    @AfterPermissionGranted(100)
    private void downloadFile(){
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!EasyPermissions.hasPermissions(this, permissions)){
            EasyPermissions.requestPermissions(this, "", 100, permissions);
        }else{
            File root = Environment.getExternalStorageDirectory();
            String path = root.getAbsolutePath() + "/edu/download";
            File fileDirectory = new File(path);
            if(!fileDirectory.exists()){
                fileDirectory.mkdirs();
            }
            String downloadFilenname = "card.mp4";
            String url = "http://192.168.0.107:8080/card.mp4";
            FileManager.getInstance().download(url, new FileCallBack<ResponseBody>(fileDirectory.getAbsolutePath(), downloadFilenname) {
                @Override
                public void onSuccess(ResponseBody responseBody) {
                   log("下载完成 ：" + ConvertUtils.byte2MemorySize(responseBody.contentLength(),MemoryConstants.MB));
                }

                @Override
                public void progress(long progress, long total) {
                    String hadDownload = ConvertUtils.byte2MemorySize(progress, MemoryConstants.MB) + "m";
                    String totalSize = ConvertUtils.byte2MemorySize(total, MemoryConstants.MB) + "m";
                    log("下载了 : " + hadDownload + " , total :" + totalSize);
                }

                @Override
                public void onStart() {
                    log("开始下载");
                }

                @Override
                public void onCompleted() {
                    log("下载完成");
                }

                @Override
                public void onError(Throwable e) {
                    log("下载失败");
                }
            });
        }
    }

    private void log(String msg){
        Log.d("Download", msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
