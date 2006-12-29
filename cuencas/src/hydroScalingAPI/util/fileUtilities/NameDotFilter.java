/*
 * NameFilter.java
 *
 * Created on June 15, 2003, 2:05 PM
 */

package hydroScalingAPI.util.fileUtilities;

/**
 *
 * @author Ricardo Mantilla
 */
public class NameDotFilter extends Object implements java.io.FileFilter{
    
    String myName;
    String myExt;
    
    /** Creates a new instance of NameFilter */
    public NameDotFilter(String name,String ext) {
        myName=name;
        myExt=ext.toLowerCase();
    }
    
    public boolean accept(java.io.File file) {
        return file.getName().toLowerCase().lastIndexOf("."+myExt) != -1 && 
               file.getName().lastIndexOf(myName) != -1;
    }
    
}
