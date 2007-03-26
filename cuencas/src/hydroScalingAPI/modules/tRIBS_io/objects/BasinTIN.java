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
    private byte[][] matDir;
    private int[][] magnitudes;
    private float[][] DEM;
    
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
    
    private visad.FlatField demField,theColors;
    private int[] nodesPerPolygon;
    private int totalPolygonNodes;
    
    private FlatField vals_ff_Li;
    private float[][] pointProps;
    private UnionSet allTriang,allPoly;
    
    /** Creates a new instance of BasinTIN */
    public BasinTIN( int x, int y, byte[][] direcc, int[][] magnit, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        
        matDir=direcc;
        metaDatos=md;
        magnitudes=magnit;
        
        //Getting data
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaDatos);
        metaData.setLocationBinaryFile(new java.io.File(metaDatos.getLocationMeta().getPath().substring(0,metaDatos.getLocationMeta().getPath().lastIndexOf("."))+".corrDEM"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        DEM=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        demField=metaData.getField();
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        
        float[][] lonLatsBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        
        float[][] lonLatsDivide=myCuenca.getLonLatBasinDivide();
        int[][] xyDivide=myCuenca.getXYBasinDivide();
        
        pointsInTriangulation=new java.util.Vector();
        typeOfPoint=new java.util.Vector();
        elevationOfPoint=new java.util.Vector();
        
        double latP,lonP,locElev,nLatP,nLonP,adjustFactorLat,adjustFactorLon;
        visad.RealTuple spotValue;
        
        adjustFactorLat=metaDatos.getResLat()/3600.0;
        adjustFactorLon=metaDatos.getResLon()/3600.0;
        
        //adding points inside the basin
        for (int i = 0;i<xyBasin[0].length;i++){
            if(magnitudes[xyBasin[1][i]][xyBasin[0][i]] <= 0){
                latP=lonLatsBasin[1][i]+adjustFactorLat/2.0;
                lonP=lonLatsBasin[0][i]+adjustFactorLon/2.0;
                
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
                
                latP=lonLatsBasin[1][i]+adjustFactorLat/2.0;
                lonP=lonLatsBasin[0][i]+adjustFactorLon/2.0;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                locElev=spotValue.getValues()[0];

                pointsInTriangulation.add(new Gdc_Coord_3d(latP,lonP,locElev));
                typeOfPoint.add(new int[] {3});
                elevationOfPoint.add(locElev);
                
                int delta_yP=((matDir[(int)yP][xP]-1)/3)-1;
                int delta_xP=((matDir[yP][xP]-1)%3)-1;
                
                nLatP=latP+delta_yP*adjustFactorLat;
                nLonP=lonP+delta_xP*adjustFactorLon;
                
                spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {nLonP,nLatP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                double currentElev=locElev;
                double forwardElev=spotValue.getValues()[0];
                
                if (i>0){
                    
                    //A new point a quarter of the way forward

                    nLatP=latP+delta_yP*adjustFactorLat*1.0/4.0;
                    nLonP=lonP+delta_xP*adjustFactorLon*1.0/4.0;

                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(3*currentElev+forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);

                    //A new point half way forward
                    
                    nLatP=latP+delta_yP*adjustFactorLat*2.0/4.0;
                    nLonP=lonP+delta_xP*adjustFactorLon*2.0/4.0;
                    
                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(2*currentElev+2*forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);

                        //New points creating a buffer for the river

                        nLatP=latP+(2*delta_yP-bfs*delta_xP)*adjustFactorLat/4.0;
                        nLonP=lonP+(2*delta_xP+bfs*delta_yP)*adjustFactorLon/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);

                        nLatP=latP+(2*delta_yP+bfs*delta_xP)*adjustFactorLat/4.0;
                        nLonP=lonP+(2*delta_xP-bfs*delta_yP)*adjustFactorLon/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);

                    //A new point three quarters of the way forward

                    nLatP=latP+delta_yP*adjustFactorLat*3.0/4.0;
                    nLonP=lonP+delta_xP*adjustFactorLon*3.0/4.0;

                    spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                    locElev=(currentElev+3*forwardElev)/4.0;

                    pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                    typeOfPoint.add(new int[] {3});
                    elevationOfPoint.add(locElev);
                    
                    //Two new points acting as buffer for the initial network point
                    //An if statement is needed to avoid hillslope points where there is river network
                    
                    if(magnitudes[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] <= 0 || matDir[xyBasin[1][i]-delta_xP][xyBasin[0][i]+delta_yP] != 10-directionsKey[1-delta_xP][1+delta_yP]){
                        nLatP=latP-delta_xP*adjustFactorLat*bfs/4.0;
                        nLonP=lonP+delta_yP*adjustFactorLon*bfs/4.0;

                        spotValue=(visad.RealTuple) demField.evaluate(new visad.RealTuple(domain, new double[] {lonP,latP}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                        locElev=spotValue.getValues()[0];

                        pointsInTriangulation.add(new Gdc_Coord_3d(nLatP,nLonP,locElev));
                        typeOfPoint.add(new int[] {0});
                        elevationOfPoint.add(locElev);
                    }
                    
                    if(magnitudes[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] <= 0 || matDir[xyBasin[1][i]+delta_xP][xyBasin[0][i]-delta_yP] != 10-directionsKey[1+delta_xP][1-delta_yP]){
                        nLatP=latP+delta_xP*adjustFactorLat*bfs/4.0;
                        nLonP=lonP-delta_yP*adjustFactorLon*bfs/4.0;

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
        
        pointProps=new float[3][numPoints];
        
        for(int i=0;i<numPoints;i++){
            pointProps[0][i]=(float)utm[i].x;
            pointProps[1][i]=(float)utm[i].y;
            pointProps[2][i]=(float)((int[])typeOfPoint.get(i))[0];
            pointsInTriangulation.add(utm[i]);
        }
        
        float[][] pointIndex=new float[1][pointProps[1].length];
        for(int i=0;i<pointProps[0].length;i++){
            pointIndex[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,pointIndex);
        
        vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
        vals_ff_Li.setSamples( pointProps );
        
        filteredPointsInTriangulation=(java.util.Vector)pointsInTriangulation.clone();
        filteredTypeOfPoint=(java.util.Vector)typeOfPoint.clone();
        filteredElevationOfPoint=(java.util.Vector)elevationOfPoint.clone();

    }
    
    /** Creates a new instance of BasinTIN */
    public BasinTIN(java.io.File pathToTriang, String baseName) throws RemoteException, VisADException, java.io.IOException{
        
        //Getting data
        
        countNoBorder=0;
        
        //Read nodes file
        java.io.File nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+".nodes");
        java.io.BufferedReader bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        String fullLine;
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        int localNumPoints=Integer.parseInt(fullLine);
        pointProps=new float[3][localNumPoints];
        for (int i = 0; i < localNumPoints; i++) {
            String[] lineData=bufferNodes.readLine().split(" ");
            pointProps[0][i]=Float.parseFloat(lineData[0]);
            pointProps[1][i]=Float.parseFloat(lineData[1]);
            pointProps[2][i]=Float.parseFloat(lineData[3]);
            if(pointProps[2][i] != 1 && pointProps[2][i] != 2) countNoBorder++;
            
        }
        bufferNodes.close();
        
        float[][] linkAccumAVal=new float[1][pointProps[1].length];
        for(int i=0;i<pointProps[0].length;i++){
            linkAccumAVal[0][i]=(float)i;//xyLinkValues[0][i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,linkAccumAVal);
        
        vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
        vals_ff_Li.setSamples( pointProps );
        
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
        nodesPerPolygon=new int[countNoBorder];

        lines=new float[2][];

        for(int j=0;j<countNoBorder;j++){
            nodesPerPolygon[j]=voroPolys[j].size();

            lines = new float[2][nodesPerPolygon[j]];
            
            for(int i=0;i<nodesPerPolygon[j]-1;i++){
                String[] lineData=((String)voroPolys[j].get(i)).split(",");
                lines[0][i]=Float.parseFloat(lineData[0]);
                lines[1][i]=Float.parseFloat(lineData[1]);
            }
            lines[0][nodesPerPolygon[j]-1]=lines[0][0];
            lines[1][nodesPerPolygon[j]-1]=lines[1][0];

            polygons[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);

//            Delaunay dela1=new DelaunayClarkson(lines);
//            
            regions[j] = DelaunayCustom.fill(polygons[j]);
            
            nodesPerPolygon[j]=regions[j].getLength();
            totalPolygonNodes+=nodesPerPolygon[j];

            //float theColor=3.0f*(float)Math.random();
            //for (int i = j*4; i < (j+1)*4; i++) colors[0][i]=theColor;
            
        }

        allPoly=new UnionSet(domainXLYL,polygons);
        
        UnionSet allRegions=new UnionSet(domainXLYL,regions);

        float[][] colors=new float[1][countNoBorder];
        for (int i = 0; i < colors[0].length; i++) {
            colors[0][i]=10f*(float)Math.random();
        }
        colors[0]=valuesToVoroValues(colors[0]);
        theColors=new FlatField(func_xEasting_yNorthing_to_Color,allRegions);
        theColors.setSamples(colors);
        
    }
    
    public double[] getAspect(){
        hydroScalingAPI.util.statistics.Stats eastStats=new hydroScalingAPI.util.statistics.Stats(pointProps[1]);
        hydroScalingAPI.util.statistics.Stats nortStats=new hydroScalingAPI.util.statistics.Stats(pointProps[0]);
        
        return new double[] {Math.min(1,(nortStats.maxValue-nortStats.minValue)/(eastStats.maxValue-eastStats.minValue)), 
                             Math.min(1,(eastStats.maxValue-eastStats.minValue)/(nortStats.maxValue-nortStats.minValue))};
        
    }
    
    public void filterPoints(boolean ridges, float Zr) throws RemoteException, VisADException{
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
        
        Irregular1DSet xVarIndex=new Irregular1DSet(posIndex,linkAccumAVal);
        
        vals_ff_Li = new FlatField( func_Inex_to_xEasting_yNorthing_Color, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
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
            
            allTriang=new UnionSet(domainXLYL,triangles);
            
            int numValidPoints=0;
            for(int j=0;j<xyLinkValues[2].length;j++) {
                if(xyLinkValues[2][j] != 1 ) numValidPoints++;
            }
            
            float[] s0 = samples[0];
            float[] s1 = samples[1];
            
            countNoBorder=numValidPoints-1;
            
            nodesPerPolygon=new int[countNoBorder];
            
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
                    
                    polygons[kp]=new Gridded2DSet(domainXLYL,lines1,lines1[0].length);
                    nodesPerPolygon[kp++]=lines1[0].length;
                }
            }
            allPoly=new UnionSet(domainXLYL,polygons);
            
            
        }
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
    
    public void writeTriangulation(java.io.File outputLocation) throws java.io.IOException{
        
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
        float[][] samples=new float[4][numPoints];
        for(int i=0;i<numPoints;i++){
            Utm_Coord_3d utmLocal=(Utm_Coord_3d)filteredPointsInTriangulation.get(i);
            samples[0][i]=(float)utmLocal.x;
            samples[1][i]=(float)utmLocal.y;
            samples[2][i]=((Double)filteredElevationOfPoint.get(i)).floatValue();
            samples[3][i]=(float)((int[])filteredTypeOfPoint.get(i))[0];
        }
        
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
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
