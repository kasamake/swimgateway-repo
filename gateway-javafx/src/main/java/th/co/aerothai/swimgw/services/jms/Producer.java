package th.co.aerothai.swimgw.services.jms;

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
//	private String clientId;
	private Connection connection;
	private Session session;
	private MessageProducer producerAIXM;
	private MessageProducer producerFIXM;
	private MessageProducer producerWXXM;
	
	private boolean connectionLost = false;
//	Producer producerAIXM = new Producer();
//	Producer producerFIXM = new Producer();
//	Producer producerWXXM = new Producer();
//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
//	private static String url = "tcp://172.16.21.206:61616";
	// default broker URL is : tcp://localhost:61616"

	public void create(String broker, String client, String username, String password) throws JMSException {

//		logger.info(client + ": create connection to ActiveMQ");
		
		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

			connection = connectionFactory.createConnection(username, password);
			connection.setClientID(client);
			connection.start();
			
//			connection.setExceptionListener(new ExceptionListener() {
//	            public void onException(JMSException exception) {
//	                logger.error("ExceptionListener triggered: " + exception.getMessage(), exception);
//	                try {
//	                    Thread.sleep(3000); // Wait 5 seconds (JMS server restarted?)
//	                    logger.info("Restart connection for "+ client);
//	                    create(broker, client, username, password);
//	                } catch (InterruptedException e) {
//	                    logger.error("Error pausing thread" + e.getMessage());
//	                } 
//	            }
//	        });
			connection.setExceptionListener(new ExceptionListener() {
	            public void onException(JMSException exception) {
	                logger.error("ExceptionListener triggered: " + exception.getMessage());
	                connectionLost = true;
	            }
	        });
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Queue name for AIXM, FIXM, WXXM are "AMHS2AIXM", "AMHS2FIXM", "AMHS2WXXM");
			Destination destinationAIXM = session.createQueue("AMHS2AIXM");
			Destination destinationFIXM = session.createQueue("AMHS2FIXM");
			Destination destinationWXXM = session.createQueue("AMHS2WXXM");
			producerAIXM = session.createProducer(destinationAIXM);
			producerFIXM = session.createProducer(destinationFIXM);
			producerWXXM = session.createProducer(destinationWXXM);
			connectionLost = false;
		
		
	}

	
	public boolean testConnection(String broker, String client, String username, String password) {

		
		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

			try {
				connection = connectionFactory.createConnection(username, password);
				connection.setClientID(client);
				connection.start();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				closeConnection();
				return true;
			} catch (JMSException e) {
//				System.out.println("Failed connecting to ActiveMQ");
				return false;
			}
			


	}
	public void closeConnection() throws JMSException {
//		logger.info("Close Connection for producer");
		if(connection!=null) {
			connection.close();
		}
		return;
	}


	public void sendMessage(String text, String queueName) throws JMSException {
		// create a JMS TextMessage
		TextMessage textMessage = session.createTextMessage(text);

		// send the message to the queue destination
		switch (queueName) {
		case "AIXM":
			producerAIXM.send(textMessage);
			break;
		case "FIXM":
			producerFIXM.send(textMessage);
			break;
		case "WXXM":
			producerWXXM.send(textMessage);
			break;
		default:
			break;
		}
//		logger.info("Sent message with text= "+text);
	}

	public boolean isConnectionLost() {
		return connectionLost;
	}

	public void setConnectionLost(boolean connectionLost) {
		this.connectionLost = connectionLost;
	}


}
