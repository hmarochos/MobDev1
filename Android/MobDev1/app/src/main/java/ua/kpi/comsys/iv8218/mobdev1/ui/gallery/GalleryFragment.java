package ua.kpi.comsys.iv8218.mobdev1.ui.gallery;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ua.kpi.comsys.iv8218.mobdev1.R;
import ua.kpi.comsys.iv8218.mobdev1.ui.notifications.NotificationsFragment;

public class GalleryFragment extends Fragment {
    private static final int RESULT_LOAD_IMAGE = 2;

    private static View root;
    private static ScrollView scrollView;
    private static LinearLayout scrollMain;
    private static ArrayList<ImageView> allImages;
    private static ArrayList<ArrayList<Object>> placeholderList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_gallery, container, false);
        setRetainInstance(true);

        scrollView = root.findViewById(R.id.scrollview_gallery);
        scrollMain = root.findViewById(R.id.linear_main);

        allImages = new ArrayList<>();
        placeholderList = new ArrayList<>();

        Button btnAddImage = root.findViewById(R.id.button_add_pic);
        btnAddImage.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
            gallery.setType("image/*");
            startActivityForResult(gallery, RESULT_LOAD_IMAGE);
        });

        AsyncLoadGallery aTask = new AsyncLoadGallery();
        aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                "19193969-87191e5db266905fe8936d565",
                "\"fun+party\"",
                "30");

        return root;
    }

    protected static void loadImages(ArrayList<String> images){
        if (images != null) {
            for (String img :
                    images) {
                addImage(false, null, img);
            }
        }
        else {
            Toast.makeText(root.getContext(), "Cannot load data!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK){
            Uri imageUri = data.getData();
            addImage(true, imageUri, "");
        }
    }

    private static void addImage(boolean isLocal, Uri imageUri, String imageUrl) {
        ProgressBar loadingImageBar = new ProgressBar(root.getContext());
        loadingImageBar.setLayoutParams(
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        loadingImageBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(root.getContext(), R.color.purple_500),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        loadingImageBar.setVisibility(View.GONE);
        loadingImageBar.setId(loadingImageBar.hashCode());

        ImageView newImage = new ImageView(root.getContext());
        if (isLocal)
            newImage.setImageURI(imageUri);
        else {
            loadingImageBar.setVisibility(View.VISIBLE);
            new NotificationsFragment.DownloadImageTask(newImage, loadingImageBar, root.getContext()).execute(imageUrl);
        }
        newImage.setBackgroundColor(Color.GRAY);
        ConstraintLayout.LayoutParams imageParams =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        imageParams.dimensionRatio = "1";
        newImage.setLayoutParams(imageParams);
        newImage.setId(newImage.hashCode());
        setImagePlace(newImage, loadingImageBar);

        allImages.add(newImage);
    }

    private static void setImagePlace(ImageView newImage, ProgressBar loadBar){
        ConstraintLayout tmpLayout = null;
        ConstraintSet tmpSet = null;
        if (allImages.size() > 0) {
            tmpLayout = (ConstraintLayout) getConstraintArrayList(0, placeholderList);
            if (allImages.size() % 10 != 0) {
                tmpLayout.addView(newImage);
                tmpLayout.addView(loadBar);
            }
            tmpSet = (ConstraintSet) getConstraintArrayList(1, placeholderList);

            tmpSet.clone(tmpLayout);

            tmpSet.setMargin(newImage.getId(), ConstraintSet.START, 3);
            tmpSet.setMargin(newImage.getId(), ConstraintSet.TOP, 3);
            tmpSet.setMargin(newImage.getId(), ConstraintSet.END, 3);
            tmpSet.setMargin(newImage.getId(), ConstraintSet.BOTTOM, 3);

            tmpSet.connect(loadBar.getId(), ConstraintSet.START, newImage.getId(), ConstraintSet.START);
            tmpSet.connect(loadBar.getId(), ConstraintSet.TOP, newImage.getId(), ConstraintSet.TOP);
            tmpSet.connect(loadBar.getId(), ConstraintSet.END, newImage.getId(), ConstraintSet.END);
            tmpSet.connect(loadBar.getId(), ConstraintSet.BOTTOM, newImage.getId(), ConstraintSet.BOTTOM);
        }

        switch (allImages.size() % 10){
            case 0:{
                placeholderList.add(new ArrayList<>());

                ConstraintLayout newConstraint = new ConstraintLayout(root.getContext());
                placeholderList.get(placeholderList.size()-1).add(newConstraint);
                newConstraint.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                scrollMain.addView(newConstraint);

                Guideline vertical_25 = makeGuideline(ConstraintLayout.LayoutParams.VERTICAL,
                        0.25f);
                Guideline vertical_50 = makeGuideline(ConstraintLayout.LayoutParams.VERTICAL,
                        0.5f);
                Guideline vertical_75 = makeGuideline(ConstraintLayout.LayoutParams.VERTICAL,
                        0.75f);

                Guideline horizontal_25 = makeGuideline(ConstraintLayout.LayoutParams.HORIZONTAL,
                        1f);
                Guideline horizontal_50 = makeGuideline(ConstraintLayout.LayoutParams.HORIZONTAL,
                        1f);
                Guideline horizontal_75 = makeGuideline(ConstraintLayout.LayoutParams.HORIZONTAL,
                        1f);

                newConstraint.addView(vertical_25, 0);
                newConstraint.addView(vertical_50, 1);
                newConstraint.addView(vertical_75, 2);
                newConstraint.addView(horizontal_25, 3);
                newConstraint.addView(horizontal_50, 4);
                newConstraint.addView(horizontal_75, 5);

                newConstraint.addView(newImage);

                ConstraintSet newConstraintSet = new ConstraintSet();
                placeholderList.get(placeholderList.size()-1).add(newConstraintSet);
                newConstraintSet.clone(newConstraint);

                newConstraintSet.setMargin(newImage.getId(), ConstraintSet.START, 3);
                newConstraintSet.setMargin(newImage.getId(), ConstraintSet.TOP, 3);
                newConstraintSet.setMargin(newImage.getId(), ConstraintSet.END, 3);
                newConstraintSet.setMargin(newImage.getId(), ConstraintSet.BOTTOM, 3);

                connectInConstraint(newConstraintSet, newImage.getId(),
                        ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID,
                        vertical_25.getId(), horizontal_25.getId());

                newConstraintSet.connect(loadBar.getId(), ConstraintSet.START, newImage.getId(), ConstraintSet.START);
                newConstraintSet.connect(loadBar.getId(), ConstraintSet.TOP, newImage.getId(), ConstraintSet.TOP);
                newConstraintSet.connect(loadBar.getId(), ConstraintSet.END, newImage.getId(), ConstraintSet.END);
                newConstraintSet.connect(loadBar.getId(), ConstraintSet.BOTTOM, newImage.getId(), ConstraintSet.BOTTOM);

                newConstraintSet.applyTo(newConstraint);
                break;
            }

            case 1: {
                tmpSet.setGuidelinePercent(tmpLayout.getChildAt(3).getId(), 0.5f);

                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(0).getId(), ConstraintSet.PARENT_ID,
                        tmpLayout.getChildAt(2).getId(), tmpLayout.getChildAt(4).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 2: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(2).getId(), ConstraintSet.PARENT_ID,
                        ConstraintSet.PARENT_ID, tmpLayout.getChildAt(3).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 3: {
                connectInConstraint(tmpSet, newImage.getId(),
                        ConstraintSet.PARENT_ID, tmpLayout.getChildAt(3).getId(),
                        tmpLayout.getChildAt(0).getId(), tmpLayout.getChildAt(4).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 4: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(2).getId(), tmpLayout.getChildAt(3).getId(),
                        ConstraintSet.PARENT_ID, tmpLayout.getChildAt(4).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 5: {
                tmpSet.setGuidelinePercent(tmpLayout.getChildAt(3).getId(), 0.25f);
                tmpSet.setGuidelinePercent(tmpLayout.getChildAt(4).getId(), 0.5f);
                tmpSet.setGuidelinePercent(tmpLayout.getChildAt(5).getId(), 0.75f);

                connectInConstraint(tmpSet, newImage.getId(),
                        ConstraintSet.PARENT_ID, tmpLayout.getChildAt(4).getId(),
                        tmpLayout.getChildAt(1).getId(), ConstraintSet.PARENT_ID);

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 6: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(1).getId(), tmpLayout.getChildAt(4).getId(),
                        tmpLayout.getChildAt(2).getId(), tmpLayout.getChildAt(5).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 7: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(2).getId(), tmpLayout.getChildAt(4).getId(),
                        ConstraintSet.PARENT_ID, tmpLayout.getChildAt(5).getId());

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 8: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(1).getId(), tmpLayout.getChildAt(5).getId(),
                        tmpLayout.getChildAt(2).getId(), ConstraintSet.PARENT_ID);

                tmpSet.applyTo(tmpLayout);
                break;
            }

            case 9: {
                connectInConstraint(tmpSet, newImage.getId(),
                        tmpLayout.getChildAt(2).getId(), tmpLayout.getChildAt(5).getId(),
                        ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID);

                tmpSet.applyTo(tmpLayout);
                break;
            }
        }
    }

    private static void connectInConstraint(ConstraintSet constraintSet, int mainView,
                                            int startView, int topView,
                                            int endView, int bottomView){
        constraintSet.connect(mainView, ConstraintSet.START,
                startView, startView == ConstraintSet.PARENT_ID ?
                                ConstraintSet.START : ConstraintSet.END);
        constraintSet.connect(mainView, ConstraintSet.TOP,
                topView, topView == ConstraintSet.PARENT_ID ?
                                ConstraintSet.TOP : ConstraintSet.BOTTOM);
        constraintSet.connect(mainView, ConstraintSet.END,
                endView, endView == ConstraintSet.PARENT_ID ?
                                ConstraintSet.END : ConstraintSet.START);
        constraintSet.connect(mainView, ConstraintSet.BOTTOM,
                bottomView, bottomView == ConstraintSet.PARENT_ID ?
                                ConstraintSet.BOTTOM : ConstraintSet.TOP);
    }

    private static Guideline makeGuideline(int orientation, float percent){
        Guideline guideline = new Guideline(root.getContext());
        guideline.setId(guideline.hashCode());

        ConstraintLayout.LayoutParams guideline_Params =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);
        guideline_Params.orientation = orientation;

        guideline.setLayoutParams(guideline_Params);

        guideline.setGuidelinePercent(percent);

        return guideline;
    }

    private static Object getConstraintArrayList(int index, ArrayList<ArrayList<Object>> list){
        return list.get(list.size()-1).get(index);
    }

    private static class AsyncLoadGallery extends AsyncTask<String, Void, ArrayList<String>> {
        private String getRequest(String url){
            StringBuilder result = new StringBuilder();
            try {
                URL getReq = new URL(url);
                URLConnection bookConnection = getReq.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(bookConnection.getInputStream()));
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

        private ArrayList<String> parseImages(String jsonText) throws ParseException {
            ArrayList<String> result = new ArrayList<>();

            JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonText);

            JSONArray images = (JSONArray) jsonObject.get("hits");
            for (Object img : images) {
                JSONObject tmp = (JSONObject) img;
                result.add((String) tmp.get("webformatURL"));
            }

            return result;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private ArrayList<String> search(String api, String req, String count){
            String jsonResponse = String.format("https://pixabay.com/api/?key=%s&q=%s&image_type=photo&per_page=%s",
                    api, req, count);
            try {
                return parseImages(getRequest(jsonResponse));
            } catch (ParseException e) {
                System.err.println("Incorrect content of JSON file!");
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            return search(strings[0], strings[1], strings[2]);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(ArrayList<String> images) {
            super.onPostExecute(images);
            GalleryFragment.loadImages(images);
        }
    }
}

