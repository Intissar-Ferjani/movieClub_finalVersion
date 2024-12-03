package com.example.movieclub;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "data")
public class DataClass {
    @PrimaryKey
    public int key;
    public String url;
    public String herourl;
    public String title;
    public String description;
    public boolean added;

    public DataClass(int key,String url, String herourl, String title, String description, boolean added) {
        this.key=key;
        this.url = url;
        this.herourl = herourl;
        this.title = title;
        this.description = description;
        this.added=added;
    }

    public int getKey() {
        return key;
    }

    public boolean isAdded() {
        return added;
    }

    public String getUrl() {
        return url;
    }

    public String getHerourl() {
        return herourl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {return key;}
}
