/*
 * MetaNetwork.java
 *
 * Created on July 10, 2003, 12:16 PM
 */

package hydroScalingAPI.io;

/**
 *
 * @author  ricardo
 */
public class MetaNetwork {
    
    private hydroScalingAPI.io.MetaRaster metaData;
    
    private int[][] streamRecord,linkRecord;
    private int[] pointRecord;
    
    private int largestOrder;
    
    /** Creates a new instance of MetaNetwork */
    public MetaNetwork(hydroScalingAPI.io.MetaRaster md)  throws java.io.IOException{
        metaData=md;
        
        String pathToStreams=metaData.getLocationBinaryFile().getPath();
        pathToStreams=pathToStreams.substring(0,pathToStreams.lastIndexOf("."))+".stream";
        java.io.File fileStream=new java.io.File(pathToStreams);
        String pathToLink=metaData.getLocationBinaryFile().getPath();
        pathToLink=pathToLink.substring(0,pathToLink.lastIndexOf("."))+".link";
        java.io.File fileLink =new java.io.File(pathToLink);
        String pathToPoint=metaData.getLocationBinaryFile().getPath();
        pathToPoint=pathToPoint.substring(0,pathToPoint.lastIndexOf("."))+".point";
        java.io.File filePoint=new java.io.File(pathToPoint);

        try{
            java.io.FileInputStream pipeToStream=new java.io.FileInputStream(fileStream);
            java.io.BufferedInputStream bufferStream=new java.io.BufferedInputStream(pipeToStream);
            java.io.DataInputStream dataStream=new java.io.DataInputStream(bufferStream);
            
            int totalNumStreams=bufferStream.available()/4/6;
            streamRecord =new int[totalNumStreams][6];
            for (int i=0;i<totalNumStreams;i++){
                //A stream record contains: Head_ID,             Contact_ID,          Tail_ID,             Order,               Ini_link,            Num_link
                streamRecord[i]=new int[] { dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt()};
                largestOrder=Math.max(streamRecord[i][3],largestOrder);
            }
            dataStream.close();
            bufferStream.close();
            pipeToStream.close();

            java.io.FileInputStream pipeToLink=new java.io.FileInputStream(fileLink);
            java.io.BufferedInputStream bufferLink=new java.io.BufferedInputStream(pipeToLink);
            java.io.DataInputStream dataLink=new java.io.DataInputStream(bufferLink);
            
            int totalNumLinks=bufferLink.available()/4/6;
            linkRecord =new int[totalNumLinks][6];
            for (int i=0;i<totalNumLinks;i++){
                //A link record contains:   Magnitude,         Head_ID,           Contact_ID,       Tail_ID,            Ini_point,         Num_point
                linkRecord[i]=new int[] {   dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt()};
            }
            dataLink.close();
            bufferLink.close();
            pipeToLink.close();
            
            java.io.FileInputStream pipeToPoint=new java.io.FileInputStream(filePoint);
            java.io.BufferedInputStream bufferPoint=new java.io.BufferedInputStream(pipeToPoint);
            java.io.DataInputStream dataPoint=new java.io.DataInputStream(bufferPoint);
            
            int totalNumPoints=bufferPoint.available()/4;
            pointRecord =new int[totalNumPoints];
            for (int i=0;i<totalNumPoints;i++){
                //A link point contains:  Point_ID
                pointRecord[i]=dataPoint.readInt();
            }
            
            dataPoint.close();
            bufferPoint.close();
            pipeToPoint.close();
            
        }catch(java.io.IOException IOE){
            System.err.println("Failed loading streams file. "+fileStream);
            System.err.println(IOE);
        }
        
    }
    
    public int getLargestOrder(){
        return largestOrder;
    }
    
    public int[][] getStreamRecord(){
        return streamRecord;
    }
    
    public int[][] getLinkRecord(){
        return linkRecord;
    }
    
    public int[] getPointRecord(){
        return pointRecord;
    }
    
    public visad.UnionSet getUnionSet(int orderRequested) throws visad.VisADException{
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                java.util.Vector oneStream=new java.util.Vector();
                for (int j=0;j<numLink;j++){
                    int iniPoint=linkRecord[iniLink+j][4]/4;
                    int numPoint=linkRecord[iniLink+j][5];
                    for (int k=0;k<numPoint;k++){
                        
                        float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                        float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
                        
                        oneStream.add(new float[] {lon,lat});
                    }
                }
                allStreams.add(oneStream);
            }
        }
        
        visad.Gridded2DSet[] griddedStreams=new visad.Gridded2DSet[allStreams.size()];
        for (int i=0;i<griddedStreams.length;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                riverPath[0][j]=riverNode[0];
                riverPath[1][j]=riverNode[1];
            }
            griddedStreams[i]=new visad.Gridded2DSet(domain,riverPath,riverPath[0].length);
        }
        
        return new visad.UnionSet(domain,griddedStreams);
    }
    
    public visad.UnionSet getUnionSet(int orderRequested, byte[][] basinMask) throws visad.VisADException{
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                
                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];
                
                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }
        
        visad.Gridded2DSet[] griddedStreams=new visad.Gridded2DSet[allStreams.size()];
        for (int i=0;i<griddedStreams.length;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                riverPath[0][j]=riverNode[0];
                riverPath[1][j]=riverNode[1];
            }
            griddedStreams[i]=new visad.Gridded2DSet(domain,riverPath,riverPath[0].length);
        }
        
        return new visad.UnionSet(domain,griddedStreams);
    }
    
    public void printXYsFile(int orderRequested) throws java.io.IOException{
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                java.util.Vector oneStream=new java.util.Vector();
                for (int j=0;j<numLink;j++){
                    int iniPoint=linkRecord[iniLink+j][4]/4;
                    int numPoint=linkRecord[iniLink+j][5];
                    for (int k=0;k<numPoint;k++){
                        
                        float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                        float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
                        
                        oneStream.add(new float[] {lon,lat});
                    }
                }
                allStreams.add(oneStream);
            }
        }
        
        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            System.out.println();
            System.out.println("Stream "+(orderRequested*1e4+i+1));
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                System.out.println(riverNode[0]+" "+riverNode[1]);
            }
        }
        
    }
    
    public void printXYsFile(int orderRequested, byte[][] basinMask) throws java.io.IOException{
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                
                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];
                
                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }
        
        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            System.out.println();
            System.out.println("Stream "+(orderRequested*1e4+i+1));
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                System.out.println(riverNode[0]+" "+riverNode[1]);
            }
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dir"));
        
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3187,1490,matDirs,metaModif);
            
            byte[][] basMask=laCuenca.getBasinMask();

            MetaNetwork test=new MetaNetwork(metaModif);

            test.printXYsFile(Integer.parseInt(args[0]),basMask);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }/* catch (visad.VisADException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }*/
        
    }
    
}
