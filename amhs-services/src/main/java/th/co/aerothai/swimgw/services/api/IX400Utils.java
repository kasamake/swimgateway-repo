package th.co.aerothai.swimgw.services.api;

import java.util.List;
import th.co.aerothai.swimgw.models.Msgbox;

public abstract interface IX400Utils
{
//  public abstract int createSession(int paramInt);
  
  public abstract List<Msgbox> retreiveMsg();
  
//  public abstract int getLatestSqn();
  
  public abstract void end();
  
  public abstract List<Msgbox> getMsgBoxBeanList();
  
  public abstract int sendMsgbox(Msgbox msgbox);
  
  public abstract List<Msgbox> getMsgBoxBeanList(String or, String dn, String pa, String credential) throws Exception;
  
  public abstract boolean testConnection (String or, String dn, String pa, String credential);
}
