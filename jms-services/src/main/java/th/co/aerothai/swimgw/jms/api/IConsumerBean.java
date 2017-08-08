package th.co.aerothai.swimgw.jms.api;

import javax.jms.Connection;
import javax.jms.JMSException;

public interface IConsumerBean {
	public abstract Connection createConnection(String clientId, String queueName) throws JMSException;
	public abstract void closeConnection(Connection connection) throws JMSException;
	public abstract String receiveMessage(int timeout) throws JMSException;
	public abstract Connection startListening(String clientId, String queueName) throws JMSException;
}
