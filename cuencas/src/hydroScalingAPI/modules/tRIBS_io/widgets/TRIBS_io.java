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
    
    private FunctionType func_xEasting_yNorthing=new FunctionType(xIndex, espacioXLYL);
    
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
    
    private DisplayImplJ3D display_TIN;
    private ScalarMap eastMap,northMap,pointsMap;
    
    private visad.java3d.DisplayRendererJ3D dr;
    
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
    private ConstantMap[] pointsCMap,linesCMap,linesCMap1;
    
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
        jPanel12.add("Center",display_TIN.getComponent());
        
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
            Delaunay.perturb(samples,0.1f,false);
            delaun = (Delaunay) new DelaunayClarkson(samples);
            delaun.improve(samples,1);
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
        
        hydroScalingAPI.tools.Stats eastStats=new hydroScalingAPI.tools.Stats(xyLinkValues[1]);
        hydroScalingAPI.tools.Stats nortStats=new hydroScalingAPI.tools.Stats(xyLinkValues[0]);
        
        ProjectionControl pc = display_TIN.getProjectionControl();
        
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

        display_TIN.addReference( data_refPoints,pointsCMap );
        
        data_refTr = new DataReferenceImpl("data_ref_TRIANG");
        linesCMap = new ConstantMap[] {new ConstantMap( 0.50f, Display.LineWidth)};

        display_TIN.addReference( data_refTr,linesCMap );
        
        data_refPoly = new DataReferenceImpl("data_ref_poly");
        linesCMap1 = new ConstantMap[] {    new ConstantMap( 1.0f, Display.Red),
                                            new ConstantMap( 0.0f, Display.Green),
                                            new ConstantMap( 1.0f, Display.Blue),
                                            new ConstantMap( 1.50f, Display.LineWidth)};

        display_TIN.addReference( data_refPoly,linesCMap1 );
        
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
        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        jPanel9 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
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

        panelOpciones.addTab("Input File", jPanel9);

        jPanel10.add(panelOpciones, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Input Options", jPanel10);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel11.add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Output Analysis", jPanel11);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
                display_TIN.addReference(data_refPoly,linesCMap1);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN.removeReference(data_refPoly);
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
                display_TIN.addReference(data_refTr,linesCMap);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN.removeReference(data_refTr);
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
                display_TIN.addReference(data_refPoints,pointsCMap);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN.removeReference(data_refPoints);
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
            java.io.File theFile=new java.io.File("/Users/ricardo/Documents/databases/Smallbasin_DB/Rasters/Topography/1_Arcsec/NED_06075640.metaDEM");
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
    private javax.swing.JButton exportPoiButton;
    private javax.swing.JButton exportTriButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane panelOpciones;
    private javax.swing.JCheckBox pointsCheckBox;
    private javax.swing.JCheckBox trianglesCheckBox;
    private javax.swing.JCheckBox voronoiCheckBox;
    private javax.swing.JSlider zrSlider;
    // End of variables declaration//GEN-END:variables
    
}
