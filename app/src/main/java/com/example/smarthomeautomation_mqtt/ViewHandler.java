package com.example.smarthomeautomation_mqtt;

import android.graphics.Color;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

public class ViewHandler {

    static boolean hInit=false;
    static boolean tInit=false;

    public static void temperatureChartInit(SlimChart tChart){
        ArrayList<Stat> tStats = new ArrayList<>();
        tStats.add(new Stat(50f, Color.rgb(232, 232, 232)));
        tChart.setStatList(tStats);
        tChart.setText("- -");
        tInit=false;
    }

    public static void humidityChartInit(SlimChart hChart){
        ArrayList<Stat> hStats = new ArrayList<>();
        hStats.add(new Stat(100f, Color.rgb(232, 232, 232)));
        hChart.setText("- -");
        hChart.setStatList(hStats);
        hInit=false;
    }

    public static void setTemperatureData(SlimChart tChart, float temp){
        ArrayList<Stat> tStats = new ArrayList<>();
        tStats.add(new Stat(50f, Color.rgb(232, 232, 232)));
        for (int i = 0; i < temp; i++) {
            tStats.add(new Stat((float) i, Color.rgb(255, 255 - (int) (i * 5.1), 0)));
        }
        if(temp<50) {
            tChart.setStatList(tStats);
        }
        tChart.setText(temp+"\u00B0 C");
        if(!tInit){
            tChart.setStartAnimationDuration(2000);
            tChart.playStartAnimation();
        }
        tInit=true;
    }

    public static void setHumidityData(SlimChart hChart, float humid){
        ArrayList<Stat> hStats = new ArrayList<>();
        hStats.add(new Stat(100f, Color.rgb(232, 232, 232)));
        for (int i = 0; i < humid; i++) {
            hStats.add(new Stat((float) i, Color.rgb(0, 180, 255 - (int) (i * 2.55))));
        }

        hChart.setStatList(hStats);
        hChart.setText(humid+" %");
        if(!hInit){
            hChart.setStartAnimationDuration(2000);
            hChart.playStartAnimation();
        }
        hInit=true;
    }

    public static void lineChartInit(LineChartView chartView, TextView info){

        info.setText("Fetching Data...");
        LineChartData data = new LineChartData();
        chartView.setLineChartData(data);

    }

    public static void setLineChartData(LineChartView chartView, DataSnapshot snapshot, TextView info ){

        int i=0; int j=0;

        List<PointValue> tPointValues=new ArrayList<>();
        List<PointValue> hPointvalues=new ArrayList<>();

        Iterable<DataSnapshot> snapshots = snapshot.getChildren();
        Iterator<DataSnapshot> iterator=snapshots.iterator();

        while(iterator.hasNext()){
            DataSnapshot point=iterator.next();
            Integer x = Integer.parseInt(point.child("temperature").getValue().toString());
            Integer y = Integer.parseInt(point.child("humidity").getValue().toString());
            tPointValues.add(new PointValue(i++, x));
            hPointvalues.add(new PointValue(j++, y));
        }

        Line humidity = new Line(hPointvalues)
                .setColor(Color.rgb(0,0, 0))
                .setCubic(true)
                .setStrokeWidth(3)
                .setPointRadius(3);

        Line temperature=new Line(tPointValues)
                .setColor(Color.rgb(255, 255, 255))
                .setCubic(true)
                .setStrokeWidth(3)
                .setShape(ValueShape.DIAMOND)
                .setPointRadius(3);

        List<Line> lines= new ArrayList<>();
        lines.add(humidity);
        lines.add(temperature);
        LineChartData data=new LineChartData();
        data.setLines(lines);
        chartView.setLineChartData(data);

        info.setText("Last 24 hours...");
    }

    public static void renderDHT(String response, SlimChart tChart, SlimChart hChart) {
        Log.d("Info", "Recieving DHT");
        String[] status = response.trim().split("/");
        float tmp = Float.parseFloat(status[0]);
        float humid = Float.parseFloat(status[1]);
        setTemperatureData(tChart, tmp);
        setHumidityData(hChart, humid);
    }

    public static void renderSwitch(String response, Switch sw){
        if (response.equals("1"))
            sw.setChecked(true);
        else
            sw.setChecked(false);
    }
}
