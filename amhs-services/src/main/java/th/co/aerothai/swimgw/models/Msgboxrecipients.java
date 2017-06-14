package th.co.aerothai.swimgw.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement(name = "msgboxrecipients")
@XmlAccessorType (XmlAccessType.FIELD)
public class Msgboxrecipients 
{
    @XmlElement(name = "msgboxrecipient")
    private List<Msgboxrecipient> msgboxrecipients = null;
 
    public List<Msgboxrecipient> getMsgboxrecipients() {
        return msgboxrecipients;
    }
 
    public void setMsgboxrecipients(List<Msgboxrecipient> msgboxrecipients) {
        this.msgboxrecipients = msgboxrecipients;
    }
}