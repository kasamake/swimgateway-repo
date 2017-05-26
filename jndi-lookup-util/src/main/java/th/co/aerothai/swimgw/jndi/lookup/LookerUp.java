package th.co.aerothai.swimgw.jndi.lookup;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class LookerUp
{
  private Properties prop = new Properties();
  private String jndiPrefix;
  
  public LookerUp()
  {
    this.prop.put("java.naming.factory.url.pkgs", "org.jboss.ejb.client.naming");
  }
  
  public Object findLocalSessionBean(String moduleName, String beanName, String interfaceFullQualifiedName)
    throws NamingException
  {
    Context context = new InitialContext(this.prop);
    Object object = context.lookup("java:global/" + moduleName + "/" + beanName + "!" + interfaceFullQualifiedName);
    context.close();
    
    return object;
  }
  
  public Object findSessionBean(String jndiName)
    throws NamingException
  {
    Context context = new InitialContext(this.prop);
    Object object = context.lookup(jndiName);
    context.close();
    
    return object;
  }
}
