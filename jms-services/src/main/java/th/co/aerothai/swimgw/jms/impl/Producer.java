package th.co.aerothai.swimgw.jms.impl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import th.co.aerothai.swimgw.models.Msgbox;

public class Producer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

	private String clientId;
	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;

//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static String url = "tcp://172.16.21.206:61616";
	// default broker URL is : tcp://localhost:61616"

	public void create(String clientId, String queueName) throws JMSException {
		this.clientId = clientId;

		LOGGER.info(clientId + ": create connection to '{}'", queueName);
		
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

	public void closeConnection() throws JMSException {
		connection.close();
	}

	public void sendMessage(String text) throws JMSException {
		// create a JMS TextMessage
		TextMessage textMessage = session.createTextMessage(text);

		// send the message to the queue destination
		messageProducer.send(textMessage);

//		messageProducer.send
		LOGGER.info(clientId + ": sent message with text='{}'", text);
	}

	public void sendMsgbox(Msgbox msgbox) throws JMSException {

		ObjectMessage objectMessage = session.createObjectMessage(msgbox);
		// create a JMS TextMessage
		// TextMessage textMessage = session.createTextMessage(text);

		// send the message to the queue destination
		// messageProducer.send(textMessage);
		messageProducer.send(objectMessage);

		 LOGGER.info(clientId + ": sent message as a MsgBox with Subject='{}'", msgbox.getMsgSubject());
	}
}
