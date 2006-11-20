/*
 * SelectWhitewatersDLGs.java
 *
 * Created on March 30, 2004, 11:01 AM
 */

package hydroScalingAPI.examples.dlgs;

/**
 *
 * @author  ricardo
 */
public class SelectWhitewatersDLGs {
    
    /** Creates a new instance of SelectWhitewatersDLGs */
    public SelectWhitewatersDLGs(String location) {
        
        java.io.File[] filesToLookAt=new java.io.File(location).listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("dlg"));
        
        for(int i=0;i<filesToLookAt.length;i++){
            try{
                hydroScalingAPI.io.MetaDLG metaDLG1=new hydroScalingAPI.io.MetaDLG (filesToLookAt[i].getPath(),new java.awt.geom.Rectangle2D.Double(-97-19/60.,37+39/60.,1,1));
            } catch (java.io.IOException IOE){
                System.err.println("BAD FILE: "+filesToLookAt[i].getName());
            } catch (visad.VisADException vie){
                System.err.println("BAD FILE: "+filesToLookAt[i].getName());
            }
        }
        
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String locationDir="/home/ricardo/garbage/gisdasc.kgs.ukans.edu/gisdata/dlg/dlg_100k/roads";
        new SelectWhitewatersDLGs(locationDir);
    }
    
}
