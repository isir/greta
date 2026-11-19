import socket
import keyboard
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
        
        response = greta_conn.recv(1024).decode()
        print("greta:", response)
        if "Generator started" in response:
            # greta_conn.send(message.encode())
            break
    
    while True:
        
        try:
            
            # lock.acquire()
            # if feedback == "reactive":
            #     message = "turnShift"
            # else:
            #     message = "reactive"
            # lock.release()
                
            response = greta_conn.recv(1024).decode()
            print("greta:", response)

            greta_conn.send(message.encode())
            
            # input()
        
        except Exception as e:
            
            print("greta error:", e)
            input()
        
def feedback_loop(feedback_conn, lock):
    
    message = "end"
    message_id = '0'

    message_choice = {'0':'end', '1':'start'}

    print('greta speech end: 0, greta speech start: 1 (now: {})'.format(message))
    
    loop_cnt = 0
    
    prev_message_id = message_id
    
    while True:
        
        try:

            # message_id = inputimeout(prompt='greta speech end: 0, greta speech start: 1 (now: {})'.format(message), timeout=0.01)
            # message_id = inputimeout(prompt='greta speech end: 0, greta speech start: 1 (now: {})'.format(message), timeout=0.01)
            
            if keyboard.is_pressed('0'):
                curr_message_id = '0'
            elif keyboard.is_pressed('1'):
                curr_message_id = '1'
            else:
                curr_message_id = prev_message_id
            
            
            # if not(message_id in message_choice):
            #     message_id = '0'
            
            if curr_message_id != prev_message_id:
                
                message_id = curr_message_id
                prev_message_id = curr_message_id

                message = message_choice[message_id]            
                
                # lock.acquire()
                # if feedback == "start":
                #     message = "end"
                # else:
                #     message = "start"
                # lock.release()
                    
                feedback_conn.send(message.encode())
                
                response = feedback_conn.recv(1024).decode()
                # print("feedback:", response)

                print('greta speech end: 0, greta speech start: 1 (now: {})'.format(message))

        
        except Exception as e:
            
            print("feedback error:", e)
            input()


if __name__ == "__main__":
    main()