package com.example.smarthomeautomation_mqtt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lecho.lib.hellocharts.view.LineChartView;

import static com.example.smarthomeautomation_mqtt.ViewHandler.*;

public class MainActivity extends AppCompatActivity {

    Switch fansw;
    Switch bulbsw;
    Switch motorsw;

    TextView info;

    LineChartView lineChartView;
    SlimChart hChart;
    SlimChart tChart;

    ConstraintLayout layout;
    CardView fanCard;
    CardView bulbCard;
    CardView motorCard;

    NotificationHandler notificationHandler;
    NotificationManagerCompat notificationManager;

    static MqttAndroidClient client;

    Snackbar disconnected;
    Snackbar retry;

    Map<Integer, String> names;
    Map<View, View> views;

    int GAS=100;
    int WATER=200;

    String port;
    String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        doConnect();
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.menu_video)
                    startActivity(new Intent(MainActivity.this, VideoActivity.class));
                else if(item.getItemId()==R.id.menu_setting)
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                else
                    showDialog();
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup, popup.getMenu());
        popup.show();
    }

    public void showDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Created For Mini Project @ GLA\n\n" +
                "Team Members:\n\u2022 Kishan Singh\n\u2022 Pragati Prabha\n\u2022 Renu Tiwari\n\u2022 Nishi Yadav\n\n" +
                "Source Code: \nhttps://github.com/kishansinghverma/SmartHomeAutomation_MQTT");
        alertDialogBuilder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog dialog=alertDialogBuilder.create();
        dialog.show();
    }

    public void publishToTopic(String topic, String msg){

        try {
            MqttMessage message = new MqttMessage();
            message.setRetained(true);
            message.setPayload(msg.getBytes());
            client.publish(topic, message);
            Log.d("Info", "Published SuccessFully");
        }
        catch (MqttException e) {
            toast("Can't Connect To Host!!");
        }

    }

    public void switchHandler(View v){
        if(((Switch)v).isChecked())
            ((Switch)v).setChecked(false);
        else
            ((Switch)v).setChecked(true);

        payloadCreator(v);
    }

    public void payloadCreator(View v) {
        v=views.get(v);
        toast("Trying to switch...");
        String msg;
        String topic="post/"+ names.get(v.getId());

        if(((Switch)v).isChecked())
            msg="0";
        else
            msg="1";
        publishToTopic(topic, msg);
    }

    private void doNotify(String topic, String response){
        if(topic.equals("get/water")) {

            if (response.equals("1"))
                notificationManager.notify(WATER, notificationHandler.createWaterNotification(this).build());
            else
                notificationManager.cancel(WATER);
        }
        else if(topic.equals("get/gas")){

            if (response.equals("1"))
                notificationManager.notify(GAS, notificationHandler.createGasNotification(this).build());
            else
                notificationManager.cancel(GAS);
        }
    }

    public void doConnect() {
        retry.show();
        final String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(getApplicationContext(), "tcp://"+ip+":"+port, clientId);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    subscribeToTopic();
                    toast("Reconnected To Server!");
                    Log.d("Info","Connection Reconnected");
                } else {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("Info", "Connection Disconnected");
                activateControls(false);
                retry.show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                if(topic.equals("get/fan"))
                    renderSwitch(message.toString(), fansw);

                else if(topic.equals("get/bulb"))
                    renderSwitch(message.toString(), bulbsw);

                else if(topic.equals("get/motor"))
                    renderSwitch(message.toString(), motorsw);

                else if(topic.equals("get/dht"))
                    renderDHT(message.toString(), tChart, hChart);

                else if(topic.equals("get/gas") || topic.equals("get/water"))
                    doNotify(topic, message.toString());

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("info", "Delivered");
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Info", "Connection Succeed!");
                    toast("Connected To Server!");
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Info", "Connection Failure!");
                    activateControls(false);

                    disconnected=Snackbar.make(layout, "Can't Connect To Server", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            doConnect();
                        }
                    });
                    disconnected.show();
                }
            });
        }
        catch (MqttException ex){
            Log.d("Exception", "MqttException");
        }
    }

    public void subscribeToTopic(){
        retry.dismiss();
        disconnected.dismiss();
        listenDHTData();

        activateControls(true);

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

    public void listenDHTData(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dht = database.getReference("dht");

        dht.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setLineChartData(lineChartView, dataSnapshot, info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initComponents(){

        if(SettingActivity.MODE.equals("local")){
            port=SettingActivity.LOCAL_PORT;
            ip=SettingActivity.LOCAL_IP;
        }
        else {
            port=SettingActivity.GLOBAL_PORT;
            ip=SettingActivity.GLOBAL_IP;
        }

        fanCard=findViewById(R.id.fancard);
        bulbCard=findViewById(R.id.bulbcard);
        motorCard=findViewById(R.id.motorcard);
        fansw=findViewById(R.id.fansw);
        bulbsw=findViewById(R.id.bulbsw);
        motorsw=findViewById(R.id.motorsw);

        layout=findViewById(R.id.layout);
        lineChartView=findViewById(R.id.chart);
        hChart=findViewById(R.id.humidity);
        tChart=findViewById(R.id.temp);
        info=findViewById(R.id.chart_Info);

        retry=Snackbar.make(layout, "Disconnected! Trying To Connect...", Snackbar.LENGTH_INDEFINITE);
        disconnected=Snackbar.make(layout, "Can't Connect To Server", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConnect();
            }
        });

        activateControls(false);

        temperatureChartInit(tChart);
        humidityChartInit(hChart);
        lineChartInit(lineChartView, info);

        names =new HashMap<>();
        names.put(R.id.fansw, "fan");
        names.put(R.id.bulbsw, "bulb");
        names.put(R.id.motorsw, "motor");

        views=new HashMap<>();
        views.put(fanCard, fansw);
        views.put(bulbCard, bulbsw);
        views.put(motorCard, motorsw);
        views.put(fansw, fansw);
        views.put(bulbsw, bulbsw);
        views.put(motorsw, motorsw);


        notificationHandler=new NotificationHandler(getSystemService(NotificationManager.class));
        notificationManager = NotificationManagerCompat.from(this);
    }

    public void activateControls(boolean flag){
        fanCard.setEnabled(flag);
        bulbCard.setEnabled(flag);
        motorCard.setEnabled(flag);
        fansw.setEnabled(flag);
        bulbsw.setEnabled(flag);
        motorsw.setEnabled(flag);
    }

    public void toast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,25);
        toast.show();
    }
}
