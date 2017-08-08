package th.co.aerothai.swimgw.client.javafx;

import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

import th.co.aerothai.swimgw.jms.api.IRemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.jms.impl.RemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;

public class EJBClient {
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

	// public static boolean authenticate(String _userName, String _password)
	// throws Exception {
	// System.out.println("Initializing property");
	// Properties p = EJBClient.getProperty(_userName, _password);
	// InitialContext ic = EJBClient.getInitialContext(p);
	// System.out.println("InitialContext Obtained");
	// com.incept.LoginFXIntf rem = (com.incept.LoginFXIntf)
	// ic.lookup("java:global/Login/LoginFXBean");
	// // or you can do lookup by fully qualified class name as
	// //com.incept.LoginFXIntf rem = (com.incept.LoginFXIntf)
	// ic.lookup("com.incept.LoginFXIntf");
	// boolean result = rem.loginClicked(_userName, _password);
	// System.out.println("Result of authentication:"+ result);
	// return result;
	// }
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

	public static void startReceivingMessage() throws Exception {
		System.out.println("Initializing property");
		Properties p = EJBClient.getProperty("", "");
		InitialContext ic = EJBClient.getInitialContext(p);
		System.out.println("InitialContext Obtained");

		String moduleName = "client-web";
		String beanName = "RemoteMsgboxTimerBean";
		String interfaceQualifiedName = IRemoteMsgboxTimerBean.class.getName();

		IRemoteMsgboxTimerBean msgboxTimerBean = (IRemoteMsgboxTimerBean) ic.lookup(moduleName + "/" + beanName + "!" + interfaceQualifiedName);
		msgboxTimerBean.openConnection();
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
	
	public static void stopReceivingMessage() throws Exception {
		System.out.println("Initializing property");
		Properties p = EJBClient.getProperty("", "");
		InitialContext ic = EJBClient.getInitialContext(p);
		System.out.println("InitialContext Obtained");

		String moduleName = "client-web";
		String beanName = "RemoteMsgboxTimerBean";
		String interfaceQualifiedName = IRemoteMsgboxTimerBean.class.getName();

		IRemoteMsgboxTimerBean msgboxTimerBean = (IRemoteMsgboxTimerBean) ic.lookup(moduleName + "/" + beanName + "!" + interfaceQualifiedName);
		msgboxTimerBean.closeConnection();
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