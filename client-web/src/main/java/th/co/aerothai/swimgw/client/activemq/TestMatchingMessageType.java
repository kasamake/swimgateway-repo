package th.co.aerothai.swimgw.client.activemq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMatchingMessageType {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String testMsg = "(CNL-AIQ320-IS -A320/M-SFDGHIRW/SD -WMKK0825 -N0463F360 AGOSA1N AGOSA A457 VPG/N0465F350 A457 VAS M769 RIGTO/N0463F360 M769 SURMA -VTBD0157 VTBU VTBS -EET/VTBB0043 REG/HSABY SEL/CJHQ COM/L B1 NAV/RNAV10 RNP10 A1 RNAV5 B1 RNAV2 C1 RNAV1 D1 RNP4 L1 RNP1 O1 RNP APCH BARO VNAV S2 ODE/880459 RMK/TCAS II EQUIPPED FDMC CONVERT DOF/17060) ";
		String testMsg = "SAAE31 VTBB 080000 METAR VTBD 080000Z 03004KT 8000 FEW030 SCT090 25/19 Q1011 NOSIG= METAR VTBS 080000Z 35002KT 9999 FEW030 BKN100 25/18 Q1011 NOSIG= METAR VTBU 080000Z 01004KT 4000 BR FEW020 SCT040 25/18 Q1010 NOSIG= METAR VTCC 080000Z NIL= METAR VTSG 080000Z NIL= METAR VTSP 080000Z NIL= METAR VTSS 080000Z NIL= ";
		String msgType = checkMessageType(testMsg);
		System.out.println("MsgType: "+msgType);
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
//        	String pattern = "(\\D{2})(\\D{2})";
            
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(msgtxt);
            
            
//            Match m = Regex.Match(msgtxt, pattern);
            if (m.find())
            {
                String type = m.group(1);
//                System.out.println("Group1: "+m.group(0));
                System.out.println("Group1: "+m.group(1));
                System.out.println("Group2: "+m.group(2));
                if (type.equals("SA") || type.equals("SP") || type.equals("WS") || type.equals("FT") || type.equals("FC") || type.equals("FK") || 
                		type.equals("FV") || type.equals("WV") || type.equals("WC") || type.equals("WO") || type.equals("UA")){
                	msgtype = type;
                	System.out.println(type);
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
