package th.co.swimgw.jms.clients;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;

import th.co.swimgw.jms.pubsub.Consumer;
import th.co.swimgw.jms.pubsub.Producer;

@ManagedBean(name = "consumerManagedBean")
@SessionScoped
public class ConsumerManagedBean {

	String message;
	String queueName;
	
	Consumer consumer;
	@PostConstruct
	  public void setup()
	  {
	    System.out.println("Setting up a consumer");
	    this.message = "";
	    this.queueName = "AMHS";
	  }
	
//	public void sendMessage() throws JMSException {
//		Producer producer = new Producer();
//		producer.create("jms-client", this.queueName);
//		
//		producer.sendMessage(this.message);
//		producer.closeConnection();
//	}
	
	public void startConnection() {
		consumer = new Consumer();
		try {
			consumer.create("jms-client-consumer", queueName);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void receiveMessage() {
		consumer = new Consumer();
		try {
			consumer.create("jms-client-consumer", queueName);
			message = consumer.receiveMessage(1000);
			consumer.closeConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopConnection() {
		try {
			consumer.closeConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startListening() {
		consumer = new Consumer();
		try {
			consumer.startListening("jms-client-consumer", queueName);
		} catch (JMSException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@PreDestroy
	public void closeConnection() {
		try {
			consumer.closeConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	
}
