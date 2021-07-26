using UnityEngine;
using animationparameters;
using thrift;
using thrift.gen_csharp;

public class ThriftReceiver : MonoBehaviour
{
    InstantiationAPReceiver apReceiver;

    // Use this for initialization
    void Start()
    {
        Debug.Log("Start");
        apReceiver = new InstantiationAPReceiver(69, 9090);
        apReceiver.startConnection();
        Debug.Log("Receiver started");
    }

    // Update is called once per frame
    void Update()
    {
        if (apReceiver.timer.isSynchronized())
        {
            AnimationParametersFrame currentAPFrame = apReceiver.getCurrentFrame(apReceiver.timer.getTimeMillis() / 40);

            if (currentAPFrame != null)
            {
                Debug.Log(apReceiver.timer.getTimeMillis() / 40 + " " + currentAPFrame.AnimationParametersFrame2String());
            }
        }
    }

    void OnApplicationQuit()
    {
        apReceiver.stopConnector();
    }

}

public class InstantiationAPReceiver : APReceiver
{
    APFramesList apFramesList;
    bool firstMessageReceived;

    public InstantiationAPReceiver(int numOfAP, int port) : base(numOfAP, port)
    {
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
