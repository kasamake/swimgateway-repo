package th.co.aerothai.swimgw.services.api;

import java.util.List;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;

public abstract interface IMsgboxBean
{
  public abstract Msgbox addMsgbox(Msgbox paramMsgbox);
  
  public abstract boolean addMsgbox(List<Msgbox> paramList);
  
  public abstract int addMsgbox(List<Msgbox> paramList, int paramInt);
  
  public abstract Msgbox getMsgbox(int paramInt);
  
  public abstract List<Msgbox> getMsgboxes(String paramString);
  
  public abstract int saveFileAttachment(Msgboxattachment paramMsgboxattachment, int paramInt1, int paramInt2);
}
