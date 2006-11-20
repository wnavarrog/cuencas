/*
 * DemViewer3D.java
 *
 * Created on June 20, 2003, 10:21 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author  ricardo
 */
public class DemViewer3D extends hydroScalingAPI.subGUIs.widgets.RasterViewer implements visad.DisplayListener {
    
    /** Creates a new instance of DemViewer3D */
    public DemViewer3D(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws java.rmi.RemoteException, visad.VisADException{
        super(parent,md,relMaps);
        
        display=new visad.java3d.DisplayImplJ3D("disp");
        
        getContentPane().add("Center",display.getComponent());
        
        show();
        toFront();
    }
    
    /** send a DisplayEvent to this DisplayListener  */
    public void displayChanged(visad.DisplayEvent e) throws visad.VisADException, java.rmi.RemoteException {
    }
    
}
