/*
 * dotFilter.java
 *
 * Created on 3 de diciembre de 2000, 17:20
 */

package hydroScalingAPI.util.fileUtilities;

/**
 *
 * @author  Ricardo Mantilla
 */
public class DotFilter extends Object implements java.io.FileFilter{

    String[] myExt;
    
    /** Creates new filtroExtenciones */
    public DotFilter(String ext) {
        myExt=ext.toLowerCase().split(",");
    }

    public boolean accept(final java.io.File p1) {
        boolean extExists=false;
        for(int i=0;i<myExt.length;i++) extExists|=p1.getName().toLowerCase().lastIndexOf("."+myExt[i]) != -1;
        return extExists;
    }
}