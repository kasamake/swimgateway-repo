package th.co.aerothai.swimgw.client.javafx;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;

import th.co.aerothai.swimgw.jms.api.IRemoteConsumerBean;
import th.co.aerothai.swimgw.jms.api.IRemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;

public class Monitor implements Runnable {
	private volatile boolean exit = false;
	String moduleName = "client-web";
	String beanX400Utils = "RemoteX400UtilsBean";
	String beanConsumer = "RemoteConsumerBean";
	String interfaceQualifiedNamebeanX400Utils = IRemoteX400Utils.class.getName();
	String interfaceQualifiedNamebeanConsumer= IRemoteConsumerBean.class.getName();

	String beanNameAMHSToSWIM = "RemoteMsgboxTimerBean";
	String interfaceQualifiedNameAMHSToSWIM = IRemoteMsgboxTimerBean.class.getName();
	
	private String or;
	private String dn;
	private String pa;
	private String credential;
	private String broker;
	private String client;
	private String username;
	private String password;
	
	private boolean amhsConnection;
	private boolean jmsConnection; 
	public Monitor(String or, String dn, String pa, String credential, String broker, String client, String username,
			String password) {
		super();
		this.or = or;
		this.dn = dn;
		this.pa = pa;
		this.credential = credential;
		this.broker = broker;
		this.client = client;
		this.username = username;
		this.password = password;
	}

	public void run() {
		Properties p = EJBClient.getProperty("", "");

		try {
			InitialContext ic = EJBClient.getInitialContext(p);
			IRemoteX400Utils x400Utils = (IRemoteX400Utils) ic
					.lookup(moduleName + "/" + beanX400Utils + "!" + interfaceQualifiedNamebeanX400Utils);
			
			IRemoteConsumerBean consumerBean = (IRemoteConsumerBean) ic
					.lookup(moduleName + "/" + beanConsumer + "!" + interfaceQualifiedNamebeanConsumer);
			
			IRemoteMsgboxTimerBean msgboxTimerBean = (IRemoteMsgboxTimerBean) ic
					.lookup(moduleName + "/" + beanNameAMHSToSWIM + "!" + interfaceQualifiedNameAMHSToSWIM);
			while (!exit) {
				// exit = !msgboxTimerBean.isTestAMHS();
				
				
				amhsConnection = amhsConnection = x400Utils.testConnection(or, dn, pa, credential);
				jmsConnection = jmsConnection = consumerBean.testConnection(broker, client, username, password);
				
				exit = !(amhsConnection && jmsConnection);
				Thread.sleep(10000);
				System.out.println("Service is running.....");
			}
			
			msgboxTimerBean.closeConnection();
			consumerBean.closeConnection();
			System.out.println("Service is stopped....");

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		exit = true;
	}
}
