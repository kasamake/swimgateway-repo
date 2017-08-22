package th.co.aerothai.swimgw.services.amhs;

import com.isode.x400api.ACP127Resp;
import com.isode.x400api.ALI;
import com.isode.x400api.BodyPart;
import com.isode.x400api.DistField;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Message;
import com.isode.x400api.OtherRecip;
import com.isode.x400api.PSS;
import com.isode.x400api.Recip;
import com.isode.x400api.Session;
import com.isode.x400api.X400_att;
import com.isode.x400api.X400ms;

/**
 ** Set of functions which submit a message into a P7 messge store.
 */

//import com.isode.x400api.*;
//import com.isode.x400api.test.config;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.File;

public class SendUtils {

	/**
	 * Submit a message into via the Message Store.
	 */
	public static void send_msg(String[] args) {
		int type = 0;
		int status;

		// Use the OR address used in the P7 bind as the recipient
		String p7_sender = config.p7_bind_yuaa;

		// instantiate a message object, and make it an API object
		// by opening an API session
		Session session_obj = new Session();

		// Open the session using the instance field values
		// if (config.use_opencheck == true) {
		// String ret_psw = config.p7_ret_psw;
		//
		// System.out.println("Open connection using opencheck,"
		// + " config.p7_bind_oraddr = " + config.p7_bind_oraddr
		// + " config.p7_bind_dn = " + config.p7_bind_dn
		// + " config.p7_credentials = " + config.p7_credentials
		// + " config.p7_pa = " + config.p7_pa
		// + " ret_psw = " + ret_psw
		// );
		//
		// status = com.isode.x400api.X400ms.x400_ms_opencheck(type,
		// config.p7_bind_oraddr, config.p7_bind_dn,
		// config.p7_credentials, config.p7_pa, ret_psw, session_obj);
		// } else {
		System.out.println("Open connection using open," + " config.p7_bind_oraddr = " + config.p7_bind_yuaa
				+ " config.p7_bind_dn = " + config.p7_bind_dn + " config.p7_credentials = " + config.p7_credentials
				+ " config.p7_pa = " + config.p7_pa);
		status = com.isode.x400api.X400ms.x400_ms_open(type, config.p7_bind_yuaa, config.p7_bind_dn,
				config.p7_credentials, config.p7_pa, session_obj);
		// }
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_open failed " + status);
			return;
		}
		System.out.println("Opened MS session successfully");

		// turn on all logging for this session
		status = com.isode.x400api.X400ms.x400_ms_setstrdefault(session_obj, X400_att.X400_S_LOG_CONFIGURATION_FILE,
				"x400api.xml", -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_setstrdefault failed " + status);
			return;
		}

		// instantiate a message object, and make it an API object
		MSMessage msmessage_obj = new MSMessage();
		// if (config.building_simple_message) {
		// status = build_simple_msmsg(session_obj, msmessage_obj, p7_sender);
		// } else {
		status = build_msmsg(session_obj, msmessage_obj, p7_sender);
		// }
		if (status != X400_att.X400_E_NOERROR) {
			return;
		}

		// Set up a default security env so we can sign messages
		// if (config.building_signed_message) {
		// status = X400msTestSignUtils.setup_default_sec_env(session_obj,
		// config.sec_id, config.sec_id_dn, config.sec_pphr);
		// if (status != X400_att.X400_E_NOERROR) {
		// return;
		// }
		// }

		// message all assembled - submit it
		status = com.isode.x400api.X400ms.x400_ms_msgsend(msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgsend failed " + status);
			return;
		}
		System.out.println("Submitted message successfully");

		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return;
		}
		System.out.println("Closed MS Session successfully\n");
		return;
	}

	/**
	 * Build a message to submit into the Message Store.
	 */
	private static int build_simple_msmsg(Session session_obj, MSMessage msmessage_obj, String orig) {
		return X400_att.X400_E_NYI;
	}

	/**
	 * Build a message to submit into the Message Store.
	 */
	private static int build_msmsg(Session session_obj, MSMessage msmessage_obj, String orig) {
		int status;
		int rno = 1;
		String unknown_recip = "/CN=invalid/O=GatewayMTA/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

		// String swim_recip =
		// "/CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/";

		// make message object an API object
		status = com.isode.x400api.X400ms.x400_ms_msgnew(session_obj, X400_att.X400_MSG_MESSAGE, msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		System.out.println("Building message envelope");
		status = build_ms_env(msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		System.out.println("Building message content");
		status = build_ms_content(session_obj, orig, msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		// if (config.ms_use_p772 == true) {
		// System.out.println("Adding P772 attributes");
		// status = build_p772(msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgnew failed " + status);
		// return status;
		// }
		// }

		System.out.println("Adding originator");
		// envelope
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_OR_ADDRESS, orig, -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}
		// header
		status = add_ms_recip(msmessage_obj, orig, X400_att.X400_ORIGINATOR, rno++);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		System.out.println("Adding recipients");

		String recipient_yuaa = config.p7_bind_yuaa;
		// add recip to msg - use originator as recip 1
		status = add_ms_recip(msmessage_obj, recipient_yuaa, X400_att.X400_RECIP_STANDARD, rno++);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		// add unknown (unroutable) recip to message
		// status = add_ms_recip(msmessage_obj, unknown_recip,
		// X400_att.X400_RECIP_STANDARD, rno++);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgnew failed " + status);
		// return status;
		// }
		//
		// // Add DL exempted recipents
		// status = add_ms_recip(msmessage_obj, config.mt_msg_dl_exempted_addr,
		// X400_att.X400_DL_EXEMPTED_RECIP, rno++);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_mt_msgnew failed " + status);
		// return status;
		// }
		//
		//
		//
		// // cause the message to be signed
		// if (config.building_signed_message) {
		// status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(
		// msmessage_obj, X400_att.X400_B_SEC_GEN_MOAC, 1);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }
		// }

		return status;
	}

	/**
	 * Build a message to submit into the Message Store.
	 */
	private static int build_msmsg(Session session_obj, MSMessage msmessage_obj, String orig, Msgbox msgBox) {
		int status;
		int rno = 1;
		// String unknown_recip
		// = "/CN=invalid/O=GatewayMTA/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";
		//
		// String swim_recip =
		// "/CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/";

		// make message object an API object
		status = com.isode.x400api.X400ms.x400_ms_msgnew(session_obj, X400_att.X400_MSG_MESSAGE, msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		System.out.println("Building message envelope");
		status = build_ms_env(msmessage_obj, msgBox);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		System.out.println("Building message content");
		status = build_ms_content(session_obj, orig, msmessage_obj, msgBox);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		// if (config.ms_use_p772 == true) {
		// System.out.println("Adding P772 attributes");
		// status = build_p772(msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgnew failed " + status);
		// return status;
		// }
		// }

		System.out.println("Adding originator");
		// envelope
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_OR_ADDRESS, orig, -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}
		// header
		status = add_ms_recip(msmessage_obj, orig, X400_att.X400_ORIGINATOR, rno++);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgnew failed " + status);
			return status;
		}

		for (Msgboxrecipient msgBoxRecipient : msgBox.getMsgboxrecipients()) {
			System.out.println("Adding recipients");
			switch (msgBoxRecipient.getRecipienttype()) {
			case "PRIMARY":
				status = add_ms_recip(msmessage_obj, msgBoxRecipient.getOraddress(), X400_att.X400_RECIP_STANDARD, rno++);
				break;
			case "CC":
				status = add_ms_recip(msmessage_obj, msgBoxRecipient.getOraddress(), X400_att.X400_RECIP_CC, rno++);
				status = add_ms_recip(msmessage_obj, msgBoxRecipient.getOraddress(), X400_att.X400_RECIP_ENVELOPE, rno++);
				break;
			case "BCC":
				status = add_ms_recip(msmessage_obj, msgBoxRecipient.getOraddress(), X400_att.X400_RECIP_BCC, rno++);
				status = add_ms_recip(msmessage_obj, msgBoxRecipient.getOraddress(), X400_att.X400_RECIP_ENVELOPE, rno++);
				break;
			// case "ORIGINATOR":
			// origin = origin + " " + msgBoxRecipient.getAliasname();
			// break;
			default:
				break;
			}
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msgnew failed " + status);
				return status;
			}
		}

		// add unknown (unroutable) recip to message
		// status = add_ms_recip(msmessage_obj, unknown_recip,
		// X400_att.X400_RECIP_STANDARD, rno++);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgnew failed " + status);
		// return status;
		// }
		//
		// // Add DL exempted recipents
		// status = add_ms_recip(msmessage_obj, config.mt_msg_dl_exempted_addr,
		// X400_att.X400_DL_EXEMPTED_RECIP, rno++);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_mt_msgnew failed " + status);
		// return status;
		// }
		//
		//
		//
		// // cause the message to be signed
		// if (config.building_signed_message) {
		// status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(
		// msmessage_obj, X400_att.X400_B_SEC_GEN_MOAC, 1);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }
		// }

		return status;
	}

	/**
	 * Add a recipient into a message for submission into the Message Store.
	 */
	private static int add_ms_recip(MSMessage msmessage_obj, String recip, int type, int rno) {
		int status;
		// value to use as the alternate recipient
		// String alt_recip = "/CN=lppt2/OU=lppt/O="
		// + config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";
		// String phone_num = "02087830203";

		// instantiate a recip object, and make it an API object
		Recip recip_obj = new Recip();
		status = com.isode.x400api.X400ms.x400_ms_recipnew(msmessage_obj, type, recip_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipnew failed " + status);
			return status;
		}

		// add some X.400 envelope attributes into the recipient

		// use length of -1 to indicate NULL terminated
		status = com.isode.x400api.X400ms.x400_ms_recipaddstrparam(recip_obj, X400_att.X400_S_OR_ADDRESS, recip, -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddstrparam failed " + status);
			return status;
		}

		/*!< X.400 Originating MTA report request:
	     non-delivery-report 1
	     report              2
	     audited-report      3
	     <br>This value should be set only by Gateway applications (like an MTCU)</br>
	 */
		status = com.isode.x400api.X400ms.x400_ms_recipaddintparam(recip_obj, X400_att.X400_N_MTA_REPORT_REQUEST, 2);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddintparam failed " + status);
			return status;
		}

		/*!< X.400 Originator report request:
	     no report           0
	     non-delivery-report 1
	     report              2
	    <br>This value should be set by user agent applications and Gateway applications<br/>
	*/
		status = com.isode.x400api.X400ms.x400_ms_recipaddintparam(recip_obj, X400_att.X400_N_REPORT_REQUEST, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddintparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_recipaddintparam(recip_obj, X400_att.X400_N_ORIGINAL_RECIPIENT_NUMBER,
				rno);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddintparam failed " + status);
			return status;
		}

		// status = com.isode.x400api.X400ms.x400_ms_recipaddstrparam(recip_obj,
		// X400_att.X400_S_ORIGINATOR_REQUESTED_ALTERNATE_RECIPIENT,
		// alt_recip, -1);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_recipaddstrparam failed " + status);
		// return status;
		// }

		// add some X.400 recip attributes into the recipient
		status = com.isode.x400api.X400ms.x400_ms_recipaddintparam(recip_obj, X400_att.X400_N_NOTIFICATION_REQUEST, 2); // 7
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddstrparam failed " + status);
			return status;
		}
		status = com.isode.x400api.X400ms.x400_ms_recipaddintparam(recip_obj, X400_att.X400_N_REPLY_REQUESTED, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddstrparam failed " + status);
			return status;
		}
		status = com.isode.x400api.X400ms.x400_ms_recipaddstrparam(recip_obj, X400_att.X400_S_FREE_FORM_NAME, recip,
				recip.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_recipaddstrparam failed " + status);
			return status;
		}

		// status = com.isode.x400api.X400ms.x400_ms_recipaddstrparam(recip_obj,
		// X400_att.X400_S_TELEPHONE_NUMBER, phone_num, phone_num.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_recipaddstrparam failed " + status);
		// return status;
		// }
		//
		// if (config.mt_use_p772 == true) {
		// /* Add P772 per-recipient extensions */
		// System.out.println("Adding STANAG 4406 A2.1 ACP127 notification
		// request");
		//
		// /* STANAG 4406 A2.1 ACP127 notification request */
		// status = com.isode.x400api.X400.x400_recipaddintparam(recip_obj,
		// X400_att.X400_N_ACP127_NOTI_TYPE,
		// X400_att.X400_ACP127_NOTI_TYPE_NEG);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_recipaddintparam X400_N_ACP127_NOTI_TYPE
		// failed " + status);
		// return status;
		// }
		//
		// /* STANAG 4406 A2.1 ACP127 notification request */
		// status = com.isode.x400api.X400.x400_recipaddintparam(recip_obj,
		// X400_att.X400_N_ACP127_NOTI_TYPE,
		// X400_att.X400_ACP127_NOTI_TYPE_POS);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_recipaddintparam X400_N_ACP127_NOTI_TYPE
		// failed " + status);
		// return status;
		// }
		//
		// /* STANAG 4406 A2.1 ACP127 notification request */
		// status = com.isode.x400api.X400.x400_recipaddintparam(recip_obj,
		// X400_att.X400_N_ACP127_NOTI_TYPE,
		// X400_att.X400_ACP127_NOTI_TYPE_TRANS);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_recipaddintparam X400_N_ACP127_NOTI_TYPE
		// failed " + status);
		// return status;
		// }
		//
		// }

		System.out.println("Added recipient " + recip + "to MS Message");
		return status;
	}

	/**
	 * Build the envelope for a message for submission into the Message Store.
	 */
	private static int build_ms_env(MSMessage msmessage_obj) {
		int status;
		// String hostname = "attlee";
		// String orig_ret_addr = "/CN=origretr/OU=lppt/O="
		// + config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

		String orig_ret_addr = config.p7_bind_dn;
		// String msg_id = "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/;"
		// + config.hostname + ".2810401-030924.140212";
		String content_id = "030924.140219";
		 String latest_del_time = "170927120000Z";

		// X400_N_CONTENT_LENGTH is probe only

		// Priority: 0 - normal, 1 - non-urgent, 2 - urgent
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_PRIORITY, 2);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Disclosure of recipients: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_DISCLOSURE, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Implicit conversion prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Alternate recipient allowed: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Content return request: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_CONTENT_RETURN_REQUEST, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Recipient reassignment prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Distribution List expansion prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_DL_EXPANSION_PROHIBITED, 1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Conversion with loss prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Message Identifier. In RFC 2156 String form
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_MESSAGE_IDENTIFIER, msg_id, msg_id.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }

		// Content Identifier
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_CONTENT_IDENTIFIER,
				content_id, content_id.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

//		 Latest Delivery Time: UTCTime format YYMMDDHHMMSS<zone>
		 status =
		 com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		 X400_att.X400_S_LATEST_DELIVERY_TIME, latest_del_time,
		 latest_del_time.length());
		 if (status != X400_att.X400_E_NOERROR) {
		 System.out.println("x400_ms_msgaddintparam failed " + status);
		 return status;
		 }

		// Originator Return Address (X.400 String format)
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
				X400_att.X400_S_ORIGINATOR_RETURN_ADDRESS, orig_ret_addr, orig_ret_addr.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}
		return status;

	}

	/**
	 * Build the envelope for a message for submission into the Message Store.
	 */
	private static int build_ms_env(MSMessage msmessage_obj, Msgbox msgbox) {
		int status;
		// String hostname = "attlee";
		// String orig_ret_addr = "/CN=origretr/OU=lppt/O="
		// + config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

		String orig_ret_addr = config.p7_bind_dn;
		// String msg_id = "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/;"
		// + config.hostname + ".2810401-030924.140212";
		
//		String content_id = "030924.140219";
//		 String latest_del_time = "170927120000Z";

//		String content_id = msgbox.getpContIdt();
//		String latest_del_time = msgbox.getpLatestdelivery();
		// X400_N_CONTENT_LENGTH is probe only

		// Priority: 0 - normal, 1 - non-urgent, 2 - urgent
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_PRIORITY, Integer.parseInt(msgbox.getMsgPriority()));
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Disclosure of recipients: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_DISCLOSURE, (msgbox.getPRcpDisc()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Implicit conversion prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED, (msgbox.getPImpConvPhb()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Alternate recipient allowed: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_ALTERNATE_RECIPIENT_ALLOWED, (msgbox.getPAltRcpAllow()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Content return request: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_CONTENT_RETURN_REQUEST, (msgbox.getPContReturn()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Recipient reassignment prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED, (msgbox.getPRcpRasgPhb()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Distribution List expansion prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_DL_EXPANSION_PROHIBITED, (msgbox.getPDlExpPhb()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Conversion with loss prohibited: 0 - no, 1 - yes
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
				X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED, (msgbox.getPConvLossPhb()) ? 1 : 0);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// Message Identifier. In RFC 2156 String form
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_MESSAGE_IDENTIFIER, msg_id, msg_id.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }

		// Content Identifier
		if(msgbox.getpContIdt()!=null){
			status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_CONTENT_IDENTIFIER,
					msgbox.getpContIdt(), msgbox.getpContIdt().length());
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msgaddintparam failed " + status);
				return status;
			}
		}
		

//		 Latest Delivery Time: UTCTime format YYMMDDHHMMSS<zone>
		if(msgbox.getpLatestdelivery()!=null){
			 status =
					 com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
					 X400_att.X400_S_LATEST_DELIVERY_TIME, msgbox.getpLatestdelivery(),
					 msgbox.getpLatestdelivery().length());
					 if (status != X400_att.X400_E_NOERROR) {
					 System.out.println("x400_ms_msgaddintparam failed " + status);
					 return status;
					 }
		}


		// Originator Return Address (X.400 String format)
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
				X400_att.X400_S_ORIGINATOR_RETURN_ADDRESS, orig_ret_addr, orig_ret_addr.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed 3" + status);
			return status;
		}
		return status;

	}
	/**
	 * Build the content for a message for submission into the Message Store.
	 */
	private static int build_ms_content(Session session_obj, String oraddress, MSMessage msmessage_obj) {
		int status;
		int num_atts = 0;
		String subject = "Test message from Java3";
		String content_ia5 = "IA5 content from Java\r\nLine 1\r\nLine 2";
		String content_ia5_att = "IA5 att content from Java\r\nLine 1\r\nLine 2";
		String content_8859_1 = "8859_1 content from Java\r\nLine 1\r\nLine 2\u00a3\u00a3";
		String content_8859_2 = "8859_2 content from Java\r\nLine 1\r\nLine 2\u00a3\u00a3";
		String content_bin = "binary content from Java\r\nLine 1\r\nLine 2\u00a3\u00a3";
		String content_ftbp = "ftbp content from Java\r\nLine 1\r\nLine 2\u00a3\u00a3";
		byte[] emptybinarydata = new byte[0];
		/* test binary data: note the NULs near the end */
		byte[] binarydata = new byte[] { 98, 105, 110, 97, 114, 121, 32, 99, 111, 110, 116, 101, 110, 116, 32, 102, 114,
				111, 109, 32, 74, 97, 118, 97, 13, 10, 76, 105, 110, 101, 32, 49, 13, 10, 76, 105, 110, 101, 32, 50, 0,
				97 };

		String ipm_id = "1064400656.24922*";
		String ipm_rep_id = "1064400656.24923*";
		String ipm_obs_id = "1064400656.24924*";
		String ipm_rel_id = "1064400656.24925*";
		String orig_ref = "orig-ref-val";
		String def_utc = "050924120000";

		int importance = 2;
		int sensitivity = 3;
		int autoforwarded = 1;

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_IPM_IDENTIFIER, ipm_id,
				ipm_id.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_SUBJECT, subject, -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_REPLIED_TO_IDENTIFIER,
				ipm_rep_id, ipm_rep_id.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_OBSOLETED_IPMS,
				ipm_obs_id, ipm_obs_id.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_RELATED_IPMS,
				ipm_rel_id, ipm_rel_id.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_EXPIRY_TIME, def_utc,
				def_utc.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_REPLY_TIME, def_utc,
				def_utc.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_IMPORTANCE, importance);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_SENSITIVITY,
				sensitivity);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_AUTOFORWARDED,
				autoforwarded);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		// now add the attachments/bodyparts

		// Add an IA5 attachment using AddStrParam
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_T_IA5TEXT, content_ia5,
				content_ia5.length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}
		num_atts++;

		// Add an IA5 attachment
		status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(msmessage_obj, X400_att.X400_T_IA5TEXT,
				content_ia5_att, content_ia5_att.length(), emptybinarydata);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddattachment failed " + status);
			return status;
		}
		num_atts++;

		// Add an 8859-1 attachment
		status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(msmessage_obj, X400_att.X400_T_ISO8859_1,
				content_8859_1, content_8859_1.length(), emptybinarydata);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddattachment failed " + status);
			return status;
		}
		num_atts++;

		// Add an 8859-2 attachment
		status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(msmessage_obj, X400_att.X400_T_ISO8859_1,
				content_8859_2, content_8859_2.length(), emptybinarydata);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddattachment failed " + status);
			return status;
		}
		num_atts++;

		// add a forwarded msg attachment
		// instantiate a message object putting it into an API object
		// Message fwd_message_obj = new Message();
		// String fwd_recip_oraddress = config.fwd_recip_oraddress;
		// String fwd_recip_oraddress = config.p7_bind_oraddr;

		// "/CN=lppt2/OU=lppt/O="
		// + config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

		// System.out.println("Building message for message bodypart");
		// status = X400BuildFwdMsg.build_fwd_msg(fwd_message_obj,
		// fwd_recip_oraddress);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("build_fwd_msg failed " + status);
		// return status;
		// }

		// add the X400Message as a bodypart to the X400msMessage
		// status = com.isode.x400api.X400ms.x400_ms_msgaddmessagebody(
		// msmessage_obj, fwd_message_obj);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_addmessagebody failed " + status);
		// return status;
		// }
		// num_atts++;

		// Add an bin attachment
		status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(msmessage_obj, X400_att.X400_T_BINARY, content_bin,
				content_bin.length(), binarydata);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddattachment failed (bin)" + status);
			return status;
		}
		num_atts++;

		//
		// /* Now add P772 bodyparts */
		// if (config.ms_use_p772 == true) {
		// status = add_adatp3 (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_corrections (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_acp127data (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_fwd_enc (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		//
		// status = add_fwd_MM (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// }
		//
		// status = add_x420_fwd_content (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;

		// record number of attachments
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_NUM_ATTACHMENTS,
				num_atts);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		/*************
		 * The ACP127 Notification Response is only present in a Military
		 * Notification ******** status = add_acp127resp(msmessage_obj); if
		 * (status != X400_att.X400_E_NOERROR) {
		 * System.out.println("add_acp127resp " + status); return status; }
		 ******************/

		return status;

	}

	/**
	 * Build the content for a message for submission into the Message Store.
	 */
	private static int build_ms_content(Session session_obj, String oraddress, MSMessage msmessage_obj, Msgbox msgBox) {
		int status;

		int num_atts = 0;

//		String ipm_id = "1064400656.24922*";
		String ipm_id = msgBox.getIpmId();

		// int num_atts = 0;
		// String subject = "Test message from Java3";
		// String content_ia5= "IA5 content from Java\r\nLine 1\r\nLine 2";
		// String content_ia5_att = "IA5 att content from Java\r\nLine 1\r\nLine
		// 2";
		// String content_8859_1 = "8859_1 content from Java\r\nLine 1\r\nLine
		// 2\u00a3\u00a3";
		// String content_8859_2 = "8859_2 content from Java\r\nLine 1\r\nLine
		// 2\u00a3\u00a3";
		// String content_bin = "binary content from Java\r\nLine 1\r\nLine
		// 2\u00a3\u00a3";
		// String content_ftbp = "ftbp content from Java\r\nLine 1\r\nLine
		// 2\u00a3\u00a3";
		// byte[] emptybinarydata = new byte[0];
		// /* test binary data: note the NULs near the end */
		// byte[] binarydata = new byte[]
		// {98,105,110,97,114,121,32,99,111,110,116,101,110,116,32,102,114,111,109,32,74,97,118,97,13,10,76,105,110,101,32,49,13,10,76,105,110,101,32,50,0,97};
		//
		// String ipm_id = "1064400656.24922*";
		// String ipm_rep_id = "1064400656.24923*";
		// String ipm_obs_id = "1064400656.24924*";
		// String ipm_rel_id = "1064400656.24925*";
		// String orig_ref = "orig-ref-val";
		// String def_utc = "050924120000";
		//
		// int importance = 2;
		// int sensitivity = 3;
		// int autoforwarded = 1;

//		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_IPM_IDENTIFIER, ipm_id,
//				ipm_id.length());
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("x400_ms_msgaddstrparam failed " + status);
//			return status;
//		}

//		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_SUBJECT,
//				msgBox.getMsgSubject(), -1);
		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_S_SUBJECT,
				msgBox.getMsgSubject(), msgBox.getMsgSubject().length());
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}

		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_REPLIED_TO_IDENTIFIER, ipm_rep_id,
		// ipm_rep_id.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddstrparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_OBSOLETED_IPMS, ipm_obs_id,
		// ipm_obs_id.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddstrparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_RELATED_IPMS, ipm_rel_id, ipm_rel_id.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddstrparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_EXPIRY_TIME, def_utc, def_utc.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddstrparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj,
		// X400_att.X400_S_REPLY_TIME, def_utc, def_utc.length());
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddstrparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
		// X400_att.X400_N_IMPORTANCE, importance);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
		// X400_att.X400_N_SENSITIVITY, sensitivity);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }
		//
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj,
		// X400_att.X400_N_AUTOFORWARDED, autoforwarded);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddintparam failed " + status);
		// return status;
		// }

		// now add the attachments/bodyparts


		// Add an IA5 attachment using AddStrParam
//		new String(msgBox.getMsgText().getBytes(), "ASCII");

		status = com.isode.x400api.X400ms.x400_ms_msgaddstrparam(msmessage_obj, X400_att.X400_T_IA5TEXT,
				msgBox.getMsgText(), msgBox.getMsgText().length());

		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddstrparam failed " + status);
			return status;
		}
		num_atts++;

		//
		// // Add an IA5 attachment
		// status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(
		// msmessage_obj, X400_att.X400_T_IA5TEXT, content_ia5_att,
		// content_ia5_att.length(), emptybinarydata);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddattachment failed " + status);
		// return status;
		// }
		// num_atts++;
		//
		// // Add an 8859-1 attachment
		// status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(
		// msmessage_obj, X400_att.X400_T_ISO8859_1, content_8859_1,
		// content_8859_1.length(), emptybinarydata);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddattachment failed " + status);
		// return status;
		// }
		// num_atts++;
		//
		// // Add an 8859-2 attachment
		// status =
		// com.isode.x400api.X400ms.x400_ms_msgaddattachment(msmessage_obj,
		// X400_att.X400_T_ISO8859_1, content_8859_2, content_8859_2.length(),
		// emptybinarydata);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddattachment failed " + status);
		// return status;
		// }
		// num_atts++;
		//
		// add a forwarded msg attachment
		// instantiate a message object putting it into an API object
		// Message fwd_message_obj = new Message();
		// String fwd_recip_oraddress = config.fwd_recip_oraddress;
		// String fwd_recip_oraddress = config.p7_bind_oraddr;

		// "/CN=lppt2/OU=lppt/O="
		// + config.hostname + "/PRMD=TestPRMD/ADMD=TestADMD/C=GB/";

		// System.out.println("Building message for message bodypart");
		// status = X400BuildFwdMsg.build_fwd_msg(fwd_message_obj,
		// fwd_recip_oraddress);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("build_fwd_msg failed " + status);
		// return status;
		// }

		// add the X400Message as a bodypart to the X400msMessage
		// status = com.isode.x400api.X400ms.x400_ms_msgaddmessagebody(
		// msmessage_obj, fwd_message_obj);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_addmessagebody failed " + status);
		// return status;
		// }
		// num_atts++;
		//
		for (Msgboxattachment attachment : msgBox.getMsgboxattachments()) {
			// Add FTBP attachment

			// status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(
			// msmessage_obj, X400_att.X400_T_FTBP, attachment.getFilename(),
			// attachment.getFilesize(), attachment.getBfile());
			// if (status != X400_att.X400_E_NOERROR) {
			// System.out.println("x400_ms_msgaddattachment failed (FTBP)" +
			// status);
			// return status;
			// }

			BodyPart bodypart_obj = new BodyPart();
			status = com.isode.x400api.X400.x400_bodypartnew(X400_att.X400_T_FTBP, bodypart_obj);
			if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_bodypartnew failed " + status);
		}

			status = com.isode.x400api.X400.x400_bodypartaddstrparam(bodypart_obj, X400_att.X400_S_FTBP_FILENAME,
					attachment.getFilename(), attachment.getFilename().length());
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartaddstrparam failed " + status);
			}
//			status = com.isode.x400api.X400.x400_bodypartaddintparam(bodypart_obj, X400_att.X400_N_BODY_TYPE,
//					X400_att.X400_T_FTBP);
//			if (status != X400_att.X400_E_NOERROR) {
//				System.out.println("x400_bodypartaddintparam failed (FTBP)" + status);
//			}


			status = com.isode.x400api.X400.x400_bodypartaddbyteparam(bodypart_obj, X400_att.X400_S_BODY_DATA,
					attachment.getBfile(), attachment.getFilename().length());
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartaddbyteparam failed " + status);
			}
			
			status = com.isode.x400api.X400ms.x400_ms_msgaddbodypart(msmessage_obj, bodypart_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msgaddbodypart failed " + status);
			}
			num_atts++;

		}

		// BodyPart bodypart_obj = new BodyPart();
		//

		// status =
		// com.isode.x400api.X400.x400_msgaddmessagebodywtype(msmessage_obj,
		// arg1, arg2)
		// status = com.isode.x400api.X400.x400_bodypartaddbyteparam(arg0, arg1,
		// arg2, arg3)
		// status =
		// com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj,
		// X400_att.X400_S_BODY_DATA, ret_value,
		// binarydata);
		//

		// Add an bin attachment
		// status = com.isode.x400api.X400ms.x400_ms_msgaddattachment(
		// msmessage_obj, X400_att.X400_T_BINARY, content_bin,
		// content_bin.length(), binarydata);
		// if (status != X400_att.X400_E_NOERROR) {
		// System.out.println("x400_ms_msgaddattachment failed (bin)" + status);
		// return status;
		// }
		// num_atts++;
		//
		//
		// /* Now add P772 bodyparts */
		// if (config.ms_use_p772 == true) {
		// status = add_adatp3 (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_corrections (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_acp127data (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// status = add_fwd_enc (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		//
		// status = add_fwd_MM (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;
		//
		// }
		//
		// status = add_x420_fwd_content (msmessage_obj);
		// if (status != X400_att.X400_E_NOERROR)
		// return status;
		// num_atts++;

		// record number of attachments
		status = com.isode.x400api.X400ms.x400_ms_msgaddintparam(msmessage_obj, X400_att.X400_N_NUM_ATTACHMENTS,
				num_atts);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgaddintparam failed " + status);
			return status;
		}

		/*************
		 * The ACP127 Notification Response is only present in a Military
		 * Notification ******** status = add_acp127resp(msmessage_obj); if
		 * (status != X400_att.X400_E_NOERROR) {
		 * System.out.println("add_acp127resp " + status); return status; }
		 ******************/

		return status;

	}

	/**
	 * Submit a message (MsgBox) into via the Message Store.
	 */
	public static int send_msg(String[] args, Msgbox msgBox) {
		int type = 0;
		int status;

		// Use the OR address used in the P7 bind as the recipient
		String p7_sender = config.p7_bind_swim;

		// instantiate a message object, and make it an API object
		// by opening an API session
		Session session_obj = new Session();

		System.out.println("Open connection using open," + " config.p7_bind_oraddr = " + config.p7_bind_swim
				+ " config.p7_bind_dn = " + config.p7_bind_dn + " config.p7_credentials = " + config.p7_credentials
				+ " config.p7_pa = " + config.p7_pa);
		status = com.isode.x400api.X400ms.x400_ms_open(type, config.p7_bind_swim, config.p7_bind_dn,
				config.p7_credentials, config.p7_pa, session_obj);

		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_open failed " + status);
			return status;
		}
		System.out.println("Opened MS session successfully");

		// turn on all logging for this session
		status = com.isode.x400api.X400ms.x400_ms_setstrdefault(session_obj, X400_att.X400_S_LOG_CONFIGURATION_FILE,
				"x400api.xml", -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_setstrdefault failed " + status);
			return status;
		}

		// instantiate a message object, and make it an API object
		MSMessage msmessage_obj = new MSMessage();
		// if (config.building_simple_message) {
		// status = build_simple_msmsg(session_obj, msmessage_obj, p7_sender);
		// } else {
		status = build_msmsg(session_obj, msmessage_obj, p7_sender, msgBox);
		// }
		if (status != X400_att.X400_E_NOERROR) {
			return status;
		}

		// Set up a default security env so we can sign messages
		// if (config.building_signed_message) {
		// status = X400msTestSignUtils.setup_default_sec_env(session_obj,
		// config.sec_id, config.sec_id_dn, config.sec_pphr);
		// if (status != X400_att.X400_E_NOERROR) {
		// return;
		// }
		// }

		// message all assembled - submit it
		status = com.isode.x400api.X400ms.x400_ms_msgsend(msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgsend failed " + status);
			return status;
		}
		System.out.println("Submitted message successfully**");

		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return status;
		}
		System.out.println("Closed MS Session successfully\n");
		return status;
	}

	public static int send_msg(Msgbox msgBox, String or, String dn, String pa, String credential) {
		int type = 0;
		int status;

		// Use the OR address used in the P7 bind as the recipient
		String p7_sender = or;

		// instantiate a message object, and make it an API object
		// by opening an API session
		Session session_obj = new Session();

		System.out.println("Open connection using open," + " config.p7_bind_oraddr = " + or
				+ " config.p7_bind_dn = " + dn + " config.p7_credentials = " + credential
				+ " config.p7_pa = " + pa);
		status = com.isode.x400api.X400ms.x400_ms_open(type, or, dn,
				credential, pa, session_obj);

		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_open failed " + status);
			return status;
		}
		System.out.println("Opened MS session successfully");

		// turn on all logging for this session
		status = com.isode.x400api.X400ms.x400_ms_setstrdefault(session_obj, X400_att.X400_S_LOG_CONFIGURATION_FILE,
				"x400api.xml", -1);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_setstrdefault failed " + status);
			return status;
		}

		// instantiate a message object, and make it an API object
		MSMessage msmessage_obj = new MSMessage();
		// if (config.building_simple_message) {
		// status = build_simple_msmsg(session_obj, msmessage_obj, p7_sender);
		// } else {
		status = build_msmsg(session_obj, msmessage_obj, p7_sender, msgBox);
		// }
		if (status != X400_att.X400_E_NOERROR) {
			return status;
		}

		// Set up a default security env so we can sign messages
		// if (config.building_signed_message) {
		// status = X400msTestSignUtils.setup_default_sec_env(session_obj,
		// config.sec_id, config.sec_id_dn, config.sec_pphr);
		// if (status != X400_att.X400_E_NOERROR) {
		// return;
		// }
		// }

		// message all assembled - submit it
		status = com.isode.x400api.X400ms.x400_ms_msgsend(msmessage_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgsend failed " + status);
			return status;
		}
		System.out.println("Submitted message successfully**");

		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return status;
		}
		System.out.println("Closed MS Session successfully\n");
		return status;
	}
}
