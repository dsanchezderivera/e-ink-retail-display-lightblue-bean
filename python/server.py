import SocketServer
import BaseHTTPServer
import serial

ser = serial.Serial('/tmp/tty.LightBlue-Bean', 57600, timeout=0.25) # wait 250msec for bean

class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith('/white'):
            print "{} send a WHITE!".format(self.client_address[0])
            ser.write("white\\n")
            print "Sent!"
            self.send_response(200)

        elif self.path.startswith('/black'):
            print "{} send a BLACK!".format(self.client_address[0])
            ser.write("black\\n")
            print "Sent!"
            self.send_response(200)
    def do_POST(self):
        if self.path.startswith('/image'):
            self.send_response(200)
            varLen = int(self.headers['Content-Length'])
            print "{} send a POST image with length: {}".format(self.client_address[0], varLen)
            data = self.rfile.read(varLen)
            print data

def reader():
        data = ser.read(1)              # read one, blocking
        n = ser.inWaiting()             # look if there is more
        if n:
            data = data + ser.read(n)   # and get as much as possible
        if data:
            print "LBB sent: {}".format(data)


if __name__ == "__main__":
    HOST, PORT = "localhost", 9996

    # Create the server, binding to localhost on port 9999
    server = SocketServer.TCPServer((HOST, PORT), MyHandler)
    server.timeout = 0.25
    while 1:
        server.handle_request()
        reader()

    