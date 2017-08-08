package th.co.aerothai.swimgw.services.api;

import java.util.List;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;

public abstract interface IConvertorBean
{
  public abstract String convertMsgboxToXml(int id);
  public abstract String convertMsgboxToFile(int id);
  public abstract String convertMsgboxToXml(Msgbox msgbox);
  public abstract Msgbox convertXMLFiletoMsgbox(String filename);
//  public abstract String convertMsgboxToJson(int id);
  public abstract Msgbox convertXMLStringtoMsgbox(String xml);
//  
//  public abstract boolean addMsgbox(List<Msgbox> paramList);
//  
//  public abstract int addMsgbox(List<Msgbox> paramList, int paramInt);
//  
//  public abstract Msgbox getMsgbox(int paramInt);
//  
//  public abstract List<Msgbox> getMsgboxes(String paramString);
//  
//  public abstract int saveFileAttachment(Msgboxattachment paramMsgboxattachment, int paramInt1, int paramInt2);
}
