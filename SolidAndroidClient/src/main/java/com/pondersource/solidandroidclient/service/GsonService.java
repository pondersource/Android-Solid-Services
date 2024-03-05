package com.pondersource.solidandroidclient.service;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inrupt.client.spi.JsonService;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GsonService implements JsonService {

    private final Gson gson;

    public GsonService() {
        gson = new GsonBuilder()
                //.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }
    @Override
    public <T> void toJson(T object, @NonNull OutputStream output) throws IOException {
        String objectString = gson.toJson(object);
        output.write(objectString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public <T> T fromJson(InputStream input, Class<T> clazz) throws IOException {
        String str = inputStreamToString(input);
        return gson.fromJson(str, clazz);
    }

    @Override
    public <T> T fromJson(@NonNull InputStream input, Type type) throws IOException {
        return gson.fromJson(inputStreamToString(input), type);
    }

    private String inputStreamToString(InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }
}
