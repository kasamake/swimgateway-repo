package th.co.aerothai.swimgw.jms.api;

import javax.ejb.Remote;

@Remote
public interface IRemoteMsgboxTimerBean {
	public abstract void openConnection();
	public abstract void closeConnection();
	public abstract void atSchedule ();
//	public abstract String checkMessageType(String msgtxt);
	public abstract void setRunning(boolean running);
}
