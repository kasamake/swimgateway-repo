package th.co.aerothai.swimgw.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.services.amhs.ConvertorBean;
import th.co.aerothai.swimgw.services.amhs.RcvUtils;
import th.co.aerothai.swimgw.services.amhs.SendUtils;
import th.co.aerothai.swimgw.services.jms.Producer;

public class Main {
	public static void main(String[] args) {

		// MsgboxBean msgboxBean = new MsgboxBean();
		// Msgbox msgbox = msgboxBean.getMsgbox(637);
		// System.out.println("MSG subject: "+msgbox.getMsgText());

		// Msgbox msgbox = createSampleMsg();
		// Msgbox msgbox2 = createSampleMsg();
		// List<Msgbox> msgboxs = new ArrayList<>();
		// msgboxs.add(msgbox);
		// msgboxs.add(msgbox2);
		// MsgboxBean.addMsgbox(msgboxs);

		// try {
		// List<Msgbox> msgboxs = RcvUtils.getMsgboxBeanList(
		// "CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/", "c=TH",
		// "&quot;3001&quot;/URI+0000+URL+itot://172.16.21.134:3001", "secret");
		// MsgboxBean.addMsgbox(msgboxs);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// SendUtils.send_msg(createSampleMsg(),
		// "CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/", "c=TH",
		// "\"3001\"/URI+0000+URL+itot://172.16.21.134:3001", "secret");

		/*
		 * Logger logger = Logger.getLogger(Main.class); List<Msgbox> msgboxs =
		 * new ArrayList<>(); try { msgboxs = RcvUtils.getMsgboxBeanList(
		 * "CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/", "c=TH",
		 * "\"3001\"/URI+0000+URL+itot://172.16.21.134:3001", "secret");
		 * 
		 * Producer producer = new Producer(); // Producer producerFIXM = new
		 * Producer(); // Producer producerWXXM = new Producer();
		 * 
		 * try { producer.create("tcp://172.16.21.206:61616", "jms-client" +
		 * "-producer", "karaf", "karaf"); //
		 * producerFIXM.create("tcp://172.16.21.206:61616", "jms-client" +
		 * "-producer-fixm", "karaf", "karaf", "AMHS2FIXM"); //
		 * producerWXXM.create("tcp://172.16.21.206:61616", "jms-client" +
		 * "-producer-wxxm", "karaf", "karaf", "AMHS2WXXM");
		 * 
		 * if(msgboxs.size()>0){
		 * 
		 * for (Msgbox msgbox : msgboxs) { // do something with the msg (try to
		 * add something to the subject) //
		 * msgbox.setMsgSubject(msgbox.getMsgSubject());
		 * 
		 * String type = ConvertorBean.checkMessageType(msgbox.getMsgText());
		 * msgbox.setMsgType(type); System.out.println("Type: "+type); String
		 * xml = ConvertorBean.convertMsgboxToXml(msgbox);
		 * System.out.println("XML: "+xml); switch (type) { case "FPL": case
		 * "DEF": case "ARR": case "CHG": case "DLA": case "CNL":
		 * logger.info("Message:has been sent to FIXM successfully " +
		 * msgbox.getMsgboxToSwimDetail()); producer.sendMessage(xml, "FIXM");
		 * break; case "NOTAM":
		 * logger.info("Message:has been sent to AIXM successfully " +
		 * msgbox.getMsgboxToSwimDetail()); producer.sendMessage(xml, "AIXM");
		 * // try { // producerAIXM.sendMessage(xml); //
		 * LOGGER.info("Message: '{}' has been sent to AIXM successfully",
		 * msgbox.getMsgboxToSwimDetail()); // } catch (JMSException e) { //
		 * LOGGER.error("Message: '{}' failed to be sent to AIXM", e); ////
		 * LOGGER.error("Message: "+
		 * msgbox.getMsgboxToSwimDetail()+" cannot be sent to AIXM", e); // }
		 * break; case "SA": case "SP": case "WS": case "FT": case "FC": case
		 * "FK": case "FV": case "WV": case "WC": case "WO": case "UA": case
		 * "NO": logger.info("Message:has been sent to WXXM successfully " +
		 * msgbox.getMsgboxToSwimDetail()); producer.sendMessage(xml, "WXXM");
		 * // try { // producerWXXM.sendMessage(xml); //
		 * LOGGER.info("Message: '{}' has been sent to AIXM successfully",
		 * msgbox.getMsgboxToSwimDetail()); // } catch (JMSException e) { //
		 * LOGGER.error("Message: '{}' failed to be sent to WXXM", e); ////
		 * LOGGER.error("Message: "+
		 * msgbox.getMsgboxToSwimDetail()+" cannot be sent to WXXM", e); // }
		 * break; default:
		 * 
		 * // messages cannot be categorized --> This incidence needs to be
		 * logged // producer.sendMessage(xml); break; }
		 * 
		 * MsgboxBean.addMsgbox(msgbox); }
		 * 
		 * int retreiveMsgNo = msgboxs.size(); int lastestSqn = ((Msgbox)
		 * msgboxs.get(retreiveMsgNo - 1)).getMsgsqn().intValue();
		 * System.out.println("Retreive: "+retreiveMsgNo);
		 * System.out.println("Sqn: "+lastestSqn); } } catch (JMSException e1) {
		 * // TODO Auto-generated catch block e1.printStackTrace(); }
		 * producer.closeConnection(); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		ScheduledExecutorService execService = Executors.newScheduledThreadPool(5);
		execService.scheduleAtFixedRate(() -> {
			// The repetitive task, say to update Database
			System.out.println("hi there at: " + new java.util.Date());
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 0, 5000L, TimeUnit.MILLISECONDS);

	}

	public static Msgbox createSampleMsg() {
		String content_id = "030924.140219";
		String latest_del_time = "170927120000Z";

		Msgbox msgBox = new Msgbox();
		msgBox.setMsgOrgn("/CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/");
		msgBox.setMsgOrgn2("VTBBSWIM");
		msgBox.setMsgSubject("Test sending message from Java");
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
}
