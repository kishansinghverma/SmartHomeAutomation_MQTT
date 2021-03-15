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
    private String localClientId = MqttClient.generateClientId();
    private String cloudClientId = MqttClient.generateClientId();

    private static MqttManager mqttManager=new MqttManager();
    public MqttAndroidClient localClient;
    public MqttAndroidClient cloudClient;
    private MqttAndroidClient activeClient;
    private MqttConnectOptions localConnectOptions;
    private MqttConnectOptions cloudConnectOptions;
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

            cloudConnectOptions = new MqttConnectOptions();
            cloudConnectOptions.setUserName("kishansinghverma");
            cloudConnectOptions.setPassword("Kishan.123".toCharArray());
            cloudConnectOptions.setCleanSession(true);
            cloudConnectOptions.setConnectionTimeout(5);
            cloudConnectOptions.setAutomaticReconnect(true);
            cloudClient = new MqttAndroidClient(context, "tcp://klinux.ml", cloudClientId);

            localConnectOptions = new MqttConnectOptions();
            localConnectOptions.setCleanSession(true);
            localConnectOptions.setConnectionTimeout(1);
            localClient = new MqttAndroidClient(context, "tcp://192.168.1.100", localClientId);

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
            activeClient.publish(topic, String.valueOf(code).getBytes(), 1, false);
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
            activeClient.subscribe(topics, new int[]{0, 0});
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }

    public void registerCallbacks(final MqttAndroidClient client){
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

                if (client == localClient) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectCloud();
                        }
                    }, 3000);
                }
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

    public void connectLocal(){
        registerCallbacks(localClient);
        cloudClient.unregisterResources();

        layoutManager.showSnackbar("Connecting To Local Server...");
        try {
            IMqttToken token = localClient.connect(localConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    activeClient = localClient;
                    subscribeToTopic();
                    layoutManager.Toast("Connected To Local Server!");
                    layoutManager.activateControls(true);
                    layoutManager.hideSnackBar();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    layoutManager.activateControls(false);
                    layoutManager.showSnackbar("Local Connection Failed!");
                    connectCloud();
                }
            });
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }

    public void connectCloud(){
        registerCallbacks(cloudClient);
        localClient.unregisterResources();
        
        layoutManager.showSnackbar("Connecting To Cloud Server...");
        try {
            IMqttToken token = cloudClient.connect(cloudConnectOptions);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    activeClient = cloudClient;
                    subscribeToTopic();
                    layoutManager.Toast("Connected To Cloud Server!");
                    layoutManager.activateControls(true);
                    layoutManager.hideSnackBar();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    layoutManager.activateControls(false);
                    layoutManager.showSnackbar("Cloud Connection Failed!");
                }
            });
        } catch (MqttException e) {Log.d("Error", e.toString());}
    }
}
