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
import javax.xml.bind.JAXBException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.MsgboxBean;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.amhs.ConvertorBean;
import th.co.aerothai.swimgw.services.amhs.SendUtils;
import th.co.aerothai.swimgw.services.amhs.X400UtilsException;


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

	private Logger logger = Logger.getLogger(Consumer.class);
	private static final String NO_MESSAGE = "no message";

//	private String clientId;
	private Connection connection;
	private Session session;
	private MessageConsumer messageConsumer;
//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
//	 private static String url = "tcp://172.16.21.206:61616";
		private boolean swimConnectionLost = false;
		private boolean amhsConnectionLost = false;

	public void closeConnection() throws JMSException {
//		logger.info("Close Connection for consumer");
		if(connection!=null){
			connection.close();
		}
		
	}

	
	public void startListening(String broker, String client, String username, String password, String queueName, 
			String or, String dn, String pa, String credential) throws JMSException {
//		logger.info("Start Listening");

		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);

		// create a Connection
		connection = connectionFactory.createConnection(username, password);
		connection.setClientID(client);
		connection.start();
		connection.setExceptionListener(new ExceptionListener() {
            public void onException(JMSException exception) {
                logger.error("ExceptionListener triggered: " + exception.getMessage());
                swimConnectionLost = true;
            }
        });
		// create a Session
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Destination destination = session.createQueue(queueName);

		// create a MessageConsumer for receiving messages
		messageConsumer = session.createConsumer(destination);

		swimConnectionLost = false;
		messageConsumer.setMessageListener(new MessageListener() {

			Msgbox msgbox = new Msgbox();

			@Override
			public void onMessage(Message msg) {
				try {
					if (!(msg instanceof TextMessage))
						throw new RuntimeException("no text message");
					TextMessage tm = (TextMessage) msg;
//					System.out.println(tm.getText()); // print message
					swimConnectionLost = false;
					
					try {
						msgbox = ConvertorBean.convertXMLStringtoMsgbox(tm.getText());
						String msgText = msgbox.getMsgText();
						msgText = msgText.replaceAll("\n", "\r\n");
						msgbox.setMsgText(msgText);
						MsgboxBean.addMsgbox(msgbox);
						
						try {
							SendUtils.send_msg(msgbox, or, dn, pa, credential);
						}catch (X400UtilsException e) {
							logger.error("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
							+ " failed to be sent to Message Store (AMHS)");
							amhsConnectionLost = true;
						} 
						logger.info("Message" + "(" + msgbox.getId() + "): " + msgbox.getMsgboxToSwimDetail()
						+ " [Message Store]");
//						+ " has been sent to Message Store successfully");
					} catch (JAXBException e) {
						logger.error("XML: " + tm.getText()
						+ " cannot be converted to AMHS message format");
					}

					
				} catch (JMSException e) {
					logger.error("Message: " + msg
					+ " failed to be read from ActiveMQ (SWIM)");
				
					swimConnectionLost = true;
				} 
			}
		});

	}


	public boolean isSwimConnectionLost() {
		return swimConnectionLost;
	}


	public void setSwimConnectionLost(boolean swimConnectionLost) {
		this.swimConnectionLost = swimConnectionLost;
	}


	public boolean isAmhsConnectionLost() {
		return amhsConnectionLost;
	}


	public void setAmhsConnectionLost(boolean amhsConnectionLost) {
		this.amhsConnectionLost = amhsConnectionLost;
	}


}