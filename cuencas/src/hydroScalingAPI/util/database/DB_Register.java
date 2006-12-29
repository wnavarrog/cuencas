/*
 * DB_Register.java
 *
 * Created on June 16, 2003, 1:34 AM
 */

package hydroScalingAPI.util.database;

/**
 *
 * @author Ricardo Mantilla
 */
public class DB_Register {
    
    java.util.Hashtable properties=new java.util.Hashtable();
    
    public DB_Register (String[] Fields,Object[] Values) {
        
        for (int i=0;i<Fields.length;i++){
            properties.put(Fields[i],Values[i]);
        }
        
    }
    
    public Object getProperty(String propName){
        return properties.get(propName);
    }
    
    public String toString(){
        return properties.toString();
    }
    
}