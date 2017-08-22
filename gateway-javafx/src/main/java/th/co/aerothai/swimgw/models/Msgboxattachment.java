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
@Table(name="MSGBOXATTACHMENT")
@NamedQuery(name="Msgboxattachment.findAll", query="SELECT m FROM Msgboxattachment m")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Msgboxattachment
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="ID")
  private Integer id;
  @Column(name="BFILE")
  private byte[] bfile;
  @Column(name="FILENAME")
  private String filename;
  @Column(name="FILESIZE")
  private Integer filesize;
  @Column(name="FILETYPE")
  private Integer filetype;
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
  
  public byte[] getBfile()
  {
    return this.bfile;
  }
  
  public void setBfile(byte[] bfile)
  {
    this.bfile = bfile;
  }
  
  public String getFilename()
  {
    return this.filename;
  }
  
  public void setFilename(String filename)
  {
    this.filename = filename;
  }
  
  public Integer getFilesize()
  {
    return this.filesize;
  }
  
  public void setFilesize(Integer filesize)
  {
    this.filesize = filesize;
  }
  
  public Integer getFiletype()
  {
    return this.filetype;
  }
  
  public void setFiletype(Integer filetype)
  {
    this.filetype = filetype;
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
