package th.co.aerothai.swimgw.models;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

@Entity
@Table(name="MSGBOXRECIPIENT")
@NamedQuery(name="Msgboxrecipient.findAll", query="SELECT m FROM Msgboxrecipient m")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Msgboxrecipient
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="ID")
  @XmlTransient
  private Integer id;
  @Column(name="ADDRESS")
  private Integer address;
  @Column(name="ALIASNAME")
  private String aliasname;
  @Column(name="DELIVERY_REPORT")
  private Integer deliveryReport;
  @Column(name="ORADDRESS")
  private String oraddress;
  @Column(name="RECEIPT_NOTIFICATION")
  private Integer receiptNotification;
  @Column(name="RECIPIENTTYPE")
  private String recipienttype;
  @ManyToOne
  @JoinColumn(name="MSGID", referencedColumnName="ID", insertable=true)
  @XmlTransient
  private Msgbox msgbox;
  
  public Integer getId()
  {
    return this.id;
  }
  
  public void setId(Integer id)
  {
    this.id = id;
  }
  
  public Integer getAddress()
  {
    return this.address;
  }
  
  public void setAddress(Integer address)
  {
    this.address = address;
  }
  
  public Integer getDeliveryReport()
  {
    return this.deliveryReport;
  }
  
  public void setDeliveryReport(Integer deliveryReport)
  {
    this.deliveryReport = deliveryReport;
  }
  
  public Integer getReceiptNotification()
  {
    return this.receiptNotification;
  }
  
  public void setReceiptNotification(Integer receiptNotification)
  {
    this.receiptNotification = receiptNotification;
  }
  
  public String getAliasname()
  {
    return this.aliasname;
  }
  
  public void setAliasname(String aliasname)
  {
    this.aliasname = aliasname;
  }
  
  public String getOraddress()
  {
    return this.oraddress;
  }
  
  public void setOraddress(String oraddress)
  {
    this.oraddress = oraddress;
  }
  
  public String getRecipienttype()
  {
    return this.recipienttype;
  }
  
  public void setRecipienttype(String recipienttype)
  {
    this.recipienttype = recipienttype;
  }
  
  public Msgbox getMsgbox()
  {
    return this.msgbox;
  }
  
  public void setMsgbox(Msgbox msgbox)
  {
    this.msgbox = msgbox;
  }
}
