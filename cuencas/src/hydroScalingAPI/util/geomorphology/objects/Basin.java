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


package hydroScalingAPI.util.geomorphology.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class Basin extends Object{

    private int[][] xyBasin;
    private int[][] xyContour;
    private float[][] lonLatCountour;
    private int minX,maxX,minY,maxY;
    private float minZ,maxZ;
    private hydroScalingAPI.io.MetaRaster localMetaRaster;
    
    private int[][] toMark;
    
    public Basin(int x, int y, byte[][] fullDirMatrix, hydroScalingAPI.io.MetaRaster mr) {
        
        localMetaRaster=mr;
        
        minX=fullDirMatrix[0].length-1;
        maxX=0;
        minY=fullDirMatrix.length-1;
        maxY=0;
        
        java.util.Vector idsBasin=new java.util.Vector();
        
        toMark=new int[1][]; toMark[0]=new int[] {x,y};
        idsBasin.add(toMark[0]);
        while(toMark != null){
            getBasin(fullDirMatrix, idsBasin,toMark);
        }
        
        xyBasin=new int[2][idsBasin.size()];
        
        int nPoints=idsBasin.size();
        
        for (int k=0; k < nPoints; k++){
            int[] xys=(int[]) idsBasin.elementAt(k);
            xyBasin[0][k]=xys[0];
            minX=Math.min(minX,xys[0]); maxX=Math.max(maxX,xys[0]); 
            xyBasin[1][k]=xys[1];
            minY=Math.min(minY,xys[1]); maxY=Math.max(maxY,xys[1]); 
        }
        
        findBasinDivide(fullDirMatrix);
        
    }
    
    public String toString(){
        return localMetaRaster.getLocationBinaryFile().getName()+"_Basin_x"+xyBasin[0][0]+"_y_"+xyBasin[1][0];
    }
    
    public void getBasin(byte [][] fullDirMatrix, java.util.Vector idsBasin,int[][] ijs){
        java.util.Vector tribsVector=new java.util.Vector();
        
        for(int incoming=0;incoming<ijs.length;incoming++){
            int j=ijs[incoming][0];
            int i=ijs[incoming][1];
            for (int k=0; k <= 8; k++){
                if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k){
                    tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                    //getBasin(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,idsBasin);
                }
            }
        }
        int countTribs=tribsVector.size();
        //System.out.println(countTribs);
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
                idsBasin.add(toMark[k]);
                //System.out.println(">>"+toMark[k][0]+" "+toMark[k][1]);
            }
        } else {
            toMark=null;
        }
    }
    
    /*public void getBasin(int i,int j,byte [][] fullDirMatrix, java.util.Vector idsBasin){
       for (int k=0; k <= 8; k++){
           if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k){
             idsBasin.add(new int[] {j+(k%3)-1,i+(k/3)-1});
             getBasin(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,idsBasin);
           }
       }
    }*/
    
    public float[][] getLonLatBasin(){
        
        float[][] LonLatBasin=new float[2][xyBasin[0].length];
    
        for (int k=0; k < xyBasin[0].length; k++){
            LonLatBasin[0][k]=(float) (xyBasin[0][k]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            LonLatBasin[1][k]=(float) (xyBasin[1][k]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
        }
        return LonLatBasin;
    }
    
    public int[][] getXYBasin(){
        return xyBasin;
    }
    
    public visad.Gridded2DSet getBasinDivide() throws visad.VisADException {
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet basinCountour=new visad.Gridded2DSet(domain,lonLatCountour,lonLatCountour[0].length);
        
        return basinCountour;
    }
    
    public float[] getDivideShapeFactor(){
        
        float shapeF=1;
        
        float[][] lonLatCountourMeters=new float[2][lonLatCountour[0].length];
        
        geotransform.coords.Gdc_Coord_3d gdc = new geotransform.coords.Gdc_Coord_3d();
        geotransform.coords.Utm_Coord_3d utm = new geotransform.coords.Utm_Coord_3d();
        utm.zone=(byte)(4/27.0*lonLatCountour[1][0]+28.666);
        utm.z=0;
        utm.hemisphere_north=true;

        geotransform.transforms.Gdc_To_Utm_Converter.Init(new geotransform.ellipsoids.CD_Ellipsoid());
        
        for(int i=0;i<lonLatCountour[0].length;i++){
        
            gdc.longitude=lonLatCountour[0][i];
            gdc.latitude=lonLatCountour[1][i];

            geotransform.transforms.Gdc_To_Utm_Converter.Convert(gdc,utm);
            
            lonLatCountourMeters[0][i]=(float)utm.x;
            lonLatCountourMeters[1][i]=(float)utm.y;

            
        }
        
        float dm=Float.MIN_VALUE; int llPos=0;
        for(int i=0;i<lonLatCountourMeters[0].length;i++){
            float ddd=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[1][0], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            if(ddd > dm) {
                dm=ddd;
                llPos=i;
            }
        }
        float dm1=Float.MIN_VALUE,dm2=Float.MIN_VALUE, dAl=Float.MIN_VALUE;
        for(int i=llPos+1;i<lonLatCountourMeters[0].length;i++){
            dm1=distancePoints(lonLatCountourMeters[0][llPos], lonLatCountourMeters[1][llPos], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            dm2=distancePoints(lonLatCountourMeters[0][llPos], lonLatCountourMeters[0][llPos], lonLatCountourMeters[0][i], lonLatCountourMeters[0][i]);
            dAl=Math.max(dm1*dm2/(float)Math.sqrt(dm1*dm1+dm2*dm2),dAl);
        }
        float dAr=Float.MIN_VALUE;
        for(int i=1;i<llPos;i++){
            dm1=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[1][0], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            dm2=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[0][0], lonLatCountourMeters[0][i], lonLatCountourMeters[0][i]);
            dAr=Math.max(dm1*dm2/(float)Math.sqrt(dm1*dm1+dm2*dm2),dAr);
        }
        shapeF=dm/(dAr+dAl);
        
        return new float[] {shapeF,dm,dAr+dAl};
    }
    
    private float distancePoints(float X0,float Y0, float X1,float Y1){
        return (float)Math.sqrt(Math.pow(X0-X1,2)+Math.pow(Y0-Y1,2));
    }
    
    public int[][] getXYBasinDivide(){
        return xyContour;
    }
    
    public float[][] getLonLatBasinDivide(){
        return lonLatCountour;
    }
    
    public visad.RealTuple getOutletTuple() throws visad.VisADException, java.rmi.RemoteException{
        float[] LonLatBasin=new float[2];
        
        LonLatBasin[0]=(float) (xyBasin[0][0]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
        LonLatBasin[1]=(float) (xyBasin[1][0]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());

        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, LonLatBasin[0]),
                             new visad.Real(visad.RealType.Latitude,  LonLatBasin[1])};
        return new visad.RealTuple(rtd1);
    }
    
    public void findBasinDivide(byte[][] fullDirMatrix){
        
        //Relleno la matriz con 0's y 1's
        
        int[][] matrizBusqueda=new int[maxY-minY+5][maxX-minX+5];
        for (int j=0;j<xyBasin[0].length;j++){
            matrizBusqueda[xyBasin[1][j]-minY+2][xyBasin[0][j]-minX+2]=1;
        }
        
        //Determino la direccion inicial de busqueda
        
        int dirSalida=0;
        int outX=xyBasin[0][0]-minX+2;
        int outY=xyBasin[1][0]-minY+2;
        
//        System.out.println("outX: "+outX);
//        System.out.println("outY: "+outY);
//        System.out.println("matrizBusqueda[outY][outX]: "+matrizBusqueda[outY][outX]);
        
        if (matrizBusqueda[outY+0][outX+1]==0){
            dirSalida=180;
            outX=outX+1;
            outY=outY+0;
        } else  if (matrizBusqueda[outY+0][outX-1]==0){
                    dirSalida=0;
                    outX=outX-1;
                    outY=outY+0;
                } else  if (matrizBusqueda[outY+1][outX+0]==0){
                                dirSalida=270;
                                outX=outX+0;
                                outY=outY+1;
                        } else  if (matrizBusqueda[outY-1][outX+0]==0){
                                    dirSalida=90;
                                    outX=outX+0;
                                    outY=outY-1;
                                }
        
        int searchPointX=outX;
        int searchPointY=outY;
        
        java.util.Vector idsContorno=new java.util.Vector();
        
        int foreX;
        int foreY;
        
//        System.out.println("searchPointX: "+searchPointX);
//        System.out.println("searchPointY: "+searchPointY);
//        System.out.println("matrizBusqueda[searchPointY][searchPointX]: "+matrizBusqueda[searchPointY][searchPointX]);
//        
//        for(int i=0;i<matrizBusqueda.length;i++){
//            for(int j=0;j<matrizBusqueda[0].length;j++){
//                System.out.print(matrizBusqueda[i][j]+" ");
//            }
//            System.out.print("    ");
//            for(int j=0;j<matrizBusqueda[0].length;j++){
//                if(matrizBusqueda[i][j] == 1) 
//                    System.out.print("("+fullDirMatrix[i+minY-2][j+minX-2]+") ");
//                else
//                    System.out.print("["+fullDirMatrix[i+minY-2][j+minX-2]+"] ");
//            }
//            System.out.println();
//        }
//        System.exit(0);
        
        do{
            do{
                dirSalida+=90;
                foreX=searchPointX+(int) Math.round(Math.cos(dirSalida*Math.PI/180.0));
                foreY=searchPointY+(int) Math.round(Math.sin(dirSalida*Math.PI/180.0));
            } while(matrizBusqueda[foreY][foreX] == 1);
            matrizBusqueda[foreY][foreX]=8;
            
            for (int dirVec=-90;dirVec<=180;dirVec+=90){
                int lookX=foreX+(int) Math.round(Math.cos((dirSalida+dirVec)*Math.PI/180.0));
                int lookY=foreY+(int) Math.round(Math.sin((dirSalida+dirVec)*Math.PI/180.0));
                if (matrizBusqueda[lookY][lookX]==1){
                    idsContorno.add(new int[] {foreX+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec-270)-45)*Math.PI/360.0))),foreY+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec)-45)*Math.PI/360.0)))});
                } else break;
            }
            searchPointX=foreX;
            searchPointY=foreY;
            dirSalida-=180;
        } while(foreX!=outX || foreY!=outY);
        
        idsContorno.add(idsContorno.firstElement());
        
        lonLatCountour=new float[2][idsContorno.size()];
        xyContour=new int[2][idsContorno.size()];
        for (int k=0; k <idsContorno.size(); k++){
            int[] xys=(int[]) idsContorno.elementAt(k);
            xyContour[0][k]=xys[0]+minX-2;
            xyContour[1][k]=xys[1]+minY-2;
            lonLatCountour[0][k]=(float) ((xys[0]+minX-2)*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            lonLatCountour[1][k]=(float) ((xys[1]+minY-2)*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
        }
    }
    
    public int getMinX(){
        return minX;
    }
    
    public int getMaxX(){
        return maxX;
    }
    
    public int getMinY(){
        return minY;
    }
    
    public int getMaxY(){
        return maxY;
    }
    
    public float[] getElevations(hydroScalingAPI.io.MetaRaster mr) throws java.io.IOException{
        
        float[] elevationsBasin=new float[xyBasin[0].length];
        String currFormat=mr.getFormat();
        java.io.File currBinLoc=mr.getLocationBinaryFile();
        mr.restoreOriginalFormat();
        mr.setLocationBinaryFile(new java.io.File(currBinLoc.getAbsolutePath().subSequence(0, currBinLoc.getAbsolutePath().lastIndexOf("."))+".dem"));
        float[][] elevations=new hydroScalingAPI.io.DataRaster(mr).getFloat();
        minZ=Float.MAX_VALUE;
        maxZ=Float.MIN_VALUE;
        
        for (int k=0; k < xyBasin[0].length; k++){
            elevationsBasin[k]=elevations[xyBasin[1][k]][xyBasin[0][k]]; 
        }
        
        return elevationsBasin;
    
    }
    
    public float getRelief(hydroScalingAPI.io.MetaRaster mr) throws java.io.IOException{
        
        String currFormat=mr.getFormat();
        java.io.File currBinLoc=mr.getLocationBinaryFile();
        mr.restoreOriginalFormat();
        mr.setLocationBinaryFile(new java.io.File(currBinLoc.getAbsolutePath().subSequence(0, currBinLoc.getAbsolutePath().lastIndexOf("."))+".dem"));
        float[][] elevations=new hydroScalingAPI.io.DataRaster(mr).getFloat();
        minZ=Float.MAX_VALUE;
        maxZ=Float.MIN_VALUE;
        
        for (int k=0; k < xyBasin[0].length; k++){
            minZ=Math.min(minZ,elevations[xyBasin[1][k]][xyBasin[0][k]]); 
            maxZ=Math.max(maxZ,elevations[xyBasin[1][k]][xyBasin[0][k]]); 
        }
        
        return maxZ-minZ;
        
    }
    
    public float getRelief(float [][] elevations) throws java.io.IOException{
        
        minZ=Float.MAX_VALUE;
        maxZ=Float.MIN_VALUE;
        
        for (int k=0; k < xyBasin[0].length; k++){
            minZ=Math.min(minZ,elevations[xyBasin[1][k]][xyBasin[0][k]]); 
            maxZ=Math.max(maxZ,elevations[xyBasin[1][k]][xyBasin[0][k]]); 
        }
        
        return maxZ-minZ;
        
    }

    public float getMaxZ(hydroScalingAPI.io.MetaRaster mr) throws java.io.IOException{

        String currFormat=mr.getFormat();
        java.io.File currBinLoc=mr.getLocationBinaryFile();
        mr.restoreOriginalFormat();
        mr.setLocationBinaryFile(new java.io.File(currBinLoc.getAbsolutePath().subSequence(0, currBinLoc.getAbsolutePath().lastIndexOf("."))+".dem"));
        float[][] elevations=new hydroScalingAPI.io.DataRaster(mr).getFloat();
        maxZ=Float.MIN_VALUE;

        for (int k=0; k < xyBasin[0].length; k++){
            maxZ=Math.max(maxZ,elevations[xyBasin[1][k]][xyBasin[0][k]]);
        }

        return maxZ;

    }

    public float getMaxZ(float [][] elevations) throws java.io.IOException{

        maxZ=Float.MIN_VALUE;

        for (int k=0; k < xyBasin[0].length; k++){
            maxZ=Math.max(maxZ,elevations[xyBasin[1][k]][xyBasin[0][k]]);
        }

        return maxZ;

    }

    public float getMinZ(hydroScalingAPI.io.MetaRaster mr) throws java.io.IOException{

        String currFormat=mr.getFormat();
        java.io.File currBinLoc=mr.getLocationBinaryFile();
        mr.restoreOriginalFormat();
        mr.setLocationBinaryFile(new java.io.File(currBinLoc.getAbsolutePath().subSequence(0, currBinLoc.getAbsolutePath().lastIndexOf("."))+".dem"));
        float[][] elevations=new hydroScalingAPI.io.DataRaster(mr).getFloat();
        
        return elevations[xyBasin[1][0]][xyBasin[0][0]];
        
    }       
            
    public float getMinZ(float [][] elevations) throws java.io.IOException{

        return elevations[xyBasin[1][0]][xyBasin[0][0]];
        
    }   

    public int getOutletID(){
        return xyBasin[1][0]*localMetaRaster.getNumCols()+xyBasin[0][0];
    }
    
    public  byte[][] getBasinMask(){
        byte[][] basinMask = new byte[localMetaRaster.getNumRows()][localMetaRaster.getNumCols()];
        for (int i=0;i<xyBasin[0].length;i++){
            basinMask[xyBasin[1][i]][xyBasin[0][i]]=1;
        }
        return basinMask;
    }
    
    public  byte[][] getNetworkMask(){
        try{
            String pathToRasterNetwork=localMetaRaster.getLocationBinaryFile().getPath();
            pathToRasterNetwork=pathToRasterNetwork.subSequence(0, pathToRasterNetwork.lastIndexOf("."))+".redRas";
            localMetaRaster.setLocationBinaryFile(new java.io.File(pathToRasterNetwork));
            localMetaRaster.setFormat("Byte");

                byte [][] rasterNetwork=new hydroScalingAPI.io.DataRaster(localMetaRaster).getByte();

            byte[][] basinMask = new byte[maxY-minY+1][maxX-minX+1];
            for (int i=0;i<xyBasin[0].length;i++){
                if (rasterNetwork[xyBasin[1][i]][xyBasin[0][i]] == 1){
                    basinMask[xyBasin[1][i]-minY][xyBasin[0][i]-minX]=1;
                }
            }
            return basinMask;
        }  catch (java.io.IOException ioe){
            System.err.println("Failed loading raster network file. "+localMetaRaster);
            System.err.println(ioe);
        }
        return null;
    }
    
    public static void main (String args[]) {
        
        
        try{
            java.io.File theFile=new java.io.File("/home/ricardo/peaseRiver_database/peaseRiver.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/home/ricardo/peaseRiver_database/peaseRiver.dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            System.out.println("Extraction begins");
            java.util.Calendar iniTime=java.util.Calendar.getInstance();
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3154, 502,matDirs,metaModif);
            
            System.out.println(laCuenca.getDivideShapeFactor()[0]);
            System.exit(0);
            
            
            System.out.println("Relief: "+laCuenca.getRelief(metaModif));
            java.util.Calendar finalTime=java.util.Calendar.getInstance();
            System.out.println("Extraction ends");
            System.out.println(">>> Running Time is "+(finalTime.getTimeInMillis()-iniTime.getTimeInMillis())/1000.);
            /*byte[][] nm=laCuenca.getBasinMask();
            for(int i=0;i<nm.length;i++) {
                for(int j=0;j<nm.length;j++) System.out.print(nm[i][j]+"");
                System.out.println();
            }*/
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }

}
