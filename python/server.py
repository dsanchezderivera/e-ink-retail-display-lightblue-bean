import SocketServer
import BaseHTTPServer
import serial

ser = serial.Serial('/tmp/tty.LightBlue-Bean', 57600, timeout=0.25) # wait 250msec for bean

def isfloat(value):
  try:
    float(value)
    return True
  except ValueError:
    return False

class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith('/white'):
            print "{} send a WHITE!".format(self.client_address[0])
            ser.write("white\n")
            print "Sent!"
            self.send_response(200)

        elif self.path.startswith('/black'):
            print "{} send a BLACK!".format(self.client_address[0])
            ser.write("black\n")
            print "Sent!"
            self.send_response(200)
    def do_POST(self):  # Receive a image in the post data 
        if self.path.startswith('/image'):
            self.send_response(200)
            varLen = int(self.headers['Content-Length'])
            print "{} send a POST image with length: {}".format(self.client_address[0], varLen)
            # The image data received
            data = self.rfile.read(varLen)
            print data
            print "Sending Image to LBB"
            # Send to the LBB first string which indicates a image
            ser.write("image\n")
            # First response from Lbb indicates wating for the first line
            keep = 0
            print "Waiting response from LBB"
            enviando = True
            while enviando:
                datarcv = ser.read(1)              # read one, blocking
                n = ser.inWaiting()             # look if there is more
                if n:
                    datarcv = datarcv + ser.read(n)   # and get as much as possible
                if datarcv:
                    print "LBB response: {}".format(datarcv)
                    #if float, send line
                    if isfloat(datarcv):
                    # keep = keep+1
                        print "Is float"
                        print "Sending data packet: {}".format(datarcv)
                        tmpString = data[((int(datarcv))*33):(((int(datarcv))*33)+33)]
                        # print tobits(tmpString)
                        ser.write(tmpString)
                        if((int(datarcv))==174):
                            enviando = False
                    else:
                        print "Not float"
            print "Finish!"
            

def reader():
        datarcv1 = ser.read(1)              # read one, blocking
        n = ser.inWaiting()             # look if there is more
        if n:
            datarcv1 = datarcv1 + ser.read(n)   # and get as much as possible
        if datarcv1:
            print "LBB sent: {}".format(datarcv1)

def tobits(s):
    result = []
    for c in s:
        bits = bin(ord(c))[2:]
        bits = '00000000'[len(bits):] + bits
        result.extend([int(b) for b in bits])
    return result

if __name__ == "__main__":
    HOST, PORT = "localhost", 9996

    # Create the server, binding to localhost on port 9999
    server = SocketServer.TCPServer((HOST, PORT), MyHandler)
    server.timeout = 0.25
    while 1:
        server.handle_request()
        reader()


