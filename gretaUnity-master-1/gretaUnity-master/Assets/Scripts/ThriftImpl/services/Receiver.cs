using System;
using UnityEngine;
using System.Threading;
/**
 *
 * @author Ken
 */


using thrift.gen_csharp;
using Thrift;
using Thrift.Protocol;
using Thrift.Transport;
using Thrift.Collections;
using Thrift.Server;

namespace thrift.services
{
	public abstract class Receiver : Connector
	{
 	private SimpleComHandler handler;
    private SimpleCom.Processor processor;
    TServerTransport serverTransport;
    TServer server=null;
    Thread receiverThread;
    Message message;

    public Receiver():this(DEFAULT_THRIFT_PORT) {
        
    }

    public Receiver(int port) : base(DEFAULT_THRIFT_HOST, port){
        
    }
		
    public override void startConnector() {
        try {
            handler = new SimpleComHandler(this);
            processor = new SimpleCom.Processor(handler);
            receiverThread = new Thread(new ThreadStart(startingSimpleServer));
			receiverThread.IsBackground = true; 
            receiverThread.Start();
        } catch (Exception x) {
            Debug.LogError(x);
        }
    }
    
    public override void stopConnector(){
		if(server!=null){
			Debug.Log("Stoping the Receiver...");
           server.Stop();
           serverTransport.Close();
				Debug.Log("Receiver stopped");
       }    
			
		if(receiverThread!=null && receiverThread.IsAlive){
            receiverThread.Abort();
				
        }		   
   
         setConnected(false);
    }
		
    public void startingSimpleServer() {
        
            Debug.Log("Try to start Receiver SimpleServer on " + getHost() + " - " + getPort());
            try {
                serverTransport = new TServerSocket(getPort());
                server = new TSimpleServer(processor,serverTransport);

                // Use this for a multithreaded server
                // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                Debug.Log("Starting the simple server...");
                setConnected(true);
                server.Serve();
            } catch (Exception e) {
                Debug.LogError(e);
            }
        }
    

    public abstract void perform(Message m);
	}
}

