package ru19july.tgchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private ChartData gsonObj;

    //ChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //chartView = findViewById(R.id.chartView);

        String json = loadJSONFromAsset();
        Log.d(TAG, "JSON: " + json);

        ChartData chartData = parseJson(json);

        try {
            JSONArray jsonColumns= new JSONArray(json);
            for (int i=0; i<jsonColumns.length(); i++){
                JSONObject jsonColumn = jsonColumns.getJSONObject(i);
                Log.d(TAG, "onCreate: jsonColumn: " + jsonColumn);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ChartData parseJson(String json) {
        Log.d(TAG, "json(" + json.length() + "): " + json + "");
        int arrays = 0;
        int structs = 0;
        for(int i=0; i<json.length(); i++) {
            String s = json.substring(i, i + 1);
            //Log.d(TAG, "s: " + s);
            if (s.equals("[")) {
                arrays++;

            }
            if (s.equals("]")) {
                Log.d(TAG, "parseJson: ARRAYS: " + arrays);
                arrays--;
            }
            if (s.equals("{")) {
                structs++;

            }
            if (s.equals("}")) {
                Log.d(TAG, "parseJson: STRUCTS: " + structs);
                structs--;
            }
                //Gson gson = new Gson();
            //Reader reader = new InputStreamReader(ims);

            //gsonObj = gson.fromJson(json, ChartData.class);
            //Log.d(TAG, "parseJson: " + gsonObj);
        }




        return null;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("chart_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
