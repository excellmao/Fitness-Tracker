package com.example.fitnesstracker.database;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.example.fitnesstracker.BuildConfig;

public class SupabaseSync {
    public static void fetchExercises(Context context) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();

            // Build the HTTP Request with your Supabase keys
            Request request = new Request.Builder()
                    .url(BuildConfig.SUPABASE_URL)
                    .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonText = response.body().string();

                    // Convert the JSON text into a List of Exercise objects
                    Type listType = new TypeToken<List<Exercise>>(){}.getType();
                    List<Exercise> cloudExercises = gson.fromJson(jsonText, listType);

                    // Inject them into the local Room Database
                    FitnessDatabase db = FitnessDatabase.getInstance(context);

                    // Optional: Clear old exercises before inserting new ones to prevent duplicates
                    // db.routineDao().deleteAllExercises();

                    db.routineDao().insertExercises(cloudExercises);
                    Log.d("SupabaseSync", "Successfully synced " + cloudExercises.size() + " exercises!");

                } else {
                    Log.e("SupabaseSync", "Failed to fetch: " + response.code());
                }
            } catch (IOException e) {
                Log.e("SupabaseSync", "Network Error: " + e.getMessage());
            }
        }).start();
    }
}