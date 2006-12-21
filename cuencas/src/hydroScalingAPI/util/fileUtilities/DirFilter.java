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
public class DirFilter extends Object implements java.io.FileFilter{

    /** Creates new filtroExtenciones */
    public DirFilter() {
    }

    public boolean accept(final java.io.File p1) {
        return p1.isDirectory();
    }
}