/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.csc311hw4;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author ngoot
 */
public class Song {
    @SerializedName("ID")
    public int ID;
    @SerializedName("Title")
    public String Title;
    @SerializedName("Artist")
    public String Artist;
    @SerializedName("Genre")
    public String Genre;
    @SerializedName("Duration")
    public String Duration;
    
    
    
    public int getID(){
        return ID;
    }
    
    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String Artist) {
        this.Artist = Artist;
    }
    
    public void setGenre(String Genre){
        this.Genre = Genre;
    }
    
    public String getGenre(){
        return Genre;
    }
    
    public String getDuration() {
        return Duration;
    }
    
    public void setDuration(String Duration) {
        this.Duration = Duration;
    }
    
    public void setID(int ID){
        this.ID = ID;
    }
    public Song(int ID, String Title, String Artist, String Genre, String Duration) {
        this.ID = ID;
        this.Title = Title;
        this.Artist = Artist;
        this.Genre = Genre;
        this.Duration = Duration;
    }
    /**
     * Converts duration string to int for filtering.
     * @return Duration as int
     */
    public int getDurationInSeconds(){
        String[] split = Duration.split(":");
        int minutes = Integer.parseInt(split[0]);
        int seconds = Integer.parseInt(split[1]);
        return minutes * 60 + seconds;
    }
    
    
    @Override
    public String toString() {
        return "ID: " + ID + ", Title: " + Title + ", Artist: " + Artist + " Genre: " + Genre + ", Duration: " + Duration;
        
    }
}
