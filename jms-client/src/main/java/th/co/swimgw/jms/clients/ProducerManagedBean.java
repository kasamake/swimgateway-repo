package th.co.swimgw.jms.clients;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;

import th.co.swimgw.jms.pubsub.Consumer;
import th.co.swimgw.jms.pubsub.Producer;

@ManagedBean(name = "producerManagedBean")
@SessionScoped
public class ProducerManagedBean {

	String message;
	String queueName;
	
	@PostConstruct
	  public void setup()
	  {
	    System.out.println("Setting up a producer");
	    this.message = "";
	    this.queueName = "AMHS";
	  }
	
	public void sendMessage() throws JMSException {
		Producer producer = new Producer();
		producer.create("jms-client-producer", this.queueName);
		
		producer.sendMessage(this.message);
		producer.closeConnection();
		
		this.message = "";
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
