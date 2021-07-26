using System;
using System.IO;
using UnityEngine;

public class Utilities
{
    public static float distancePoint(Vector3 v1, Vector3 v2, float isX, float isY, float isZ)
    {
        return (Mathf.Sqrt((v1.x - v2.x) * (v1.x - v2.x) * isX + (v1.y - v2.y) * (v1.y - v2.y) * isY + (v1.z - v2.z) * (v1.z - v2.z) * isZ));
    }

    public static Vector3 middlePoint(Vector3 v1, Vector3 v2)
    {
        Vector3 v = new Vector3((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
        return v;
    }

    // convert string line to value array
    public static float[] StringToArray(string input, string separator, int bias)
    {
        string[] stringList = input.Split(separator.ToCharArray(), StringSplitOptions.RemoveEmptyEntries);
        float[] list = new float[stringList.Length + bias];
        list[0] = 0.0f;
        for (int i = 0; i < stringList.Length; i++)
            list[i + bias] = (float)Convert.ChangeType(stringList[i], typeof(float));

        return list;
    }

    public static float total(float x, float y)
    {
        return (x + y);
    }

    public static float claim(float val, float infThres, float supThres)
    {
        if (val < infThres)
            val = infThres;
        if (val > supThres)
            val = supThres;
        return val;
    }

    public static float radianInBAPToAngle(float radian)
    {
        return (radian * 180.0f) / (100000.0f * Mathf.PI);
    }

    public static void writeTest(string fileName, string text)
    {
        FileStream testFile = new FileStream(fileName, FileMode.Append, FileAccess.Write);
        StreamWriter tw = new StreamWriter(testFile);
        tw.WriteLine(text);
        tw.Close();
        testFile.Close();
    }

    public static float ValueInBAPToTranslation(float val)
    {
        return (val / 50000000000.0f);
    }

    /*
    public Image CaptureScreen()
    {
    Rectangle screenSize = Screen.PrimaryScreen.Bounds;
    Bitmap target = new Bitmap(screenSize.Width,screenSize.Height);
    using(Graphics g = Graphics.FromImage(target))
    {
    g.CopyFromScreen(0,0,0,0,new Size(screenSize.Width,screenSize.Height));
    }
    return target;
    }
    */
}
