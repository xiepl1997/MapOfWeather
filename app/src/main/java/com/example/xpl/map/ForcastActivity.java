package com.example.xpl.map;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ForcastActivity extends AppCompatActivity {
    private TextView day1, day2, day3, day4, day5;
    private String Json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forcast);
        day1 = (TextView)findViewById(R.id.day1);
        day2 = (TextView)findViewById(R.id.day2);
        day3 = (TextView)findViewById(R.id.day3);
        day4 = (TextView)findViewById(R.id.day4);
        day5 = (TextView)findViewById(R.id.day5);
        Intent intent = getIntent();
        Json = intent.getStringExtra("Json");
        try {
            JsonAnalysis(Json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //分析Json方法
    private void JsonAnalysis(String Json) throws JSONException {
        JSONObject object = null;
        try{
            object = new JSONObject(Json);
        }catch (JSONException e){
            e.printStackTrace();
        }
        for(int i = 0; i < 5; i++) {
            //数据来自www.sojson.com
            JSONObject objectInfo = object.getJSONObject("data").getJSONArray("forecast").getJSONObject(i);
            String date = objectInfo.optString("date");
            String high = objectInfo.optString("high");
            String low = objectInfo.optString("low");
            String type = objectInfo.optString("type");
            String fx = objectInfo.optString("fx");
            String fl = objectInfo.optString("fl");
            String notice = objectInfo.optString("notice");

            String weatherResult = date + "\n" + type + "\n" + high + "\n" + low + "\n" + fx + "\n风速 " + fl + "\n\"" + notice + "\"";

            if(i == 0){
                day1.setText(weatherResult);
            }
            else if(i == 1){
                day2.setText(weatherResult);
            }
            else if(i == 2){
                day3.setText(weatherResult);
            }
            else if(i == 3){
                day4.setText(weatherResult);
            }
            else{
                day5.setText(weatherResult);
            }
        }
    }
}
