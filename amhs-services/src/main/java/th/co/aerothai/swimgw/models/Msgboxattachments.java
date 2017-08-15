package th.co.aerothai.swimgw.models;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement(name = "msgboxattachments")
@XmlAccessorType (XmlAccessType.FIELD)
public class Msgboxattachments implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlElement(name = "msgboxattachment")
    private List<Msgboxattachment> msgboxattachments = null;
 
    
    public List<Msgboxattachment> getMsgboxattachments() {
		return msgboxattachments;
	}

	public void setMsgboxattachments(List<Msgboxattachment> msgboxattachments) {
		this.msgboxattachments = msgboxattachments;
	}
}