using UnityEngine;

namespace audioElements
{
    public class AudioElement
    {
        private string name;
        private string id;
        public byte[] rawData;
        private float[] data;
        private int sample, frequency, channels, sampleRate;
        private float length;
        private bool isBigEndian;
        private long frameNumber;

        public AudioElement()
        {
            id = "default";
            name = "default";
            frameNumber = -1;
            rawData = null;
            length = 0;
        }

        //EB : I had to add a new constructor that can initialize the raw data buffer received by VIB
        //BR : I added the sampleRate
        public AudioElement(string id_, string name_, long frameNumber_, byte[] rawData_, int sampleRate)
        {
            id = id_;
            name = name_;
            frameNumber = frameNumber_;
            this.sampleRate = sampleRate;
            rawData = new byte[rawData_.Length];
            for (int iPCM = 0; iPCM < rawData_.Length; iPCM++)
                rawData[iPCM] = rawData_[iPCM];
            length = rawData.Length;
            Debug.Log("audio element created " + id + " " + name + " " + frameNumber + " " + length);

        }

        public AudioElement(string id_, string name_, long frameNumber_)
        {
            id = id_;
            name = name_;
            frameNumber = frameNumber_;
            rawData = null;
            length = 0;
            // Debug.Log("audio element created "+ id + " " + name + " " + frameNumber);
        }

        public string getName()
        {
            return name;
        }

        public void setName(string name_)
        {
            name = name_;
        }

        public void setId(string id_)
        {
            id = id_;
        }
        public string getId()
        {
            // Debug.Log ("audio element id asekd" + id);
            return id;
        }

        public long getFrameNumber()
        {
            return frameNumber;
        }

        public void setFrameNumber(long frameNumber_)
        {
            frameNumber = frameNumber_;
        }

        //EB : I added new get and set methods to read and modify the raw data buffer
        public float getLength()
        {
            return length;
        }

        public void setLength(float l)
        {
            length = l;
        }

        public byte[] getRawData()
        {
            return rawData;
        }

        public void setRawData(byte[] rd)
        {
            rawData = new byte[rd.Length];
            length = rd.Length;
            for (int i = 0; i < length; i++)
                rawData[i] = rd[i];
        }

        //BR : retrieve the sample rate
        public int getSampleRate()
        {
            return sampleRate;
        }

        public void setSampleRate(int sr)
        {
            sampleRate = sr;
        }
    }
}

