package com.example.smarthomeautomation_mqtt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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

public class MqttManager {

    private Context context;
    private String clientId = MqttClient.generateClientId();
    private static MqttManager mqttManager=new MqttManager();
    private MqttAndroidClient  client;
    private MqttConnectOptions mqttConnectOptions;
    private LayoutManager layoutManager;
    public boolean[] mainBoard=new boolean[5];
    public boolean[] extraBoard=new boolean[4];

    public void setContext(Context context){
        this.context=context;
    }
    private MqttManager(){}
    public static MqttManager getInstance(){
        return mqttManager;
    }

    public void initializeComponents(){
        if(context!=null) {
            layoutManager = LayoutManager.getInstance();

            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(3);
            client = new MqttAndroidClient(context, "tcp://192.168.1.100:1883", clientId);

        }
        else
            throw new IllegalStateException("Context Not Defined For Client!");
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
            Toast.makeText(context, "Not Switched!", Toast.LENGTH_SHORT);
            Log.d("Error", e.toString());
        }
    }

    private void parseMessage(String topic, MqttMessage mqttMessage){
        int code=0;
        String msg = new String(mqttMessage.getPayload());

        try { code=Integer.parseInt(msg); }
        catch (NumberFormatException ex){
            layoutManager.Toast("Data Format Error!");
            return;
        }

        if(topic.equals("post/main")){
            if(code>31){
                layoutManager.Toast("Invalid Data Recieved");
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
                    layoutManager.switches[j].setChecked(mainBoard[j]);
                }
            }
        }
        else if(topic.equals("post/extra")){
            if(code>15){
                layoutManager.Toast("Invalid Data Recieved");
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
                    layoutManager.switches[j+5].setChecked(extraBoard[j]);
                }
            }
        }
    }

    private void subscribeToTopic(){
        String[] topics={"post/main", "post/extra"};
        try {
            client.subscribe(topics, new int[]{0, 0});
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }

    public void registerCallbacks(){
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect){
                    subscribeToTopic();
                    layoutManager.Toast("Reconnected To Server!");
                    layoutManager.activateControls(true);
                    layoutManager.hideSnackBar();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                layoutManager.activateControls(false);
                layoutManager.showSnackbar(cause.toString());
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

    public void connect(){
        layoutManager.showSnackbar("Connecting To Server...");
        try {
            IMqttToken token = client.connect(mqttConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                    layoutManager.Toast("Connected To Server!");
                    layoutManager.activateControls(true);
                    layoutManager.hideSnackBar();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    layoutManager.activateControls(false);
                    layoutManager.showSnackbar("Connection Failed!");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mqttManager.connect();
                        }
                    }, 2000);
                }
            });
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }
}
