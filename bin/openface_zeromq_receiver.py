import zmq
import socket

context = zmq.Context()

#  Socket to talk to server
#print("Connecting to hello world server…")
#socket_zmq= context.socket(zmq.SUB)
#socket_zmq.connect("tcp://localhost:5000")
#socket_zmq.setsockopt(zmq.SUBSCRIBE, b'')


#UDP_IP = "10.51.18.246"
#UDP_PORT = 4000
#MESSAGE = "Hello, World!"

#print("UDP target IP: %s" % UDP_IP)
#print("UDP target port: %s" % UDP_PORT)
#print("message: %s" % MESSAGE)

#sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM) # UDP
#sock.sendto(MESSAGE.encode(), (UDP_IP, UDP_PORT))


ip = "10.51.18.246"
port = 100
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM,0)
s.connect((ip,port))
while(True):
    #message=socket_zmq.recv()
    #print("Received reply %s" % (message))
    #c'est bon n'enleve surtout pas!
    send_data="Something"
    s.sendto(send_data.encode('utf-8'), (ip, port))
    #print("\n\n 1. Client Sent : ", send_data, "\n\n")
    #data, address = s.recvfrom(4096)
    #print("\n\n 2. Client received : ", data.decode('utf-8'), "\n\n")
#  Do 10 requests, waiting each time for a response  Get the reply.


#while(True):
 #   print("listening")
    #message=socket_zmq.recv()
    #print("Received reply %s" % (message))
#    sock = socket.socket(socket.AF_INET, # Internet
#    socket.SOCK_DGRAM) # UDP
#    sock.sendto(MESSAGE.encode(), (UDP_IP, UDP_PORT))
    
