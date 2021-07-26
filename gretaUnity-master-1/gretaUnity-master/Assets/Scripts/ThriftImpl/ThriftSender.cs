using System;
using UnityEngine;
using thrift.gen_csharp;
using thrift.services;

public class ThriftSender : MonoBehaviour
{
    Sender sender;
    Message message;
    int cpt;

    // Use this for initialization
    void Start()
    {
        Debug.Log("Start");
        message = new Message();
        Debug.Log("new message created");
        cpt = 0;
        sender = new Sender("localhost", 9095);
        Debug.Log("new sender created");
        sender.startConnection();
        Debug.Log("sender connection started");
    }

    // Update is called once per frame
    void Update()
    {
        if (sender.isConnected())
        {
            message.Type = "trou de balle";
            message.Time = 2;
            message.Id = Convert.ToString(cpt);
            sender.send(message);
            cpt++;
        }
    }
}
