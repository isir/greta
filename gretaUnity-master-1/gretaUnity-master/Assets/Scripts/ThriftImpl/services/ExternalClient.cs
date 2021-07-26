using System;
using UnityEngine;
using System.Runtime.CompilerServices;
using System.Threading;


using thrift.gen_csharp;

using Thrift;
using Thrift.Protocol;
using Thrift.Transport;
using Thrift.Collections;

namespace thrift.services
{
	public abstract class ExternalClient : Connector
	{
		TSocket transport;
		ExternalCom.Client client;
		Thread extClientCheckConnectionThread;
		Thread extClientUpdateMessageThread;
		Message message;
		bool newMessage;
		int cptThreadStarted;
		bool starting;
		
		public ExternalClient (): this(DEFAULT_THRIFT_HOST,DEFAULT_THRIFT_PORT)
		{
		}

		public ExternalClient (String host, int port): base(host, port)
		{
			message = new Message ();
			message.Id = "initMessage";
			newMessage = false;	
			cptThreadStarted = 0;
		}

		~ExternalClient ()
		{
			if (transport != null) {
				Debug.Log ("Closing the simple client...");
				transport.Close ();
			}
		}

		public void updateMessage ()
		{
			//Debug.Log ("updateMessage in thread: "+Thread.CurrentThread.GetHashCode());
			if (this.isConnected ()) {
				try {
					lock (client) {
						long requestLength = DateTime.Now.Ticks/10000;
						Message m = client.update (message.Id);
						requestLength = DateTime.Now.Ticks/10000 - requestLength;
/* Debug request length !!!*/	Debug.Log (" requestLength " + requestLength);
						if (m.Type != "empty") {
							newMessage = true;
							message = m;
							//	Debug.Log("new message received on port: "+this.getPort ()+" "+m.Id +" "+m.FirstFrameNumber +" "+ m.LastFrameNumber);
						} else if (m.Type == "empty"){
							newMessage = false;
						} else {
							client.isStarted ();
							
						}
					}
				} catch (Exception ex) {
					Debug.Log ("Cannot get update from ServerToExternal on " + this.getHost () + " " + this.getPort () + " "+ex.Message + ex.StackTrace + ex.InnerException);
				//	Debug.Log ("Check that there is no remaining java.exe running");
					/*if (isConnected ()) {
						if(!this.client.isStarted ()){
							startConnection ();
						}
					}*/
					//updateMessage ();
				}
			} else {
				Debug.Log ("Thrift ExternalClient not connected");
			}
		}
    
		public bool isNewMessage ()
		{
			return newMessage;
		}
    
		public Message getMessage ()
		{
			//    Debug.Log("new message " + message.Id +" received on the client");
			return message;
		}
		public override void startConnector ()
		{
			try {
				lock (this) {
					this.transport = new TSocket (this.getHost (), this.getPort ());
					this.transport.Open ();
					this.transport.Timeout=1000;
					TProtocol protocol = new TBinaryProtocol (this.transport);
					this.client = new ExternalCom.Client (protocol);
					if (this.client != null) {
						setConnected (true);
					//	Debug.Log ("Connected to " + this.getHost () + " - " + this.getPort ());
						/*     extClientCheckConnectionThread = new Thread(new ThreadStart(ExternalClientConnectionCheckThread));
					extClientCheckConnectionThread.IsBackground = true; 
	                extClientCheckConnectionThread.Start();*/
						
						if(extClientUpdateMessageThread==null){
							extClientUpdateMessageThread = new Thread (new ThreadStart (ExternalClientUpdateMessageThread));
							extClientUpdateMessageThread.IsBackground = true; 
							extClientUpdateMessageThread.Start ();
						}
						
					}
				}
			} catch (Exception ex) {
				Console.Error.WriteLine ("Exception " + ex.Message);
			}
		}
    
		public override void stopConnector ()
		{
			if(this.isOnConnection()  || this.isConnected ()){
		//	Debug.Log ("stopConnector this.transport.IsOpen " + this.transport.IsOpen);
				if (this.transport.IsOpen) {
					this.transport.Close ();
				}
					
				this.setConnected(false);
			}
		//	Debug.Log ("stopConnector this.transport.IsOpen " + this.transport.IsOpen);
		}

		public void send (Message m)
		{
			try {
				lock (client) {
					client.send (m);
				}
			} catch (Exception ex) {
				Debug.Log ("Cannot send Message on " + this.getHost () + " " + this.getPort ()+ " " + ex.StackTrace);
			//	Debug.Log ("Check that there is no remaining java.exe running");
				if (isConnected ()) {
					startConnection ();
				}
			}
		}

		public new void setPort (int port)
		{
			if (getPort () != port) {
				base.setPort (port);
			}
		}

		public new void setPort (String port)
		{
			setPort (Convert.ToInt16 (port));
		}

		public new void setHost (String host)
		{
			if (!getHost ().Equals (host)) {
				base.setHost (host);
			}
  
		}

		public void ExternalClientUpdateMessageThread ()
		{
        
			cptThreadStarted ++;
		//	Debug.Log ("externalClientUpdateMessageThread started number: " + cptThreadStarted);
			while (Thread.CurrentThread==extClientUpdateMessageThread) {
				if(isConnected()){
						updateMessage ();
						if (isNewMessage ()) {
							perform (this.getMessage ());
						}
				}
				try {
					Thread.Sleep (100);
				} catch (Exception ex) {
					Debug.Log (ex);
				}
                

			}
			cptThreadStarted --;
		//	Debug.Log ("externalClientUpdateMessageThread stopping number: " + cptThreadStarted);        
				
		}

		public abstract void perform (Message m);
		
		public void ExternalClientConnectionCheckThread ()
		{
        
		//	Debug.Log ("ExternalClientConnectionCheckThread start");
			try {
				while (isConnected()) {
					bool clientStarted = false;
					lock (this.client) {
						clientStarted = this.client.isStarted ();
					}
					if (clientStarted) {
						try {
							Thread.Sleep (100);
						} catch (Exception ex) {
							Debug.Log (ex);
						}
					}
				}
			} catch (Exception e) {
				Debug.Log (e);
				this.startConnection ();

			}
		}
	

	}
	

}