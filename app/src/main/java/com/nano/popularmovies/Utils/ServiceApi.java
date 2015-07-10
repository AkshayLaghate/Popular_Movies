package com.nano.popularmovies.Utils;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Akki on 09/07/15.
 */
public class ServiceApi {



    public static TMDBService tmdbService;
    public static MovieService movieService;

    public static TMDBService getTMDBService() {
        if (tmdbService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://api.themoviedb.org")
                    .build();

            tmdbService = restAdapter.create(TMDBService.class);
        }
        return tmdbService;
    }

    public static MovieService getMovieService() {
        if (movieService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://api.themoviedb.org")
                    .build();

            movieService = restAdapter.create(MovieService.class);
        }
        return movieService;
    }

    public interface TMDBService {

        @GET("/3/discover/movie")
        MovieData listMovies(@Query("sort_by") String sort, @Query("api_key") String apiKey);
    }

    public interface MovieService {

        @GET("/3/discover/movie")
        MovieData movieDetails(@Query("sort_by") String sort, @Query("api_key") String apiKey);
    }

}
