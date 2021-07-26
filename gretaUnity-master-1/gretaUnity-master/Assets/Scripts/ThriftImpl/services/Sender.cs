using System;
using UnityEngine;
using System.Runtime.CompilerServices;


using thrift.gen_csharp;

using Thrift;
using Thrift.Protocol;
using Thrift.Transport;
using Thrift.Collections;

namespace thrift.services
{
	public class Sender : Connector
	{
		TTransport transport;
		SimpleCom.Client client;

		public Sender (): this(DEFAULT_THRIFT_HOST,DEFAULT_THRIFT_PORT)
		{
		}

		public Sender (String host, int port): base(host, port)
		{
		}

		~Sender ()
		{
			if (transport != null) {
				Debug.Log ("Closing the simple client...");
				transport.Close ();
			}
		}

		public override void startConnector ()
		{
			try {
				lock (this) {
					this.transport = new TSocket (this.getHost (), this.getPort ());
					TProtocol protocol = new TBinaryProtocol (this.transport);
					this.client = new SimpleCom.Client (protocol);
					this.transport.Open ();
					Debug.Log ("Connected to " + this.getHost () + " - " + this.getPort ());
					Debug.Log ("sender to String: " + this.client.ToString ());
					if (this.client != null) {
						setConnected (true);
					}
				}
			} catch (Exception ex) {
				Debug.LogError("Exception " + ex.Message);
			}
		}
    
		public override void stopConnector ()
		{
			if (this.isOnConnection () || this.isConnected ()) {
				//	Debug.Log ("stopConnector this.transport.IsOpen " + this.transport.IsOpen);
				if (this.transport.IsOpen) {
					this.transport.Close ();
				}
					
				this.setConnected (false);
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
				Debug.Log ("Can not send Message. " + ex.StackTrace);
				Debug.Log ("Check that the right Receiver is connected on " + this.getHost () + " " + this.getPort ());
				Debug.Log ("Check that there is no remaining java.exe running");
				/*  if (isConnected()) {
                startConnection();
            }*/
			}
		}

		public new void setPort (int port)
		{
			if (getPort () != port) {
				base.setPort (port);
				stopConnector ();
				//startConnection ();
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
				stopConnector ();
				//startConnection ();
			}
    
		}
	}


}