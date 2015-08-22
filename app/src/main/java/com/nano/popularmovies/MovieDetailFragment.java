package com.nano.popularmovies;

import android.app.Dialog;
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
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "movie_id";
    public static final String ARG_MOVIE_NAME = "movie_name";
    public static final String ARG_MOVIE_DESC = "movie_desc";
    public static final String ARG_MOVIE_DATE = "movie_date";
    public static final String ARG_MOVIE_RATING = "movie_rating";
    public static final String ARG_MOVIE_THUMB = "movie_thumb";

    private static final String TAG_POSTER = "backdrops";
    private static final String TAG_REVIEW = "results"; // works for both reviews and videos api call
    private static final String TAG_KEY = "key";
    private static final String TAG_NAME = "name";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_CONTENT = "content";


    @Bind(R.id.appbar)
    AppBarLayout appBar;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.colHeader)
    ImageView ivPoster;
    @Bind(R.id.ivThumbNew)
    ImageView ivThumb;
    @Bind(R.id.description_data)
    TextView tvDesc;
    @Bind(R.id.tvDateNew)
    TextView tvDate;
    @Bind(R.id.tvRatingNew)
    TextView tvRating;
    @Bind(R.id.reviews_details)
    TextView tvReview;
    @Bind(R.id.lvVids)
    ListView lvVids;

    JSONArray dataArray = null;
    ArrayList<String> posterList, favMovies;
    ArrayList<HashMap<String, String>> reviews, videos, torrents;

    String movie_id, name, description, date, rating, poster_path, review;

    Bitmap poster;
    Bitmap thumb;

    TinyDB tiny;

    boolean isFav, loadingComplete = false;

    ProgressDialog pd;

    byte[] big, thumbDB;

    SystemBarTintManager tintManager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * The dummy content this fragment is presenting.
     */

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_new, container, false);


        // Show the dummy content as text in a TextView.
        // ((TextView) rootView.findViewById(R.id.movie_detail)).setText(getArguments().getString(ARG_ITEM_ID)+"\n"+getArguments().getString(ARG_MOVIE_NAME));
        ButterKnife.bind(this, rootView);
        tintManager = new SystemBarTintManager(getActivity());
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(getActivity(), "Loading details... Wait some time", Toast.LENGTH_SHORT).show();


                tiny.putListString("movies", favMovies);
            }
        });

        movie_id = getArguments().getString(ARG_ITEM_ID);
        name = getArguments().getString(ARG_MOVIE_NAME);
        description = getArguments().getString(ARG_MOVIE_DESC);
        date = getArguments().getString(ARG_MOVIE_DATE);
        rating = getArguments().getString(ARG_MOVIE_RATING);
        poster_path = getArguments().getString(ARG_MOVIE_THUMB);

        Log.e("Id :", movie_id);

        tiny = new TinyDB(getActivity());
        posterList = new ArrayList<>();
        reviews = new ArrayList<>();
        videos = new ArrayList<>();
        favMovies = new ArrayList<>();
        torrents = new ArrayList<>();

        favMovies = tiny.getListString("movies");

        isFav = checkFav();

        ivPoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);

        lvVids.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videos.get(position).get(TAG_KEY)));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Video not available!", Toast.LENGTH_SHORT).show();
                    Log.e("error", e.toString());
                }
            }
        });
        lvVids.setVisibility(View.INVISIBLE);

        collapsingToolbar.setTitle(name);

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

        if (isFav) {
            fab.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.star_filled));
        }

        return rootView;

    }

    // Check if network connection is available
    public boolean isNetworkOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void saveMovieToFav() {

        byte[] thumb_array = DBBitmapUtility.getBytes(thumb);
        byte[] poster_array = DBBitmapUtility.getBytes(poster);
        // Add a new movie record
        ContentValues values = new ContentValues();

        values.put(MovieProvider.ID, movie_id);
        values.put(MovieProvider.NAME,
                name);
        values.put(MovieProvider.DESCRIPTION, description);
        values.put(MovieProvider.DATE, date);
        values.put(MovieProvider.RATING, rating);
        values.put(MovieProvider.REVIEW, tvReview.getText().toString());
        values.put(MovieProvider.POSTER, thumb_array);
        values.put(MovieProvider.BIG, poster_array);
        values.put(MovieProvider.POSTER_PATH, poster_path);

        Uri uri = getActivity().getContentResolver().insert(
                MovieProvider.CONTENT_URI, values);

        Toast.makeText(getActivity(),
                "Added to favourites", Toast.LENGTH_LONG).show();
    }

    private void removeFromFav() {

        int rows = getActivity().getContentResolver().delete(MovieProvider.CONTENT_URI, "id=?", new String[]{movie_id});
        Toast.makeText(getActivity(), "Removed from favourites", Toast.LENGTH_SHORT).show();

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    private void showTorList() {

        final Dialog dialog = new Dialog(getActivity());


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.tor_list);

        ListView lv = (ListView) dialog.findViewById(R.id.lvTor);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(torrents.get(position).get("tor_magnet")));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "No Torrent found", Toast.LENGTH_SHORT).show();
                    Log.e("error", e.toString());
                }
            }
        });

        lv.setAdapter(new TorListAdapter());

        dialog.show();

    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            Log.w("Total vids : ", "=" + videos.size());
            return videos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.custom_row, parent, false);
            }

            TextView tvList = (TextView) v.findViewById(R.id.tvList);
            tvList.setText(videos.get(position).get(TAG_NAME));


            return v;
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


                        thumb = Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + poster_path).placeholder(R.drawable.default_placeholder).get();
                        poster = Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342/" + posterList.get(0)).placeholder(R.drawable.default_placeholder).get();


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

            if (poster != null) {
                generatePallete(poster);
                ivPoster.setImageBitmap(Bitmap.createScaledBitmap(poster, ivPoster.getWidth(), ivPoster.getHeight(), false));
            }


            if (thumb != null)
                ivThumb.setImageBitmap(Bitmap.createScaledBitmap(thumb, ivThumb.getWidth(), ivThumb.getHeight(), false));


        }

    }

    private class GetReviews extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            tvReview.setText("Loading...");

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

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

                        HashMap<String, String> review = new HashMap<String, String>();

                        review.put(TAG_AUTHOR, author);
                        review.put(TAG_CONTENT, content);

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

                        HashMap<String, String> video = new HashMap<String, String>();

                        video.put(TAG_KEY, key);
                        video.put(TAG_NAME, name);

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

            lvVids.setAdapter(new ListViewAdapter());
            setListViewHeightBasedOnChildren(lvVids);
            lvVids.setVisibility(View.VISIBLE);
        }

    }

    private class LoadOfflineData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Cursor c = getActivity().getContentResolver().query(MovieProvider.CONTENT_URI, null, MovieProvider.ID + "=?", new String[]{movie_id}, null);

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
            super.onPostExecute(aVoid);
        }
    }

    private class SerachYTS extends AsyncTask<Void, Void, Void> {

        String key;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Searching torrents...");
            pd.setCancelable(false);
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

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

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject data = dataArray.getJSONObject(i);


                        String title = data.getString("torrent_title");
                        String seeds = data.getString("seeds");
                        String leeches = data.getString("leeches");
                        String size = data.getString("size");
                        key = data.getString("magnet_uri");

                        HashMap<String, String> torrent = new HashMap<>();
                        torrent.put("tor_title", title);
                        torrent.put("tor_seeds", seeds);
                        torrent.put("tor_leeches", leeches);
                        torrent.put("tor_size", size);
                        torrent.put("tor_magnet", key);

                        torrents.add(torrent);
                        Log.e("magnet", key);


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
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
            super.onPostExecute(aVoid);
            if (torrents.size() > 0) {
                showTorList();
            } else {
                Toast.makeText(getActivity(), "No Torrent found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class TorListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return torrents.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.tor_row, parent, false);

            }


            TextView tvTitle = (TextView) v.findViewById(R.id.tvTorTitle);
            TextView tvSize = (TextView) v.findViewById(R.id.tvTorSize);
            TextView tvSeeds = (TextView) v.findViewById(R.id.tvTorSeeds);
            TextView tvLeeches = (TextView) v.findViewById(R.id.tvTorLeeches);

            tvTitle.setText(torrents.get(position).get("tor_title"));
            tvSize.setText(humanReadableByteCount(Long.parseLong(torrents.get(position).get("tor_size")), true));
            tvSeeds.setText(torrents.get(position).get("tor_seeds"));
            tvLeeches.setText(torrents.get(position).get("tor_leeches"));

            return v;
        }
    }
}
