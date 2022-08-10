package com.example.hasansafwanhajjar.degree;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public MainActivity() throws JSONException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] forecastArray = {"Today - Sunny - 88/63", "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63", "Thurs - Ast - 75/65",
                "Fri - Heavy Rain - 65/56", "Sat - Sunny - 80/68"};
        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        ArrayAdapter mForecastAdapter = new ArrayAdapter<String>(this,
                R.layout.list_items_forcast, R.id.list_item_forecast_textView, weekForecast);
        ListView listView = (ListView) findViewById(R.id.List_forcast);
        listView.setAdapter(mForecastAdapter);



        String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=776dedb6f686cf859ad2b7d4f4743bee";

        URL url = new URL(urlString);
        // Create the request to OpenWeatherMap, and open the connection
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*****************/

        InputStream inputStream = urlConnection.getInputStream();
        String forecastJsonStr = readFromStream(inputStream);

        String in;
        JSONObject reader = null;
        try {
            reader = new JSONObject(in);
            JSONObject sys = reader.getJSONObject("sys");
            String country = sys.getString("country");

            JSONObject main = reader.getJSONObject("main");
            String temperature = main.getString("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // function

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream == null) {
            // Nothing to do.
            return null;
        } else {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        }
        if (output.length() == 0) {
            // Stream was empty. No point in parsing.
            return null;
        }

        return output.toString();
    }

    private String[] getWeatherDateFromJson(String forecastJsonStr, int numDays) throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray("list");
        Time dayTime = new Time();
        dayTime.setToNow();
        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        // now we work exclusively in UTC dayTime = new Time();
        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String highAndLow;
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);
            JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
            description = weatherObject.getString("main");
            JSONObject temperatureObject = dayForecast.getJSONObject("temp");
            double high = temperatureObject.getDouble("max");
            double low = temperatureObject.getDouble("min");
            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;
    }

    /**
     * Prepare the weather high/low for presentation
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn’t care about tenths of degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private String getReadableDateString(long time) {
        SimpleDateFormat shortendDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortendDateFormat.format(time);
    }


}
}



