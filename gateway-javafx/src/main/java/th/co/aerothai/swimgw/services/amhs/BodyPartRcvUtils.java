package th.co.aerothai.swimgw.services.amhs;


import com.isode.x400api.BodyPart;
import com.isode.x400api.MSMessage;
import com.isode.x400api.Message;
import th.co.aerothai.swimgw.models.Msgbox;

import com.isode.x400api.*;


public class BodyPartRcvUtils
{

    /**
     * Display the MS Message content (ie the attachments/bodyparts) of 
     * a message retrieved from the message store.
     */
    public static Msgbox do_msg_content_as_bp(MSMessage msmessage_obj, Msgbox msgBox)
    {
	int status;
	int paramtype;
	int int_value;
	int len;
	String value;
	int att_num;
	byte[] binarydata = new byte[config.maxlen];
	int att_type;
	int att_len;

	System.out.println("------");
	System.out.println("Content");

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();

	paramtype = X400_att.X400_N_NUM_ATTACHMENTS;
    	status = com.isode.x400api.X400ms.x400_ms_msggetintparam(
	    msmessage_obj, paramtype);
	if (status == X400_att.X400_E_NO_VALUE) {
	    System.out.println("no int value for " + paramtype);
	}
	else if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_ms_msggetintparam (" 
		+ X400_att.X400_N_NUM_ATTACHMENTS + ") failed " + status);
	    msgBox.setStatus(status);
	    return msgBox;
	} 
	int_value = msmessage_obj.GetIntValue();
	System.out.println("number of attachments " + int_value);

	if ( int_value == 0) {
	    System.out.println("No attachments");
	    msgBox.setStatus(X400_att.X400_E_NOERROR);
	    return msgBox;
	}
		
	// Get all the bodyparts..
	BodyPart bodypart_obj = new BodyPart();
	Message message_obj = new Message();
	for ( att_num = 0; ; att_num++ ) {
	    msgBox = RcvUtils.get_bp(msmessage_obj, 
	    		bodypart_obj, att_num, msgBox);
	    status = msgBox.getStatus();
	    
	    if (status == X400_att.X400_E_MESSAGE_BODY) {
	    	// need to get it as a message bodypart
			System.out.println("BodyPart " + att_num + " is a message");
		    	status = get_msgbp(msmessage_obj, message_obj, att_num);
			if (status != X400_att.X400_E_NOERROR) {
			    System.out.println("Failed getting msg BP");
			    msgBox.setStatus(status);
			    return msgBox;
			}
	    } else if (status == X400_att.X400_E_MISSING_ATTR) {
		System.out.println("No more bodyparts");
		break;
	    } else if (status != X400_att.X400_E_NOERROR) {
	    	msgBox.setStatus(status);
		    return msgBox;
	    }
	}

        System.out.println("Read " + att_num + " bodyparts");
        System.out.println("------------------------");
	    msgBox.setStatus(X400_att.X400_E_NOERROR);
	    return msgBox;
    }

    /**
     * Fetch and display a MS Message bodypart  (ie a message retrieved 
     * the message store).
     */
    public static int get_msgbp(MSMessage msmessage_obj, Message message_obj, int att_num)
    {
	int status;
	int int_value;
	int type;
	int len;
	String value;

	System.out.println("------------------------");
	System.out.println("Reading Message BodyPart " + att_num);
	// get the message body part from the MSMessage
	status = com.isode.x400api.X400ms.x400_ms_msggetmessagebody(
	    msmessage_obj, att_num, message_obj);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_ms_msggetmessagebody failed " + status);
	    return status;
	}

	// Initialise object to receive returned String value
	StringBuffer ret_value = new StringBuffer();

	System.out.println("Read Message BodyPart successfully");

	// we've got the message body part OK, find out what it is ...
	status = com.isode.x400api.X400.x400_msggetstrparam(
                  message_obj, X400_att.X400_S_OBJECTTYPE, ret_value);
	if (status != X400_att.X400_E_NOERROR) {
	    System.out.println("x400_msggetstrparam failed " + status);
	    return status;
	}
	// get the returned values, and display it
	len = ret_value.length();
	//value = message_obj.GetStrValue();
	value = ret_value.toString();
	System.out.println("Got BodyPart string object type successfully ");
	if (value.equals("message")) {
	    System.out.println("++++++++++++++++++++++++++++++++++++++");
            System.out.println("Message body part (ie forwarded message)");
	    // no envelope for a forwarded msg
            // status = X400msTestRcvNativeUtils.do_xmsg_env(message_obj);
            status = RcvNativeUtils.do_xmsg_headers(message_obj);
	    /* the content can be retrieved as attachments or
	     * bodyparts. Use do_xmsg_content for the former */
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
	return X400_att.X400_E_NOERROR;

    }
}

