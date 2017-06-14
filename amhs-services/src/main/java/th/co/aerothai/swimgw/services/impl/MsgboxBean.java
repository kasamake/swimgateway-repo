package th.co.aerothai.swimgw.services.impl;

import java.io.PrintStream;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;

@Stateless
@Local
public class MsgboxBean
  implements IMsgboxBean
{
  @PersistenceContext(unitName="SwimgwPersistenceUnit")
  private EntityManager entityManager;
  
  public Msgbox addMsgbox(Msgbox msgbox)
  {
    System.out.println("Add Msgbox");
    this.entityManager.persist(msgbox);
    for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
      saveFileAttachment(msgBoxAttachment, msgBoxAttachment.getId().intValue(), msgbox.getId());
    }
    System.out.println("Msgbox ID: " + msgbox.getId());
    return msgbox;
  }
  
  public boolean addMsgbox(List<Msgbox> msgboxes)
  {
    System.out.println("Add msgboxes");
    for (int i = 0; i < msgboxes.size(); i++)
    {
      Msgbox msgbox = (Msgbox)msgboxes.get(i);
      this.entityManager.persist(msgbox);
      System.out.println("Msgbox ID: " + msgbox.getId());
      for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
        saveFileAttachment(msgBoxAttachment, msgBoxAttachment.getId().intValue(), msgbox.getId());
      }
    }
    return true;
  }
  
  public int addMsgbox(List<Msgbox> msgboxes, int latestAdd)
  {
    return 0;
  }
  
  public Msgbox getMsgbox(int id)
  {
    System.out.println("Get Msgbox");
    return (Msgbox)this.entityManager.find(Msgbox.class, Integer.valueOf(id));
  }
  
  public List<Msgbox> getMsgboxes(String msgTo)
  {
    return null;
  }
  
  public int saveFileAttachment(Msgboxattachment msgBoxAttachment, int msgBoxAttachmentId, int msgBoxId)
  {
    return 0;
  }
}
