package th.co.aerothai.swimgw.jms.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import th.co.aerothai.swimgw.jms.api.IRemoteMsgboxTimerBean;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;
import th.co.aerothai.swimgw.services.api.IX400Utils;

@Singleton(name="RemoteMsgboxTimerBean")
@Remote(IRemoteMsgboxTimerBean.class)
@Lock(LockType.READ)
public class RemoteMsgboxTimerBean implements IRemoteMsgboxTimerBean{

	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteMsgboxTimerBean.class);
	
//	private static Logger logger = LogManager.getLogger(MsgboxTimerBean.class.getName());
	@EJB
    IX400Utils x400Utils;

	@EJB
	IConvertorBean convertorBean;
	
    @EJB
    IMsgboxBean msgboxBean;            
    
    private int retreiveMsgNo;
    private int lastestSqn;

    private boolean running;
    
    private String or;
    private String dn;
    private String pa;
    private String credential;
    private String broker;
    private String client;
    private String username;
    private String password;
    
//    private Producer producer;
    private Producer producerAIXM;
    private Producer producerFIXM;
    private Producer producerWXXM;
//    private AtomicBoolean busy = new AtomicBoolean(false);
    
    public void openConnection() {
//    	producer = new Producer();
//        producer.create("jms-client-producer", "AMHS");
        LOGGER.info("Open Connections to ActiveMQ -- AIXM/FIXM/WXXM");
        LOGGER.debug("Debug: Open Connections to ActiveMQ -- AIXM/FIXM/WXXM");
        try {
            producerAIXM = new Producer();
			producerAIXM.create(broker,client+"-producer-aixm",username,password, "AMHS2AIXM");
			producerFIXM = new Producer();
			producerFIXM.create(broker,client+"-producer-fixm",username,password, "AMHS2FIXM");
			producerWXXM = new Producer();
			producerWXXM.create(broker,client+"-producer-wxxm",username,password, "AMHS2WXXM");
			
//			setRunning(true);
		} catch (JMSException e) {
			LOGGER.error("Producers cannot be created", e);
			try {
				producerAIXM.closeConnection();
				producerFIXM.closeConnection();
				producerWXXM.closeConnection();
			} catch (JMSException e1) {
				// do nothing
			}
		}
        setRunning(true);

    }
    
    public void closeConnection() {
//    	producer.closeConnection();
    	LOGGER.info("Close Connections to ActiveMQ -- AIXM/FIXM/WXXM");
    	setRunning(false);
    	try {
			producerAIXM.closeConnection();
		} catch (JMSException e) {
			LOGGER.error("Producer cannot be disconnected for AIXM", e);
		}
    	try {
			producerFIXM.closeConnection();
		} catch (JMSException e) {
			LOGGER.error("Producer cannot be disconnected for FIXM", e);
		}
    	try {
			producerWXXM.closeConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void atSchedule(){
//        if (!busy.compareAndSet(false, true)) {
//            return;
//        }

//        try {
    	
    		if(running){
    			
                System.out.println("Timer work started");
                
                //main task to do
                List<Msgbox> msgboxes = this.x400Utils.getMsgBoxBeanList(this.or, this.dn, this.pa, this.credential);
                
                if(msgboxes.size()>0){
       
                    for (Msgbox msgbox : msgboxes) {
                    	// do something with the msg (try to add something to the subject)
                    	msgbox.setMsgSubject(msgbox.getMsgSubject()+"  [sent from Gateway]");
                    	
                    	String type = checkMessageType(msgbox.getMsgText());
                    	msgbox.setMsgType(type);
//                    	System.out.println("Type: "+type);
    					String xml = this.convertorBean.convertMsgboxToXml(msgbox);
//    					System.out.println("XML: "+xml);
                    	switch (type) {
						case "FPL":
						case "DEF":
						case "ARR":
						case "CHG":
						case "DLA":
						case "CNL":
							try {
								producerFIXM.sendMessage(xml);
								LOGGER.info("Message: '{}' has been sent to FIXM successfully", msgbox.getMsgboxToSwimDetail());
							} catch (JMSException e) {
								LOGGER.error("Message: '{}' failed to be sent to FIXM", e);
//								LOGGER.error("Message: "+ msgbox.getMsgboxToSwimDetail()+" cannot be sent to FIXM", e);
							}
							break;
						case "NOTAM":
							try {
								producerAIXM.sendMessage(xml);
								LOGGER.info("Message: '{}' has been sent to AIXM successfully", msgbox.getMsgboxToSwimDetail());
							} catch (JMSException e) {
								LOGGER.error("Message: '{}' failed to be sent to AIXM", e);
//								LOGGER.error("Message: "+ msgbox.getMsgboxToSwimDetail()+" cannot be sent to AIXM", e);
							}
							break;
						case "SA":
						case "SP":
						case "WS":
						case "FT":
						case "FC":
						case "FK":
						case "FV":
						case "WV":
						case "WC":
						case "WO":
						case "UA":
						case "NO":
							try {
								producerWXXM.sendMessage(xml);
								LOGGER.info("Message: '{}' has been sent to AIXM successfully", msgbox.getMsgboxToSwimDetail());
							} catch (JMSException e) {
								LOGGER.error("Message: '{}' failed to be sent to WXXM", e);
//								LOGGER.error("Message: "+ msgbox.getMsgboxToSwimDetail()+" cannot be sent to WXXM", e);
							}
							break;
						default:
							
							// messages cannot be categorized --> This incidence needs to be logged 
//							producer.sendMessage(xml);
							break;
						}

                    	this.msgboxBean.addMsgbox(msgbox);
    				}
                    
                    this.retreiveMsgNo = msgboxes.size();
    				this.lastestSqn = ((Msgbox) msgboxes.get(this.retreiveMsgNo - 1)).getMsgsqn().intValue();
                }
                
                try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					LOGGER.error("Something work with thrad sleep: "+e);
				}
                System.out.println("Timer work done");
    		}
    }
   
    private String checkMessageType(String msgtxt) {
    	String msgtype = "";
        if (msgtxt.indexOf("(FPL-") >= 0) msgtype = "FPL";
        else if (msgtxt.indexOf("(DEP-") >= 0) msgtype = "DEP";
        else if (msgtxt.indexOf("(ARR-") >= 0) msgtype = "ARR";
        else if (msgtxt.indexOf("(CHG-") >= 0) msgtype = "CHG";
        else if (msgtxt.indexOf("(DLA-") >= 0) msgtype = "DLA";
        else if (msgtxt.indexOf("(CNL-") >= 0) msgtype = "CNL";
        else if (msgtxt.indexOf(" NOTAM") >= 0) msgtype = "NOTAM";

        if (msgtype == "")
        {
//            String pattern0 = "([A-Z]{2})[A-Z]{2}[0-9]{2} [A-Z]{4} [0-9]{6}";
            String pattern = "(\\D{2})(\\D{2}\\d{2}\\s{1}\\D{4}\\s{1}\\d{6})";
            
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(msgtxt);
            if (m.find())
            {
                String type = m.group(1);
//                System.out.println("Group1: "+m.group(1));
//                System.out.println("Group2: "+m.group(2));
                if (type.equals("SA") || type.equals("SP") || type.equals("WS") || type.equals("FT") || type.equals("FC") || type.equals("FK") || 
                		type.equals("FV") || type.equals("WV") || type.equals("WC") || type.equals("WO") || type.equals("UA") || type.equals("NO")){
                	msgtype = type;
                }
            }

            if (msgtype == "")
            {
                if (msgtxt.indexOf("SVC ") > -1)
                    msgtype = "SVC";
            }
            if(msgtype == "") msgtype = "Not Match";
        }
		return msgtype;
        
    }

//
//	@Lock(LockType.READ)
//	public boolean isRunning() {
//		return running;
//	}

	@Lock(LockType.READ)
	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setOr(String or) {
		this.or = or;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public void setPa(String pa) {
		this.pa = pa;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
    
}
