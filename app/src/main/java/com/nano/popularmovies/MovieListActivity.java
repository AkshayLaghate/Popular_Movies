package com.nano.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import butterknife.Bind;


/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MovieListFragment} and the item details
 * (if present) is a {@link MovieDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MovieListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MovieListActivity extends ActionBarActivity
        implements MovieListFragment.OnMovieSelectedListener {

    @Bind(R.id.toolbar)
    Toolbar bar;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((MovieListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.movie_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.

        bar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(bar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.refresh:

                if (isNetworkOnline()) {
                    new GetMovies().execute(query);
                } else {
                    Toast.makeText(MovieListActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.menuSortNewest:

                item.setChecked(true);

                movies = null;
                movieList.clear();

                query = query_Popular;

                if (isNetworkOnline()) {

                    new GetMovies().execute(query_Popular);
                    bar.setTitle("Most Popular");
                } else {
                    Toast.makeText(MovieListActivity.this, "Check Network Connection and try again!",
                            Toast.LENGTH_SHORT).show();

                }

                break;

            case R.id.menuSortRating:

                item.setChecked(true);
                movies = null;
                movieList.clear();

                query = query_Rating;

                if (isNetworkOnline()) {

                    new GetMovies().execute(query_Rating);
                    bar.setTitle("Highest Rated");
                } else {
                    Toast.makeText(MovieListActivity.this, "Check Network Connection and try again!",
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
    }*/

    /**
     * Callback method from {@link MovieListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */


    @Override
    public void onMovieSelected(String id, String name, String description, String date, String rating, String thumb) {
        Log.e("Name : ", "" + name);
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MovieDetailFragment.ARG_ITEM_ID, id);
            arguments.putString(MovieDetailFragment.ARG_MOVIE_NAME, name);
            arguments.putString(MovieDetailFragment.ARG_MOVIE_DESC, description);
            arguments.putString(MovieDetailFragment.ARG_MOVIE_DATE, date);
            arguments.putString(MovieDetailFragment.ARG_MOVIE_RATING, rating);
            arguments.putString(MovieDetailFragment.ARG_MOVIE_THUMB, thumb);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MovieDetailActivity.class);
            detailIntent.putExtra(MovieDetailFragment.ARG_ITEM_ID, name);
            startActivity(detailIntent);
        }
    }
}
