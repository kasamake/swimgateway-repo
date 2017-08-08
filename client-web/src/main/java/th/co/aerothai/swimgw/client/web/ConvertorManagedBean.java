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
import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;
import th.co.aerothai.swimgw.services.api.IConvertorBean;
import th.co.aerothai.swimgw.services.api.IMsgboxBean;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.x400.SendUtils;

@ManagedBean(name = "convertorManagedBean")
@SessionScoped
public class ConvertorManagedBean {

	String xmlString;
	int msgId; 
	String filename;
	String jsonString;
	@EJB
	IConvertorBean convertorBean;

	@EJB
	IMsgboxBean msgboxBean;
	
	@PostConstruct
	public void setup() {
		System.out.println("Setting up after creating the JSF managed bean.");
	}

	public String convertToXmlString() {
		this.xmlString = this.convertorBean.convertMsgboxToXml(msgId);

		return "convertor.xhtml";
	}
	
	public String convertToXmlFile() {
		this.xmlString = this.convertorBean.convertMsgboxToFile(msgId);

		return "convertor.xhtml";
	}
	
	public String convertXMLtoMsgbox() {
		Msgbox msgbox = this.convertorBean.convertXMLFiletoMsgbox(filename);
		msgboxBean.addMsgbox(msgbox);
		return "convertor.xhtml";
	}

//	public String convertToJsonString() {
//		this.jsonString = this.convertorBean.convertMsgboxToJson(msgId);
//
//		return "convertor.xhtml";
//	}
	@PreDestroy
	public void cleanUp() {
		System.out.println("Cleaning up before destroying the JSF managed bean.");

	}

	public String getXmlString() {
		return xmlString;
	}
 
	public void setXmlString(String xmlString) {
		this.xmlString = xmlString;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}


}
