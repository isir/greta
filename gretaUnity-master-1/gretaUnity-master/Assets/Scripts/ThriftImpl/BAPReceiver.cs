using animationparameters;
using thrift;
using thrift.gen_csharp;

namespace thriftImpl
{
    public class BAPReceiver : APReceiver
    {
        APFramesList apFramesList;

        public BAPReceiver(int numOfAP, int port) : base(numOfAP, port)
        {
            apFramesList = new APFramesList(new AnimationParametersFrame(296, 0));
        }

        public override void perform(Message m)
        {

            /* IMPULSION-VERVE */
            //Debug.Log ("+++++++++++++++++++ Message BAP received");

            apFramesList.addAPFrames(getGretaAPFrameList(m), m.Id);
        }

        public AnimationParametersFrame getCurrentFrame(long currentTime)
        {
            //Debug.Log ("current frame: "+ apFramesList.getCurrentFrame(currentTime));
            return apFramesList.getCurrentFrame(currentTime);
        }
    }
}
