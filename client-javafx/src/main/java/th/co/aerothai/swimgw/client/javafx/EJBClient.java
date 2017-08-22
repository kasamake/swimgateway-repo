package th.co.aerothai.swimgw.client.javafx;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;

import th.co.aerothai.swimgw.jms.api.IRemoteConsumerBean;
import th.co.aerothai.swimgw.jms.api.IRemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.jms.impl.RemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;

public class EJBClient{
	
	/**
	 * 
	 */

	static Connection connection;
	static String moduleName = "client-web";
	static String beanNameAMHSToSWIM = "RemoteMsgboxTimerBean";
	static String beanNameSWIMToAMHS = "RemoteConsumerBean";
	static String interfaceQualifiedNameAMHSToSWIM = IRemoteMsgboxTimerBean.class.getName();
	static String interfaceQualifiedNameSWIMToAMHS = IRemoteConsumerBean.class.getName();
	
	static Monitor monitor;
	static Thread t1;
	
	static int isConnected;
	public static Properties getProperty(String _userName, String _password) {

		Properties jndiProperties = new Properties();
		jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		jndiProperties.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");

		// This property is important for remote resolving
		 jndiProperties.put("jboss.naming.client.ejb.context", true);
//		 jndiProperties.put(Context.SECURITY_PRINCIPAL, "swimgw");
//		 jndiProperties.put(Context.SECURITY_CREDENTIALS, "swimswim");
		return jndiProperties;

	}

	public static InitialContext getInitialContext(Properties props) throws Exception {
		InitialContext ic = new InitialContext(props);
		return ic;
	}

	public static List<Msgbox> receiveMessage() throws Exception {
		System.out.println("Initializing property");
		Properties p = EJBClient.getProperty("", "");
		InitialContext ic = EJBClient.getInitialContext(p);
		System.out.println("InitialContext Obtained");

		String moduleName = "client-web";
		String beanName = "RemoteX400UtilsBean";
		String interfaceQualifiedName = IRemoteX400Utils.class.getName();

//		IRemoteX400Utils x400Utils = (IRemoteX400Utils) ic.lookup("java:global/" + moduleName + "/" + beanName + "!" + interfaceQualifiedName);
		IRemoteX400Utils x400Utils = (IRemoteX400Utils) ic.lookup(moduleName + "/" + beanName + "!" + interfaceQualifiedName);
		List<Msgbox> msgboxes = x400Utils.getMsgBoxBeanList();
		if (msgboxes.size() != 0) {
			System.out.println("Number of messages: " + msgboxes.size());
			System.out.println("Lastest sequence: " + msgboxes.get(msgboxes.size() - 1).getMsgsqn().intValue());

		} else {
			System.out.println("No messages found");
		}

		return msgboxes;
	}

	public static void startReceivingMessage(String or, String dn, String pa, String credential,
			String broker, String client, String username, String password) throws Exception {
		
		monitor = new Monitor(or, dn, pa, credential, broker, client, username, password);
		t1 = new Thread(monitor, "T1"); 
		t1.start();

		System.out.println("Initializing property");
		Properties p = EJBClient.getProperty("", "");
		InitialContext ic = EJBClient.getInitialContext(p);
		System.out.println("InitialContext Obtained");
		IRemoteConsumerBean consumerBean = (IRemoteConsumerBean) ic.lookup(moduleName + "/" + beanNameSWIMToAMHS + "!" + interfaceQualifiedNameSWIMToAMHS);
		
		IRemoteMsgboxTimerBean msgboxTimerBean = (IRemoteMsgboxTimerBean) ic.lookup(moduleName + "/" + beanNameAMHSToSWIM + "!" + interfaceQualifiedNameAMHSToSWIM);
		msgboxTimerBean.setOr(or);
		msgboxTimerBean.setDn(dn);
		msgboxTimerBean.setPa(pa);
		msgboxTimerBean.setCredential(credential);
		msgboxTimerBean.setBroker(broker);
		msgboxTimerBean.setClient(client);
		msgboxTimerBean.setUsername(username);
		msgboxTimerBean.setPassword(password);
		int isConnected = msgboxTimerBean.openConnection();
		
		System.out.println("IS CONNECTED: "+isConnected);

		if(isConnected ==0){
			consumerBean.setOr(or);
			consumerBean.setDn(dn);
			consumerBean.setPa(pa);
			consumerBean.setCredential(credential);
			consumerBean.setBroker(broker);
			consumerBean.setClient(client);
			consumerBean.setUsername(username);
			consumerBean.setPassword(password);
			consumerBean.startListening();
		}
		

		
//		boolean pingSwim = msgboxTimerBean.pingSWIM();
//		System.out.println("PING: "+pingSwim);
	}
	
	public static void stopReceivingMessage() throws Exception {
		monitor.stop();
		System.out.println("Initializing property");
		Properties p = EJBClient.getProperty("", "");
		InitialContext ic = EJBClient.getInitialContext(p);
		System.out.println("InitialContext Obtained");

//		String interfaceQualifiedName = IRemoteMsgboxTimerBean.class.getName();

		IRemoteMsgboxTimerBean msgboxTimerBean = (IRemoteMsgboxTimerBean) ic.lookup(moduleName + "/" + beanNameAMHSToSWIM + "!" + interfaceQualifiedNameAMHSToSWIM);
		msgboxTimerBean.closeConnection();
		
		IRemoteConsumerBean consumerBean = (IRemoteConsumerBean) ic.lookup(moduleName + "/" + beanNameSWIMToAMHS + "!" + interfaceQualifiedNameSWIMToAMHS);
		consumerBean.closeConnection();
//		List<Msgbox> msgboxes = x400Utils.getMsgBoxBeanList();
//		if (msgboxes.size() != 0) {
//			System.out.println("Number of messages: " + msgboxes.size());
//			System.out.println("Lastest sequence: " + msgboxes.get(msgboxes.size() - 1).getMsgsqn().intValue());
//
//		} else {
//			System.out.println("No messages found");
//		}
//		return msgboxes;
	}
	// public static List<Msgbox> receiveMessage() throws Exception {
	// // System.out.println("Initializing property");
	// // Properties p = EJBClient.getProperty("","");
	// // InitialContext ic = EJBClient.getInitialContext(p);
	// // System.out.println("InitialContext Obtained");
	// //
	// // IRemoteX400Utils x400Utils = (IRemoteX400Utils)
	// // ic.lookup("java:global/client-web/X400UtilsBean");
	//
	// String moduleName = "client-web";
	// String beanName = "RemoteX400UtilsBean";
	//// String interfaceQualifiedName = IRemoteX400Utils.class.getName();
	//
	// String interfaceQualifiedName =
	// "th.co.aerothai.swimgw.services.api.IRemoteX400Utils";
	// LookerUp lookerup = new LookerUp();
	//
	// IRemoteX400Utils x400Utils = ((IRemoteX400Utils)
	// lookerup.findLocalSessionBean(moduleName, beanName,
	// interfaceQualifiedName));
	//
	// List<Msgbox> msgboxes = x400Utils.getMsgBoxBeanList();
	// if (msgboxes.size() != 0) {
	// System.out.println("Number of messages: " + msgboxes.size());
	// System.out.println("Lastest sequence: " + msgboxes.get(msgboxes.size() -
	// 1).getMsgsqn().intValue());
	//
	// } else {
	// System.out.println("No messages found");
	// }
	//
	// return msgboxes;
	// }

//	public static void main(String args[]) throws Exception {
//		System.out.println("****EJBClient main****");
//		List<Msgbox> msgboxes = receiveMessage();
//
//	}
//	
//	 public static void printBindings(InitialContext jndi) throws Exception {
//	 System.out.println("Obtained initial Context");
//	 System.out.println("Name in namespace=" + jndi.getNameInNamespace());
//	 NamingEnumeration eenum = jndi.listBindings("");// )listBindings("jdbc");
//	
//	 int counter = 0;
//	 System.out.println("Elements bound in JNDI -----");
//	 while (eenum.hasMoreElements()) {
//	 System.out.println(counter + "=" + eenum.nextElement());
//	 counter++;
//	 }
//	 System.out.println("--------------------------------");
//	 }
}