import opensmile
from pydub import AudioSegment
import time
import socket
import numpy as np
import pickle 
import scipy.io.wavfile as wavf


fs = 44100  # Sample rate
#seconds = 3  # Duration of recording


#myrecording = sd.rec(int(seconds * fs), samplerate=fs, channels=2)
#sd.wait()  # Wait until recording is finished
#write('output.wav', fs, myrecording)  # Save as WAV file 

smile = opensmile.Smile(
    feature_set='opensmile_realtime.conf',
    feature_level='funcconcat',
)
ip = "10.51.16.223"
port = 4444
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, 0)
t1 = 0 #Works in milliseconds
t2 = 40
newAudio = AudioSegment.from_wav("output1.wav")
size=len(newAudio)
while(t2<=size):
    start=time.time()
    
    # Fixed version of smile.process_signal
    x_sig = np.reshape(newAudio[t1:t2], (1,len(newAudio[t1:t2]))) #(1,int(fs*0.04))
    x_sig = smile.process_signal(x_sig, fs)
    x_sig = x_sig.mean()
    x = pickle.dumps((x_sig))
    
    # Wrong version of smile.process_signal
    #x=pickle.dumps((smile.process_signal(newAudio[t1:t2].get_array_of_samples(),fs).values[-1]))
    s.sendto(x, (ip, port))
    end = time.time()
    
    #print(smile.process_signal(newAudio[t1:t2].get_array_of_samples(),fs).values[0][0], )
    #newAudio[t1:t2].export("output3.wav", format="wav")
    print(end-start)
    t1,t2=t1+40,t2+40
    samples = newAudio[t1:t2].get_array_of_samples()
    print("[INFO]",t1,t2, size, size%40,newAudio.channels)
    if((size%40)!=0 and t2>size):
        print(size)
        samples=np.concatenate((newAudio.get_array_of_samples(), np.zeros(40-size%40)),axis=None)
        print(size,len(samples))

