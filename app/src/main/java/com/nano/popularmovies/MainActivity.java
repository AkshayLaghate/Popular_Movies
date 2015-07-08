package com.nano.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;


public class MainActivity extends AppCompatActivity {

    // JSON Node names
    private static final String TAG_RESULTS = "results";
    private static final String TAG_ID = "id";

    private static final String TAG_NAME = "original_title";
    private static final String TAG_DESCRIPTION = "overview";
    private static final String TAG_THUMBNAIL = "poster_path";
    private static final String TAG_POPULARITY = "popularity";
    private static final String TAG_RATING = "vote_average";
    private static final String TAG_DATE = "release_date";

    private static String urlPopular = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=3545a57a2f23dac5f3a1a0ddb84aa0df";
    private static String urlRating = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&api_key=3545a57a2f23dac5f3a1a0ddb84aa0df";
    Bitmap[] imgs;


    StaggeredGridView sgridView;

    ProgressDialog pd;
    ImageAdapter adapter;
    // contacts JSONArray
    JSONArray movies = null;
    // Hashmap for ListView
    ArrayList<HashMap<String, String>> movieList;
    ActionBar bar;
    DisplayMetrics metrics;
    int height, width;
    Configuration config;

    SwipeRefreshLayout swipe;

    TinyDB tiny;

    ArrayList<byte[]> poster_list;
    ArrayList<Card> cards = new ArrayList<Card>();
    private String url = urlPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        metrics = this.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;


        bar = getSupportActionBar();
        bar.setTitle("Most Popular");
        adapter = new ImageAdapter(MainActivity.this);

        pd = new ProgressDialog(MainActivity.this);

        pd.setCancelable(false);

        sgridView = (StaggeredGridView) findViewById(R.id.grid_view);


        movieList = new ArrayList<HashMap<String, String>>();

        poster_list = new ArrayList<>();

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipe.setColorSchemeResources(R.color.teal_700, R.color.indigo_700);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkOnline()) {

                    new GetMovies().execute();
                } else {
                    Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (isNetworkOnline()) {
            new GetMovies().execute();
        } else {
            Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                    Toast.LENGTH_SHORT).show();
        }

        //gridview = (GridView) findViewById(R.id.gridview);
        //mGridView = (CardGridStaggeredView) findViewById(R.id.carddemo_extras_grid_stag);




     /*   config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            gridview.setNumColumns(4);
        } else {

            gridview.setNumColumns(2);
        }*/

        sgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                //showDetails(position);
                openDetails(position);
            }
        });



        /*gridview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


            }
        });*/

    }

    private void openDetails(int position) {

        Bundle bag = new Bundle();
        bag.putString("movie_id", movieList.get(position).get(TAG_ID));
        bag.putString("movie_title", movieList.get(position).get(TAG_NAME));
        bag.putString("movie_desc", movieList.get(position).get(TAG_DESCRIPTION));
        bag.putString("movie_release", movieList.get(position).get(TAG_DATE));
        bag.putString("movie_rating", movieList.get(position).get(TAG_RATING));
        bag.putString("poster_path", movieList.get(position).get(TAG_THUMBNAIL));

        Intent i = new Intent("com.nano.popularmovies.DETAILS");

        i.putExtras(bag);

        startActivity(i);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {


            case R.id.refresh:

                if (isNetworkOnline()) {
                    new GetMovies().execute();
                } else {
                    Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuSortNewest:

                item.setChecked(true);

                movies = null;
                movieList.clear();


                url = urlPopular;
                if (isNetworkOnline()) {

                    new GetMovies().execute();
                    bar.setTitle("Most Popular");
                } else {
                    Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();

                }

                break;

            case R.id.menuSortRating:

                item.setChecked(true);
                movies = null;
                movieList.clear();


                url = urlRating;
                if (isNetworkOnline()) {

                    new GetMovies().execute();
                    bar.setTitle("Highest Rated");
                } else {
                    Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuSortFav:

                item.setChecked(true);

                imgs = null;
                movieList.clear();

                new LoadFavs().execute();
                bar.setTitle("Favourites");
                break;
        }


        return super.onOptionsItemSelected(item);
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

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        gridview.setVisibility(View.INVISIBLE);

        width = metrics.widthPixels;
        height = metrics.heightPixels;

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            gridview.setNumColumns(4);
            if (isNetworkOnline()) {
                new GetMovies().execute();
            } else {
                Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {


            gridview.setNumColumns(2);
            if (isNetworkOnline()) {
                new GetMovies().execute();
            } else {
                Toast.makeText(MainActivity.this, "Check Network Connection and try again!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @Override
    public void onResume() {

        //sgridView.setSelection(index);
        super.onResume();

    }

    @Override
    public void onPause() {
        // index = sgridView.getFirstVisiblePosition();
        super.onPause();

    }

    // Adapter for gridview
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        // references to our images


        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return imgs.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {


            convertView = null;


            if (convertView == null) {
                // if it's not recycled, initialize some attributes


                LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cardlib_card, null);


                // get layout from mobile.xml

                //v.setLayoutParams(new GridView.LayoutParams(width / 2, height / 2));

            }


            CardViewNative cardView = (CardViewNative) convertView.findViewById(R.id.carddemo_largeimage);


            MaterialLargeImageCard card =
                    MaterialLargeImageCard.with(getApplicationContext())
                            .setTitle(movieList.get(position).get(TAG_NAME))
                            .setSubTitle("Rating : " + movieList.get(position).get(TAG_RATING) + "/10")
                            .useDrawableExternal(new MaterialLargeImageCard.DrawableExternal() {
                                @Override
                                public void setupInnerViewElements(ViewGroup viewGroup, View mview) {

                                    Drawable d = new BitmapDrawable(getResources(), imgs[position]);

                                    mview.setBackground(d);
                                }
                            }).build();


            cardView.setCard(card);

            return convertView;


        }

        /*public View getView(int position, View convertView, ViewGroup parent) {


            ImageView imageView;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);

                imageView.setLayoutParams(new GridView.LayoutParams(width / 2, height / 2));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(imgs[position]);
            return imageView;


        }*/


    }

    // Load movie posters in background thread
    class LoadImgs extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub

            imgs = new Bitmap[movies.length()];

            for (int i = 0; i < movies.length(); i++) {

                try {
                    imgs[i] = Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185/" + movieList.get(i).get(TAG_THUMBNAIL)).placeholder(R.drawable.default_placeholder).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub

            swipe.setRefreshing(false);
            sgridView.setAdapter(adapter);
            sgridView.invalidateViews();
            sgridView.setVisibility(View.VISIBLE);


            //new LoadCards().execute();

        }
    }

    // Make api call to tMDB and get JSON data in background thread
    private class GetMovies extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            sgridView.setVisibility(View.INVISIBLE);
            swipe.post(new Runnable() {
                @Override
                public void run() {
                    swipe.setRefreshing(true);
                }
            });



        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    movies = jsonObj.getJSONArray(TAG_RESULTS);

                    // looping through all results
                    for (int i = 0; i < movies.length(); i++) {
                        JSONObject c = movies.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);

                        String desc = c.getString(TAG_DESCRIPTION);
                        String thumb = c.getString(TAG_THUMBNAIL);
                        String pouplarity = c.getString(TAG_POPULARITY);
                        String rating = c.getString(TAG_RATING);
                        String date = c.getString(TAG_DATE);

                        // tmp hashmap for single movie
                        HashMap<String, String> movie = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        movie.put(TAG_ID, id);
                        movie.put(TAG_NAME, name);
                        movie.put(TAG_DESCRIPTION, desc);
                        movie.put(TAG_THUMBNAIL, thumb);
                        movie.put(TAG_POPULARITY, pouplarity);
                        movie.put(TAG_RATING, rating);
                        movie.put(TAG_DATE, date);


                        // adding movie to movie list
                        movieList.add(movie);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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


            new LoadImgs().execute();

        }

    }

    private class LoadCards extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pd.setMessage("Setting up the UI.....");

        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < movieList.size(); i++) {

                final int finalI = i;
                MaterialLargeImageCard card =
                        MaterialLargeImageCard.with(getApplicationContext())
                                .setTitle(movieList.get(i).get(TAG_NAME).toString())
                                .useDrawableExternal(new MaterialLargeImageCard.DrawableExternal() {
                                    @Override
                                    public void setupInnerViewElements(ViewGroup viewGroup, View mview) {

                                        Drawable d = new BitmapDrawable(getResources(), imgs[finalI]);

                                        mview.setBackground(d);
                                    }
                                }).build();

                cards.add(card);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {



            /*gridview.setAdapter(adapter);
            gridview.invalidateViews();
            gridview.setVisibility(View.VISIBLE);


*/
            pd.dismiss();
            sgridView.setAdapter(adapter);
            sgridView.invalidateViews();
            sgridView.setVisibility(View.VISIBLE);
        }

    }


    private class LoadFavs extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            tiny = new TinyDB(MainActivity.this);
            pd.setMessage("Loading Movies Info....");

        }

        @Override
        protected Void doInBackground(Void... params) {
            // Retrieve student records
            String URL = "content://com.nano.provider.popularmovies/movies";

            Uri movies = Uri.parse(URL);
            Cursor c = getContentResolver().query(movies, null, null, null, "name");
            ArrayList<byte[]> thumbList = new ArrayList<>();

            if (c.moveToFirst()) {
                do {


                    HashMap<String, String> movie = new HashMap<>();
                    movie.put(TAG_ID, c.getString(c.getColumnIndex(MovieProvider.ID)));
                    movie.put(TAG_NAME, c.getString(c.getColumnIndex(MovieProvider.NAME)));
                    movie.put(TAG_DESCRIPTION, c.getString(c.getColumnIndex(MovieProvider.DESCRIPTION)));


                    movie.put(TAG_RATING, c.getString(c.getColumnIndex(MovieProvider.RATING)));
                    movie.put(TAG_DATE, c.getString(c.getColumnIndex(MovieProvider.DATE)));


                    // adding movie to movie list
                    movieList.add(movie);
                    thumbList.add(c.getBlob(7));


                } while (c.moveToNext());
            }

            imgs = new Bitmap[movieList.size()];
            for (int i = 0; i < movieList.size(); i++) {
                imgs[i] = DBBitmapUtility.getImage(thumbList.get(i));
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            pd.dismiss();
            sgridView.setAdapter(adapter);
            sgridView.invalidateViews();
            sgridView.setVisibility(View.VISIBLE);

        }


    }


}



