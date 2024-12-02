package com.example.movieclub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class contentDetails extends AppCompatActivity {

    private AppDatabase database;
    private DAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_details);

        // Initialize the database
        database = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "AppDatabase"
        ).fallbackToDestructiveMigration().build();
        dao = database.dao();

        // Get the data from the Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String heroUrl = intent.getStringExtra("heroUrl");
        boolean isAddedToWatchlist = intent.getBooleanExtra("addedToWatchlist", false);
        int movieId = intent.getIntExtra("movieId", -1);

        // Bind data to views
        ImageView heroImage = findViewById(R.id.heroimage);
        TextView titleView = findViewById(R.id.title);
        TextView descriptionView = findViewById(R.id.description);
        ImageView heroImageView = findViewById(R.id.heroimage);
        MaterialButton watchlistButton = findViewById(R.id.watchlist);
        MaterialButton watchTrailerButton = findViewById(R.id.watchTrailor);

        titleView.setText(title);
        descriptionView.setText(description);
        Glide.with(this).load(heroUrl).into(heroImageView);

        // Update watchlist button state
        updateWatchlistButton(watchlistButton, isAddedToWatchlist);

        // Handle watchlist button click
        watchlistButton.setOnClickListener(view -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> {
                final boolean[] isAddedToWatchlistWrapper = {isAddedToWatchlist}; // Wrap in an array

                if (isAddedToWatchlistWrapper[0]) {
                    dao.deleteDataById(movieId);
                    runOnUiThread(() -> {
                        isAddedToWatchlistWrapper[0] = false;
                        updateWatchlistButton(watchlistButton, false);
                        Toast.makeText(contentDetails.this, "Removed from Watchlist", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    dao.insertOrUpdate(new DataClass(movieId, "", heroUrl, title, description, true));
                    runOnUiThread(() -> {
                        isAddedToWatchlistWrapper[0] = true;
                        updateWatchlistButton(watchlistButton, true);
                        Toast.makeText(contentDetails.this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                    });
                }

            });
        });

        watchTrailerButton.setOnClickListener(view -> {
            String trailerUrl = "https://www.youtube.com/results?search_query=" + title + "+trailer";
            Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            startActivity(trailerIntent);
        });


    }

    private void updateWatchlistButton(MaterialButton button, boolean isAdded) {
        if (isAdded) {
            button.setText("Added to Watchlist");
            button.setIconResource(R.drawable.baseline_check_24);
        } else {
            button.setText("Add to Watchlist");
            button.setIconResource(R.drawable.baseline_star_24);
        }
    }
}
