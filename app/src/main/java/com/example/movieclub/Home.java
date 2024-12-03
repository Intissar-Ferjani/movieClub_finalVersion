package com.example.movieclub;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Home extends AppCompatActivity {

    private AppDatabase database;
    private DAO dao_object;
    private TabLayout tabLayout;
    private BlurView bottomBlurView;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeDatabase();
        queue = Volley.newRequestQueue(this);
        initView();
        setupBlurView();
        setupTabLayout();
        loadInitialData();
        setupButtons();
    }

    private void initializeDatabase() {
        database = Room.databaseBuilder(this, AppDatabase.class, "AppDatabase")
                .fallbackToDestructiveMigration()
                .build();
        dao_object = database.dao();
    }

    private void initView() {
        tabLayout = findViewById(R.id.tabLayout);
        bottomBlurView = findViewById(R.id.bottomBlurView);
    }

    private void setupBlurView() {
        final float radius = 25f;
        final Drawable windowBackground = getWindow().getDecorView().getBackground();
        BlurAlgorithm algorithm = getBlurAlgorithm();

        bottomBlurView.setupWith(findViewById(R.id.main), algorithm)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
    }

    private void setupTabLayout() {
        tabLayout.getTabAt(0).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(Home.this, R.color.md_theme_light_primary);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }
                if (tab.getPosition() == 1) {
                    Intent intent = new Intent(Home.this, Watchlist.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int tabIconColor = ContextCompat.getColor(Home.this, R.color.md_theme_light_onPrimary);
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(tabIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadInitialData() {
        loadData("Trending", R.id.trendingRVIEW);
        loadData("NetflixOriginals", R.id.netflixoriginalsRVIEW);
        loadData("TopRated", R.id.topratedRVIEW);
        loadData("ActionMovies", R.id.actionmoviesRVIEW);
        loadData("ComedyMovies", R.id.comedymoviesRVIEW);
        loadData("HorrorMovies", R.id.horrormoviesRVIEW);
        loadData("RomanceMovies", R.id.romancemoviesRVIEW);
    }

    private void setupButtons() {
        ImageButton profile = findViewById(R.id.profile);
        MaterialButton movie = findViewById(R.id.movies);
        MaterialButton tv = findViewById(R.id.tvshows);
        ImageButton search = findViewById(R.id.search);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);
            }
        });

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Movie.class);
                startActivity(intent);
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, TVShow.class);
                startActivity(intent);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, SearchActivity.class);
                startActivity(intent);
            }
        });

    }

    private void loadData(String collection, int rviewid) {
        ArrayList<DataClass> arrayList = new ArrayList<>();
        Adapter adapter = new Adapter(Home.this, arrayList, R.layout.itemposter);
        RecyclerView recyclerView = findViewById(rviewid);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            DataClass movie = arrayList.get(position);
            Intent intent = new Intent(Home.this, contentDetails.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("heroUrl", movie.getHerourl());
            intent.putExtra("addedToWatchlist", movie.isAdded());
            intent.putExtra("movieId", movie.getKey());
            intent.putExtra("contentType", collection);
            startActivity(intent);
        });

        fetchMovieData(new GetURL().fetch(collection), arrayList, adapter);
    }

    private void fetchMovieData(String url, ArrayList<DataClass> arrayList, Adapter adapter) {
        String baseimageurl = "https://image.tmdb.org/t/p/original";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject movies = jsonArray.getJSONObject(i);
                            int key = movies.getInt("id");
                            String imgurl = baseimageurl + movies.getString("poster_path");
                            String herourl = baseimageurl + movies.getString("backdrop_path");
                            String title = movies.optString("title", movies.optString("name", movies.optString("original_title")));
                            String description = movies.getString("overview");

                            DataClass data = new DataClass(key, imgurl, herourl, title, description, false);
                            arrayList.add(data);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(Home.this, "ERROR", Toast.LENGTH_SHORT).show());

        queue.add(stringRequest);
    }


    private BlurAlgorithm getBlurAlgorithm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new RenderEffectBlur();
        } else {
            return new RenderScriptBlur(this);
        }
    }
}
