/*
 * extensionToFormat.java
 *
 * Created on June 28, 2003, 11:44 AM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author Ricardo Mantilla
 */
public abstract class ExtensionToFormat {
    
    public static String getFormat(String extension){
        java.util.Hashtable extToFormat=new java.util.Hashtable();

        extToFormat.put(".areas","Float");
        extToFormat.put(".corrDEM","Double");
        extToFormat.put(".dir","Byte");
        extToFormat.put(".dtopo","Integer");
        extToFormat.put(".gdo","Float");
        extToFormat.put(".horton","Byte");
        extToFormat.put(".lcp","Float");
        extToFormat.put(".ltc","Float");
        extToFormat.put(".mcd","Float");
        extToFormat.put(".magn","Integer");
        extToFormat.put(".redRas","Byte");
        extToFormat.put(".slope","Double");
        extToFormat.put(".tdo","Integer");
        extToFormat.put(".tcd","Float");
        
        return (String)extToFormat.get(extension);
    }
    
}
