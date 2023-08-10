package com.accenture.dansmarue.app;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.util.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by PK on 12/06/2017.
 */

public class UnsafeOkHttpGlideModule implements GlideModule {

    private static final String TAG = UnsafeOkHttpGlideModule.class.getName();

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((DansMaRueApplication) context));
    }

    public static class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {

        /**
         * The default factory for {@link OkHttpUrlLoader}s.
         */
        public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
            private static volatile OkHttpClient internalClient;
            private OkHttpClient client;

            private static OkHttpClient getInternalClient(DansMaRueApplication context) {
                if (internalClient == null) {
                    synchronized (Factory.class) {
                        if (internalClient == null) {
                            internalClient = context.getApplicationComponent().exposeOkHttpClient();
                        }
                    }
                }
                return internalClient;
            }

            /**
             * Constructor for a new Factory that runs requests using a static singleton client.
             */
            public Factory(DansMaRueApplication context) {
                this(getInternalClient(context));
            }

            /**
             * Constructor for a new Factory that runs requests using given client.
             */
            public Factory(OkHttpClient client) {
                this.client = client;
            }

            @Override
            public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
                return new OkHttpUrlLoader(client);
            }

            @Override
            public void teardown() {
                // Do nothing, this instance doesn't own the client.
            }
        }

        private final OkHttpClient client;

        public OkHttpUrlLoader(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
            return new OkHttpStreamFetcher(client, model);
        }

        public class OkHttpStreamFetcher implements DataFetcher<InputStream> {

            private final OkHttpClient client;
            private final GlideUrl url;
            private InputStream stream;
            private ResponseBody responseBody;

            public OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
                this.client = client;
                this.url = url;
            }

            @Override
            public InputStream loadData(Priority priority) throws Exception {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url.toStringUrl());

                for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
                    String key = headerEntry.getKey();
                    requestBuilder.addHeader(key, headerEntry.getValue());
                }

                Request request = requestBuilder.build();

                Response response = client.newCall(request).execute();
                responseBody = response.body();
                if (!response.isSuccessful()) {
                    throw new IOException("Request failed with code: " + response.code());
                }

                long contentLength = responseBody.contentLength();
                stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
                return stream;
            }

            @Override
            public void cleanup() {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.i(TAG, "Fail stream close");
                    }
                }
                if (responseBody != null) {
                    responseBody.close();
                }
            }

            @Override
            public String getId() {
                return url.getCacheKey();
            }

            @Override
            public void cancel() {
                // TODO: call cancel on the client when this method is called on a background thread. See #257
            }
        }
    }
}
