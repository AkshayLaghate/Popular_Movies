package com.nano.popularmovies;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    GridView gridview;
    ProgressDialog pd;
    ImageAdapter adapter;
    // contacts JSONArray
    JSONArray movies = null;
    // Hashmap for ListView
    ArrayList<HashMap<String, String>> movieList;
    ActionBar bar;
    private String url = urlPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bar = getSupportActionBar();
        bar.setTitle("Most Popular");
        adapter = new ImageAdapter(MainActivity.this);


        movieList = new ArrayList<HashMap<String, String>>();

        if (isNetworkOnline()) {
            new GetMovies().execute();
        } else {
            Toast.makeText(MainActivity.this, "Check Netwrok Connection and try again!",
                    Toast.LENGTH_SHORT).show();
        }

        gridview = (GridView) findViewById(R.id.gridview);


        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "" + movieList.get(position).get(TAG_NAME),
                        Toast.LENGTH_SHORT).show();
                showDeatils(position);
            }
        });


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
                    Toast.makeText(MainActivity.this, "Check Netwrok Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuSortNewest:

                movies = null;
                movieList.clear();


                url = urlPopular;
                if (isNetworkOnline()) {

                    new GetMovies().execute();
                    bar.setTitle("Most Popular");
                } else {
                    Toast.makeText(MainActivity.this, "Check Netwrok Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuSortRating:
                movies = null;
                movieList.clear();


                url = urlRating;
                if (isNetworkOnline()) {

                    new GetMovies().execute();
                    bar.setTitle("Highest Rated");
                } else {
                    Toast.makeText(MainActivity.this, "Check Netwrok Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }


        return super.onOptionsItemSelected(item);
    }

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

    public void showDeatils(int position) {

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);

        dialog.setContentView(R.layout.movie_detail);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);


        TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
        ImageView ivPoster = (ImageView) dialog.findViewById(R.id.ivPoster);
        TextView tvDesc = (TextView) dialog.findViewById(R.id.tvDesc);
        TextView tvRating = (TextView) dialog.findViewById(R.id.tvRating);
        TextView tvDate = (TextView) dialog.findViewById(R.id.tvDate);

        tvTitle.setText(movieList.get(position).get(TAG_NAME));
        ivPoster.setImageBitmap(imgs[position]);
        tvDesc.setText(movieList.get(position).get(TAG_DESCRIPTION));
        tvRating.setText(movieList.get(position).get(TAG_RATING));
        tvDate.setText(movieList.get(position).get(TAG_DATE));

        dialog.show();
    }

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
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            View view = null;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes

                 view = getLayoutInflater().inflate(R.layout.grid_item, null);
                TextView text = (TextView) view.findViewById(R.id.tvGrid);
                text.setText("Test");

                 imageView = (ImageView) view.findViewById(R.id.ivGrid);
                //imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

           // imageView.setImageResource(mThumbIds[position]);
            imageView.setImageBitmap(imgs[position]);
            return imageView;
/*
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {

                gridView = new View(mContext);

                // get layout from mobile.xml
                gridView = inflater.inflate(R.layout.grid_item, null);

                // set image based on selected text
                ImageView imageView = (ImageView) gridView
                        .findViewById(R.id.ivGrid);


                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                imageView.setPadding(8, 8, 8, 8);

                TextView tvGrid = (TextView) gridView.findViewById(R.id.tvGrid);
                tvGrid.setText( (position + 1)+". "+movieList.get(position).get(TAG_NAME));


                imageView.setImageBitmap(imgs[position]);


            } else {
                gridView = convertView;
            }

            return gridView;*/
        }


    }

    class LoadImgs extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub

            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading Movies Info..");
            pd.setCanceledOnTouchOutside(false);
            pd.setIndeterminate(false);
            pd.show();



        }

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub


            imgs = new Bitmap[movies.length()];

            for (int i = 0; i < movies.length(); i++) {

                try {
                    imgs[i] = Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w342/" + movieList.get(i).get(TAG_THUMBNAIL)).placeholder(R.drawable.default_placeholder).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub

            // adapter.notifyDataSetChanged();


            pd.dismiss();


            //gridview.invalidateViews();
            gridview.setAdapter(adapter);
            gridview.invalidateViews();



        }
    }

    private class GetMovies extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Connecting to server...");
            pd.setCancelable(false);
            pd.show();

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

                    // looping through All Contacts
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
            if (pd.isShowing())
                pd.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            new LoadImgs().execute();

        }

    }

}



