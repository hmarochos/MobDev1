package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RawRes;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import ua.kpi.comsys.iv8218.mobdev1.R;


public class MovieInfo {
    public void showPopupWindow(final View view, Movie movie) {
        view.getContext();
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.popup_movie_info, null);

        try {
            parseMovieInfo(readTextFile(popupView.getContext(),
                    getResId(movie.getImdbID(), R.raw.class)), movie);
        } catch (Exception e) {
            System.err.println("Error while reading JSON file!");
            e.printStackTrace();
        }

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        if (movie.getPoster().length() != 0){
            ((ImageView) popupView.findViewById(R.id.movie_info_image)).setImageResource(
                    getResId(movie.getPoster().toLowerCase()
                            .split("\\.")[0], R.drawable.class));
        }
        ((TextView) popupView.findViewById(R.id.movie_info_title))      .setText(movie.getTitle());
        ((TextView) popupView.findViewById(R.id.movie_info_year))       .setText(movie.getYear());
        ((TextView) popupView.findViewById(R.id.movie_info_released))   .setText(movie.getReleased());
        ((TextView) popupView.findViewById(R.id.movie_info_runtime))    .setText(movie.getRuntime());
        ((TextView) popupView.findViewById(R.id.movie_info_genre))      .setText(movie.getGenre());
        ((TextView) popupView.findViewById(R.id.movie_info_director))   .setText(movie.getDirector());
        ((TextView) popupView.findViewById(R.id.movie_info_actors))     .setText(movie.getActors());
        ((TextView) popupView.findViewById(R.id.movie_info_plot))       .setText(movie.getPlot());
        ((TextView) popupView.findViewById(R.id.movie_info_language))   .setText(movie.getLanguage());
        ((TextView) popupView.findViewById(R.id.movie_info_country))    .setText(movie.getCountry());
        ((TextView) popupView.findViewById(R.id.movie_info_awards))     .setText(movie.getAwards());
        ((TextView) popupView.findViewById(R.id.movie_info_rating))     .setText(movie.getRating());
        ((TextView) popupView.findViewById(R.id.movie_info_production)) .setText(movie.getProduction());
    }

    private void parseMovieInfo(String jsonText, Movie movie) throws ParseException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonText);
        movie.addInfo(  (String) jsonObject.get("Rated"),
                        (String) jsonObject.get("Released"),
                        (String) jsonObject.get("Runtime"),
                        (String) jsonObject.get("Genre"),
                        (String) jsonObject.get("Director"),
                        (String) jsonObject.get("Writer"),
                        (String) jsonObject.get("Actors"),
                        (String) jsonObject.get("Plot"),
                        (String) jsonObject.get("Language"),
                        (String) jsonObject.get("Country"),
                        (String) jsonObject.get("Awards"),
                        (String) jsonObject.get("imdbRating"),
                        (String) jsonObject.get("imdbVotes"),
                        (String) jsonObject.get("Production"));
    }

    private static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static String readTextFile(Context context, @RawRes int id){
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
            System.err.println("FIle cannot be reading!");
            e.printStackTrace();
        }
        return outputStream.toString();
    }
}
