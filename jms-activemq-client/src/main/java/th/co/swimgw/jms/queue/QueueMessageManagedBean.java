package th.co.swimgw.jms.queue;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

@ManagedBean(name = "queueMessageManagedBean")
@SessionScoped
public class QueueMessageManagedBean {

	@EJB
	IQueueMessageBean messageSenderBean;
	
	String message;
	
	@PostConstruct
	public void setup() {
//		ApplicationContext jmsContext = null;
//		jmsContext = new FileSystemXmlApplicationContext(
////				"path/to/jmsContextSender.xml");
//				"src/main/webapp/WEB-INF/jmsContextSender.xml");
//		messageSenderBean = (IMessageSenderBean) jmsContext.getBean("messageSenderBean");
	}
	
	public void sendMessage(){
		messageSenderBean.sendMessage(message,"AMHS");
//		messageSenderBean.sendMessage(new Person("Kasama", 32), "AMHS");
	}

	public void receiveMessage(){
//		messageSenderBean.sendMessage(message,"AMHS");
		messageSenderBean.receiveMessage("AMHS");
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
