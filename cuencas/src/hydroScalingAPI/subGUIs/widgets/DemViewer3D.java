/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/*
 * DemViewer3D.java
 *
 * Created on June 20, 2003, 10:21 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author Ricardo Mantilla
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
