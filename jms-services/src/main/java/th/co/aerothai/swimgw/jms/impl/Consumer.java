package th.co.aerothai.swimgw.jms.impl;

import java.util.List;

import javax.ejb.EJB;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;

public class Consumer {

//	@EJB
//	IConvertorBean convertorBean;
//
//	@EJB
//	IMsgboxBean msgboxBean;
//
//	@EJB
//	IRemoteX400Utils x400Utils;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

	private static final String NO_MESSAGE = "no message";

	private String clientId;
	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;
//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	 private static String url = "tcp://172.16.21.206:61616";

	public void create(String clientId, String queueName) throws JMSException {
		LOGGER.info("Start Connection");
		this.clientId = clientId;

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// create a Connection
//		connection = connectionFactory.createConnection();
        connection = connectionFactory.createConnection("karaf", "karaf");
		connection.setClientID(clientId);

		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// create the Topic from which messages will be received
		// Topic topic = session.createTopic(topicName);
		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(queueName);

		// create a MessageConsumer for receiving messages
		messageConsumer = session.createConsumer(destination);

		// start the connection in order to receive messages
		connection.start();
	}

	public void closeConnection() throws JMSException {
		LOGGER.info("Close Connection");
		connection.close();
	}

	public String receiveMessage(int timeout) throws JMSException {

		String text = NO_MESSAGE;

		// read a message from the topic destination
		Message message = messageConsumer.receive(timeout);

		
		if (!(message instanceof TextMessage))
			throw new RuntimeException("no text message");
		TextMessage tm = (TextMessage) message;
//			System.out.println(tm.getText()); // print message
		LOGGER.debug(clientId + ": received message with text='{}'", text);
		
		// check if a message was received
//		if (message != null) {
//			// cast the message to the correct type
//			TextMessage textMessage = (TextMessage) message;
//
//			// retrieve the message content
//			text = textMessage.getText();
//			LOGGER.debug(clientId + ": received message with text='{}'", text);
//
//			// create msg
//			// greeting = "Hello " + text + "!";
//		} else {
//			LOGGER.debug(clientId + ": no message received");
//		}

		LOGGER.info("greeting={}", text);
		return tm.toString();
	}

	public void startListening(String clientId, String queueName) throws JMSException, InterruptedException {
		LOGGER.info("Start Listening");
		this.clientId = clientId;

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// create a Connection
//		connection = connectionFactory.createConnection();
		connection = connectionFactory.createConnection("karaf", "karaf");
		connection.setClientID(clientId);

		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// create the Topic from which messages will be received
		// Topic topic = session.createTopic(topicName);
		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(queueName);

		// create a MessageConsumer for receiving messages
		messageConsumer = session.createConsumer(destination);

		messageConsumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message msg) {
				try {
					if (!(msg instanceof TextMessage))
						throw new RuntimeException("no text message");
					TextMessage tm = (TextMessage) msg;
					System.out.println(tm.getText()); // print message
				} catch (JMSException e) {
					System.err.println("Error reading message");
				}
			}
		});

		// start the connection in order to receive messages
		connection.start();

//		Thread.sleep(60 * 1000); // receive messages for 60s (more)
//		closeConnection(); // free all resources (more)
	}
	
	public MessageConsumer startListening(String broker, String client, String username, String password, String queueName, 
			String or, String dn, String pa, String credential) throws JMSException, InterruptedException {
		LOGGER.info("Start Listening");

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

		// create a Connection
//		connection = connectionFactory.createConnection();
		connection = connectionFactory.createConnection(username, password);
		connection.setClientID(client+"-consumer");

		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// create the Topic from which messages will be received
		// Topic topic = session.createTopic(topicName);
		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(queueName);

		// create a MessageConsumer for receiving messages
		messageConsumer = session.createConsumer(destination);

//		messageConsumer.setMessageListener(new MessageListener() {
//
//
//			@Override
//			public void onMessage(Message msg) {
//				try {
//					if (!(msg instanceof TextMessage))
//						throw new RuntimeException("no text message");
//					TextMessage tm = (TextMessage) msg;
//					System.out.println(tm.getText()); // print message
//
//					Msgbox msgbox = convertorBean.convertXMLStringtoMsgbox(tm.getText());
//					System.out.println("Converted Msgbox subject: " + msgbox.getMsgSubject());
//					System.out.println("Converted Msgbox Text: " + msgbox.getMsgText());
////					for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
////						System.out.println("Attachment: " + msgBoxAttachment.getFilename());
////					}
//
//					
//					String msgText = msgbox.getMsgText();
//					msgText = msgText.replaceAll("\n", "\r\n");
//					msgbox.setMsgText(msgText);
//					
////					List<Msgboxrecipient> msgboxrecipients = msgbox.getMsgboxrecipients();
//
//					int status = x400Utils.sendMsgbox(msgbox, or, dn, pa, credential);
//					if (status == X400_att.X400_E_NOERROR)
//						msgboxBean.addMsgbox(msgbox);
//				} catch (JMSException e) {
//					System.err.println("Error reading message");
//				} 
//			}
//		});

		// start the connection in order to receive messages
		connection.start();
		return messageConsumer;
//		Thread.sleep(60 * 1000); // receive messages for 60s (more)
//		closeConnection(); // free all resources (more)
	}
}