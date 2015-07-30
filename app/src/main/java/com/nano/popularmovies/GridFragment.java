package com.nano.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.nano.popularmovies.Utils.DBBitmapUtility;
import com.nano.popularmovies.Utils.MovieData;
import com.nano.popularmovies.Utils.MovieProvider;
import com.nano.popularmovies.Utils.Result;
import com.nano.popularmovies.Utils.ServiceApi;
import com.nano.popularmovies.Utils.TinyDB;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;
import it.gmariotti.cardslib.library.view.CardViewNative;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GridFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "original_title";
    private static final String TAG_DESCRIPTION = "overview";
    private static final String TAG_THUMBNAIL = "poster_path";
    private static final String TAG_POPULARITY = "popularity";
    private static final String TAG_RATING = "vote_average";
    private static final String TAG_DATE = "release_date";
    Bitmap[] imgs;
    @Bind(R.id.toolbar)
    Toolbar bar;
    @Bind(R.id.grid_view)
    StaggeredGridView sgridView;
    ProgressDialog pd;
    ImageAdapter adapter;
    JSONArray movies = null;
    ArrayList<HashMap<String, String>> movieList;
    DisplayMetrics metrics;
    int height, width;
    SwipeRefreshLayout swipe;
    TinyDB tiny;
    ArrayList<byte[]> poster_list;
    List<Result> results;
    String query_Rating = "vote_average.desc";
    String query_Popular = "popularity.desc";
    String query = query_Popular;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private String api_key = "3545a57a2f23dac5f3a1a0ddb84aa0df";

    public GridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GridFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GridFragment newInstance(String param1, String param2) {
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        // Toast.makeText(this, "Memory Available = " + Runtime.getRuntime().maxMemory() / 1000, Toast.LENGTH_SHORT).show();


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_grid, container, false);

        metrics = this.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(bar);
        bar.setTitle("Most Popular");

        ButterKnife.bind(this, v);
        adapter = new ImageAdapter(getActivity().getApplicationContext());

        pd = new ProgressDialog(getActivity().getApplicationContext());

        pd.setCancelable(false);

        movieList = new ArrayList<HashMap<String, String>>();

        poster_list = new ArrayList<>();

        swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipe.setColorSchemeResources(R.color.pink_A400, R.color.indigo_700);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkOnline()) {

                    new GetMovies().execute(query);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (isNetworkOnline()) {
            new GetMovies().execute(query_Popular);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Check Network Connection and try again!",
                    Toast.LENGTH_SHORT).show();
        }


        sgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                openDetails(position);
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            convertView = null;

            if (convertView == null) {
                // if it's not recycled, initialize some attributes


                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cardlib_card, parent, false);

            }


            CardViewNative cardView = (CardViewNative) convertView.findViewById(R.id.carddemo_largeimage);


            MaterialLargeImageCard card =
                    MaterialLargeImageCard.with(getActivity().getApplicationContext())
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

            imgs = new Bitmap[movieList.size()];

            for (int i = 0; i < movieList.size(); i++) {

                try {

                    imgs[i] = Picasso.with(getActivity().getApplicationContext()).load("http://image.tmdb.org/t/p/w185/" + movieList.get(i).get(TAG_THUMBNAIL)).placeholder(R.drawable.default_placeholder).get();

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

        }
    }

    // Make api call to tMDB and get JSON data in background thread
    private class GetMovies extends AsyncTask<String, Void, Void> {


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
        protected Void doInBackground(String... params) {

            MovieData md = ServiceApi.getTMDBService().listMovies(params[0], api_key);
            results = md.getResults();

            for (int i = 0; i < results.size(); i++) {


                // tmp hashmap for single movie
                HashMap<String, String> movie = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                movie.put(TAG_ID, String.valueOf(results.get(i).getId()));
                movie.put(TAG_NAME, results.get(i).getTitle());
                movie.put(TAG_DESCRIPTION, results.get(i).getOverview());
                movie.put(TAG_THUMBNAIL, results.get(i).getPosterPath());
                movie.put(TAG_POPULARITY, String.valueOf(results.get(i).getPopularity()));
                movie.put(TAG_RATING, String.valueOf(results.get(i).getVoteAverage()));
                movie.put(TAG_DATE, results.get(i).getReleaseDate());


                // adding movie to movie list
                movieList.add(movie);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            new LoadImgs().execute();

        }

    }


    private class LoadFavs extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            tiny = new TinyDB(getActivity().getApplicationContext());
            pd.setMessage("Loading Movies Info....");

        }

        @Override
        protected Void doInBackground(Void... params) {

            String URL = "content://com.nano.provider.popularmovies/movies";

            Uri movies = Uri.parse(URL);
            Cursor c = getActivity().getContentResolver().query(movies, null, null, null, "name");
            ArrayList<byte[]> thumbList = new ArrayList<>();

            if (c.moveToFirst()) {
                do {

                    HashMap<String, String> movie = new HashMap<>();
                    movie.put(TAG_ID, c.getString(c.getColumnIndex(MovieProvider.ID)));
                    movie.put(TAG_NAME, c.getString(c.getColumnIndex(MovieProvider.NAME)));
                    movie.put(TAG_DESCRIPTION, c.getString(c.getColumnIndex(MovieProvider.DESCRIPTION)));
                    movie.put(TAG_THUMBNAIL, c.getString(c.getColumnIndex(MovieProvider.POSTER_PATH)));

                    movie.put(TAG_RATING, c.getString(c.getColumnIndex(MovieProvider.RATING)));
                    movie.put(TAG_DATE, c.getString(c.getColumnIndex(MovieProvider.DATE)));


                    // adding movie to movie list
                    movieList.add(movie);
                    thumbList.add(c.getBlob(8));


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
