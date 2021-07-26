using System.Collections.Generic;
using animationparameters;
using thrift.gen_csharp;
using thrift.services;
using time;

namespace thrift
{
    public abstract class APReceiver : Receiver
    {
        private int numberOfAP;
        public TimeController timer;

        public APReceiver(int numberOfAP_) : base()
        {
            numberOfAP = numberOfAP_;
            timer = new TimeController();
        }

        public APReceiver(int numberOfAP_, int port) : base(port)
        {
            numberOfAP = numberOfAP_;
            timer = new TimeController();
        }

        public AnimationParametersFrame newAnimParamFrame(int frameNumber)
        {
            return new AnimationParametersFrame(frameNumber);
        }

        public List<AnimationParametersFrame> getGretaAPFrameList(Message m)
        {
            //if(!timer.isSynchronized())
            setCurrentTime(m);

            // Debug.Log("Message id: "+m.Id +", type: "+ m.Type +", time: "+ m.Time +", frameNumber: "+ m.APFrameList[0].FrameNumber + ", number of frames: " + m.APFrameList.Count);
            return thriftAPFrameList2gretaAPFrameList(m.APFrameList);
        }

        public void setCurrentTime(Message m)
        {
            timer.setTimeMillis(m.Time);
        }

        public List<AnimationParametersFrame> thriftAPFrameList2gretaAPFrameList(List<ThriftAnimParamFrame> thriftAPframes)
        {
            List<AnimationParametersFrame> gretaAPFrameList = new List<AnimationParametersFrame>(thriftAPframes.Count);
            foreach (ThriftAnimParamFrame thriftFrame in thriftAPframes)
            {
                AnimationParametersFrame gretaFrame = new AnimationParametersFrame(numberOfAP, (long)thriftFrame.FrameNumber);
                List<ThriftAnimParam> thriftAPList = thriftFrame.AnimParamList;
                int i = 0;
                foreach (ThriftAnimParam thriftAP in thriftAPList)
                {
                    gretaFrame.setAnimationParameter(i, thriftAP.Value, thriftAP.Mask);
                    ++i;
                }
                gretaAPFrameList.Add(gretaFrame);
            }
            return gretaAPFrameList;
        }
    }
}
