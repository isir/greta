using audioElements;
using thrift.gen_csharp;
using thrift.services;
using time;

namespace thriftImpl
{
    public class AudioExternalClient : ExternalClient
    {
        public TimeController timer;
        private string idCurrentAudio;
        AudioElementList audioElementList;
        bool newAudio;

        public AudioExternalClient() : base()
        {
            audioElementList = new AudioElementList();
            timer = new TimeController();
            newAudio = false;
            idCurrentAudio = "default";
        }

        public AudioExternalClient(string host, int port) : base(host, port)
        {
            audioElementList = new AudioElementList();
            timer = new TimeController();
            newAudio = false;
            idCurrentAudio = "default";
        }

        public bool isNewAudio()
        {
            return newAudio;
        }

        public void setNewAudio(bool newAudio_)
        {
            newAudio = newAudio_;
        }

        public override void perform(Message m)
        {
            // Debug.Log ("AudioReceived: "+ m.Type + " " + m.Id+ " " + m.Time + " " +m.String_content);
            setCurrentTime(m);
            //BR : retrieve the sample rate
            string s_sampleRate = "";
            float f_sampleRate = 16000;
            m.Properties.TryGetValue("sampleRate", out s_sampleRate);
            if (s_sampleRate != null)
            {
                float.TryParse(s_sampleRate, out f_sampleRate);
            }
            int sampleRate = (int)f_sampleRate;
            //EB: do you need the possibility to read the buffer even in this case? I didn't know how to test
            // this code. If you don't need the raw data buffer just erase the if and the else and keep just
            // the last line
            if (m.Binary_content.Length > 0)
                audioElementList.addAudioElement(new AudioElement(m.Id, m.String_content, m.Time, m.Binary_content, sampleRate));
            else
                audioElementList.addAudioElement(new AudioElement(m.Id, m.String_content, m.Time));

        }

        public AudioElement getCurrentAudioElement(long currentTime)
        {
            //Debug.Log ("current frame: "+ apFramesList.getCurrentFrame(currentTime));
            AudioElement audioElement = audioElementList.getCurrentAudioElement(currentTime);
            // Debug.Log ("idCurrentaudio " + idCurrentAudio + "; id peeked audio "+audioElement.getId());
            if (audioElement.getId() != idCurrentAudio && audioElement.getName() != "")
            {
                newAudio = true;
                idCurrentAudio = "" + audioElement.getId();
                return audioElement;
            }
            else {
                return null;
            }
        }

        public void setCurrentTime(Message m)
        {
            timer.setTimeMillis(m.Time);
        }
    }
}
