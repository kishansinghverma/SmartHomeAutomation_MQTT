package com.example.smarthomeautomation_mqtt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;
    MqttAndroidClient client;
    MqttConnectOptions mqttConnectOptions;
    Switch[] switches=new Switch[9];
    CardView[] cards=new CardView[9];
    HashMap<View, Integer> combo;
    Snackbar retry;
    boolean[] mainBoard=new boolean[5];
    boolean[] extraBoard=new boolean[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        connectToServer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void publishToTopic(String topic, boolean[] bits){
        int code=0;
        for(int i=0; i<bits.length; i++){
            if(bits[i])
                code+=Math.pow(2,i);
        }
        try {
            client.publish(topic, String.valueOf(code).getBytes(), 1, true);
        } catch (MqttException e) {
            toast("Not Switched!");
            Log.d("Error", e.toString());
        }
    }

    public void viewHandler(View v){
        int controlBit=combo.get(v);
        if(controlBit<5){
            mainBoard[controlBit]=!mainBoard[controlBit];
            publishToTopic("get/main", mainBoard);
        }
        else {
            extraBoard[controlBit-5]=!extraBoard[controlBit-5];
            publishToTopic("get/extra", extraBoard);
        }
    }

    public void subscribeToTopic(){
        String[] topics={"post/main", "post/extra"};
        try {
            client.subscribe(topics, new int[]{0, 0});
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }

    public void initComponents(){

        cards[0]=findViewById(R.id.dualbulbcard);
        cards[1]=findViewById(R.id.fancard);
        cards[2]=findViewById(R.id.mainsocketcard);
        cards[3]=findViewById(R.id.cflcard);
        cards[4]=findViewById(R.id.singlebulbcard);
        cards[5]=findViewById(R.id.heavysocketcard);
        cards[6]=findViewById(R.id.lightsocketcard);
        cards[7]=findViewById(R.id.wallbulbcard);
        cards[8]=findViewById(R.id.outerbulbcard);

        switches[0]=findViewById(R.id.dualbulbswitch);
        switches[1]=findViewById(R.id.fanswitch);
        switches[2]=findViewById(R.id.mainsocketswitch);
        switches[3]=findViewById(R.id.cflswitch);
        switches[4]=findViewById(R.id.singlebulbswitch);
        switches[5]=findViewById(R.id.heavysocketswitch);
        switches[6]=findViewById(R.id.lightsocketswitch);
        switches[7]=findViewById(R.id.wallbulbswitch);
        switches[8]=findViewById(R.id.outerbulbswitch);

        combo=new HashMap<>();
        for(int i=0; i<9; i++){
            combo.put(switches[i], i);
            combo.put(cards[i], i);
            switches[i].setEnabled(true);
            switches[i].setClickable(false);
        }

        layout=findViewById(R.id.layout);
        retry=Snackbar.make(layout, "Disconnected! Check Connection!", Snackbar.LENGTH_INDEFINITE);
        activateControls(false);

        String clientId = MqttClient.generateClientId();
        mqttConnectOptions=new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setConnectionTimeout(3);

        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.1.100:1883", clientId);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect){
                    toast("Reconnected To Server!");
                    subscribeToTopic();
                    activateControls(true);
                    retry.dismiss();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                activateControls(false);
                retry.setText(cause.toString()).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                parseMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("Info", "Message Delivered!");
            }
        });
    }

    public void activateControls(boolean flag){
        for(CardView cardView: cards){
            cardView.setEnabled(flag);
        }
    }

    public void toast(String message){
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void parseMessage(String topic, MqttMessage mqttMessage){
        int code=0;
        String msg = new String(mqttMessage.getPayload());

        try { code=Integer.parseInt(msg); }
        catch (NumberFormatException ex){
            toast("Data Format Error!");
            return;
        }

        if(topic.equals("post/main")){
            if(code>31){
                toast("Invalid Data Recieved");
            }
            else{
                int[] bits=new int[]{0,0,0,0,0};
                int i=0;
                while (code>1){
                    bits[i++]=code%2;
                    code/=2;
                }
                bits[i]=code;
                Collections.reverse(Arrays.asList(bits));
                for (int j=0; j<5; j++) {
                    mainBoard[j] = (bits[j] == 1);
                    switches[j].setChecked(mainBoard[j]);
                }
            }
        }
        else if(topic.equals("post/extra")){
            if(code>15){
                toast("Invalid Data Recieved");
            }
            else{
                int[] bits=new int[]{0,0,0,0};
                int i=0;
                while (code>1){
                    bits[i++]=code%2;
                    code/=2;
                }
                bits[i]=code;
                Collections.reverse(Arrays.asList(bits));
                for (int j=0; j<4; j++) {
                    extraBoard[j] = (bits[j] == 1);
                    switches[j+5].setChecked(extraBoard[j]);
                }
            }
        }
    }

    public void connectToServer(){
        retry.setText("Connecting To Server...").show();
        try {
            IMqttToken token = client.connect(mqttConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    toast("Connected To Server!");
                    subscribeToTopic();
                    activateControls(true);
                    retry.dismiss();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    activateControls(false);
                    retry.setText("Connection Failed!").show();
                }
            });
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }

    public void refreshData(View v){
        connectToServer();
    }

}
