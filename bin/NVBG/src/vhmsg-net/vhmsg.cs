using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Web;

using KeyValuePairConsumer = System.Collections.Generic.KeyValuePair<string, Apache.NMS.IMessageConsumer>;


namespace VHMsg
{
   /// <remarks>
   /// This is the class that contains the message when received via the MessageEvent handler
   /// It is received as an argument from MessageEvent
   /// </remarks>
   public class Message : EventArgs
   {
      /// <summary>
      /// String containing the message
      /// </summary>
      public string s;

      /// <summary>
      /// properties containing the multikey portion
      /// </summary>
      public Dictionary<string, string> properties;

      /// <summary>
      /// Constructor
      /// </summary>
      /// <param name="s"></param>
      /// <param name="properties"></param>
      public Message(string s, Dictionary<string, string> properties)
      {
         this.s = s;
         this.properties = properties;
      }
   }



   /// <remarks>
   /// This is the main class for using this library
   /// </remarks>
   public class Client : IDisposable
   {
      private const string VHMSG_VERSION = "1.0.0.0";


      private bool m_disposed = false;

      private Apache.NMS.IConnection connection;
      private Apache.NMS.ISession m_session;
      private Apache.NMS.IDestination m_destination;
      private Apache.NMS.IMessageProducer m_Producer;

      private string m_host;
      private string m_port;
      private string m_scope;
      private bool m_immediateMethod = true;
      private bool m_subscribedAll = false;

      private List<KeyValuePairConsumer> m_consumers;

      // variables needed for polling
      private List<Message> m_messages = new List<Message>();
      private object m_messageLock = new object();
      private ManualResetEvent m_waitCondition = new ManualResetEvent(false);
      private object m_waitLock = new object();


      /// <summary>
      /// Delegate for the MessageEvent handler
      /// </summary>
      public delegate void MessageEventHandler(object sender, Message message);

      /// <summary>
      /// Message Handler.  Clients need to add their callback function to this handler to receive messages.
      /// </summary>
      public event MessageEventHandler MessageEvent;

      System.Text.Encoding encoding = System.Text.Encoding.UTF8;

      /// <summary>
      /// Gets the server being used for the connection
      /// </summary>
      public string Server
      {
         get { return m_host; }
      }


      /// <summary>
      /// Gets the server being used for the connection
      /// </summary>
      public string Port
      {
         get { return m_port; }
      }


      /// <summary>
      /// Gets/Sets the scope being used for the connection
      /// </summary>
      public string Scope
      {
         get { return m_scope; }
         set { m_scope = value; }
      }


      /// <summary>
      /// Constructor
      /// </summary>
      public Client()
      {
         m_consumers = new List<KeyValuePair<string, Apache.NMS.IMessageConsumer>>();
         SetServerFromEnvironment();
         SetPortFromEnvironment();
         SetScopeFromEnvironment();
      }


      /// <summary>
      /// Destructor, follows the IDisposable pattern to force the release of ActiveMQ resources
      /// </summary>
      ~Client()
      {
         Dispose(false);
      }


      /// <summary>
      /// Dispose method.  Manually releases ActiveMQ resources.  Follows the IDisposable pattern
      /// </summary>
      public void Dispose()
      {
         Dispose(true);

         GC.SuppressFinalize(this);  // tell the GC that the Finalize process no longer needs to be run for this object.
      }


      /// <summary>
      /// Dispose method.  Manually releases ActiveMQ resources.  Follows the IDisposable pattern
      /// </summary>
      /// <param name="disposeManagedResources"></param>
      protected virtual void Dispose(bool disposeManagedResources)
      {
         // process only if mananged and unmanaged resources have not been disposed of.
         if (!m_disposed)
         {
            if (disposeManagedResources)
            {
               m_waitCondition.Close();
               CloseConnection();
            }

            m_disposed = true;
         }
      }


      /// <summary>
      /// 
      /// </summary>
      protected void SetScopeFromEnvironment()
      {
         string scope = System.Environment.GetEnvironmentVariable("VHMSG_SCOPE");
         if (scope != null)
         {
            m_scope = scope;
         }
         else
         {
            m_scope = "NV";
         }
      }


      /// <summary>
      /// 
      /// </summary>
      protected void SetServerFromEnvironment()
      {
         string host = System.Environment.GetEnvironmentVariable("VHMSG_SERVER");
         if (host != null)
         {
            m_host = host;
         }
         else
         {
            m_host = "localhost";
         }
      }


      /// <summary>
      /// 
      /// </summary>
      protected void SetPortFromEnvironment()
      {
         string port = System.Environment.GetEnvironmentVariable("VHMSG_PORT");
         if (port != null)
         {
            m_port = port;
         }
         else
         {
            m_port = "61616";
         }
      }


      /// <summary>
      /// Opens a connection to the server.
      /// <para />
      /// By default, it uses 3 system environment variables as parameters
      /// <para />
      /// VHMSG_SERVER - This specifies the server to connect to.  It can either be an ip address or domain name
      /// <para />
      /// VHMSG_PORT - This specifies the port to connect to.  
      /// <para />
      /// VHMSG_SCOPE - A unique id used to distinguish messages sent by different modules using the same server.  For example, if two users
      /// are using the same server, they would set different scopes so that they wouldn't receives each other's messages.
      /// </summary>
      public void OpenConnection()
      {
         string user = null;     // ActiveMQConnection.DEFAULT_USER;
         string password = null; // ActiveMQConnection.DEFAULT_PASSWORD;
         string url = Apache.NMS.ActiveMQ.ConnectionFactory.DEFAULT_BROKER_URL;
         bool topic = true;
         //bool transacted = false;
         Apache.NMS.AcknowledgementMode ackMode = Apache.NMS.AcknowledgementMode.AutoAcknowledge;


         url = "tcp://" + m_host + ":" + m_port;


            //System.out.println("getConnection(): " + url + " " + m_scope );


         Apache.NMS.ActiveMQ.ConnectionFactory connectionFactory = new Apache.NMS.ActiveMQ.ConnectionFactory(new Uri(url));
         connection = connectionFactory.CreateConnection(user, password);
         //connection.setExceptionListener( this );
         connection.Start();

         //m_session = connection.CreateSession( transacted, ackMode );
         m_session = connection.CreateSession(ackMode);
         if (topic)
         {
            m_destination = m_session.GetTopic("NVBG");
         }
         else
         {
            m_destination = m_session.GetQueue("NVBG");
         }

         m_Producer = m_session.CreateProducer(m_destination);
         m_Producer.DeliveryMode = Apache.NMS.MsgDeliveryMode.NonPersistent;  // Persistent = false;  // m_Producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
      }


      /// <summary>
      /// Opens a connection to the server using a specified host.  See <see cref="OpenConnection()"/>.
      /// </summary>
      /// <param name="server">the host to connect to.  It can either be an ip address or domain name</param>
      /// <seealso cref="OpenConnection()"/>
      public void OpenConnection(string server)
      {
         m_host = server;
         OpenConnection();
      }


      /// <summary>
      /// Opens a connection to the server using a specified host.  See <see cref="OpenConnection()"/>.
      /// </summary>
      /// <param name="server">the host to connect to.  It can either be an ip address or domain name</param>
      /// <param name="port">the port to connect to.</param>
      /// <seealso cref="OpenConnection()"/>
      public void OpenConnection(string server, string port)
      {
         m_host = server;
         m_port = port;
         OpenConnection();
      }


      /// <summary>
      /// Closes the connection to the server that was previously opened via <see cref="OpenConnection()"/>.
      /// </summary>
      public void CloseConnection()
      {
         lock (m_messageLock)
         {
         }

         connection.Dispose();
         m_consumers.Clear();
      }


      /// <summary>
      /// Sends a message to the server using 2 arguments.  The first argument is the first word in the message, the second argument is the rest of the message.
      /// </summary>
      /// <param name="op">The first word in the message</param>
      /// <param name="arg">The rest of the message</param>
      public void SendMessage(string op, string arg)
      {
         op  = op.Trim();
         arg = arg.Trim();

         if (string.IsNullOrEmpty(op))
            return;


         string arg_encoded = HttpUtility.UrlEncode(arg, Encoding.UTF8);

         string mess = op + " " + arg_encoded;

         Apache.NMS.ActiveMQ.Commands.ActiveMQTextMessage message = (Apache.NMS.ActiveMQ.Commands.ActiveMQTextMessage)m_session.CreateTextMessage(mess);
         message.SetObjectProperty("ELVISH_SCOPE", m_scope);
         message.SetObjectProperty("MESSAGE_PREFIX", op);
         message.SetObjectProperty("VHMSG_VERSION", VHMSG_VERSION);
         message.SetObjectProperty("VHMSG", "VHMSG");
         message.SetObjectProperty("MESSAGE_TYPE_VHMSG", "VHMSG");
         //message.SetObjectProperty( op, arg );

         m_Producer.Send(message);
      }


      /// <summary>
      /// Sends a message to the server.  The argument contains the full message
      /// </summary>
      /// <param name="opandarg">The full message to send</param>
      public void SendMessage(string opandarg)
      {
         opandarg = opandarg.Trim();

         string op;
         string arg;

         int index  = opandarg.IndexOf(" ");
         if (index < 0)
         {
            op  = opandarg;
            arg = "";
         }
         else
         {
            op  = opandarg.Substring(0, index);
            arg = opandarg.Remove(0, index + 1);
         }

         SendMessage(op, arg);
      }


      /// <summary>
      /// Sends a message to the server where each item in the array is a separate word in the message.
      /// </summary>
      /// <param name="op">The first word in the message</param>
      /// <param name="args">An array containing the rest of the message.  Each item in the array is a separate word</param>
      public void SendMessage(string op, string[] args)
      {
         StringBuilder concatargs = new StringBuilder();

         for (int i = 0; i < args.Length; i++)
         {
            if (i == 0)
            {
               concatargs.Append(args[i]);
            }
            else
            {
               concatargs.Append(" ");
               concatargs.Append(args[i]);
            }
         }

         SendMessage(op, concatargs.ToString());
      }


      /// <summary>
      /// 
      /// </summary>
      public void EnablePollingMethod()
      {
         m_immediateMethod = false;
      }


      /// <summary>
      /// 
      /// </summary>
      public void EnableImmediateMethod()
      {
         m_immediateMethod = true;
      }


      /// <summary>
      /// Subscribes to a message.  This notifies the server that we are interested in messages that contain the given argument as the first word in the message.
      /// See <see cref="SendMessage(string,string)"/>
      /// <para />
      /// For each message that is received, the MessageEvent handler will be called for all listeners.  See <see cref="OnMessage"/>
      /// <para />
      /// More than one subscription can be made.  Alternatively, an asterisk (*) may be sent as a special-case argument that indicates we're interested in *all* messages.
      /// This should be used very sparingly because it can cause quite a bit of network traffic.
      /// </summary>
      /// <param name="req">Indicates what types of messages we are interested in receiving.  This tells the server to send messages where the first word matches req</param>
      public void SubscribeMessage(string req)
      {
         // check if we've already subscribed to this message
         foreach (KeyValuePairConsumer c in m_consumers)
         {
            if (c.Key == req)
            {
               return;
            }
         }

         // special case for asterisk.  If we pass in an asterisk, we are subscribing to all messages
         string messageSelector;
         if (req == "*")
         {
            messageSelector = "ELVISH_SCOPE = '" + m_scope + "' AND MESSAGE_PREFIX LIKE '%'";
            m_subscribedAll = true;
         }
         else
         {
            messageSelector = "ELVISH_SCOPE = '" + m_scope + "' AND MESSAGE_PREFIX = '" + req + "'";
         }


         //string messageSelector = "ELVISH_SCOPE = '" + m_scope + "' AND MESSAGE_PREFIX = '" + req + "'";
         //m_consumers.add( m_session.createDurableConsumer( m_destination, req, reqString, false ) );
         //try
         {
            //MessageConsumer c = m_session.createDurableSubscriber( (Topic)m_destination, req, messageSelector, false );
            Apache.NMS.IMessageConsumer c = m_session.CreateConsumer(m_destination, messageSelector);
            //MessageConsumer c = m_session.createConsumer( m_destination, null );
            //MessageConsumer c = m_session.createConsumer( m_destination );
            c.Listener += this.OnMessage;

            m_consumers.Add(new KeyValuePairConsumer(req, c));

            //System.out.println("subscribeMessage(): " + messageSelector);
         }

         // if we subscribed to "*", remove all the other listeners to prevent duplicate messages
         if (m_subscribedAll)
         {
            foreach (KeyValuePairConsumer c in m_consumers)
            {
               if (c.Key.CompareTo("*") != 0)
               {
                  c.Value.Listener -= this.OnMessage;
                  c.Value.Listener -= this.OnMessageIgnore;
                  c.Value.Listener += this.OnMessageIgnore;
               }
            }
         }
      }


      /// <summary>
      /// 
      /// </summary>
      public bool UnsubscribeMessage(string req)
      {
         // if we are unsubscribing from "*", re-add all the other listeners so that messages continue to be received (only once)
         if (req.CompareTo("*") == 0)
         {
            foreach (KeyValuePairConsumer c in m_consumers)
            {
                if (c.Key.CompareTo("*") != 0)
                {
                   c.Value.Listener -= this.OnMessage;
                   c.Value.Listener -= this.OnMessageIgnore;
                   c.Value.Listener += this.OnMessage;
                }
            }

            m_subscribedAll = false;
         }

         foreach (KeyValuePairConsumer c in m_consumers)
         {
            if (c.Key == req)
            {
               c.Value.Close();
               m_consumers.Remove(c);
               return true;
            }
         }

         return false;
      }


      /// <summary>
      /// 
      /// </summary>
      public int Poll()
      {
         int numMessages = 0;

         if (!m_immediateMethod)
         {
            for (;;)
            {
               Message args;

               lock (m_messageLock)
               {
                  if (m_messages.Count == 0)
                  {
                     break;
                  }

                  args = m_messages[0];
                  m_messages.RemoveAt(0);
               }

               MessageEvent(this, args);

               numMessages++;
            }
         }

         return numMessages;
      }


      /// <summary>
      /// 
      /// </summary>
      /// <param name="waitTimeSeconds"></param>
      public void WaitAndPoll(double waitTimeSeconds)
      {
         lock (m_waitLock)
         {
            // if there are already messages in the queue, process them and return (don't wait)
            int numMsgs = Poll();
            if (numMsgs > 0)
                return;

            int waitTimeMilliseconds = (int)(waitTimeSeconds * 1000);
            m_waitCondition.WaitOne(waitTimeMilliseconds);
            m_waitCondition.Reset();

            Poll();
         }
      }


      /// <summary>
      /// This function is a callback function received whenever an ActiveMQ message is received from the server.  It processes the message and passes it on to the client via
      /// the MessageEvent handler.
      /// </summary>
      /// <param name="msg">ActiveMQ message received from the server</param>
      protected void OnMessage(Apache.NMS.IMessage msg)
      {
         string message = "";
         //string elements  = null ;
         string temp = null;
         //string holder = null ;
         //StringTokenizer st = null;
         //int index = 0 ;
         Console.WriteLine("Stupid");
         //System.out.println("onMessage(): " + ((TextMessage)msg).getText() );

         //if ( msg instanceof TextMessage )
         {
            Apache.NMS.ActiveMQ.Commands.ActiveMQTextMessage txtMsg = (Apache.NMS.ActiveMQ.Commands.ActiveMQTextMessage)msg;
            temp = txtMsg.Text;
            
            temp = HttpUtility.UrlDecode(temp, encoding);

            temp = temp.Trim();

            /*
            // Strip off first char of args if it is a "
            if( temp.substring(0,1).compareToIgnoreCase("\"") == 0 )
            {
               temp = (temp.substring( 1, temp.length())).trim() ;
               if( temp.substring(0,1).compareToIgnoreCase("\"") == 0 )
               {
                  // if 2 double quotes at end, take one double quote off
                  message += temp.substring( 0, temp.length()-1 );
               }
               else
               {
                  message += "\"" + temp ;
               }
            }
            else
            {
               message += temp ;
            }
            */
            message = temp;

            Dictionary<string, string> properties = new Dictionary<string, string>();
            foreach (string key in txtMsg.Properties.Keys)
            {
                object data = txtMsg.Properties[key];
                string sData = data as string;
                if (sData != null)
                {
                    properties[key] = HttpUtility.UrlDecode(sData, encoding).Trim();
                }
            }

            Message args = new Message(message, properties);

            if (m_immediateMethod)
            {
               MessageEvent(this, args);
            }
            else
            {
               lock (m_messageLock)
               {
                  m_messages.Add(args);
               }

               // signal the other thread that we've received a message (only used in WaitAndPoll() )
               lock (m_waitLock)
               {
                  m_waitCondition.Set();
               }
            }
         }
      }

      /// <summary>
      /// This function is a callback function received whenever an ActiveMQ message is received from the server.  It ignores the message and returns immediately.
      /// </summary>
      /// <param name="msg">ActiveMQ message received from the server</param>
      protected void OnMessageIgnore(Apache.NMS.IMessage msg)
      {
          return;
      }
   }
}
