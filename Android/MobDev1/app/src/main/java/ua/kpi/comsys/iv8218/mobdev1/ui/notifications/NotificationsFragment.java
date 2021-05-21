package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.daimajia.swipe.SwipeLayout;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ua.kpi.comsys.iv8218.mobdev1.R;

public class NotificationsFragment extends Fragment {

    private static HashMap<SwipeLayout, Movie> moviesMap;
    private static LinearLayout movieList;
    private static View root;
    private static TextView noItems;
    private static ProgressBar loadingBar;
    private static Set<SwipeLayout> removeSet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_notifications, container, false);
        setRetainInstance(true);
        movieList = root.findViewById(R.id.scroll_lay);
        moviesMap = new HashMap<>();

        noItems = root.findViewById(R.id.no_movies_view);
        loadingBar = root.findViewById(R.id.loading_bar);

        removeSet = new HashSet<>();

        SearchView searchView = root.findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                removeSet.addAll(moviesMap.keySet());
                if (query.length() >= 3) {
                    AsyncLoadMovies aTask = new AsyncLoadMovies();
                    loadingBar.setVisibility(View.VISIBLE);
                    noItems.setVisibility(View.GONE);
                    aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
                }
                else {
                    for (SwipeLayout swipeLayout : removeSet) {
                        binClicked(swipeLayout);
                    }
                    removeSet.clear();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                removeSet.addAll(moviesMap.keySet());
                if (query.length() >= 3) {
                    AsyncLoadMovies aTask = new AsyncLoadMovies();
                    loadingBar.setVisibility(View.VISIBLE);
                    noItems.setVisibility(View.GONE);
                    aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
                }
                else {
                    for (SwipeLayout swipeLayout : removeSet) {
                        binClicked(swipeLayout);
                    }
                    removeSet.clear();
                }
                return false;
            }
        });

        Button btnAddMovie = root.findViewById(R.id.button_add_movie);
        btnAddMovie.setOnClickListener(v -> {
            MovieAdd popUpClass = new MovieAdd();
            Object[] popups = popUpClass.showPopupWindow(v);

            View popupView = (View) popups[0];
            PopupWindow popupWindow = (PopupWindow) popups[1];

            EditText inputTitle = popupView.findViewById(R.id.input_title);
            EditText inputYear = popupView.findViewById(R.id.input_type);
            EditText inputType = popupView.findViewById(R.id.input_year);

            Button buttonAdd = popupView.findViewById(R.id.button_add_add);
            buttonAdd.setOnClickListener(v1 -> {
                if (inputTitle.getText().toString().length() != 0 &&
                        inputYear.getText().toString().length() != 0 &&
                        inputType.getText().toString().length() != 0) {

                    addNewMovie(new Movie(inputTitle.getText().toString(),
                            inputYear.getText().toString(), "",
                            inputType.getText().toString(), ""));
                    changeLaySizes();

                    popupWindow.dismiss();
                }
                else{
                    Toast.makeText(getActivity(), "You must fill all fields!",
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        changeLaySizes();

        return root;
    }

    protected static void loadMovies(ArrayList<Movie> movies){
        for (SwipeLayout swipeLayout : removeSet) {
            binClicked(swipeLayout);
        }
        if (movies != null) {
            removeSet.clear();
            if (movies.size() > 0) {
                noItems.setVisibility(View.GONE);
                for (Movie movie :
                        movies) {
                    addNewMovie(movie);
                }
            } else {
                noItems.setVisibility(View.VISIBLE);
            }
        }
        else {
            noItems.setVisibility(View.VISIBLE);
            Toast.makeText(root.getContext(), "Cannot load data!", Toast.LENGTH_LONG).show();
        }
        loadingBar.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        changeLaySizes();
    }

    private void changeLaySizes(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) root.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        for (SwipeLayout movieshelf :
                moviesMap.keySet()) {
            movieshelf.getChildAt(0).setLayoutParams(
                    new ConstraintLayout.LayoutParams(width/3, width/3));
        }
    }

    public static void binClicked(SwipeLayout swipeLayout){
        moviesMap.remove(swipeLayout);
        movieList.removeView(swipeLayout);
        if (moviesMap.keySet().isEmpty()){
            noItems.setVisibility(View.VISIBLE);
        }
    }

    private static void addNewMovie(Movie movie){
        SwipeLayout swipeLay = new SwipeLayout(root.getContext());
        swipeLay.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLay.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
        movieList.addView(swipeLay);

        ImageButton btnBin = new ImageButton(root.getContext());
        btnBin.setPadding(50, 0, 50, 0);
        btnBin.setBackgroundColor(Color.RED);
        btnBin.setImageResource(R.drawable.ic_delete_white_48dp);

        LinearLayout.LayoutParams btnBinParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        btnBinParams.gravity = Gravity.RIGHT;
        swipeLay.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLay.addView(btnBin, 0, btnBinParams);

        ConstraintLayout movieLayTmp = new ConstraintLayout(root.getContext());
        movieLayTmp.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        swipeLay.addView(movieLayTmp);

        btnBin.setOnClickListener(v -> binClicked(swipeLay));
        movieLayTmp.setOnClickListener(v -> {
            if (movie.getImdbID().length() != 0 && !movie.getImdbID().equals("noid")) {
                MovieInfo popUpClass = new MovieInfo();
                popUpClass.showPopupWindow(v, movie);
            }
        });

        ProgressBar loadingImageBar = new ProgressBar(root.getContext());
        loadingImageBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(root.getContext(), R.color.purple_500),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        loadingImageBar.setVisibility(View.GONE);
        loadingImageBar.setId(loadingImageBar.hashCode());
        movieLayTmp.addView(loadingImageBar);

        ImageView imageTmp = new ImageView(root.getContext());
        imageTmp.setId(imageTmp.hashCode());
        ConstraintLayout.LayoutParams imgParams =
                new ConstraintLayout.LayoutParams(300, 300);
        if (movie.getPoster().length() != 0)
            imageTmp.setImageResource(
                    getResId(movie.getPoster().toLowerCase()
                            .split("\\.")[0], R.drawable.class));
        imageTmp.setImageResource(R.drawable.no_image);
        if (movie.getPoster().length() != 0){
            imageTmp.setVisibility(View.INVISIBLE);
            loadingImageBar.setVisibility(View.VISIBLE);
            new DownloadImageTask(imageTmp, loadingImageBar, root.getContext()).execute(movie.getPoster());
        }
        movieLayTmp.addView(imageTmp, 0, imgParams);

        ConstraintLayout textConstraint = new ConstraintLayout(root.getContext());
        textConstraint.setId(textConstraint.hashCode());
        movieLayTmp.addView(textConstraint, 1);

        TextView textTitle = new TextView(root.getContext());
        textTitle.setId(textTitle.hashCode());
        textTitle.setPadding(0, 1, 5, 1);
        textTitle.setText(movie.getTitle());
        textTitle.setEllipsize(TextUtils.TruncateAt.END);
        textTitle.setMaxLines(4);
        ConstraintLayout.LayoutParams textTitleParams =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textConstraint.addView(textTitle, 0, textTitleParams);

        TextView textYear = new TextView(root.getContext());
        textYear.setText(movie.getYear());
        textYear.setPadding(0, 1, 5, 1);
        textYear.setId(textYear.hashCode());
        ConstraintLayout.LayoutParams textYearParams =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textConstraint.addView(textYear, 1, textYearParams);

        TextView textType = new TextView(root.getContext());
        textType.setText(movie.getType());
        textType.setPadding(0, 0, 5, 4);
        textType.setId(textType.hashCode());
        ConstraintLayout.LayoutParams textTypeParams =
                new ConstraintLayout.LayoutParams(ConstraintSet.WRAP_CONTENT,
                        ConstraintSet.WRAP_CONTENT);
        textConstraint.addView(textType, 2, textTypeParams);

        ConstraintSet textConstraintSet = new ConstraintSet();
        textConstraintSet.clone(textConstraint);

        textConstraintSet.connect(textTitle.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        textConstraintSet.connect(textTitle.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START);
        textConstraintSet.connect(textYear.getId(), ConstraintSet.TOP,
                textTitle.getId(), ConstraintSet.BOTTOM);
        textConstraintSet.connect(textYear.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START);
        textConstraintSet.connect(textYear.getId(), ConstraintSet.BOTTOM,
                textType.getId(), ConstraintSet.TOP);
        textConstraintSet.connect(textType.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        textConstraintSet.connect(textType.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START);

        textConstraintSet.setVerticalBias(textYear.getId(), 0.3f);

        textConstraintSet.setMargin(textYear.getId(), ConstraintSet.TOP, 3);
        textConstraintSet.setMargin(textYear.getId(), ConstraintSet.BOTTOM, 3);

        textConstraintSet.applyTo(textConstraint);

        ConstraintSet movieLayTmpSet = new ConstraintSet();
        movieLayTmpSet.clone(movieLayTmp);

        movieLayTmpSet.connect(imageTmp.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START);
        movieLayTmpSet.connect(imageTmp.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        movieLayTmpSet.connect(imageTmp.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        movieLayTmpSet.connect(textConstraint.getId(), ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END);
        movieLayTmpSet.connect(textConstraint.getId(), ConstraintSet.TOP,
                ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        movieLayTmpSet.connect(textConstraint.getId(), ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        movieLayTmpSet.connect(textConstraint.getId(), ConstraintSet.START,
                imageTmp.getId(), ConstraintSet.END);

        movieLayTmpSet.connect(loadingImageBar.getId(), ConstraintSet.START,
                imageTmp.getId(), ConstraintSet.START);
        movieLayTmpSet.connect(loadingImageBar.getId(), ConstraintSet.TOP,
                imageTmp.getId(), ConstraintSet.TOP);
        movieLayTmpSet.connect(loadingImageBar.getId(), ConstraintSet.END,
                imageTmp.getId(), ConstraintSet.END);
        movieLayTmpSet.connect(loadingImageBar.getId(), ConstraintSet.BOTTOM,
                imageTmp.getId(), ConstraintSet.BOTTOM);

        movieLayTmpSet.constrainWidth(textConstraint.getId(), ConstraintSet.MATCH_CONSTRAINT);
        movieLayTmpSet.constrainHeight(textConstraint.getId(), ConstraintSet.MATCH_CONSTRAINT);

        movieLayTmpSet.applyTo(movieLayTmp);

        moviesMap.put(swipeLay, movie);
    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static class AsyncLoadMovies extends AsyncTask<String, Void, ArrayList<Movie>> {

        private String getRequest(String url) throws IOException{
            StringBuilder result = new StringBuilder();

            URL getReq = new URL(url);
            URLConnection movieConnection = getReq.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(movieConnection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                result.append(inputLine).append("\n");

            in.close();
            return result.toString();
        }

        private ArrayList<Movie> parseMovies(String jsonText) throws ParseException {
            ArrayList<Movie> result = new ArrayList<>();

            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonText);

            JSONArray movies = (JSONArray) jsonObject.get("Search");
            for (Object movie : movies) {
                JSONObject tmp = (JSONObject) movie;
                result.add(new Movie(
                        (String) tmp.get("Title"),
                        (String) tmp.get("Year"),
                        (String) tmp.get("imdbID"),
                        (String) tmp.get("Type"),
                        (String) tmp.get("Poster")
                ));
            }

            return result;
        }

        private ArrayList<Movie> search(String newText){
            String jsonResponse = String.format("http://www.omdbapi.com/?apikey=7e9fe69e&s=\"%s\"&page=1", newText);
            try {
                ArrayList<Movie> movies = parseMovies(getRequest(jsonResponse));
                return movies;
            } catch (Exception e) {
                System.err.println("Incorrect content of JSON file!");
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected ArrayList<Movie> doInBackground(String... strings) {
            return search(strings[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            loadMovies(movies);
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;
        @SuppressLint("StaticFieldLeak")
        ProgressBar loadingBar;
        @SuppressLint("StaticFieldLeak")
        Context context;

        public DownloadImageTask(ImageView bmImage, ProgressBar loadingBar, Context context) {
            this.bmImage = bmImage;
            this.loadingBar = loadingBar;
            this.context = context;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null)
                bmImage.setImageBitmap(result);
            else {
                bmImage.setBackgroundResource(R.drawable.no_image);
                Toast.makeText(context, "Cannot load data!", Toast.LENGTH_LONG).show();
            }
            loadingBar.setVisibility(View.GONE);
            bmImage.setVisibility(View.VISIBLE);
        }
    }
}