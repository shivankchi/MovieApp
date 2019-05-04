package com.twtech.movieapp.MovieDetails;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.twtech.movieapp.Movie;
import com.twtech.movieapp.R;
import com.twtech.movieapp.databaseSQLITE.MovieContract;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment {

    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    public static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";

    private Movie mMovie;
    private ShareActionProvider mShareActionProvider;
    private LiveData<List<Movie>> movies;
    public static List<Movie> updated_list;   //This is updated favorite movie list

    RecyclerView mRecyclerViewTrailers;
    RecyclerView mRecyclerViewReviews;

    @BindView(R.id.movie_title)
    TextView mMovieTitleView;
    @BindView(R.id.movie_overview)
    TextView mMovieOverviewView;
    @BindView(R.id.movie_release_date)
    TextView mMovieReleaseDateView;
    @BindView(R.id.movie_user_rating)
    TextView mMovieRatingView;
    @BindView(R.id.movie_poster)
    ImageView mMoviePosterView;
    @BindView(R.id.button_mark_as_favorite)
    Button mButtonMarkAsFavorite;
    @BindView(R.id.button_remove_from_favorites)
    Button mButtonRemoveFromFavorites;

    @BindViews({R.id.rating_first_star, R.id.rating_second_star, R.id.rating_third_star, R.id.rating_fourth_star, R.id.rating_fifth_star})
    List<ImageView> ratingStarViews;

    public MovieDetailFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
                activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null && activity instanceof MovieDetailActivity) {
            appBarLayout.setTitle(mMovie.getOriginalTitle());
        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));
        if (movieBackdrop != null) {
            Picasso.get()
                    .load(mMovie.getBackdropPath())
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_details, container, false);
        ButterKnife.bind(this, rootView);

        mMovieTitleView.setText(mMovie.getOriginalTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate());
        Picasso.get()
                .load(mMovie.getPosterPath())
                .config(Bitmap.Config.RGB_565)
                .into(mMoviePosterView);

        update_rating_stars();
        updateFavorites();
        Log.d(LOG_TAG, MovieContract.MovieEntry.CONTENT_URI.toString());
        /*IF savedInstanceState == null*/

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateFavorites();
    }


    private void update_rating_stars() {
        if (mMovie.getVoteAverage() != null && !mMovie.getVoteAverage().isEmpty()) {
            String user_rating_star = getResources().getString(R.string.movie_user_rating,
                    mMovie.getVoteAverage());
            mMovieRatingView.setText(user_rating_star);
            float user_rating = Float.valueOf(mMovie.getVoteAverage()) / 2;
            int integerPart = (int) user_rating;
            // Fill stars
            for (int i = 0; i < integerPart; i++) {
                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_black_24dp);
            }
            // Fill half star
            if (Math.round(user_rating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(
                        R.drawable.ic_star_half_black_24dp);
            }
        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
    }


    public void mark_as_favorite() {
        Log.d(LOG_TAG, "Calling check for favorite method");


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!check_for_favorite()) {
                   /*
                    //Store movie object to the database
                    Log.d(LOG_TAG, "So creating new movieEntry object and storing it!");
                    final Date date = new Date();
                    MovieEntry movieEntry = new MovieEntry(
                            mMovie.getId(),
                            mMovie.getVoteAverage(),
                            mMovie.getOriginalTitle(),
                            mMovie.getBackdropPath(),
                            mMovie.getOverview(),
                            mMovie.getReleaseDate(),
                            mMovie.getPosterPath(),
                            date);
                    mDb.movieDao().insertMovie(movieEntry);
                    Log.d(LOG_TAG, "Was not marked as Favorite, so Marked it!");
                  */
                   ContentValues movie_data = new ContentValues();
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            mMovie.getId());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,mMovie.getVoteAverage());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,mMovie.getOriginalTitle());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH, mMovie.getBackdropPath());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, mMovie.getOverview());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, mMovie.getReleaseDate());
                    movie_data.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH, mMovie.getPosterPath());
                    getContext().getContentResolver().insert(
                            MovieContract.MovieEntry.CONTENT_URI,
                            movie_data
                    );
                    Log.d(LOG_TAG, "Was not marked as Favorite, so Marked it!");

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Log.d(LOG_TAG, "Calling uupdate Favorites, inside markasfavorite");
                updateFavorites();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void remove_from_favorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (check_for_favorite()) {
                    //mDb.movieDao().deleteMovieById(mMovie.getId());

                    getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavorites();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void updateFavorites() {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Log.d(LOG_TAG, "Calling uupdate Favorites, inside updateFavorites");
                return check_for_favorite();
            }

            @Override
            protected void onPostExecute(Boolean favorite) {
                Log.d(LOG_TAG, "Inside update favorite: " + favorite);
                if (favorite) {
                    mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
                    mButtonMarkAsFavorite.setVisibility(View.GONE);
                } else {
                    mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
                    mButtonRemoveFromFavorites.setVisibility(View.GONE);
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mark_as_favorite();
                    }
                });

        mButtonRemoveFromFavorites.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove_from_favorites();
                    }
                });

    }

    private boolean check_for_favorite() {
        //Check the database if this is already in the list
        //Using SQLite
            Cursor movieCursor = getContext().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                    null,
                    null);

            if (movieCursor != null && movieCursor.moveToFirst()) {
                movieCursor.close();
                return true;
            } else {
                return false;
            }
    }


}