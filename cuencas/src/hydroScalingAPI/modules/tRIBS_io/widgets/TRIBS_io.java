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

import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;
    
/**
 *
 * @author  Ricardo Mantilla
 */
public class TRIBS_io extends java.awt.Dialog {
    
    private RealType    xIndex=RealType.getRealType("xIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("posXY");
    
    private RealTupleType   espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor}),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    private FunctionType func_xEasting_yNorthing=new FunctionType(xIndex, espacioXLYL);
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    public hydroScalingAPI.io.MetaRaster metaDatos;
    public byte[][] matDir;
    public hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myBasinResults;
    public hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
    
    private int[][] magnitudes;
    float[][] DEM;
    public float[] corrDEM;
    
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    
    private DisplayImplJ3D display_TIN;
    private ScalarMap eastMap,northMap,pointsMap;
    
    private visad.java3d.DisplayRendererJ3D dr;
    
    private byte[][] directionsKey={{1,2,3},{4,5,6},{7,8,9}};
    private java.util.Vector pointsInTriangulation,typeOfPoint,filteredPointsInTriangulation,filteredTypeOfPoint;
    private int numPointsBasin=0;
    private DataReferenceImpl data_refLi,data_refTr;
    
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
        setBounds(0,0, 950, 700);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaDatos);
        metaData.setLocationBinaryFile(new java.io.File(metaDatos.getLocationMeta().getPath().substring(0,metaDatos.getLocationMeta().getPath().lastIndexOf("."))+".corrDEM"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        DEM=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        //Get Data concerning the module
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        
        //Graphical structure for triangulated points
        dr=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN = new DisplayImplJ3D("displayTIN",dr);
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) display_TIN.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap = new ScalarMap( xEasting , Display.XAxis );
        eastMap.setScalarName("East Coordinate");
        northMap = new ScalarMap( yNorthing , Display.YAxis );
        northMap.setScalarName("North Coordinate");
        pointsMap=new ScalarMap( nodeColor , Display.RGB );

        display_TIN.addMap(eastMap);
        display_TIN.addMap(northMap);
        display_TIN.addMap(pointsMap);
        
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
        
        initializePoints();
        jPanel1.add("Center",display_TIN.getComponent());
        
    }
    
    private float pointLaplacian(int x,int y){
        
        int i=y;
        int j=x;
        
        float LAP;

        if(matDir[i][j] == 1 || matDir[i][j] == 3 ||matDir[i][j] == 7 ||matDir[i][j] == 9)
            LAP=(DEM[i-1][j]-2*DEM[i][j]+DEM[i+1][j])/30.0f+(DEM[i][j-1]-2*DEM[i][j]+DEM[i][j+1])/30.0f;
        else
            LAP=(DEM[i-1][j-1]-2*DEM[i][j]+DEM[i+1][j+1])/30.0f+(DEM[i-1][j+1]-2*DEM[i][j]+DEM[i+1][j-1])/30.0f;
        if(Math.abs(LAP) < 1e-4) {
            LAP=0;
        }
        
        return LAP;
    }
    
    private void writeTriangulation(Delaunay delaun){
        
    }
    
    private void plotPoints(float Zr) throws RemoteException, VisADException{
        
        int[][] xyBasin=myCuenca.getXYBasin();
        byte[][] basMask=myCuenca.getBasinMask();
        
        filteredPointsInTriangulation=(java.util.Vector)pointsInTriangulation.clone();
        filteredTypeOfPoint=(java.util.Vector)typeOfPoint.clone();
        
        int numPRemoved=0;
        int pB=0;
        for(int i=0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] <= 0){
                boolean neigh=false;
                for (int k=0; k <= 8; k++){
                    int jj=xyBasin[1][i]+(k/3)-1;
                    int ii=xyBasin[0][i]+(k%3)-1;
                    //if (magnitudes[jj][ii] > 0 || basMask[jj][ii] == 0){
                    if (basMask[jj][ii] == 0){
                        neigh=true;
                    }
                }
                //if(!neigh && Math.random()*100.0 < Zr){
                if(!neigh){
                    if(Math.abs(pointLaplacian(xyBasin[0][i],xyBasin[1][i]))<1e-4) {
                    //if(pointLaplacian(xyBasin[0][i],xyBasin[1][i])<1e-4) {
                        int pToRemove=(int)(pB-numPRemoved);
                        filteredPointsInTriangulation.remove(pToRemove);
                        filteredTypeOfPoint.remove(pToRemove);
                        numPRemoved++;
                    }
                }
                pB++;
            }
        }
        
        int numPoints=filteredPointsInTriangulation.size();
        float[][] xyLinkValues=new float[3][numPoints];
        for(int i=0;i<numPoints;i++){
            Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
            xyLinkValues[0][i]=(float)utmLocal.x;
            xyLinkValues[1][i]=(float)utmLocal.y;
            xyLinkValues[2][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
        }

        xyLinkValues[2][numPoints-1]=1;
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        data_refLi.setData(vals_ff_Li);
        
        if(Zr > 70){
            float[][] samples=new float[2][];
            samples[0]=xyLinkValues[0];
            samples[1]=xyLinkValues[1];
            System.out.println("the DelaunayClarkson algorithm.");
            long start = System.currentTimeMillis();
            Delaunay.perturb(samples,0.1f,false);
            Delaunay delaun = (Delaunay) new DelaunayClarkson(samples);
            long end = System.currentTimeMillis();
            float time = (end - start) / 1000f;
            System.out.println("Triangulation took " + time + " seconds.");
            
            
            System.out.println(delaun.NumEdges);
            
            for(int j=0;j<delaun.Edges.length;j++){
                for(int i=0;i<delaun.Edges[j].length;i++){
                    System.out.print(delaun.Edges[j][i]+"\t\t");
                }
                System.out.println();
            }
            
            Gridded2DSet[] triangles=new Gridded2DSet[delaun.Tri.length];
            float[][] lines=new float[2][4];
            for(int j=0;j<delaun.Tri.length;j++){
                for(int i=0;i<3;i++){
                    lines[0][i]=samples[0][delaun.Tri[j][i]];
                    lines[1][i]=samples[1][delaun.Tri[j][i]];
                }
                lines[0][3]=samples[0][delaun.Tri[j][0]];
                lines[1][3]=samples[1][delaun.Tri[j][0]];
                triangles[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);
            }
            UnionSet allTriang=new UnionSet(domainXLYL,triangles);
            
            data_refTr.setData(allTriang);
            
            for(int j=0;j<10;j++) {
                float[][] lines1=new float[2][delaun.Vertices[j].length];
                for(int i=0;i<delaun.Vertices[j].length;i++){
                    for(int k=0;k<3;k++){
                        lines1[0][i]+=samples[0][delaun.Tri[delaun.Vertices[j][i]][k]];
                        lines1[1][i]+=samples[1][delaun.Tri[delaun.Vertices[j][i]][k]];
                    }
                    lines1[0][i]/=3.0;
                    lines1[1][i]/=3.0;
                    
                }

                DataReferenceImpl data_ref = new DataReferenceImpl("data_ref_"+j);
                data_ref.setData(new Gridded2DSet(domainXLYL,lines1,lines1[0].length));
                ConstantMap[] linesCMap = {     new ConstantMap( 1.0f, Display.Red),
                                                new ConstantMap( 0.0f, Display.Green),
                                                new ConstantMap( 1.0f, Display.Blue),
                                                new ConstantMap( 1.50f, Display.LineWidth)};

                display_TIN.addReference( data_ref,linesCMap );
            }
            
        }
        
        System.out.println(filteredPointsInTriangulation.size()/(float)pointsInTriangulation.size());
        System.out.println((1-(numPointsBasin-numPRemoved)/(float)numPointsBasin));

        
    }
    
    private void initializePoints() throws RemoteException, VisADException, java.io.IOException {
        
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        corrDEM=myCuenca.getElevations();
        
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        int[][] xyDivide=myCuenca.getXYBasinDivide();
        
        pointsInTriangulation=new java.util.Vector();
        typeOfPoint=new java.util.Vector();
        
        //adding points inside the basin
        for (int i = 0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] <= 0){
                pointsInTriangulation.add(new Gdc_Coord_3d(lonLatsBasin[1][i]+metaDatos.getResLat()/3600.0/2.0,lonLatsBasin[0][i]+metaDatos.getResLon()/3600.0/2.0,corrDEM[0]));
                typeOfPoint.add(new int[] {3});
                numPointsBasin++;
            }
        }
        
        float bfs=0.8f;
        
        //adding points along the river network
        for (int i = 0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] > 0){
                int yP=xyBasin[1][i];
                int xP=xyBasin[0][i];
                
                double latP=lonLatsBasin[1][i]+metaDatos.getResLat()/3600.0/2.0;
                double lonP=lonLatsBasin[0][i]+metaDatos.getResLon()/3600.0/2.0;
                
                pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,corrDEM[0]));
                typeOfPoint.add(new int[] {0});
                if (i>0){
                    int delta_yP=((matDir[(int)yP][xP]-1)/3)-1;
                    int delta_xP=((matDir[yP][xP]-1)%3)-1;

                    double nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*1.0/4.0;
                    double nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*1.0/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                    typeOfPoint.add(new int[] {0});
                    
//                        nLatP=latP+(1*delta_yP-bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
//                        nLonP=lonP+(1*delta_xP+bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;
//
//                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
//                        typeOfPoint.add(new int[] {2});
//
//                        nLatP=latP+(1*delta_yP+bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
//                        nLonP=lonP+(1*delta_xP-bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;
//
//                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
//                        typeOfPoint.add(new int[] {2});
                    
                    nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*2.0/4.0;
                    nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*2.0/4.0;
                    
                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                    typeOfPoint.add(new int[] {0});
                    
                        nLatP=latP+(2*delta_yP-bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
                        nLonP=lonP+(2*delta_xP+bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                        typeOfPoint.add(new int[] {2});

                        nLatP=latP+(2*delta_yP+bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
                        nLonP=lonP+(2*delta_xP-bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                        typeOfPoint.add(new int[] {2});

                    nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*3.0/4.0;
                    nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*3.0/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                    typeOfPoint.add(new int[] {0});
                    
//                        nLatP=latP+(3*delta_yP-bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
//                        nLonP=lonP+(3*delta_xP+bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;
//
//                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
//                        typeOfPoint.add(new int[] {2});
//
//                        nLatP=latP+(3*delta_yP+bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
//                        nLonP=lonP+(3*delta_xP-bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;
//
//                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
//                        typeOfPoint.add(new int[] {2});
                    
                    if(magnitudes[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] <= 0 || matDir[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] != 10-directionsKey[1-delta_xP][1+delta_yP]){
                        nLatP=latP-delta_xP*metaDatos.getResLat()/3600.0*bfs/4.0;
                        nLonP=lonP+delta_yP*metaDatos.getResLon()/3600.0*bfs/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                        typeOfPoint.add(new int[] {2});
                    }
                    
                    if(magnitudes[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] <= 0 || matDir[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] != 10-directionsKey[1+delta_xP][1-delta_yP]){
                        nLatP=latP+delta_xP*metaDatos.getResLat()/3600.0*bfs/4.0;
                        nLonP=lonP-delta_yP*metaDatos.getResLon()/3600.0*bfs/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,corrDEM[0]));
                        typeOfPoint.add(new int[] {2});
                    }
                    
                }
            }
        }
        //adding points in the basin divide
        for(int i=0;i<lonLatsDivide[0].length-1;i++){
            pointsInTriangulation.add(new Gdc_Coord_3d((lonLatsDivide[1][i]+lonLatsDivide[1][i+1])/2.0,(lonLatsDivide[0][i]+lonLatsDivide[0][i+1])/2.0,corrDEM[0]));
            typeOfPoint.add(new int[] {2});
        }

        hydroScalingAPI.tools.Stats eastStats=new hydroScalingAPI.tools.Stats(lonLatsDivide[0]);
        hydroScalingAPI.tools.Stats nortStats=new hydroScalingAPI.tools.Stats(lonLatsDivide[1]);
        
        ProjectionControl pc = display_TIN.getProjectionControl();
        pc.setAspectCartesian(new double[] {1, Math.max((eastStats.maxValue-eastStats.minValue),(nortStats.maxValue-nortStats.minValue))/Math.min((eastStats.maxValue-eastStats.minValue),(nortStats.maxValue-nortStats.minValue))});        
        
        
        int numPoints=pointsInTriangulation.size();
        Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
        Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];
        
        for(int i=0;i<numPoints;i++){
            gdc[i]=(Gdc_Coord_3d)pointsInTriangulation.get(i);
            utm[i]=new Utm_Coord_3d();
        }
        
        Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());
        Gdc_To_Utm_Converter.Convert(gdc,utm);
        
        pointsInTriangulation.removeAllElements();
        hydroScalingAPI.util.probability.UniformDistribution rUn=new hydroScalingAPI.util.probability.UniformDistribution(-0.1f,0.1f);
        
        float[][] xyLinkValues=new float[3][numPoints];
        for(int i=0;i<numPoints;i++){
            xyLinkValues[0][i]=(float)utm[i].x;
            xyLinkValues[1][i]=(float)utm[i].y;
            xyLinkValues[2][i]=(float)((int[])typeOfPoint.get(i))[0];
            pointsInTriangulation.add(utm[i]);
        }

        xyLinkValues[2][numPoints-1]=1;
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        data_refLi = new DataReferenceImpl("data_ref_LINK");
        data_refLi.setData(vals_ff_Li);
        
        ConstantMap[] pointsCMap = {    //new ConstantMap( 1.0f, Display.Red),
                                        //new ConstantMap( 1.0f, Display.Green),
                                        //new ConstantMap( 0.0f, Display.Blue),
                                        new ConstantMap( 10.50f, Display.PointSize)};
            
        display_TIN.addReference( data_refLi,pointsCMap );
        
        data_refTr = new DataReferenceImpl("data_ref_TRIANG");
        ConstantMap[] linesCMap = {    //new ConstantMap( 1.0f, Display.Red),
                                        //new ConstantMap( 1.0f, Display.Green),
                                        //new ConstantMap( 0.0f, Display.Blue),
                                        new ConstantMap( 0.50f, Display.LineWidth)};

        display_TIN.addReference( data_refTr,linesCMap );
        
//        float[][] samples=new float[2][];
//        samples[0]=xyLinkValues[0];
//        samples[1]=xyLinkValues[1];
//        Gridded2DSet testLine=new Gridded2DSet(domainXLYL,samples,samples[0].length);
//        DataReferenceImpl data_refTr = new DataReferenceImpl("data_ref_TRIANG");
//        data_refTr.setData(testLine);
//        display_TIN.addReference(data_refTr);
        
        
//        float[][] samples=new float[2][];
//        samples[0]=xyLinkValues[0];
//        samples[1]=xyLinkValues[1];
//        System.out.println("the DelaunayWatson algorithm.");
//        long start = System.currentTimeMillis();
//        Delaunay delaun = (Delaunay) new DelaunayWatson(samples);
//        long end = System.currentTimeMillis();
//        float time = (end - start) / 1000f;
//        System.out.println("Triangulation took " + time + " seconds.");
        
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
        zrSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                zrSliderMouseReleased(evt);
            }
        });

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

    private void zrSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zrSliderMouseReleased
        try{
            plotPoints(zrSlider.getValue());
        } catch (VisADException v){
            System.out.print(v);
        }catch (RemoteException r){
            System.out.print(r);
        }
    }//GEN-LAST:event_zrSliderMouseReleased

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
            
            //new TRIBS_io(tempFrame, 85, 42,matDirs,magnitudes,metaModif).setVisible(true);
            new TRIBS_io(tempFrame, 310, 132,matDirs,magnitudes,metaModif).setVisible(true);
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
