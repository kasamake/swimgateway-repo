package th.co.aerothai.swimgw.services.api;

import java.util.List;

import javax.ejb.Remote;

import th.co.aerothai.swimgw.models.Msgbox;

@Remote
public abstract interface IRemoteX400Utils
{
//  public abstract int createSession(int paramInt);
  
  public abstract List<Msgbox> retreiveMsg();
  
//  public abstract int getLatestSqn();
  
  public abstract void end();
  
  public abstract List<Msgbox> getMsgBoxBeanList();
  
  public abstract int sendMsgbox(Msgbox msgbox);
  
  public abstract List<Msgbox> getMsgBoxBeanList(String or, String dn, String pa, String credential);

  public abstract int sendMsgbox(Msgbox msgbox, String or, String dn, String pa, String credential);
}
