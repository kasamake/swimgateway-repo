package th.co.swimgw.jms.clients;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TestClient {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		ApplicationContext jmsContext = null;
		jmsContext = new FileSystemXmlApplicationContext(
//				"path/to/jmsContextSender.xml");
				"src/main/webapp/WEB-INF/jmsContextSender.xml");
		SimpleMessageSender messageSender = (SimpleMessageSender) jmsContext
				.getBean("simpleMessageSender");

		// Create a session within the connection.
		messageSender.sendMessage("hello123!");
	}
}
	