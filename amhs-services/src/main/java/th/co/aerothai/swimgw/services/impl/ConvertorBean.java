package th.co.aerothai.swimgw.services.impl;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxattachments;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.models.Msgboxrecipients;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;

@Stateless
@Local
public class ConvertorBean implements IConvertorBean {
	@PersistenceContext(unitName = "SwimgwPersistenceUnit")
	private EntityManager entityManager;

	public String convertMsgboxToXml(int id) {
		System.out.println("convertMsgboxToXml");
		Msgbox msgbox = this.entityManager.find(Msgbox.class, Integer.valueOf(id));
		
		Msgboxrecipients msgboxrecipients = new Msgboxrecipients();
		msgboxrecipients.setMsgboxrecipients(msgbox.getMsgboxrecipients());
		msgbox.setMsgboxrecipientlist(msgboxrecipients);
		String xml = "";
		// Save customer to XML
		JAXBContext jc;
		
		try {
			jc = JAXBContext.newInstance(Msgbox.class);
			Marshaller marshaller = jc.createMarshaller();
			StringWriter writer = new StringWriter();
			marshaller.marshal(msgbox, writer);
			xml = writer.toString();
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return xml;
	}
	
	public String convertMsgboxToFile(int id) {
		System.out.println("convertMsgboxToXmlFile");
		
		Msgbox msgbox = this.entityManager.find(Msgbox.class, Integer.valueOf(id));
		
		Msgboxrecipients msgboxrecipients = new Msgboxrecipients();
		msgboxrecipients.setMsgboxrecipients(msgbox.getMsgboxrecipients());
		msgbox.setMsgboxrecipientlist(msgboxrecipients);
		
		Msgboxattachments msgboxattachments = new Msgboxattachments();
		msgboxattachments.setMsgboxattachments(msgbox.getMsgboxattachments());
		msgbox.setMsgboxattachmentList(msgboxattachments);
		
//		System.out.println("Attachment size: "+ msgbox.getMsgboxattachments().size()+"  ==  "+msgbox.getMsgboxattachmentList().getMsgboxattachments().size());
		File file = new File("C:\\Users\\Kasama\\file.xml");
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(Msgbox.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(msgbox, file);
			jaxbMarshaller.marshal(msgbox, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Msgbox convertXMLtoMsgbox(String filename) {
	     try {  
	    	   
//	    	 filename = "C:\\Users\\Kasama\\file.xml";
	         File file = new File(filename);  
	         JAXBContext jaxbContext = JAXBContext.newInstance(Msgbox.class);  
	    
	         Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
	         
	         Msgbox msgbox = (Msgbox) jaxbUnmarshaller.unmarshal(file);
//	         Question que= (Question) jaxbUnmarshaller.unmarshal(file);  
	           
	         System.out.println(msgbox.getId()+" "+msgbox.getMsgSubject());  
	         System.out.println("Recipient:");  
	         List<Msgboxrecipient> recipientList=msgbox.getMsgboxrecipientlist().getMsgboxrecipients();
	         msgbox.setMsgboxrecipients(recipientList);
	         for(Msgboxrecipient msgboxrecipient:recipientList)  
	        	 msgboxrecipient.setMsgbox(msgbox);
	         
	         List<Msgboxattachment> attachmentList=msgbox.getMsgboxattachmentList().getMsgboxattachments();
	         msgbox.setMsgboxattachments(attachmentList);
	         for(Msgboxattachment msgboxattachment:attachmentList)  
	        	 msgboxattachment.setMsgbox(msgbox);
	         return msgbox;
	       } catch (JAXBException e) {  
	         e.printStackTrace();  
	       }  
	     return null;
	}
	//
	// public boolean addMsgbox(List<Msgbox> msgboxes)
	// {
	// System.out.println("add msgbox");
	// for (int i = 0; i < msgboxes.size(); i++)
	// {
	// Msgbox msgbox = (Msgbox)msgboxes.get(i);
	// this.entityManager.persist(msgbox);
	// System.out.println("Msgbox ID: " + msgbox.getId());
	// for (Msgboxattachment msgBoxAttachment : msgbox.getMsgboxattachments()) {
	// saveFileAttachment(msgBoxAttachment, msgBoxAttachment.getId().intValue(),
	// msgbox.getId());
	// }
	// }
	// return true;
	// }
	//
	// public int addMsgbox(List<Msgbox> msgboxes, int latestAdd)
	// {
	// return 0;
	// }
	//
	// public Msgbox getMsgbox(int id)
	// {
	// System.out.println("Get Msgbox");
	// return (Msgbox)this.entityManager.find(Msgbox.class,
	// Integer.valueOf(id));
	// }
	//
	// public List<Msgbox> getMsgboxes(String msgTo)
	// {
	// return null;
	// }
	//
	// public int saveFileAttachment(Msgboxattachment msgBoxAttachment, int
	// msgBoxAttachmentId, int msgBoxId)
	// {
	// return 0;
	// }
}