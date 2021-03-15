package com.example.smarthomeautomation_mqtt;

import android.app.Activity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class LayoutManager {

    private Activity activity;
    public Switch[] switches=new Switch[9];
    private CardView[] cards=new CardView[9];
    private ConstraintLayout layout;
    private HashMap<View, Integer> combo;
    private MqttManager mqtt;
    private Snackbar snackbar;

    private LayoutManager() {}
    public void setContext(Activity activity){this.activity=activity;}
    private static LayoutManager instance = new LayoutManager();
    public static LayoutManager getInstance () {
        return instance;
    }

    public void initializeComponents(){
        if (activity != null) {
            mqtt=MqttManager.getInstance();

            cards[0] = activity.findViewById(R.id.dualbulbcard);
            cards[1] = activity.findViewById(R.id.fancard);
            cards[2] = activity.findViewById(R.id.mainsocketcard);
            cards[3] = activity.findViewById(R.id.cflcard);
            cards[4] = activity.findViewById(R.id.singlebulbcard);
            cards[5] = activity.findViewById(R.id.heavysocketcard);
            cards[6] = activity.findViewById(R.id.lightsocketcard);
            cards[7] = activity.findViewById(R.id.wallbulbcard);
            cards[8] = activity.findViewById(R.id.outerbulbcard);

            switches[0] = activity.findViewById(R.id.dualbulbswitch);
            switches[1] = activity.findViewById(R.id.fanswitch);
            switches[2] = activity.findViewById(R.id.mainsocketswitch);
            switches[3] = activity.findViewById(R.id.cflswitch);
            switches[4] = activity.findViewById(R.id.singlebulbswitch);
            switches[5] = activity.findViewById(R.id.heavysocketswitch);
            switches[6] = activity.findViewById(R.id.lightsocketswitch);
            switches[7] = activity.findViewById(R.id.wallbulbswitch);
            switches[8] = activity.findViewById(R.id.outerbulbswitch);

            combo = new HashMap<>();
            for (int i = 0; i < 9; i++) {
                combo.put(switches[i], i);
                combo.put(cards[i], i);
                switches[i].setEnabled(true);
                switches[i].setClickable(false);
            }

            layout = activity.findViewById(R.id.layout);
            snackbar=Snackbar.make(layout, "", Snackbar.LENGTH_INDEFINITE);
            activateControls(false);
        }
        else
            throw new IllegalStateException("Activity Context Not Defined!");

    }

    public void activateControls(boolean flag){
        for(CardView cardView : cards){
            cardView.setEnabled(flag);
        }
    }

    public void viewHandler(View v){
        int controlBit=combo.get(v);
        if(controlBit<5){
            mqtt.mainBoard[controlBit]=!mqtt.mainBoard[controlBit];
            mqtt.publishToTopic("get/main", mqtt.mainBoard);
        }
        else {
            mqtt.extraBoard[controlBit-5]=!mqtt.extraBoard[controlBit-5];
            mqtt.publishToTopic("get/extra", mqtt.extraBoard);
        }
    }

    public void Toast(String message){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
    public void showSnackbar(String message){
        snackbar.setText(message).show();
    }
    public void hideSnackBar(){
        snackbar.dismiss();
    }
}
