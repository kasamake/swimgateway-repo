package th.co.aerothai.swimgw.services.x400;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.isode.x400api.ALI;
import com.isode.x400api.BodyPart;
import com.isode.x400api.DLExpHist;
import com.isode.x400api.DistField;
import com.isode.x400api.InternalTraceinfo;
import com.isode.x400api.MSListResult;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Message;
import com.isode.x400api.OtherRecip;
import com.isode.x400api.PSS;
import com.isode.x400api.Recip;
import com.isode.x400api.RediHist;
import com.isode.x400api.Session;
import com.isode.x400api.Traceinfo;
import com.isode.x400api.X400_att;

import th.co.aerothai.swimgw.models.Msgbox;
import th.co.aerothai.swimgw.models.Msgboxattachment;
import th.co.aerothai.swimgw.models.Msgboxrecipient;


//import com.isode.x400api.test.X400msTestRcvNativeUtils;
//import com.isode.x400api.test.X400msTestRepRcvUtils;
//import com.isode.x400api.test.X400msTestBodyPartRcvUtils;
//import com.isode.x400api.test.X400msTestRepRcvUtils;
//import com.isode.x400api.test.config;
//import com.isode.x400api.test.X400msTestRcvNativeUtils;
//import com.isode.x400api.test.X400msTestRcvUtils;
//import com.isode.x400api.test.X400msTestRepRcvUtils;
//import com.isode.x400api.test.config;
//import com.isode.x400api.test.config;

public class RcvUtils {
	
	public static List<Msgbox> getAllMsg(String[] args) {

		Session session_obj = new Session();
		int type = 0;
		int paramtype;
		int close_status;
		
		// Open Connection x400
		int status = com.isode.x400api.X400ms.x400_ms_open(type, config.p7_bind_swim, config.p7_bind_dn,
				config.p7_credentials, config.p7_pa, session_obj);
		System.out.println("x400_ms_open Status: " + status);
		MSListResult mslistresult_obj = new MSListResult();
		status = com.isode.x400api.X400ms.x400_ms_list(session_obj, null,
				// "040101000000Z", // (optional) Time since when to list
				// messages - UTC time string, and is used to select only
				// messages which were delivered after that time & date
				mslistresult_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_list failed " + status);
			// close the API session
			close_status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
			if (close_status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_close failed " + status);
				return null;
			}
			System.out.println("Closed MS Session successfully\n");
			return null;
		}
		System.out.println("Opened MS session successfully, " + session_obj.GetNumMsgs() + " messages waiting");
		List<Msgbox> msgBoxs = new ArrayList<>();
		
		for (int i = 1;; i++) {
			
			Msgbox msgBox = new Msgbox();
			
			paramtype = X400_att.X400_N_MS_SEQUENCE_NUMBER;
			status = com.isode.x400api.X400ms.x400_ms_listgetintparam(mslistresult_obj, paramtype, i);

			if (status == X400_att.X400_E_NO_MORE_RESULTS) {
				System.out.println("No more list results, i = " + i);
				// All done
				break;
			}
			int seq = mslistresult_obj.GetIntValue();
			
			msgBox.setMsgsqn(seq);

			System.out.println("Receiving message " + i);
			System.out.println("=================================================");
			System.out.println("Sequence number: " + mslistresult_obj.GetIntValue());

			StringBuffer ret_value = new StringBuffer();
			paramtype = X400_att.X400_S_SUBJECT;
			status = com.isode.x400api.X400ms.x400_ms_listgetstrparam(mslistresult_obj, paramtype, i, ret_value);
			System.out.println("Subject ret_value (" + i + ") : " + ret_value.toString());
			msgBox.setMsgSubject(ret_value.toString());
			
			// instantiate a message object, and retrieve a msg
			// putting it into an API object
			MSMessage msmessage_obj = new MSMessage();
			// use seqn of 0 to retrieve the next msg
			status = com.isode.x400api.X400ms.x400_ms_msggetstart(session_obj, mslistresult_obj.GetIntValue(),
					msmessage_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetstart failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
					return null;
				}
				System.out.println("Closed MS Session successfully\n");
				return null;
			}

			// check what we got back
			type = msmessage_obj.GetType();
			if (type == X400_att.X400_MSG_MESSAGE) {
				int int_value;

				// got a message - is it an IPN ?
				System.out.println("Retrieved MS Message successfully - displaying");
				paramtype = X400_att.X400_N_IS_IPN;
				status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("failed to test whether IPN or message " + status);
				} else {
					int_value = msmessage_obj.GetIntValue();
					if (int_value != 0) {
						// It's an IPN ...
						System.out.println("Retrieved IPN successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
					} else {
						// It's not an IPN ...
						System.out.println("Retrieved msg (not ipn) successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
//						get_p772(msmessage_obj);
						// Send back an IPN to the originator (if requested)
						// -1 means positive
						status = send_ipn(msmessage_obj, -1);
					}
				}
			} else if (type == X400_att.X400_MSG_REPORT) {
				System.out.println("Retrieved MS Report successfully - displaying");
				status = RepRcvUtils.do_rep_env(msmessage_obj);
				status = RepRcvUtils.do_rep_content(msmessage_obj);
				status = RepRcvUtils.do_rep_retcontent(msmessage_obj);
			} else if (type == X400_att.X400_MSG_PROBE) {
				// Not handling a probe here
				System.out.println("Retrieved MS Report successfully - not displaying");
			} else {
				// Unknown object
				System.out.println("Retrieved unknown message type " + type);
			}

			status = com.isode.x400api.X400ms.x400_ms_msggetfinish(msmessage_obj, 0, 0);
			System.out.println("=================================================");
			msgBoxs.add(msgBox);
			
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetfinish failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
//					return;
				}
				System.out.println("Closed Session successfully\n");
//				return;
			}
		}


		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return null;
		}
		System.out.println("Closed Session successfully\n");
		return msgBoxs;
//		return;
	}
	
	public static List<Msgbox> getMsgboxBeanList(String[] args) {

		Session session_obj = new Session();
		int type = 0;
		int paramtype;
		int close_status;
		
		// Open Connection x400
		int status = com.isode.x400api.X400ms.x400_ms_open(type, config.p7_bind_swim, config.p7_bind_dn,
				config.p7_credentials, config.p7_pa, session_obj);
		System.out.println("x400_ms_open Status: " + status);
		MSListResult mslistresult_obj = new MSListResult();
		status = com.isode.x400api.X400ms.x400_ms_list(session_obj, null,
				// "040101000000Z", // (optional) Time since when to list
				// messages - UTC time string, and is used to select only
				// messages which were delivered after that time & date
				mslistresult_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_list failed " + status);
			// close the API session
			close_status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
			if (close_status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_close failed " + status);
				return null;
			}
			System.out.println("Closed MS Session successfully\n");
			return null;
		}
		System.out.println("Opened MS session successfully, " + session_obj.GetNumMsgs() + " messages waiting");
		List<Msgbox> msgBoxs = new ArrayList<>();
		
		for (int i = 1;; i++) {
			
			Msgbox msgBox = new Msgbox();
			paramtype = X400_att.X400_N_MS_SEQUENCE_NUMBER;
			status = com.isode.x400api.X400ms.x400_ms_listgetintparam(mslistresult_obj, paramtype, i);

			if (status == X400_att.X400_E_NO_MORE_RESULTS) {
				System.out.println("No more list results, i = " + i);
				// All done
				break;
			}
			int seq = mslistresult_obj.GetIntValue();
			
			msgBox.setMsgsqn(seq);

			System.out.println("Receiving message " + i);
			System.out.println("=================================================");
			System.out.println("Sequence number: " + mslistresult_obj.GetIntValue());

			StringBuffer ret_value = new StringBuffer();
			paramtype = X400_att.X400_S_SUBJECT;
			status = com.isode.x400api.X400ms.x400_ms_listgetstrparam(mslistresult_obj, paramtype, i, ret_value);
			System.out.println("Subject ret_value (" + i + ") : " + ret_value.toString());
			msgBox.setMsgSubject(ret_value.toString());
			
			// instantiate a message object, and retrieve a msg
			// putting it into an API object
			MSMessage msmessage_obj = new MSMessage();
			// use seqn of 0 to retrieve the next msg
			status = com.isode.x400api.X400ms.x400_ms_msggetstart(session_obj, mslistresult_obj.GetIntValue(),
					msmessage_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetstart failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
					return null;
				}
				System.out.println("Closed MS Session successfully\n");
				return null;
			}

			// check what we got back
			type = msmessage_obj.GetType();
			if (type == X400_att.X400_MSG_MESSAGE) {
				int int_value;

				// got a message - is it an IPN ?
				System.out.println("Retrieved MS Message successfully - displaying");
				paramtype = X400_att.X400_N_IS_IPN;
				status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("failed to test whether IPN or message " + status);
				} else {
					int_value = msmessage_obj.GetIntValue();
					if (int_value != 0) {
						// It's an IPN ...
						System.out.println("Retrieved IPN successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
					} else {
						// It's not an IPN ...
						System.out.println("Retrieved msg (not ipn) successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
//						get_p772(msmessage_obj);
						// Send back an IPN to the originator (if requested)
						// -1 means positive
						status = send_ipn(msmessage_obj, -1);
					}
				}
			} 
			
//			else if (type == X400_att.X400_MSG_REPORT) {
//				System.out.println("Retrieved MS Report successfully - displaying");
//				status = ReportRcvUtils.do_rep_env(msmessage_obj);
//				status = ReportRcvUtils.do_rep_content(msmessage_obj);
//				status = ReportRcvUtils.do_rep_retcontent(msmessage_obj);
//			} else if (type == X400_att.X400_MSG_PROBE) {
//				// Not handling a probe here
//				System.out.println("Retrieved MS Report successfully - not displaying");
//			} else {
//				// Unknown object
//				System.out.println("Retrieved unknown message type " + type);
//			}

			status = com.isode.x400api.X400ms.x400_ms_msggetfinish(msmessage_obj, 0, 0);
			System.out.println("=================================================");
			msgBoxs.add(msgBox);
//			ApplicationClient.latestSequence = msgBox.getMsgsqn();
			
//			 delete the API msg object and from the Store
			 status = com.isode.x400api.X400ms.x400_ms_msgdel(msmessage_obj, 0);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msgget failed " + status);
				return msgBoxs;
			}

			
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetfinish failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
					return msgBoxs;
				}
				System.out.println("Closed Session successfully\n");
				return msgBoxs;
			}
		}


		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return msgBoxs;
		}
		System.out.println("Closed Session successfully\n");
		return msgBoxs;
//		return;
	}
	
	public static List<Msgbox> getMsgboxBeanList(String or, String dn, String pa, String credential) {

		Session session_obj = new Session();
		int type = 0;
		int paramtype;
		int close_status;
		
		// Open Connection x400
		int status = com.isode.x400api.X400ms.x400_ms_open(type, or, dn,
				credential, config.p7_pa, session_obj);
		System.out.println("***x400_ms_open Status: " + status);
		MSListResult mslistresult_obj = new MSListResult();
		status = com.isode.x400api.X400ms.x400_ms_list(session_obj, null,
				// "040101000000Z", // (optional) Time since when to list
				// messages - UTC time string, and is used to select only
				// messages which were delivered after that time & date
				mslistresult_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_list failed " + status);
			// close the API session
			close_status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
			if (close_status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_close failed " + status);
				return null;
			}
			System.out.println("Closed MS Session successfully\n");
			return null;
		}
		System.out.println("Opened MS session successfully, " + session_obj.GetNumMsgs() + " messages waiting");
		List<Msgbox> msgBoxs = new ArrayList<>();
		
		for (int i = 1;; i++) {
			
			Msgbox msgBox = new Msgbox();
			paramtype = X400_att.X400_N_MS_SEQUENCE_NUMBER;
			status = com.isode.x400api.X400ms.x400_ms_listgetintparam(mslistresult_obj, paramtype, i);

			if (status == X400_att.X400_E_NO_MORE_RESULTS) {
				System.out.println("No more list results, i = " + i);
				// All done
				break;
			}
			int seq = mslistresult_obj.GetIntValue();
			
			msgBox.setMsgsqn(seq);

			System.out.println("Receiving message " + i);
			System.out.println("=================================================");
			System.out.println("Sequence number: " + mslistresult_obj.GetIntValue());

			StringBuffer ret_value = new StringBuffer();
			paramtype = X400_att.X400_S_SUBJECT;
			status = com.isode.x400api.X400ms.x400_ms_listgetstrparam(mslistresult_obj, paramtype, i, ret_value);
			System.out.println("Subject ret_value (" + i + ") : " + ret_value.toString());
			msgBox.setMsgSubject(ret_value.toString());
			
			// instantiate a message object, and retrieve a msg
			// putting it into an API object
			MSMessage msmessage_obj = new MSMessage();
			// use seqn of 0 to retrieve the next msg
			status = com.isode.x400api.X400ms.x400_ms_msggetstart(session_obj, mslistresult_obj.GetIntValue(),
					msmessage_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetstart failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
					return null;
				}
				System.out.println("Closed MS Session successfully\n");
				return null;
			}

			// check what we got back
			type = msmessage_obj.GetType();
			if (type == X400_att.X400_MSG_MESSAGE) {
				int int_value;

				// got a message - is it an IPN ?
				System.out.println("Retrieved MS Message successfully - displaying");
				paramtype = X400_att.X400_N_IS_IPN;
				status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("failed to test whether IPN or message " + status);
				} else {
					int_value = msmessage_obj.GetIntValue();
					if (int_value != 0) {
						// It's an IPN ...
						System.out.println("Retrieved IPN successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
					} else {
						// It's not an IPN ...
						System.out.println("Retrieved msg (not ipn) successfully - displaying");
						msgBox = do_msg_env(msmessage_obj, msgBox);
						msgBox = do_msg_headers(msmessage_obj, msgBox);
						/*
						 * the content can be retrieved as attachments or
						 * bodyparts. Use do_msg_content for the former
						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
						msgBox = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, msgBox);
						status = msgBox.getStatus();
//						get_p772(msmessage_obj);
						// Send back an IPN to the originator (if requested)
						// -1 means positive
						status = send_ipn(msmessage_obj, -1);
					}
				}
			} 
			
//			else if (type == X400_att.X400_MSG_REPORT) {
//				System.out.println("Retrieved MS Report successfully - displaying");
//				status = ReportRcvUtils.do_rep_env(msmessage_obj);
//				status = ReportRcvUtils.do_rep_content(msmessage_obj);
//				status = ReportRcvUtils.do_rep_retcontent(msmessage_obj);
//			} else if (type == X400_att.X400_MSG_PROBE) {
//				// Not handling a probe here
//				System.out.println("Retrieved MS Report successfully - not displaying");
//			} else {
//				// Unknown object
//				System.out.println("Retrieved unknown message type " + type);
//			}

			status = com.isode.x400api.X400ms.x400_ms_msggetfinish(msmessage_obj, 0, 0);
			System.out.println("=================================================");
			msgBoxs.add(msgBox);
//			ApplicationClient.latestSequence = msgBox.getMsgsqn();
			
//			 delete the API msg object and from the Store
			 status = com.isode.x400api.X400ms.x400_ms_msgdel(msmessage_obj, 0);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msgget failed " + status);
				return msgBoxs;
			}

			
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_msggetfinish failed " + status);
				// close the API session
				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("x400_ms_close failed " + status);
					return msgBoxs;
				}
				System.out.println("Closed Session successfully\n");
				return msgBoxs;
			}
		}


		// close the API session
		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_close failed " + status);
			return msgBoxs;
		}
		System.out.println("Closed Session successfully\n");
		return msgBoxs;
//		return;
	}
	
//	public static void rcv_all_msg(String[] args) {
//
//		Session session_obj = new Session();
//		int type = 0;
//		int paramtype;
//		int close_status;
//		
//		// Open Connection x400
//		int status = com.isode.x400api.X400ms.x400_ms_open(type, config.p7_bind_oraddr, config.p7_bind_dn,
//				config.p7_credentials, config.p7_pa, session_obj);
//		System.out.println("x400_ms_open Status: " + status);
//		MSListResult mslistresult_obj = new MSListResult();
//		status = com.isode.x400api.X400ms.x400_ms_list(session_obj, "040101000000Z",
//				// "040101000000Z", // (optional) Time since when to list
//				// messages - UTC time string, and is used to select only
//				// messages which were delivered after that time & date
//				mslistresult_obj);
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("x400_ms_list failed " + status);
//			// close the API session
//			close_status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
//			if (close_status != X400_att.X400_E_NOERROR) {
//				System.out.println("x400_ms_close failed " + status);
//				return;
//			}
//			System.out.println("Closed MS Session successfully\n");
//			return;
//		}
//		System.out.println("Opened MS session successfully, " + session_obj.GetNumMsgs() + " messages waiting");
//		for (int i = 1;; i++) {
//			paramtype = X400_att.X400_N_MS_SEQUENCE_NUMBER;
//			status = com.isode.x400api.X400ms.x400_ms_listgetintparam(mslistresult_obj, paramtype, i);
//
//			if (status == X400_att.X400_E_NO_MORE_RESULTS) {
//				System.out.println("No more list results, i = " + i);
//				// All done
//				break;
//			}
//
//			System.out.println("Receiving message " + i);
//			System.out.println("=================================================");
//			System.out.println("Sequence number: " + mslistresult_obj.GetIntValue());
//
//			StringBuffer ret_value = new StringBuffer();
//			paramtype = X400_att.X400_S_SUBJECT;
//			status = com.isode.x400api.X400ms.x400_ms_listgetstrparam(mslistresult_obj, paramtype, i, ret_value);
//			System.out.println("Subject ret_value (" + i + ") : " + ret_value.toString());
//
//			// instantiate a message object, and retrieve a msg
//			// putting it into an API object
//			MSMessage msmessage_obj = new MSMessage();
//			// use seqn of 0 to retrieve the next msg
//			status = com.isode.x400api.X400ms.x400_ms_msggetstart(session_obj, mslistresult_obj.GetIntValue(),
//					msmessage_obj);
//			if (status != X400_att.X400_E_NOERROR) {
//				System.out.println("x400_ms_msggetstart failed " + status);
//				// close the API session
//				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
//				if (status != X400_att.X400_E_NOERROR) {
//					System.out.println("x400_ms_close failed " + status);
//					return;
//				}
//				System.out.println("Closed MS Session successfully\n");
//				return;
//			}
//
//			// check what we got back
//			type = msmessage_obj.GetType();
//			if (type == X400_att.X400_MSG_MESSAGE) {
//				int int_value;
//
//				// got a message - is it an IPN ?
//				System.out.println("Retrieved MS Message successfully - displaying");
//				paramtype = X400_att.X400_N_IS_IPN;
//				status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
//				if (status != X400_att.X400_E_NOERROR) {
//					System.out.println("failed to test whether IPN or message " + status);
//				} else {
//					int_value = msmessage_obj.GetIntValue();
//					if (int_value != 0) {
//						// It's an IPN ...
//						System.out.println("Retrieved IPN successfully - displaying");
//						status = do_msg_env(msmessage_obj, msgBox);
//						status = do_msg_headers(msmessage_obj);
//						/*
//						 * the content can be retrieved as attachments or
//						 * bodyparts. Use do_msg_content for the former
//						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
//					} else {
//						// It's not an IPN ...
//						System.out.println("Retrieved msg (not ipn) successfully - displaying");
//						status = do_msg_env(msmessage_obj);
//						status = do_msg_headers(msmessage_obj);
//						/*
//						 * the content can be retrieved as attachments or
//						 * bodyparts. Use do_msg_content for the former
//						 */
//						status = BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj);
////						get_p772(msmessage_obj);
//						// Send back an IPN to the originator (if requested)
//						// -1 means positive
//						status = send_ipn(msmessage_obj, -1);
//					}
//				}
//			} else if (type == X400_att.X400_MSG_REPORT) {
//				System.out.println("Retrieved MS Report successfully - displaying");
//				status = ReportRcvUtils.do_rep_env(msmessage_obj);
//				status = ReportRcvUtils.do_rep_content(msmessage_obj);
//				status = ReportRcvUtils.do_rep_retcontent(msmessage_obj);
//			} else if (type == X400_att.X400_MSG_PROBE) {
//				// Not handling a probe here
//				System.out.println("Retrieved MS Report successfully - not displaying");
//			} else {
//				// Unknown object
//				System.out.println("Retrieved unknown message type " + type);
//			}
//
//			status = com.isode.x400api.X400ms.x400_ms_msggetfinish(msmessage_obj, 0, 0);
//			if (status != X400_att.X400_E_NOERROR) {
//				System.out.println("x400_ms_msggetfinish failed " + status);
//				// close the API session
//				status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
//				if (status != X400_att.X400_E_NOERROR) {
//					System.out.println("x400_ms_close failed " + status);
//					return;
//				}
//				System.out.println("Closed Session successfully\n");
//				return;
//			}
//			System.out.println("=================================================");
//		}
//
//
//		// close the API session
//		status = com.isode.x400api.X400ms.x400_ms_close(session_obj);
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("x400_ms_close failed " + status);
//			return;
//		}
//		System.out.println("Closed Session successfully\n");
//		return;
//	}
	/**
	 * Submit an IPN for the message received.
	 */
	private static int send_ipn(MSMessage msmessage_obj, int reason) {
		int status;
		MSMessage ipn = new MSMessage();

		System.out.println("Creating IPN ...");
		status = com.isode.x400api.X400ms.x400_ms_msgmakeIPN(msmessage_obj, reason, ipn);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("msgmakeIPN failed " + status);
			return status;
		}

		// now submit the IPN
		System.out.println("Submitting IPN ...");
		status = com.isode.x400api.X400ms.x400_ms_msgsend(ipn);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msgsend failed " + status);
			return status;
		}
		System.out.println("Submitted IPN successfully");

		return status;
	}
	/**
	 * Display a MS Message envelope (ie a message retrieved the message store).
	 */
	private static Msgbox do_msg_env(MSMessage msmessage_obj, Msgbox msgBox) {
		int status;
		int paramtype;
		int len = -1, maxlen = -1;
		int recip_type = 1;
		int recip_num = 1;
		int entry = 1;

		String value; // string to contain value returned from API
		int int_value; // int to contain value returned from API

		System.out.println("Message Envelope:");
		System.out.println("----------------");

		// Initialise object to receive returned String value
		StringBuffer ret_value = new StringBuffer();

		// get orig address: string attribute from the message envelope
		paramtype = X400_att.X400_S_OR_ADDRESS;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out
					.println("x400_ms_msggetstrparam failed X400_S_OR_ADDRESS(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Originator address (" + len + ") " + ret_value.toString());
			msgBox.setMsgOrgn(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_MESSAGE_IDENTIFIER;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Message ID(" + len + ") " + ret_value.toString());
			msgBox.setMtsId(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_CONTENT_IDENTIFIER;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Content ID(" + len + ") " + ret_value.toString());
			msgBox.setpContIdt(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_ORIGINAL_ENCODED_INFORMATION_TYPES;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Orig EITs(" + len + ") " + ret_value.toString());
			msgBox.setAtsEncode(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_MESSAGE_SUBMISSION_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Message submission time(" + len + ") " + ret_value.toString());
			msgBox.setReceivetime(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_MESSAGE_DELIVERY_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Message delivery time(" + len + ") " + ret_value.toString());
		}

		// get the latest delivery time
		// which won't work for a delivered msg
		paramtype = X400_att.X400_S_LATEST_DELIVERY_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out
					.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status + "(as expected)");
		} else {
			len = ret_value.length();
			System.out.println("Latest delivery time(" + len + ") " + ret_value.toString());
			msgBox.setpLatestdelivery(ret_value.toString());
		}

		// get some string attributes from the message envelope
		paramtype = X400_att.X400_S_ORIGINATOR_RETURN_ADDRESS;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no string value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetstrparam failed(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Orig return address(" + len + ") " + ret_value.toString());
		}

		// get some integer attributes from the message
		paramtype = X400_att.X400_N_CONTENT_TYPE;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Content type " + int_value);
		}

		paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Num attachments " + int_value);
			msgBox.setAttachcount(int_value);
		}

		paramtype = X400_att.X400_N_PRIORITY;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Priority " + int_value);
			msgBox.setMsgPriority(Integer.toString(int_value));

		}

		// we don't expect this to be present for delivery env
		paramtype = X400_att.X400_N_DISCLOSURE;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_BADPARAM) {
			System.out.println("no int value for Disclosure of recips prohibited(" + paramtype + ") (as expected)");
		} else if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Disclosure of recips prohibited " + int_value);
			msgBox.setPRcpDisc(int_value != 0);
		}

		paramtype = X400_att.X400_N_IMPLICIT_CONVERSION_PROHIBITED;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Implicit conversion prohibited " + int_value);
			msgBox.setPImpConvPhb(int_value != 0);
		}

		paramtype = X400_att.X400_N_CONTENT_RETURN_REQUEST;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_BADPARAM) {
			System.out.println("no int value for Content return request(" + paramtype + ") (as expected)");
		} else if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Content return request " + int_value);
			msgBox.setPContReturn(int_value != 0);
		}

		paramtype = X400_att.X400_N_RECIPIENT_REASSIGNMENT_PROHIBITED;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_BADPARAM) {
			System.out.println("no int value for Recipient reassignment prohibited(" + paramtype + ") (as expected)");
		} else if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Recipient reassignment prohibited " + int_value);
			msgBox.setPRcpRasgPhb(int_value != 0);
		}

		paramtype = X400_att.X400_N_DL_EXPANSION_PROHIBITED;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_BADPARAM) {
			System.out.println(
					"no int value for Distribution List expansion prohibited(" + paramtype + ") (as expected)");
		} else if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Distribution List expansion prohibited " + int_value);
			msgBox.setPDlExpPhb(int_value != 0);
		}

		paramtype = X400_att.X400_N_CONVERSION_WITH_LOSS_PROHIBITED;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Conversion with loss prohibited " + int_value);
			msgBox.setPConvLossPhb(int_value != 0);
		}

		// Find out how many recips
//		int recipNum = 0;
//		
//		status = com.isode.x400api.X400ms.x400_ms_msgcountrecip(msmessage_obj, X400_att.X400_RECIP_PRIMARY);
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("can't count recips");
//		} else {
//			System.out.println("Number of recipients (primary) is " + msmessage_obj.GetNumRecips());
//			recipNum = recipNum + msmessage_obj.GetNumRecips();
//		}
//
//		status = com.isode.x400api.X400ms.x400_ms_msgcountrecip(msmessage_obj, X400_att.X400_RECIP_CC);
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("can't count recips");
//		} else {
//			System.out.println("Number of recipients (cc) is " + msmessage_obj.GetNumRecips());
//			recipNum = recipNum + msmessage_obj.GetNumRecips();
//		}
//		
//		status = com.isode.x400api.X400ms.x400_ms_msgcountrecip(msmessage_obj, X400_att.X400_RECIP_BCC);
//		if (status != X400_att.X400_E_NOERROR) {
//			System.out.println("can't count recips");
//		} else {
//			System.out.println("Number of recipients (bcc) is " + msmessage_obj.GetNumRecips());
//			recipNum = recipNum + msmessage_obj.GetNumRecips();
//		}
//		
//		status = get_ms_recips(msmessage_obj, X400_att.X400_RECIP_ENVELOPE, "envelope: ");
//		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
//			System.out.println("can't count recips");
//		} else {
//			System.out.println("Number of recipients (envelope) is " + msmessage_obj.GetNumRecips());
//			recipNum = recipNum + msmessage_obj.GetNumRecips();
//		}
//		msgBox.setRcpcount(recipNum);
		


		System.out.println("Get_ms_traceinfo ");
		Traceinfo traceinfo_obj = new Traceinfo();
		entry = 1;
		status = X400_att.X400_E_NOERROR;
//		while (status == X400_att.X400_E_NOERROR) {
//			status = get_ms_traceinfo(msmessage_obj, entry, traceinfo_obj, X400_att.X400_TRACE_INFO);
//			if (status != X400_att.X400_E_NOERROR) {
//				System.out.println("get_ms_traceinfo failed " + status);
//				break;
//			}
//			entry++;
//		}
		String totalTraceinfo = "";
		while (status == X400_att.X400_E_NOERROR) {
			String traceinfo = get_ms_traceinfo_text(msmessage_obj, entry, traceinfo_obj, X400_att.X400_TRACE_INFO);
			if (traceinfo.length() < 1) {
				System.out.println("get_ms_traceinfo failed " + status);
				break;
			}
			totalTraceinfo = totalTraceinfo + traceinfo + " ";
			entry++;
		}
		msgBox.setTraceinfo(totalTraceinfo.trim());
		
		System.out.println("Get_ms_internal_traceinfo ");
		InternalTraceinfo internaltraceinfo_obj = new InternalTraceinfo();
		entry = 1;
		status = X400_att.X400_E_NOERROR;
//		while (status == X400_att.X400_E_NOERROR) {
//			status = get_ms_internaltraceinfo(msmessage_obj, entry, internaltraceinfo_obj);
//			if (status != X400_att.X400_E_NOERROR) {
//				System.out.println("get_ms_internaltraceinfo failed " + status);
//				break;
//			}
//			entry++;
//		}
		String totalInternalTraceinfo = "";
		while (status == X400_att.X400_E_NOERROR) {
			status = get_ms_internaltraceinfo(msmessage_obj, entry, internaltraceinfo_obj);
			String internaltraceinfo = get_ms_internaltraceinfo_text(msmessage_obj, entry, internaltraceinfo_obj);
			if (internaltraceinfo.length() < 1) {
				System.out.println("get_ms_internaltraceinfo failed " + status);
				break;
			}
			totalInternalTraceinfo = totalInternalTraceinfo + internaltraceinfo + " ";
			entry++;
		}
		msgBox.setTraceinfointernal(totalInternalTraceinfo);
		
		System.out.println("get_ms_dleh ");
		/* Distribution List Expansion History */
		DLExpHist dleh_obj = new DLExpHist();
		while (status == X400_att.X400_E_NOERROR) {
			status = get_ms_dleh(msmessage_obj, entry, dleh_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("get_ms_dleh failed " + status);
				break;
			}
			entry++;
		}
		return msgBox;

	}
	
	/**
	 * Display the MS Message headers (ie a message retrieved the message store
	 * using the msgetmsg() API call).
	 */
	private static Msgbox do_msg_headers(MSMessage msmessage_obj, Msgbox msgBox) {
		int status;
		int paramtype;
		int len = -1, maxlen = -1;

		String value; // string to contain value returned from API
		int int_value; // int to contain value returned from API

		System.out.println("Message Content:");
		System.out.println("----------------");

		// Initialise object to receive returned String value
		StringBuffer ret_value = new StringBuffer();

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_IPM_IDENTIFIER;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for IPM ID(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("IPM IDentifier(" + len + ") " + ret_value.toString());
			msgBox.setIpmId(ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_SUBJECT;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for Subject(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Subject(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_REPLIED_TO_IDENTIFIER;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for replied to ID(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Replied-to-identifier(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_OBSOLETED_IPMS;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for obsoleted IPMs(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Obsoleted IPMs(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_RELATED_IPMS;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for related IPMs(" + paramtype + "result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Related IPMs(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_EXPIRY_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for expiry time(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Expiry Time(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_REPLY_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for reply time(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Reply Time(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_AUTHORIZATION_TIME;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for auth time(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Authorisation Time(" + len + ") " + ret_value.toString());
		}

		// get some string attributes from the message content
		paramtype = X400_att.X400_S_ORIGINATORS_REFERENCE;
		status = com.isode.x400api.X400ms.x400_ms_msggetstrparam(msmessage_obj, paramtype, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for orig ref(" + paramtype + ") result is " + status);
		} else {
			len = ret_value.length();
			System.out.println("Originator's Reference(" + len + ") " + ret_value.toString());
		}

		paramtype = X400_att.X400_N_IMPORTANCE;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for importance " + paramtype);
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Importance " + int_value);
		}

		paramtype = X400_att.X400_N_SENSITIVITY;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for sensitivity" + paramtype + ")");
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Sensitivity " + int_value);
		}

		paramtype = X400_att.X400_N_AUTOFORWARDED;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for autoforwarded (" + paramtype + ")");
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + status);
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("Autoforwarded " + int_value);
		}

		paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
		status = com.isode.x400api.X400ms.x400_ms_msggetintparam(msmessage_obj, paramtype);
		if (status == X400_att.X400_E_NO_VALUE) {
			System.out.println("no int value for num atts " + paramtype + ")");
		} else if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetintparam failed " + paramtype + ")");
		} else {
			int_value = msmessage_obj.GetIntValue();
			System.out.println("number of attachments " + int_value);
		}

		List<Msgboxrecipient> msgBoxRecipients = new ArrayList<>();
//		status = get_ms_recips(msmessage_obj, X400_att.X400_RECIP_PRIMARY, "PRIMARY");
		
		msgBoxRecipients = get_ms_recips_list(msmessage_obj, X400_att.X400_RECIP_PRIMARY, "PRIMARY");
		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
			System.out.println("get_ms_recips failed " + status);
			return msgBox;
		} 

//		status = get_ms_recips(msmessage_obj, X400_att.X400_RECIP_CC, "CC");
		msgBoxRecipients.addAll(get_ms_recips_list(msmessage_obj, X400_att.X400_RECIP_CC, "CC"));
		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
			System.out.println("get_ms_recips failed " + status);
			return msgBox;
		}
//		status = get_ms_recips(msmessage_obj, X400_att.X400_RECIP_BCC, "BCC");
		msgBoxRecipients.addAll(get_ms_recips_list(msmessage_obj, X400_att.X400_RECIP_BCC, "BCC"));
		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
			System.out.println("get_ms_recips failed " + status);
			return msgBox;
		}
		
//		status = get_ms_recips(msmessage_obj, X400_att.X400_ORIGINATOR, "ORIGINATOR");
		msgBoxRecipients.addAll(get_ms_recips_list(msmessage_obj, X400_att.X400_ORIGINATOR, "ORIGINATOR"));
		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
			System.out.println("get_ms_recips failed " + status);
			return msgBox;
		}
		
		// we monitor only for VTBBSWIM, so we don't want envelope information (There would be only VTBBSWIM in an envelope)
//		status = get_ms_recips(msmessage_obj, X400_att.X400_RECIP_ENVELOPE, "ENVELOPE");
//		msgBoxRecipients.addAll(get_ms_recips_list(msmessage_obj, X400_att.X400_RECIP_ENVELOPE, "ENVELOPE"));	
//		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_NO_RECIP) {
//			System.out.println("get_ms_recips failed (env)" + status);
//			return msgBox;
//		}
		
		// set alias name for origin, msgcc, msgbcc, msgto in MSGBOX
		String origin = "";
		String msgcc = "";
		String msgbcc = "";
		String msgto = "";
		
		for (Msgboxrecipient msgBoxRecipient : msgBoxRecipients) {
			msgBoxRecipient.setMsgbox(msgBox);
			switch (msgBoxRecipient.getRecipienttype()) {
			case "PRIMARY":
				msgto = msgto + " " + msgBoxRecipient.getAliasname();
				break;
			case "CC":
				msgcc = msgcc + " " + msgBoxRecipient.getAliasname();
				break;
			case "BCC":
				msgbcc = msgbcc + " " + msgBoxRecipient.getAliasname();
				break;
			case "ORIGINATOR":
				origin = origin + " " + msgBoxRecipient.getAliasname();
				break;
			default:
				break;
			}
		}
		msgBox.setMsgTo(msgto.trim());
		msgBox.setMsgCc(msgcc.trim());
		msgBox.setMsgBcc(msgbcc.trim());
		msgBox.setMsgOrgn2(origin.trim());
		System.out.println("msgBoxRecipients size: "+msgBoxRecipients.size());
		msgBox.setRcpcount(msgBoxRecipients.size());
		msgBox.setMsgboxrecipients(msgBoxRecipients);
		return msgBox;
	}
	
	/**
	 * Display the MS Message recipients (ie those of a message retrieved the
	 * message store.
	 */
	private static int get_ms_recips(MSMessage msmessage_obj, int type, String logstr) {
		int status;
		int paramtype;
		int recip_num = 1;
		int len = -1, maxlen = -1;
		int int_value; // int to contain value returned from API

		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();

		// instantiate a recip object, and retrieve a recip
		// putting it into an API object
		Recip recip_obj = new Recip();
		for (recip_num = 1;; recip_num++) {

			// get the recip
			status = com.isode.x400api.X400ms.x400_ms_recipget(msmessage_obj, type, recip_num, recip_obj);
			if (status == X400_att.X400_E_NO_RECIP) {
				System.out.println("no more recips ...");
				break;
			} else if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_recipget failed " + status);
				break;
			}
  
			System.out.println("-------------- Recipient " + recip_num + "--------------" + logstr);
			// display recip name
			paramtype = X400_att.X400_S_OR_ADDRESS;
			status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
			} else {
				len = ret_value.length();
				System.out.println("oraddress" + "(" + len + ")" + ret_value.toString());
			}

			// Get envelope values
			if (type == X400_att.X400_RECIP_ENVELOPE) {
				// display recip properties
				paramtype = X400_att.X400_S_OR_ADDRESS;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for originator address(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println(logstr + recip_num + "(" + len + ")" + ret_value.toString());
				}

				// display recip properties
				paramtype = X400_att.X400_N_RESPONSIBILITY;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for Responsibility(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Responsibility " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_MTA_REPORT_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for MTA report req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("MTA report request " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_REPORT_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for report req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Originator report request " + int_value);
				}

				RediHist redihist_obj = new RediHist();
				int entry = 1;
				status = X400_att.X400_E_NOERROR;
				while (status == X400_att.X400_E_NOERROR) {
					status = get_ms_redihist(null, recip_obj, entry, redihist_obj);
					if (status != X400_att.X400_E_NOERROR) {
						System.out.println("get_ms_redihist failed " + status);
						break;
					}
					entry++;
				}

			}

			// Get content values
			if (type != X400_att.X400_RECIP_ENVELOPE) {
				// display recip properties
				paramtype = X400_att.X400_N_NOTIFICATION_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for notification req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Responsibility " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_REPLY_REQUESTED;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for reply requested(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Reply Request " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_S_FREE_FORM_NAME;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for Free form name(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println("Free form name" + "(" + len + ")" + ret_value.toString());
				}

				// display recip properties
				paramtype = X400_att.X400_S_TELEPHONE_NUMBER;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for Telephone Number(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println("Telephone Number" + "(" + len + ")" + ret_value.toString());
				}

//				if (config.mt_use_p772 == true) {
//					/* Get P772 per-recipient extensions */
//					paramtype = X400_att.X400_N_ACP127_NOTI_TYPE;
//					status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
//					if (status != X400_att.X400_E_NOERROR) {
//						System.out.println("no int value for Notification request(" + paramtype + ") failed " + status);
//					} else {
//						int_value = recip_obj.GetIntValue();
//
//						if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_NEG) != 0) {
//							System.out.println("P772 ACP127 Notification Request Type Negative\n");
//						}
//						if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_POS) != 0) {
//							System.out.println("P772 ACP127 Notification Request Type Positive\n");
//						}
//
//						if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_TRANS) != 0) {
//							System.out.println("P772 ACP127 Notification Request Type Transfer\n");
//						}
//
//					}
//
//				}

			}
		}

		/*************
		 * The ACP127 Notification Response is only present in a Military
		 * Notification ********
		 * 
		 * System.out.println("Fetching ACP127 notification response");
		 * ACP127Resp resp_obj = new ACP127Resp(); status =
		 * com.isode.x400api.X400ms.x400_ms_acp127respget(msmessage_obj,
		 * resp_obj); if (status != X400_att.X400_E_NOERROR) {
		 * System.out.println("x400_acp127respget failed " + status); } else {
		 * status = com.isode.x400api.X400.x400_acp127respgetintparam(resp_obj);
		 * if (status != X400_att.X400_E_NOERROR) {
		 * System.out.println("x400_acp127respgetintparam failed " + status); }
		 * else { int_value = recip_obj.GetIntValue();
		 * 
		 * if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_NEG) != 0) {
		 * System.out.println("P772 ACP127 Notification response Negative\n"); }
		 * if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_POS) != 0) {
		 * System.out.println("P772 ACP127 Notification response Positive\n"); }
		 * 
		 * if ((int_value & X400_att.X400_ACP127_NOTI_TYPE_TRANS) != 0) {
		 * System.out.println("P772 ACP127 Notification response Transfer\n"); }
		 * 
		 * }
		 * 
		 * status = com.isode.x400api.X400.x400_acp127respgetstrparam( resp_obj,
		 * X400_att.X400_S_ACP127_NOTI_RESP_TIME, ret_value); if (status !=
		 * X400_att.X400_E_NOERROR) {
		 * System.out.println("x400_acp127respgetstrparam failed(" +
		 * X400_att.X400_S_ACP127_NOTI_RESP_TIME + "): result is " + status); }
		 * else { System.out.println("P772 ACP127 Resp time " +
		 * ret_value.toString()); }
		 * 
		 * status = com.isode.x400api.X400.x400_acp127respgetstrparam( resp_obj,
		 * X400_att.X400_S_ACP127_NOTI_RESP_RECIPIENT, ret_value); if (status !=
		 * X400_att.X400_E_NOERROR) {
		 * System.out.println("x400_acp127respgetstrparam failed(" +
		 * X400_att.X400_S_ACP127_NOTI_RESP_RECIPIENT + "): result is " +
		 * status); } else { System.out.println("P772 ACP127 Resp Recipient" +
		 * ret_value.toString()); }
		 * 
		 * status = com.isode.x400api.X400.x400_acp127respgetstrparam( resp_obj,
		 * X400_att.X400_S_ACP127_NOTI_RESP_SUPP_INFO, ret_value); if (status !=
		 * X400_att.X400_E_NOERROR) {
		 * System.out.println("x400_acp127respgetstrparam failed(" +
		 * X400_att.X400_S_ACP127_NOTI_RESP_SUPP_INFO + "): result is " +
		 * status); } else { System.out.println("P772 ACP127 Resp Supp info" +
		 * ret_value.toString()); }
		 * 
		 * System.out.println("Address list indicator"); ALI ali = new ALI();
		 * int entry = 1; status = X400_att.X400_E_NOERROR; while(status ==
		 * X400_att.X400_E_NOERROR) { status =
		 * X400msTestRcvUtils.get_acp127respali( resp_obj, entry, ali ); if
		 * (status != X400_att.X400_E_NOERROR) {
		 * System.out.println("get_acp127respali failed " + status); break; }
		 * entry++; }
		 * 
		 * }
		 * 
		 **************************/

		return X400_att.X400_E_NOERROR;
	}
	
	/**
	 * Display the MS Message recipients (ie those of a message retrieved the
	 * message store.
	 */
	private static List<Msgboxrecipient> get_ms_recips_list(MSMessage msmessage_obj, int type, String logstr) {
		int status;
		int paramtype;
		int recip_num = 1;
		int len = -1, maxlen = -1;
		int int_value; // int to contain value returned from API

		List<Msgboxrecipient> msgBoxRecipients = new ArrayList<>();
		
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();

		// instantiate a recip object, and retrieve a recip
		// putting it into an API object
		Recip recip_obj = new Recip();
		for (recip_num = 1;; recip_num++) {

			
			// get the recip
			status = com.isode.x400api.X400ms.x400_ms_recipget(msmessage_obj, type, recip_num, recip_obj);
			if (status == X400_att.X400_E_NO_RECIP) {
				System.out.println("no more recips ...");
				break;
			} else if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_ms_recipget failed " + status);
				break;
			}

			Msgboxrecipient msgBoxRecipient = new Msgboxrecipient();
			msgBoxRecipient.setRecipienttype(logstr);
			System.out.println("-------------- Recipient " + recip_num + "--------------" + logstr);
			// display recip name (oraddress)
			paramtype = X400_att.X400_S_OR_ADDRESS;
			status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("no string value for oraddress(" + paramtype + ") failed " + status);
			} else {
				len = ret_value.length();
				System.out.println("oraddress" + "(" + len + ")" + ret_value.toString());
				msgBoxRecipient.setOraddress(ret_value.toString());
			}

			// get recip common name
			paramtype = X400_att.X400_S_COMMON_NAME;
			status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("no string value for common name(" + paramtype + ") failed " + status);
			} else {
				len = ret_value.length();
				System.out.println("common name" + "(" + len + ")" + ret_value.toString());
				msgBoxRecipient.setAliasname(ret_value.toString());
			}
			
			// Get envelope values
			if (type == X400_att.X400_RECIP_ENVELOPE) {
				// display recip properties
				paramtype = X400_att.X400_S_OR_ADDRESS;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for originator address(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println(logstr + recip_num + "(" + len + ")" + ret_value.toString());
				}

				// display recip properties
				paramtype = X400_att.X400_N_RESPONSIBILITY;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for Responsibility(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Responsibility " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_MTA_REPORT_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for MTA report req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("MTA report request " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_REPORT_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for report req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Originator report request " + int_value);
				}

				RediHist redihist_obj = new RediHist();
				int entry = 1;
				status = X400_att.X400_E_NOERROR;
				while (status == X400_att.X400_E_NOERROR) {
					status = get_ms_redihist(null, recip_obj, entry, redihist_obj);
					if (status != X400_att.X400_E_NOERROR) {
						System.out.println("get_ms_redihist failed " + status);
						break;
					}
					entry++;
				}

			}

			// Get content values
			if (type != X400_att.X400_RECIP_ENVELOPE) {
				// display recip properties
				paramtype = X400_att.X400_N_NOTIFICATION_REQUEST;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for notification req(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Responsibility " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_N_REPLY_REQUESTED;
				status = com.isode.x400api.X400ms.x400_ms_recipgetintparam(recip_obj, paramtype);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no int value for reply requested(" + paramtype + ") failed " + status);
				} else {
					int_value = recip_obj.GetIntValue();
					System.out.println("Reply Request " + int_value);
				}

				// display recip properties
				paramtype = X400_att.X400_S_FREE_FORM_NAME;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for Free form name(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println("Free form name" + "(" + len + ")" + ret_value.toString());
				}

				// display recip properties
				paramtype = X400_att.X400_S_TELEPHONE_NUMBER;
				status = com.isode.x400api.X400ms.x400_ms_recipgetstrparam(recip_obj, paramtype, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for Telephone Number(" + paramtype + ") failed " + status);
				} else {
					len = ret_value.length();
					System.out.println("Telephone Number" + "(" + len + ")" + ret_value.toString());
				}
			}
			
			msgBoxRecipients.add(msgBoxRecipient);
		}

//		return X400_att.X400_E_NOERROR;
		return msgBoxRecipients;
	}
	public static int get_ms_redihist(MSMessage msmessage_obj, Recip recip_obj, int entry, RediHist redihist_obj) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();
		int ret_int = 0;

		if (msmessage_obj == null) {
			status = com.isode.x400api.X400.x400_redihistget(recip_obj, entry, redihist_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("no redirection history found \n" + " " + status);
				return status;
			}
		} else {
			status = com.isode.x400api.X400ms.x400_ms_redihistgetenv(msmessage_obj, entry, redihist_obj);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("no redirection history found \n" + " " + status);
				return status;
			}
		}
		System.out.println("Redirection History found " + " " + status);

		status = com.isode.x400api.X400.x400_redihistgetstrparam(redihist_obj, X400_att.X400_S_REDIRECTION_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for redirection history redirection time " + status);
		} else {
			System.out.println("redirection history redirection time: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_redihistgetstrparam(redihist_obj, X400_att.X400_S_OR_ADDRESS, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for redirection history redirection address " + status);
		} else {
			System.out.println("redirection history redirection address: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_redihistgetstrparam(redihist_obj, X400_att.X400_S_DIRECTORY_NAME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for redirection history redirection DN " + status);
		} else {
			System.out.println("redirection history redirection DN: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_redihistgetintparam(redihist_obj, X400_att.X400_N_REDIRECTION_REASON);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for redirection history redirection reason" + status);
		} else {
			ret_int = redihist_obj.GetIntValue();
			System.out.println("Got redirection history redirection reason: " + ret_int);
		}

		return X400_att.X400_E_NOERROR;
	}
	
	public static int get_ms_traceinfo(MSMessage msmessage_obj, int entry, Traceinfo traceinfo_obj, int type) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();
		int ret_int = 0;

		status = com.isode.x400api.X400ms.x400_ms_traceinfoget(msmessage_obj, entry, traceinfo_obj, type);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no trace info found \n" + " " + status);
			return status;
		}
		System.out.println("Traceinfo found " + " " + status);

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_GLOBAL_DOMAIN_ID,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info GDI " + status);
		} else {
			System.out.println("Trace info GDI: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_ARRIVAL_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info arrival time " + status);
		} else {
			System.out.println("Trace info arrival time: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_ATTEMPTED_DOMAIN,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info attempted domain " + status);
		} else {
			System.out.println("Trace info attempted domain: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_AA_DEF_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info deferred delivery time " + status);
		} else {
			System.out.println("Trace info deferred delivery time : " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_AA_CEIT,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info CEIT" + status);
		} else {
			System.out.println("Trace info CEIT : " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_ROUTING_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for routing action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info routing action: " + ret_int);
		}

		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_AA_REDIRECTED);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for redirection" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info redirected: " + ret_int);
		}

		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_AA_DLOPERATION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for dloperation" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info dloperation: " + ret_int);
		}

		return X400_att.X400_E_NOERROR;
	}

//		1. /PRMD=THAILAND/ADMD=ICAO/C=XX/, Relayed, 17-03-29 07:21:08Z
	public static String get_ms_traceinfo_text(MSMessage msmessage_obj, int entry, Traceinfo traceinfo_obj, int type) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();
		int ret_int = 0;

		String traceinfo = "";
		status = com.isode.x400api.X400ms.x400_ms_traceinfoget(msmessage_obj, entry, traceinfo_obj, type);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no trace info found \n" + " " + status);
			return "";
		}
		System.out.println("Traceinfo found " + " " + status);

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_GLOBAL_DOMAIN_ID,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info GDI " + status);
		} else {
			System.out.println("Trace info GDI: " + ret_value.toString());
			traceinfo = entry + ". " + ret_value.toString();
		}
		
		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_ROUTING_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for routing action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info routing action: " + ret_int);
			
			// Adding these from internal trace info
			if (ret_int == X400_att.X400_MTA_SI_ROUTING_ACTION_RELAYED) {
				System.out.println("SI routing action " + entry + "  :relayed");
				traceinfo = traceinfo + ", Relayed";
			} else {
				System.out.println("SI routing action " + entry + "  :rerouted");
				traceinfo = traceinfo + ", Rerouted";
			}
		}


		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_ARRIVAL_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info arrival time " + status);
		} else {
			System.out.println("Trace info arrival time: " + ret_value.toString());
			
            DateFormat originaldf = new SimpleDateFormat("yyMMddHHmmss"); 
            originaldf.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            //17-03-29 07:21:08Z
            DateFormat converteddf = new SimpleDateFormat("yy-MM-dd HH:mm:ss"); 
            converteddf.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            String convertedArrival = "";
			try {
				String arrival = ret_value.toString().substring(0, ret_value.length()-1);
				Date arrivaltime = originaldf.parse(arrival);
				convertedArrival = converteddf.format(arrivaltime)+"Z";
			} catch (ParseException e) {
				e.printStackTrace();
			}

			traceinfo = traceinfo + ", " + convertedArrival;
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_ATTEMPTED_DOMAIN,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info attempted domain " + status);
		} else {
			System.out.println("Trace info attempted domain: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_AA_DEF_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info deferred delivery time " + status);
		} else {
			System.out.println("Trace info deferred delivery time : " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetstrparam(traceinfo_obj, X400_att.X400_S_DSI_AA_CEIT,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for trace info CEIT" + status);
		} else {
			System.out.println("Trace info CEIT : " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_AA_REDIRECTED);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for redirection" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info redirected: " + ret_int);
		}

		status = com.isode.x400api.X400.x400_traceinfogetintparam(traceinfo_obj, X400_att.X400_N_DSI_AA_DLOPERATION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for dloperation" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info dloperation: " + ret_int);
		}

		return traceinfo;
	}
	
	public static int get_ms_internaltraceinfo(MSMessage msmessage_obj, int entry, InternalTraceinfo traceinfo_obj) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();
		int ret_int = 0;

		status = com.isode.x400api.X400ms.x400_ms_internaltraceinfoget(msmessage_obj, entry, traceinfo_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no trace info found \n" + " " + status);
			return status;
		}
		System.out.println("Internal Traceinfo found " + " " + status);

		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
				X400_att.X400_S_GLOBAL_DOMAIN_ID, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info GDI " + status);
		} else {
			System.out.println("Internal Trace info GDI: " + ret_value.toString());
		}

		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_NAME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info MTA Name " + status);
		} else {
			System.out.println("Internal Trace info MTA Name: " + ret_value.toString());
		}

		/* Now the MTA supplied information */

		/* MTA SI Arrival time */
		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_SI_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info MTA SI Time " + status);
		} else {
			System.out.println("Internal Trace info MTA SI Time: " + ret_value.toString());
		}

		/*
		 * MTA SI routing action can either be:
		 * X400_MTA_SI_ROUTING_ACTION_RELAYED or
		 * X400_MTA_SI_ROUTING_ACTION_REROUTED
		 */
		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_ROUTING_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for routing action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Got trace info routing action: ");

			if (ret_int == X400_att.X400_MTA_SI_ROUTING_ACTION_RELAYED) {
				System.out.println("SI routing action " + entry + "  :relayed");
			} else {
				System.out.println("SI routing action " + entry + "  :rerouted");
			}

		}

		/* Now a choice between attempted "mtaname" and "GDI" */

		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_ATTEMPTED_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for attempted action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Internal Trace info attempted action: ");

			if (ret_int == X400_att.X400_MTA_SI_RA_MTA) {
				status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
						X400_att.X400_S_MTA_SI_ATTEMPTED_MTA, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for internal trace info Attempted MTA Name " + status);
				} else {
					System.out.println("Internal Trace info Attempted MTA Name: " + ret_value.toString());
				}

			} else {
				status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
						X400_att.X400_S_MTA_SI_ATTEMPTED_DOMAIN, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for internal trace info Attempted Domain " + status);
				} else {
					System.out.println("Internal Trace info Attempted Domain : " + ret_value.toString());
				}

			}
		}
		/* followed by additional actions */

		/* Additional action: deferred time */
		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
				X400_att.X400_S_MTA_SI_DEFERRED_TIME, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info X400_S_MTA_SI_DEFERRED_TIME " + status);
		} else {
			System.out.println("Internal Trace info additional action deferred time: " + ret_value.toString());
		}

		/* Additional action: CEIT */

		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_SI_CEIT,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info X400_S_MTA_SI_CEIT " + status);
		} else {
			System.out.println("Internal Trace info additional action CEIT: " + ret_value.toString());
		}

		/* Additional action: other actions */

		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_OTHER_ACTIONS);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for other action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info other action: " + ret_int);
		}

		if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_REDIRECTED) > 0) {
			System.out.println("SI Other Action " + entry + "  : redirected");
		}

		if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_DLOPERATION) > 0) {
			System.out.println("SI Other Action " + entry + " : DL operation");
		}

		return X400_att.X400_E_NOERROR;
	}
	
//	1. hkamhs : /PRMD=THAILAND/ADMD=ICAO/C=XX/, Relayed, 17-03-29 07:21:08Z
	public static String get_ms_internaltraceinfo_text(MSMessage msmessage_obj, int entry, InternalTraceinfo traceinfo_obj) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();
		int ret_int = 0;

		String internaltraceinfo = "";
		
		status = com.isode.x400api.X400ms.x400_ms_internaltraceinfoget(msmessage_obj, entry, traceinfo_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no trace info found \n" + " " + status);
			return "";
		}
		System.out.println("Internal Traceinfo found " + " " + status);

		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_NAME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info MTA Name " + status);
		} else {
			System.out.println("Internal Trace info MTA Name: " + ret_value.toString());
			internaltraceinfo = entry + ". " + ret_value.toString(); 
		}
		
		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
				X400_att.X400_S_GLOBAL_DOMAIN_ID, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info GDI " + status);
		} else {
			System.out.println("Internal Trace info GDI: " + ret_value.toString());
			internaltraceinfo = internaltraceinfo + " : "+ ret_value.toString(); 
		}

		/*
		 * MTA SI routing action can either be:
		 * X400_MTA_SI_ROUTING_ACTION_RELAYED or
		 * X400_MTA_SI_ROUTING_ACTION_REROUTED
		 */
		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_ROUTING_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for routing action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Got trace info routing action: ");

			if (ret_int == X400_att.X400_MTA_SI_ROUTING_ACTION_RELAYED) {
				System.out.println("SI routing action " + entry + "  :relayed");
				internaltraceinfo = internaltraceinfo + ", Relayed";
			} else {
				System.out.println("SI routing action " + entry + "  :rerouted");
				internaltraceinfo = internaltraceinfo + ", Rerouted";
			}

		}

		/* Now the MTA supplied information */

		/* MTA SI Arrival time */
		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_SI_TIME,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info MTA SI Time " + status);
		} else {
			System.out.println("Internal Trace info MTA SI Time: " + ret_value.toString());
			
            DateFormat originaldf = new SimpleDateFormat("yyMMddHHmmss"); 
            originaldf.setTimeZone(TimeZone.getTimeZone("UTC"));

            DateFormat converteddf = new SimpleDateFormat("yy-MM-dd HH:mm:ss"); 
            converteddf.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            String convertedArrival = "";
			try {
				String arrival = ret_value.toString().substring(0, ret_value.length()-1);
				Date arrivaltime = originaldf.parse(arrival);
				convertedArrival = converteddf.format(arrivaltime)+"Z";
			} catch (ParseException e) {
				e.printStackTrace();
			}

			internaltraceinfo = internaltraceinfo + ", " + convertedArrival;
		}

		/* Now a choice between attempted "mtaname" and "GDI" */

		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_ATTEMPTED_ACTION);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for attempted action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Internal Trace info attempted action: ");

			if (ret_int == X400_att.X400_MTA_SI_RA_MTA) {
				status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
						X400_att.X400_S_MTA_SI_ATTEMPTED_MTA, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for internal trace info Attempted MTA Name " + status);
				} else {
					System.out.println("Internal Trace info Attempted MTA Name: " + ret_value.toString());
				}

			} else {
				status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
						X400_att.X400_S_MTA_SI_ATTEMPTED_DOMAIN, ret_value);
				if (status != X400_att.X400_E_NOERROR) {
					System.out.println("no string value for internal trace info Attempted Domain " + status);
				} else {
					System.out.println("Internal Trace info Attempted Domain : " + ret_value.toString());
				}

			}
		}
		/* followed by additional actions */

		/* Additional action: deferred time */
		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj,
				X400_att.X400_S_MTA_SI_DEFERRED_TIME, ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info X400_S_MTA_SI_DEFERRED_TIME " + status);
		} else {
			System.out.println("Internal Trace info additional action deferred time: " + ret_value.toString());
		}

		/* Additional action: CEIT */

		status = com.isode.x400api.X400.x400_internaltraceinfogetstrparam(traceinfo_obj, X400_att.X400_S_MTA_SI_CEIT,
				ret_value);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for internal trace info X400_S_MTA_SI_CEIT " + status);
		} else {
			System.out.println("Internal Trace info additional action CEIT: " + ret_value.toString());
		}

		/* Additional action: other actions */

		status = com.isode.x400api.X400.x400_internaltraceinfogetintparam(traceinfo_obj,
				X400_att.X400_N_MTA_SI_OTHER_ACTIONS);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no int value for other action" + status);
		} else {
			ret_int = traceinfo_obj.GetIntValue();
			System.out.println("Trace info other action: " + ret_int);
		}

		if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_REDIRECTED) > 0) {
			System.out.println("SI Other Action " + entry + "  : redirected");
		}

		if ((ret_int & X400_att.X400_MTA_SI_OTHER_ACTION_DLOPERATION) > 0) {
			System.out.println("SI Other Action " + entry + " : DL operation");
		}

		return internaltraceinfo;
	}
	/**
	 * Display the Distribution List Expansion History if present,
	 */
	private static int get_ms_dleh(MSMessage msmessage_obj, int entry, DLExpHist dleh_obj) {
		int status, len;
		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();

		// Need the session object to get the error string (why ?)
		// com.isode.x400api.X400ms.x400_ms_get_string_error(session_obj,
		// status));

		status = com.isode.x400api.X400ms.x400_ms_DLexphistget(msmessage_obj, entry, dleh_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no DLEH found \n" + " " + status);
			return status;
		}
		System.out.println("DLEH found " + " " + status);

		// got this DLEH element - print the values
		status = com.isode.x400api.X400.x400_DLgetstrparam(dleh_obj, X400_att.X400_S_OR_ADDRESS, ret_value);

		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("no string value for OR Address in DL  failed " + status);
		} else {
			len = ret_value.length();
			System.out.println("DLExpansion List entry " + "OR Address" + ret_value.toString());
		}

		return X400_att.X400_E_NOERROR;
	}
	
	
	
    /**
	 * Fetch and display a MT bodypart
	 */
	public static Msgbox get_bp(MSMessage msmessage_obj, BodyPart bodypart_obj, int att_num, Msgbox msgBox) {
		int status;
		int int_value;
		int len;
		int bp_type;
		byte[] binarydata = new byte[config.maxlen];

		List<Msgboxattachment> msgBoxAttachments = msgBox.getMsgboxattachments();
		
		System.out.println("----------------");
		System.out.println("Reading MS BodyPart " + att_num);

		// Initialise object for returning string values
		StringBuffer ret_value = new StringBuffer();

		status = com.isode.x400api.X400ms.x400_ms_msggetbodypart(msmessage_obj, att_num, bodypart_obj);
//		if (status != X400_att.X400_E_NOERROR && status != X400_att.X400_E_MESSAGE_BODY) {
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetbodypart failed " + status);
			msgBox.setStatus(status);
			return msgBox;
		}

		// what kind of body part ?
		status = com.isode.x400api.X400.x400_bodypartgetintparam(bodypart_obj, X400_att.X400_N_BODY_TYPE);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_bodypartgetintparam failed " + status);
			msgBox.setStatus(status);
			return msgBox;
		}
		int_value = bodypart_obj.GetIntValue();
		System.out.println("Got BodyPart int value : bp type is " + int_value);
		Message message_obj = new Message();
		
		Msgboxattachment msgBoxAttachment = new Msgboxattachment();
		msgBoxAttachment.setMsgbox(msgBox);

		switch (int_value) {
		case X400_att.X400_T_FWD_CONTENT:
			System.out.println("Got Forwarded Content");
			status = get_msgbp(msmessage_obj, message_obj, att_num, int_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("get_msgbp failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			break;
		case X400_att.X400_T_MESSAGE:
			System.out.println("Got Forwarded Message");

			status = get_msgbp(msmessage_obj, message_obj, att_num, int_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("get_msgbp failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}

			break;
		case X400_att.X400_T_ADATP3:
			/* STANAG 4406 B1.1 ADatP3 */
			System.out.println("Got P772 ADatP3");

			status = com.isode.x400api.X400.x400_bodypartgetintparam(bodypart_obj, X400_att.X400_N_ADATP3_PARM);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetintparam failed " + status);

			} else {
				int_value = bodypart_obj.GetIntValue();
				System.out.println("Got ADatP3 param is:" + int_value);
			}

			status = com.isode.x400api.X400.x400_bodypartgetintparam(bodypart_obj, X400_att.X400_N_ADATP3_CHOICE);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetintparam failed " + status);

			} else {
				int_value = bodypart_obj.GetIntValue();
				System.out.println("Got ADatP3 Choice is:" + int_value);
			}

			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_ADATP3_DATA,
					ret_value, binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			System.out.println("Got ADatP3 data" + ret_value.toString());
			break;

		case X400_att.X400_T_CORRECTIONS:
			/* STANAG 4406 B1.2 Corrections */
			System.out.println("Got P772 Correction");
			status = com.isode.x400api.X400.x400_bodypartgetintparam(bodypart_obj, X400_att.X400_N_CORREC_PARM);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetintparam failed " + status);

			} else {
				int_value = bodypart_obj.GetIntValue();
				System.out.println("Got correction param is:" + int_value);
			}

			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_CORREC_DATA,
					ret_value, binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			System.out.println("Got Correction data" + ret_value.toString());
			break;

		case X400_att.X400_T_FWDENC:
			System.out.println("Got P772 Forwarded encrypted");

			status = get_msgbp(msmessage_obj, message_obj, att_num, int_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("get_msgbp failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			break;

		case X400_att.X400_T_MM:
			System.out.println("Got P772 T MM");

			status = get_msgbp(msmessage_obj, message_obj, att_num, int_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("get_msgbp failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			break;

		case X400_att.X400_T_ACP127DATA:
			System.out.println("Got P772 ACP127 Data");
			status = com.isode.x400api.X400.x400_bodypartgetintparam(bodypart_obj, X400_att.X400_N_ACP127DATA_PARM);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetintparam failed " + status);

			} else {
				int_value = bodypart_obj.GetIntValue();
				System.out.println("Got ACP127 data param is:" + int_value);
			}

			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_ACP127_DATA,
					ret_value, binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			System.out.println("Got ACP127Data" + ret_value.toString());
			break;

		case X400_att.X400_T_BINARY:
		case X400_att.X400_T_FTBP:
			// read the data from the body part

			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_BODY_DATA, ret_value,
					binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}

			bp_type = bodypart_obj.GetBpType();
			len = bodypart_obj.GetBpLen();

			System.out.println("Binary Attachment (" + int_value + "), len = " + len);
//			for (int i = 0; i < len; i++)
//				System.out.print(binarydata[i] + " ");
			
//			String h = bytesToHex(binarydata, len);
//			System.out.println("Hex: "+h);
			msgBoxAttachment.setFilesize(len);
			msgBoxAttachment.setFiletype(int_value);
			
			byte[] fielBytes = Arrays.copyOfRange(binarydata, 0, len);
			
			msgBoxAttachment.setBfile(fielBytes);
			
			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_FTBP_FILENAME, ret_value,
					binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			msgBoxAttachment.setFilename(ret_value.toString());
			msgBoxAttachments.add(msgBoxAttachment);
			break;
			
		case X400_att.X400_T_IA5TEXT:
			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_BODY_DATA, ret_value,
					binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			System.out.println(
					"Got BodyPart string value(" + ret_value.capacity() + "): bp value is \n" + ret_value.toString());
			msgBox.setMsgText(ret_value.toString());
			
			byte[] textBytes = ret_value.toString().getBytes();
			msgBoxAttachment.setFilesize(textBytes.length);
			msgBoxAttachment.setFiletype(int_value);
			msgBoxAttachment.setBfile(textBytes);
			msgBoxAttachment.setFilename("IA5 Text");
			msgBoxAttachments.add(msgBoxAttachment);
			break;
		default:
			// read the data from the body part
			status = com.isode.x400api.X400.x400_bodypartgetstrparam(bodypart_obj, X400_att.X400_S_BODY_DATA, ret_value,
					binarydata);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_bodypartgetstrparam failed " + status);
				msgBox.setStatus(status);
				return msgBox;
			}
			
			System.out.println(
					"Got BodyPart string value(" + ret_value.capacity() + "): bp value is \n" + ret_value.toString());
//			msgBox.setMsg_text(ret_value.toString());
			break;
		}

		msgBox.setStatus(status);
		msgBox.setMsgboxattachments(msgBoxAttachments);
		return msgBox;
	}
	
//	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
//	public static String bytesToHex(byte[] bytes, int len) {
//	    char[] hexChars = new char[bytes.length * 2];
//	    for ( int j = 0; j < len; j++ ) {
//	        int v = bytes[j] & 0xFF;
//	        hexChars[j * 2] = hexArray[v >>> 4];
//	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//	    }
//	    return new String(hexChars);
//	}
	
	public static int get_msgbp(MSMessage msmessage_obj, Message message_obj, int att_num, int type) {
		int status;
		int int_value;

		int len;
		String value;

		System.out.println("------------------------");
		System.out.println("Reading Message BodyPart " + att_num);
		// get the message body part from the MTMessage
		status = com.isode.x400api.X400ms.x400_ms_msggetmessagebody(msmessage_obj, att_num, message_obj);
		if (status != X400_att.X400_E_NOERROR) {
			System.out.println("x400_ms_msggetmessagebody failed " + status);
			return status;
		}

		StringBuffer ret_value = new StringBuffer();
		System.out.println("get_msgbp " + type);
		switch (type) {
		case X400_att.X400_T_MM:
			System.out.println("Fetched P772 Military Message bodypart");

			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
					ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("MM bodypart delivery time " + ret_value.toString());
			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_SUBJECT, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("MM subject: " + ret_value.toString());
			break;
		case X400_att.X400_T_FWDENC:
			System.out.println("Fetched P772 Forwarded encrypted bodypart");
			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
					ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("P772 forwarded encrypted delivery time " + ret_value.toString());

			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_ENCRYPTED_DATA, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("P772 forwarded encrypted data: " + ret_value.length() + " Bytes");

			break;
		case X400_att.X400_T_FWD_CONTENT:
			System.out.println("Fetched Forwarded content bodypart");
			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_MESSAGE_DELIVERY_TIME,
					ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("Forwarded Content delivery time " + ret_value.toString());

			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_MESSAGE_IDENTIFIER,
					ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("Forwarded Content bodypart message id: " + ret_value.toString());

			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_FWD_CONTENT_STRING,
					ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			System.out.println("Forwarded Content bodypart message id: " + ret_value.length());

			break;

		default:
			// Initialise object to receive returned String value

			System.out.println("Read Message BodyPart successfully: " + type);

			// we've got the message body part OK, find out what it is ...
			status = com.isode.x400api.X400.x400_msggetstrparam(message_obj, X400_att.X400_S_OBJECTTYPE, ret_value);
			if (status != X400_att.X400_E_NOERROR) {
				System.out.println("x400_msggetstrparam failed " + status);
				return status;
			}
			// get the returned values, and display it
			len = ret_value.length();
			// value = message_obj.GetStrValue();
			value = ret_value.toString();
			System.out.println("Got BodyPart string object type successfully ");
			if (value.equals("message")) {
				System.out.println("++++++++++++++++++++++++++++++++++++++");
				System.out.println("Message body part (ie forwarded message)");
				// no envelope for a forwarded msg
				// status = X400msTestRcvNativeUtils.do_xmsg_env(message_obj);
				status = RcvNativeUtils.do_xmsg_headers(message_obj);
				/*
				 * the content can be retrieved as attachments or bodyparts. Use
				 * do_xmsg_content for the former
				 */
				status = RcvNativeUtils.do_xmsg_content_as_bp(message_obj);
				// status = do_xmsg_content(message_obj);
				System.out.println("++++++++++++++++++++++++++++++++++++++");
			} else if (value.equals("report")) {
				System.out.println("Retrieved report body part");
				status = RepRcvUtils.do_rep_env(msmessage_obj);
				status = RepRcvUtils.do_rep_content(msmessage_obj);
				status = RepRcvUtils.do_rep_retcontent(msmessage_obj);
			} else if (value.equals("probe")) {
				// Not handling a probe here
				System.out.println("Retrieved probe body part");
				return X400_att.X400_E_NYI;
			} else {
				// unknown type
				System.out.println("Retrieved unknown message  body part type " + value);
				return X400_att.X400_E_BADPARAM;
			}

		}
		return X400_att.X400_E_NOERROR;

	}
}