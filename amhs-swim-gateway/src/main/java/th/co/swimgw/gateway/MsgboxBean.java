package th.co.swimgw.gateway;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;


@ManagedBean
@ViewScoped
public class MsgboxBean {
	private List<Msgbox> msgboxs;
 

    public List<Msgbox> getMsgboxs() {
		return msgboxs;
	}


	@PostConstruct
    public void setup()  {
		List<Msgbox> msgboxs = new ArrayList<Msgbox>();
 
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
//	    msgBox.setAtsEncodeAtt(X400_att.X400_T_IA5TEXT);
//	    
//        Customer customer1 = new Customer();
//        customer1.setFirstName("John");
//        customer1.setLastName("Doe");
//        customer1.setCustomerId(123456);
// 
//        customers.add(customer1);
// 
//        Customer customer2 = new Customer();
//        customer2.setFirstName("Adam");
//        customer2.setLastName("Scott");
//        customer2.setCustomerId(98765);
// 
//        customers.add(customer2);
// 
//        Customer customer3 = new Customer();
//        customer3.setFirstName("Jane");
//        customer3.setLastName("Doe");
//        customer3.setCustomerId(65432);
 
        msgboxs.add(msgBox);
        this.msgboxs = msgboxs;
    }
}
