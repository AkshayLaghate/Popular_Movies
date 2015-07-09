package com.nano.popularmovies.Utils;

/**
 * Created by Akki on 09/07/15.
 */

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result {

    public boolean adult;
    @SerializedName("backdrop_path")
    public String backdropPath;
    @SerializedName("genre_ids")
    public List<Integer> genreIds = new ArrayList<Integer>();
    public int id;
    @SerializedName("original_language")
    public String originalLanguage;
    @SerializedName("original_title")
    public String originalTitle;
    public String overview;
    @SerializedName("release_date")
    public String releaseDate;
    @SerializedName("poster_path")
    public String posterPath;
    public float popularity;
    public String title;
    public boolean video;
    @SerializedName("vote_average")
    public float voteAverage;
    @SerializedName("vote_count")
    public int voteCount;
    public Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The adult
     */
    public boolean isAdult() {
        return adult;
    }

    /**
     * @param adult The adult
     */
    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    /**
     * @return The backdropPath
     */
    public String getBackdropPath() {
        return backdropPath;
    }

    /**
     * @param backdropPath The backdrop_path
     */
    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    /**
     * @return The genreIds
     */
    public List<Integer> getGenreIds() {
        return genreIds;
    }

    /**
     * @param genreIds The genre_ids
     */
    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    /**
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The originalLanguage
     */
    public String getOriginalLanguage() {
        return originalLanguage;
    }

    /**
     * @param originalLanguage The original_language
     */
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    /**
     * @return The originalTitle
     */
    public String getOriginalTitle() {
        return originalTitle;
    }

    /**
     * @param originalTitle The original_title
     */
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    /**
     * @return The overview
     */
    public String getOverview() {
        return overview;
    }

    /**
     * @param overview The overview
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * @return The releaseDate
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate The release_date
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * @return The posterPath
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * @param posterPath The poster_path
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * @return The popularity
     */
    public float getPopularity() {
        return popularity;
    }

    /**
     * @param popularity The popularity
     */
    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The video
     */
    public boolean isVideo() {
        return video;
    }

    /**
     * @param video The video
     */
    public void setVideo(boolean video) {
        this.video = video;
    }

    /**
     * @return The voteAverage
     */
    public float getVoteAverage() {
        return voteAverage;
    }

    /**
     * @param voteAverage The vote_average
     */
    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    /**
     * @return The voteCount
     */
    public int getVoteCount() {
        return voteCount;
    }

    /**
     * @param voteCount The vote_count
     */
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}