import SocketServer
import serial

ser = serial.Serial('/tmp/tty.LightBlue-Bean', 57600, timeout=0.25) # wait 250msec for bean

class MyTCPHandler(SocketServer.BaseRequestHandler):
    """
    The RequestHandler class for our server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.
    """

    def handle(self):
        # self.request is the TCP socket connected to the client
        self.data = self.request.recv(1024).strip()
        print "{} wrote:".format(self.client_address[0])
        print self.data
        ser.write("white\\n")
        # just send back the same data, but upper-cased
        self.request.sendall(self.data.upper())

def reader():
        data = ser.read(1)              # read one, blocking
        n = ser.inWaiting()             # look if there is more
        if n:
            data = data + ser.read(n)   # and get as much as possible
        if data:
            print data
            # escape outgoing data when needed (Telnet IAC (0xff) character)
            #data = serial.to_bytes(self.rfc2217.escape(data))
            #self._write_lock.acquire()




if __name__ == "__main__":
    HOST, PORT = "localhost", 9996

    # Create the server, binding to localhost on port 9999
    server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)
    server.timeout = 0.5
    while 1:
        server.handle_request()
        reader()

    