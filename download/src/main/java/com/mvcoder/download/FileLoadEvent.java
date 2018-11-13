package com.mvcoder.download;

import android.support.annotation.NonNull;

/**
 * 文件加载事件
 */
public class FileLoadEvent {

    private String tag;
    private long total;
    private long bytesLoaded;

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public long getTotal() {
        return total;
    }

    /**
     * 文件加载事件的构造函数.
     * @param total 文件总大小
     * @param bytesLoaded 已加载文件的大小
     */
    public FileLoadEvent(long total, long bytesLoaded, @NonNull String tag) {
        this.total = total;
        this.bytesLoaded = bytesLoaded;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
