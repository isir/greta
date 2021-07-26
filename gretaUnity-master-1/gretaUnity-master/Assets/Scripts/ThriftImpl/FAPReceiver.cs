using animationparameters;
using thrift;
using thrift.gen_csharp;

namespace thriftImpl
{
    public class FAPReceiver : APReceiver
    {
        APFramesList apFramesList;

        public FAPReceiver(int numOfAP, int port) : base(numOfAP, port)
        {
            apFramesList = new APFramesList(new AnimationParametersFrame(69, 0));
        }

        public FAPReceiver(int numOfAP, int port, int type) : base(numOfAP, port)
        { // type is local or distant
            apFramesList = new APFramesList(new AnimationParametersFrame(69, 0));
        }

        public override void perform(Message m)
        {
            apFramesList.addAPFrames(getGretaAPFrameList(m), m.Id);
        }

        public AnimationParametersFrame getCurrentFrame(long currentTime)
        {
            //Debug.Log ("current frame: "+ apFramesList.getCurrentFrame(currentTime));
            return apFramesList.getCurrentFrame(currentTime);
        }
    }
}
