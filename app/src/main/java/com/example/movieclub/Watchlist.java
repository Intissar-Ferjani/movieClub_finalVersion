package com.example.movieclub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Watchlist extends AppCompatActivity {

    private AppDatabase database;
    private DAO dao_object;
    private RecyclerView watchlistRecyclerView;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        // Initialize the Room database
        database = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "AppDatabase"
        ).fallbackToDestructiveMigration().build();
        dao_object = database.dao();

        watchlistRecyclerView = findViewById(R.id.watchlistRecyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        setupTabLayout();
        loadWatchlistData();
    }

    private void setupTabLayout() {
        tabLayout.getTabAt(1).select(); // Select the Watchlist tab

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Home tab selected
                    Intent intent = new Intent(Watchlist.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadWatchlistData() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                List<DataClass> localData = dao_object.getAll();
                ArrayList<DataClass> arrayList = new ArrayList<>(localData);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Adapter smalladapter = new Adapter(Watchlist.this, arrayList, R.layout.itemposter_small);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(Watchlist.this, 3);
                        watchlistRecyclerView.setLayoutManager(gridLayoutManager);
                        watchlistRecyclerView.setAdapter(smalladapter);
                        smalladapter.setOnItemClickListener(new Adapter.onItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                showMovieDetails(arrayList.get(position));
                            }


                        });
                    }
                });
            }
        });
    }

    private void showMovieDetails(DataClass movie) {
        // Create an intent to navigate to ContentDetails activity
        Intent intent = new Intent(Watchlist.this, contentDetails.class);

        // Pass movie data through the intent
        intent.putExtra("title", movie.getTitle()); // Pass the title of the movie
        intent.putExtra("description", movie.getDescription()); // Pass the description of the movie
        intent.putExtra("heroUrl", movie.getHerourl()); // Pass the hero image URL
        intent.putExtra("addedToWatchlist", true); // Assuming it's already in the watchlist
        intent.putExtra("movieId", movie.getId()); // Pass the movie ID

        // Start the ContentDetails activity
        startActivity(intent);
    }

}

