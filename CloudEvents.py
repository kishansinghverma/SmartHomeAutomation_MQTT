import threading
from Adafruit_IO import MQTTClient
import paho.mqtt.client as mqtt

ADAFRUIT_IO_KEY = 'aio_HbUd53uVxWWj7FThZuOn2dftY68a'
ADAFRUIT_IO_USERNAME = 'kishansinghverma'
GLOBAL_FEED = 'device'
client = MQTTClient(ADAFRUIT_IO_USERNAME, ADAFRUIT_IO_KEY)
lClient = mqtt.Client()

extraBit=0
mainBit=0

def processCode(devs, states):
    
    for i in range(5-len(mainBit)):
        mainBit.insert(0, 0)
        
    for i in range(4-len(extraBit)):
        extraBit.insert(0, 0)

    mainBit.reverse()
    extraBit.reverse()
    
    for i, x in enumerate(devs):
        if(x>5):
            extraBit[x-6]=states[i]
        else:
            mainBit[x-1]=states[i]
    print(extraBit, mainBit)

    mbit=0
    xbit=0

    for i in range(len(mainBit)):
        mbit+=mainBit[i]*(2**i)
        
    for i in range(len(extraBit)):
        xbit+=extraBit[i]*(2**i)

    lClient.publish("get/main", payload=mbit, qos=0, retain=True)
    lClient.publish("get/extra", payload=xbit, qos=0, retain=True)

    
        

def connected(client):
    print('Connected to Adafruit IO!  Listening for {0} changes...'.format(GLOBAL_FEED))
    client.subscribe(GLOBAL_FEED)

def subscribe(client, userdata, mid, granted_qos):
    print('Subscribed to {0} with QoS {1}'.format(GLOBAL_FEED, granted_qos[0]))

def disconnected(client):
    print('Disconnected from Adafruit IO!')

def message(client, feed_id, payload):
    global extraBit
    global mainBit
    
    devs, states=(str(payload)).split("/")
    devs=list(map(int, devs))
    states=list(map(int, states))
    print(feed_id, payload)
    processCode(devs, states)

def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("post/#")

def on_message(client, userdata, msg):
    global extraBit
    global mainBit
    
    if(msg.topic == "post/extra"):
        extraBit=list(map(int, bin(int(msg.payload.decode('utf-8')))[2:]))
    elif(msg.topic == "post/main"):
        mainBit=list(map(int, bin(int(msg.payload.decode('utf-8')))[2:]))
        
    print(msg.topic, int(msg.payload.decode('utf-8')))

def runGlobal():
    client.on_connect    = connected
    client.on_disconnect = disconnected
    client.on_message    = message
    client.on_subscribe  = subscribe
    client.connect()
    client.loop_blocking()
    
def runLocal():
    lClient.on_connect = on_connect
    lClient.on_message = on_message
    lClient.connect("127.0.0.1", 1883, 60)
    lClient.loop_forever()

localConn=threading.Thread(target=runLocal)
globalConn=threading.Thread(target=runGlobal)

globalConn.start()
localConn.start()

