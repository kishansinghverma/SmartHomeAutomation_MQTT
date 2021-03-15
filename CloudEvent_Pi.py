import threading
import paho.mqtt.client as mqtt
import paho.mqtt.subscribe as subscribe


LOCAL_HOST="127.0.01"
PORT=1883
GLOBAL_HOST="klinux.ml"
PASSWORD = 'Kishan.123'
USERNAME = 'kishansinghverma'

gClient = mqtt.Client()
gClient.username_pw_set(USERNAME, password=PASSWORD)
lClient = mqtt.Client()

def on_connect_global(client, userdata, flags, rc):
    print("Connected to Global Server")
    #For Syncing
    messages = subscribe.simple("post/#", msg_count=2)
    for msg in messages:
        gClient.publish(msg.topic, payload=msg.payload, qos=0, retain=True)
    
    gClient.subscribe("get/#")

def on_subscribe_global(client, userdata, mid, granted_qos):
    print("Subscribed to global feeds") 

def on_disconnect_global(client, userdata, rc):
    print('Disconnected from global server!',rc)

def on_message_global(client, userdata, msg):
    print("Global/"+msg.topic, "->", int(msg.payload.decode('utf-8')))
    lClient.publish(msg.topic, payload=msg.payload, qos=0, retain=True)

def on_connect_local(client, userdata, flags, rc):
    print("Connected to Local Server")
    client.subscribe("post/#")
    
def on_subscribe_local(client, userdata, mid, granted_qos):
    print("Subscribed to local feeds") 

def on_disconnect_local(client, userdata, rc):
    print('Disconnected from Local Server')

def on_message_local(client, userdata, msg):
    try:
        gClient.publish(msg.topic, payload=msg.payload, qos=0, retain=True)
    except:
        print("Unable to update on global server!");
        
    print("Local/"+msg.topic, '->', int(msg.payload.decode('utf-8')))

def runGlobal():
    gClient.on_connect = on_connect_global
    gClient.on_message = on_message_global
    gClient.on_subscribe = on_subscribe_global
    gClient.on_disconnect = on_disconnect_global
    
    gClient.connect(GLOBAL_HOST, PORT, 60)
    gClient.loop_forever()
    
def runLocal():
    lClient.on_connect = on_connect_local
    lClient.on_message = on_message_local
    lClient.on_subscribe = on_subscribe_local
    lClient.on_disconnect = on_disconnect_local
    
    lClient.connect(LOCAL_HOST, PORT, 60)
    lClient.loop_forever()
    
def startLocal():
    localConn=threading.Thread(target=runLocal)
    localConn.start()
    
def startGlobal():
    globalConn=threading.Thread(target=runGlobal)
    globalConn.start()

startLocal()
startGlobal()


