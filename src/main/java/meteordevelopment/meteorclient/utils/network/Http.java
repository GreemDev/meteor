/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.json.DateDeserializer;

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
import java.util.function.Consumer;
import java.util.stream.Stream;

@Deprecated(since = "rev54", forRemoval = true)
public class Http {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new DateDeserializer())
        .create();

    private enum Method {
        // I have no idea why IntelliJ is screaming that this can't be accessed when it's private & yet that's literally how it's been this entire time & worked
        // changed to public just to shut IJ up
        GET,
        POST
    }

    public static class Request {
        private HttpRequest.Builder builder;
        private Method method;

        public Request(Method method, String url) {
            try {
                this.builder = HttpRequest.newBuilder().uri(new URI(url)).header("User-Agent", MeteorClient.NAME);
                this.method = method;
            } catch (URISyntaxException e) {
                e.printStackTrace();
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
            return bodyJson(GSON.toJson(object));
        }

        private <T> T _send(String accept, HttpResponse.BodyHandler<T> responseBodyHandler) {
            builder.header("Accept", accept);
            if (method != null) builder.method(method.name(), HttpRequest.BodyPublishers.noBody());

            try {
                var res = CLIENT.send(builder.build(), responseBodyHandler);
                return res.statusCode() == 200 ? res.body() : null;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void receiveInputStream(Consumer<InputStream> callback) {
            MeteorExecutor.execute(() -> callback.accept(sendInputStream()));
        }

        public void receiveString(Consumer<String> callback) {
            MeteorExecutor.execute(() -> callback.accept(sendString()));
        }

        public void receiveLines(Consumer<Stream<String>> callback) {
            MeteorExecutor.execute(() -> callback.accept(sendLines()));
        }

        public void receiveJson(Consumer<JsonObject> callback) {
            MeteorExecutor.execute(() -> callback.accept(sendJson()));
        }

        public <T> void receiveJson(Type type, Consumer<T> callback) {
            MeteorExecutor.execute(() -> callback.accept(sendJson(type)));
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

        public JsonObject sendJson() {
            return sendJson(JsonObject.class);
        }

        public <T> T sendJson(Type type) {
            InputStream in = _send("application/json", HttpResponse.BodyHandlers.ofInputStream());
            return in == null ? null : GSON.fromJson(new InputStreamReader(in), type);
        }
    }

    public static Request get(String url) {
        return new Request(Method.GET, url);
    }

    public static Request post(String url) {
        return new Request(Method.POST, url);
    }
}
