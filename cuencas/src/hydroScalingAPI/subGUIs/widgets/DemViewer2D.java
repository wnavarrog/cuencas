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
 * DemViewer.java
 *
 * Created on June 20, 2003, 2:34 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author Ricardo Mantilla
 */
public class DemViewer2D extends hydroScalingAPI.subGUIs.widgets.RasterViewer implements visad.DisplayListener{
    
    private visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
    
    /** Creates a new instance of DemViewer */
    public DemViewer2D(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws java.rmi.RemoteException, visad.VisADException, java.io.IOException{
        super(parent,md,relMaps);
        
        setTitle(metaData.toString());
        localField=metaData.getField();
        
        dr=new  visad.java3d.TwoDDisplayRendererJ3D();
        display = new visad.java3d.DisplayImplJ3D("disp",dr);
        
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
        
        colorScaleMap=new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
        colorScaleMap.setRange(0,255);
        display.addMap(colorScaleMap);
        
        //If metaDEM & already processed enable DEM Tools
        String pathToNetwork=metaData.getLocationBinaryFile().getPath();
        pathToNetwork=pathToNetwork.substring(0,pathToNetwork.lastIndexOf("."))+".stream";

        boolean isProcessed=new java.io.File(pathToNetwork).exists();
        if (isProcessed && metaData.getLocationMeta().getName().lastIndexOf(".metaDEM") != -1){
            updateNetworkPopupMenu();
            updateBasinsPopupMenu();
            demToolsEnable(true);
            java.io.File originalFile=metaData.getLocationBinaryFile();
            String originalFormat=metaData.getFormat();
            
            metaData.setLocationBinaryFile(new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".dir"));
            metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
            
            fullDirMatrix=new hydroScalingAPI.io.DataRaster(metaData).getByte();
            
            metaData.setLocationBinaryFile(originalFile);
            metaData.setFormat(originalFormat);
            
        } else {
            demToolsEnable(false);
        }
        
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
    
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {
        
        int id = DispEvt.getId();
        
        if (activeEvent == 0){
        
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_PRESSED_CENTER){

                    //Initialize Box Zoom
                            
                }
            } catch (Exception e) {
                System.err.println(e);
            }

            try {
                if (DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER) {

                    //End Box Zoom
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 1){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                    int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                    
                    traceBasinContour(MatX, MatY, true);
                    
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 2){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                    int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                    
                    traceRiverPath(MatX, MatY);                    
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 3){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    hydroScalingAPI.subGUIs.widgets.LocationsEditor theEditor=new hydroScalingAPI.subGUIs.widgets.LocationsEditor(mainFrame);
                    theEditor.setLatLong(resultY,resultX);
                    theEditor.setVisible(true);

                    mainFrame.addNewLocationInteractively(theEditor);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        try {
            if (id == DispEvt.MOUSE_MOVED) {
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                
                
                setLongitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX,1)+" ["+MatX+"]");
                setLatitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY,0)+" ["+MatY+"]");
                visad.RealTuple spotValue=(visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                /*java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
                java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
                dpoint4.applyPattern("0.00000000000000000000000");*/
                
                setValueLabel(""+spotValue.getValues()[0]);
                
            }
            
        } catch (Exception e) {
            System.err.println(e);
        }
        
    }
    
    
    
}
