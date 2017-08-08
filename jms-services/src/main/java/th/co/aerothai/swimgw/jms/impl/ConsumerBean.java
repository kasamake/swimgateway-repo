package th.co.aerothai.swimgw.jms.impl;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import th.co.aerothai.swimgw.jms.api.IConsumerBean;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IX400Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isode.x400api.X400_att;

@Stateless
public class ConsumerBean implements IConsumerBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBean.class);

	private static final String NO_MESSAGE = "no message";

	private String clientId;
	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;
	// private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static String url = "tcp://172.16.21.206:61616";

	@EJB
	IConvertorBean convertorBean;

	@EJB
	IMsgboxBean msgboxBean;

	@EJB
	IX400Utils x400Utils;

	@Override
	public Connection createConnection(String clientId, String queueName) throws JMSException {
		LOGGER.info("Start Connection");
		this.clientId = clientId;

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// create a Connection
		// connection = connectionFactory.createConnection();
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
		return connection;
	}

	@Override
	public void closeConnection(Connection connection) throws JMSException {
		LOGGER.info("Close Connection");
		connection.close();
	}

	@Override
	public String receiveMessage(int timeout) throws JMSException {

		String text = NO_MESSAGE;

		// read a message from the topic destination
		Message message = messageConsumer.receive(timeout);

		if (!(message instanceof TextMessage))
			throw new RuntimeException("no text message");
		TextMessage tm = (TextMessage) message;
		// System.out.println(tm.getText()); // print message
		// LOGGER.debug(clientId + ": received message with text='{}'", text);

		System.out.println(tm.getText()); // print message

		Msgbox msgbox = convertorBean.convertXMLStringtoMsgbox(tm.getText());
		System.out.println("Converted Msgbox subject: " + msgbox.getMsgSubject());
		for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
			System.out.println("Attachment: " + msgBoxAttachment.getFilename());
		}

		// ************** Editing **************************
		// List<Msgboxrecipient> msgboxrecipients =
		// msgbox.getMsgboxrecipients();
		//
		// for(int i = 0; i < msgboxrecipients.size(); i++) {
		// Msgboxrecipient msgboxrecipient = msgboxrecipients.get(0);
		// if(msgboxrecipient.getAliasname().equals("VTBBSWIM")) {
		// msgboxrecipients.remove(i);
		// }
		// }
		//
		// msgbox.setMsgboxrecipients(msgboxrecipients);
		int status = x400Utils.sendMsgbox(msgbox);
		if (status == X400_att.X400_E_NOERROR)
			msgboxBean.addMsgbox(msgbox);

		LOGGER.info("greeting={}", text);
		return tm.toString();
	}

	@Override
	public Connection startListening(String clientId, String queueName) throws JMSException {
		LOGGER.info("Start Listening");
		this.clientId = clientId;

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

		// create a Connection
		// connection = connectionFactory.createConnection();
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

					Msgbox msgbox = convertorBean.convertXMLStringtoMsgbox(tm.getText());
					System.out.println("Converted Msgbox subject: " + msgbox.getMsgSubject());
					System.out.println("Converted Msgbox Text: " + msgbox.getMsgText());
//					for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
//						System.out.println("Attachment: " + msgBoxAttachment.getFilename());
//					}

					
					String msgText = msgbox.getMsgText();
					msgText = msgText.replaceAll("\n", "\r\n");
					msgbox.setMsgText(msgText);
					
					List<Msgboxrecipient> msgboxrecipients = msgbox.getMsgboxrecipients();

					// for(int i = 0; i < msgboxrecipients.size(); i++) {
					// Msgboxrecipient msgboxrecipient =
					// msgboxrecipients.get(i);
					// if(msgboxrecipient.getAliasname().equals("VTBBSWIM")) {
					// msgboxrecipients.remove(i);
					// }
					// }
					int status = x400Utils.sendMsgbox(msgbox);
					if (status == X400_att.X400_E_NOERROR)
						msgboxBean.addMsgbox(msgbox);
				} catch (JMSException e) {
					System.err.println("Error reading message");
				} 
			}
		});

		// start the connection in order to receive messages
		connection.start();
		return connection;

		// Thread.sleep(60 * 1000); // receive messages for 60s (more)
		// closeConnection(); // free all resources (more)
	}

}
