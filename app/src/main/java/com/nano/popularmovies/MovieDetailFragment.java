package com.nano.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


    /**
     * The dummy content this fragment is presenting.
     */

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_new, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getArguments().getString(ARG_MOVIE_NAME));
        // Show the dummy content as text in a TextView.
        // ((TextView) rootView.findViewById(R.id.movie_detail)).setText(getArguments().getString(ARG_ITEM_ID)+"\n"+getArguments().getString(ARG_MOVIE_NAME));

        return rootView;
    }
}
