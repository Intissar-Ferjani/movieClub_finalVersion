package com.example.movieclub;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Movie extends AppCompatActivity {

    private RecyclerView movieRecyclerView;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        // Initialize UI elements
        movieRecyclerView = findViewById(R.id.movieRecyclerView);
        movieRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(this);

        // Load movie data
        loadData();
    }

    private void loadData() {
        String collection = "Movie"; // Collection type for movies
        ArrayList<DataClass> arrayList = new ArrayList<>();
        Adapter smallAdapter = new Adapter(this, arrayList, R.layout.itemposter_small);

        movieRecyclerView.setAdapter(smallAdapter);

        // URL for fetching movies
        GetURL getURL = new GetURL();
        String url = getURL.fetch(collection);

        // Base URL for images
        String baseImageUrl = "https://image.tmdb.org/t/p/original";

        // Create the Volley StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray results = jsonObject.getJSONArray("results");

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject movie = results.getJSONObject(i);

                                int key = movie.getInt("id");
                                String imgUrl = baseImageUrl + movie.getString("poster_path");
                                String heroUrl = baseImageUrl + movie.getString("backdrop_path");
                                String title = movie.optString("title", "Unknown Title");
                                String description = movie.getString("overview");

                                // Add the data to the array list
                                arrayList.add(new DataClass(key, imgUrl, heroUrl, title, description, false));
                            }

                            // Notify the adapter of data changes
                            smallAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Movie.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Movie.this, "Failed to load movies", Toast.LENGTH_SHORT).show();
                Log.e("Movie", "Volley Error: " + error.getMessage());
            }
        });

        // Add the request to the queue
        requestQueue.add(stringRequest);
    }
}
