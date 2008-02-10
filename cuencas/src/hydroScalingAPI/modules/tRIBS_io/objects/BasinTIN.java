/*
 * BasinTIN.java
 *
 * Created on March 10, 2007, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import visad.*;
import java.rmi.RemoteException;

import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;

import java.security.*;
import java.math.*;


/**
 *
 * @author Ricardo Mantilla
 */
public class BasinTIN {
    
    private RealType    posIndex=RealType.getRealType("posIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("nodeColor"),
                        voiColor=RealType.getRealType("voiColor");
    
    private RealTupleType   domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude),
                            espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor}),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    private FunctionType func_Inex_to_xEasting_yNorthing_Color=new FunctionType(posIndex, espacioXLYL),
                         func_xEasting_yNorthing_to_Color=new FunctionType(domainXLYL,voiColor);
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    public hydroScalingAPI.util.geomorphology.objects.LinksAnalysis myLinksStructure;
    public hydroScalingAPI.modules.networkAnalysis.objects.RSNDecomposition myRSNAnalysis;
    
    private byte[][] matDir;
    private byte[][] hortonOrder;
    private float[][] DEM;
    private int x,y;
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    
    private byte[][] directionsKey={{1,2,3},{4,5,6},{7,8,9}};
    private java.util.Vector pointsInTriangulation,
                             typeOfPoint,
                             elevationOfPoint,
                             filteredPointsInTriangulation,
                             filteredTypeOfPoint,
                             filteredElevationOfPoint;
    private Delaunay delaun;
    private int numPointsBasin=0;
    private int countNoBorder;
    private int outletNodeID;
    
    private visad.FlatField demField,theColors;
    private visad.Gridded2DSet polygonCountour;
    private int[] nodesPerPolygon;
    private int totalPolygonNodes;
    
    private FlatField vals_ff_Li;
    private float[][] pointProps;
    private UnionSet allTriang,allPoly;
    
    private double latP,lonP,locElev,nLatP,nLonP,adjustFactorLat,adjustFactorLon;
    private visad.RealTuple spotValue;
    public double minX,minY;
    public byte TIN_Zone;
    public boolean TIN_Hemisphere;
    
    private double maxZr=0.0;
    private float baseLatticePercent;
    
    /** Creates a new instance of BasinTIN */
    public BasinTIN( int xO, int yO, byte[][] direcc, byte[][] order, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        
        matDir=direcc;
        metaDatos=md;
        hortonOrder=order;
        x=xO;
        y=yO;
        
        //Getting data
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaDatos);
        metaData.setLocationBinaryFile(new java.io.File(metaDatos.getLocationMeta().getPath().substring(0,metaDatos.getLocationMeta().getPath().lastIndexOf("."))+".corrDEM"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        DEM=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        demField=metaData.getField();
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        myLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        myRSNAnalysis=new hydroScalingAPI.modules.networkAnalysis.objects.RSNDecomposition(myLinksStructure);
        
        int[][] xyBasin=myCuenca.getXYBasin();
        int numPointsImportant=0;
        for (int i = 0;i<xyBasin[0].length;i++){
            if(pointLaplacian(xyBasin[0][i],xyBasin[1][i]) != 0){
                numPointsImportant++;
            }
        }
        
        baseLatticePercent=numPointsImportant/(float)xyBasin[0].length;
        
        System.out.println(baseLatticePercent);
        
        initializeNetworkPoints(0,1,1);
        filterPoints(0,0,0);
        
        intializeBoundaryPolygon();

    }
    
    /** Creates a new instance of BasinTIN */
    public BasinTIN(hydroScalingAPI.mainGUI.ParentGUI mainFrame, java.io.File pathToTriang, String baseName) throws RemoteException, VisADException, java.io.IOException{
        
        //Getting data
        
        countNoBorder=0;
        
        System.out.println(">>>> Reading Edges");
        minX=Double.MAX_VALUE;
        minY=Double.MAX_VALUE;
        java.io.File nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+".nodes");
        java.io.BufferedReader bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        String fullLine;
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        int localNumPoints=Integer.parseInt(fullLine);
        for (int i = 0; i < localNumPoints; i++) {
            String[] lineData=bufferNodes.readLine().split(" ");
            minX=Math.min(minX,Double.parseDouble(lineData[0]));
            minY=Math.min(minY,Double.parseDouble(lineData[1]));
        }
        bufferNodes.close();
        
        pointsInTriangulation=new java.util.Vector();
        typeOfPoint=new java.util.Vector();
        
        nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+".nodes");
        bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        pointProps=new float[3][localNumPoints];
        for (int i = 0; i < localNumPoints; i++) {
            String[] lineData=bufferNodes.readLine().split(" ");
            pointProps[0][i]=(float)(Double.parseDouble(lineData[0])-minX);
            pointProps[1][i]=(float)(Double.parseDouble(lineData[1])-minY);
            pointProps[2][i]=(float)(Double.parseDouble(lineData[3]));
            if(pointProps[2][i] != 1 && pointProps[2][i] != 2) countNoBorder++;
            if(pointProps[2][i] == 2) outletNodeID=i;
            Utm_Coord_3d thisUtm=new Utm_Coord_3d(); thisUtm.x=pointProps[0][i]; thisUtm.y=pointProps[1][i];
            pointsInTriangulation.add(thisUtm);
            typeOfPoint.add(pointProps[2][i]);
        }
        bufferNodes.close();
        
        float[][] linkAccumAVal=new float[1][pointProps[1].length];
        for(int i=0;i<pointProps[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,linkAccumAVal);
        
        vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
        vals_ff_Li.setSamples( pointProps );
        
        System.out.println(">>>> Reading Triangles");
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
        samples[0]=pointProps[0];//.clone();
        samples[1]=pointProps[1];//.clone();
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

        allTriang=new UnionSet(domainXLYL,triangles);

        System.out.println(">>>> Reading Voronoi Polygons");
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
        nodesPerPolygon=new int[countNoBorder];

        lines=new float[2][];
        double[][] newLines=new double[2][];
        
        String polygonsIntrouble="";

        for(int j=0;j<countNoBorder;j++){
            nodesPerPolygon[j]=voroPolys[j].size();

            lines = new float[2][nodesPerPolygon[j]];
            
            for(int i=0;i<nodesPerPolygon[j]-1;i++){
                String[] lineData=((String)voroPolys[j].get(i)).split(",");
                lines[0][i]=(float)(Double.parseDouble(lineData[0])-minX);
                lines[1][i]=(float)(Double.parseDouble(lineData[1])-minY);
            }
            lines[0][nodesPerPolygon[j]-1]=lines[0][0];
            lines[1][nodesPerPolygon[j]-1]=lines[1][0];

            polygons[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);
            try {
                
                regions[j] = DelaunayCustom.fill(polygons[j]);
                        
            } catch (VisADException ex) {
                polygonsIntrouble+=j+"\n";
                break;
            }
            
            
            nodesPerPolygon[j]=regions[j].getLength();
            totalPolygonNodes+=nodesPerPolygon[j];

            
        }

        if(!polygonsIntrouble.equalsIgnoreCase("")){
                Object[] options = { "OK"};
                javax.swing.JOptionPane.showOptionDialog(mainFrame, "The following nodes have problems: \n"+polygonsIntrouble+"Please modify your *_voi file to correct self intersecting paths\nDo not attempt to display any spatial field", "Attention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]);
        }else{
            allPoly=new UnionSet(domainXLYL,polygons);
            UnionSet allRegions=new UnionSet(domainXLYL,regions);
            theColors=new FlatField(func_xEasting_yNorthing_to_Color,allRegions);
        }

        
    }
    
    public void initializeNetworkPoints(int netLevel, int pathDensity, int bufferType) throws RemoteException, VisADException, java.io.IOException{
        
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        int[][] xyDivide=myCuenca.getXYBasinDivide();
        
        pointsInTriangulation=new java.util.Vector();
        typeOfPoint=new java.util.Vector();
        elevationOfPoint=new java.util.Vector();
        
        adjustFactorLat=metaDatos.getResLat()/3600.0;
        adjustFactorLon=metaDatos.getResLon()/3600.0;
        
        float bfs=0.5f;
        
        //adding points along the river network
        for (int i = 0;i<xyBasin[0].length;i++){
            if(hortonOrder[xyBasin[1][i]][xyBasin[0][i]] > netLevel){
                int yP=xyBasin[1][i];
                int xP=xyBasin[0][i];
                
                latP=lonLatsBasin[1][i]+adjustFactorLat/2.0;
                lonP=lonLatsBasin[0][i]+adjustFactorLon/2.0;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                locElev=spotValue.getValues()[0];

                pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
                typeOfPoint.add(new int[] {3});
                elevationOfPoint.add(locElev-1.00);
                
                int delta_yP=((matDir[(int)yP][xP]-1)/3)-1;
                int delta_xP=((matDir[yP][xP]-1)%3)-1;
                
                nLatP=latP+delta_yP*adjustFactorLat;
                nLonP=lonP+delta_xP*adjustFactorLon;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {nLonP,nLatP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                double currentElev=locElev;
                double forwardElev=spotValue.getValues()[0];
                
                if (i>0){
                    
                    //if this is a channel head add a point a quarter of the way backwards (avoids problems with triangulation of almost colinear points)
                    int llegan = 0;
                    for (int k=0; k <= 8; k++){
                        if (matDir[yP+(k/3)-1][xP+(k%3)-1]==9-k && hortonOrder[yP+(k/3)-1][xP+(k%3)-1] > netLevel)
                            llegan++;
                    }
                    
                    if(llegan == 0) {
                        nLatP=latP-delta_yP*adjustFactorLat*1.0/4.0;
                        nLonP=lonP-delta_xP*adjustFactorLon*1.0/4.0;

                        locElev=currentElev;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {5});
                        elevationOfPoint.add(locElev);
                    }
                    
                    if (pathDensity >= 2){
                    
                        //A new point a quarter of the way forward

                        nLatP=latP+delta_yP*adjustFactorLat*1.0/4.0;
                        nLonP=lonP+delta_xP*adjustFactorLon*1.0/4.0;

                        locElev=(3*currentElev+forwardElev)/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {3});
                        elevationOfPoint.add(locElev-1.00);

                    }
                    
                    
                    if (pathDensity >= 1){
                    
                        //A new point half way forward
                    
                        nLatP=latP+delta_yP*adjustFactorLat*2.0/4.0;
                        nLonP=lonP+delta_xP*adjustFactorLon*2.0/4.0;

                        locElev=(2*currentElev+2*forwardElev)/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {3});
                        elevationOfPoint.add(locElev-1.00);
                        
                        if (bufferType == 1){

                            //New points creating a buffer for the river

                            nLatP=latP+(2*delta_yP-bfs*delta_xP)*adjustFactorLat/4.0;
                            nLonP=lonP+(2*delta_xP+bfs*delta_yP)*adjustFactorLon/4.0;

                            pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                            typeOfPoint.add(new int[] {5});
                            elevationOfPoint.add(locElev);

                            nLatP=latP+(2*delta_yP+bfs*delta_xP)*adjustFactorLat/4.0;
                            nLonP=lonP+(2*delta_xP-bfs*delta_yP)*adjustFactorLon/4.0;

                            pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                            typeOfPoint.add(new int[] {5});
                            elevationOfPoint.add(locElev);
                            
                        }
                    }
                    
                    if (pathDensity >= 2){

                        //A new point three quarters of the way forward

                        nLatP=latP+delta_yP*adjustFactorLat*3.0/4.0;
                        nLonP=lonP+delta_xP*adjustFactorLon*3.0/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);

                        locElev=(currentElev+3*forwardElev)/4.0;

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {3});
                        elevationOfPoint.add(locElev-1.00);
                        
                    }
                    
                    if (bufferType == 1){

                        //Two new points acting as buffer for the initial network point
                        //An if statement is needed to avoid hillslope points where there is river network

                        if(hortonOrder[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] <= 0 || matDir[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] != 10-directionsKey[1-delta_xP][1+delta_yP]){
                            nLatP=latP-delta_xP*adjustFactorLat*bfs/4.0;
                            nLonP=lonP+delta_yP*adjustFactorLon*bfs/4.0;

                            pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                            typeOfPoint.add(new int[] {5});
                            elevationOfPoint.add(currentElev);
                        }

                        if(hortonOrder[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] <= 0 || matDir[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] != 10-directionsKey[1+delta_xP][1-delta_yP]){
                            nLatP=latP+delta_xP*adjustFactorLat*bfs/4.0;
                            nLonP=lonP-delta_yP*adjustFactorLon*bfs/4.0;

                            pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                            typeOfPoint.add(new int[] {5});
                            elevationOfPoint.add(currentElev);
                        }
                    }
                }
            }
        }
        
        //Adding points in the basin divide
        int numPoints=lonLatsDivide[0].length-1;
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
        System.out.println("Done Creating points grid");
        
    }
    
    private void intializeBoundaryPolygon() throws visad.VisADException{
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        
        int numPoints=lonLatsDivide[0].length-1;
        Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
        Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

        for(int i=0;i<lonLatsDivide[0].length-1;i++){
            
            latP=(lonLatsDivide[1][i]+lonLatsDivide[1][i+1])/2.0;
            lonP=(lonLatsDivide[0][i]+lonLatsDivide[0][i+1])/2.0;
            
            gdc[i]=new Gdc_Coord_3d();
                gdc[i].longitude=lonP;
                gdc[i].latitude=latP;

            utm[i]=new Utm_Coord_3d();
            
        }
        
        Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());
        Gdc_To_Utm_Converter.Convert(gdc,utm);
        
        float[][] xyContour=new float[2][utm.length];
        for (int i = 0; i < utm.length; i++) {
            xyContour[0][i]=(float)(utm[i].x-minX);
            xyContour[1][i]=(float)(utm[i].y-minY);
        }
        
        polygonCountour=new visad.Gridded2DSet(domainXLYL,xyContour,xyContour[0].length);
    }
    
    public visad.Gridded2DSet getPolygon() throws visad.VisADException {
        return polygonCountour;
    }
    
    public int getOutletNode(){
        return outletNodeID;
    }
    
    public int getNumPoints(){
        return pointsInTriangulation.size();
    }
    
    public double[] getAspect(){
        hydroScalingAPI.util.statistics.Stats eastStats=new hydroScalingAPI.util.statistics.Stats(pointProps[0]);
        hydroScalingAPI.util.statistics.Stats nortStats=new hydroScalingAPI.util.statistics.Stats(pointProps[1]);
        
        return new double[] {Math.min(1,(eastStats.maxValue-eastStats.minValue)/(nortStats.maxValue-nortStats.minValue)),
                             Math.min(1,(nortStats.maxValue-nortStats.minValue)/(eastStats.maxValue-eastStats.minValue))};
        
    }
    
    public void filterPoints(int ridges, float Zr, int netLevel) throws RemoteException, VisADException{
        
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        
        filteredPointsInTriangulation=pointsInTriangulation;
        filteredTypeOfPoint=typeOfPoint;
        filteredElevationOfPoint=elevationOfPoint;
        
        int numPointsT=pointsInTriangulation.size();
        int numPointsO=0;
        
        System.out.println(">>> Done adding border and network points");
        
        if(Zr == 0){
            
            //adding points inside the basin
            for (int i = 0;i<xyBasin[0].length;i++){
                if(hortonOrder[xyBasin[1][i]][xyBasin[0][i]] <= netLevel){
                    latP=lonLatsBasin[1][i]+adjustFactorLat/2.0;
                    lonP=lonLatsBasin[0][i]+adjustFactorLon/2.0;

                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);

                    locElev=spotValue.getValues()[0];

                    filteredPointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
                    filteredTypeOfPoint.add(new int[] {0});
                    filteredElevationOfPoint.add(locElev);
                    numPointsBasin++;
                }
            }
        
            int numPoints=filteredPointsInTriangulation.size();
            Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
            Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

            for(int i=0;i<numPoints;i++){
                gdc[i]=(Gdc_Coord_3d)filteredPointsInTriangulation.get(i);
                utm[i]=new Utm_Coord_3d();
            }

            Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());
            Gdc_To_Utm_Converter.Convert(gdc,utm);
            
            TIN_Zone=utm[0].zone;
            TIN_Hemisphere=utm[0].hemisphere_north;

            System.out.println("Done Tranforming coordinates");

            minX=Double.MAX_VALUE;
            minY=Double.MAX_VALUE;
            for (int i = 0; i < numPoints; i++) {
                minX=Math.min(minX,utm[i].x);
                minY=Math.min(minY,utm[i].y);
            }

            pointProps=new float[3][numPoints];
            float[][] elevPoints=new float[1][numPoints];

            for(int i=0;i<numPoints;i++){
                pointProps[0][i]=(float)(utm[i].x-minX);
                pointProps[1][i]=(float)(utm[i].y-minY);
                pointProps[2][i]=(float)((int[])typeOfPoint.get(i))[0];
                elevPoints[0][i]=((Double)elevationOfPoint.get(i)).floatValue();
            }

            System.out.println("Done Creating Array pointsInTriangulation");

            float[][] pointIndex=new float[1][pointProps[1].length];
            for(int i=0;i<pointProps[0].length;i++){
                pointIndex[0][i]=(float)i;//xyLinkValues[0][i];
            }

            Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,pointIndex);

            vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
            vals_ff_Li.setSamples( pointProps );

            System.out.println("Done Creating fields");

            return;
        }
        
        //First: determine ridges at the desired scale
            
        int[][] hillSlopesMask=myCuenca.getEncapsulatedHillslopesMask(matDir,myRSNAnalysis,ridges+1);

        visad.RealTuple spotValue; double locElev;

        int cuencaMinX=myCuenca.getMinX();
        int cuencaMinY=myCuenca.getMinY();
        double cuencaResLon=metaDatos.getResLon();
        double cuencaResLat=metaDatos.getResLat();
        double cuencaMinLon=metaDatos.getMinLon();
        double cuencaMinLat=metaDatos.getMinLat();

        double randomThreshold=1-baseLatticePercent/Zr;//0.95;//0.95-ridges*0.05;

        System.out.println(randomThreshold);


        for (int i = 0; i < hillSlopesMask.length; i++) {
            for (int j = 0; j < hillSlopesMask[0].length; j++) {
                if(hillSlopesMask[i][j] != 0 && hortonOrder[i+cuencaMinY-1][j+cuencaMinX-1] <= netLevel){

                    double myLon=(j+cuencaMinX-1+0.5)*cuencaResLon/3600.0+cuencaMinLon;
                    double myLat=(i+cuencaMinY-1+0.5)*cuencaResLat/3600.0+cuencaMinLat;


                    boolean gotIn=false;
                    float normalizer=0f;
                    float pushX=0f;
                    float pushY=0f;

                    for (int k=0; k <= 8; k++){
                        int ii=i+(k/3)-1;
                        int jj=j+(k%3)-1;

                        if ((k==1 || k==3 || k==5 || k==7) && hillSlopesMask[ii][jj] != hillSlopesMask[i][j]){

                            normalizer++;
                            pushX+=jj-j;
                            pushY+=ii-i;


                            gotIn=true;
                        }
                    }

                    if(!gotIn && Zr < 20){
                        if(Math.random() > randomThreshold){
                            gotIn=true;
                        }
                    }

                    if(gotIn){

                        double[] lonLatPair=new double[] {myLon,myLat};

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, lonLatPair),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                        locElev=spotValue.getValues()[0];
                        filteredPointsInTriangulation.add(new Gdc_Coord_3d(myLat,myLon,locElev));
                        filteredTypeOfPoint.add(new int[] {0});
                        filteredElevationOfPoint.add(locElev);
                    }
                }
            }
        }
        
        
        //Third: Project and add to display
        
        int numPoints=filteredPointsInTriangulation.size();
        Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
        Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

        for(int i=0;i<numPoints;i++){
            gdc[i]=(Gdc_Coord_3d)filteredPointsInTriangulation.get(i);
            utm[i]=new Utm_Coord_3d();
        }

        Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());
        Gdc_To_Utm_Converter.Convert(gdc,utm);

        System.out.println("Done Tranforming coordinates");

//        minX=Double.MAX_VALUE;
//        minY=Double.MAX_VALUE;
//        for (int i = 0; i < numPoints; i++) {
//            minX=Math.min(minX,utm[i].x);
//            minY=Math.min(minY,utm[i].y);
//        }

        pointProps=new float[3][numPoints];
        float[][] elevPoints=new float[1][numPoints];

        for(int i=numPoints-1;i>=0;i--){
            pointProps[0][i]=(float)(utm[i].x-minX);
            pointProps[1][i]=(float)(utm[i].y-minY);
            pointProps[2][i]=(float)((int[])typeOfPoint.get(i))[0];
            elevPoints[0][i]=((Double)elevationOfPoint.get(i)).floatValue();
        }

        System.out.println("Done Creating Array pointsInTriangulation");

        float[][] pointIndex=new float[1][pointProps[1].length];
        for(int i=0;i<pointProps[0].length;i++){
            pointIndex[0][i]=(float)i;//xyLinkValues[0][i];
        }

        Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,pointIndex);

        vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
        vals_ff_Li.setSamples( pointProps );

        System.out.println("Done Creating fields");
        
        if(false){

            //Second: remove points according to Zr

            int iii=0;
            float[][] samplesO=new float[5][numPointsO];
            for(int j=0;j<numPointsO;j++){
                if(((int[])typeOfPoint.get(j))[0] == 0){
                    Utm_Coord_3d utmLocal=(Utm_Coord_3d)pointsInTriangulation.get(j);
                    samplesO[0][iii]=(float)(utmLocal.x-minX);
                    samplesO[1][iii]=(float)(utmLocal.y-minY);
                    samplesO[2][iii]=j;
                    samplesO[3][iii]=1;
                    samplesO[4][iii]=((Double)elevationOfPoint.get(j)).floatValue();
                    iii++;
                }
            }

            System.out.println(">>> Created array of interior points with "+numPointsO+" elements");



            //ITERATIVE COMPONENT

            int pointsPreserved;
            float[][] samples;
            float[] samplesElevs;

            if(maxZr != 0.0){

                pointsPreserved=-1;

                int passNumber=0;

                System.out.println(">>> Initializing Zr Iterations");

                while(pointsPreserved != 0){

                    pointsPreserved=0;

                    numPoints=filteredPointsInTriangulation.size();
                    samples=new float[2][numPoints];
                    samplesElevs=new float[numPoints];
                    for(int i=0;i<numPoints;i++){
                        Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
                        samples[0][i]=(float)(utmLocal.x-minX);
                        samples[1][i]=(float)(utmLocal.y-minY);
                        samplesElevs[i]=((Double)filteredElevationOfPoint.get(i)).floatValue();
                    }

                    long startQ = System.currentTimeMillis();

                    System.out.println("DelaunayClarkson algorithm.");
                    delaun = (Delaunay) new DelaunayClarkson(samples);

                    double passThreshold=0.03125*Math.exp(0.6931*passNumber);

                    System.out.println("Walk through algorithm.");
                    for (int i = 0; i < delaun.Tri.length; i++) {

                        int px1=(int)(1e5*samples[0][delaun.Tri[i][0]]);
                        int px2=(int)(1e5*samples[0][delaun.Tri[i][1]]);
                        int px3=(int)(1e5*samples[0][delaun.Tri[i][2]]);

                        int py1=(int)(1e5*samples[1][delaun.Tri[i][0]]);
                        int py2=(int)(1e5*samples[1][delaun.Tri[i][1]]);
                        int py3=(int)(1e5*samples[1][delaun.Tri[i][2]]);

                        java.awt.Polygon myTri=
                                new java.awt.Polygon(
                                    new int[] {px1,px2,px3}, 
                                    new int[] {py1,py2,py3},
                                    3);

                        java.awt.Rectangle myBounds=myTri.getBounds();

        //                System.out.println("Now working on Triangle "+i+" with bounds "+myBounds.x+" "+(myBounds.x+myBounds.width) + " "+myBounds.y+" "+(myBounds.y+myBounds.height));
        //                System.out.println(" CoordinatesX "+px1+" "+px2+" "+px3);
        //                System.out.println(" CoordinatesY "+py1+" "+py2+" "+py3);


                        int pointX_C=(int)((px1+px2+px3)/3.0);
                        int pointY_C=(int)((py1+py2+py3)/3.0);

                        double d1_C=Math.sqrt(Math.pow(pointX_C-px1,2)+Math.pow(pointY_C-py1,2));
                        double d2_C=Math.sqrt(Math.pow(pointX_C-px2,2)+Math.pow(pointY_C-py2,2));
                        double d3_C=Math.sqrt(Math.pow(pointX_C-px3,2)+Math.pow(pointY_C-py3,2));

                        if(d1_C < 3e6 && d2_C < 3e6 && d3_C < 3e6) {
                            continue;
                        }

                        if(passNumber > 0){
                            if(Math.random() > passThreshold){
                                continue;
                            }
                        }


                        java.util.Vector potentialCandidates=new java.util.Vector();
                        int maxIndex=-9999;
                        float maxVal=-1.0f;

                        int numPointsAnalyzed=0;

                        for(int j=0;j<samplesO[0].length;j++){

                            int pointX=(int)(1e5*samplesO[0][j]);
                            int pointY=(int)(1e5*samplesO[1][j]);

                            if(samplesO[3][j] == 1){
                                double d1=Math.sqrt(Math.pow(pointX-px1,2)+Math.pow(pointY-py1,2));
                                double d2=Math.sqrt(Math.pow(pointX-px2,2)+Math.pow(pointY-py2,2));
                                double d3=Math.sqrt(Math.pow(pointX-px3,2)+Math.pow(pointY-py3,2));

                                if(d1 < 1e5 || d2 < 1e5 || d3 < 1e5) samplesO[3][j] = 0;
                            }


                            if(samplesO[3][j] == 1 && pointX > myBounds.x && pointX < myBounds.x+myBounds.width && pointY > myBounds.y && pointY < myBounds.y+myBounds.height){
                                if(myTri.contains(pointX,pointY)) {

                                    double e0=samplesO[4][j];
                                    double e1=samplesElevs[delaun.Tri[i][0]];
                                    double d1=Math.sqrt(Math.pow(pointX-px1,2)+Math.pow(pointY-py1,2));
                                    double e2=samplesElevs[delaun.Tri[i][1]];
                                    double d2=Math.sqrt(Math.pow(pointX-px2,2)+Math.pow(pointY-py2,2));
                                    double e3=samplesElevs[delaun.Tri[i][2]];
                                    double d3=Math.sqrt(Math.pow(pointX-px3,2)+Math.pow(pointY-py3,2));

                                    float diffElev=(float)((e1*d1+e2*d2+e3*d3)/(d1+d2+d3)-e0);

                                    if(Math.abs(diffElev) > maxVal) {
                                        maxIndex=potentialCandidates.size();
                                        maxVal=Math.abs(diffElev);
                                    }

                                    int indexToGet=(int)samplesO[2][j];
                                    potentialCandidates.add(new float[] {pointX/1e5f,pointY/1e5f,indexToGet,diffElev,j});
                                }
                                numPointsAnalyzed++;
                            }

                        }

        //                System.out.println("Number of Potential Candidates: "+potentialCandidates.size()+" Number of points considered: "+numPointsAnalyzed);

                        if(potentialCandidates.size() > 0){

                            maxZr=Math.max(maxVal,maxZr);

        //                    System.out.println(maxIndex+" "+maxVal);
        //                    for (int j = 0; j < potentialCandidates.size(); j++) {
        //                        System.out.println(java.util.Arrays.toString((float[])potentialCandidates.get(j)));
        //                    }
                            if(Zr != -9999 && maxVal > Zr){
                                float[] infoPoint=(float[])potentialCandidates.get(maxIndex);
                                int indexToGet=(int)infoPoint[2];
                                filteredPointsInTriangulation.add(pointsInTriangulation.get(indexToGet));
                                filteredTypeOfPoint.add(typeOfPoint.get(indexToGet));
                                filteredElevationOfPoint.add(elevationOfPoint.get(indexToGet));

                                samplesO[3][(int)infoPoint[4]]=0;
                                pointsPreserved++;

                            }
                        }

                    }
                    long endQ = System.currentTimeMillis();
                    float timeQ = (endQ - startQ) / 1000f;
                    System.out.println("Going through took " + timeQ + " seconds.");

                    System.out.println("Pass: "+passNumber);
                    System.out.println("Number of points included is: "+pointsPreserved);
                    System.out.println("Random Threshold: "+passThreshold);

                    passNumber++;
                }
            } else maxZr = 40;

        }

        //Fourth: Calculate triangulation and Voronoi Polygons
        float[][] samples=new float[2][];
        samples[0]=pointProps[0];//.clone();
        samples[1]=pointProps[1];//.clone();
        System.out.println("the DelaunayClarkson algorithm.");
        long start = System.currentTimeMillis();
        Delaunay.perturb(samples,0.5f,false);
        delaun = (Delaunay) new DelaunayClarkson(samples);
        
        long end = System.currentTimeMillis();
        float time = (end - start) / 1000f;
        System.out.println("Triangulation took " + time + " seconds.");
        //samples[0]=xyLinkValues[0].clone();
        //samples[1]=xyLinkValues[1].clone();

        vals_ff_Li.setSamples( pointProps );

//        System.out.println(delaun.NumEdges);
//
//        for(int j=0;j<delaun.Edges.length;j++){
//            for(int i=0;i<delaun.Edges[j].length;i++){
//                System.out.print(delaun.Edges[j][i]+"\t\t");
//            }
//            System.out.println();
//        }
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

        allTriang=new UnionSet(domainXLYL,triangles);

        int numValidPoints=0;
        for(int j=0;j<pointProps[2].length;j++) {
            if(pointProps[2][j] != 1 ) numValidPoints++;
        }

        float[] s0 = samples[0];
        float[] s1 = samples[1];

        countNoBorder=numValidPoints-1;

        nodesPerPolygon=new int[countNoBorder];

        Gridded2DSet[] polygons=new Gridded2DSet[numValidPoints-1];
        int kp=0;
        for(int k=0;k<pointProps[2].length;k++) {
        //for(int k=0;k<5;k++) {    
            if(pointProps[2][k] != 1 && pointProps[2][k] != 2){
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

                polygons[kp]=new Gridded2DSet(domainXLYL,lines1,lines1[0].length);
                nodesPerPolygon[kp++]=lines1[0].length;
            }
        }
        allPoly=new UnionSet(domainXLYL,polygons);
            
    }
    

    
    /**
     * Creates a {@link visad.RealTuple} to be plotted in a {@link visad.Display}
     * @return The {@link visad.RealTuple}
     * @throws visad.VisADException Captures errors while creating the {@link visad.RealTuple}
     * @throws java.rmi.RemoteException Captures errors while creating the {@link visad.RealTuple}
     */
    public visad.RealTuple getPositionTuple(int index) throws visad.VisADException, java.rmi.RemoteException{
        Utm_Coord_3d thePointInfo = (Utm_Coord_3d)pointsInTriangulation.get(index);
        double xx=thePointInfo.x;
        double yy=thePointInfo.y;
        visad.Real[] rtd1 = {new visad.Real(xEasting, xx),
                             new visad.Real(yNorthing,  yy)};
        return new visad.RealTuple(rtd1);
    }
    
    public visad.Tuple getTextTuple(int index)  throws visad.VisADException, java.rmi.RemoteException{
        visad.TextType t = visad.TextType.getTextType("text");
        Utm_Coord_3d thePointInfo = (Utm_Coord_3d)pointsInTriangulation.get(index);
        double xx=thePointInfo.x;
        double yy=thePointInfo.y;
        visad.Data[] rtd1 = {new visad.Real(xEasting, xx),
                             new visad.Real(yNorthing,  yy),
                             new visad.Text(t, ""+index)};
        return new visad.Tuple(rtd1);
    }
    
    public double getMaxZr(){
        return maxZr;
    }
    
    public visad.FlatField getPointsFlatField(){
        return vals_ff_Li;
    }
    
    public visad.FlatField getValuesFlatField(){
        return theColors;
    }
    
    public UnionSet getTrianglesUnionSet(){
        return allTriang;
    }
    
    public UnionSet getPolygonsUnionSet(){
        return allPoly;
    }
    
    public int getBasinOrder(){
        return myLinksStructure.getBasinOrder();
    }
    
    public void writeTriangulation(java.io.File outputLocation) throws java.io.IOException{
        
        //I could never get this to work
        if (true) return;
        
        
        int numPoints=filteredPointsInTriangulation.size();
        float[][] samples=new float[3][numPoints];
        for(int i=0;i<numPoints;i++){
            Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
            samples[0][i]=(float)utmLocal.x;
            samples[1][i]=(float)utmLocal.y;
            samples[2][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
        }

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
    
    public void writePoints(java.io.File outputLocation) throws java.io.IOException{
        
        int numPoints=filteredPointsInTriangulation.size();
        Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
        Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

        for(int i=0;i<numPoints;i++){
            gdc[i]=(Gdc_Coord_3d)filteredPointsInTriangulation.get(i);
            utm[i]=new Utm_Coord_3d();
        }

        Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());
        Gdc_To_Utm_Converter.Convert(gdc,utm);

        System.out.println("Done Tranforming coordinates");
        
        float[][] samples=new float[4][numPoints];
        for(int i=0;i<numPoints;i++){
            Utm_Coord_3d utmLocal=utm[i];
            samples[0][i]=(float)utmLocal.x;
            samples[1][i]=(float)utmLocal.y;
            samples[2][i]=((Double)filteredElevationOfPoint.get(i)).floatValue();
            samples[3][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
            if(samples[3][i]==5) samples[3][i]=0;
        }
        
        System.out.println("Points File");
        
        String sufix="";
        if(outputLocation.getPath().indexOf(".points") == -1) sufix=".points";

        java.io.File nodesFileLocation=new java.io.File(outputLocation.getPath()+sufix);
        java.io.BufferedWriter writerNodes = new java.io.BufferedWriter(new java.io.FileWriter(nodesFileLocation));
        writerNodes.write(samples[0].length+"\n");
        
        for (int i=0; i<samples[0].length; i++){
            writerNodes.write((samples[0][i]+Math.random()*0.2)+" ");
            writerNodes.write((samples[1][i]+Math.random()*0.2)+" ");
            writerNodes.write(samples[2][i]+" ");
            writerNodes.write((int)samples[3][i]+"\n");
        }
        
        writerNodes.close();
        
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
    
    public int getNumVoi(){
        return countNoBorder;
    }
    
    public float[] valuesToVoroValues(float[] theValues){
        if(theValues.length != countNoBorder) return null;
        
        float[] voroValues=new float[totalPolygonNodes];
        int k=0;
        for (int i = 0; i < theValues.length; i++) {
            for (int j = 0; j < nodesPerPolygon[i]; j++) {
                voroValues[k++]=theValues[i];
            }
        }
        return voroValues;
        
    }
    
    public String GridMD5(){
        
        String s="TheEntireGrid:";
        try {
            float[][] values=vals_ff_Li.getFloats();
            for (int i = 0; i < values.length; i++) {
                for (int j = 0; j < values[i].length; j+=10) {
                    s+=""+values[i][j];
                }
            }
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(),0,s.length());
            return new BigInteger(1,m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}
