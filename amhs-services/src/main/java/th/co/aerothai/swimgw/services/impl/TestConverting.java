package th.co.aerothai.swimgw.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;

public class TestConverting {

	public static void main(String[] args) {

		try {

			String filename = "C:\\Users\\Kasama\\file.xml";
//			File file = new File(filename);
//
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String line;
			StringBuilder sb = new StringBuilder();

			while ((line = br.readLine()) != null) {
				sb.append(line.trim());
			}

			String xml = sb.toString();
			System.out.println("XML0: "+xml);
//			xml = xml.replaceAll("\u0001", "[SOH]");
//			System.out.println("XML1: "+xml);
//			xml = xml.replaceAll("\u0002", "[STX]");
//			System.out.println("XML2: "+xml);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(Msgbox.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(xml);
			Msgbox msgbox = (Msgbox) jaxbUnmarshaller.unmarshal(reader);

			System.out.println("MSG BOX: " + msgbox.getId() + " " + msgbox.getMsgText());
			List<Msgboxrecipient> recipientList = msgbox.getMsgboxrecipientlist().getMsgboxrecipients();
			msgbox.setMsgboxrecipients(recipientList);
			for (Msgboxrecipient msgboxrecipient : recipientList)
				msgboxrecipient.setMsgbox(msgbox);

			List<Msgboxattachment> attachmentList = msgbox.getMsgboxattachmentList().getMsgboxattachments();
			msgbox.setMsgboxattachments(attachmentList);
			for (Msgboxattachment msgboxattachment : attachmentList)
				msgboxattachment.setMsgbox(msgbox);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
