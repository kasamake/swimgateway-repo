package th.co.aerothai.swimgw.client.web;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.jms.JMSException;

import th.co.aerothai.swimgw.client.activemq.Producer;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IX400Utils;


@Singleton
@Lock(LockType.READ)
public class MsgboxTimerBean {

	@EJB
    IX400Utils x400Utils;

	@EJB
	IConvertorBean convertorBean;
	
    @EJB
    IMsgboxBean msgboxBean;            
    
    private int retreiveMsgNo;
    private int lastestSqn;

    private boolean running;
    
    private Producer producer;
    private Producer producerAIXM;
    private Producer producerFIXM;
    private Producer producerWXXM;
//    private AtomicBoolean busy = new AtomicBoolean(false);
    
    public void openConnection() throws JMSException {
    	producer = new Producer();
        producer.create("jms-client-producer", "AMHS");
        
        producerAIXM = new Producer();
        producerAIXM.create("jms-client-producer-aixm", "AMHS2AIXM");
        
        producerFIXM = new Producer();
        producerFIXM.create("jms-client-producer-fixm", "AMHS2FIXM");
        
        producerWXXM = new Producer();
        producerWXXM.create("jms-client-producer-wxxm", "AMHS2WXXM");
    }
    
    public void closeConnection() throws JMSException {
    	producer.closeConnection();
    	producerAIXM.closeConnection();
    	producerFIXM.closeConnection();
    	producerWXXM.closeConnection();
    }
    
    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void atSchedule() throws InterruptedException, JMSException {
//        if (!busy.compareAndSet(false, true)) {
//            return;
//        }

//        try {
    	
    		if(running){
    			
                System.out.println("Timer work started");
                
                //main task to do
                List<Msgbox> msgboxes = this.x400Utils.getMsgBoxBeanList();
                
                if(msgboxes.size()>0){
                	
//                	producer = new Producer();
//                	
//                    producer.create("jms-client-producer", "AMHS");
                    
                    for (Msgbox msgbox : msgboxes) {
                    	// do something with the msg (try to add something to the subject)
                    	msgbox.setMsgSubject(msgbox.getMsgSubject()+"  [sent from Gateway]");
                    	
                    	String type = checkMessageType(msgbox.getMsgText());
                    	System.out.println("Type: "+type);
    					String xml = this.convertorBean.convertMsgboxToXml(msgbox);
    					System.out.println("XML: "+xml);
                    	switch (type) {
						case "FPL":
						case "DEF":
						case "ARR":
						case "CHG":
						case "DLA":
						case "CNL":
							producerFIXM.sendMessage(xml);
							break;
						case "NOTAM":
							producerAIXM.sendMessage(xml);
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
							producerWXXM.sendMessage(xml);
							break;
						default:
							producer.sendMessage(xml);
							break;
						}

    					
//    					producer.sendMessage(xml);
    					this.msgboxBean.addMsgbox(msgbox);
    				}
                    
                    this.retreiveMsgNo = msgboxes.size();
    				this.lastestSqn = ((Msgbox) msgboxes.get(getRetreiveMsgNo() - 1)).getMsgsqn().intValue();
                }
                
                Thread.sleep(2000);
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

	public int getRetreiveMsgNo() {
		return retreiveMsgNo;
	}
	public void setRetreiveMsgNo(int retreiveMsgNo) {
		this.retreiveMsgNo = retreiveMsgNo;
	}
	public int getLastestSqn() {
		return lastestSqn;
	}
	public void setLastestSqn(int lastestSqn) {
		this.lastestSqn = lastestSqn;
	}

	@Lock(LockType.READ)
	public boolean isRunning() {
		return running;
	}

	@Lock(LockType.READ)
	public void setRunning(boolean running) {
		this.running = running;
	}

    
}
