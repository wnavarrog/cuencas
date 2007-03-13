/*
 * TRIBS_io.java
 *
 * Created on January 24, 2007, 1:27 PM
 */

package hydroScalingAPI.modules.tRIBS_io.widgets;


import java.io.IOException;
import java.util.Iterator;
import visad.*;
import visad.java2d.DisplayImplJ2D;
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
public class TRIBS_io extends javax.swing.JDialog {
    
    private RealType    xIndex=RealType.getRealType("xIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("posXY");
    
    private RealTupleType   espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor}),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    private FunctionType func_xEasting_yNorthing=new FunctionType(xIndex, espacioXLYL),
                         func_xEasting_yNorthing_to_Color=new FunctionType(domainXLYL,nodeColor);
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myBasinResults;
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
    
    private int[][] magnitudes;
    private float[][] DEM;
    
    private visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
    private visad.FlatField demField;
    
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    
    private DisplayImplJ3D display_TIN_I,display_TIN_O;
    private ScalarMap eastMap_I,northMap_I,pointsMap_I,
                      eastMap_O,northMap_O,pointsMap_O;
    
    private visad.java3d.DisplayRendererJ3D drI,drO;
    
    private byte[][] directionsKey={{1,2,3},{4,5,6},{7,8,9}};
    private java.util.Vector pointsInTriangulation,
                             typeOfPoint,
                             elevationOfPoint,
                             filteredPointsInTriangulation,
                             filteredTypeOfPoint,
                             filteredElevationOfPoint;
    private Delaunay delaun;
    private int numPointsBasin=0;
    private DataReferenceImpl data_refPoints,data_refTr,data_refPoly;
    private ConstantMap[] pointsCMap,
                          linesCMap,
                          linesCMap1;
    
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
        demField=metaData.getField();
        
        //Get Data concerning the module
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        //Graphical structure for aggregated response
        hydroScalingAPI.util.plot.XYJPanel Ppanel1 = 
                new hydroScalingAPI.util.plot.XYJPanel( "RTF File", "time [h]" , "Runoff [m^3/s]");
        hydroScalingAPI.util.plot.XYJPanel Ppanel2 = 
                new hydroScalingAPI.util.plot.XYJPanel( "qout Files", "time [h]" , "Discharge [m^3/s] / Stage [m]");
        
        rftPanel.add("Center",Ppanel1);
        qoutPanel.add("Center",Ppanel2);
        
        
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
        
        
        initializePoints();
        jPanel12.add("Center",display_TIN_I.getComponent());
        
        //Graphical structure for output triangulated points
        drO=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN_O = new DisplayImplJ3D("display_TIN_O",drO);
        
        dispGMC = (GraphicsModeControl) display_TIN_O.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_O = new ScalarMap( xEasting , Display.XAxis );
        eastMap_O.setScalarName("East Coordinate");
        northMap_O = new ScalarMap( yNorthing , Display.YAxis );
        northMap_O.setScalarName("North Coordinate");
        pointsMap_O=new ScalarMap( nodeColor , Display.RGB );

        display_TIN_O.addMap(eastMap_O);
        display_TIN_O.addMap(northMap_O);
        display_TIN_O.addMap(pointsMap_O);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_TIN_O);
        
        jPanel17.add("Center",display_TIN_O.getComponent());
        
        if(!pathTextField.getText().equals("") && !baseNameTextField.getText().equals("")){
            plotOutputPoints();
        }
        
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
    
    private float pointIncoming(int x,int y){
        
        int i=y;
        int j=x;
        
        int llegan=0;
        for (int k=0; k <= 8; k++){
            if (matDir[i+(k/3)-1][j+(k%3)-1]==9-k)
                llegan++;
        }
        
        return llegan;
    }
    
    private void writeTriangulation(Delaunay delaun,float[][] samples,java.io.File outputLocation) throws java.io.IOException{
        
        float[] s0 = samples[0];
        float[] s1 = samples[1];
        
        int[][] tri = delaun.Tri;
        int[][] edges = delaun.Edges;
        int numedges = delaun.NumEdges;
        int[][] walk=delaun.Walk;
        int[][] vert=delaun.Vertices;
        
        System.out.println("Edges File");
        java.util.Hashtable edges_order_map=new java.util.Hashtable();
        java.util.Vector edges_ef=new java.util.Vector();
        int runningID=0;
        for(int k=0;k<vert.length;k++){
            java.util.Vector localEdges=new java.util.Vector();
            for (int j=0; j<vert[k].length; j++) {
                int i=vert[k][j];
                float v1X=s0[tri[i][1]]-s0[tri[i][0]];
                float v1Y=s1[tri[i][1]]-s1[tri[i][0]];
                float v2X=s0[tri[i][2]]-s0[tri[i][0]];
                float v2Y=s1[tri[i][2]]-s1[tri[i][0]];

                float kComp=v1X*v2Y-v2X*v1Y;
                
                int pPos=0;
                for (int l=1; l<3; l++) if(k == tri[i][l]) pPos=l;
                //System.out.println("P"+k+" In Triang("+(kComp>0?"+":"-")+"):"+" P"+tri[i][0]+" P"+tri[i][1]+" P"+tri[i][2]+" pPos "+pPos);
                int L1pos=(pPos+1+(kComp>0?0:1))%3;
                int L2pos=(pPos+1+(kComp>0?1:0))%3;
                //System.out.println("    L1 is: E"+k+"-"+tri[i][L1pos]);
                //System.out.println("    L2 is: E"+k+"-"+tri[i][L2pos]);
                if(!localEdges.contains("E"+k+"-"+tri[i][L1pos])) localEdges.add("E"+k+"-"+tri[i][L1pos]);
                if(!localEdges.contains("E"+k+"-"+tri[i][L2pos])) localEdges.add("E"+k+"-"+tri[i][L2pos]);
                
            }
            localEdges.add(localEdges.firstElement());
            Object[] myEdges=localEdges.toArray();
            for (int j=1; j<myEdges.length; j++) {
                edges_ef.add(myEdges[j]+" "+k+" "+((String)myEdges[j]).substring(((String)myEdges[j]).indexOf("-")+1)+" "+myEdges[j-1]);
                if(edges_order_map.get((String)myEdges[j]) == null){
                    edges_order_map.put((String)myEdges[j],""+(2*runningID));
                    String inverseEdge=((String)myEdges[j]).substring(1);
                    String[] elementsIE=inverseEdge.split("-");
                    inverseEdge="E"+elementsIE[1]+"-"+elementsIE[0];
                    edges_order_map.put(inverseEdge,""+(2*runningID+1));
                    runningID++;
                } 
            }
            
        }
        int totEdges=edges_ef.size();
        //System.out.println(totEdges+" "+edges_order_map.size());
        java.util.Enumeration en=edges_order_map.keys();
//        while(en.hasMoreElements()) {
//            Object tttt=en.nextElement();
//            System.out.println(tttt+" "+edges_order_map.get(tttt));
//        }
        String[] edges_ef_ready=new String[totEdges];
        for(int i=0;i<totEdges;i++){
            String[] toPrint=((String)edges_ef.get(i)).split(" ");
            //System.out.println(i+" "+edges_ef.get(i));
            //System.out.println(edges_order_map.get(toPrint[0])+" "+toPrint[1]+" "+toPrint[2]+" "+edges_order_map.get(toPrint[3]));
            edges_ef_ready[Integer.parseInt((String)edges_order_map.get(toPrint[0]))]=toPrint[1]+" "+toPrint[2]+" "+edges_order_map.get(toPrint[3]);
        }
        java.io.File edgesFileLocation=new java.io.File(outputLocation.getPath()+".edges");
        java.io.BufferedWriter writerEdges = new java.io.BufferedWriter(new java.io.FileWriter(edgesFileLocation));
        writerEdges.write("0.0000\n");
        writerEdges.write(totEdges+"\n");
        for(int i=0;i<totEdges;i++){
            System.out.println(edges_ef_ready[i]);
            writerEdges.write(edges_ef_ready[i]+"\n");
        }
        writerEdges.close();
        
        System.out.println("Nodes File");
        java.util.Vector nodes_nf=new java.util.Vector();
        java.util.Vector edges_nf=new java.util.Vector();
        java.util.Vector codes_nf=new java.util.Vector();
        
        
        for (int i=0; i<edges.length; i++) {
            for (int j=0; j<3; j++) {
                if(!nodes_nf.contains("P"+tri[i][j])){
                    nodes_nf.add("P"+tri[i][j]);
                    edges_nf.add("E"+tri[i][j]+"-"+tri[i][(j+1)%3]);
                }
            }
        }
        
        java.io.File nodesFileLocation=new java.io.File(outputLocation.getPath()+".nodes");
        java.io.BufferedWriter writerNodes = new java.io.BufferedWriter(new java.io.FileWriter(nodesFileLocation));
        writerNodes.write("0.0000\n");
        writerNodes.write(vert.length+"\n");
        
        java.io.File zFileLocation=new java.io.File(outputLocation.getPath()+".z");
        java.io.BufferedWriter writerZ = new java.io.BufferedWriter(new java.io.FileWriter(zFileLocation));
        writerZ.write("0.0000\n");
        writerZ.write(vert.length+"\n");
        
        
        for(int i=0;i<vert.length;i++){
            int pointID=Integer.parseInt(((String)nodes_nf.get(i)).substring(1));
            System.out.println(samples[0][pointID]+" "+samples[1][pointID]+" "+edges_order_map.get(edges_nf.get(i))+" "+"BC-XX");
            writerNodes.write(samples[0][pointID]+" "+samples[1][pointID]+" "+edges_order_map.get(edges_nf.get(i))+" "+(int)samples[2][pointID]+"\n");

            float pointZ=((Double)filteredElevationOfPoint.get(pointID)).floatValue();
            int pointT=(int)samples[2][pointID];
            if (pointT == 1){
                pointZ+=4;
            }
            if (pointT == 3){
                pointZ-=.1;
            }
            if (pointT == 2){
                pointZ-=2;
            }
            System.out.println(pointZ);
            writerZ.write(pointZ+"\n");
            
        }
        writerNodes.close();
        writerZ.close();
        
        System.out.println("Triangles File");
        java.io.File triFileLocation=new java.io.File(outputLocation.getPath()+".tri");
        java.io.BufferedWriter writerTri = new java.io.BufferedWriter(new java.io.FileWriter(triFileLocation));
        writerTri.write("0.0000\n");
        writerTri.write(edges.length+"\n");
        for (int i=0; i<edges.length; i++) {
            float v1X=s0[tri[i][1]]-s0[tri[i][0]];
            float v1Y=s1[tri[i][1]]-s1[tri[i][0]];
            float v2X=s0[tri[i][2]]-s0[tri[i][0]];
            float v2Y=s1[tri[i][2]]-s1[tri[i][0]];

            float kComp=v1X*v2Y-v2X*v1Y;
            //System.out.print("T("+i+(kComp>0?"+":"-")+")");
            if(kComp>0){
                System.out.print(tri[i][2]+" "+tri[i][1]+" "+tri[i][0]);
                System.out.print(" "+walk[i][0]+" "+walk[i][2]+" "+walk[i][1]);
                System.out.print(" "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][1]));
                writerTri.write(tri[i][2]+" "+tri[i][1]+" "+tri[i][0]);
                writerTri.write(" "+walk[i][0]+" "+walk[i][2]+" "+walk[i][1]);
                writerTri.write(" "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][1]));
                
            } else {
                System.out.print(tri[i][0]+" "+tri[i][1]+" "+tri[i][2]);
                System.out.print(" "+walk[i][1]+" "+walk[i][2]+" "+walk[i][0]);
                System.out.print(" "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][1]));
                writerTri.write(tri[i][0]+" "+tri[i][1]+" "+tri[i][2]);
                writerTri.write(" "+walk[i][1]+" "+walk[i][2]+" "+walk[i][0]);
                writerTri.write(" "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][1]));
            }
            System.out.println();
            writerTri.write("\n");
        }
        writerTri.close();
        
    }
    
    private void writePoints(float[][] samples,java.io.File outputLocation) throws java.io.IOException{
        
        System.out.println("Points File");

        java.io.File nodesFileLocation=new java.io.File(outputLocation.getPath()+".points");
        java.io.BufferedWriter writerNodes = new java.io.BufferedWriter(new java.io.FileWriter(nodesFileLocation));
        writerNodes.write(samples[0].length+"\n");
        
        
        for (int i=0; i<samples[0].length; i++){
            writerNodes.write(samples[0][i]+" ");
            writerNodes.write(samples[1][i]+" ");
            writerNodes.write(samples[2][i]+" ");
            writerNodes.write((int)samples[3][i]+"\n");
        }
        
        writerNodes.close();
        
    }
    
    private double[] intersection(double x1, double y1, double x2, double y2,double x3, double y3, double x4, double y4){
        double b1=x1*y2-y1*x2;
        double b2=x3*y4-y3*x4;
        
        double U1=(x3-x4)*b1-(x1-x2)*b2;
        double U2=(y3-y4)*b1-(y1-y2)*b2;
        double BG=(x1-x2)*(y3-y4)-(x3-x4)*(y1-y2);
        
        double xi=U1/BG;
        double yi=U2/BG;
        
        double[] answ={xi,yi};
        
        return answ;
    }
    
    private void sortTriangles(Delaunay delaun,float[][] samples){
        for(int j=0;j<delaun.Vertices.length;j++) {
            float[][] lines1=new float[3][delaun.Vertices[j].length];
            for(int i=0;i<delaun.Vertices[j].length;i++){
                for(int k=0;k<3;k++){
                    lines1[0][i]+=samples[0][delaun.Tri[delaun.Vertices[j][i]][k]];
                    lines1[1][i]+=samples[1][delaun.Tri[delaun.Vertices[j][i]][k]];
                }
                lines1[0][i]/=3.0;
                lines1[1][i]/=3.0;
                double delX=lines1[0][i]-samples[0][j];
                double delY=lines1[1][i]-samples[1][j];
                lines1[2][i]=(float)(((delX>0&delY<0)?360:0)+(delX>0?0:180)+(delX==0?90:Math.atan(delY/delX)/2.0/Math.PI*360));
                
            }
            float[] sortedAngles=lines1[2].clone();
            java.util.Arrays.sort(sortedAngles);
            int[] sortedTriList=new int[delaun.Vertices[j].length];
            for(int i=0;i<delaun.Vertices[j].length;i++) sortedTriList[java.util.Arrays.binarySearch(sortedAngles,lines1[2][i])]=delaun.Vertices[j][i];
            delaun.Vertices[j]=sortedTriList;
        }
    }
    
    private java.io.File findTriEdgNodes(){
        
        
        return null;
    }
    
    private void plotOutputPoints() throws RemoteException, VisADException, java.io.IOException{
        java.io.File pathToTriang=findTriEdgNodes();
        pathToTriang=new java.io.File("/Users/ricardo/temp/TEST_VOI_OUT/SMALLBASIN/Output/voronoi");
        String baseName="smallbasin";
        
        int countNoBorder=0;
        
        //Read nodes file
        java.io.File nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+".nodes");
        java.io.BufferedReader bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        String fullLine;
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        int localNumPoints=Integer.parseInt(fullLine);
        float[][] xyLinkValues=new float[3][localNumPoints];
        for (int i = 0; i < localNumPoints; i++) {
            String[] lineData=bufferNodes.readLine().split(" ");
            xyLinkValues[0][i]=Float.parseFloat(lineData[0]);
            xyLinkValues[1][i]=Float.parseFloat(lineData[1]);
            xyLinkValues[2][i]=Float.parseFloat(lineData[3]);
            if(xyLinkValues[2][i] != 1 && xyLinkValues[2][i] != 2) countNoBorder++;
            
        }
        bufferNodes.close();
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        
        hydroScalingAPI.util.statistics.Stats eastStats=new hydroScalingAPI.util.statistics.Stats(xyLinkValues[1]);
        hydroScalingAPI.util.statistics.Stats nortStats=new hydroScalingAPI.util.statistics.Stats(xyLinkValues[0]);
        
        ProjectionControl pc = display_TIN_O.getProjectionControl();
        
        pc.setAspectCartesian(new double[] {1, (eastStats.maxValue-eastStats.minValue)/(nortStats.maxValue-nortStats.minValue)});        
        
        
        //Read triangles file
        java.io.File trianFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+".tri");
        java.io.BufferedReader bufferTrian = new java.io.BufferedReader(new java.io.FileReader(trianFile));
        fullLine=bufferTrian.readLine();
        fullLine=bufferTrian.readLine();
        int localNumTrian=Integer.parseInt(fullLine);
        int[][] triangulation=new int[localNumTrian][3];
        for (int i = 0; i < localNumTrian; i++) {
            String[] lineData=bufferTrian.readLine().split(" ");
            triangulation[i][0]=Integer.parseInt(lineData[0]);
            triangulation[i][1]=Integer.parseInt(lineData[1]);
            triangulation[i][2]=Integer.parseInt(lineData[2]);
            
        }
        bufferTrian.close();
        
        float[][] samples=new float[2][];
        samples[0]=xyLinkValues[0];//.clone();
        samples[1]=xyLinkValues[1];//.clone();
        System.out.println("the DelaunayClarkson algorithm.");
        long start = System.currentTimeMillis();
        delaun = new DelaunayCustom(samples,triangulation);
        long end = System.currentTimeMillis();
        float time = (end - start) / 1000f;
        System.out.println("Triangulation took " + time + " seconds.");
        
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

        //Read voronoi file
        java.io.File voroFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+"_voi");
        java.io.BufferedReader bufferVoro = new java.io.BufferedReader(new java.io.FileReader(voroFile));
        
        java.util.Vector[] voroPolys=new java.util.Vector[countNoBorder];
        
        for (int i = 0; i < countNoBorder; i++) {
            voroPolys[i]=new java.util.Vector();
            fullLine=bufferVoro.readLine();
            while(!fullLine.equalsIgnoreCase("END")){
                fullLine=bufferVoro.readLine();
                voroPolys[i].add(fullLine);
            }
            
        }
        bufferVoro.close();
        
        Gridded2DSet[] polygons=new Gridded2DSet[countNoBorder];
        Irregular2DSet[] regions=new Irregular2DSet[countNoBorder];

        lines=new float[2][];

        for(int j=0;j<countNoBorder;j++){
            lines = new float[2][voroPolys[j].size()-1];
            
            for(int i=0;i<voroPolys[j].size()-1;i++){
                String[] lineData=((String)voroPolys[j].get(i)).split(",");
                lines[0][i]=Float.parseFloat(lineData[0]);
                lines[1][i]=Float.parseFloat(lineData[1]);
            }

            polygons[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);

//            Delaunay dela1=new DelaunayClarkson(lines);
//            
            regions[j] = DelaunayCustom.fill(polygons[j]);

            //float theColor=3.0f*(float)Math.random();
            //for (int i = j*4; i < (j+1)*4; i++) colors[0][i]=theColor;
        }

        UnionSet allPoly=new UnionSet(domainXLYL,polygons);
        
        UnionSet allRegions=new UnionSet(domainXLYL,regions);

        float[][] colors=new float[1][allRegions.getLength()];
        for (int i = 0; i < colors[0].length; i++) {
            colors[0][i]=2.6f*(float)Math.random();
        }
        
        FlatField theColors=new FlatField(func_xEasting_yNorthing_to_Color,allRegions);
        theColors.setSamples(colors);

        DataReference region_ref = new DataReferenceImpl("region");
        region_ref.setData(theColors);
        //display_TIN_O.addReference( region_ref );
        
        DataReferenceImpl local_data_refPoints = new DataReferenceImpl("data_ref_Points");
        local_data_refPoints.setData(vals_ff_Li);
        
        ConstantMap[] local_pointsCMap = new ConstantMap[] {new ConstantMap( 10.50f, Display.PointSize)};
        display_TIN_O.addReference( local_data_refPoints, local_pointsCMap );
        
        DataReferenceImpl local_data_refTr = new DataReferenceImpl("data_ref_Trian");
        local_data_refTr.setData(allTriang);
        display_TIN_O.addReference( local_data_refTr );
        
        DataReferenceImpl local_data_refPoly = new DataReferenceImpl("data_ref_poly");
        local_data_refPoly.setData(allPoly);
        ConstantMap[] local_linesCMap1 = new ConstantMap[] {    new ConstantMap( 1.0f, Display.Red),
                                            new ConstantMap( 0.0f, Display.Green),
                                            new ConstantMap( 1.0f, Display.Blue),
                                            new ConstantMap( 1.50f, Display.LineWidth)};

        display_TIN_O.addReference( local_data_refPoly,local_linesCMap1 );
            
    }
    
    private void plotPoints(float Zr) throws RemoteException, VisADException{
        
        int[][] xyBasin=myCuenca.getXYBasin();
        byte[][] basMask=myCuenca.getBasinMask();
        
        filteredPointsInTriangulation=(java.util.Vector)pointsInTriangulation.clone();
        filteredTypeOfPoint=(java.util.Vector)typeOfPoint.clone();
        filteredElevationOfPoint=(java.util.Vector)elevationOfPoint.clone();
        
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
                    //if(pointLaplacian(xyBasin[0][i],xyBasin[1][i])>-10.1) {
                    //if(pointLaplacian(xyBasin[0][i],xyBasin[1][i])==0) {
                    if(pointIncoming(xyBasin[0][i],xyBasin[1][i])>0) {
                    //if(Math.random()*100.0 < Zr){
                        int pToRemove=(int)(pB-numPRemoved);
                        filteredPointsInTriangulation.remove(pToRemove);
                        filteredTypeOfPoint.remove(pToRemove);
                        filteredElevationOfPoint.remove(pToRemove);
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
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        data_refPoints.setData(vals_ff_Li);
        
        if(Zr > 70){
            float[][] samples=new float[2][];
            samples[0]=xyLinkValues[0];//.clone();
            samples[1]=xyLinkValues[1];//.clone();
            System.out.println("the DelaunayClarkson algorithm.");
            long start = System.currentTimeMillis();
            Delaunay.perturb(samples,0.01f,false);
            delaun = (Delaunay) new DelaunayClarkson(samples);
            long end = System.currentTimeMillis();
            float time = (end - start) / 1000f;
            System.out.println("Triangulation took " + time + " seconds.");
            //samples[0]=xyLinkValues[0].clone();
            //samples[1]=xyLinkValues[1].clone();
            
            vals_ff_Li.setSamples( xyLinkValues );
        
            data_refPoints.setData(vals_ff_Li);
            
            
            
            
//            System.out.println(delaun.NumEdges);
//            
//            for(int j=0;j<delaun.Edges.length;j++){
//                for(int i=0;i<delaun.Edges[j].length;i++){
//                    System.out.print(delaun.Edges[j][i]+"\t\t");
//                }
//                System.out.println();
//            }
            sortTriangles(delaun,samples);
            
            
            Gridded2DSet[] triangles=new Gridded2DSet[delaun.Tri.length];
            Irregular2DSet[] regions=new Irregular2DSet[delaun.Tri.length];
            
            float[][] colors=new float[1][4*delaun.Tri.length];
            float[][] lines=new float[2][4];
            
            for(int j=0;j<delaun.Tri.length;j++){
                for(int i=0;i<3;i++){
                    lines[0][i]=samples[0][delaun.Tri[j][i]];
                    lines[1][i]=samples[1][delaun.Tri[j][i]];
                }
                lines[0][3]=samples[0][delaun.Tri[j][0]];
                lines[1][3]=samples[1][delaun.Tri[j][0]];
                
                triangles[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);
                
                regions[j] = new Irregular2DSet(domainXLYL,lines);
                
                float theColor=3.0f*(float)Math.random();
                for (int i = j*4; i < (j+1)*4; i++) colors[0][i]=theColor;
            }
            
            UnionSet allTriang=new UnionSet(domainXLYL,triangles);
            
            data_refTr.setData(allTriang);

            UnionSet allRegions=new UnionSet(domainXLYL,regions);

            FlatField theColors=new FlatField(func_xEasting_yNorthing_to_Color,allRegions);
            theColors.setSamples(colors);

            DataReference region_ref = new DataReferenceImpl("region");
            region_ref.setData(theColors);

            //display_TIN_I.addReference(region_ref);
            
            int numValidPoints=0;
            for(int j=0;j<xyLinkValues[2].length;j++) {
                if(xyLinkValues[2][j] != 1 ) numValidPoints++;
            }
            
            float[] s0 = samples[0];
            float[] s1 = samples[1];
            
            Gridded2DSet[] polygons=new Gridded2DSet[numValidPoints-1];
            int kp=0;
            for(int k=0;k<xyLinkValues[2].length;k++) {
            //for(int k=0;k<5;k++) {    
                if(xyLinkValues[2][k] != 1 && xyLinkValues[2][k] != 2){
                    float[][] lines1=new float[2][delaun.Vertices[k].length+1];
                    for(int j=0;j<delaun.Vertices[k].length;j++){
                        
                        int i=delaun.Vertices[k][j];
                        float v1X=s0[delaun.Tri[i][1]]-s0[delaun.Tri[i][0]];
                        float v1Y=s1[delaun.Tri[i][1]]-s1[delaun.Tri[i][0]];
                        float v2X=s0[delaun.Tri[i][2]]-s0[delaun.Tri[i][0]];
                        float v2Y=s1[delaun.Tri[i][2]]-s1[delaun.Tri[i][0]];

                        float kComp=v1X*v2Y-v2X*v1Y;

                        int pPos=0;
                        for (int l=1; l<3; l++) if(k == delaun.Tri[i][l]) pPos=l;
                        //System.out.println("P"+k+" In Triang("+(kComp>0?"+":"-")+"):"+" P"+tri[i][0]+" P"+tri[i][1]+" P"+tri[i][2]+" pPos "+pPos);
                        int L1pos=(pPos+1+(kComp>0?0:1))%3;
                        int L2pos=(pPos+1+(kComp>0?1:0))%3;
                        
                        double xs1=s0[delaun.Tri[i][pPos]];
                        double ys1=s1[delaun.Tri[i][pPos]];
                        
                        double xs2=s0[delaun.Tri[i][L2pos]];
                        double ys2=s1[delaun.Tri[i][L2pos]];
                        
                        double xa1=(xs1+xs2)/2.0f;
                        double ya1=(ys1+ys2)/2.0f;
                        double xb1=0;
                        double yb1;
                        if(ys2-ys1 != 0)
                            yb1=ya1+(xs2-xs1)/(ys2-ys1)*xa1;
                        else{
                            xb1=xa1;
                            yb1=0;
                        }
                        
                        double xs3=s0[delaun.Tri[i][L1pos]];
                        double ys3=s1[delaun.Tri[i][L1pos]];
                        
                        double xa2=(xs1+xs3)/2.0f;
                        double ya2=(ys1+ys3)/2.0f;
                        double xb2=0;
                        double yb2;
                        if(ys3-ys1 != 0)
                            yb2=ya2+(xs3-xs1)/(ys3-ys1)*xa2;
                        else{
                            xb2=xa1;
                            yb2=0;
                        }
                        
                        double[] result=intersection(xa1,ya1,xb1,yb1,xa2,ya2,xb2,yb2);

                        double xvoi=result[0];
                        double yvoi=result[1];
                        
                        lines1[0][j]=(float)xvoi;
                        lines1[1][j]=(float)yvoi;

                    }
                    lines1[0][delaun.Vertices[k].length]=lines1[0][0];
                    lines1[1][delaun.Vertices[k].length]=lines1[1][0];
                    
                    polygons[kp++]=new Gridded2DSet(domainXLYL,lines1,lines1[0].length);
                }
            }
            UnionSet allPoly=new UnionSet(domainXLYL,polygons);
            
            //Gridded2DSet allPoly=polygons[1];
            data_refPoly.setData(allPoly);
            
            
        }
        
        System.out.println(filteredPointsInTriangulation.size()/(float)pointsInTriangulation.size());
        System.out.println((1-(numPointsBasin-numPRemoved)/(float)numPointsBasin));

        
    }
    
    private void initializePoints() throws RemoteException, VisADException, java.io.IOException {
        
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        int[][] xyDivide=myCuenca.getXYBasinDivide();
        
        pointsInTriangulation=new java.util.Vector();
        typeOfPoint=new java.util.Vector();
        elevationOfPoint=new java.util.Vector();
        
        double latP,lonP,locElev,nLatP,nLonP;
        visad.RealTuple spotValue;
        
        //adding points inside the basin
        for (int i = 0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] <= 0){
                latP=lonLatsBasin[1][i]+metaDatos.getResLat()/3600.0/2.0;
                lonP=lonLatsBasin[0][i]+metaDatos.getResLon()/3600.0/2.0;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                locElev=spotValue.getValues()[0];

                pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
                typeOfPoint.add(new int[] {0});
                elevationOfPoint.add(locElev);
                numPointsBasin++;
            }
        }
        
        float bfs=0.8f;
        
        //adding points along the river network
        for (int i = 0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] > 0){
                int yP=xyBasin[1][i];
                int xP=xyBasin[0][i];
                
                latP=lonLatsBasin[1][i]+metaDatos.getResLat()/3600.0/2.0;
                lonP=lonLatsBasin[0][i]+metaDatos.getResLon()/3600.0/2.0;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                locElev=spotValue.getValues()[0];

                pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
                typeOfPoint.add(new int[] {3});
                elevationOfPoint.add(locElev);
                
                int delta_yP=((matDir[(int)yP][xP]-1)/3)-1;
                int delta_xP=((matDir[yP][xP]-1)%3)-1;
                
                nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0;
                nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {nLonP,nLatP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                double currentElev=locElev;
                double forwardElev=spotValue.getValues()[0];
                
                if (i>0){
                    
                    //A new point a quarter of the way forward

                    nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*1.0/4.0;
                    nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*1.0/4.0;

                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(3*currentElev+forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);

                    //A new point half way forward
                    
                    nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*2.0/4.0;
                    nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*2.0/4.0;
                    
                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(2*currentElev+2*forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);

                        //New points creating a buffer for the river

                        nLatP=latP+(2*delta_yP-bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
                        nLonP=lonP+(2*delta_xP+bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);

                        nLatP=latP+(2*delta_yP+bfs*delta_xP)*metaDatos.getResLat()/3600.0/4.0;
                        nLonP=lonP+(2*delta_xP-bfs*delta_yP)*metaDatos.getResLon()/3600.0/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);

                    //A new point three quarters of the way forward

                    nLatP=latP+delta_yP*metaDatos.getResLat()/3600.0*3.0/4.0;
                    nLonP=lonP+delta_xP*metaDatos.getResLon()/3600.0*3.0/4.0;

                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(currentElev+3*forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);
                    
                    //Two new points acting as buffer for the initial network point
                    //An if statement is needed to avoid hillslope points where there is river network
                    
                    if(magnitudes[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] <= 0 || matDir[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] != 10-directionsKey[1-delta_xP][1+delta_yP]){
                        nLatP=latP-delta_xP*metaDatos.getResLat()/3600.0*bfs/4.0;
                        nLonP=lonP+delta_yP*metaDatos.getResLon()/3600.0*bfs/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);
                    }
                    
                    if(magnitudes[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] <= 0 || matDir[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] != 10-directionsKey[1+delta_xP][1-delta_yP]){
                        nLatP=latP+delta_xP*metaDatos.getResLat()/3600.0*bfs/4.0;
                        nLonP=lonP-delta_yP*metaDatos.getResLon()/3600.0*bfs/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);
                    }
                    
                }
            }
        }
        //Adding points in the basin divide
        for(int i=0;i<lonLatsDivide[0].length-1;i++){
            latP=(lonLatsDivide[1][i]+lonLatsDivide[1][i+1])/2.0;
            lonP=(lonLatsDivide[0][i]+lonLatsDivide[0][i+1])/2.0;
            
            spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
            locElev=spotValue.getValues()[0];

            pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
            typeOfPoint.add(new int[] {1});
            elevationOfPoint.add(locElev);
            
        }
        int lastElemIndex=typeOfPoint.size()-2;
        typeOfPoint.setElementAt(new int[] {2},lastElemIndex);
        System.out.println(elevationOfPoint.get(lastElemIndex));
        
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
        
        float[][] xyLinkValues=new float[3][numPoints];
        for(int i=0;i<numPoints;i++){
            xyLinkValues[0][i]=(float)utm[i].x;
            xyLinkValues[1][i]=(float)utm[i].y;
            xyLinkValues[2][i]=(float)((int[])typeOfPoint.get(i))[0];
            pointsInTriangulation.add(utm[i]);
        }
        
        hydroScalingAPI.util.statistics.Stats eastStats=new hydroScalingAPI.util.statistics.Stats(xyLinkValues[1]);
        hydroScalingAPI.util.statistics.Stats nortStats=new hydroScalingAPI.util.statistics.Stats(xyLinkValues[0]);
        
        ProjectionControl pc = display_TIN_I.getProjectionControl();
        
        pc.setAspectCartesian(new double[] {1, (eastStats.maxValue-eastStats.minValue)/(nortStats.maxValue-nortStats.minValue)});        
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(xIndex,linkAccumAVal);
        
        FlatField vals_ff_Li = new FlatField( func_xEasting_yNorthing, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        data_refPoints = new DataReferenceImpl("data_ref_Points");
        data_refPoints.setData(vals_ff_Li);
        
        pointsCMap = new ConstantMap[] {new ConstantMap( 10.50f, Display.PointSize)};

        display_TIN_I.addReference( data_refPoints,pointsCMap );
        
        data_refTr = new DataReferenceImpl("data_ref_TRIANG");
        linesCMap = new ConstantMap[] {new ConstantMap( 0.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refTr,linesCMap );
        
        data_refPoly = new DataReferenceImpl("data_ref_poly");
        linesCMap1 = new ConstantMap[] {    new ConstantMap( 1.0f, Display.Red),
                                            new ConstantMap( 0.0f, Display.Green),
                                            new ConstantMap( 1.0f, Display.Blue),
                                            new ConstantMap( 1.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refPoly,linesCMap1 );
        
        filteredPointsInTriangulation=(java.util.Vector)pointsInTriangulation.clone();
        filteredTypeOfPoint=(java.util.Vector)typeOfPoint.clone();
        filteredElevationOfPoint=(java.util.Vector)elevationOfPoint.clone();
            
        
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
        panel_IO = new javax.swing.JTabbedPane();
        jPanel10 = new javax.swing.JPanel();
        panelOpciones = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        zrSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        exportTriButton = new javax.swing.JButton();
        exportPoiButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        pointsCheckBox = new javax.swing.JCheckBox();
        trianglesCheckBox = new javax.swing.JCheckBox();
        voronoiCheckBox = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel16 = new javax.swing.JPanel();
        rftPanel = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        qoutPanel = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jPanel22 = new javax.swing.JPanel();
        pointsCheckBox1 = new javax.swing.JCheckBox();
        trianglesCheckBox1 = new javax.swing.JCheckBox();
        voronoiCheckBox1 = new javax.swing.JCheckBox();
        jPanel15 = new javax.swing.JPanel();
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
        jPanel10.setLayout(new java.awt.BorderLayout());

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

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridLayout(1, 3));

        pointsCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox.setSelected(true);
        pointsCheckBox.setText("Show Points");
        pointsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBoxActionPerformed(evt);
            }
        });

        jPanel3.add(pointsCheckBox);

        trianglesCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox.setSelected(true);
        trianglesCheckBox.setText("Show Triangles");
        trianglesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        trianglesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBoxActionPerformed(evt);
            }
        });

        jPanel3.add(trianglesCheckBox);

        voronoiCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        voronoiCheckBox.setSelected(true);
        voronoiCheckBox.setText("Show Voronoi Polygons");
        voronoiCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        voronoiCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        voronoiCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voronoiCheckBoxActionPerformed(evt);
            }
        });

        jPanel3.add(voronoiCheckBox);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel12.setLayout(new java.awt.BorderLayout());

        jPanel1.add(jPanel12, java.awt.BorderLayout.CENTER);

        panelOpciones.addTab("TIN", jPanel1);

        panelOpciones.addTab("3D TIN", jPanel4);

        panelOpciones.addTab("Land Cover", jPanel6);

        panelOpciones.addTab("Soil Type", jPanel7);

        panelOpciones.addTab("Rainfall", jPanel8);

        panelOpciones.addTab("Ground Water", jPanel13);

        panelOpciones.addTab("Weather", jPanel14);

        panelOpciones.addTab("Input File", jPanel9);

        jPanel10.add(panelOpciones, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Input Options", jPanel10);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel16.setLayout(new java.awt.GridLayout(2, 0));

        rftPanel.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new java.awt.GridLayout(6, 0));

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel4.setText("Runoff Component Time Series (*.rft)");
        jPanel21.add(jLabel4);

        jCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Infiltration-excess Runoff");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel21.add(jCheckBox1);

        jCheckBox2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Saturation-excess Runoff");
        jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel21.add(jCheckBox2);

        jCheckBox3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox3.setSelected(true);
        jCheckBox3.setText("Perched Return Flow");
        jCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel21.add(jCheckBox3);

        jCheckBox4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox4.setSelected(true);
        jCheckBox4.setText("Groundwater Exfiltration");
        jCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel21.add(jCheckBox4);

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

        jComboBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        jPanel23.add(jComboBox1);

        jCheckBox5.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Discharge");
        jCheckBox5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel23.add(jCheckBox5);

        jCheckBox6.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jCheckBox6.setSelected(true);
        jCheckBox6.setText("Channel stage");
        jCheckBox6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel23.add(jCheckBox6);

        qoutPanel.add(jPanel23, java.awt.BorderLayout.WEST);

        jPanel16.add(qoutPanel);

        jTabbedPane2.addTab("Temporal Response", jPanel16);

        jPanel17.setLayout(new java.awt.BorderLayout());

        jPanel20.setLayout(new java.awt.GridLayout(1, 2));

        jComboBox2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Times", "Time 01", "Time 02", "Time 03", "Time 04", "Time-integrated Spatial Output" }));
        jPanel20.add(jComboBox2);

        jComboBox3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Variables", "Variable 01", "Variable 02", "Variable 03", "Variable 04" }));
        jPanel20.add(jComboBox3);

        jPanel17.add(jPanel20, java.awt.BorderLayout.NORTH);

        jPanel22.setLayout(new java.awt.GridLayout(1, 3));

        pointsCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox1.setSelected(true);
        pointsCheckBox1.setText("Show Points");
        pointsCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pointsCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBox1ActionPerformed(evt);
            }
        });

        jPanel22.add(pointsCheckBox1);

        trianglesCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox1.setSelected(true);
        trianglesCheckBox1.setText("Show Triangles");
        trianglesCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        trianglesCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBox1ActionPerformed(evt);
            }
        });

        jPanel22.add(trianglesCheckBox1);

        voronoiCheckBox1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        voronoiCheckBox1.setSelected(true);
        voronoiCheckBox1.setText("Show Voronoi Polygons");
        voronoiCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        voronoiCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        voronoiCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voronoiCheckBox1ActionPerformed(evt);
            }
        });

        jPanel22.add(voronoiCheckBox1);

        jPanel17.add(jPanel22, java.awt.BorderLayout.SOUTH);

        jTabbedPane2.addTab("Spatial Response", jPanel17);

        jPanel11.add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Output Analysis", jPanel11);

        getContentPane().add(panel_IO, java.awt.BorderLayout.CENTER);

        jPanel15.setLayout(new java.awt.GridLayout(2, 1, 0, 3));

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

    private void voronoiCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voronoiCheckBox1ActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_voronoiCheckBox1ActionPerformed

    private void trianglesCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBox1ActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_trianglesCheckBox1ActionPerformed

    private void pointsCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBox1ActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_pointsCheckBox1ActionPerformed

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
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("nodes","Nodes File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);

            if (fc.getSelectedFile() == null) return;
            int numPoints=filteredPointsInTriangulation.size();
            float[][] xyLinkValues=new float[4][numPoints];
            for(int i=0;i<numPoints;i++){
                Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
                xyLinkValues[0][i]=(float)utmLocal.x;
                xyLinkValues[1][i]=(float)utmLocal.y;
                xyLinkValues[2][i]=((Double)filteredElevationOfPoint.get(i)).floatValue();
                xyLinkValues[3][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
            }
        
            writePoints(xyLinkValues,fc.getSelectedFile());
        } catch (java.io.IOException IOE){
            System.err.println("Failed writing triangulation files for this basin.");
            IOE.printStackTrace();
        }
    }//GEN-LAST:event_exportPoiButtonActionPerformed

    private void voronoiCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voronoiCheckBoxActionPerformed
        if(voronoiCheckBox.isSelected()){
            try {
                display_TIN_I.addReference(data_refPoly,linesCMap1);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refPoly);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_voronoiCheckBoxActionPerformed

    private void trianglesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBoxActionPerformed
        if(trianglesCheckBox.isSelected()){
            try {
                display_TIN_I.addReference(data_refTr,linesCMap);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refTr);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_trianglesCheckBoxActionPerformed

    private void pointsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBoxActionPerformed
        if(pointsCheckBox.isSelected()){
            try {
                display_TIN_I.addReference(data_refPoints,pointsCMap);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refPoints);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_pointsCheckBoxActionPerformed

    private void zrSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_zrSliderMouseReleased
        try{
            plotPoints(zrSlider.getValue());
        } catch (VisADException v){
            System.out.print(v);
        }catch (RemoteException r){
            System.out.print(r);
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
            int numPoints=filteredPointsInTriangulation.size();
            float[][] xyLinkValues=new float[3][numPoints];
            for(int i=0;i<numPoints;i++){
                Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
                xyLinkValues[0][i]=(float)utmLocal.x;
                xyLinkValues[1][i]=(float)utmLocal.y;
                xyLinkValues[2][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
            }
        
            writeTriangulation(delaun,xyLinkValues,fc.getSelectedFile());
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
    private javax.swing.JTextField baseNameTextField;
    private javax.swing.JButton changePath;
    private javax.swing.JButton exportPoiButton;
    private javax.swing.JButton exportTriButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane panelOpciones;
    private javax.swing.JTabbedPane panel_IO;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JCheckBox pointsCheckBox;
    private javax.swing.JCheckBox pointsCheckBox1;
    private javax.swing.JPanel qoutPanel;
    private javax.swing.JPanel rftPanel;
    private javax.swing.JCheckBox trianglesCheckBox;
    private javax.swing.JCheckBox trianglesCheckBox1;
    private javax.swing.JCheckBox voronoiCheckBox;
    private javax.swing.JCheckBox voronoiCheckBox1;
    private javax.swing.JSlider zrSlider;
    // End of variables declaration//GEN-END:variables
    
}

