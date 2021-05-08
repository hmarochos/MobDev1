package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.daimajia.swipe.SwipeLayout;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import ua.kpi.comsys.iv8218.mobdev1.R;

public class NotificationsFragment extends Fragment {

    private HashMap<ConstraintLayout, Movie> moviesMap;
    LinearLayout movieList;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_notifications, container, false);

        movieList = root.findViewById(R.id.scroll_lay);
        moviesMap = new HashMap<>();

        try {
            ArrayList<Movie> movies = parseMovies(readTextFile(root.getContext(), R.raw.movieslist));
            for (Movie movie :
                    movies) {
                addNewMovie(root, movieList, movie);
            }
        } catch (Exception e) {
            System.err.println("Incorrect content of JSON file!");
            e.printStackTrace();
        }

        SearchView simpleSearchView = root.findViewById(R.id.search_view);

        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                int countResults = 0;
                for (ConstraintLayout movie :
                        moviesMap.keySet()) {
                    if (newText == null){
                        ((SwipeLayout) movie.getParent()).setVisibility(View.VISIBLE);
                        countResults++;
                    }
                    else {
                        if (moviesMap.get(movie).getTitle().toLowerCase()
                                .contains(newText.toLowerCase()) || newText.length() == 0){
                            ((SwipeLayout) movie.getParent()).setVisibility(View.VISIBLE);
                            countResults++;
                        }
                        else
                            ((SwipeLayout) movie.getParent()).setVisibility(View.GONE);
                    }
                }

                if (countResults == 0){
                    root.findViewById(R.id.no_movies_view).setVisibility(View.VISIBLE);
                }
                else {
                    root.findViewById(R.id.no_movies_view).setVisibility(View.GONE);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int countResults = 0;
                for (ConstraintLayout movie :
                        moviesMap.keySet()) {
                    if (newText == null){
                        ((SwipeLayout) movie.getParent()).setVisibility(View.VISIBLE);
                        countResults++;
                    }
                    else {
                        if (moviesMap.get(movie).getTitle().toLowerCase()
                                .contains(newText.toLowerCase()) || newText.length() == 0){
                            ((SwipeLayout) movie.getParent()).setVisibility(View.VISIBLE);
                            countResults++;
                        }
                        else
                            ((SwipeLayout) movie.getParent()).setVisibility(View.GONE);
                    }
                }

                if (countResults == 0){
                    root.findViewById(R.id.no_movies_view).setVisibility(View.VISIBLE);
                }
                else {
                    root.findViewById(R.id.no_movies_view).setVisibility(View.GONE);
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

                    addNewMovie(root, movieList, new Movie(inputTitle.getText().toString(),
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        changeLaySizes();
    }

    private void changeLaySizes(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) root.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        for (ConstraintLayout movieshelf :
                moviesMap.keySet()) {
            movieshelf.getChildAt(0).setLayoutParams(
                    new ConstraintLayout.LayoutParams(width/3, width/3));
        }
    }

    public void binClicked(SwipeLayout swipeLayout){
        moviesMap.remove(swipeLayout);
        movieList.removeView(swipeLayout);
    }

    private void addNewMovie(View root, LinearLayout movieList, Movie movie){
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

        ImageView imageTmp = new ImageView(root.getContext());
        imageTmp.setId(imageTmp.hashCode());
        ConstraintLayout.LayoutParams imgParams =
                new ConstraintLayout.LayoutParams(300, 300);
        if (movie.getPoster().length() != 0)
            imageTmp.setImageResource(
                    getResId(movie.getPoster().toLowerCase()
                            .split("\\.")[0], R.drawable.class));
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

        movieLayTmpSet.constrainWidth(textConstraint.getId(), ConstraintSet.MATCH_CONSTRAINT);
        movieLayTmpSet.constrainHeight(textConstraint.getId(), ConstraintSet.MATCH_CONSTRAINT);

        movieLayTmpSet.applyTo(movieLayTmp);

        moviesMap.put(movieLayTmp, movie);
    }

    public static String readTextFile(Context context, @RawRes int id){
        InputStream inputStream = context.getResources().openRawResource(id);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int size;
        try {
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            System.err.println("File cannot be reading!");
            e.printStackTrace();
        }
        return outputStream.toString();
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
}