package th.co.swimgw.gateway;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

public class Msgbox implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String atsEncode;
	private String atsFilingtime;
	private String atsOrgnref;
	private String atsPriority;
	private Integer attachcount;
	private Timestamp dtcreate;
	private Timestamp dtupdate;
	private String ipmId;
	private Boolean ipnRequest = Boolean.valueOf(false);
	private Integer mailboxid;
	private String msgBcc;
	private String msgCc;
	private String msgOrgn;
	private String msgOrgn2;
	private String msgPriority;
	private String msgSecurity;
	private String msgSubject;
	private String msgText;
	private String msgTo;
	private String msgType;
	private Boolean msgread = Boolean.valueOf(false);
	private Integer msgsqn;
	private Integer msgtype;
	private String mtsId;
	private Boolean pAltRcpAllow = Boolean.valueOf(false);
	private String pContIdt;
	private Boolean pContReturn = Boolean.valueOf(false);
	private Boolean pConvLossPhb = Boolean.valueOf(false);
	private String pDeferdelivery;
	private Boolean pDlExpPhb = Boolean.valueOf(false);
	private Boolean pImpConvPhb = Boolean.valueOf(false);
	private String pLatestdelivery;
	private Boolean pRcpDisc = Boolean.valueOf(false);
	private Boolean pRcpRasgPhb = Boolean.valueOf(false);
	private Integer rcpcount;
	private String receivetime;
	private Boolean rowdelete = Boolean.valueOf(false);
	private Integer size;
	private String subjectIpm;
	private Integer timezone;
	private String traceinfo;
	private String traceinfointernal;
	private Timestamp tsRcv;
	private Integer usercreate;
	private Integer userupdate;

//	private List<Msgboxrecipient> msgboxrecipients = new ArrayList<Msgboxrecipient>();
//	private List<Msgboxattachment> msgboxattachments = new ArrayList<Msgboxattachment>();

	private int status;

	private int atsEncodeAtt;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAtsFilingtime() {
		return this.atsFilingtime;
	}

	public void setAtsFilingtime(String atsFilingtime) {
		this.atsFilingtime = atsFilingtime;
	}

	public String getAtsOrgnref() {
		return this.atsOrgnref;
	}

	public void setAtsOrgnref(String atsOrgnref) {
		this.atsOrgnref = atsOrgnref;
	}

	public String getAtsPriority() {
		return this.atsPriority;
	}

	public void setAtsPriority(String atsPriority) {
		this.atsPriority = atsPriority;
	}

	public void setAtsEncode(String atsEncode) {
		this.atsEncode = atsEncode;
	}

	public Integer getAttachcount() {
		return this.attachcount;
	}

	public void setAttachcount(Integer attachcount) {
		this.attachcount = attachcount;
	}

	public Timestamp getDtcreate() {
		return this.dtcreate;
	}

	public void setDtcreate(Timestamp dtcreate) {
		this.dtcreate = dtcreate;
	}

	public Timestamp getDtupdate() {
		return this.dtupdate;
	}

	public void setDtupdate(Timestamp dtupdate) {
		this.dtupdate = dtupdate;
	}

	public void setIpnRequest(boolean ipnRequest) {
		this.ipnRequest = Boolean.valueOf(ipnRequest);
	}

	public Integer getMailboxid() {
		return this.mailboxid;
	}

	public void setMailboxid(Integer mailboxid) {
		this.mailboxid = mailboxid;
	}

	public boolean getMsgread() {
		return this.msgread.booleanValue();
	}

	public void setMsgread(boolean msgread) {
		this.msgread = Boolean.valueOf(msgread);
	}

	public Integer getMsgsqn() {
		return this.msgsqn;
	}

	public void setMsgsqn(Integer msgsqn) {
		this.msgsqn = msgsqn;
	}

	public Integer getMsgtype() {
		return this.msgtype;
	}

	public void setMsgtype(Integer msgtype) {
		this.msgtype = msgtype;
	}

	public boolean getPAltRcpAllow() {
		return this.pAltRcpAllow.booleanValue();
	}

	public void setPAltRcpAllow(boolean pAltRcpAllow) {
		this.pAltRcpAllow = Boolean.valueOf(pAltRcpAllow);
	}

	public boolean getPContReturn() {
		return this.pContReturn.booleanValue();
	}

	public void setPContReturn(boolean pContReturn) {
		this.pContReturn = Boolean.valueOf(pContReturn);
	}

	public boolean getPConvLossPhb() {
		return this.pConvLossPhb.booleanValue();
	}

	public void setPConvLossPhb(boolean pConvLossPhb) {
		this.pConvLossPhb = Boolean.valueOf(pConvLossPhb);
	}

	public boolean getPDlExpPhb() {
		return this.pDlExpPhb.booleanValue();
	}

	public void setPDlExpPhb(boolean pDlExpPhb) {
		this.pDlExpPhb = Boolean.valueOf(pDlExpPhb);
	}

	public boolean getPImpConvPhb() {
		return this.pImpConvPhb.booleanValue();
	}

	public void setPImpConvPhb(boolean pImpConvPhb) {
		this.pImpConvPhb = Boolean.valueOf(pImpConvPhb);
	}

	public boolean getPRcpDisc() {
		return this.pRcpDisc.booleanValue();
	}

	public void setPRcpDisc(boolean pRcpDisc) {
		this.pRcpDisc = Boolean.valueOf(pRcpDisc);
	}

	public boolean getPRcpRasgPhb() {
		return this.pRcpRasgPhb.booleanValue();
	}

	public void setPRcpRasgPhb(boolean pRcpRasgPhb) {
		this.pRcpRasgPhb = Boolean.valueOf(pRcpRasgPhb);
	}

	public Integer getRcpcount() {
		return this.rcpcount;
	}

	public void setRcpcount(Integer rcpcount) {
		this.rcpcount = rcpcount;
	}

	public boolean getRowdelete() {
		return this.rowdelete.booleanValue();
	}

	public void setRowdelete(boolean rowdelete) {
		this.rowdelete = Boolean.valueOf(rowdelete);
	}

	public Integer getSize() {
		return this.size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getSubjectIpm() {
		return this.subjectIpm;
	}

	public void setSubjectIpm(String subjectIpm) {
		this.subjectIpm = subjectIpm;
	}

	public Integer getTimezone() {
		return this.timezone;
	}

	public void setTimezone(Integer timezone) {
		this.timezone = timezone;
	}

	public Timestamp getTsRcv() {
		return this.tsRcv;
	}

	public void setTsRcv(Timestamp tsRcv) {
		this.tsRcv = tsRcv;
	}

	public Integer getUsercreate() {
		return this.usercreate;
	}

	public void setUsercreate(Integer usercreate) {
		this.usercreate = usercreate;
	}

	public Integer getUserupdate() {
		return this.userupdate;
	}

	public void setUserupdate(Integer userupdate) {
		this.userupdate = userupdate;
	}

	public String getMsgCc() {
		return this.msgCc;
	}

	public void setMsgCc(String msgCc) {
		this.msgCc = msgCc;
	}

	public String getMsgOrgn() {
		return this.msgOrgn;
	}

	public void setMsgOrgn(String msgOrgn) {
		this.msgOrgn = msgOrgn;
	}

	public String getMsgOrgn2() {
		return this.msgOrgn2;
	}

	public void setMsgOrgn2(String msgOrgn2) {
		this.msgOrgn2 = msgOrgn2;
	}

	public String getMsgPriority() {
		return this.msgPriority;
	}

	public void setMsgPriority(String msgPriority) {
		this.msgPriority = msgPriority;
	}

	public String getMsgSecurity() {
		return this.msgSecurity;
	}

	public void setMsgSecurity(String msgSecurity) {
		this.msgSecurity = msgSecurity;
	}

	public String getMsgSubject() {
		return this.msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgText() {
		return this.msgText;
	}

	public void setMsgText(String msgText) {
		this.msgText = msgText;
	}

	public String getMsgTo() {
		return this.msgTo;
	}

	public void setMsgTo(String msgTo) {
		this.msgTo = msgTo;
	}

	public String getMsgType() {
		return this.msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMtsId() {
		return this.mtsId;
	}

	public void setMtsId(String mtsId) {
		this.mtsId = mtsId;
	}

	public boolean ispAltRcpAllow() {
		return this.pAltRcpAllow.booleanValue();
	}

	public void setpAltRcpAllow(boolean pAltRcpAllow) {
		this.pAltRcpAllow = Boolean.valueOf(pAltRcpAllow);
	}

	public String getReceivetime() {
		return this.receivetime;
	}

	public void setReceivetime(String receivetime) {
		this.receivetime = receivetime;
	}

	public String getTraceinfo() {
		return this.traceinfo;
	}

	public void setTraceinfo(String traceinfo) {
		this.traceinfo = traceinfo;
	}

	public String getTraceinfointernal() {
		return this.traceinfointernal;
	}

	public void setTraceinfointernal(String traceinfointernal) {
		this.traceinfointernal = traceinfointernal;
	}

	public boolean isIpnRequest() {
		return this.ipnRequest.booleanValue();
	}

	public void setIpmId(String ipmId) {
		this.ipmId = ipmId;
	}

	public void setMsgBcc(String msgBcc) {
		this.msgBcc = msgBcc;
	}

	public String getpContIdt() {
		return this.pContIdt;
	}

	public void setpContIdt(String pContIdt) {
		this.pContIdt = pContIdt;
	}

	public String getpDeferdelivery() {
		return this.pDeferdelivery;
	}

	public void setpDeferdelivery(String pDeferdelivery) {
		this.pDeferdelivery = pDeferdelivery;
	}

	public String getpLatestdelivery() {
		return this.pLatestdelivery;
	}

	public void setpLatestdelivery(String pLatestdelivery) {
		this.pLatestdelivery = pLatestdelivery;
	}

//	public List<Msgboxrecipient> getMsgboxrecipients() {
//		return this.msgboxrecipients;
//	}
//
//	public void setMsgboxrecipients(List<Msgboxrecipient> msgboxrecipients) {
//		this.msgboxrecipients = msgboxrecipients;
//	}
//
//	public List<Msgboxattachment> getMsgboxattachments() {
//		return this.msgboxattachments;
//	}
//
//	public void setMsgboxattachments(List<Msgboxattachment> msgboxattachments) {
//		this.msgboxattachments = msgboxattachments;
//	}

	public String getAtsEncode() {
		return atsEncode;
	}

	public String getIpmId() {
		return ipmId;
	}

	public String getMsgBcc() {
		return msgBcc;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getAtsEncodeAtt() {
		return atsEncodeAtt;
	}

	public void setAtsEncodeAtt(int atsEncodeAtt) {
		this.atsEncodeAtt = atsEncodeAtt;
	}

//	public Msgboxrecipients getMsgboxrecipientlist() {
//		return msgboxrecipientlist;
//	}
//
//	public void setMsgboxrecipientlist(Msgboxrecipients msgboxrecipientlist) {
//		this.msgboxrecipientlist = msgboxrecipientlist;
//	}
//
//	public Msgboxattachments getMsgboxattachmentList() {
//		return msgboxattachmentList;
//	}
//
//	public void setMsgboxattachmentList(Msgboxattachments msgboxattachmentList) {
//		this.msgboxattachmentList = msgboxattachmentList;
//	}
	
	
}
