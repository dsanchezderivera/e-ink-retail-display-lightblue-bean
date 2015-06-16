import SocketServer
import BaseHTTPServer
import serial
import binascii

# wait 250msec for bean
ser = serial.Serial('/dev/cu.LightBlue-Bean', 57600, timeout=0.25)


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
            print "{} send a POST image with length: {}".format(
                self.client_address[0], varLen)
            # The image data received
            data = self.rfile.read(varLen)

            # Binary!!!
            binary = bin(int(binascii.hexlify(data), 16))
            print binary
            #

            # # print data
            # print "Sending Image to LBB"
            # # Send to the LBB first string which indicates a image
            # ser.write("image\n")
            # # First response from Lbb indicates wating for the first line
            # keep = 0
            # print "Waiting response from LBB"
            # enviando = True
            # while enviando:
            #     datarcv = ser.read(1)  # read one, blocking
            #     n = ser.inWaiting()    # look if there is more
            #     if n:
            #         # and get as much as possible
            #         datarcv = datarcv + ser.read(n)
            #     if datarcv:
            #         print "LBB response: {}".format(datarcv)
            #         # if float, send line
            #         if isfloat(datarcv):
            #             # keep = keep+1
            #             print "Is float"
            #             print "Sending data packet: {}".format(datarcv)
            #             index = int(datarcv)
            #             if (index == 0):
            #                 print "Eneviando primera linea"
            #                 tmpString = data[0:33]
            #                 ser.write(tmpString)
            #             else:
            #                 print "Sending data packet: {}".format(index)
            #                 tmpString = data[(((index-1)*1155)+33):((((
            #                     index-1)*1155)+1155)+33)]
            #                 ser.write(tmpString)
            #             if((index) == 5):
            #                 enviando = False
            #         else:
            #             print "Not float"
            # print "Finish!"


def runlen(s):
    r = ""
    l = len(s)
    if l == 0:
        return ""
    if l == 1:
        return s + "1"

    last = s[0]
    cnt = 1
    i = 1
    while i < l:
        if s[i] == s[i - 1]:  # check it is the same letter
            cnt += 1
        else:
            r = r + s[i - 1] + str(cnt)  # if not, store the previous data
            cnt = 1
        i += 1
    r = r + s[i - 1] + str(cnt)
    return r


def reader():
        datarcv1 = ser.read(1)  # read one, blocking
        n = ser.inWaiting()     # look if there is more
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
