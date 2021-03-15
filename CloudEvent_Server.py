import http.server
import socketserver
from urllib.parse import urlparse
from urllib.parse import parse_qs
import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish

creds={'username':'kishansinghverma', 'password':'Kishan.123'}

devs={
    "dual lights" : {'main':[1]},
    "fan" : {'main':[2]},
    "main socket" : {'main':[2]},
    "cfl light" : {'main':[8]},
    "single light" : {'main':[16]},
    "heavy socket" : {'extra':[1]},
    "light socket" : {'extra':[2]},
    "low light" : {'extra':[4]},
    "outer light" : {'extra':[8]},
    "all lights" : {'main':[1,8,16], 'extra':[4]},
    "all system" : {'main':[1,2,8,16], 'extra':[1,4]}
    }

states=['0', '1']
def sum(a, b):
    return a|b
def minus(a, b):
    return (a&b)^a

def sendPayload(dev, state):
    msg="Success : Status Updated"
    
    main = subscribe.simple("post/main", auth=creds)
    extra = subscribe.simple("post/extra", auth=creds)
    mBit, xBit=0,0

    try:
        mBit=eval(str(main.payload, 'UTF-8'))
        xBit=eval(str(extra.payload, 'UTF-8'))
    except:
        return "Error While Getting Current Status"

    data=devs[dev]
    
    if(state == '0'):
        if('main' in data.keys()):
            for x in data['main']:
                mBit=minus(mBit, x)
        if('extra' in data.keys()):
            for x in data['extra']:
                xBit=minus(xBit, x)
            
    if(state == '1'):
        if('main' in data.keys()):
            for x in data['main']:
                mBit=sum(mBit, x)
        if('extra' in data.keys()):
            for x in data['extra']:
                xBit=sum(xBit, x)
                
    msgs=[{'topic':'get/main', 'payload':mBit}, {'topic':'get/extra', 'payload':xBit}]
    publish.multiple(msgs, auth=creds)

    return msg

def processEvent(event):
    msg="Unknown Error During Event Handling!"

    result=event.split("/");
    if(len(result)==2):
        dev, state=result
        if(dev in devs.keys()):
            if(state in states):
                msg=sendPayload(dev, state)
            else:
                msg="Invalid Status Code"
        else:
            msg="Please register this device";
    else:
        msg="Invalid Event";

    return msg;



class MyHttpRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):

        query_components = parse_qs(urlparse(self.path).query)
        if 'event' in query_components:
            self.send_response(200)
            self.send_header("Content-type", "text/html")
            self.end_headers()
            event = query_components["event"][0]
            msg=processEvent(event);
            html = f"<h1>"+msg+"</h1><br><h2>Event: "+event+"</h2>"
            self.wfile.write(bytes(html, "utf8"))

        else:
            self.send_response(400)
            self.send_header("Content-type", "text/html")
            self.end_headers()

        return

handler_object = MyHttpRequestHandler

PORT = 8585
my_server = socketserver.TCPServer(("", PORT), handler_object)
print("Starting Service on", PORT);
my_server.serve_forever()
