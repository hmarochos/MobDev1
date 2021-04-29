package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import ua.kpi.comsys.iv8218.mobdev1.R;

public class NotificationsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        LinearLayout movieList = root.findViewById(R.id.movie_linear_scrolable);
        ArrayList<ConstraintLayout> moviesLinear = new ArrayList<>();

        try {
            ArrayList<Movie> movies = parseMovies(readTextFile(root.getContext(), R.raw.movieslist));
            for (Movie movie :
                    movies) {
                ConstraintLayout movieLayTmp = new ConstraintLayout(root.getContext());
                movieLayTmp.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                movieList.addView(movieLayTmp);

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
                textTitle.setEllipsize(TextUtils.TruncateAt.END);
                textTitle.setMaxLines(3);
                textTitle.setText(movie.getTitle());
                ConstraintLayout.LayoutParams textTitleParams =
                        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT);
                textConstraint.addView(textTitle, 0, textTitleParams);

                TextView textSubtitle = new TextView(root.getContext());
                textSubtitle.setText(movie.getYear());
                textSubtitle.setPadding(0, 1, 5, 1);
                textSubtitle.setId(textSubtitle.hashCode());
                ConstraintLayout.LayoutParams textSubtitleParams =
                        new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT);
                textConstraint.addView(textSubtitle, 1, textSubtitleParams);

                TextView textPrice = new TextView(root.getContext());
                textPrice.setText(movie.getType());
                textPrice.setPadding(0, 0, 5, 4);
                textPrice.setId(textPrice.hashCode());
                ConstraintLayout.LayoutParams textPriceParams =
                        new ConstraintLayout.LayoutParams(ConstraintSet.WRAP_CONTENT,
                                ConstraintSet.WRAP_CONTENT);
                textConstraint.addView(textPrice, 2, textPriceParams);

                ConstraintSet textConstraintSet = new ConstraintSet();
                textConstraintSet.clone(textConstraint);

                textConstraintSet.connect(textTitle.getId(), ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                textConstraintSet.connect(textTitle.getId(), ConstraintSet.START,
                        ConstraintSet.PARENT_ID, ConstraintSet.START);
                textConstraintSet.connect(textSubtitle.getId(), ConstraintSet.TOP,
                        textTitle.getId(), ConstraintSet.BOTTOM);
                textConstraintSet.connect(textSubtitle.getId(), ConstraintSet.START,
                        ConstraintSet.PARENT_ID, ConstraintSet.START);
                textConstraintSet.connect(textSubtitle.getId(), ConstraintSet.BOTTOM,
                        textPrice.getId(), ConstraintSet.TOP);
                textConstraintSet.connect(textPrice.getId(), ConstraintSet.BOTTOM,
                        ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                textConstraintSet.connect(textPrice.getId(), ConstraintSet.START,
                        ConstraintSet.PARENT_ID, ConstraintSet.START);

                textConstraintSet.setVerticalBias(textSubtitle.getId(), 0.3f);

                textConstraintSet.setMargin(textSubtitle.getId(), ConstraintSet.TOP, 3);
                textConstraintSet.setMargin(textSubtitle.getId(), ConstraintSet.BOTTOM, 3);

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

                moviesLinear.add(movieLayTmp);
            }
        } catch (ParseException e) {
            System.err.println("Incorrect content of JSON file!");
            e.printStackTrace();
        }

        LinearLayout fragmentMoviesLay = root.findViewById(R.id.fragment_movies_lay);

        fragmentMoviesLay.post(() -> {
            int width = fragmentMoviesLay.getWidth();
            for (ConstraintLayout movieshelf :
                    moviesLinear) {
                movieshelf.getChildAt(0).setLayoutParams(
                        new ConstraintLayout.LayoutParams(width/3, width/3));
            }
        });

        return root;
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