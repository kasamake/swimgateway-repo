package th.co.aerothai.swimgw.client.web;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.jms.Connection;
import javax.jms.JMSException;

import th.co.aerothai.swimgw.jms.api.IConsumerBean;


@ManagedBean(name = "consumerManagedBean")
@ViewScoped
//@SessionScoped
public class ConsumerManagedBean {

	String message;
	String queueName;
	Connection connection;
	String status;
	
	@EJB
	IConsumerBean consumerBean;
	
//	Consumer consumer;
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
//		consumer = new Consumer();
		try {
			connection = consumerBean.createConnection("jms-client-consumer", queueName);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void receiveMessage() {
//		consumer = new Consumer();
		try {
			connection = consumerBean.createConnection("jms-client-consumer", queueName);
			message = consumerBean.receiveMessage(1000);
			consumerBean.closeConnection(connection);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopConnection() {
		this.status = "Service stopped";
		try {
			consumerBean.closeConnection(connection);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startListening() {
//		consumer = new Consumer();
		this.status = "Service started";
		try {
			connection = consumerBean.startListening("jms-client-consumer", queueName);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@PreDestroy
	public void closeConnection() {
		this.status = "Service stopped";
		try {
			consumerBean.closeConnection(connection);
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
