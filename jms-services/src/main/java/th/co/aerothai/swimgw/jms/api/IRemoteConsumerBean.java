package th.co.aerothai.swimgw.jms.api;

import javax.ejb.Remote;
import javax.jms.Connection;
import javax.jms.JMSException;

@Remote
public interface IRemoteConsumerBean {
//	public abstract Connection createConnection(String clientId, String queueName) throws JMSException;
	public abstract void closeConnection();
//	public abstract String receiveMessage(int timeout) throws JMSException;
//	public abstract Connection startListening(String clientId, String queueName) throws JMSException;
	public abstract void startListening() throws JMSException, InterruptedException;
	public abstract void setOr(String or);
	public abstract void setDn(String dn);
	public abstract void setPa(String pa);
	public abstract void setCredential(String credential);
	public abstract void setBroker(String broker);
	public abstract void setClient(String client);
	public abstract void setUsername(String username);
	public abstract void setPassword(String password);
}
