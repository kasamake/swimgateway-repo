package th.co.aerothai.swimgw.client.web;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.NamingException;
import javax.validation.constraints.AssertTrue;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.jms.impl.MsgboxTimerBean;
//import th.co.aerothai.swimgw.jndi.lookup.LookerUp;
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.x400.SendUtils;

@ManagedBean(name = "msgboxManagedBean")
@SessionScoped
public class MsgboxManagedBean {
	private int latestSqn;

	@EJB
	private IX400Utils x400Utils;
	private int retreiveMsgNo;
	private int sqn;
	private boolean serviceRunning;
	@EJB
	IMsgboxBean msgboxBean;

	@EJB
	MsgboxTimerBean timerBean;
	
	@PostConstruct
	public void setup() {
		System.out.println("Setting up after creating the JSF managed bean.");
	}

	// public String start()
	// throws NamingException
	// {
	// if (this.x400Utils != null)
	// {
	// this.x400Utils.end();
	// this.x400Utils = null;
	// System.out.println("Refreshing Utils!");
	// }
	// String moduleName = "client-web";
	//
	//
	// String beanName = "X400UtilsBean";
	//
	// String interfaceQualifiedName = IX400Utils.class.getName();
	//
	//
	// LookerUp wildf9Lookerup = new LookerUp();
	//
	//
	//
	// this.x400Utils =
	// ((IX400Utils)wildf9Lookerup.findLocalSessionBean(moduleName, beanName,
	// interfaceQualifiedName));
	//
	// return "msgbox.xhtml";
	// }

	public String receiveMessage() throws NamingException {
		// if (this.x400Utils != null)
		// {
		// this.x400Utils.end();
		// this.x400Utils = null;
		// }
		// String moduleName = "client-web";
		//
		//
		// String beanName = "X400UtilsBean";
		//
		// String interfaceQualifiedName = IX400Utils.class.getName();
		//
		//
		// LookerUp wildf9Lookerup = new LookerUp();
		//
		//
		//
		// this.x400Utils =
		// ((IX400Utils)wildf9Lookerup.findLocalSessionBean(moduleName,
		// beanName,
		// interfaceQualifiedName));

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

		return "msgbox.xhtml";
	}

	public String sendMessage() throws NamingException {
		// if (this.x400Utils != null)
		// {
		// this.x400Utils.end();
		// this.x400Utils = null;
		// }
		// String moduleName = "client-web";
		//
		// String beanName = "X400UtilsBean";
		//
		// String interfaceQualifiedName = IX400Utils.class.getName();
		//
		// LookerUp wildf9Lookerup = new LookerUp();
		//
		// this.x400Utils =
		// ((IX400Utils)wildf9Lookerup.findLocalSessionBean(moduleName,
		// beanName,
		// interfaceQualifiedName));

		Msgbox sendMsgbox = createSampleMsg();
		int status = SendUtils.send_msg(null, sendMsgbox);
		if (status == 0) {
			Msgbox addMsgbox = this.msgboxBean.addMsgbox(sendMsgbox);
			System.out.println("send message ID: " + addMsgbox.getId());
		}
		return "msgbox.xhtml";
	}

	public static Msgbox createSampleMsg() {
		String content_id = "030924.140219";
		String latest_del_time = "170927120000Z";

		Msgbox msgBox = new Msgbox();
		msgBox.setMsgOrgn("/CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/");
		msgBox.setMsgOrgn2("VTBBSWIM");
		msgBox.setMsgSubject("Test 1 2 3");
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
		recipient2.setOraddress("/CN=VTBBYUAA/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/");
		recipient2.setAliasname("VTBBYUAA");
		recipient2.setRecipienttype("PRIMARY");
		recipients.add(recipient2);

		msgBox.setMsgTo("VTBBYUAB VTBBYUAA");
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

		// Msgboxattachment attachment3 = new Msgboxattachment();
		// attachment3.setMsgbox(msgBox);
		// Path path = Paths.get("C:/Users/Kasama/Documents/TestPdfFile.pdf",
		// new String[0]);
		// try
		// {
		// byte[] bytes3 = Files.readAllBytes(path);
		// attachment3.setFilename("TestPdfFile.pdf");
		// attachment3.setFiletype(Integer.valueOf(406));
		// attachment3.setFilesize(Integer.valueOf(bytes3.length));
		// attachment3.setBfile(bytes3);
		// attachments.add(attachment3);
		// }
		// catch (IOException e)
		// {
		// e.printStackTrace();
		// }
		msgBox.setMsgboxattachments(attachments);

		msgBox.setAttachcount(Integer.valueOf(attachments.size()));
		msgBox.setRcpcount(Integer.valueOf(recipients.size()));
		return msgBox;
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

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

}
