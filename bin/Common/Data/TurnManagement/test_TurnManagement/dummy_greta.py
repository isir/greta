import socket
from threading import Thread, Lock

def main():
    
    feedback_server_host = socket.gethostname()
    feedback_server_port = 5960
    
    greta_host = socket.gethostname()
    greta_port = 5961
    
    RATE = 16000
    
    vad_interval = 0.01
    
    feedback_server = socket.socket()
    feedback_server.bind((feedback_server_host, feedback_server_port))
    feedback_server.settimeout(0.1)
    feedback_server.listen(10)

    greta_server = socket.socket()
    greta_server.bind((greta_host, greta_port))
    greta_server.settimeout(0.1)
    greta_server.listen(10)
    
    lock = Lock()
    
    while True:
        
        try:
            
            feedback_conn, address = feedback_server.accept()
            feedback_thread = Thread(target = feedback_loop, args = (feedback_conn, lock))
            feedback_thread.daemon = True
            feedback_thread.start()
            
            greta_conn, address = greta_server.accept()
            greta_conn.send('ok'.encode())
            greta_thread = Thread(target = greta_loop, args = (greta_conn, lock))
            greta_thread.daemon = True
            greta_thread.start()
        
        except:
            
            pass

def greta_loop(greta_conn, lock):
    
    # feedback = "reactive"
    
    message = 'ok'
    
    while True:
        
        response = greta_conn.recv(100).decode()
        print(response)
        if "Generator started" in response:
            greta_conn.send(message.encode())
            break
    
    while True:
        
        try:
            
            # lock.acquire()
            # if feedback == "reactive":
            #     message = "turnShift"
            # else:
            #     message = "reactive"
            # lock.release()
                
            response = greta_conn.recv(100).decode()
            print("greta:", response)

            greta_conn.send(message.encode())
            
            # input()
        
        except Exception as e:
            
            print("greta error:", e)
            input()
        
def feedback_loop(feedback_conn, lock):
    
    message = "end"
    
    while True:
        
        try:
            
            # lock.acquire()
            # if feedback == "start":
            #     message = "end"
            # else:
            #     message = "start"
            # lock.release()
                
            feedback_conn.send(message.encode())
            
            response = feedback_conn.recv(100).decode()
            # print("feedback:", response)
        
        except Exception as e:
            
            print("feedback error:", e)
            input()


if __name__ == "__main__":
    main()