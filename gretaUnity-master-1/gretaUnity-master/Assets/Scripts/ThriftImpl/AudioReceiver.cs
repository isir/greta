using audioElements;
using thrift.gen_csharp;
using thrift.services;
using time;

namespace thriftImpl
{
    public class AudioReceiver : Receiver
    {
        public TimeController timer;
        private string idCurrentAudio;
        AudioElementList audioElementList;
        bool newAudio;

        public AudioReceiver() : base()
        {
            audioElementList = new AudioElementList();
            timer = new TimeController();
            newAudio = false;
            idCurrentAudio = "default";
        }

        public AudioReceiver(int port) : base(port)
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
            //EB : I need to recover the raw data buffer from the message and to create an AudioElement that contains such a buffer

            if (m.Binary_content.Length > 0)
                audioElementList.addAudioElement(new AudioElement(m.Id, m.String_content, m.Time, m.Binary_content, sampleRate));
            else
                audioElementList.addAudioElement(new AudioElement(m.Id, m.String_content, m.Time));
        }

        public AudioElement getCurrentAudioElement(long currentTime)
        {
            //Debug.Log ("current frame: "+ apFramesList.getCurrentFrame(currentTime));
            //Debug.Log ("DANS LA LIST AUDIO : " + audioElementList.Count());
            AudioElement audioElement = audioElementList.getCurrentAudioElement(currentTime);
            //Debug.Log ("idCurrentaudio " + idCurrentAudio + "; id peeked audio "+audioElement.getId());
            if (audioElement != null && audioElement.getId() != idCurrentAudio && audioElement.getName() != "")
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

        public void ClearList()
        {
            this.audioElementList.Clear();
        }
    }
}
