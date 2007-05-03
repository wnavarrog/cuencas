/*
 * TRIBS_io.java
 *
 * Created on January 24, 2007, 1:27 PM
 */

package hydroScalingAPI.modules.tRIBS_io.widgets;


import java.io.IOException;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;
    
/**
 * This module is a graphical interface for creating input and analyzing output
 * for/from the tRIBS program developed at MIT
 * @author Ricardo Mantilla
 */
public class TRIBS_io extends javax.swing.JDialog  implements visad.DisplayListener{
    
    private RealType    posIndex=RealType.getRealType("posIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("nodeColor"),
                        voiColor=RealType.getRealType("voiColor");
    
    private RealTupleType   espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor}),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private DisplayImplJ3D display_TIN_I,display_TIN_Os,display_TIN_Od;
    private ScalarMap eastMap_I,northMap_I,pointsMap_I,voiColorMap_I,
                      eastMap_O,northMap_O,pointsMap_O,voiColorMap_O;
    
    private hydroScalingAPI.util.plot.XYJPanel PpanelRTF;
    private hydroScalingAPI.util.plot.XYJPanel PpanelQOUT;
    private hydroScalingAPI.util.plot.XYJPanel PpanelMRF;
    private hydroScalingAPI.util.plot.XYJPanel PpanelPixel;
    
    private visad.java3d.DisplayRendererJ3D drI,drO;
    
    private DataReferenceImpl data_refPoints_I,data_refTr_I,data_refPoly_I,data_refFill_I,
                              data_refPoints_O,data_refTr_O,data_refPoly_O,data_refFill_O;
    
    private ConstantMap[] pointsCMap_I,trianglesCMap_I,polygonsCMap_I,
                          pointsCMap_O,trianglesCMap_O,polygonsCMap_O;
    
    private hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN basTIN_I,basTIN_O;
    
    private hydroScalingAPI.modules.tRIBS_io.objects.FileDynamicManager fdm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileIntegratedManager fim;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileMrfManager fmrfm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FilePixelManager fpm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileQoutManager fqm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileRftManager frftm;
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @param x The column number of the basin outlet location
     * @param y The row number of the basin outlet location
     * @param direcc The direction matrix associated to the DEM where the basin is embeded
     * @param magnit The magnitudes matrix associated to the DEM where the basin is embeded
     * @param md The MetaRaster associated to the DEM where the basin is embeded
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, int[][] magnit, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        this(parent);
        basTIN_I=new hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN(x,y,direcc,magnit,md);
        initializeInputTabs();
    }
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @param outputsDirectory Path to tRIBS output directory
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent, java.io.File outputsDirectory, String baseName) throws RemoteException, VisADException, java.io.IOException{
        this(parent);
        panel_IO.setSelectedIndex(1);
        pathTextField.setText(outputsDirectory.getPath());
        baseNameTextField.setText(baseName);
        System.out.println(">>Loading TIN");
        basTIN_O=new hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN(findTriEdgNodes(outputsDirectory),baseName);
        System.out.println(">>Loading MRF");
        fmrfm=new hydroScalingAPI.modules.tRIBS_io.objects.FileMrfManager(findMRF(outputsDirectory));
        System.out.println(">>Loading RFT");
        frftm=new hydroScalingAPI.modules.tRIBS_io.objects.FileRftManager(findRFT(outputsDirectory));
        System.out.println(">>Loading Qouts");
        fqm=new hydroScalingAPI.modules.tRIBS_io.objects.FileQoutManager(findQouts(outputsDirectory));
        System.out.println(">>Loading Pixel Files");
        java.io.File[] listOfPixelFiles=findPixel(outputsDirectory);
        if(listOfPixelFiles.length > 0) fpm=new hydroScalingAPI.modules.tRIBS_io.objects.FilePixelManager(listOfPixelFiles);
        System.out.println(">>Loading Integrated Files");
        fim=new hydroScalingAPI.modules.tRIBS_io.objects.FileIntegratedManager(findIntegratedOutput(outputsDirectory),basTIN_O.getNumVoi());
        System.out.println(">>Loading Dynamic Files");
        fdm=new hydroScalingAPI.modules.tRIBS_io.objects.FileDynamicManager(findDynamicOutput(outputsDirectory),basTIN_O.getNumVoi());
        System.out.println(">>Initializing Interface");
        initializeOutputTabs();
    }
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent) throws RemoteException, VisADException, java.io.IOException{
        super(parent, false);
        initComponents();
        
        mainFrame=parent;
        
        //Set up general interface
        setTitle("TRIBS I/O Module");
        setBounds(0,0, 950, 700);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        //Graphical structure for aggregated response
        PpanelRTF = 
                new hydroScalingAPI.util.plot.XYJPanel( "Runoff Mechanism Response", "Simulation time [h]" , "Runoff [m^3/s]");
        PpanelQOUT = 
                new hydroScalingAPI.util.plot.XYJPanel( "Hydrographs", "Simulation time [h]" , "Discharge [m^3/s] / Stage [m]");
        PpanelMRF = 
                new hydroScalingAPI.util.plot.XYJPanel( "Basin-Averaged Response", "Simulation time [h]" , "Value");
        PpanelPixel = 
                new hydroScalingAPI.util.plot.XYJPanel( "Local Response", "Simulation time [h]" , "Value");
        
        rftPanel.add("Center",PpanelRTF);
        qoutPanel.add("Center",PpanelQOUT);
        mrfPanel.add("Center",PpanelMRF);
        pixelPanel.add("Center",PpanelPixel);
        
        //Graphical structure for input triangulated points
        drI=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN_I = new DisplayImplJ3D("display_TIN_I",drI);
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) display_TIN_I.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_I = new ScalarMap( xEasting , Display.XAxis );
        eastMap_I.setScalarName("East Coordinate");
        northMap_I = new ScalarMap( yNorthing , Display.YAxis );
        northMap_I.setScalarName("North Coordinate");
        pointsMap_I=new ScalarMap( nodeColor , Display.RGB );

        display_TIN_I.addMap(eastMap_I);
        display_TIN_I.addMap(northMap_I);
        display_TIN_I.addMap(pointsMap_I);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_TIN_I);
        
        jPanel12.add("Center",display_TIN_I.getComponent());
        
        //Graphical structure for output triangulated points
        drO=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN_Os = new DisplayImplJ3D("display_TIN_O",drO);
        
        dispGMC = (GraphicsModeControl) display_TIN_Os.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_O = new ScalarMap( xEasting , Display.XAxis );
        eastMap_O.setScalarName("East Coordinate");
        northMap_O = new ScalarMap( yNorthing , Display.YAxis );
        northMap_O.setScalarName("North Coordinate");
        pointsMap_O=new ScalarMap( nodeColor , Display.RGB );
        pointsMap_O.setRange(0,4);
        voiColorMap_O=new ScalarMap( voiColor , Display.RGB );
        voiColorMap_O.setRange(0,10);
                
                
        display_TIN_Os.addMap(eastMap_O);
        display_TIN_Os.addMap(northMap_O);
        display_TIN_Os.addMap(pointsMap_O);
        display_TIN_Os.addMap(voiColorMap_O);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_TIN_Os);
        
        display_TIN_Os.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display_TIN_Os.addDisplayListener(this);
        
        jPanel17.add("Center",display_TIN_Os.getComponent());
    }
    
    private java.io.File findTriEdgNodes(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("nodes"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0].getParentFile();
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findTriEdgNodes(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private java.io.File findMRF(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("mrf"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0];
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findMRF(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotMrf(int i){
        double[] time=fmrfm.getTime();
        double[] values=fmrfm.getSeries(i);
        PpanelMRF.removeAll();
        PpanelMRF.addDatos(time,values,-9999,java.awt.Color.BLUE,1);
    }
    
    private java.io.File findRFT(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("rft"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0];
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findRFT(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotRft(){
        PpanelRTF.removeAll();
        if(infExBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(1),-9999,java.awt.Color.BLUE,1);
        if(satExBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(2),-9999,java.awt.Color.RED,1);
        if(perFlBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(3),-9999,java.awt.Color.GREEN,1); 
        if(groFlBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(4),-9999,java.awt.Color.YELLOW,1); 
    }
    
    private java.io.File[] findQouts(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("qout"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findQouts(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotQouts(){
        Object theKey=qNodesCombo.getSelectedItem();
        PpanelQOUT.removeAll();
        if(dischBox.isSelected()) PpanelQOUT.addDatos(fqm.getTime(theKey),fqm.getSeries(theKey,1),-9999,java.awt.Color.BLUE,1);
        if(stageBox.isSelected()) PpanelQOUT.addDatos(fqm.getTime(theKey),fqm.getSeries(theKey,2),-9999,java.awt.Color.RED,1);
    }
    
    private java.io.File[] findPixel(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("pixel"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findPixel(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotPixel(){
        Object theKey=pNodesCombo.getSelectedItem();
        int theIndex=pNodesVarsCombo.getSelectedIndex();
        PpanelPixel.removeAll();
        PpanelPixel.addDatos(fpm.getTime(theKey),fpm.getSeries(theKey,theIndex+2),-9999,java.awt.Color.BLUE,1);
        
    }
    
    private java.io.File[] findIntegratedOutput(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameEndingFilter(baseNameTextField.getText(),"i"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findIntegratedOutput(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    private void paintParametersVoronoiPolygons(int locAndCol){
        if(locAndCol >= 2000){
            paintParametersVoronoiPolygons(locAndCol,baseNameTextField.getText()+".0000_00d");
        } else {
            paintParametersVoronoiPolygons(locAndCol,"Initial Condition");
        }
    }
    private void paintParametersVoronoiPolygons(int locAndCol,Object fileKey){
        
        System.out.println("It is going to plot "+fileKey+" var# "+locAndCol);
        
        float[][] values=new float[1][];
        
        if(locAndCol >= 2000){
            values[0] = fdm.getValues((String)fileKey,locAndCol-2000);
        } else {
            values[0] = fim.getValues((String)fileKey,locAndCol-1000);
        }

        hydroScalingAPI.util.statistics.Stats
                fieldStats=new hydroScalingAPI.util.statistics.Stats(values[0]);
        
        values[0]=basTIN_O.valuesToVoroValues(values[0]);
        
        try {
            voiColorMap_O.setRange(fieldStats.minValue,fieldStats.maxValue);
            basTIN_O.getValuesFlatField().setSamples(values);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private java.io.File[] findDynamicOutput(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameEndingFilter(baseNameTextField.getText(),"d"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findDynamicOutput(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    
    private void plotOutputPoints() throws RemoteException, VisADException, java.io.IOException{
        java.io.File outputsDirectory=findTriEdgNodes(new java.io.File(pathTextField.getText()));
        String baseName=baseNameTextField.getText();
        
        basTIN_O=new hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN(outputsDirectory,baseName);
        
        ProjectionControl pc = display_TIN_Os.getProjectionControl();
        pc.setAspectCartesian(basTIN_O.getAspect());        
        initializeOutputTabs();
            
    }
    
    private void plotPoints(float Zr) throws RemoteException, VisADException{
        basTIN_I.filterPoints(ridgeLevelCombo.getSelectedIndex(),Zr);
        data_refPoints_I.setData(basTIN_I.getPointsFlatField());
        data_refTr_I.setData(basTIN_I.getTrianglesUnionSet());
        data_refPoly_I.setData(basTIN_I.getPolygonsUnionSet());
    }
    
    private void initializeInputTabs() throws RemoteException, VisADException, java.io.IOException {
        
        ProjectionControl pc = display_TIN_I.getProjectionControl();
        pc.setAspectCartesian(basTIN_I.getAspect());        
        
        data_refPoints_I = new DataReferenceImpl("data_ref_Points");
        data_refPoints_I.setData(basTIN_I.getPointsFlatField());
        
        pointsCMap_I = new ConstantMap[] {new ConstantMap( 5.0f, Display.PointSize)};

        display_TIN_I.addReference( data_refPoints_I,pointsCMap_I );
        
        data_refTr_I = new DataReferenceImpl("data_ref_TRIANG");
        trianglesCMap_I = new ConstantMap[] {new ConstantMap( 0.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refTr_I,trianglesCMap_I );
        
        data_refPoly_I = new DataReferenceImpl("data_ref_poly");
        polygonsCMap_I = new ConstantMap[] {    new ConstantMap( 1.0f, Display.Red),
                                                new ConstantMap( 0.0f, Display.Green),
                                                new ConstantMap( 1.0f, Display.Blue),
                                                new ConstantMap( 1.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refPoly_I,polygonsCMap_I );
        
    }
    
    private void initializeOutputTabs() throws RemoteException, VisADException, java.io.IOException {
        
        ProjectionControl pc = display_TIN_Os.getProjectionControl();
        pc.setAspectCartesian(basTIN_O.getAspect());
        
        data_refPoints_O = new DataReferenceImpl("data_ref_Points");
        data_refPoints_O.setData(basTIN_O.getPointsFlatField());
        pointsCMap_O = new ConstantMap[] {new ConstantMap( 5.0f, Display.PointSize)};
        display_TIN_Os.addReference( data_refPoints_O,pointsCMap_O );
        
        data_refTr_O = new DataReferenceImpl("data_ref_TRIANG");
        data_refTr_O.setData(basTIN_O.getTrianglesUnionSet());
        trianglesCMap_O = new ConstantMap[] {new ConstantMap( 0.50f, Display.LineWidth)};
        display_TIN_Os.addReference( data_refTr_O,trianglesCMap_O );
        
        data_refPoly_O = new DataReferenceImpl("data_ref_poly");
        data_refPoly_O.setData(basTIN_O.getPolygonsUnionSet());
        polygonsCMap_O = new ConstantMap[] {    new ConstantMap( 1.0f, Display.Red),
                                                new ConstantMap( 0.0f, Display.Green),
                                                new ConstantMap( 1.0f, Display.Blue),
                                                new ConstantMap( 1.50f, Display.LineWidth)};
        display_TIN_Os.addReference( data_refPoly_O,polygonsCMap_O );
        
        data_refFill_O = new DataReferenceImpl("data_ref_Fill");
        data_refFill_O.setData(basTIN_O.getValuesFlatField());
        display_TIN_Os.addReference( data_refFill_O );
        
        if(basTIN_O.getNumVoi() > 10000) display_TIN_Os.disableEvent(visad.DisplayEvent.MOUSE_MOVED);
        
        
        plotMrf(2);
        plotRft();
        
        qNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(fqm.getKeys()));
        qNodesCombo.setSelectedItem("Outlet");
        
        if(fpm != null){
            pNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(fpm.getKeys()));
            pNodesVarsCombo.setModel(new javax.swing.DefaultComboBoxModel(ListOfVariables.localOutput));
            pNodesCombo.setSelectedIndex(0);
            int[] pixelIndex=fpm.getLocationIndexes(baseNameTextField.getText());
            for (int i=0;i<pixelIndex.length;i++){
                final int myIndex=pixelIndex[i];
                Runnable addPoint = new Runnable() {
                    public void run() {
                        try {
                            visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                            rtref1.setData(basTIN_O.getPositionTuple(myIndex));
                            visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(15.0, visad.Display.PointSize ),
                                                           new visad.ConstantMap(0.7, visad.Display.Red),
                                                           new visad.ConstantMap(0.7, visad.Display.Green),
                                                           new visad.ConstantMap(0.7, visad.Display.Blue)};
                            display_TIN_Os.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                            visad.CellImpl cells1 = new visad.CellImpl() {
                                private boolean first = true;
                                public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                    if (first) first = false;
                                    else {
                                        pixelAction(myIndex);
                                    }
                                }
                            };
                            cells1.addReference(rtref1);
                        } catch (visad.VisADException exc) {
                            System.err.println("Failed showing gauges");
                            System.err.println(exc);
                        } catch (java.io.IOException exc) {
                            System.err.println("Failed showing gauges");
                            System.err.println(exc);
                        }
                    }
                };
                new Thread(addPoint).start();
            }
        }
        
        spaceParamsCombo.setModel(new javax.swing.DefaultComboBoxModel(ListOfVariables.spatialParams));
        
        Object[] spatialMoments=new String[] {"Available Times","Initial Condition","Final State"};
        
        spatialMoments=hydroScalingAPI.tools.ArrayTools.concatentate(spatialMoments,fdm.getKeys());
        avaTimesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialMoments));
        System.out.println("Step done");
        
    }
    
    private void pixelAction(int index){
        pNodesCombo.setSelectedItem(baseNameTextField.getText()+index+".pixel");
        panelOutputs.setSelectedIndex(4);
    }
    
    /**
     * A required method to handle interaction with the various visad.Display
     * @param DispEvt The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD objects
     */
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {
        
        int id = DispEvt.getId();
        visad.DisplayRenderer dr=display_TIN_Os.getDisplayRenderer();
        
        try {
            if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= eastMap_O.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= northMap_O.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                longitudeLabel.setText(""+resultX);
                latitudeLabel.setText(""+resultY);
                visad.Real spotValue=(visad.Real) basTIN_O.getValuesFlatField().evaluate(new visad.RealTuple(domainXLYL, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                valueLabel.setText(""+spotValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if (id == DispEvt.MOUSE_MOVED) {
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= eastMap_O.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= northMap_O.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                longitudeLabel.setText(""+resultX);
                latitudeLabel.setText(""+resultY);
                visad.Real spotValue=(visad.Real) basTIN_O.getValuesFlatField().evaluate(new visad.RealTuple(domainXLYL, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                valueLabel.setText(""+spotValue);
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        mrfButtonGroup = new javax.swing.ButtonGroup();
        panel_IO = new javax.swing.JTabbedPane();
        jPanel10 = new javax.swing.JPanel();
        panelInputs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        exportTriButton = new javax.swing.JButton();
        exportPoiButton = new javax.swing.JButton();
        jPanel32 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        ridgeLevelCombo = new javax.swing.JComboBox();
        jPanel33 = new javax.swing.JPanel();
        zrSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        pointsCheckBox_I = new javax.swing.JCheckBox();
        trianglesCheckBox_I = new javax.swing.JCheckBox();
        voronoiCheckBox_I = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        panelOutputs = new javax.swing.JTabbedPane();
        jPanel24 = new javax.swing.JPanel();
        mrfPanel = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        jRadioButton11 = new javax.swing.JRadioButton();
        jPanel16 = new javax.swing.JPanel();
        rftPanel = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        infExBox = new javax.swing.JCheckBox();
        satExBox = new javax.swing.JCheckBox();
        perFlBox = new javax.swing.JCheckBox();
        groFlBox = new javax.swing.JCheckBox();
        qoutPanel = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        qNodesCombo = new javax.swing.JComboBox();
        dischBox = new javax.swing.JCheckBox();
        stageBox = new javax.swing.JCheckBox();
        jPanel26 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        spaceParamsCombo = new javax.swing.JComboBox();
        jPanel17 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        avaTimesCombo = new javax.swing.JComboBox();
        avaVariablesCombo = new javax.swing.JComboBox();
        jPanel22 = new javax.swing.JPanel();
        pointsCheckBox_O = new javax.swing.JCheckBox();
        trianglesCheckBox_O = new javax.swing.JCheckBox();
        voronoiCheckBox_O = new javax.swing.JCheckBox();
        valuesCheckBox_O = new javax.swing.JCheckBox();
        jPanel30 = new javax.swing.JPanel();
        pixelPanel = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        pNodesCombo = new javax.swing.JComboBox();
        pNodesVarsCombo = new javax.swing.JComboBox();
        jPanel15 = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jPanel61 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        jPanel62 = new javax.swing.JPanel();
        jLabel323 = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        changePath = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        baseNameTextField = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        panel_IO.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        panel_IO.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.GridLayout(2, 1));

        exportTriButton.setText("Export Trinangulation");
        exportTriButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTriButtonActionPerformed(evt);
            }
        });

        jPanel5.add(exportTriButton);

        exportPoiButton.setText("Export Points");
        exportPoiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPoiButtonActionPerformed(evt);
            }
        });

        jPanel5.add(exportPoiButton);

        jPanel2.add(jPanel5, java.awt.BorderLayout.EAST);

        jPanel32.setLayout(new java.awt.GridLayout(2, 0));

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        jLabel9.setText("Ridges Level");
        jPanel32.add(jLabel9);

        ridgeLevelCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        ridgeLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Ridges", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6" }));
        ridgeLevelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ridgeLevelComboActionPerformed(evt);
            }
        });

        jPanel32.add(ridgeLevelCombo);

        jPanel2.add(jPanel32, java.awt.BorderLayout.WEST);

        jPanel33.setLayout(new java.awt.BorderLayout());

        zrSlider.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        zrSlider.setPaintLabels(true);
        zrSlider.setPaintTicks(true);
        zrSlider.setValue(0);
        zrSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zrSliderMouseReleased(evt);
            }
        });

        jPanel33.add(zrSlider, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Zr");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel33.add(jLabel1, java.awt.BorderLayout.NORTH);

        jPanel2.add(jPanel33, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridLayout(1, 3));

        pointsCheckBox_I.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox_I.setSelected(true);
        pointsCheckBox_I.setText("Show Points");
        pointsCheckBox_I.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointsCheckBox_I.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox_I.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBox_IActionPerformed(evt);
            }
        });

        jPanel3.add(pointsCheckBox_I);

        trianglesCheckBox_I.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox_I.setSelected(true);
        trianglesCheckBox_I.setText("Show Triangles");
        trianglesCheckBox_I.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        trianglesCheckBox_I.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox_I.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBox_IActionPerformed(evt);
            }
        });

        jPanel3.add(trianglesCheckBox_I);

        voronoiCheckBox_I.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        voronoiCheckBox_I.setSelected(true);
        voronoiCheckBox_I.setText("Show Voronoi Polygons");
        voronoiCheckBox_I.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        voronoiCheckBox_I.setMargin(new java.awt.Insets(0, 0, 0, 0));
        voronoiCheckBox_I.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voronoiCheckBox_IActionPerformed(evt);
            }
        });

        jPanel3.add(voronoiCheckBox_I);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel12.setLayout(new java.awt.BorderLayout());

        jPanel1.add(jPanel12, java.awt.BorderLayout.CENTER);

        panelInputs.addTab("TIN", jPanel1);

        panelInputs.addTab("3D TIN", jPanel4);

        panelInputs.addTab("Land Cover", jPanel6);

        panelInputs.addTab("Soil Type", jPanel7);

        panelInputs.addTab("Rainfall", jPanel8);

        panelInputs.addTab("Ground Water", jPanel13);

        panelInputs.addTab("Weather", jPanel14);

        panelInputs.addTab("Input File", jPanel9);

        jPanel10.add(panelInputs, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Input Options", jPanel10);

        jPanel11.setLayout(new java.awt.BorderLayout());

        panelOutputs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelOutputsStateChanged(evt);
            }
        });

        jPanel24.setLayout(new java.awt.GridLayout(1, 0));

        mrfPanel.setLayout(new java.awt.BorderLayout());

        jPanel25.setLayout(new java.awt.GridLayout(12, 0));

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel6.setText("Average Temporal Forcing (*.mrf)");
        jPanel25.add(jLabel6);

        mrfButtonGroup.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Mean Areal Precipitation [mm/h]");
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton1);

        mrfButtonGroup.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton2.setText("Maximum Rainfall Rate [mm/h]");
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton2);

        mrfButtonGroup.add(jRadioButton3);
        jRadioButton3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton3.setText("Minimum Rainfall Rate [mm/h]");
        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton3);

        mrfButtonGroup.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton4.setText("Forecast State [ ]");
        jRadioButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton4);

        mrfButtonGroup.add(jRadioButton5);
        jRadioButton5.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton5.setText("Mean Surface Soil Moisture [m^3/m^3]");
        jRadioButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton5);

        mrfButtonGroup.add(jRadioButton6);
        jRadioButton6.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton6.setText("Mean Soil Moisture in Root Zone [m^3/m^3]");
        jRadioButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton6);

        mrfButtonGroup.add(jRadioButton7);
        jRadioButton7.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton7.setText("Mean Soil Moisture in Unsaturated Zone [m^3/m^3]");
        jRadioButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton7);

        mrfButtonGroup.add(jRadioButton8);
        jRadioButton8.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton8.setText("Mean Depth to Groundwater [mm]");
        jRadioButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton8);

        mrfButtonGroup.add(jRadioButton9);
        jRadioButton9.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton9.setText("Mean Evapotranspiration [mm]");
        jRadioButton9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton9ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton9);

        mrfButtonGroup.add(jRadioButton10);
        jRadioButton10.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton10.setText("Areal Fraction of Surface Saturation [ ]");
        jRadioButton10.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton10ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton10);

        mrfButtonGroup.add(jRadioButton11);
        jRadioButton11.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton11.setText("Areal Fraction of Rainfall [ ]");
        jRadioButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton11ActionPerformed(evt);
            }
        });

        jPanel25.add(jRadioButton11);

        mrfPanel.add(jPanel25, java.awt.BorderLayout.WEST);

        jPanel24.add(mrfPanel);

        panelOutputs.addTab("Temporal Forcing", jPanel24);

        jPanel16.setLayout(new java.awt.GridLayout(2, 0));

        rftPanel.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new java.awt.GridLayout(6, 0));

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel4.setText("Runoff Component Time Series (*.rft)");
        jPanel21.add(jLabel4);

        infExBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        infExBox.setSelected(true);
        infExBox.setText("Infiltration-excess Runoff");
        infExBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        infExBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        infExBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infExBoxActionPerformed(evt);
            }
        });

        jPanel21.add(infExBox);

        satExBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        satExBox.setSelected(true);
        satExBox.setText("Saturation-excess Runoff");
        satExBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        satExBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        satExBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                satExBoxActionPerformed(evt);
            }
        });

        jPanel21.add(satExBox);

        perFlBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        perFlBox.setSelected(true);
        perFlBox.setText("Perched Return Flow");
        perFlBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        perFlBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        perFlBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                perFlBoxActionPerformed(evt);
            }
        });

        jPanel21.add(perFlBox);

        groFlBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        groFlBox.setSelected(true);
        groFlBox.setText("Groundwater Exfiltration");
        groFlBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        groFlBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        groFlBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groFlBoxActionPerformed(evt);
            }
        });

        jPanel21.add(groFlBox);

        rftPanel.add(jPanel21, java.awt.BorderLayout.WEST);

        jPanel16.add(rftPanel);

        qoutPanel.setLayout(new java.awt.BorderLayout());

        jPanel23.setLayout(new java.awt.GridLayout(6, 0));

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel5.setText("Discharge Time Series (*.qout)");
        jLabel5.setMaximumSize(new java.awt.Dimension(179, 13));
        jLabel5.setMinimumSize(new java.awt.Dimension(179, 13));
        jLabel5.setPreferredSize(new java.awt.Dimension(179, 13));
        jPanel23.add(jLabel5);

        qNodesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        qNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        qNodesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qNodesComboActionPerformed(evt);
            }
        });

        jPanel23.add(qNodesCombo);

        dischBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        dischBox.setSelected(true);
        dischBox.setText("Discharge");
        dischBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dischBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dischBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dischBoxActionPerformed(evt);
            }
        });

        jPanel23.add(dischBox);

        stageBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        stageBox.setSelected(true);
        stageBox.setText("Channel stage");
        stageBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stageBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stageBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stageBoxActionPerformed(evt);
            }
        });

        jPanel23.add(stageBox);

        qoutPanel.add(jPanel23, java.awt.BorderLayout.WEST);

        jPanel16.add(qoutPanel);

        panelOutputs.addTab("Temporal Response", jPanel16);

        jPanel26.setLayout(new java.awt.BorderLayout());

        jPanel27.setLayout(new java.awt.GridLayout(1, 2));

        spaceParamsCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        spaceParamsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Variables", "Variable 01", "Variable 02", "Variable 03", "Variable 04" }));
        spaceParamsCombo.setLightWeightPopupEnabled(false);
        spaceParamsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spaceParamsComboActionPerformed(evt);
            }
        });

        jPanel27.add(spaceParamsCombo);

        jPanel26.add(jPanel27, java.awt.BorderLayout.NORTH);

        panelOutputs.addTab("Spatial Parameterization", jPanel26);

        jPanel17.setLayout(new java.awt.BorderLayout());

        jPanel20.setLayout(new java.awt.GridLayout(1, 2));

        avaTimesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        avaTimesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Times", "Time 01", "Time 02", "Time 03", "Time 04", "Time-integrated Spatial Output" }));
        avaTimesCombo.setLightWeightPopupEnabled(false);
        avaTimesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avaTimesComboActionPerformed(evt);
            }
        });

        jPanel20.add(avaTimesCombo);

        avaVariablesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Variables" }));
        avaVariablesCombo.setLightWeightPopupEnabled(false);
        avaVariablesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avaVariablesComboActionPerformed(evt);
            }
        });

        jPanel20.add(avaVariablesCombo);

        jPanel17.add(jPanel20, java.awt.BorderLayout.NORTH);

        jPanel22.setLayout(new java.awt.GridLayout(1, 4));

        pointsCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox_O.setSelected(true);
        pointsCheckBox_O.setText("Show Points");
        pointsCheckBox_O.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointsCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBox_OActionPerformed(evt);
            }
        });

        jPanel22.add(pointsCheckBox_O);

        trianglesCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox_O.setSelected(true);
        trianglesCheckBox_O.setText("Show Triangles");
        trianglesCheckBox_O.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        trianglesCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBox_OActionPerformed(evt);
            }
        });

        jPanel22.add(trianglesCheckBox_O);

        voronoiCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        voronoiCheckBox_O.setSelected(true);
        voronoiCheckBox_O.setText("Show Voronoi Polygons");
        voronoiCheckBox_O.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        voronoiCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        voronoiCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voronoiCheckBox_OActionPerformed(evt);
            }
        });

        jPanel22.add(voronoiCheckBox_O);

        valuesCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        valuesCheckBox_O.setSelected(true);
        valuesCheckBox_O.setText("Show Variable Values");
        valuesCheckBox_O.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        valuesCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        valuesCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valuesCheckBox_OActionPerformed(evt);
            }
        });

        jPanel22.add(valuesCheckBox_O);

        jPanel17.add(jPanel22, java.awt.BorderLayout.SOUTH);

        panelOutputs.addTab("Spatial Response", jPanel17);

        jPanel30.setLayout(new java.awt.GridLayout(1, 0));

        pixelPanel.setLayout(new java.awt.BorderLayout());

        jPanel31.setLayout(new java.awt.GridLayout(0, 3));

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel8.setText("Local high resolution output (*.pixel)");
        jPanel31.add(jLabel8);

        pNodesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        pNodesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pNodesComboActionPerformed(evt);
            }
        });

        jPanel31.add(pNodesCombo);

        pNodesVarsCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pNodesVarsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        pNodesVarsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pNodesVarsComboActionPerformed(evt);
            }
        });

        jPanel31.add(pNodesVarsCombo);

        pixelPanel.add(jPanel31, java.awt.BorderLayout.NORTH);

        jPanel30.add(pixelPanel);

        panelOutputs.addTab("Local Output", jPanel30);

        jPanel11.add(panelOutputs, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Output Analysis", jPanel11);

        getContentPane().add(panel_IO, java.awt.BorderLayout.CENTER);

        jPanel15.setLayout(new java.awt.GridLayout(3, 1, 0, 3));

        jPanel28.setLayout(new java.awt.GridLayout(1, 3));

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel31.setText("Easting : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel61);

        jPanel29.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel7.setText("Northing : ");
        jPanel29.add(jLabel7, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel29.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel29);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel323.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel323.setText("Value : ");
        jPanel62.add(jLabel323, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel62);

        jPanel15.add(jPanel28);

        jPanel18.setLayout(new java.awt.BorderLayout());

        changePath.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        changePath.setText("Change Reference Path");
        changePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePathActionPerformed(evt);
            }
        });

        jPanel18.add(changePath, java.awt.BorderLayout.EAST);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel2.setText("Reference Path : ");
        jPanel18.add(jLabel2, java.awt.BorderLayout.WEST);

        pathTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jPanel18.add(pathTextField, java.awt.BorderLayout.CENTER);

        jPanel15.add(jPanel18);

        jPanel19.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel3.setText("Base Name : ");
        jPanel19.add(jLabel3, java.awt.BorderLayout.WEST);

        baseNameTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jPanel19.add(baseNameTextField, java.awt.BorderLayout.CENTER);

        jPanel15.add(jPanel19);

        getContentPane().add(jPanel15, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ridgeLevelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ridgeLevelComboActionPerformed
        try {
            plotPoints(zrSlider.getValue());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_ridgeLevelComboActionPerformed

    private void pNodesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pNodesComboActionPerformed
        plotPixel();
    }//GEN-LAST:event_pNodesComboActionPerformed

    private void pNodesVarsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pNodesVarsComboActionPerformed
        plotPixel();
    }//GEN-LAST:event_pNodesVarsComboActionPerformed

    private void avaVariablesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avaVariablesComboActionPerformed
        if(avaVariablesCombo.getSelectedIndex() == 0){
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
        if(avaTimesCombo.getSelectedIndex() < 3){
            paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
            return;
        }
        paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+2,avaTimesCombo.getSelectedItem());
    }//GEN-LAST:event_avaVariablesComboActionPerformed

    private void avaTimesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avaTimesComboActionPerformed
        if(avaTimesCombo.getSelectedIndex() == 0){
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
            Object[] spatialVariables=new String[] {"Available Variables"};
            avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
            return;
        }
        if(avaTimesCombo.getSelectedIndex() < 3){
            if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Integrated Variables")){
                paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
            } else {
                try {
                    voiColorMap_O.setRange(0,0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                } catch (VisADException ex) {
                    ex.printStackTrace();
                }
                Object[] spatialVariables=new String[] {"Available Integrated Variables"};
                spatialVariables=hydroScalingAPI.tools.ArrayTools.concatentate(spatialVariables,ListOfVariables.integratedOutput);
                avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
            }
            return;
        }

        if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Dynamic Variables")){
            paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+2,avaTimesCombo.getSelectedItem());
        } else {
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
            Object[] spatialVariables=new String[] {"Available Dynamic Variables"};
            spatialVariables=hydroScalingAPI.tools.ArrayTools.concatentate(spatialVariables,ListOfVariables.dynamicOutput);
            avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
        }
        
    }//GEN-LAST:event_avaTimesComboActionPerformed

    private void spaceParamsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spaceParamsComboActionPerformed
        if(spaceParamsCombo.getSelectedIndex() == 0) return;
        paintParametersVoronoiPolygons(ListOfVariables.spatialVarSource[spaceParamsCombo.getSelectedIndex()]);
    }//GEN-LAST:event_spaceParamsComboActionPerformed

    private void qNodesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qNodesComboActionPerformed
        plotQouts();
    }//GEN-LAST:event_qNodesComboActionPerformed

    private void dischBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dischBoxActionPerformed
        plotQouts();
    }//GEN-LAST:event_dischBoxActionPerformed

    private void stageBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stageBoxActionPerformed
        plotQouts();
    }//GEN-LAST:event_stageBoxActionPerformed

    private void groFlBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groFlBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_groFlBoxActionPerformed

    private void perFlBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_perFlBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_perFlBoxActionPerformed

    private void satExBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_satExBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_satExBoxActionPerformed

    private void infExBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infExBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_infExBoxActionPerformed

    private void jRadioButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton11ActionPerformed
        plotMrf(12);
    }//GEN-LAST:event_jRadioButton11ActionPerformed

    private void jRadioButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton10ActionPerformed
        plotMrf(11);
    }//GEN-LAST:event_jRadioButton10ActionPerformed

    private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton9ActionPerformed
        plotMrf(10);
    }//GEN-LAST:event_jRadioButton9ActionPerformed

    private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton8ActionPerformed
        plotMrf(9);
    }//GEN-LAST:event_jRadioButton8ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
        plotMrf(8);
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        plotMrf(7);
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
        plotMrf(6);
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        plotMrf(5);
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        plotMrf(4);
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        plotMrf(3);
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        plotMrf(2);
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void panelOutputsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelOutputsStateChanged
      if ( panelOutputs.getSelectedIndex()==2) {
          jPanel26.add("Center",display_TIN_Os.getComponent());
          jPanel26.add("South",jPanel22);
          if(spaceParamsCombo.getSelectedIndex() == 0) {
              try {
                    voiColorMap_O.setRange(0,0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                } catch (VisADException ex) {
                    ex.printStackTrace();
                }
          } else{
            paintParametersVoronoiPolygons(ListOfVariables.spatialVarSource[spaceParamsCombo.getSelectedIndex()]);
          }
      }

      if ( panelOutputs.getSelectedIndex()==3) {
          jPanel17.add("Center",display_TIN_Os.getComponent());
          jPanel17.add("South",jPanel22);
          if(avaTimesCombo.getSelectedIndex() == 0) {
              try {
                  voiColorMap_O.setRange(0,0);
              } catch (RemoteException ex) {
                  ex.printStackTrace();
              } catch (VisADException ex) {
                  ex.printStackTrace();
              }
          } else{
              if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Integrated Variables")){
                  paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
              } else {
                  paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+2,avaTimesCombo.getSelectedItem());
              }
          }
      }
    }//GEN-LAST:event_panelOutputsStateChanged

    private void valuesCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valuesCheckBox_OActionPerformed
        if(valuesCheckBox_O.isSelected()){
            try {
                display_TIN_Os.addReference(data_refFill_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_Os.removeReference(data_refFill_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_valuesCheckBox_OActionPerformed

    private void voronoiCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voronoiCheckBox_OActionPerformed
        if(voronoiCheckBox_O.isSelected()){
            try {
                display_TIN_Os.addReference(data_refPoly_O,polygonsCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_Os.removeReference(data_refPoly_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_voronoiCheckBox_OActionPerformed

    private void trianglesCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBox_OActionPerformed
        if(trianglesCheckBox_O.isSelected()){
            try {
                display_TIN_Os.addReference(data_refTr_O,trianglesCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_Os.removeReference(data_refTr_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_trianglesCheckBox_OActionPerformed

    private void pointsCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBox_OActionPerformed
        if(pointsCheckBox_O.isSelected()){
            try {
                display_TIN_Os.addReference(data_refPoints_O,pointsCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_Os.removeReference(data_refPoints_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_pointsCheckBox_OActionPerformed

    private void changePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePathActionPerformed
        try {
            plotOutputPoints();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_changePathActionPerformed

    private void exportPoiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPoiButtonActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser();
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Directory Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("points","Points File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);

            if (fc.getSelectedFile() == null) return;
            
            basTIN_I.writePoints(fc.getSelectedFile());
        } catch (java.io.IOException IOE){
            System.err.println("Failed writing triangulation files for this basin.");
            IOE.printStackTrace();
        }
    }//GEN-LAST:event_exportPoiButtonActionPerformed

    private void voronoiCheckBox_IActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voronoiCheckBox_IActionPerformed
        if(voronoiCheckBox_I.isSelected()){
            try {
                display_TIN_I.addReference(data_refPoly_I,polygonsCMap_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refPoly_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_voronoiCheckBox_IActionPerformed

    private void trianglesCheckBox_IActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBox_IActionPerformed
        if(trianglesCheckBox_I.isSelected()){
            try {
                display_TIN_I.addReference(data_refTr_I,trianglesCMap_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refTr_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_trianglesCheckBox_IActionPerformed

    private void pointsCheckBox_IActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBox_IActionPerformed
        if(pointsCheckBox_I.isSelected()){
            try {
                display_TIN_I.addReference(data_refPoints_I,pointsCMap_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refPoints_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_pointsCheckBox_IActionPerformed

    private void zrSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zrSliderMouseReleased
        try {
            plotPoints(zrSlider.getValue());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_zrSliderMouseReleased

    private void exportTriButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportTriButtonActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser();
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Directory Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("nodes","Nodes File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);

            if (fc.getSelectedFile() == null) return;
            basTIN_I.writeTriangulation(fc.getSelectedFile());
        } catch (java.io.IOException IOE){
            System.err.println("Failed writing triangulation files for this basin.");
            IOE.printStackTrace();
        }
    }//GEN-LAST:event_exportTriButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Test for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Topography/1_Arcsec/NED_06075640.metaDEM");
            //java.io.File theFile=new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            
            new TRIBS_io(tempFrame, 56,79,matDirs,magnitudes,metaModif).setVisible(true);
            //new TRIBS_io(tempFrame, 111,80,matDirs,magnitudes,metaModif).setVisible(true);
            
            ///home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/"),"smallbasin"
            ///home/ricardo/workFiles/tribsWork/sampleTribs/Output_Mar23a_07/"),"urp"
            //new TRIBS_io(tempFrame, new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/"),"smallbasin").setVisible(true);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox avaTimesCombo;
    private javax.swing.JComboBox avaVariablesCombo;
    private javax.swing.JTextField baseNameTextField;
    private javax.swing.JButton changePath;
    private javax.swing.JCheckBox dischBox;
    private javax.swing.JButton exportPoiButton;
    private javax.swing.JButton exportTriButton;
    private javax.swing.JCheckBox groFlBox;
    private javax.swing.JCheckBox infExBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel323;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton11;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.ButtonGroup mrfButtonGroup;
    private javax.swing.JPanel mrfPanel;
    private javax.swing.JComboBox pNodesCombo;
    private javax.swing.JComboBox pNodesVarsCombo;
    private javax.swing.JTabbedPane panelInputs;
    private javax.swing.JTabbedPane panelOutputs;
    private javax.swing.JTabbedPane panel_IO;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JCheckBox perFlBox;
    private javax.swing.JPanel pixelPanel;
    private javax.swing.JCheckBox pointsCheckBox_I;
    private javax.swing.JCheckBox pointsCheckBox_O;
    private javax.swing.JComboBox qNodesCombo;
    private javax.swing.JPanel qoutPanel;
    private javax.swing.JPanel rftPanel;
    private javax.swing.JComboBox ridgeLevelCombo;
    private javax.swing.JCheckBox satExBox;
    private javax.swing.JComboBox spaceParamsCombo;
    private javax.swing.JCheckBox stageBox;
    private javax.swing.JCheckBox trianglesCheckBox_I;
    private javax.swing.JCheckBox trianglesCheckBox_O;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JCheckBox valuesCheckBox_O;
    private javax.swing.JCheckBox voronoiCheckBox_I;
    private javax.swing.JCheckBox voronoiCheckBox_O;
    private javax.swing.JSlider zrSlider;
    // End of variables declaration//GEN-END:variables
    
}

class ListOfVariables{
    public static int[] spatialVarSource={  -1,
                                            2000,
                                            2001,
                                            2002,
                                            1003,
                                            1004,
                                            1005,
                                            1006,
                                            1007,
                                            1008,
                                            1009,
                                            2025,
                                            2026};

    public static String[] spatialParams={  "Available Variables",
                                            "Node Identification, ID [id]",
                                            "Elevation, Z [m]",
                                            "Slope, S [|radian|]",
                                            "Voronoi Area, VAr [m2]",
                                            "Contributing Area, CAr [km2]",
                                            "Curvature, Curv [ ]",
                                            "Flow Edge Length, EdgL [m]",
                                            "Tangent of Flow Edge Slope, tan(Slp) [ ]",
                                            "Width of Voronoi Flow Window, FWidth [m]",
                                            "Site Aspect as Angle from North, Aspect [radian]",
                                            "Soil Type []",
                                            "Land Use Type []"};
    
    public static String[] integratedOutput={   "Average Soil Moisture, top 10 cm, AvSM [m3/m3]",
                                                "Average Root Zone Moisture, top 1 m, AvRtM [m3/m3]",
                                                "Infiltration-excess Runoff Occurences, HOccr [# of TIMESTEP]",
                                                "Infiltration-excess Runoff Average Rate, HRt [mm/hr]",
                                                "Saturation-excess Runoff Occurences, SbOccr [# of TIMESTEP]",
                                                "Saturation-excess Runoff Average Rate, SbRt 	[mm/hr]",
                                                "Perched Return Runoff Occurences, POccr [# of TIMESTEP]",
                                                "Perched Return Runoff Average Rate, PRt [mm/hr]",
                                                "Groundwater Exfiltration Runoff Occurences, SatOccr [# of GWSTEP]",
                                                "Groundwater Exfiltration Runoff Average Rate, SatRt [mm/hr]",
                                                "Soil Saturation Occurences, SoiSatOccr [# of TIMESTEP]",
                                                "Recharge-Discharge Variable, RchDsch [m]",
                                                "Average Evapotranspiration, AveET [mm/hr]",
                                                "Evaporative Fraction, EvpFrct [ ]"};
    
    public static String[] dynamicOutput={  "Depth to groundwater table, Nwt [mm]",
                                            "Total moisture above the water table, Mu [mm]",
                                            "Moisture content in the initialization profile, Mi [mm]",
                                            "Wetting front depth, Nf [mm]",
                                            "Top front depth, Nt [mm]",
                                            "Unsaturated lateral flow out from cell, Qpout [mm/hr]",
                                            "Unsaturated lateral flow into cell, Qpin [mm/hr]",
                                            "Surface Runoff, Srf [mm]",
                                            "Rainfall, Rain [mm/hr]",
                                            "Soil Moisture, top 10 cm, SoilMoist [ m3/m3]",
                                            "Root  Zone Moisture, top 1 m, RootMoist [ m3/m3]",
                                            "Canopy Storage, CanStorg [mm]",
                                            "Actual Evaporation, ActEvp [mm/hr]",
                                            "Evaporation from Bare Soil, EvpSoil [mm/hr]",
                                            "Total Evapotranspiration, ET [mm/hr]",
                                            "Ground Heat Flux, Gflux [W/m2]",
                                            "Sensible Heat Flux, Hflux [W/m2]",
                                            "Latent Heat Flux, Lflux [W/m2]",
                                            "Discharge, Qstrm 	[m3/s]",
                                            "Channel Stage, Hlev [m]",
                                            "Flow Velocity, FlwVlc [m/s]"};
    
    public static String[] localOutput={  "Depth to groundwater table, Nwt [mm]",
                                          "Wetting front depth, Nf [mm]",
                                          "Top front depth, Nt [mm]",
                                          "Total moisture above the water table, Mu [mm]",
                                          "Moisture content in the initialization profile, Mi [mm]",
                                          "Unsaturated lateral flow out from cell, Qpout [mm/hr]",
                                          "Unsaturated lateral flow into cell, Qpin [mm/hr]",
                                          "Transmissivity, Trnsm [m2/hr]",
                                          "Groundwater flux, GWflx [m3/hr]",
                                          "Surface Runoff, Srf [mm]",
                                          "Rainfall, Rain [mm/hr]",
                                          "Soil Moisture, top 10 cm, SoilMoist [m3/m3]",
                                          "Root  Zone Moisture, top 1 m, RootMoist [m3/m3]",
                                          "Air Temperature, AirT [C]",
                                          "Dew Point Temperature, DewT [C]",
                                          "Surface Temperature, SurfT [C]",
                                          "Soil Temperature, SoilT [C]",
                                          "Atmospheric Pressure, Press [Pa]",
                                          "Relative Humidity, RelHum [ ]",
                                          "Sky Cover, SkyCov [ ]",
                                          "Wind Speed, Wind [m/s]",
                                          "Net Radiation, NetRad [W/m2]",
                                          "Incoming Shortwave Radiation, ShrtRadIn [W/m2]",
                                          "Incoming Direct Shortwave Radiation, ShrtRadIn_dir [W/m2]",
                                          "Incoming Diffuse Shortwave Radiation, ShrtRadIn_dif 	[W/m2]",
                                          "Shortwave Absorbed Radition, Vegetation, ShortAbsbVeg [W/m2]",
                                          "Shortwave Absorbed Radition, Soil, ShortAbsbSoi [W/m2]",
                                          "Incoming Longwave Radiation, LngRadIn [W/m2]",
                                          "Outgoing Longwave Radiation, LngRadOut [W/m2]",
                                          "Potential Evaporation, PotEvp [mm/hr]",
                                          "Actual Evaporation, ActEvp [mm/hr]",
                                          "Total Evapotranspiration, EvpTtrs [mm/hr]",
                                          "Evaporation from Wet Canopy, EvpWetCan [mm/hr]",
                                          "Evaporation from Dry Canopy (Transpiration), EvpDryCan [mm/hr]",
                                          "Evaporation from Bare Soil, EvpSoil [mm/hr]",
                                          "Ground Heat Flux, Gflux [W/m2]",
                                          "Sensible Heat Flux, Hflux [W/m2]",
                                          "Latent Heat Flux, Lflux [W/m2]",
                                          "Net Precipitation, NetPrecip [mm/hr]",
                                          "Canopy Storage, CanStorg [mm]",
                                          "Cumulative Interception, CumIntercept [mm]",
                                          "Interception, Intercept [mm]",
                                          "Recharge [mm/hr]",
                                          "Runon [mm]",
                                          "Surface Runoff in Hour, Surf_Hour [mm]",
                                          "Discharge, Qstrm [m3/s]",
                                          "Channel Stage, Hlev"};
}

