#include "config.h"
int pins[4]={14,12,13,15};

AdafruitIO_Feed *getFeed = io.feed("get-extra");
AdafruitIO_Feed *postFeed = io.feed("post-extra");

void setPinMode(){
  pinMode(pins[0], OUTPUT); 
  pinMode(pins[1], OUTPUT); 
  pinMode(pins[2], OUTPUT); 
  pinMode(pins[3], OUTPUT); 
}
void initFromROM(){
  EEPROM.begin(512);
  for(int i=0; i<4; i++){
    Serial.print((EEPROM.read(i)));
    digitalWrite(pins[i], (EEPROM.read(i))==0);
  }
}

void setup() {
  Serial.begin(9600);
  while(! Serial);
  setPinMode();
  initFromROM();
  io.connect();
  getFeed->onMessage(handleMessage);
  getFeed->get();
}

void loop() {
  io.run();
}

void handleMessage(AdafruitIO_Data *data) {
  int code=atoi(data->value());
  if(code<16)
    parseMessage(code);
}

void parseMessage(int code){
  postFeed->save(code);
  
  int states[4]={0,0,0,0};
  int i=0;
  
  while(code>1){
    int bit=code%2;
    code/=2;
    states[i++]=bit;
  }
  states[i]=code;
  
  Serial.println();
  for(int j=3; j>=0; j--){
    EEPROM.write(j, states[j]);
    Serial.print(states[j]);
  }
  EEPROM.commit();

  for(int j=3; j>=0; j--)
    digitalWrite(pins[j], states[j]==0);
}
