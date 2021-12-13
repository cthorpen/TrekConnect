package com.thorpen.trekconnect;

import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class GoogleAPI {
    static final String TAG = "GoogleAPIClass";
    // Required strings for api execution
    private static final String SEARCH_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final String API_KEY = "AIzaSyCCADvRH2ybVI9kBloxJhCA0GkGNC_1Vdo";

    // Allows access to methods
    MapsActivity mapsActivity;

    // Constructor for mapsActivity
    public GoogleAPI(MapsActivity mapsActivity){
        this.mapsActivity = mapsActivity;
    }

    // Executes url to get list
    public void fetchNearMeLocations(){
        String url = constructSearchLocationListURL();
        FetchLocationListAsyncTask asyncTask = new FetchLocationListAsyncTask();
        asyncTask.execute(url);
    }

    // Construct location list url
    public String constructSearchLocationListURL() {
        String url = SEARCH_BASE_URL;
        url += "?location=" + mapsActivity.getCurrLat() + "%2C" + mapsActivity.getCurrLng();
        url += "&radius=15000";
        url += "&keyword=Trails";
        url += "&key=" + API_KEY;
        return url;
    }

    // Fetch location information
    class FetchLocationListAsyncTask extends AsyncTask<String, Void, List<Place>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mapsActivity.moveCamera(.50f);
            Toast.makeText(mapsActivity, "Finding trails near you...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected List<Place> doInBackground(String... strings) {
            String url = strings[0];
            List<Place> placeList = new ArrayList<>();

            try {
                URL urlObject = new URL(url);
                HttpsURLConnection urlConnection = (HttpsURLConnection) urlObject.openConnection();

                String jsonResult = "";
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) { // if doesnt fail
                    jsonResult += (char) data;
                    data = reader.read();
                }

                // parse the JSON
                JSONObject jsonObject = new JSONObject(jsonResult);
                JSONArray placeArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < placeArray.length(); i++) {
                    JSONObject singlePlaceObject = placeArray.getJSONObject(i);
                    String id = singlePlaceObject.getString("place_id");
                    String name = singlePlaceObject.getString("name");
                    JSONObject geometryObject = singlePlaceObject.getJSONObject("geometry");
                    JSONObject locationObject = geometryObject.getJSONObject("location");
                    String lat = locationObject.getString("lat");
                    String lng = locationObject.getString("lng");
                    Place place = new Place(id, name, lat, lng);

                    if (place != null) {
                        placeList.add(place);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return placeList;
        }

        @Override
        protected void onPostExecute(List<Place> places) {
            super.onPostExecute(places);
            mapsActivity.receivedPlaces(places);
            Toast.makeText(mapsActivity, "Trails found!", Toast.LENGTH_SHORT).show();
        }
    }


}
