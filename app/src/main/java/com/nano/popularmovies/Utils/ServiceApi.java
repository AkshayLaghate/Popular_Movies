package com.nano.popularmovies.Utils;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Akki on 09/07/15.
 */
public class ServiceApi {


    public static TMDBService tmdbService;

    public static TMDBService getTMDBService() {
        if (tmdbService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://api.themoviedb.org")
                    .build();

            tmdbService = restAdapter.create(TMDBService.class);
        }
        return tmdbService;
    }

    public interface TMDBService {
        @GET("/3/discover/movie")
        MovieData listMovies(@Query("sort_by") String sort, @Query("api_key") String apiKey);
    }
}
