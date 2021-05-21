package ua.kpi.comsys.iv8218.mobdev1.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;

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

import ua.kpi.comsys.iv8218.mobdev1.R;


public class MovieInfo {
    private static View popupView;
    private static ProgressBar loadingImage;
    private static ImageView movieImage;
    private static Movie movie;

    public void showPopupWindow(final View view, Movie movie) {
        view.getContext();
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        popupView = inflater.inflate(R.layout.popup_movie_info, null);
        MovieInfo.movie = movie;

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        loadingImage = popupView.findViewById(R.id.loadingImageInfo);
        movieImage = popupView.findViewById(R.id.movie_info_image);

        AsyncLoadMovieInfo aTask = new AsyncLoadMovieInfo();
        aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, movie.getImdbID());
    }

    protected static void setInfoData(){
        movieImage.setVisibility(View.INVISIBLE);
        loadingImage.setVisibility(View.VISIBLE);
        new NotificationsFragment
                .DownloadImageTask(movieImage, loadingImage, popupView.getContext())
                .execute(movie.getPoster());

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

    private static class AsyncLoadMovieInfo extends AsyncTask<String, Void, Void> {
        private String getRequest(String url){
            StringBuilder result = new StringBuilder();
            try {
                URL getReq = new URL(url);
                URLConnection movieConnection = getReq.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(movieConnection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    result.append(inputLine).append("\n");

                in.close();

            } catch (MalformedURLException e) {
                System.err.println(String.format("Incorrect URL <%s>!", url));
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        private void parseMovieInfo(String jsonText) throws ParseException {
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

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void search(String isbn13){
            String jsonResponse = String.format("http://www.omdbapi.com/?apikey=7e9fe69e&i=%s", isbn13);
            try {
                parseMovieInfo(getRequest(jsonResponse));
            } catch (ParseException e) {
                System.err.println("Incorrect content of JSON file!");
                e.printStackTrace();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(String... strings) {
            search(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MovieInfo.setInfoData();
        }
    }
}
