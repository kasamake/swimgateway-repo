package th.co.aerothai.swimgw.services.amhs;

import com.isode.x400api.X400_att;

public class config
{
  public static final String p7_bind_dn = "c=TH";
  public static final String p7_credentials = "secret";
  public static final String p7_pa = "\"3001\"/URI+0000+URL+itot://172.16.21.134:3001";
  public static final int maxlen = 600000;
  public static final String p7_bind_swim = "CN=VTBBSWIM/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/";
  public static final String p7_bind_yuaa = "CN=VTBBYUAA/OU=VTBB/O=VTBB/PRMD=THAILAND/ADMD=ICAO/C=XX/";
  
  public static final int ia5text = X400_att.X400_T_IA5TEXT;
}
