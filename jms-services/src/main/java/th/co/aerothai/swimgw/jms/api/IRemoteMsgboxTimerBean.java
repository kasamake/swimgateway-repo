package th.co.aerothai.swimgw.jms.api;

import javax.ejb.Remote;

@Remote
public interface IRemoteMsgboxTimerBean {
	public abstract int openConnection();
	public abstract void closeConnection();
	public abstract void atSchedule ();
//	public abstract String checkMessageType(String msgtxt);
	public abstract void setRunning(boolean running);
	public abstract void setOr(String or);
	public abstract void setDn(String dn);
	public abstract void setPa(String pa);
	public abstract void setCredential(String credential);
	public abstract void setBroker(String broker);
	public abstract void setClient(String client);
	public abstract void setUsername(String username);
	public abstract void setPassword(String password);
	
	public abstract boolean isRunning();
//	public abstract boolean pingSWIM();
//	public abstract boolean isTestAMHS();
}
