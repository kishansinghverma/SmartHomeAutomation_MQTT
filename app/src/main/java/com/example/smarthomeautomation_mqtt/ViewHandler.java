package com.example.smarthomeautomation_mqtt;

import android.graphics.Color;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

public class ViewHandler {
    public static void temperatureChartInit(SlimChart tChart){
        ArrayList<Stat> tStats = new ArrayList<>();
        tStats.add(new Stat(100f, Color.rgb(232, 232, 232)));

        for (int i = 0; i < 75; i++) {
            tStats.add(new Stat((float) i, Color.rgb(255, 250 - (int) (i * 2.55), 0)));
        }

        tChart.setStatList(tStats);
        tChart.setStartAnimationDuration(2000);
        tChart.playStartAnimation();
    }

    public static void humidityChartInit(SlimChart hChart){
        ArrayList<Stat> hStats = new ArrayList<>();
        hStats.add(new Stat(100f, Color.rgb(232, 232, 232)));

        for (int i = 0; i < 75; i++) {
            hStats.add(new Stat((float) i, Color.rgb(0, 180, 255-(int)(i * 2.55))));
        }

        hChart.setStatList(hStats);
        hChart.setStartAnimationDuration(2000);
        hChart.playStartAnimation();
    }

    public static void lineChartInit(LineChartView chartView){

        List<PointValue> values1 = new ArrayList<>();
        values1.add(new PointValue(0, 2));
        values1.add(new PointValue(1, 4));
        values1.add(new PointValue(2, 3));
        values1.add(new PointValue(3, 4));

        List<PointValue> values2 = new ArrayList<>();
        values2.add(new PointValue(0, 5));
        values2.add(new PointValue(1, 2));
        values2.add(new PointValue(2, 3));
        values2.add(new PointValue(3, 1));

        Line line1 = new Line(values1)
                .setColor(Color.rgb(0,0, 0))
                .setCubic(true)
                .setStrokeWidth(3)
                .setPointRadius(3);

        Line line2=new Line(values2)
                .setColor(Color.rgb(255, 255, 255))
                .setCubic(true)
                .setStrokeWidth(3)
                .setShape(ValueShape.DIAMOND)
                .setPointRadius(3);

        List<Line> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        chartView.setLineChartData(data);

    }

    public static void renderDHT(String response, SlimChart tChart, SlimChart hChart) {
        String[] status = response.trim().split("/");
        float tmp = Float.parseFloat(status[0]);
        float humid = Float.parseFloat(status[1]);
        //temptv.setText(tmp + "\u00B0C~" + humid + "%\nLast Updated\n" + status[2]);
    }

    public static void renderFan(String response, Switch fansw){
        /*if (response.equals("1")) {
            fantv.setText("Fan Is On!");
            fansw.setChecked(true);
            faniv.setVisibility(View.GONE);
            (findViewById(R.id.gif)).setVisibility(View.VISIBLE);
        } else {
            fantv.setText("Fan Is Off!");
            fansw.setChecked(false);
            (findViewById(R.id.gif)).setVisibility(View.GONE);
            faniv.setVisibility(View.VISIBLE);
        }*/
    }

    public static void renderBulb(String response, Switch bulbsw){
        /*if (response.equals("1")) {
            bulbtv.setText("Bulb Is On!");
            bulbsw.setChecked(true);
            bulbiv.setImageResource(R.drawable.bon);

        } else {
            bulbtv.setText("Bulb Is Off!");
            bulbsw.setChecked(false);
            bulbiv.setImageResource(R.drawable.boff);
        }*/

    }

    public static void renderMotor(String response, Switch motorsw){
        /*if (response.equals("1")) {
            bulbtv.setText("Bulb Is On!");
            bulbsw.setChecked(true);
            bulbiv.setImageResource(R.drawable.bon);

        } else {
            bulbtv.setText("Bulb Is Off!");
            bulbsw.setChecked(false);
            bulbiv.setImageResource(R.drawable.boff);
        }*/

    }
}
