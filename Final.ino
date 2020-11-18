#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <EEPROM.h>
#include <cstdlib>

const char* ssid = "HappyHome";
const char* password = "145789632*";
const char* mqtt_server = "192.168.1.100";
//int pins[4]={14,12,13,15};
int pins[5]={16,5,4,0,2};

WiFiClient espClient;
PubSubClient client(espClient);

void callback(char* topic, byte* payload, unsigned int length) {
  char buffer[length];
  byte msg[length];
  
  for (int i = 0; i < length; i++) {
    buffer[i]=(char)payload[i];
    msg[i]=payload[i];
  }
  
  client.publish("post/main", msg, length, true);
  int code = std::atoi(buffer);
  parseMessage(code);
}

void setPinMode(){
  pinMode(pins[0], OUTPUT); 
  pinMode(pins[1], OUTPUT); 
  pinMode(pins[2], OUTPUT); 
  pinMode(pins[3], OUTPUT); 
  pinMode(pins[4], OUTPUT); 
}

void initFromROM(){
  EEPROM.begin(512);
  for(int i=0; i<5; i++){
    Serial.print((EEPROM.read(i)));
    digitalWrite(pins[i], (EEPROM.read(i))==0);
  }
}

void setup_wifi() {

  delay(10);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  randomSeed(micros());
}

void reconnect() {
  while (!client.connected()) {
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      client.subscribe("get/main");
    } else {
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(9600);
  while(! Serial);
  setPinMode();
  initFromROM();
  setup_wifi();
  
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void loop() {

  if (!client.connected()) {
    reconnect();
  }
  client.loop();
}

void parseMessage(int code){
  int states[5]={0,0,0,0,0};
  int i=0;
  
  while(code>1){
    int bit=code%2;
    code/=2;
    states[i++]=bit;
  }
  states[i]=code;
  
  Serial.println();
  for(int j=4; j>=0; j--){
    EEPROM.write(j, states[j]);
    Serial.print(states[j]);
  }
  EEPROM.commit();

  for(int j=4; j>=0; j--)
    digitalWrite(pins[j], states[j]==0);
}
