/*
 * TRIBS_io.java
 *
 * Created on January 24, 2007, 1:27 PM
 */

package hydroScalingAPI.modules.tRIBS_io.widgets;


import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
    
/**
 *
 * @author  Ricardo Mantilla
 */
public class TRIBS_io extends java.awt.Dialog {
    
    private RealType    xIndex=RealType.getRealType("xIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("posXY");
    
    private RealTupleType espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor});
    
    private FunctionType func_xEasting_yNorthing=new FunctionType(xIndex, espacioXLYL);
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    public hydroScalingAPI.io.MetaRaster metaDatos;
    public byte[][] matDir;
    public hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myBasinResults;
    public hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
    
    private int[][] magnitudes;
    
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    
    private DisplayImplJ3D display_TIN;
    private ScalarMap eastMap,northMap,pointsMap;
    
    private visad.java3d.DisplayRendererJ3D dr;
    
    /**
     * Creates new form TRIBS_io
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, int[][] magnit, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        super(parent, true);
        initComponents();
        
        matDir=direcc;
        metaDatos=md;
        mainFrame=parent;
        magnitudes=magnit;
        
        //Set up general interface
        setBounds(0,0, 780, 500);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        
        //Get Data concerning the all Rainfall-Runoff module
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        
        //Graphical structure for triangulated points
        dr=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN = new DisplayImplJ3D("displayTIN",dr);
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) display_TIN.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        ProjectionControl pc = display_TIN.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, 0.5});        
        eastMap = new ScalarMap( xEasting , Display.XAxis );
        eastMap.setScalarName("East Coordinate");
        northMap = new ScalarMap( yNorthing , Display.YAxis );
        northMap.setScalarName("North Coordinate");
        pointsMap=new ScalarMap( nodeColor , Display.RGB );

        display_TIN.addMap(eastMap);
        display_TIN.addMap(northMap);
        display_TIN.addMap(pointsMap);
        
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        int[][] xyDivide=myCuenca.getXYBasinDivide();
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();

        float[][] xyLinkValues=new float[3][xyBasin[0].length];
        
        
        xyLinkValues[0][0]=(float)(lonLatsBasin[0][0]+metaDatos.getResLon()/2.0);
        xyLinkValues[1][0]=(float)(lonLatsBasin[1][0]+metaDatos.getResLat()/2.0);
        xyLinkValues[2][0]=2;
                
        for(int i=1;i<xyBasin[0].length;i++){
            if(magnit[xyBasin[1][i]][xyBasin[0][i]] > 0){
                xyLinkValues[0][i]=(float)(lonLatsBasin[0][i]+metaDatos.getResLon()/2.0);
                xyLinkValues[1][i]=(float)(lonLatsBasin[1][i]+metaDatos.getResLat()/2.0);
                xyLinkValues[2][i]=0;
            } else {
                xyLinkValues[0][i]=(float)(lonLatsBasin[0][i]+metaDatos.getResLon()/2.0);
                xyLinkValues[1][i]=(float)(lonLatsBasin[1][i]+metaDatos.getResLat()/2.0);
                xyLinkValues[2][i]=3;
            }
        }
//        for(int i=0;i<lonLatsDivide[0].length-1;i++) System.out.println(((lonLatsDivide[0][i]+lonLatsDivide[0][i+1])/2.0)+" "+((lonLatsDivide[1][i]+lonLatsDivide[1][i+1])/2.0)+" "+fixedDem[xyDivide[1][i]][xyDivide[0][i]]+" "+1);

        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        DataReferenceImpl data_refLi = new DataReferenceImpl("data_ref_LINK");
        data_refLi.setData(vals_ff_Li);
        
        ConstantMap[] pointsCMap = {    //new ConstantMap( 1.0f, Display.Red),
                                        //new ConstantMap( 1.0f, Display.Green),
                                        //new ConstantMap( 0.0f, Display.Blue),
                                        new ConstantMap( 4.50f, Display.PointSize)};
            
        display_TIN.addReference( data_refLi,pointsCMap );
        
        display_TIN.getComponent().addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int rot = e.getWheelRotation();
                try{
                    ProjectionControl pc1 = display_TIN.getProjectionControl();
                    double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                            1.0, 1.0, 1.0,
                            0.0, 0.0, 0.0);
                    double[] currentMatrix = pc1.getMatrix();
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
                    pc1.setMatrix(scaleMatrix);
                } catch (java.rmi.RemoteException re) {} catch (visad.VisADException ve) {}
            }
        });
        
        jPanel1.add("Center",display_TIN.getComponent());
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        panelOpciones = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        zrSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        exportButton = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        zrSlider.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        zrSlider.setValue(0);
        zrSlider.setName("null");
        jPanel2.add(zrSlider, java.awt.BorderLayout.CENTER);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Zr");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel1, java.awt.BorderLayout.WEST);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        jPanel2.add(exportButton, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        panelOpciones.addTab("TIN", jPanel1);

        add(panelOpciones, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
//        try{
//            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBasePolygonsPath);
//            fc.setFileSelectionMode(fc.FILES_ONLY);
//            fc.setDialogTitle("Directory Selection");
//            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("points","Points File");
//            fc.addChoosableFileFilter(mdtFilter);
//            fc.showSaveDialog(this);
//
//            //if (fc.getSelectedFile() == null) return;
//            
//            java.io.File theFile=metaDatos.getLocationBinaryFile();
//            metaDatos.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".redRas"));
//            metaDatos.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".redRas"));
//            byte[][] rasterNet=new hydroScalingAPI.io.DataRaster(metaDatos).getByte();
//            
//            metaDatos.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".corrDEM"));
//            metaDatos.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
//            double[][] fixedDem=new hydroScalingAPI.io.DataRaster(metaDatos).getDouble();
//            
//        
//            hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOut,yOut,matDir,metaDatos);
//            float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
//            int[][] xyDivide=myCuenca.getXYBasinDivide();
//            float[][] lonLatsBasin=myCuenca.getLonLatBasin();
//            int[][] xyBasin=myCuenca.getXYBasin();
//            
//            System.out.println((lonLatsDivide[0].length+lonLatsBasin[0].length-1));
//            System.out.println((lonLatsBasin[0][0]+metaDatos.getResLon()/2.0)+" "+(lonLatsBasin[1][0]+metaDatos.getResLat()/2.0)+" "+fixedDem[xyBasin[1][0]][xyBasin[0][0]]+" "+2);
//            
//            for(int i=0;i<xyBasin[0].length;i++){
//                if(rasterNet[xyBasin[1][i]][xyBasin[0][i]] != 1)
//                    System.out.println((lonLatsBasin[0][i]+metaDatos.getResLon()/2.0)+" "+(lonLatsBasin[1][i]+metaDatos.getResLat()/2.0)+" "+fixedDem[xyBasin[1][i]][xyBasin[0][i]]+" "+0);
//                else
//                    System.out.println((lonLatsBasin[0][i]+metaDatos.getResLon()/2.0)+" "+(lonLatsBasin[1][i]+metaDatos.getResLat()/2.0)+" "+fixedDem[xyBasin[1][i]][xyBasin[0][i]]+" "+3);
//                
//            }
//            for(int i=0;i<lonLatsDivide[0].length-1;i++) System.out.println(((lonLatsDivide[0][i]+lonLatsDivide[0][i+1])/2.0)+" "+((lonLatsDivide[1][i]+lonLatsDivide[1][i+1])/2.0)+" "+fixedDem[xyDivide[1][i]][xyDivide[0][i]]+" "+1);
//            mainFrame.setUpGUI(true);
//            
//            closeDialog(null);
//        } catch (java.io.IOException IOE){
//            System.err.println("Failed creating polygon file for this basin. "+xOut+" "+yOut);
//            System.err.println(IOE);
//        }
    }//GEN-LAST:event_exportButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            java.io.File theFile=new java.io.File("/Users/ricardo/Documents/databases/Test_DB/Rasters/Topography/58447060.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            
            new TRIBS_io(tempFrame, 85, 42,matDirs,magnitudes,metaModif).setVisible(true);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        

        System.exit(0);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane panelOpciones;
    private javax.swing.JSlider zrSlider;
    // End of variables declaration//GEN-END:variables
    
}
