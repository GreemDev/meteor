/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.config.Config;
import net.greemdev.meteor.util.misc.JsonSerialization;
import net.greemdev.meteor.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.stream.Stream;

public class Http {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, JsonSerialization.jsonToDate())
        .create();

    private enum Method {
        GET,
        POST
    }

    public static class Request {
        private HttpRequest.Builder builder;
        private Method method;

        Request(Method method, String url) {
            try {
                this.builder = HttpRequest.newBuilder().uri(new URI(url)).header("User-Agent", Config.getUserAgent());
                this.method = method;
            } catch (URISyntaxException e) {
                MeteorClient.LOG.error("Invalid URI passed to an HTTP request", e);
            }
        }

        public Request bearer(String token) {
            builder.header("Authorization", "Bearer " + token);

            return this;
        }

        public Request bodyString(String string) {
            builder.header("Content-Type", "text/plain");
            builder.method(method.name(), HttpRequest.BodyPublishers.ofString(string));
            method = null;

            return this;
        }

        public Request bodyForm(String string) {
            builder.header("Content-Type", "application/x-www-form-urlencoded");
            builder.method(method.name(), HttpRequest.BodyPublishers.ofString(string));
            method = null;

            return this;
        }

        public Request bodyJson(String string) {
            builder.header("Content-Type", "application/json");
            builder.method(method.name(), HttpRequest.BodyPublishers.ofString(string));
            method = null;

            return this;
        }

        public Request bodyJson(Object object) {
            builder.header("Content-Type", "application/json");
            builder.method(method.name(), HttpRequest.BodyPublishers.ofString(GSON.toJson(object)));
            method = null;

            return this;
        }

        private <T> T _send(String accept, HttpResponse.BodyHandler<T> responseBodyHandler) {
            builder.header("Accept", accept);
            if (method != null) builder.method(method.name(), HttpRequest.BodyPublishers.noBody());

            try {
                var res = CLIENT.send(builder.build(), responseBodyHandler);
                return res.statusCode() == 200 ? res.body() : null;
            } catch (IOException | InterruptedException e) {
                MeteorClient.LOG.error("Error sending HTTP request", e);
                return null;
            }
        }

        public void send() {
            _send("*/*", HttpResponse.BodyHandlers.discarding());
        }

        public InputStream sendInputStream() {
            return _send("*/*", HttpResponse.BodyHandlers.ofInputStream());
        }

        public String sendString() {
            return _send("*/*", HttpResponse.BodyHandlers.ofString());
        }

        public Stream<String> sendLines() {
            return _send("*/*", HttpResponse.BodyHandlers.ofLines());
        }

        public <T> T sendJson(Type type) {
            return utils.let(_send("application/json", HttpResponse.BodyHandlers.ofInputStream()), in ->
                in != null
                    ? GSON.fromJson(new InputStreamReader(in), type)
                    : null
            );
        }
    }

    public static Request get(String url) {
        return new Request(Method.GET, url);
    }

    public static Request post(String url) {
        return new Request(Method.POST, url);
    }
}
