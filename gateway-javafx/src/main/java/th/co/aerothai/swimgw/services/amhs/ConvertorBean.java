package th.co.aerothai.swimgw.services.amhs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.persistence.oxm.CharacterEscapeHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxattachments;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.models.Msgboxrecipients;

public class ConvertorBean{


	public static String convertMsgboxToXml(Msgbox msgbox) throws JAXBException {
		System.out.println("convertMsgboxToXml (msgbox)");

		System.out.println("MSG TEXT: "+msgbox.getMsgText());
		Msgboxrecipients msgboxrecipients = new Msgboxrecipients();
		msgboxrecipients.setMsgboxrecipients(msgbox.getMsgboxrecipients());
		msgbox.setMsgboxrecipientlist(msgboxrecipients);

		Msgboxattachments msgboxattachments = new Msgboxattachments();
		msgboxattachments.setMsgboxattachments(msgbox.getMsgboxattachments());
		msgbox.setMsgboxattachmentList(msgboxattachments);

		String xml = "";
		// Save customer to XML
		JAXBContext jc;

//		try {
			jc = JAXBContext.newInstance(Msgbox.class);
			Marshaller marshaller = jc.createMarshaller();


			StringWriter writer = new StringWriter();
			marshaller.marshal(msgbox, writer);
			xml = writer.toString();
//
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return xml;
	}


	public static Msgbox convertXMLStringtoMsgbox(String xml) throws JAXBException {
//		try {

			// filename = "C:\\Users\\Kasama\\file.xml";
			// File file = new File(filename);
			JAXBContext jaxbContext = JAXBContext.newInstance(Msgbox.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			xml = xml.replaceAll("\u0001", "#SOH#");
//			System.out.println("XML1: " + xml);

			xml = xml.replaceAll("\u0002", "#STX#");
//			System.out.println("XML2: " + xml);
			StringReader reader = new StringReader(xml);
			Msgbox msgbox = (Msgbox) jaxbUnmarshaller.unmarshal(reader);
			// Question que= (Question) jaxbUnmarshaller.unmarshal(file);

			
//			System.out.println("TEXT0: " + msgbox.getMsgText());
			String msgText = msgbox.getMsgText();
			
			msgText = msgText.replaceAll("#SOH#", "\u0001");
			msgText = msgText.replaceAll("#STX#", "\u0002");
			msgbox.setMsgText(msgText);
			
			String origin = "";
			String msgcc = "";
			String msgbcc = "";
			String msgto = "";
			if(msgbox.getMsgboxrecipientlist()!=null) {
				List<Msgboxrecipient> recipientList = msgbox.getMsgboxrecipientlist().getMsgboxrecipients();
				msgbox.setMsgboxrecipients(recipientList);
				for (Msgboxrecipient msgboxrecipient : recipientList){
					msgboxrecipient.setMsgbox(msgbox);
					switch (msgboxrecipient.getRecipienttype()) {
					case "PRIMARY":
						msgto = msgto + " " + msgboxrecipient.getAliasname();
						break;
					case "CC":
						msgcc = msgcc + " " + msgboxrecipient.getAliasname();
						break;
					case "BCC":
						msgbcc = msgbcc + " " + msgboxrecipient.getAliasname();
						break;
					case "ORIGINATOR":
						origin = origin + " " + msgboxrecipient.getAliasname();
						break;
					default:
						break;
					}
				}
					
				msgbox.setMsgTo(msgto.trim());
				msgbox.setMsgCc(msgcc.trim());
				msgbox.setMsgBcc(msgbcc.trim());
				if(origin.length() > 0) {
					msgbox.setMsgOrgn2(origin.trim());
				}
			}

			if(msgbox.getMsgboxattachmentList()!=null){
				List<Msgboxattachment> attachmentList = msgbox.getMsgboxattachmentList().getMsgboxattachments();
				msgbox.setMsgboxattachments(attachmentList);
				for (Msgboxattachment msgboxattachment : attachmentList)
					msgboxattachment.setMsgbox(msgbox);
			}

			return msgbox;
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		}
//		return null;
	}
	
	
	public static String checkMessageType(String msgtxt) {
    	String msgtype = "";
        if (msgtxt.indexOf("(FPL-") >= 0) msgtype = "FPL";
        else if (msgtxt.indexOf("(DEP-") >= 0) msgtype = "DEP";
        else if (msgtxt.indexOf("(ARR-") >= 0) msgtype = "ARR";
        else if (msgtxt.indexOf("(CHG-") >= 0) msgtype = "CHG";
        else if (msgtxt.indexOf("(DLA-") >= 0) msgtype = "DLA";
        else if (msgtxt.indexOf("(CNL-") >= 0) msgtype = "CNL";
        else if (msgtxt.indexOf(" NOTAM") >= 0) msgtype = "NOTAM";

        if (msgtype == "")
        {
//            String pattern0 = "([A-Z]{2})[A-Z]{2}[0-9]{2} [A-Z]{4} [0-9]{6}";
            String pattern = "(\\D{2})(\\D{2}\\d{2}\\s{1}\\D{4}\\s{1}\\d{6})";
            
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(msgtxt);
            if (m.find())
            {
                String type = m.group(1);

                if (type.equals("SA") || type.equals("SP") || type.equals("WS") || type.equals("FT") || type.equals("FC") || type.equals("FK") || 
                		type.equals("FV") || type.equals("WV") || type.equals("WC") || type.equals("WO") || type.equals("UA") || type.equals("NO")){
                	msgtype = type;
                }
            }

            if (msgtype == "")
            {
                if (msgtxt.indexOf("SVC ") > -1)
                    msgtype = "SVC";
            }
            if(msgtype == "") msgtype = "Not Match";
        }
		return msgtype;
        
    }
}
