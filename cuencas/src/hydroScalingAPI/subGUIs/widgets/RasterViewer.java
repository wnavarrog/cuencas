/*
 * RasterViewer.java
 *
 * Created on June 20, 2003, 12:29 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author  ricardo
 */

public abstract class RasterViewer extends javax.swing.JInternalFrame {
    
    protected hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    protected String myStringID;
    protected hydroScalingAPI.io.MetaRaster metaData;
    protected hydroScalingAPI.io.BasinsLogReader localBasinsLog;
    
    protected final visad.util.HersheyFont font = new visad.util.HersheyFont("meteorology");
    
    protected visad.DisplayImpl display;
    protected visad.ScalarMap latitudeMap,longitudeMap,heightMap,colorScaleMap;
    protected visad.FlatField localField;
    
    protected visad.java3d.DisplayRendererJ3D dr;
    
    protected java.util.Hashtable relatedMapsList;
    
    protected hydroScalingAPI.io.MetaNetwork localNetwork;
    protected java.util.Hashtable networkReferences=new java.util.Hashtable();
    protected float[] red;
    protected float[] green;
    protected float[] blue;
    
    protected java.util.Vector temporalReferences=new java.util.Vector();
    protected java.util.Vector basinOutlets=new java.util.Vector();

    protected int activeEvent=0;
    
    protected byte[][] fullDirMatrix;
    
    protected int[][] subSetCorners=new int[2][2];
    protected int subSetCornersCounter=0;
    
    /** Creates new form RasterViewer */
    public RasterViewer(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) {
        mainFrame=parent;
        metaData=md;
        relatedMapsList=relMaps;
        
        red=    mainFrame.getInfoManager().getNetworkRed();
        green=  mainFrame.getInfoManager().getNetworkGreen();
        blue=   mainFrame.getInfoManager().getNetworkBlue();
        
        initComponents();
        setSize(650,700);
        
        updateRelatedMaps();
        addInternalFrameListener(mainFrame);
        
    }
    
    private boolean isDEM(){
        return metaData.getLocationMeta().getName().toLowerCase().lastIndexOf(".metadem") != -1;
    }
    
    protected void demToolsEnable(boolean enabled){
        showNetwork.setEnabled(enabled);
        showBasins.setEnabled(enabled);
        selectOutlet.setEnabled(enabled);
        traceStream.setEnabled(enabled);
    }
    
    protected void updateBasinsPopupMenu(){
        java.io.File originalFile=metaData.getLocationBinaryFile();
        java.io.File logFile=new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".log");
        
        if(logFile.exists()){
            
            try{
                localBasinsLog=new hydroScalingAPI.io.BasinsLogReader(logFile);
            } catch(java.io.IOException IOE){
                System.err.println("Failed loading basins log. "+metaData);
                System.err.println(IOE);
                return;
            }
            
            String[] basinsPreSelected=localBasinsLog.getPresetBasins();
            for(int i=0;i<basinsPreSelected.length;i++){
                
                addPreselectedBasin(basinsPreSelected[i]);
                
            }
            
            
        } else {
            try{
                logFile.createNewFile();
                localBasinsLog=new hydroScalingAPI.io.BasinsLogReader(logFile);
            } catch(java.io.IOException IOE){
                System.err.println("Failed creating new basins log. "+metaData);
                System.err.println(IOE);
                return;
            }
        }
        
    }
    
    protected void addPreselectedBasin(String basinLabel){
        javax.swing.JMenuItem showBasinI = new javax.swing.JMenuItem();
        showBasinI.setFont(new java.awt.Font("Dialog", 0, 10));
        showBasinI.setText(basinLabel);
        showBasinI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBasinPreSelectedActionPerformed(evt);
            }
        });

        basinsViewerPopUp.add(showBasinI);
    }
    
    protected void showBasinPreSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        
        javax.swing.JMenuItem showOrderW = (javax.swing.JMenuItem)evt.getSource();
        
        String[] basinLabel=showOrderW.getText().split(" ; ");
        String xlabel=(basinLabel[0].split(", "))[0];
        String ylabel=(basinLabel[0].split(", "))[1];
        int matX=Integer.parseInt(xlabel.substring(2).trim());
        int matY=Integer.parseInt(ylabel.substring(2).trim());
        
        try{
            traceBasinContour(matX,matY,false);
        } catch (visad.VisADException vie){
            System.err.println("Failed reloading basin: "+showOrderW.getText());
            System.err.println(vie);
        } catch (java.rmi.RemoteException rme){
            System.err.println("Failed reloading basin: "+showOrderW.getText());
            System.err.println(rme);
        }
        
        
    }
    
    protected void updateNetworkPopupMenu(){
        
        javax.swing.JCheckBoxMenuItem showAllOrders = new javax.swing.JCheckBoxMenuItem();
        showAllOrders.setFont(new java.awt.Font("Dialog", 0, 10));
        showAllOrders.setText("Show All Orders");
        showAllOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllOrdersActionPerformed(evt);
            }
        });
        
        addToNetworkPupUp(showAllOrders);
        
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        
        addToNetworkPupUp(jSeparator1);
        
        try{
            localNetwork=new hydroScalingAPI.io.MetaNetwork(metaData);
        } catch(java.io.IOException IOE){
            System.err.println("Failed loading network file. "+metaData);
            System.err.println(IOE);
            return;
        }
        int largestOrder=localNetwork.getLargestOrder();
        
        for (int i=0;i<largestOrder;i++){
            
            javax.swing.JCheckBoxMenuItem showOrderW = new javax.swing.JCheckBoxMenuItem();
            showOrderW.setFont(new java.awt.Font("Dialog", 0, 10));
            showOrderW.setText("Show Order "+(largestOrder-i)+"  streams");
            showOrderW.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showOrderWActionPerformed(evt);
                }
            });

            addToNetworkPupUp(showOrderW);
            
        }
        
    }

    protected void showAllOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        selectAllNetworkPopup();
        javax.swing.JCheckBoxMenuItem showAllOrders = (javax.swing.JCheckBoxMenuItem)evt.getSource();
        if(showAllOrders.isSelected()){
            for(int orderRequested=1;orderRequested<=localNetwork.getLargestOrder();orderRequested++){
                try {
                    if(networkReferences.get("order"+orderRequested) != null){
                        visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                            new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                            new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                            //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                            new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                        display.addReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested),lineCMap);
                    } else {
                        addStreamsWithOrder(orderRequested);
                    }
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        } else {
            for(int orderRequested=1;orderRequested<=localNetwork.getLargestOrder();orderRequested++){
                try {
                    display.removeReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested));
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        }
    }
    
    private void showOrderWActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.JCheckBoxMenuItem showOrderW = (javax.swing.JCheckBoxMenuItem)evt.getSource();
        int orderRequested=Integer.parseInt(showOrderW.getText().substring(11,13).trim());
        if (showOrderW.isSelected())
            try {
                if(networkReferences.get("order"+orderRequested) != null){
                    visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                        new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                        new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                        //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                        new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                    display.addReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested),lineCMap);
                } else {
                    addStreamsWithOrder(orderRequested);
                }
            } catch (visad.VisADException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            } catch (java.io.IOException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            }
        else
            try {
                display.removeReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested));
            } catch (visad.VisADException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            } catch (java.io.IOException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            }
        
    }
    
    public void addStreamsWithOrder(final int orderRequested){
        Runnable addStreams = new Runnable() {
            public void run() {
                try {

                    visad.UnionSet toPlot = localNetwork.getUnionSet(orderRequested);
                    if (toPlot != null){
                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("order"+orderRequested);
                        refeElemVec.setData(toPlot);
                        visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                        new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                        new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                        //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                        new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                        display.addReference(refeElemVec,lineCMap);
                        networkReferences.put("order"+orderRequested,refeElemVec);
                    }
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        };
        new Thread(addStreams).start();
    }
    
    protected void saveImage(java.io.File f) throws visad.VisADException, java.io.IOException {
        java.awt.image.BufferedImage image = (java.awt.image.BufferedImage) display.getImage();
        
        if(!f.getName().endsWith(".jpg")){
            f= new java.io.File(f.getPath()+".jpg");
        }
        
        ij.io.FileSaver saver =new ij.io.FileSaver(new ij.ImagePlus("null",image));
        saver.saveAsJpeg(f.getPath());
        
    }
    
    protected void traceBasinContour(final int MatX,final int MatY, boolean isNew) throws visad.VisADException, java.rmi.RemoteException{
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {
            if(isNew){
                try{
                    String basinLabel="x: "+MatX+", y: "+MatY+" ; Basin Code "+String.valueOf(Math.random()).substring(2);
                    localBasinsLog.addBasinToFile(basinLabel);
                    addPreselectedBasin(basinLabel);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed updating basins log file.");
                    System.err.println(exc);
                }
            }
            
            hydroScalingAPI.util.geomorphology.objects.Basin thisBasin = new hydroScalingAPI.util.geomorphology.objects.Basin(MatX,MatY, fullDirMatrix,metaData);
            
            visad.DataReferenceImpl riverRef=new visad.DataReferenceImpl("rio");
            riverRef.setData(thisBasin.getBasinDivide());
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 100/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 2.0f, visad.Display.LineWidth)};
            
            temporalReferences.add(new Object[] {riverRef,lineCMap});

            display.addReference( riverRef , lineCMap);
            
            visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("OutletTuple");
            rtref1.setData(thisBasin.getOutletTuple());
            visad.ConstantMap[] rtmaps1 = { new visad.ConstantMap(1.0, visad.Display.Red ),
                                            new visad.ConstantMap(0, visad.Display.Green ),
                                            new visad.ConstantMap(0, visad.Display.Blue ),
                                            new visad.ConstantMap(7.0, visad.Display.PointSize )};
            display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
            visad.CellImpl cells1 = new visad.CellImpl() {
                private boolean first = true;
                public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                    if (first) first = false;
                    else {
                        lunchBasinAnalyzer(MatX,MatY);
                    }
                }
            };
            cells1.addReference(rtref1);
            
            basinOutlets.add(new Object[] {rtref1,rtmaps1});
            
        }
    }
    
    protected void traceRiverPath(int MatX,int MatY) throws visad.VisADException, java.rmi.RemoteException{
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {

            java.util.Vector thisRiver=new java.util.Vector();
            thisRiver.addElement(new Integer(MatX));
            thisRiver.addElement(new Integer(MatY));

            do {
                int MatXN=MatX-1+(fullDirMatrix[MatY][MatX]-1)%3;
                int MatYN=MatY-1+(fullDirMatrix[MatY][MatX]-1)/3;

                MatX=MatXN; MatY=MatYN;

                thisRiver.addElement(new Integer(MatX));
                thisRiver.addElement(new Integer(MatY));
            } while (fullDirMatrix[MatY][MatX] > 0);

            float[][] RiverPath=new float[2][thisRiver.size()/2];

            for (int j=0;j<thisRiver.size()-1;j+=2){
                
                RiverPath[0][j/2]=(float) (metaData.getMinLon()+metaData.getResLon()/3600.0*(Double.parseDouble(thisRiver.elementAt(j).toString())) + 0.5*metaData.getResLon()/3600.0f);
                RiverPath[1][j/2]=(float) (metaData.getMinLat()+metaData.getResLat()/3600.0*(Double.parseDouble(thisRiver.elementAt(j+1).toString())) + 0.5*metaData.getResLat()/3600.0f);
            }


            visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
            visad.Gridded2DSet ScreenRiverRep=new visad.Gridded2DSet(domain,RiverPath,RiverPath[0].length);

            visad.DataReferenceImpl riverRef=new visad.DataReferenceImpl("rio");
            riverRef.setData(ScreenRiverRep);
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 64/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 77/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 160/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 1.50f, visad.Display.LineWidth)};
            
            temporalReferences.add(new Object[] {riverRef,lineCMap});

            display.addReference( riverRef , lineCMap);

        }

    }
    
    protected void assignSubDataSet(int MatX,int MatY) throws visad.VisADException, java.rmi.RemoteException{
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {

            subSetCornersCounter++;

            if(subSetCornersCounter == 1){
                subSetCorners[0][0]=MatX;
                subSetCorners[0][1]=MatY;
            } else {
                subSetCorners[1][0]=MatX;
                subSetCorners[1][1]=MatY;
                extractSubDataSet();
                subSetCornersCounter=0;
            }
            

        }

    }
    
    protected void extractSubDataSet(){
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersDemPath);
            fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
            fc.setDialogTitle("Select Destination Folder");
            int result = fc.showDialog(this,"Select");
            java.io.File selectedFile = fc.getSelectedFile();

            if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
            
            hydroScalingAPI.io.MetaRaster sectionMR=new hydroScalingAPI.io.MetaRaster(metaData);

            String secMR_Name=selectedFile.getPath()+"/section_"+subSetCorners[0][0]+"_"+subSetCorners[0][1]+"_"+subSetCorners[1][0]+"_"+subSetCorners[1][1]+"_"+metaData.getLocationMeta().getName();
            String secBin_Name=selectedFile.getPath()+"/section_"+subSetCorners[0][0]+"_"+subSetCorners[0][1]+"_"+subSetCorners[1][0]+"_"+subSetCorners[1][1]+"_"+metaData.getLocationBinaryFile().getName();
            
            sectionMR.setLocationMeta(new java.io.File(secMR_Name));
            sectionMR.setLocationBinaryFile(new java.io.File(secBin_Name));
            
            sectionMR.setNumCols(Math.abs(subSetCorners[0][0]-subSetCorners[1][0]));
            sectionMR.setNumRows(Math.abs(subSetCorners[0][1]-subSetCorners[1][1]));
            
            sectionMR.setMinLon(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLat()+metaData.getResLat()/3600.*(int)Math.min(subSetCorners[0][0],subSetCorners[1][0]),0));
            sectionMR.setMinLon(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLon()+metaData.getResLon()/3600.*(int)Math.min(subSetCorners[0][1],subSetCorners[1][1]),1));
            
            sectionMR.setFormat("float");
            
            try {
                sectionMR.writeMetaRaster(sectionMR.getLocationMeta());
                java.io.FileOutputStream        salida;
                java.io.DataOutputStream        newfile;
                java.io.BufferedOutputStream    bufferout;

                salida = new java.io.FileOutputStream(sectionMR.getLocationBinaryFile());
                bufferout=new java.io.BufferedOutputStream(salida);
                newfile=new java.io.DataOutputStream(bufferout);

                float[][] matrix=metaData.getArray();
                
                int iniX=(int)Math.min(subSetCorners[0][0],subSetCorners[1][0]);
                int iniY=(int)Math.min(subSetCorners[0][1],subSetCorners[1][1]);
                int lenX=Math.abs(subSetCorners[0][0]-subSetCorners[1][0]);
                int lenY=Math.abs(subSetCorners[0][1]-subSetCorners[1][1]);
                
                for (int j=iniY;j<iniY+lenY;j++) for (int i=iniX;i<iniX+lenX;i++) {
                    newfile.writeFloat(matrix[j][i]);
                }
                newfile.close();
                bufferout.close();
                salida.close();
                
                mainFrame.setUpGUI(true);
            } catch (java.io.IOException exc) {
                System.err.println("Failed writing section of file");
                System.err.println(exc);
            }
            
            
            
//            if (this.isDEM()){
//                if (defFileName.indexOf(".metaDEM") == -1)
//                defFileName+=".metaMDT";
//                else
//                defFileName=defFileName.substring(0,defFileName.indexOf(".metamdt"))+".metaMDT";
//            } else{
//                if (defFileName.indexOf(".metavhc") == -1)
//                defFileName+=".metaVHC";
//                else
//                defFileName=defFileName.substring(0,defFileName.indexOf(".metavhc"))+".metaVHC";
//            }
//            try{
//                java.io.File fileNewMeta = new java.io.File(defFileName);
//                javaSIH.objetos.ExtraerMapa.extraerMapa(cajaDeExtract,metaDatos,fileNewMeta,mainFrame.Idioma);
//                mainFrame.abrirRaster(fileNewMeta);
//            }catch(javaSIH.objetos.JavaSIHException ex){
//                javax.swing.JOptionPane.showMessageDialog(this,"No hay memoria suficiente","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
//            }
            
            
    }
    protected void plotTemporaryReferences(){
        for (int i=0;i<temporalReferences.size();i++){
            final int ii=i;
            Runnable addRef = new Runnable() {
                public void run() {
                    try {
                        Object[] refToAdd=(Object[]) temporalReferences.get(ii);
                        display.addReference((visad.DataReferenceImpl)refToAdd[0],(visad.ConstantMap[])refToAdd[1]);
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing temporary references - lines");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing temporary references - lines");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addRef).start();
        }
        
        for (int i=0;i<basinOutlets.size();i++){
            final int ii=i;
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        Object[] refToAdd=(Object[]) basinOutlets.get(ii);
                        display.addReferences(new visad.bom.PickManipulationRendererJ3D(),(visad.DataReferenceImpl)refToAdd[0],(visad.ConstantMap[])refToAdd[1]);
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing temporary references - points");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing temporary references - points");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addPoint).start();
        }
    }
    
    protected void plotField(){
        Runnable addField = new Runnable() {
            public void run() {
                try {
                    
                    visad.DataReferenceImpl ref_imaget1 = new visad.DataReferenceImpl("ref_imaget1");
                    ref_imaget1.setData(localField);
                    display.addReference(ref_imaget1);
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing the field");
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing the field");
                    System.err.println(exc);
                }
            }
        };
        new Thread(addField).start();
    }
    
    protected void plotNetwork(){
        if(showNetwork.isEnabled()){
            for (int i=2;i<newtorkViewerPopUp.getComponentCount();i++){
                javax.swing.JCheckBoxMenuItem theMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(i);
                int orderRequested=Integer.parseInt(theMenu.getText().substring(11,13).trim());
                if (theMenu.isSelected()) addStreamsWithOrder(orderRequested);
            }
        }
    }
    
    protected void plotVectors(){
        Object[] actVectors=mainFrame.getActiveVectors();
        for (int i=0;i<actVectors.length;i++){
            final javax.swing.JCheckBox theCheckBox=(javax.swing.JCheckBox)actVectors[i];
            if (theCheckBox.isSelected()){
                if(theCheckBox.getText().lastIndexOf(".shp") != -1){
                    if (mainFrame.getInfoManager().getDataReference("shape"+theCheckBox.getText()) == null){
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    double minX=metaData.getMinLon();
                                    double maxX=metaData.getMaxLon();
                                    double minY=metaData.getMinLat();
                                    double maxY=metaData.getMaxLat();
                                    
                                    java.awt.geom.Rectangle2D rect=new java.awt.geom.Rectangle2D.Double(minX-0.5,minY-0.5,maxX-minX+1,maxY-minY+1);
                                    ucar.unidata.gis.shapefile.EsriShapefile esri=new ucar.unidata.gis.shapefile.EsriShapefile(mainFrame.getInfoManager().dataBaseVectorsPath.getPath()+"/"+theCheckBox.getText(),rect);
                                    ucar.visad.ShapefileAdapter shF=new ucar.visad.ShapefileAdapter(esri);
                                    visad.UnionSet toPlot = shF.getData();
                                    if (toPlot != null){
                                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("shape"+theCheckBox.getText());
                                        refeElemVec.setData(toPlot);
                                        mainFrame.getInfoManager().registerDataReference("shape"+theCheckBox.getText(), refeElemVec);
                                        display.addReference(refeElemVec);
                                    }
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    } else {
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    display.addReference(mainFrame.getInfoManager().getDataReference("shape"+theCheckBox.getText()));
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    }
                }
                if(theCheckBox.getText().lastIndexOf(".dlg") != -1 || theCheckBox.getText().lastIndexOf(".opt") != -1){
                    if (mainFrame.getInfoManager().getDataReference("dlg"+theCheckBox.getText()) == null){
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    double minX=metaData.getMinLon();
                                    double maxX=metaData.getMaxLon();
                                    double minY=metaData.getMinLat();
                                    double maxY=metaData.getMaxLat();
                                    
                                    java.awt.geom.Rectangle2D rect=new java.awt.geom.Rectangle2D.Double(minX-0.5,minY-0.5,maxX-minX+1,maxY-minY+1);
                                    hydroScalingAPI.io.MetaDLG myDLG=new hydroScalingAPI.io.MetaDLG(mainFrame.getInfoManager().dataBaseVectorsPath.getPath()+"/"+theCheckBox.getText(),rect);
                                    visad.UnionSet toPlot = myDLG.getUnionSet();
                                    if (toPlot != null){
                                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("dlg"+theCheckBox.getText());
                                        refeElemVec.setData(toPlot);
                                        mainFrame.getInfoManager().registerDataReference("dlg"+theCheckBox.getText(), refeElemVec);
                                        display.addReference(refeElemVec);
                                    }
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    } else {
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    display.addReference(mainFrame.getInfoManager().getDataReference("dlg"+theCheckBox.getText()));
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    }
                }
            }
        }
    }
    
    protected void plotPolygons(){
        
        Object[] actPolygons=mainFrame.getActivePolygons();
        for (int i=0;i<actPolygons.length;i++){
            final javax.swing.JCheckBox theCheckBox=(javax.swing.JCheckBox)actPolygons[i];
            if (theCheckBox.isSelected()){
                if (mainFrame.getInfoManager().getDataReference("polygon"+theCheckBox.getText()) == null){
                    Runnable addPolygon = new Runnable() {
                        public void run() {
                            try {
                                hydroScalingAPI.io.MetaPolygon thePoly=new hydroScalingAPI.io.MetaPolygon(mainFrame.getInfoManager().dataBasePolygonsPath.getPath()+"/"+theCheckBox.getText());
                                visad.Gridded2DSet toPlot = thePoly.getPolygon();
                                if (toPlot != null){
                                    visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("polygon"+theCheckBox.getText());
                                    refeElemVec.setData(toPlot);
                                    mainFrame.getInfoManager().registerDataReference("polygon"+theCheckBox.getText(), refeElemVec);
                                    display.addReference(refeElemVec);
                                }
                            } catch (visad.VisADException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            } catch (java.io.IOException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            }
                        }
                    };
                    new Thread(addPolygon).start();
                } else {
                    Runnable addVector = new Runnable() {
                        public void run() {
                            try {
                                display.addReference(mainFrame.getInfoManager().getDataReference("polygon"+theCheckBox.getText()));
                            } catch (visad.VisADException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            } catch (java.io.IOException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            }
                        }
                    };
                    new Thread(addVector).start();
                }
            }
        }
        
    }
    
    protected void plotGauges(final boolean withNames){
        Object[] actGauges=mainFrame.getActiveGauges();
        
        for (int i=0;i<actGauges.length;i++){
            
            final hydroScalingAPI.io.MetaGauge theGauge=(hydroScalingAPI.io.MetaGauge) actGauges[i];
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                        rtref1.setData(theGauge.getPositionTuple());
                        visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(7.0, visad.Display.PointSize )};
                        display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                        visad.CellImpl cells1 = new visad.CellImpl() {
                            private boolean first = true;
                            public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                if (first) first = false;
                                else {
                                    gaugeAction(theGauge);
                                }
                            }
                        };
                        cells1.addReference(rtref1);
                        if(withNames){
                            visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                            tref.setData(theGauge.getTextTuple());
                            display.addReference(tref);
                        }
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
    
    protected void plotLocations(final boolean withNames){
        Object[] actLocations=mainFrame.getActiveLocations();
        
        for (int i=0;i<actLocations.length;i++){
            
            final hydroScalingAPI.io.MetaLocation theLocation=(hydroScalingAPI.io.MetaLocation) actLocations[i];
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                        rtref1.setData(theLocation.getPositionTuple());
                        double grey=1.0;
                        grey=theLocation.hasImages()?0.9:0.6;
                        visad.ConstantMap[] rtmaps1 = { new visad.ConstantMap(grey, visad.Display.Red ),
                                new visad.ConstantMap(grey, visad.Display.Green ),
                                new visad.ConstantMap(grey, visad.Display.Blue ),
                                new visad.ConstantMap(5.0, visad.Display.PointSize )};
                                display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                                visad.CellImpl cells1 = new visad.CellImpl() {
                                    private boolean first = true;
                                    public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                        if (first) first = false;
                                        else {
                                            locationAction(theLocation);
                                        }
                                    }
                                };
                                cells1.addReference(rtref1);
                                
                                if(withNames){
                                    visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                                    tref.setData(theLocation.getTextTuple());
                                    display.addReference(tref);
                                }
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing locations");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing locations");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addPoint).start();
        }
    }
    
    public void refreshReferences(final boolean gaugesWithNames, final boolean locationsWithNames){
        try{
            display.removeAllReferences();
        } catch (visad.VisADException exc) {
            System.err.println(exc);
        } catch (java.rmi.RemoteException rmi){
            System.err.println(rmi);
        }
        plotField();
        if(showVectorLayer.isSelected()) plotVectors();
        if(showPolygonsLayer.isSelected()) plotPolygons();
        if(showSitesLayer.isSelected()) {
            plotGauges(gaugesWithNames);
            plotLocations(locationsWithNames);
        }
        if(showNetwork.isEnabled()) plotNetwork();
        
        plotTemporaryReferences();
    }
    
    /*public void removeHeavyReferences(final boolean gaugesWithNames, final boolean locationsWithNames){
        try {
            display.removeAllReferences();
        } catch (visad.VisADException exc) {
            System.err.println(exc);
        } catch (java.io.IOException exc) {
            System.err.println(exc);
        }
     
        plotGuideMarks();
     
        if(showSitesLayer.isSelected()) {
            plotGauges(gaugesWithNames);
            plotLocations(locationsWithNames);
        }
    }
     
    private void plotGuideMarks(){
            Runnable addMarks = new Runnable() {
                public void run() {
                    try {
     
                        int gridDensity=10; int gridDensitySQ=(int)Math.pow(gridDensity,2);
     
                        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
                        visad.Gridded2DSet[] marks=new visad.Gridded2DSet[gridDensitySQ*2];
                        float[][] markCoordH=new float[2][2];
                        float[][] markCoordV=new float[2][2];
                        for (int i=0;i<gridDensity;i++){
                            for (int j=0;j<gridDensity;j++){
                                markCoordH[0][0]=(float)(metaData.getMinLon()+(j+1)/(gridDensity+1.0)*(metaData.getMaxLon()-metaData.getMinLon())-(metaData.getMaxLon()-metaData.getMinLon())/50.0);
                                markCoordH[0][1]=(float)(metaData.getMinLon()+(j+1)/(gridDensity+1.0)*(metaData.getMaxLon()-metaData.getMinLon())+(metaData.getMaxLon()-metaData.getMinLon())/50.0);
     
                                markCoordH[1][0]=(float)(metaData.getMinLat()+(i+1)/(gridDensity+1.0)*(metaData.getMaxLat()-metaData.getMinLat()));
                                markCoordH[1][1]=(float)(metaData.getMinLat()+(i+1)/(gridDensity+1.0)*(metaData.getMaxLat()-metaData.getMinLat()));
     
                                marks[i*gridDensity+j]=new visad.Gridded2DSet(domain,markCoordH,2);
     
                                markCoordV[0][0]=(float)(metaData.getMinLon()+(j+1)/(gridDensity+1.0)*(metaData.getMaxLon()-metaData.getMinLon()));
                                markCoordV[0][1]=(float)(metaData.getMinLon()+(j+1)/(gridDensity+1.0)*(metaData.getMaxLon()-metaData.getMinLon()));
     
                                markCoordV[1][0]=(float)(metaData.getMinLat()+(i+1)/(gridDensity+1.0)*(metaData.getMaxLat()-metaData.getMinLat())-(metaData.getMaxLat()-metaData.getMinLat())/50.0);
                                markCoordV[1][1]=(float)(metaData.getMinLat()+(i+1)/(gridDensity+1.0)*(metaData.getMaxLat()-metaData.getMinLat())+(metaData.getMaxLat()-metaData.getMinLat())/50.0);
     
                                marks[i*gridDensity+j+gridDensitySQ]=new visad.Gridded2DSet(domain,markCoordV,2);
                            }
                        }
     
                        visad.DataReferenceImpl marksRef = new visad.DataReferenceImpl("text");
                        marksRef.setData(new visad.UnionSet(domain, marks));
                        display.addReference(marksRef);
     
                    }
                    catch (visad.VisADException exc) {
                        System.err.println("Failed showing gauges");
                        System.err.println(exc);
                    }
                    catch (java.io.IOException exc) {
                        System.err.println("Failed showing gauges");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addMarks).start();
    }*/
    
    public void updateRelatedMaps(){
        
        Object[] mapNames=relatedMapsList.keySet().toArray();
        java.util.Arrays.sort(mapNames);
        int currentMap=0;
        for (int i=0;i<mapNames.length;i++){
            if ((mapNames[i].toString()).lastIndexOf(metaData.getName()) != -1) currentMap=i;
            relatedMaps.addItem(mapNames[i]);
        }
        relatedMaps.setSelectedIndex(currentMap);
        
        relatedMaps.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                relatedMapsItemStateChanged(evt);
            }
        });
    }
    
    protected void setLatitudeLabel(String latLabel){
        latitudeLabel.setText(latLabel);
    }
    protected void setLongitudeLabel(String lonLabel){
        longitudeLabel.setText(lonLabel);
    }
    
    protected void setValueLabel(String valLabel){
        valueLabel.setText(valLabel);
    }
    
    protected void gaugeAction(hydroScalingAPI.io.MetaGauge gaugeInfo){
        hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer gauView=new hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer(mainFrame,gaugeInfo);
        gauView.setVisible(true);
    }
    
    protected void locationAction(hydroScalingAPI.io.MetaLocation locationInfo){
        hydroScalingAPI.subGUIs.widgets.LocationsViewer locView=new hydroScalingAPI.subGUIs.widgets.LocationsViewer(mainFrame,locationInfo);
        locView.setVisible(true);
    }
    
    protected void lunchBasinAnalyzer(int MatX, int MatY){
        
        new hydroScalingAPI.subGUIs.widgets.NetworkTools(mainFrame,MatX,MatY,fullDirMatrix,metaData).setVisible(true);
        display.reDisplayAll();
        
    }
    
    public void setIdentifier(String windowIdentifier){
        myStringID=windowIdentifier;
    }
    
    public String getIdentifier(){
        return myStringID;
    }
    
    protected void addToNetworkPupUp(java.awt.Component item){
        newtorkViewerPopUp.add(item);
    }
    
    protected void selectAllNetworkPopup(){
        javax.swing.JCheckBoxMenuItem selectAllMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(0);
        for (int i=2;i<newtorkViewerPopUp.getComponentCount();i++){
            javax.swing.JCheckBoxMenuItem theMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(i);
            theMenu.setSelected(selectAllMenu.isSelected());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        newtorkViewerPopUp = new javax.swing.JPopupMenu();
        basinsViewerPopUp = new javax.swing.JPopupMenu();
        editLog = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        toolsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        visualToolbar = new javax.swing.JToolBar();
        restoreDisplay = new javax.swing.JButton();
        zoomInDisplay = new javax.swing.JButton();
        zoomOutDisplay = new javax.swing.JButton();
        snapshot = new javax.swing.JButton();
        print = new javax.swing.JButton();
        showControls = new javax.swing.JButton();
        demToolbar = new javax.swing.JToolBar();
        showNetwork = new javax.swing.JButton();
        showBasins = new javax.swing.JButton();
        selectOutlet = new javax.swing.JToggleButton();
        traceStream = new javax.swing.JToggleButton();
        locationToolbar = new javax.swing.JToolBar();
        createLocation = new javax.swing.JToggleButton();
        cutSubDataSet = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        relatedMaps = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        jPanel61 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        jPanel62 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        layersPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        showVectorLayer = new javax.swing.JCheckBox();
        showLinesLayer = new javax.swing.JCheckBox();
        showPolygonsLayer = new javax.swing.JCheckBox();
        showSitesLayer = new javax.swing.JCheckBox();

        newtorkViewerPopUp.setFont(new java.awt.Font("Dialog", 0, 10));
        newtorkViewerPopUp.setLightWeightPopupEnabled(false);
        basinsViewerPopUp.setFont(new java.awt.Font("Dialog", 0, 10));
        basinsViewerPopUp.setLightWeightPopupEnabled(false);
        editLog.setFont(new java.awt.Font("Dialog", 0, 10));
        editLog.setText("Edit Basins Log");
        basinsViewerPopUp.add(editLog);

        basinsViewerPopUp.add(jSeparator1);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        toolsPanel.setLayout(new java.awt.GridLayout(2, 1));

        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.X_AXIS));

        jPanel7.setMinimumSize(new java.awt.Dimension(18, 14));
        jPanel7.setPreferredSize(new java.awt.Dimension(140, 22));
        visualToolbar.setFont(new java.awt.Font("Dialog", 0, 10));
        visualToolbar.setPreferredSize(new java.awt.Dimension(130, 22));
        restoreDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Home16.gif")));
        restoreDisplay.setToolTipText("Restore");
        restoreDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        restoreDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        restoreDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreDisplayActionPerformed(evt);
            }
        });

        visualToolbar.add(restoreDisplay);

        zoomInDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ZoomIn16.gif")));
        zoomInDisplay.setToolTipText("Zoom In");
        zoomInDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        zoomInDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        zoomInDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInDisplayActionPerformed(evt);
            }
        });

        visualToolbar.add(zoomInDisplay);

        zoomOutDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ZoomOut16.gif")));
        zoomOutDisplay.setToolTipText("Zoom Out");
        zoomOutDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        zoomOutDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        zoomOutDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutDisplayActionPerformed(evt);
            }
        });

        visualToolbar.add(zoomOutDisplay);

        snapshot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Snapshot.gif")));
        snapshot.setToolTipText("Save JPG ...");
        snapshot.setMaximumSize(new java.awt.Dimension(18, 18));
        snapshot.setMinimumSize(new java.awt.Dimension(18, 18));
        snapshot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapshotActionPerformed(evt);
            }
        });

        visualToolbar.add(snapshot);

        print.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Print.gif")));
        print.setToolTipText("Print ...");
        print.setMaximumSize(new java.awt.Dimension(18, 18));
        print.setMinimumSize(new java.awt.Dimension(18, 18));
        print.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printActionPerformed(evt);
            }
        });

        visualToolbar.add(print);

        showControls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ControlPanel.gif")));
        showControls.setToolTipText("Control Panel");
        showControls.setMaximumSize(new java.awt.Dimension(18, 18));
        showControls.setMinimumSize(new java.awt.Dimension(18, 18));
        showControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showControlsActionPerformed(evt);
            }
        });

        visualToolbar.add(showControls);

        jPanel7.add(visualToolbar);

        demToolbar.setFont(new java.awt.Font("Dialog", 0, 10));
        demToolbar.setPreferredSize(new java.awt.Dimension(130, 22));
        showNetwork.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Network.gif")));
        showNetwork.setToolTipText("Plot Network");
        showNetwork.setMaximumSize(new java.awt.Dimension(18, 18));
        showNetwork.setMinimumSize(new java.awt.Dimension(18, 18));
        showNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNetworkActionPerformed(evt);
            }
        });

        demToolbar.add(showNetwork);

        showBasins.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Predef_Outlet.gif")));
        showBasins.setToolTipText("Predefinded Outlets");
        showBasins.setMaximumSize(new java.awt.Dimension(18, 18));
        showBasins.setMinimumSize(new java.awt.Dimension(18, 18));
        showBasins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBasinsActionPerformed(evt);
            }
        });

        demToolbar.add(showBasins);

        selectOutlet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Outlet.gif")));
        selectOutlet.setToolTipText("Select outlet");
        selectOutlet.setMaximumSize(new java.awt.Dimension(18, 18));
        selectOutlet.setMinimumSize(new java.awt.Dimension(18, 18));
        selectOutlet.setPreferredSize(new java.awt.Dimension(18, 18));
        selectOutlet.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/OutletActive.gif")));
        selectOutlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectOutletActionPerformed(evt);
            }
        });

        demToolbar.add(selectOutlet);

        traceStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/TraceRiver.gif")));
        traceStream.setToolTipText("Trace River");
        traceStream.setMaximumSize(new java.awt.Dimension(18, 18));
        traceStream.setMinimumSize(new java.awt.Dimension(18, 18));
        traceStream.setPreferredSize(new java.awt.Dimension(18, 18));
        traceStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/TraceRiverActive.gif")));
        traceStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceStreamActionPerformed(evt);
            }
        });

        demToolbar.add(traceStream);

        jPanel7.add(demToolbar);

        locationToolbar.setFont(new java.awt.Font("Dialog", 0, 10));
        locationToolbar.setPreferredSize(new java.awt.Dimension(130, 22));
        createLocation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/NewLocation.gif")));
        createLocation.setToolTipText("Create New Location");
        createLocation.setMaximumSize(new java.awt.Dimension(18, 18));
        createLocation.setMinimumSize(new java.awt.Dimension(18, 18));
        createLocation.setPreferredSize(new java.awt.Dimension(18, 18));
        createLocation.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/NewLocationActive.gif")));
        createLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLocationActionPerformed(evt);
            }
        });

        locationToolbar.add(createLocation);

        cutSubDataSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/CutSubDem.gif")));
        cutSubDataSet.setToolTipText("Create New Location");
        cutSubDataSet.setMaximumSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setMinimumSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setPreferredSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/CutSubDemActive.gif")));
        cutSubDataSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutSubDataSetActionPerformed(evt);
            }
        });

        locationToolbar.add(cutSubDataSet);

        jPanel7.add(locationToolbar);

        jPanel3.add(jPanel7);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel1.setText("Switch to : ");
        jPanel4.add(jLabel1, java.awt.BorderLayout.WEST);

        relatedMaps.setFont(new java.awt.Font("Dialog", 0, 10));
        relatedMaps.setLightWeightPopupEnabled(false);
        relatedMaps.setMaximumSize(new java.awt.Dimension(32767, 18));
        relatedMaps.setMinimumSize(new java.awt.Dimension(32, 18));
        relatedMaps.setPreferredSize(new java.awt.Dimension(32, 18));
        jPanel4.add(relatedMaps, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4);

        toolsPanel.add(jPanel3);

        jPanel2.setLayout(new java.awt.GridLayout(1, 3));

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel3.setText("Latitude : ");
        jPanel6.add(jLabel3, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel6.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel6);

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel31.setText("Longitude : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel61);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel32.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel32.setText("Value : ");
        jPanel62.add(jLabel32, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel62);

        toolsPanel.add(jPanel2);

        getContentPane().add(toolsPanel, java.awt.BorderLayout.NORTH);

        layersPanel.setLayout(new java.awt.GridLayout(1, 5));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText("Active Layers: ");
        layersPanel.add(jLabel2);

        showVectorLayer.setFont(new java.awt.Font("Dialog", 0, 10));
        showVectorLayer.setSelected(true);
        showVectorLayer.setText("Vectors");
        showVectorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVectorLayerActionPerformed(evt);
            }
        });

        layersPanel.add(showVectorLayer);

        showLinesLayer.setFont(new java.awt.Font("Dialog", 0, 10));
        showLinesLayer.setSelected(true);
        showLinesLayer.setText("Lines");
        showLinesLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLinesLayerActionPerformed(evt);
            }
        });

        layersPanel.add(showLinesLayer);

        showPolygonsLayer.setFont(new java.awt.Font("Dialog", 0, 10));
        showPolygonsLayer.setSelected(true);
        showPolygonsLayer.setText("Polygons");
        showPolygonsLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPolygonsLayerActionPerformed(evt);
            }
        });

        layersPanel.add(showPolygonsLayer);

        showSitesLayer.setFont(new java.awt.Font("Dialog", 0, 10));
        showSitesLayer.setSelected(true);
        showSitesLayer.setText("Sites");
        showSitesLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSitesLayerActionPerformed(evt);
            }
        });

        layersPanel.add(showSitesLayer);

        getContentPane().add(layersPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void cutSubDataSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutSubDataSetActionPerformed
    if(cutSubDataSet.isSelected()) {
            selectOutlet.setSelected(false);
            traceStream.setSelected(false);
            createLocation.setSelected(false);
            activeEvent=4;
            subSetCornersCounter=0;
        } else activeEvent=0;
    }//GEN-LAST:event_cutSubDataSetActionPerformed

    private void createLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLocationActionPerformed
    if(createLocation.isSelected()) {
            selectOutlet.setSelected(false);
            traceStream.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=3;
        } else activeEvent=0;
    }//GEN-LAST:event_createLocationActionPerformed

    private void showBasinsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBasinsActionPerformed
        javax.swing.JButton theButton= (javax.swing.JButton)evt.getSource();
        basinsViewerPopUp.show(theButton, 0,theButton.getHeight());
    }//GEN-LAST:event_showBasinsActionPerformed

    private void traceStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceStreamActionPerformed
        if(traceStream.isSelected()) {
            selectOutlet.setSelected(false);
            createLocation.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=2;
        } else activeEvent=0;
    }//GEN-LAST:event_traceStreamActionPerformed

    private void selectOutletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectOutletActionPerformed
        if(selectOutlet.isSelected()) {
            traceStream.setSelected(false);
            createLocation.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=1; 
        } else activeEvent=0;
    }//GEN-LAST:event_selectOutletActionPerformed
    
    private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
        Thread t = new Thread() {
            public void run() {
                
                java.awt.print.PrinterJob printJob = java.awt.print.PrinterJob.getPrinterJob();
                printJob.setPrintable(display.getPrintable());
                if(printJob.printDialog()){
                    try {
                        printJob.print();
                    }
                    catch (Exception pe) {
                        pe.printStackTrace();
                    }
                }
            }
        };
        
        t.start();
        
    }//GEN-LAST:event_printActionPerformed
    
    private void showSitesLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSitesLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showSitesLayerActionPerformed
    
    private void showPolygonsLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPolygonsLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showPolygonsLayerActionPerformed
    
    private void showLinesLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLinesLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showLinesLayerActionPerformed
    
    private void showVectorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVectorLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showVectorLayerActionPerformed
    
    private void showNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNetworkActionPerformed
        javax.swing.JButton theButton= (javax.swing.JButton)evt.getSource();
        newtorkViewerPopUp.show(theButton, 0,theButton.getHeight());
    }//GEN-LAST:event_showNetworkActionPerformed
    
    private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showControlsActionPerformed
        new hydroScalingAPI.subGUIs.widgets.RasterViewerControlsDialog(mainFrame,latitudeMap,longitudeMap,heightMap,colorScaleMap).setVisible(true);
    }//GEN-LAST:event_showControlsActionPerformed
        
    private void snapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapshotActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser();
        javax.swing.filechooser.FileFilter jpgFilter = new visad.util.ExtensionFileFilter("jpg","JPEG File");
        fc.addChoosableFileFilter(jpgFilter);
        fc.showSaveDialog(this);
        
        if(fc.getSelectedFile() == null) return;
        
        final java.io.File f=fc.getSelectedFile();
        Runnable captureImage = new Runnable() {
            public void run() {
                String msg = "Could not save image snapshot to file \"" + f.getName() +
                "\" in JPEG format. ";
                try {
                    saveImage(f);
                }
                catch (visad.VisADException exc) {
                    msg = msg + "An error occurred: " + exc.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, msg, "Error saving data",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                catch (java.io.IOException exc) {
                    msg = msg + "An I/O error occurred: " + exc.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, msg, "Error saving data",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        Thread t = new Thread(captureImage);
        t.start();
    }//GEN-LAST:event_snapshotActionPerformed
    
    private void zoomOutDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutDisplayActionPerformed
        try{
            visad.ProjectionControl pc = display.getProjectionControl();
            double[] val=pc.getMatrix();
            if(val[0] > 0.5){
                val[0]*=0.5;
                val[5]*=0.5;
                
                visad.VisADRay rayLR = dr.getMouseBehavior().findRay(this.getContentPane().getWidth()/2, (this.getContentPane().getHeight()-toolsPanel.getHeight()-layersPanel.getHeight())/2);

                val[3]=-rayLR.position[0]*val[0];     //final X position
                val[7]=-rayLR.position[1]*val[3];     //final Y position

                pc.setMatrix(val);
            }
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_zoomOutDisplayActionPerformed
    
    private void zoomInDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInDisplayActionPerformed
        try{
            
            java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
            java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
            dpoint4.applyPattern("0.00000");
            
            visad.ProjectionControl pc = display.getProjectionControl();
            double[] val=pc.getMatrix();
            
            val[0]*=2;
            val[5]*=2;

            visad.VisADRay rayLR = dr.getMouseBehavior().findRay(this.getContentPane().getWidth()/2, (this.getContentPane().getHeight()-toolsPanel.getHeight()-layersPanel.getHeight())/2);

            val[3]=-rayLR.position[0]*val[0];     //final X position
            val[7]=-rayLR.position[1]*val[3];     //final Y position*/

            pc.setMatrix(val);
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_zoomInDisplayActionPerformed
    
    private void restoreDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreDisplayActionPerformed
        try{
            display.getProjectionControl().resetProjection();
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_restoreDisplayActionPerformed
    
    private void relatedMapsItemStateChanged(java.awt.event.ItemEvent evt) {

        if (evt.getStateChange() == 1){
            Object requestedMap =evt.getItem();
            if(!(requestedMap.toString().indexOf("Map Not Available") != -1)){
                java.io.File fileToLoad=new java.io.File((String)relatedMapsList.get(requestedMap));
                if (!fileToLoad.equals(metaData.getLocationBinaryFile())){
                    String extension=fileToLoad.getName();
                    extension=extension.substring(extension.lastIndexOf("."));
                    metaData.setLocationBinaryFile(fileToLoad);
                    if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 || metaData.getLocationBinaryFile().getName().lastIndexOf(".vhc") != -1){
                        metaData.restoreOriginalFormat();
                    } else{
                        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(extension));
                    }
                    try{
                        localField=metaData.getField();
                    } catch (visad.VisADException ve){
                        System.err.println("Failed loading field");
                        System.err.println(ve);
                    }  catch (java.io.IOException ioe){
                        System.err.println("Failed loading field");
                        System.err.println(ioe);
                    }
                    
                    try {
                    
                        //If DEM load the Elevations Color Table
                       hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes=new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);

                       if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1)
                           availablePalettes.setSelectedTable("Elevations");
                       else
                           availablePalettes.setSelectedTable("Rainbow");
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing the field");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing the field");
                        System.err.println(exc);
                    }

                    refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
                }
            }

        }

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu basinsViewerPopUp;
    private javax.swing.JToggleButton createLocation;
    private javax.swing.JToggleButton cutSubDataSet;
    private javax.swing.JToolBar demToolbar;
    private javax.swing.JMenuItem editLog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JPanel layersPanel;
    private javax.swing.JToolBar locationToolbar;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JPopupMenu newtorkViewerPopUp;
    private javax.swing.JButton print;
    private javax.swing.JComboBox relatedMaps;
    private javax.swing.JButton restoreDisplay;
    private javax.swing.JToggleButton selectOutlet;
    private javax.swing.JButton showBasins;
    private javax.swing.JButton showControls;
    private javax.swing.JCheckBox showLinesLayer;
    private javax.swing.JButton showNetwork;
    private javax.swing.JCheckBox showPolygonsLayer;
    private javax.swing.JCheckBox showSitesLayer;
    private javax.swing.JCheckBox showVectorLayer;
    private javax.swing.JButton snapshot;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JToggleButton traceStream;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JToolBar visualToolbar;
    private javax.swing.JButton zoomInDisplay;
    private javax.swing.JButton zoomOutDisplay;
    // End of variables declaration//GEN-END:variables
    
}
