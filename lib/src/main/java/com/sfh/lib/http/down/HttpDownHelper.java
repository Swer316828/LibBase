package com.sfh.lib.http.down;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 功能描述:下载辅助
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/10/11
 */
public final class HttpDownHelper {

    public static class Builder {
        String url;
        ProgressListener listener;
        File tagFile;
        long readTimeout = 120L;
        long connectTimeout = 20L;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setTagFile(File tagFile) {
            this.tagFile = tagFile;
            return this;
        }

        public Builder setProgressListener(ProgressListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReadTimeout(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public File start() throws IOException {
            HttpDownHelper downHelper = new HttpDownHelper(connectTimeout,readTimeout);
            if (this.listener != null) {
                return downHelper.create(this.tagFile, this.url, this.listener);
            } else {
                return downHelper.create(this.tagFile, this.url);
            }
        }
    }

    HttpDownHelper(long readTimeout, long connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * 下载【无进度】
     *
     * @param tagFile
     * @param url
     * @return
     * @throws IOException
     */
    private File create(@NonNull File tagFile, @NonNull String url) throws IOException {

        Request request = new Request.Builder().url(url).build();
        ResponseBody body = buildResponseBody(request);

        BufferedSource bufferedSource = body.source();

        try {
            BufferedSink bufferedSink = Okio.buffer(Okio.sink(tagFile));
            long totalBytesRead = bufferedSink.writeAll(bufferedSource);
            bufferedSink.close();
        } finally {
            bufferedSource.close();
        }
        return tagFile;
    }

    /***
     * 断点下载【下载进度】
     * @param url
     * @param listener
     * @return
     * @throws IOException
     */
    private File create(@NonNull File tagFile, @NonNull String url, ProgressListener listener) throws IOException {

        long read = tagFile.exists() ? tagFile.length() : 0;
        Request request = new Request.Builder().url(url).addHeader("Accept-Encoding", "identity")
                .addHeader("RANGE", "bytes=" + read + "-").build();
        ResponseBody body = buildResponseBody(request);
        return writeFile(tagFile, body, listener);
    }

    private long readTimeout;
    private long connectTimeout;

    private ResponseBody buildResponseBody(Request request) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(this.readTimeout, TimeUnit.SECONDS);
        builder.connectTimeout(this.connectTimeout, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        return builder.build().newCall(request).execute().body();
    }

    private File writeFile(@NonNull File tagFile, ResponseBody body, ProgressListener listener) {

        BufferedSource bufferedSource = body.source();
        RandomAccessFile raf = null;
        try {
            // 随机访问文件，可以指定断点续传的起始位置
            raf = new RandomAccessFile(tagFile, "rwd");
            // 已经下载大小
            final long offest = raf.length();
            //指定断点续传的起始位置
            raf.seek(offest);

            //总大小
            final long allLength = body.contentLength() + offest;
            // 已经下载大小
            long totalBytesRead = offest;
            // 改变通知栏 百分比
            long loadSize = -1L;

            int bytesRead;
            byte buffer[] = new byte[8192];
            while ((bytesRead = bufferedSource.read(buffer)) != -1) {
                raf.write(buffer, 0, bytesRead);
                totalBytesRead += (bytesRead != -1 ? bytesRead : 0);

                long size = Math.abs((totalBytesRead * 100 / allLength));

                if (size != loadSize && listener != null) {
                    loadSize = size;
                    listener.onProgress(allLength, loadSize, totalBytesRead);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                bufferedSource.close();
            } catch (IOException e) {
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }
            }
        }
        return tagFile;

    }
}
