package th.co.aerothai.swimgw.services.x400;

import com.isode.x400api.MSMessage;
import com.isode.x400api.Recip;
import com.isode.x400api.RediHist;
import com.isode.x400api.X400ms;
import java.io.PrintStream;
import th.co.aerothai.swimgw.models.Msgbox;

public class RepRcvUtils
{
  public static int do_rep_env(MSMessage msmessage_obj)
  {
    System.out.println("---------------");
    System.out.println("Report Envelope");
    

    StringBuffer ret_value = new StringBuffer();
    

    int paramtype = 1;
    int status = X400ms.x400_ms_msggetstrparam(
      msmessage_obj, paramtype, ret_value);
    if (status != 0)
    {
      System.out.println("no string value for originator address(" + paramtype + ") failed " + status + " (as expected for a report)");
    }
    else
    {
      int len = ret_value.length();
      System.out.println("Originator: (" + len + ")" + ret_value.toString());
    }
    System.out.println("get envelope redihist");
    RediHist redihist_obj = new RediHist();
    int entry = 1;
    status = 0;
    while (status == 0)
    {
      status = RcvUtils.get_ms_redihist(msmessage_obj, 
        null, 
        entry, 
        redihist_obj);
      if (status != 0)
      {
        System.out.println("get_ms_redihist failed " + status);
        break;
      }
      entry++;
    }
    return 0;
  }
  
  public static int do_rep_content(MSMessage msmessage_obj)
  {
    int recip_num = 1;
    int len = -1;int maxlen = -1;
    


    System.out.println("-------------");
    System.out.println("Report Content");
    

    StringBuffer ret_value = new StringBuffer();
    

    int paramtype = 600;
    int status = X400ms.x400_ms_msggetstrparam(
      msmessage_obj, paramtype, ret_value);
    if (status != 0)
    {
      System.out.println("no string value for Subject ID(" + paramtype + ") failed " + status);
    }
    else
    {
      len = ret_value.length();
      System.out.println("Subject Identifier: (" + len + ")" + ret_value.toString());
    }
    Recip recip_obj = new Recip();
    for (recip_num = 1;; recip_num++)
    {
      status = X400ms.x400_ms_recipget(msmessage_obj, 
        65544, recip_num, recip_obj);
      if (status == 32)
      {
        System.out.println("no more recips ...");
        break;
      }
      if (status != 0)
      {
        System.out.println("x400_ms_recipget failed " + status);
        break;
      }
      System.out.println("-------------- Recipient " + recip_num + "--------------");
      
      paramtype = 1;
      status = X400ms.x400_ms_recipgetstrparam(
        recip_obj, paramtype, ret_value);
      if (status != 0)
      {
        System.out.println("no string value for originator address(" + paramtype + ") failed " + status);
      }
      else
      {
        len = ret_value.length();
        System.out.println("Subject Recipient " + recip_num + ": (" + len + ")" + ret_value.toString());
      }
      paramtype = 111;
      status = X400ms.x400_ms_recipgetstrparam(
        recip_obj, paramtype, ret_value);
      if (status != 0)
      {
        System.out.println("no string value for Subject Message Delivery Time(" + paramtype + ") failed " + status);
      }
      else
      {
        len = ret_value.length();
        System.out.println("Subject Message Delivery Time:(" + len + ")" + ret_value.toString());
      }
      paramtype = 611;
      status = X400ms.x400_ms_recipgetintparam(
        recip_obj, paramtype);
      if (status != 0)
      {
        System.out.println("no int value for Type of MTS user(" + paramtype + ") failed " + status);
      }
      else
      {
        int int_value = recip_obj.GetIntValue();
        System.out.println("Type of MTS user " + int_value);
      }
      paramtype = 610;
      status = X400ms.x400_ms_recipgetstrparam(
        recip_obj, paramtype, ret_value);
      if (status != 0)
      {
        System.out.println("no string value for Supplementary info(" + paramtype + ") failed " + status);
      }
      else
      {
        len = ret_value.length();
        System.out.println("Supplementary info(" + len + ")" + ret_value.toString());
      }
      paramtype = 613;
      status = X400ms.x400_ms_recipgetintparam(
        recip_obj, paramtype);
      if (status != 0)
      {
        System.out.println("no int value for Non-delivery Reason(" + paramtype + ") failed " + status);
      }
      else
      {
        int int_value = recip_obj.GetIntValue();
        System.out.println("Non-delivery Reason  " + int_value);
      }
      paramtype = 614;
      status = X400ms.x400_ms_recipgetintparam(
        recip_obj, paramtype);
      if (status != 0)
      {
        System.out.println("no int value for Non-delivery diagnostic(" + paramtype + ") failed " + status);
      }
      else
      {
        int int_value = recip_obj.GetIntValue();
        System.out.println("Non-delivery diagnostic  " + int_value);
      }
    }
    return 0;
  }
  
  public static int do_rep_retcontent(MSMessage msmessage_obj)
  {
    int recip_num = 1;
    int len = -1;int maxlen = -1;
    


    System.out.println("-----------------------");
    System.out.println("Report Returned Content");
    


    return BodyPartRcvUtils.do_msg_content_as_bp(msmessage_obj, null).getStatus();
  }
}