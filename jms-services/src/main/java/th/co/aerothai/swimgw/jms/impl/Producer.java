package th.co.aerothai.swimgw.jms.impl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;

import th.co.aerothai.swimgw.models.Msgbox;

public class Producer {

//	private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

	private Logger logger = Logger.getLogger(Producer.class);
	private String clientId;
	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static String url = "tcp://172.16.21.206:61616";
	// default broker URL is : tcp://localhost:61616"

	private int retries;
	
	public void create(String clientId, String queueName) throws JMSException {
		this.clientId = clientId;

		logger.info(clientId + ": create connection to "+queueName);
		
		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// create a Connection
		connection = connectionFactory.createConnection("karaf", "karaf");
		
		connection.setClientID(clientId);

		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(queueName);

		// create the Topic to which messages will be sent
		// Topic topic = session.createTopic(topicName);

		// create a MessageProducer for sending messages
		messageProducer = session.createProducer(destination);
	}

	public boolean create(String broker, String client, String username, String password, String queueName) {

//		if(retries > 3) 
//			return false;
		
		logger.info(client + ": create connection to "+ queueName + "  ##"+retries);
		
		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

		try {
			connection = connectionFactory.createConnection(username, password);
			connection.setClientID(client);
			connection.start();
			
			connection.setExceptionListener(new ExceptionListener() {
	            public void onException(JMSException exception) {
	                logger.error("ExceptionListener triggered: " + exception.getMessage(), exception);
	                try {
	                    Thread.sleep(3000); // Wait 5 seconds (JMS server restarted?)
	                    logger.info("Restart connection for "+ client);
	                    create(broker, client, username, password, queueName);
	                } catch (InterruptedException e) {
	                    logger.error("Error pausing thread" + e.getMessage());
	                } 
	            }
	        });
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination destination = session.createQueue(queueName);

			messageProducer = session.createProducer(destination);
			retries = 0;
			return true;
		} catch (JMSException e1) {
//			retries++;
			logger.error("Error connecting to " + queueName);
			e1.printStackTrace();
		} 
//		finally{
//	        if (connection != null) {
//	            try {
//	                connection.close();
//	            }catch(JMSException ex) {
//	               logger.warn("Couldn't close JMSConnection: ", ex);
//	            }
//	        }
//	    }
		return false;
		
	}
//	public void create(String broker, String client, String username, String password, String queueName) {
//
////		if(retries > 3) 
////			return false;
//		
//		logger.info(client + ": create connection to "+ queueName + "  ##"+retries);
//		
//		// create a Connection Factory
//		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);
//
//		try {
//			connection = connectionFactory.createConnection(username, password);
//			connection.setClientID(client);
//			connection.start();
//			
//			connection.setExceptionListener(new ExceptionListener() {
//	            public void onException(JMSException exception) {
//	                logger.error("ExceptionListener triggered: " + exception.getMessage(), exception);
//	                try {
//	                    Thread.sleep(3000); // Wait 5 seconds (JMS server restarted?)
//	                    logger.info("Restart connection for "+ client);
//	                    create(broker, client, username, password, queueName);
//	                } catch (InterruptedException e) {
//	                    logger.error("Error pausing thread" + e.getMessage());
//	                } 
//	            }
//	        });
//			
//			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//			Destination destination = session.createQueue(queueName);
//
//			messageProducer = session.createProducer(destination);
//			retries = 0;
//		} catch (JMSException e1) {
////			retries++;
//			logger.error("Error connecting to " + queueName);
//
//		} 
//		finally{
//	        if (connection != null) {
//	            try {
//	                connection.close();
//	            }catch(JMSException ex) {
//	               logger.warn("Couldn't close JMSConnection: ", ex);
//	            }
//	        }
//	    }
//		
//	}
	public void closeConnection() throws JMSException {
		if(connection!=null)
		connection.close();
	}

	public void sendMessage(String text) throws JMSException {
		// create a JMS TextMessage
		TextMessage textMessage = session.createTextMessage(text);

		// send the message to the queue destination
		messageProducer.send(textMessage);

//		messageProducer.send
		logger.info(clientId + ": sent message with text= "+text);
	}

	public void sendMsgbox(Msgbox msgbox) throws JMSException {

		ObjectMessage objectMessage = session.createObjectMessage(msgbox);
		// create a JMS TextMessage
		// TextMessage textMessage = session.createTextMessage(text);

		// send the message to the queue destination
		// messageProducer.send(textMessage);
		messageProducer.send(objectMessage);

		logger.info(clientId + ": sent message as a MsgBox with Subject= "+msgbox.getMsgSubject());
	}
	

}
