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
    public DemViewer3D(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws java.rmi.RemoteException, visad.VisADException, java.io.IOException{
        super(parent,md,relMaps);
        
        setTitle(metaData.toString());
        localField=metaData.getField();
        
        display = new visad.java3d.DisplayImplJ3D("disp");
        dr=(visad.java3d.DisplayRendererJ3D)display.getDisplayRenderer();
        
        visad.GraphicsModeControl dispGMC = (visad.GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        visad.ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, (double)metaData.getNumRows()/(double)metaData.getNumCols()});
        
        latitudeMap=new visad.ScalarMap(visad.RealType.Latitude, visad.Display.YAxis);
        latitudeMap.getAxisScale().setFont(font);
        latitudeMap.setRange(metaData.getMinLat(),metaData.getMaxLat());
        display.addMap(latitudeMap);
        
        longitudeMap=new visad.ScalarMap(visad.RealType.Longitude, visad.Display.XAxis);
        longitudeMap.getAxisScale().setFont(font);
        longitudeMap.setRange(metaData.getMinLon(),metaData.getMaxLon());
        display.addMap(longitudeMap);
        
        float[][] dataValues=localField.getFloats();
        hydroScalingAPI.tools.Stats statsVar=new hydroScalingAPI.tools.Stats(dataValues[0]);
        
        heightMap=new visad.ScalarMap(visad.RealType.Altitude, visad.Display.ZAxis);
        heightMap.getAxisScale().setFont(font);
        heightMap.setRange(statsVar.minValue,statsVar.maxValue+2*(statsVar.maxValue-statsVar.minValue));
        display.addMap(heightMap);
        
        
        colorScaleMap=new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
        colorScaleMap.setRange(0,255);
        display.addMap(colorScaleMap);
        
        demToolsEnable(false);
        
        visad.TextType t = visad.TextType.getTextType("text");
        visad.ScalarMap tmap=new visad.ScalarMap(t, visad.Display.Text);
        display.addMap(tmap);
        
        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setSize(0.1);
        tcontrol.setFont(font);
        
        display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display.addDisplayListener(this);
        
        display.getComponent().addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int rot = e.getWheelRotation();
                try{
                    visad.ProjectionControl pc = display.getProjectionControl();
                    double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                            1.0, 1.0, 1.0,
                            0.0, 0.0, 0.0);
                    double[] currentMatrix = pc.getMatrix();
                    // Zoom in
                    if (rot < 0){
                        scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                        1.1, 1.1, 1.1,
                                                                        0.0, 0.0, 0.0);
                    }
                    // Zoom out
                    if (rot > 0){
                        scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                        0.9, 0.9, 0.9,
                                                                        0.0, 0.0, 0.0);
                    }
                    scaleMatrix = dr.getMouseBehavior().multiply_matrix(scaleMatrix,currentMatrix);
                    pc.setMatrix(scaleMatrix);
                } catch (java.rmi.RemoteException re) {} catch (visad.VisADException ve) {}
            }
        });

        
        
        this.getContentPane().add("Center",display.getComponent());
        
        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes=new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);
                    
        //If DEM load the Elevations Color Table
        if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1)
            availablePalettes.setSelectedTable("Elevations");
        else
            availablePalettes.setSelectedTable("Rainbow");
                    
        super.refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
        
        show();
        toFront();
        updateUI();
    }
    
    /** send a DisplayEvent to this DisplayListener  */
    public void displayChanged(visad.DisplayEvent e) throws visad.VisADException, java.rmi.RemoteException {
    }
    
}
