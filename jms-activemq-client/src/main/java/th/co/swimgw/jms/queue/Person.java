package th.co.swimgw.jms.queue;

import java.io.Serializable;

public class Person implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -2749977649306134186L;
     
    private String name;
    private int age;
 
    public Person(String name, int age)
    {
        super();
        this.name = name;
        this.age = age;
    }
 
    public String getName()
    {
        return name;
    }
 
    public void setName(String name)
    {
        this.name = name;
    }
 
    public int getAge()
    {
        return age;
    }
 
    public void setAge(int age)
    {
        this.age = age;
    }
 
    @Override
    public String toString()
    {
        return String.format("Person [name=%s, age=%s]", name, age);
    }
 
}