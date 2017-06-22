package th.co.swimgw.jms.queue;

import java.io.Serializable;

public abstract interface IQueueMessageBean {
	public abstract void sendMessage(String message, String queueName);
	public abstract void sendMessage(Serializable message, String queueName);
	public abstract void receiveMessage(String queueName);
}
