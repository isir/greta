using System;
using UnityEngine;
using System.Threading;

/**
 * @author: Ken
 *
 */

namespace thrift.services
{
	public abstract class Connector
	{
		
		public static String DEFAULT_THRIFT_HOST = "localhost";
		public static int DEFAULT_THRIFT_PORT = 9095;
		public static int SLEEP_TIME = 1000;
		private String host;
		private int port;
		private ConnectionListener connectionListener;
		Thread starterTh;
		private bool onConnection;
		/*
     * = isStarted for Servers
     */
		private bool connected;

		public Connector () : this(DEFAULT_THRIFT_HOST, DEFAULT_THRIFT_PORT)
		{
		}

		public Connector (String host, int port)
		{
			this.host = host;
			this.port = port;
			this.connected = false;
			this.onConnection=false;
		}

		~Connector ()
		{
			stopConnector ();			
		}

		public String getHost ()
		{
			return host;
		}

		public String getPortString ()
		{
			return Convert.ToString (port);
		}

		public int getPort ()
		{
			return port;
		}

		public void setHost (String host)
		{
			this.host = host;
			stopConnector ();
			//startConnection ();
		}

		public void setPort (int port)
		{
			this.port = port;
			stopConnector ();
			//startConnection ();
		}

		public void setPort (String port)
		{
			this.port = Convert.ToInt16 (port);
			stopConnector ();
			//startConnection ();
		}

		public void setURL (String host, String port)
		{
			setHost (host);
			setPort (port);
			stopConnector ();
			//startConnection ();
		}

		public void setURL (String host, int port)
		{
			setHost (host);
			setPort (port);
			stopConnector ();
			//startConnection ();
		}

		public void setConnectionLisnter (ConnectionListener connectionListener)
		{
			this.connectionListener = connectionListener;
		}

		public void setConnected (bool isConnected)
		{
			this.connected = isConnected;
			if (connectionListener != null) {
				if (connected) {
					connectionListener.onConnection ();
				} else {
					connectionListener.onDisconnection ();
				}
			}
		}

		public void startConnection ()
		{
			//Debug.Log ("in StartConnection");
			if (this.isConnected ()) {
				setConnected (false);
				this.stopConnector ();
			}  
			onConnection = true;
			starterTh = new Thread (new ThreadStart (connectionStarting));
			starterTh.IsBackground = true;  
			starterTh.Start ();
		}

		public void connectionStarting ()
		{
			
			int cpt = 1;
			while (!this.isConnected()&&Thread.CurrentThread == starterTh) {
				//Debug.Log ("Try to start connection on " + this.getHost () + " - " + this.getPort () + " " + cpt);
				cpt++;
				this.startConnector ();
				if (!this.isConnected ()) {
					try {
						Thread.Sleep (SLEEP_TIME);
					} catch (Exception ex1) {
						Debug.LogError (ex1);
					}
				} else {
					onConnection = false;
				}
			}
		}

		public abstract void startConnector ();

		public abstract void stopConnector ();

		public bool isConnected ()
		{
			return connected;
		}
		public bool isOnConnection(){
			return onConnection;
		}

		public void setConnectionListener (ConnectionListener connectionListener)
		{
			this.connectionListener = connectionListener;
		}
	}
}

