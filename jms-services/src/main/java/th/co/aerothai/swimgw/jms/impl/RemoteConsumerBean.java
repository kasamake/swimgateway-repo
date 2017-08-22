package th.co.aerothai.swimgw.jms.impl;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
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
import th.co.aerothai.swimgw.jms.api.IRemoteConsumerBean;
import th.co.aerothai.swimgw.jms.api.IRemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;
import th.co.aerothai.swimgw.services.api.IX400Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isode.x400api.X400_att;

@Stateless(name="RemoteConsumerBean")
@Remote(IRemoteConsumerBean.class)
public class RemoteConsumerBean implements IRemoteConsumerBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConsumerBean.class);

	private static final String NO_MESSAGE = "no message";


    private String or;
    private String dn;
    private String pa;
    private String credential;
    private String broker;
    private String client;
    private String username;
    private String password;
    
	@EJB
	private IConvertorBean convertorBean;

	@EJB
	private IMsgboxBean msgboxBean;

	@EJB
	private IRemoteX400Utils x400Utils;
	
	
	private Consumer consumer;
	
	@Override
	public void closeConnection()  {
		LOGGER.info("Close Connection");
		try {
			if(consumer!=null)
			consumer.closeConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void startListening() throws JMSException, InterruptedException{
		LOGGER.info("Start Listening");

		System.out.println("broker: "+ broker + "   "+username + "   "+password +"   "+ client);
		
		consumer = new Consumer();
			MessageConsumer messageConsumer = consumer.startListening(broker, client, username, password, "AMHS", or, dn, pa, credential);
		
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
					
					String msgText = msgbox.getMsgText();
					msgText = msgText.replaceAll("\n", "\r\n");
					msgbox.setMsgText(msgText);
					
					int status = x400Utils.sendMsgbox(msgbox, or, dn, pa, credential);
					if (status == X400_att.X400_E_NOERROR)
						msgboxBean.addMsgbox(msgbox);
				} catch (JMSException e) {
					System.err.println("Error reading message");
				} 
			}
		});

	}
	public void setOr(String or) {
		this.or = or;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public void setPa(String pa) {
		this.pa = pa;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public boolean testConnection(String broker, String client, String username, String password) {

//		if(retries > 3) 
//			return false;

		
		// create a Connection Factory
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(broker);
		Connection connection;
			try {
				connection = connectionFactory.createConnection(username, password);
				connection.close();
				return true;
			} catch (JMSException e) {
				return false;
			}

	}	
}
