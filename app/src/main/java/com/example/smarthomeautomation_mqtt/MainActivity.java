package com.example.smarthomeautomation_mqtt;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity {

    Switch fansw;
    Switch bulbsw;
    TextView temptv;
    TextView fantv;
    TextView bulbtv;

    ImageView faniv;
    ImageView bulbiv;

    LinearLayout linearLayout;

    MqttAndroidClient client;
    NotificationCompat.Builder notification;
    Snackbar disconneted;

    Boolean manual=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fansw=findViewById(R.id.btnfan);
        bulbsw=findViewById(R.id.btnbulb);
        temptv =findViewById(R.id.tvtemp);
        fantv=findViewById(R.id.tvfan);
        bulbtv=findViewById(R.id.tvbulb);
        faniv=findViewById(R.id.ifan);
        bulbiv=findViewById(R.id.ibulb);
        linearLayout=findViewById(R.id.linearLayout);


        ViewHandler.temperatureChartInit((SlimChart) findViewById(R.id.temp));
        ViewHandler.humidityChartInit((SlimChart) findViewById(R.id.humidity));
        ViewHandler.lineChartInit((LineChartView) findViewById(R.id.chart));



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel("CHANNEL", "Priority", NotificationManager.IMPORTANCE_DEFAULT);
        }
        notification = new NotificationCompat.Builder(MainActivity.this, "CHANNEL");

        disconneted=Snackbar.make(linearLayout, "Disconnected!! Trying To Reconnect...", Snackbar.LENGTH_INDEFINITE);

        doConnect();

        faniv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fansw.isChecked())
                    fansw.setChecked(false);
                else
                    fansw.setChecked(true);
                fanSwitch(v);
            }
        });

        fansw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(manual) {
                    Toast.makeText(MainActivity.this, "Fan Switched", Toast.LENGTH_SHORT).show();
                    fanSwitch(linearLayout);
                }
                manual=true;
            }
        });

        bulbsw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(manual) {
                    Toast.makeText(MainActivity.this, "Bulb Switched", Toast.LENGTH_SHORT).show();
                    bulbSwitch(linearLayout);
                }
                manual=true;
            }
        });

        bulbiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bulbsw.isChecked())
                    bulbsw.setChecked(false);
                else
                    bulbsw.setChecked(true);
                bulbSwitch(v);
            }
        });

        findViewById(R.id.gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fansw.isChecked())
                    fansw.setChecked(false);
                else
                    fansw.setChecked(true);
                fanSwitch(v);
            }
        });

    }

    public void doConnect() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(getApplicationContext(), "tcp://klinux.tk:90", clientId);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                disconneted.dismiss();
                if (reconnect) {
                    Log.d("Info", "Client Reconnected");
                    Toast.makeText(MainActivity.this, "Reconnected!!", Toast.LENGTH_SHORT).show();
                    subscribeToTopic();
                } else {
                    Log.d("Info", "Client Connected");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                disconneted.show();
                Log.d("Error", "Connection Lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.equals("get/fan")){
                    manual=false;
                    renderFan(message.toString());
                }
                if(topic.equals("get/bulb")){
                    manual=false;
                    renderBulb(message.toString());
                }
                if(topic.equals("get/dht")){
                    renderDHT(message.toString());
                }
                if(topic.equals("get/gas") || topic.equals("get/water"))
                    doNotify(topic, message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("info", "Delivered");
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName("kishan");
        mqttConnectOptions.setPassword("root".toCharArray());

        try {
            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Info", "Client to server Connected");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Error", "Connection to server Failed");

                    Snackbar.make(linearLayout, "Connection To Server Failed!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    doConnect();
                                }
                            })
                            .show();
                }
            });
        }
        catch (MqttException ex){
            Log.d("Exception", "MqttException");
        }
    }

    public void subscribeToTopic(){
        try {
            client.subscribe("get/#", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Info", "Subscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Error", "Failure Subscription");
                }
            });

        } catch (MqttException ex){
            Log.d("Exception", "MqttException");
        }
    }

    public  void publishToTopic(String topic, String msg){
        try {
            MqttMessage message = new MqttMessage();
            message.setRetained(true);
            message.setPayload(msg.getBytes());
            client.publish(topic, message);
            Log.d("Info", "Published SuccessFully");
        }
        catch (MqttException e) {
            Toast.makeText(this, "Can't Connect To Host!!", Toast.LENGTH_SHORT).show();
            System.err.println("Error in Publishing");
        }

    }

    private void renderDHT(String response) {
        String[] status = response.trim().split("/");
        float tmp = Float.parseFloat(status[0]);
        float humid = Float.parseFloat(status[1]);
        temptv.setText(tmp + "\u00B0C~" + humid + "%\nLast Updated\n" + status[2]);
    }

    private void renderFan(String response){
        if (response.equals("1")) {
            fantv.setText("Fan Is On!");
            fansw.setChecked(true);
            faniv.setVisibility(View.GONE);
            (findViewById(R.id.gif)).setVisibility(View.VISIBLE);
        } else {
            fantv.setText("Fan Is Off!");
            fansw.setChecked(false);
            (findViewById(R.id.gif)).setVisibility(View.GONE);
            faniv.setVisibility(View.VISIBLE);
        }
    }

    private void renderBulb(String response){
        if (response.equals("1")) {
            bulbtv.setText("Bulb Is On!");
            bulbsw.setChecked(true);
            bulbiv.setImageResource(R.drawable.bon);

        } else {
            bulbtv.setText("Bulb Is Off!");
            bulbsw.setChecked(false);
            bulbiv.setImageResource(R.drawable.boff);
        }

    }

    public void fanSwitch(View v) {

        String msg;
        if(fansw.isChecked())
            msg="1";
        else
            msg="0";
        publishToTopic("post/fan", msg);
    }

    public void bulbSwitch(View v) {
        String msg;
        if(bulbsw.isChecked())
            msg="1";
        else
            msg="0";
        publishToTopic("post/bulb", msg);
    }

    private void doNotify(String topic, String response){
        if(topic.equals("get/water")) {

            notification.setSmallIcon(R.drawable.alert);
            notification.setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tone));
            notification.setContentTitle("Alert!");
            notification.setContentText("Water Tank Is Filled!");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (response.equals("1")) {
                notificationManager.notify(1, notification.build());
            } else
                notificationManager.cancel(1);
        }
        else if(topic.equals("get/gas")){
            notification.setSmallIcon(R.drawable.critacal);
            notification.setContentTitle("Alert!");
            notification.setContentText("Smoke Detected In House!");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (response.equals("1")) {
                notificationManager.notify(0, notification.build());
            } else
                notificationManager.cancel(0);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void makeNotificationChannel(String id, String name, int importance)
    {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        //channel.setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tone), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setLegacyStreamType(AudioManager.STREAM_NOTIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
        channel.setBypassDnd(true);
        channel.enableLights(true);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
