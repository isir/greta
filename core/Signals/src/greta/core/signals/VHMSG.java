package greta.core.signals;
/*
    This file is part of VHMsg written by Edward Fast at 
    University of Southern California's Institute for Creative Technologies.
    http://www.ict.usc.edu
    Copyright 2008 Edward Fast, University of Southern California

    VHMsg is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    VHMsg is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with VHMsg.  If not, see <http://www.gnu.org/licenses/>.
*/


import greta.core.signals.MessageEvent;
import greta.core.util.log.Logs;
import java.util.*;
import java.util.Map.*;
import java.util.AbstractMap.*;
import java.util.concurrent.locks.*;

import java.net.*;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 * This is a wrapper class for the client that performs all
 * the functionality required to send/receives messages
 */
public class VHMSG implements javax.jms.MessageListener
{
    private class IgnoreListener implements javax.jms.MessageListener
    {
        /**
         * This function is a callback function received whenever an ActiveMQ message is received from the server.  It ignores the messages and returns.
         *
         * @param msg  ActiveMQ message received from the server
         */
        public void onMessage( Message msg )
        {
            return;
        }
    }


    private static final String VHMSG_VERSION = "1.0.0.0";


    // ActiveMQ variables
    private Connection m_connection;
    private MessageProducer m_producer;
    private Session m_session;
    private Vector<SimpleEntry<String, MessageConsumer> > m_consumers = new Vector<SimpleEntry<String, MessageConsumer> >();
    private Destination m_destination;


    private String m_hostname;
    private String m_port;
    private String m_scope;

    private boolean m_isOpen = false;

    private Vector< MessageListener > m_iConsumers = new Vector< MessageListener >();

    private boolean m_immediateMethod = true;
    private boolean m_subscribedAll = false;
    private IgnoreListener m_ignoreListener = new IgnoreListener();

    // variables needed for polling
    private LinkedList< MessageEvent > m_messages = new LinkedList< MessageEvent >();
    private Object m_messageLock = new Object();
    private Lock m_waitLock = new ReentrantLock();
    private Condition m_waitCondition = m_waitLock.newCondition();


    public static final String VHMSG_SCOPE = "VHMSG_SCOPE";
    public static final String VHMSG_SERVER = "VHMSG_SERVER";
    public static final String VHMSG_PORT = "VHMSG_PORT";
    public static final String MESSAGE_PREFIX = "MESSAGE_PREFIX";
    public static final String STRING_ENCODING = "UTF8";
    public static final String MULTIKEY = "multikey";


    /**
     * Constructor.  This uses the default settings for host and scope.
     * By default, it uses 3 system environment variables as parameters
     * <p>
     * VHMSG_SERVER - This specifies the server to connect to.  It can either be an ip address or domain name
     * <p>
     * VHMSG_PORT - This specifies the port to connect to.  
     * <p>
     * VHMSG_SCOPE - A unique id used to distinguish messages sent by different modules using the same server.  For example, if two users
     * are using the same server, they would set different scopes so that they wouldn't receives each other's messages.
     */
    public VHMSG()
    {
        this( GetScopeFromEnvironment() );
    }


    /**
     * Constructor.  This uses the default setting to host, but uses the given scope.  It uses the default host based on the default constructor {@link VHMsg}
     *
     * @param scope  The scope to use
     */
    public VHMSG( String scope )
    {
        this( GetServerFromEnvironment(), GetPortFromEnvironment(), scope );
    }


    /**
     * Constructor.  This uses a given host and scope to override the default behavior.
     * 
     * @param server The server to use.  This can be an ip address or domain name
     * @param scope The scope to use.  See {@link VHMsg}
     */
    public VHMSG( String server, String scope )
    {
        this( server, GetPortFromEnvironment(), scope );
    }


    /**
     * Constructor.  This uses a given host and scope to override the default behavior.
     * 
     * @param server The server to use.  This can be an ip address or domain name
     * @param port The numeric port to use.
     * @param scope The scope to use.  See {@link VHMsg}
     */
    public VHMSG( String server, String port, String scope )
    {
        m_hostname = server;
        m_port = port;
        m_scope = scope;
    }


    /**
     * Opens a connection to the server
     * 
     *  @return boolean.   true if connected, false if error.  If connection is already opened, returns true.
     */
    public boolean openConnection()
    {
        if ( m_isOpen )
        {
            return true;
        }

        if ( m_hostname == null || m_hostname.equals( "" ) )
        {
            m_hostname = GetServerFromEnvironment();
        }

        if ( m_port == null || m_port.equals( "" ) )
        {
            m_port = GetPortFromEnvironment();
        }

        if ( m_scope == null || m_scope.equals( "" ) )
        {
            m_scope = GetScopeFromEnvironment();
        }

        try
        {
            String user = ActiveMQConnection.DEFAULT_USER;
            String password = ActiveMQConnection.DEFAULT_PASSWORD;
            String url = "tcp://" + m_hostname + ":" + m_port;
            boolean topic = true;
            boolean transacted = false;
            int ackMode = Session.AUTO_ACKNOWLEDGE;


            Logs.debug("getConnection(): " + url + " " + m_scope );


            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory( user, password, url );
            m_connection = connectionFactory.createConnection();
            //connection.setExceptionListener( this );
            m_connection.start();

            m_session = m_connection.createSession( transacted, ackMode );
            if ( topic ) 
            {
                //m_destination = m_session.createTopic( subject );
                m_destination = m_session.createTopic( m_scope );
            } 
            else 
            {
                m_destination = m_session.createQueue( m_scope );
            }

            m_producer = m_session.createProducer( m_destination );
            m_producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );



            //MessageConsumer consumer = null;
            //if (durable && topic) 
            //{
            //  consumer = session.createDurableSubscriber((Topic) destination, consumerName);
            //}

            //consumer.setMessageListener(this);

            m_isOpen = true;
        }
        catch ( Exception e )
        {
            handleException(e);
            return false;
        }

        return true;
    }


    /**
     * Opens a connection to the specified server.  Overrides what was given in the constructor.  See {@link VHMsg}
     *  
     * @param server  The server to use.  This can be an ip address or domain name
     * @return boolean.   true if connected, false if error.  If connection is already opened, returns true.
     */
    public boolean openConnection( String server )
    {
        m_hostname = server;

        return openConnection();
    }


    /**
     * Closes the connection to the server
     * 
     * @return boolean.  true is closed ok.  false if something bad happened.  Shouldn't fail unless something wrong happened within ActiveMQ internally.
     */
    public boolean closeConnection()
    {
        if( !m_isOpen )
        {
            return true;
        }

        m_isOpen = false;

        synchronized ( m_messageLock )
        {
        }

        try
        {
            m_connection.stop();
            m_connection.close();
        }
        catch ( JMSException e )
        {
            handleException(e);
            return false;
        }

        return true;
    }


    /**
     * Returns whether or not the client is connected to the server
     * 
     * @return  true if connected.  false if not
     */
    public boolean isConnected()
    {
        return m_isOpen;
    }


    public void addMessageListener( MessageListener pol )
    {
        m_iConsumers.addElement( pol );
    }


    public void removeMessageListener( MessageListener pol )
    {
        m_iConsumers.removeElement( pol );
    }

    
    public void enablePollingMethod()
    {
        m_immediateMethod = false;
    }


    public void enableImmediateMethod()
    {
        m_immediateMethod = true;
    }


    /**
     * Subscribes to a message type.  This notifies the server that we are interested in messages that contain the given argument as the first word in the message.
     * See {@link #sendMessage(String, String)}
     * <p>
     * For each message that is received, the MessageEvent handler will be called for all listeners.  See {@link #onMessage(Message)}
     * <p>
     * More than one subscribtion can be made.  Alternatively, an asterik (*) may be sent as a special-case argument that indicates we're interested in *all* messages.
     * This should be used very sparingly because it can cause quite a bit of network traffic.
     * 
     * @param req  Indicates what types of messages we are interested in receiving.  This tells the server to send messages where the first word matches req
     * @return  boolean.  true if the subscription was successful.  false if something wrong happened.  This could fail if the connection to the server was interrupted
     */
    public boolean subscribeMessage( String req )
    {
        // check if we've already subscribed to this message
        for ( SimpleEntry<String, MessageConsumer> c : m_consumers )
        {
            if ( c.getKey() == req )
            {
                return true;
            }
        }

        String messageSelector;

        // special case for asterisk.  If we pass in an asterisk, we are subscribing to all messages
        if ( req.equals( "*" ) )
        {
            messageSelector = "ELVISH_SCOPE" + " = '" + m_scope + "' AND " + MESSAGE_PREFIX + " LIKE '%'";
            m_subscribedAll = true;
        }
        else
        {
            messageSelector = "ELVISH_SCOPE" + " = '" + m_scope + "' AND " + MESSAGE_PREFIX + " = '" + req + "'";
        }

        //m_consumers.add( m_session.createDurableConsumer( m_destination, req, reqString, false ) );
        try
        {
            //MessageConsumer c = m_session.createDurableSubscriber( (Topic)m_destination, req, messageSelector, false );
            MessageConsumer c = m_session.createConsumer( m_destination, messageSelector );
            //MessageConsumer c = m_session.createConsumer( m_destination, null );
            //MessageConsumer c = m_session.createConsumer( m_destination );
            c.setMessageListener( this );
            m_consumers.add( new SimpleEntry<String, MessageConsumer>( req, c ) );

            Logs.debug("[NVBG INFO]:subscribeMessage(): " + messageSelector );
        }
        catch ( Exception e )
        {
            handleException(e);
        }

        // if we subscribed to "*", remove all the other listeners to prevent duplicate messages
        if (m_subscribedAll)
        {
            for ( SimpleEntry<String, MessageConsumer> c : m_consumers )
            {
                if (c.getKey() != "*")
                {
                    try
                    {
                        c.getValue().setMessageListener( m_ignoreListener );
                    }
                    catch ( Exception e )
                    {
                        handleException(e);
                    }
                }
            }
        }

        return true;
    }


    /**
     * Unsubscribes to the given message type 
     * @param req subscription token
     * @return true if successful
     */
    public boolean unsubscribeMessage( String req )
    {
        // if we are unsubscribing from "*", re-add all the other listeners so that messages continue to be received (only once)
        if ( req == "*" )
        {
            for ( SimpleEntry<String, MessageConsumer> c : m_consumers )
            {
                try
                {
                    c.getValue().setMessageListener( this );
                }
                catch ( Exception e )
                {
                    handleException(e);
                }
            }

            m_subscribedAll = false;
        }

        for ( SimpleEntry<String, MessageConsumer> c : m_consumers )
        {
            if ( c.getKey() == req )
            {
                try
                {
                    c.getValue().close();
                }
                catch ( Exception e )
                {
                    handleException(e);
                }

                m_consumers.remove( c );
                return true;
            }
        }

        return false;
    }


    /**
     * Sends a message to the server using 2 arguments.  The first argument is the first word in the message, the second argument is the rest of the message.
     * 
     * @param op  The first word in the message
     * @param arg The rest of the message
     * @return true if the message was sent.  false if something bad happened.  This could fail if the connection to the server was interrupted
     */
    public boolean sendMessage( String op, String arg )
    {
        try
        {
            String arg_encoded = URLEncoder.encode( arg, STRING_ENCODING );

            String mess = op + " " + arg_encoded;

            TextMessage message = m_session.createTextMessage( mess );
            message.setStringProperty( "ELVISH_SCOPE", m_scope );
            message.setStringProperty( MESSAGE_PREFIX, op );
            message.setStringProperty( "VHMSG_VERSION", VHMSG_VERSION );
            message.setStringProperty( "VHMSG", "VHMSG" );
            message.setStringProperty( "MESSAGE_TYPE_VHMSG", "VHMSG" );
            //message.setStringProperty( op, arg );

            m_producer.send( message );

            //System.out.println( "sendMessage(): " + message );
        }
        catch ( Exception e )
        {
            handleException(e);
            return false;
        }

        return true;
    }


    /**
     * Sends a message to the server.  The argument contains the full message
     *
     * @param opandarg  The full message to send
     * @return true if the message was sent.  false if something bad happened.  This could fail if the connection to the server was interrupted
     */
    public boolean sendMessage( String opandarg )
    {
        opandarg = opandarg.trim();
        int index = opandarg.indexOf( " " );
        if ( index == -1 )
        {
            return sendMessage( opandarg, "" );
        }
        else
        {
            String op = opandarg.substring( 0, index );
            String arg = opandarg.substring( index + 1, opandarg.length() );
            return sendMessage( op, arg );
        }
    }


    /**
     * Sends a message to the server where each item in the array is a separate word in the message.
     *
     * @param op The first word in the message
     * @param args An array containing the rest of the message.  Each item in the array is a separate word
     *
     * @return true if the message was sent.  false if something bad happened.  This could fail if the connection to the server was interrupted
     */
    public boolean sendMessage( String op, String[] args )
    {
        String concatargs = "";
        for ( String arg : args )
        {
            concatargs += arg + " ";
        }

        return sendMessage( op, concatargs );
    }


    /**
     * Sends a message to the server setting specific object properties in the message.  Currently only used in Anton Leuski's code.
     *
     * @param inArgs  Map containing the properties and values to set
     *
     * @return true if the message was sent.  false if something bad happened.  This could fail if the connection to the server was interrupted
     */
    public boolean sendMessage( Map<String,?> inArgs )
    {
        try
        {
            TextMessage message = m_session.createTextMessage( MULTIKEY );
            message.setStringProperty( "ELVISH_SCOPE", m_scope );
            message.setStringProperty( MESSAGE_PREFIX, MULTIKEY);

            for ( Map.Entry<String,?> me : inArgs.entrySet() )
            {
                String value = URLEncoder.encode( me.getValue().toString(), STRING_ENCODING );
                message.setObjectProperty( me.getKey(), value );
            }

            m_producer.send( message );

            //System.out.println( "sendMessage(): " + message );
        }
        catch ( Exception e )
        {
            handleException(e);
            return false;
        }

        return true;
    }


    /**
     * Gets the scope currently used by the connection
     *
     * @return the scope
     */
    public String getScope()
    {
        return m_scope;
    }


    /**
     * Gets the hostname of the server currently used by the connection
     *
     * @return the hostname
     */
    public String getServer()
    {
        return m_hostname;
    }


    public String getPort()
    {
        return m_port;
    }


    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;

        VHMSG that = (VHMSG)o;

        return !(m_hostname != null ? !m_hostname.equals(that.m_hostname) : that.m_hostname != null)
            && !(m_port != null     ? !m_port.equals(that.m_port) : that.m_port != null)
            && !(m_scope != null    ? !m_scope.equals(that.m_scope) : that.m_scope != null);
    }


    public int hashCode()
    {
        int result;
        result = m_hostname != null ? m_hostname.hashCode() : 0;
        result = 31 * result + ( m_scope != null ? m_scope.hashCode() : 0 );
        return result;
    }


    public int poll()
    {
        int numMsgs = 0;

        if ( !m_immediateMethod )
        {
            for ( ;; )
            {
                MessageEvent event;

                synchronized ( m_messageLock )
                {
                    if ( m_messages.isEmpty() )
                    {
                        break;
                    }

                    event = m_messages.getFirst();
                    m_messages.removeFirst();
                }

                for ( MessageListener m_iConsumer : m_iConsumers )
                {
                    m_iConsumer.messageAction( event );
                }

                numMsgs++;
            }
        }

        return numMsgs;
    }


    public void waitAndPoll( double waitTimeSeconds )
    {
        m_waitLock.lock();
        try
        {
            // if there are already messages in the queue, process them and return (don't wait)
            int numMsgs = poll();
            if ( numMsgs > 0 )
                return;

            long waitTimeNanoseconds = (long)( waitTimeSeconds * 1000000000 );  // 1,000,000,000
            m_waitCondition.awaitNanos( waitTimeNanoseconds );

            poll();
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        finally
        {
            m_waitLock.unlock();
        }
    }


    protected static String GetServerFromEnvironment()
    {
        String server = System.getenv( VHMSG_SERVER );
        if ( server == null )
            server = "localhost";

        return server;
    }


    protected static String GetPortFromEnvironment()
    {
        String port = System.getenv( VHMSG_PORT );
        if ( port == null )
            port = "61616";//61616

        return port;
    }


    protected static String GetScopeFromEnvironment()
    {
        String scope = System.getenv( VHMSG_SCOPE );
        if ( scope == null )
            scope = "DEFAULT_SCOPE";

        return scope;
    }

    
    /**
     * This function is a callback function received whenever an ActiveMQ message is received from the server.  It processes the message and passes it on to the client via
     * the messageAction handler.
     *
     * @param msg  ActiveMQ message received from the server
     */
    public void onMessage( Message msg )
    {
        // NOTE:  Received from a different thread

        try
        {
            String message = "";
            String temp;

            //System.out.println("onMessage(): " + ((TextMessage)msg).getText() );

            if ( msg instanceof TextMessage )
            {
                TextMessage txtMsg = (TextMessage)msg;
                temp = txtMsg.getText();

                temp = URLDecoder.decode( temp, STRING_ENCODING );

                temp = temp.trim();

                // Strip off first char of args if it is a "
                if ( temp.substring( 0, 1 ).compareToIgnoreCase( "\"" ) == 0 )
                {
                    temp = temp.substring( 1, temp.length() ).trim() ;
                    if ( temp.substring( 0, 1 ).compareToIgnoreCase( "\"" ) == 0 )
                    {
                        // if 2 double quotes at end, take one double quote off
                        message += temp.substring( 0, temp.length() - 1 );
                    }
                    else
                    {
                        message += "\"" + temp;
                    }
                }
                else
                {
                    message += temp;
                }
                
                Logs.debug(message);

                //System.out.println( "onMessage() (parsed): " + message );

                Map<String, Object> eventMap = new HashMap<String, Object>();

                for ( Enumeration<?> e = msg.getPropertyNames(); e.hasMoreElements(); )
                {
                    Object o = e.nextElement();

                    if ( o.toString().compareTo( "ELVISH_SCOPE" ) == 0 )
                    {
                        continue;
                    }
                    else if ( o.toString().compareTo( MESSAGE_PREFIX ) == 0 )
                    {
                        String prefix = msg.getObjectProperty( o.toString() ).toString();
                        if ( prefix == null || prefix.trim().equals( "" ) )
                        {
                            continue;
                        }

                        String remainder = message.substring( Math.min( message.length(), prefix.length() + 1 ) );

                        eventMap.put( prefix, remainder );
                        continue;
                    }

                    String propertyString = msg.getObjectProperty( o.toString() ).toString();
                    propertyString = URLDecoder.decode( propertyString, STRING_ENCODING );
                    eventMap.put( o.toString(), propertyString );
                }


                MessageEvent event = new MessageEvent( this, message, eventMap );

                if ( m_immediateMethod )
                {
                    for ( MessageListener m_iConsumer : m_iConsumers )
                    {
                        m_iConsumer.messageAction( event );
                    }
                }
                else
                {
                    synchronized ( m_messageLock )
                    {
                       m_messages.add( event );
                    }

                    // signal the other thread that we've received a message (only used in WaitAndPoll() )
                    m_waitLock.lock();
                    try
                    {
                        m_waitCondition.signalAll();
                    }
                    finally
                    {
                        m_waitLock.unlock();
                    }
                }
            }


            /*
            if (message.getJMSReplyTo() != null) 
            {
                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: " + message.getJMSMessageID()));
            }

            if (transacted) 
            {
                session.commit();
            } 
            else if ( ackMode  == Session.CLIENT_ACKNOWLEDGE ) 
            {
                message.acknowledge();
            }
            */
        }
        catch ( JMSException e )
        {
            handleException(e);
        }
        catch ( Exception e )
        {
            handleException(e);
        }
        
      
    }


    private void handleException(Exception e)
    {
        System.out.println( "Caught: " + e );
        e.printStackTrace();

        if (isConnected() && m_connection instanceof ActiveMQConnection && (((ActiveMQConnection)m_connection).isClosed() || !((ActiveMQConnection)m_connection).isStarted()))
            closeConnection();
    }
    
    
    public String VHMSGonMessage( Message msg )
    {
        // NOTE:  Received from a different thread

        try
        {
            String message = "";
            String temp;

            //System.out.println("onMessage(): " + ((TextMessage)msg).getText() );

            if ( msg instanceof TextMessage )
            {
                TextMessage txtMsg = (TextMessage)msg;
                temp = txtMsg.getText();

                temp = URLDecoder.decode( temp, STRING_ENCODING );

                temp = temp.trim();

                // Strip off first char of args if it is a "
                if ( temp.substring( 0, 1 ).compareToIgnoreCase( "\"" ) == 0 )
                {
                    temp = temp.substring( 1, temp.length() ).trim() ;
                    if ( temp.substring( 0, 1 ).compareToIgnoreCase( "\"" ) == 0 )
                    {
                        // if 2 double quotes at end, take one double quote off
                        message += temp.substring( 0, temp.length() - 1 );
                    }
                    else
                    {
                        message += "\"" + temp;
                    }
                }
                else
                {
                    message += temp;
                }
                
                Logs.debug(message);

                //System.out.println( "onMessage() (parsed): " + message );

                Map<String, Object> eventMap = new HashMap<String, Object>();

                for ( Enumeration<?> e = msg.getPropertyNames(); e.hasMoreElements(); )
                {
                    Object o = e.nextElement();

                    if ( o.toString().compareTo( "ELVISH_SCOPE" ) == 0 )
                    {
                        continue;
                    }
                    else if ( o.toString().compareTo( MESSAGE_PREFIX ) == 0 )
                    {
                        String prefix = msg.getObjectProperty( o.toString() ).toString();
                        if ( prefix == null || prefix.trim().equals( "" ) )
                        {
                            continue;
                        }

                        String remainder = message.substring( Math.min( message.length(), prefix.length() + 1 ) );

                        eventMap.put( prefix, remainder );
                        continue;
                    }

                    String propertyString = msg.getObjectProperty( o.toString() ).toString();
                    propertyString = URLDecoder.decode( propertyString, STRING_ENCODING );
                    eventMap.put( o.toString(), propertyString );
                }


                MessageEvent event = new MessageEvent( this, message, eventMap );

                if ( m_immediateMethod )
                {
                    for ( MessageListener m_iConsumer : m_iConsumers )
                    {
                        m_iConsumer.messageAction( event );
                    }
                }
                else
                {
                    synchronized ( m_messageLock )
                    {
                       m_messages.add( event );
                    }

                    // signal the other thread that we've received a message (only used in WaitAndPoll() )
                    m_waitLock.lock();
                    try
                    {
                        m_waitCondition.signalAll();
                    }
                    finally
                    {
                        m_waitLock.unlock();
                    }
                }
            }


            /*
            if (message.getJMSReplyTo() != null) 
            {
                replyProducer.send(message.getJMSReplyTo(), session.createTextMessage("Reply: " + message.getJMSMessageID()));
            }

            if (transacted) 
            {
                session.commit();
            } 
            else if ( ackMode  == Session.CLIENT_ACKNOWLEDGE ) 
            {
                message.acknowledge();
            }
            */
            return message;
        }
        catch ( JMSException e )
        {
            handleException(e);
        }
        catch ( Exception e )
        {
            handleException(e);
        }
        return null;
        
      
    }
}


