package com.example.smarthomeautomation_mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingActivity extends AppCompatActivity {

    FloatingActionButton save;
    TextView local, global, current_ip, current_port;
    EditText ip, port;

    static String MODE="local";
    static String LOCAL_IP="192.168.43.1";
    static String LOCAL_PORT="1883";
    static String GLOBAL_IP="klinux.tk";
    static String GLOBAL_PORT="1883";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        local=findViewById(R.id.local);
        global=findViewById(R.id.global);
        ip=findViewById(R.id.ip);
        port=findViewById(R.id.port);
        current_ip=findViewById(R.id.current_ip);
        current_port=findViewById(R.id.current_port);
        current_ip.setText("Current IP is: "+LOCAL_IP);
        current_port.setText("Current Port is: "+LOCAL_PORT);
        save=findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ip.getText().toString().equals("") && !port.getText().toString().equals("")){
                    if(MODE.equals("local")){
                        LOCAL_IP=ip.getText().toString();
                        LOCAL_PORT=port.getText().toString();
                    }
                    else {
                        GLOBAL_IP=ip.getText().toString();
                        GLOBAL_PORT=port.getText().toString();
                    }
                }
                startActivity(new Intent(SettingActivity.this, MainActivity.class));
            }
        });

    }

    public void setMode(View v){
        if(v==local){
            MODE="local";

            ip.setHint("Local IP Address");
            port.setHint("Local Port Number");
            current_ip.setText("Current IP is: "+LOCAL_IP);
            current_port.setText("Current Port is: "+LOCAL_PORT);
            local.setText("Local IP \uf058");
            global.setText("Public IP \uf111");
        }
        else {
            MODE="global";
            ip.setHint("Public IP Address");
            port.setHint("Public Port Number");
            current_ip.setText("Current IP is: "+GLOBAL_IP);
            current_port.setText("Current Port is: "+GLOBAL_PORT);
            local.setText("Local IP \uf111");
            global.setText("Public IP \uf058");
        }
    }
}
