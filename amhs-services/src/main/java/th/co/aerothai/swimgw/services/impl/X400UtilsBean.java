package th.co.aerothai.swimgw.services.impl;

import com.isode.x400api.Session;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;
import java.io.PrintStream;
import java.util.List;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.services.api.IRemoteX400Utils;
import th.co.aerothai.swimgw.services.api.IX400Utils;
import th.co.aerothai.swimgw.services.x400.RcvUtils;
import th.co.aerothai.swimgw.services.x400.SendUtils;

@Stateless
public class X400UtilsBean implements IX400Utils {
	// public int createSession(int type)
	// {
	// System.out.println("Create Session");
	//
	// Session session_obj = new Session();
	// int status = X400ms.x400_ms_open(type,
	// "CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/", "c=TH",
	// "secret", "\"3001\"/URI+0000+URL+itot://172.16.21.134:3001",
	// session_obj);
	// System.out.println("x400_ms_open Status: " + status);
	// return status;
	// }

	public List<Msgbox> retreiveMsg() {
		System.out.println("Retreive Msg");
		return null;
	}

	@Remove
	public void end() {
		System.out.println("X400UtilsBean instance will be removed..");
	}

	public List<Msgbox> getMsgBoxBeanList() {
		// System.out.println("+++++++++getMsgBoxBeanList+++++++++++");
		return RcvUtils.getMsgboxBeanList(null);
	}

	@Override
	public int sendMsgbox(Msgbox msgbox) {
		// TODO Auto-generated method stub
		return SendUtils.send_msg(null, msgbox);
	}

	@Override
	public List<Msgbox> getMsgBoxBeanList(String or, String dn, String pa, String credential) throws Exception{
		// TODO Auto-generated method stub
		return RcvUtils.getMsgboxBeanList(or, dn, pa, credential);
	}
	@Override
	public boolean testConnection(String or, String dn, String pa, String credential) {
		if (RcvUtils.testConnection(or, dn, pa, credential) == X400_att.X400_E_NOERROR) {
			return true;
		}
		return false;
	}
}
