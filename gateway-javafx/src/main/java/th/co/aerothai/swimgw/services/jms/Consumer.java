package th.co.aerothai.swimgw.services.jms;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.MsgboxBean;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.amhs.ConvertorBean;
import th.co.aerothai.swimgw.services.amhs.SendUtils;


public class Consumer {

//	@EJB
//	IConvertorBean convertorBean;
//
//	@EJB
//	IMsgboxBean msgboxBean;
//
//	@EJB
//	IRemoteX400Utils x400Utils;
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

	private Logger logger = Logger.getLogger(Producer.class);
	private static final String NO_MESSAGE = "no message";

//	private String clientId;
	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;
//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
//	 private static String url = "tcp://172.16.21.206:61616";
		private boolean connectionLost = false;
//	public void create(String clientId, String queueName) throws JMSException {
//		logger.info("Start Connection");
//		this.clientId = clientId;
//
//		// create a Connection Factory
//		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
//
//		// create a Connection
////		connection = connectionFactory.createConnection();
//        connection = connectionFactory.createConnection("karaf", "karaf");
//		connection.setClientID(clientId);
//
//		// create a Session
//		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//		// create the Topic from which messages will be received
//		// Topic topic = session.createTopic(topicName);
//		// Create the destination (Topic or Queue)
//		Destination destination = session.createQueue(queueName);
//
//		// create a MessageConsumer for receiving messages
//		messageConsumer = session.createConsumer(destination);
//
//		// start the connection in order to receive messages
//		connection.start();
//	}

	public void closeConnection() throws JMSException {
		logger.info("Close Connection for consumer");
		if(connection!=null){
			connection.close();
		}
		
	}

	
	public void startListening(String broker, String client, String username, String password, String queueName, 
			String or, String dn, String pa, String credential) throws JMSException {
		logger.info("Start Listening");

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

		// create a Connection
		connection = connectionFactory.createConnection(username, password);
		connection.setClientID(client+"-consumer");
		connection.start();
		connection.setExceptionListener(new ExceptionListener() {
            public void onException(JMSException exception) {
                logger.error("ExceptionListener triggered: " + exception.getMessage(), exception);
                connectionLost = true;
            }
        });
		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		
		// Set exception listener

		
		// create the Topic from which messages will be received

		Destination destination = session.createQueue(queueName);

		// create a MessageConsumer for receiving messages
		messageConsumer = session.createConsumer(destination);

		connectionLost = false;
		messageConsumer.setMessageListener(new MessageListener() {


			@Override
			public void onMessage(Message msg) {
				try {
					if (!(msg instanceof TextMessage))
						throw new RuntimeException("no text message");
					TextMessage tm = (TextMessage) msg;
					System.out.println(tm.getText()); // print message
					connectionLost = false;
					
					Msgbox msgbox = ConvertorBean.convertXMLStringtoMsgbox(tm.getText());
					System.out.println("Converted Msgbox subject: " + msgbox.getMsgSubject());
					System.out.println("Converted Msgbox Text: " + msgbox.getMsgText());
//					for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
//						System.out.println("Attachment: " + msgBoxAttachment.getFilename());
//					}

					
					String msgText = msgbox.getMsgText();
					msgText = msgText.replaceAll("\n", "\r\n");
					msgbox.setMsgText(msgText);
					
//					List<Msgboxrecipient> msgboxrecipients = msgbox.getMsgboxrecipients();

					int status = SendUtils.send_msg(msgbox, or, dn, pa, credential);
					if (status == X400_att.X400_E_NOERROR)
						MsgboxBean.addMsgbox(msgbox);
				} catch (JMSException e) {
					System.err.println("Error reading message");
					logger.error("ExceptionListener triggered: " + e.getMessage(), e);
					connectionLost = true;
				} 
			}
		});

//		Thread.sleep(60 * 1000); // receive messages for 60s (more)
//		closeConnection(); // free all resources (more)
	}
}