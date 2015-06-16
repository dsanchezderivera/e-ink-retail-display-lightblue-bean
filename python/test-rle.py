import binascii
import SocketServer
import BaseHTTPServer
import struct
from bitstring import BitArray, BitStream


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
            b = bytearray()
            compress = bytearray()
            b.extend(data)
            a = BitArray(b)
            binstring = a.bin
            uncompressed = 1
            index = 0
            while uncompressed:
                cnt = 0
                pixel = a[index]
                while ((index < a.length) & (pixel == a[index])) & (cnt < 255):
                    index += 1
                    cnt += 1
                    if index == a.length:
                        uncompressed = 0
                        break
                tempint = cnt
                if pixel:
                    tempint = cnt + 128
                compress.append(tempint)
            print "Sin comprimir: {}".format(len(b))
            print "Comprimido: {}".format(len(compress))


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


def tobits(s):
    result = []
    for c in s:
        bits = bin(ord(c))
        bits = '00000000'[len(bits):] + bits
        result.extend([int(b) for b in bits])
    return result


def bin(s):
    return str(s) if s <= 1 else bin(s >> 1) + str(s & 1)


if __name__ == "__main__":
    # databits = '0b111100001010'
    # print databits
    # n = int('0b111100001010', 2)
    # print n
    # strbin = bin(n)
    # print strbin
    # print "hello"
    # print bin(int("hello", 16))

    HOST, PORT = "localhost", 9996
    # Create the server, binding to localhost on port 9999
    server = SocketServer.TCPServer((HOST, PORT), MyHandler)
    server.timeout = 0.25
    while 1:
        server.handle_request()
