package th.co.swimgw.jms.queue;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

@Stateless
@Local
public class QueueMessageBean implements IQueueMessageBean {
	// private JmsTemplate jmsTemplate;
	//
	// public void setJmsTemplate(JmsTemplate jmsTemplate) {
	// this.jmsTemplate = jmsTemplate;
	// }

	public void sendMessage(final String message, final String queueName) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://localhost:61616");
		// ConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory("admin", "admin",
		// "tcp://172.16.21.206:61616");
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue queue = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(queue);

			connection.start();

			TextMessage m1 = session.createTextMessage();
			m1.setText(message);

			producer.send(m1);

			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void sendMessage(final Serializable message, final String queueName) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://localhost:61616");
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue queue = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(queue);

			connection.start();

			ObjectMessage m1 = session.createObjectMessage();
			m1.setObject(message);

			producer.send(m1);

			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void receiveMessage(String queueName) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://localhost:61616");
		// ConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory("admin", "admin",
		// "tcp://172.16.21.206:61616");
		Connection connection;
		try {
			connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue queue = session.createQueue(queueName);
			MessageConsumer consumer = session.createConsumer(queue);

			connection.start();

			Message m1 = consumer.receive();
			if (m1 instanceof TextMessage) {
				TextMessage text = (TextMessage) m1;
				System.out.println("Message is : " + text.getText());
			} 
//			else {
//				Person p = (Person) m1;
//				System.out.println("Person name : " + p.getName() + "  	age: " + p.getAge());
//			}

			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
