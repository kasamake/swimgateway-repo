package th.co.aerothai.swimgw.client.web;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.jms.impl.MsgboxTimerBean;
//import th.co.aerothai.swimgw.jndi.lookup.LookerUp;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.x400.SendUtils;

@ManagedBean(name = "amhsManagedBean")
@SessionScoped
public class AmhsManagedBean {
	private int latestSqn;

	@EJB
	private IX400Utils x400Utils;
	private int retreiveMsgNo;
	private int sqn;
//	private boolean serviceRunning;
	private String status;
	@EJB
	IMsgboxBean msgboxBean;

	@EJB
	MsgboxTimerBean timerBean;
	
	@PostConstruct
	public void setup() {
		System.out.println("Setting up after creating the JSF managed bean.");
	}


	public String receiveMessage() throws NamingException {


		List<Msgbox> msgboxes = this.x400Utils.getMsgBoxBeanList();
		System.out.println("Sqn: " + this.sqn);
		if (this.sqn != 0) {
			for (Msgbox msgbox : msgboxes) {
				if (msgbox.getMsgsqn() == this.sqn) {
					this.msgboxBean.addMsgbox(msgbox);
					break;
				}
			}
		} else {
			this.msgboxBean.addMsgbox(msgboxes);
		}

		if (msgboxes.size() != 0) {
			setRetreiveMsgNo(msgboxes.size());
			setLatestSqn(((Msgbox) msgboxes.get(getRetreiveMsgNo() - 1)).getMsgsqn().intValue());
		}

		return "amhs_receiver.xhtml";
	}

	public String sendMessage() throws NamingException {

		Msgbox sendMsgbox = createSampleMsg();
		int status = SendUtils.send_msg(null, sendMsgbox);
		if (status == 0) {
			Msgbox addMsgbox = this.msgboxBean.addMsgbox(sendMsgbox);
			System.out.println("send message ID: " + addMsgbox.getId());
		}
		return "amhs_sender.xhtml";
	}

	public static Msgbox createSampleMsg() {
		String content_id = "030924.140219";
		String latest_del_time = "170927120000Z";

		Msgbox msgBox = new Msgbox();
		msgBox.setMsgOrgn("/CN=VTBBYUAA/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/");
		msgBox.setMsgOrgn2("VTBBYUAA");
		msgBox.setMsgSubject("Test msg from Java (AMHS managed bean)");
		msgBox.setMsgText("This is a boday part from Java");
		msgBox.setpContIdt(content_id);
		msgBox.setpLatestdelivery(latest_del_time);
		msgBox.setMsgPriority("1");
		msgBox.setIpmId("1064400656.24922*");
		msgBox.setAtsEncode("ia5-text");
		msgBox.setAtsEncodeAtt(X400_att.X400_T_IA5TEXT);

		List<Msgboxrecipient> recipients = new ArrayList<Msgboxrecipient>();
		Msgboxrecipient recipient1 = new Msgboxrecipient();
		recipient1.setMsgbox(msgBox);
		recipient1.setOraddress("/CN=VTBBYUAB/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX");
		recipient1.setAliasname("VTBBYUAB");
		recipient1.setRecipienttype("PRIMARY");
		recipients.add(recipient1);

		Msgboxrecipient recipient2 = new Msgboxrecipient();
		recipient2.setMsgbox(msgBox);
		recipient2.setOraddress("/CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/");
		recipient2.setAliasname("VTBBSWIM");
		recipient2.setRecipienttype("PRIMARY");
		recipients.add(recipient2);

		msgBox.setMsgTo("VTBBYUAB VTBBSWIM");
		msgBox.setMsgboxrecipients(recipients);

		List<Msgboxattachment> attachments = new ArrayList<Msgboxattachment>();
		Msgboxattachment attachment = new Msgboxattachment();
		attachment.setMsgbox(msgBox);
		attachment.setFilename("TextFile11.txt");
		attachment.setFiletype(Integer.valueOf(406));
		byte[] textBytes = "Test attachment - file.txt".getBytes();
		attachment.setFilesize(Integer.valueOf(textBytes.length));
		attachment.setBfile(textBytes);
		attachments.add(attachment);

		Msgboxattachment attachment2 = new Msgboxattachment();
		attachment2.setMsgbox(msgBox);
		attachment2.setFilename("TextFile22.txt");
		attachment2.setFiletype(Integer.valueOf(406));
		byte[] textBytes2 = "Test attachment 2 - file.txt".getBytes();
		attachment2.setFilesize(Integer.valueOf(textBytes2.length));
		attachment2.setBfile(textBytes2);
		attachments.add(attachment2);

		msgBox.setMsgboxattachments(attachments);

		msgBox.setAttachcount(Integer.valueOf(attachments.size()));
		msgBox.setRcpcount(Integer.valueOf(recipients.size()));
		return msgBox;
	}

	public void startReceivingMessage() throws NamingException {

		this.status = "Service started";
		timerBean.openConnection();
//		timerBean.setRunning(true);

//		return "amhs_receiver.xhtml";
	}
	
	public void stopReceivingMessage() throws NamingException, JMSException {
		this.status = "Service stopped";
//		timerBean.setRunning(false);
		timerBean.closeConnection();
//		return "amhs_receiver.xhtml";
	}

	public int getLatestSqn() {
		return this.latestSqn;
	}

	public void setLatestSqn(int latestSqn) {
		this.latestSqn = latestSqn;
	}

	@PreDestroy
	public void cleanUp() {
		System.out.println("Cleaning up before destroying the JSF managed bean.");
		if (this.x400Utils != null) {
			this.x400Utils.end();
		}
	}

	public int getRetreiveMsgNo() {
		return this.retreiveMsgNo;
	}

	public void setRetreiveMsgNo(int retreiveMsgNo) {
		this.retreiveMsgNo = retreiveMsgNo;
	}

	public int getSqn() {
		return sqn;
	}

	public void setSqn(int sqn) {
		this.sqn = sqn;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


}
