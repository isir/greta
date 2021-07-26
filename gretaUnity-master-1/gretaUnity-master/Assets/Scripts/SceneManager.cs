using System;
using System.IO;
using UnityEngine;

public class SceneManager : MonoBehaviour
{
    public int height = Screen.height;
    public int width = Screen.width;
    public static bool isThrift = true;

    public const string url2 = "tcp://localhost:61616";
    public const string url1 = "tcp://137.194.54.80:61615/";

    public const string FILEURL = "Config"; // file stores list of URLs

    internal static object getGetActiveScene()
    {
        throw new NotImplementedException();
    }

    public static string BAPTopic = "BAP";
    public static string FAPTopic = "FAP";
    public static string AUDIOTopic = "";
    public static string ProducerTopic = "";


    public static float globalTime = 0;
    public static float gretaClock = -1;
    public static long currentFrame = -1;
    public static float globalTimeUnity = 0;
    public static float initUnityClock = -1;
    public const float DELAY = 0.0F;

    // change your target postion and zCamera here

    public static Vector3 iTarget = new Vector3(0.0f, 1.40f, 0f);
    public static float xCamera = 0.0f;
    public static float yCamera = 0.0f;
    public static float zCamera = -5.0f;

    public bool showPanel;

    public static string[] connectionState = new string[] { "Disconnected", "Connected" };
    public static bool isConnectedThrift = false;

    string readURL(string fileName)
    {
        TextAsset asset = Resources.Load(fileName) as TextAsset;
        string text = asset.text;

        using (StringReader reader = new StringReader(text))
        {
            string line = reader.ReadLine();
            while ((line.Length == 0) || (line[0] == '#'))
                line = reader.ReadLine();
            return line;
        }
    }

    void closeThrift()
    {
        isConnectedThrift = false;
        //...
    }

    void initThrift()
    {
        //...
    }

    // Use this for initialization
    void Awake()
    {
        showPanel = false;
        initThrift();
    }

    // Update is called once per frame
    void Update()
    {
        globalTimeUnity = Time.realtimeSinceStartup;
        if (gretaClock > 0)
        {
            currentFrame = (long)((gretaClock) / 40);
        }
        else
            globalTime = 0;

        if (Input.GetKey(KeyCode.Tab))
            showPanel = !showPanel;
    }

    void OnGUI()
    {
        if (showPanel)
        {
            GUI.Label(new Rect(width - 200, 30, 200, 100), "Player Clock: " + timeToFormat(globalTimeUnity));
            GUI.Label(new Rect(width - 200, 50, 200, 100), "Greta Clock: " + timeToFormat(globalTime));
            GUI.Label(new Rect(width - 200, 70, 200, 100), "Frame Number: " + currentFrame);
        }
    }

    public void OnApplicationQuit()
    {
        closeThrift();
    }


    public static string timeToFormat(float time)
    {
        if (time < 0)
            time = 0;
        long timeSec = (long)time;
        int minute = (int)timeSec / 60;
        int sec = (int)timeSec % 60;
        int hour = minute / 60;
        minute = minute % 60;
        int timeMili = (int)((time - timeSec) * 1000);
        return (hour.ToString() + "h:" + minute.ToString() + "m:" + sec.ToString() + "s:" + timeMili.ToString());
    }
}
