package com.codemetrics.codemetrics_generator.gen;
import com.codemetrics.codemetrics_generator.CodeMetricsConfigProperties;
import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

public class GeminiAPI {
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";

    public static String callGeminiAPI(String prompt, String API_KEY) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"contents\":[{\"role\": \"user\",\"parts\":[{\"text\": \"" + prompt + "\"}]}]}";

        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(ENDPOINT + "?key=" + API_KEY)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        // Parse JSON response to get the 'text' field
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        String text = jsonObject
                .getAsJsonArray("candidates")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();

        return text;
    }
}
