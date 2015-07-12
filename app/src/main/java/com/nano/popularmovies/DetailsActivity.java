package com.nano.popularmovies;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nano.popularmovies.Utils.DBBitmapUtility;
import com.nano.popularmovies.Utils.MovieProvider;
import com.nano.popularmovies.Utils.TinyDB;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Akki on 19/06/15.
 */
public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_POSTER = "backdrops";
    private static final String TAG_REVIEW = "results"; // works for both reviews and videos api call
    private static final String TAG_KEY = "key";
    private static final String TAG_NAME = "name";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_CONTENT = "content";
    Toolbar bar;
    AppBarLayout appBar;
    CollapsingToolbarLayout collapsingToolbar;
    FloatingActionButton fab;
    ScrollView scrollView;
    String movie_id, name, description, date, rating, poster_path, review;
    ImageView ivPoster, ivThumb, ivTeaser, ivTrailer;
    TextView tvTitle, tvDesc, tvDate, tvRating, tvReview, tvTrailer, tvTeaser;
    JSONArray dataArray = null;
    ArrayList<String> posterList, favMovies;
    ArrayList<HashMap<String, String>> reviews, videos;


    Bitmap poster;
    Bitmap thumb;

    TinyDB tiny;

    DisplayMetrics metrics;
    int height, width;

    boolean isFav, loadingComplete = false;
    MenuItem fav;

    ProgressDialog pd;

    byte[] big, thumbDB;

    SystemBarTintManager tintManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        setContentView(R.layout.detail_new);

        tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);


        bar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(bar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        appBar = (AppBarLayout) findViewById(R.id.appbar);


        Bundle bag = getIntent().getExtras();
        movie_id = bag.getString("movie_id");
        name = bag.getString("movie_title");
        description = bag.getString("movie_desc");
        date = bag.getString("movie_release");
        rating = bag.getString("movie_rating");
        poster_path = bag.getString("poster_path");


        /*metrics = this.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;*/

        /*bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setTitle("Movie Details");*/

        tiny = new TinyDB(this);
        posterList = new ArrayList<>();
        reviews = new ArrayList<>();
        videos = new ArrayList<>();
        favMovies = new ArrayList<>();

        favMovies = tiny.getListString("movies");

        isFav = checkFav();

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        ivPoster = (ImageView) findViewById(R.id.colHeader);
        ivPoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivTeaser = (ImageView) findViewById(R.id.ivTeaser);
        ivTrailer = (ImageView) findViewById(R.id.ivTrailerThumb);
        ivThumb = (ImageView) findViewById(R.id.ivThumbNew);
        ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);


        //tvTitle = (TextView) findViewById(R.id.tvPosterLabel);
        tvDesc = (TextView) findViewById(R.id.description_data);
        tvDate = (TextView) findViewById(R.id.tvDateNew);
        tvRating = (TextView) findViewById(R.id.tvRatingNew);
        tvReview = (TextView) findViewById(R.id.reviews_details);
        tvTrailer = (TextView) findViewById(R.id.tvTrailer);
        tvTeaser = (TextView) findViewById(R.id.tvTeaser);

        collapsingToolbar.setTitle(name);
        // tvTitle.setText(name);
        tvDesc.setText(description);
        tvDate.setText("Release Date : " + date);
        tvRating.setText("Rating : " + rating + "/10");


        if (isNetworkOnline()) {

            new GetPoster().execute();
            new GetReviews().execute();
            new GetVideos().execute();
        } else {

            if (isFav) {
                new LoadOfflineData().execute();
            }

        }


        ivTeaser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videos.get(0).get(TAG_KEY)));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(DetailsActivity.this, "No Teaser Available!", Toast.LENGTH_SHORT).show();
                    Log.e("error", e.toString());
                }

            }
        });

        ivTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videos.get(1).get(TAG_KEY)));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(DetailsActivity.this, "No Trailer Available!", Toast.LENGTH_SHORT).show();
                    Log.e("error", e.toString());
                }
            }
        });


        if (isFav) {

            fab.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.star_filled));

        }

    }


    // Check if network connection is available
    public boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    private boolean checkFav() {

        for (int i = 0; i < favMovies.size(); i++) {
            if (favMovies.get(i).equals(movie_id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        fav = menu.findItem(R.id.addToFav);


        if (isFav) {
            fav.setTitle("Remove from Favourites");

        }


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:

                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);

                break;

            case R.id.addToFav:

                if (loadingComplete) {
                    if (isFav) {

                        fav.setTitle("Add to Favourites");

                        favMovies.remove(movie_id);
                        removeFromFav();
                    } else {

                        fav.setTitle("Remove from Favourites");

                        favMovies.add(movie_id);
                        saveMovieToFav();
                    }
                } else
                    Toast.makeText(this, "Loading details... Wait some time", Toast.LENGTH_SHORT).show();


                tiny.putListString("movies", favMovies);


                break;

            case R.id.searchYTS:
                new SerachYTS().execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveMovieToFav() {


        byte[] thumb_array = DBBitmapUtility.getBytes(thumb);
        byte[] poster_array = DBBitmapUtility.getBytes(poster);
        // Add a new movie record
        ContentValues values = new ContentValues();

        values.put(MovieProvider.ID, movie_id);
        values.put(MovieProvider.NAME,
                name);
        values.put(MovieProvider.DESCRIPTION, description);
        values.put(MovieProvider.DATE, tvDate.getText().toString());
        values.put(MovieProvider.RATING, rating);
        values.put(MovieProvider.REVIEW, tvReview.getText().toString());
        values.put(MovieProvider.POSTER, thumb_array);
        values.put(MovieProvider.BIG, poster_array);
        values.put(MovieProvider.POSTER_PATH, poster_path);


        Uri uri = getContentResolver().insert(
                MovieProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(),
                "Added to favourites", Toast.LENGTH_LONG).show();

    }

    private void removeFromFav() {

        int rows = getContentResolver().delete(MovieProvider.CONTENT_URI, "id=?", new String[]{movie_id});
        Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    public void generatePallete(Bitmap bmp) {

        Palette.from(bmp).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                collapsingToolbar.setContentScrimColor(mutedColor);
                tintManager.setTintColor(mutedColor);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (loadingComplete) {
                    if (isFav) {

                        fab.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.star_unfilled));

                        favMovies.remove(movie_id);
                        removeFromFav();
                    } else {

                        fab.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.star_filled));

                        favMovies.add(movie_id);
                        saveMovieToFav();
                    }
                } else
                    Toast.makeText(this, "Loading details... Wait some time", Toast.LENGTH_SHORT).show();


                tiny.putListString("movies", favMovies);
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ivPoster.setImageBitmap(Bitmap.createScaledBitmap(poster, ivPoster.getWidth(), ivPoster.getHeight(), false));

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ivPoster.setImageBitmap(Bitmap.createScaledBitmap(poster, ivPoster.getWidth(), ivPoster.getHeight(), false));

        }
    }

    private class GetPoster extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            /*ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall("http://api.themoviedb.org/3/movie/" + movie_id + "/images?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df", ServiceHandler.GET);*/


            String url = "http://api.themoviedb.org/3/movie/" + movie_id + "/images?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df";
            String jsonStr = null;

            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                jsonStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Response", "Error: " + e);
            }
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    dataArray = jsonObj.getJSONArray(TAG_POSTER);

                    // looping through all results
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject c = dataArray.getJSONObject(i);


                        String thumb = c.getString("file_path");


                        posterList.add(thumb);
                    }

                    try {


                        thumb = Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185/" + poster_path).placeholder(R.drawable.default_placeholder).get();
                        poster = Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w342/" + posterList.get(0)).placeholder(R.drawable.default_placeholder).get();


                    } catch (Exception e) {

                        //poster = DBBitmapUtility.getImage();
                        e.printStackTrace();

                    }


                } catch (JSONException e) {
                    Log.w("error", e.toString());

                }
            } else {
                Log.w("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            /**
             * Updating parsed JSON data into ListView
             * */


            generatePallete(poster);
            ivPoster.setImageBitmap(Bitmap.createScaledBitmap(poster, ivPoster.getWidth(), ivPoster.getHeight(), false));

            if (thumb != null)
                ivThumb.setImageBitmap(Bitmap.createScaledBitmap(thumb, ivThumb.getWidth(), ivThumb.getHeight(), false));


            //scrollView.setBackground(poster_bg_drawable);


        }

    }

    private class GetReviews extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            /*ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall("http://api.themoviedb.org/3/movie/" + movie_id + "/reviews?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df", ServiceHandler.GET);
*/
            String url = "http://api.themoviedb.org/3/movie/" + movie_id + "/reviews?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df";
            String jsonStr = null;

            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                jsonStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Response", "Error: " + e);
            }
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    dataArray = jsonObj.getJSONArray(TAG_REVIEW);

                    // looping through all results
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject c = dataArray.getJSONObject(i);


                        String author = c.getString(TAG_AUTHOR);

                        String content = c.getString(TAG_CONTENT);


                        // tmp hashmap for single movie
                        HashMap<String, String> review = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        review.put(TAG_AUTHOR, author);
                        review.put(TAG_CONTENT, content);


                        // adding movie to movie list
                        reviews.add(review);
                    }
                } catch (JSONException e) {
                    Log.e("error", e.toString());

                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            /**
             * Updating parsed JSON data into ListView
             * */


            if (reviews.size() > 0) {

                StringBuilder reviewData = new StringBuilder();
                for (int i = 0; i < reviews.size(); i++) {
                    reviewData.append("By " + reviews.get(i).get(TAG_AUTHOR) + " : " + '\n' + reviews.get(i).get(TAG_CONTENT) + '\n' + '\n');
                }
                tvReview.setText(reviewData);
            } else {
                tvReview.setText("No reviews yet.");
            }

            loadingComplete = true;
        }

    }

    private class GetVideos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            /*ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall("http://api.themoviedb.org/3/movie/" + movie_id + "/videos?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df", ServiceHandler.GET);
*/

            String url = "http://api.themoviedb.org/3/movie/" + movie_id + "/videos?api_key=3545a57a2f23dac5f3a1a0ddb84aa0df";
            String jsonStr = null;

            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                jsonStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Response", "Error: " + e);
            }
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    dataArray = jsonObj.getJSONArray(TAG_REVIEW);

                    // looping through all results
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject c = dataArray.getJSONObject(i);


                        String key = c.getString(TAG_KEY);

                        String name = c.getString(TAG_NAME);


                        // tmp hashmap for single movie
                        HashMap<String, String> video = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        video.put(TAG_KEY, key);
                        video.put(TAG_NAME, name);


                        // adding movie to movie list
                        videos.add(video);
                    }
                } catch (JSONException e) {
                    Log.e("error", e.toString());

                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            /**
             * Updating parsed JSON data into ListView
             * */


            try {

                tvTeaser.setText(videos.get(0).get(TAG_NAME));
                tvTrailer.setText(videos.get(1).get(TAG_NAME));

                Picasso.with(getApplicationContext()).load("http://img.youtube.com/vi/" + videos.get(0).get(TAG_KEY) + "/default.jpg").placeholder(R.drawable.default_placeholder).into(ivTeaser);
                Picasso.with(getApplicationContext()).load("http://img.youtube.com/vi/" + videos.get(1).get(TAG_KEY) + "/default.jpg").placeholder(R.drawable.default_placeholder).into(ivTrailer);

            } catch (Exception e) {
                Log.e("error", e.toString());
            }


        }

    }

    private class SerachYTS extends AsyncTask<Void, Void, Void> {


        String key;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(DetailsActivity.this);
            pd.setMessage("Searching torrents...");
            pd.setCancelable(false);
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {




            /*ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);*/

            String url = "https://getstrike.net/api/v2/torrents/search/?phrase=" + name.replace(" ", "%20");
            String jsonStr = null;

            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                jsonStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Response", "Error: " + e);
            }

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    dataArray = jsonObj.getJSONArray("torrents");

                    JSONObject data = dataArray.getJSONObject(0);


                    key = data.getString("magnet_uri");


                    Log.e("magnet", key);


                } catch (JSONException e) {
                    Log.e("error", e.toString());

                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
            super.onPostExecute(aVoid);

            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(key));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(DetailsActivity.this, "No Torrent found", Toast.LENGTH_SHORT).show();
                Log.e("error", e.toString());
            }
        }
    }

    private class LoadOfflineData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Cursor c = getContentResolver().query(MovieProvider.CONTENT_URI, null, MovieProvider.ID + "=?", new String[]{movie_id}, null);

            if (c.moveToFirst()) {
                do {


                    review = c.getString(c.getColumnIndex(MovieProvider.REVIEW));
                    big = c.getBlob(9);
                    thumbDB = c.getBlob(8);


                } while (c.moveToNext());
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            loadingComplete = true;
            tvReview.setText(review);
            ivThumb.setImageBitmap(DBBitmapUtility.getImage(thumbDB));
            ivPoster.setImageBitmap(DBBitmapUtility.getImage(big));
            tvTeaser.setText("Not Available Offline");
            tvTrailer.setText("Not Available Offline");
            ivTrailer.setVisibility(View.GONE);
            ivTeaser.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }
    }

}


