package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class BilToHSJ {
    
    private java.io.File headerInputFile,geoInputFile,dataInputFile;
    
    public String[] parameters= { 
                                    "[Name]",
                                    "[Southernmost Latitude]",
                                    "[Westernmost Longitude]",
                                    "[Longitudinal Resolution (ArcSec)]",
                                    "[Latitudinal Resolution (ArcSec)]",
                                    "[# Columns]",
                                    "[# Rows]",
                                    "[Format]",
                                    "[Missing]",
                                    "[Temporal Resolution]",
                                    "[Units]",
                                    "[Information]"
                                };
    
    /** Creates a new instance of BillToHSJ */
    public BilToHSJ(java.io.File inputDirectory, java.io.File outputFile) throws java.io.IOException {
        
        if(!checkDirectoryContents(inputDirectory)) return;
        
        java.io.BufferedReader headerBuffer = new java.io.BufferedReader(new java.io.FileReader(headerInputFile));
        java.io.BufferedReader geoBuffer = new java.io.BufferedReader(new java.io.FileReader(geoInputFile));
        java.io.DataInputStream dataBuffer = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(dataInputFile)));
        
        int nCols,nRows,nBytes;
        
        headerBuffer.readLine();
        headerBuffer.readLine();

        nRows=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        nCols=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        headerBuffer.readLine();
        
        nBytes=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        
        headerBuffer.close();

        double minLat,minLon,latRes,lonRes;
        String minLatStr,minLonStr;

        latRes=Math.abs(Float.parseFloat(String.valueOf(geoBuffer.readLine().trim()))*3600.0f);
        geoBuffer.readLine();
        geoBuffer.readLine();
        lonRes=Math.abs(Float.parseFloat(String.valueOf(geoBuffer.readLine().trim()))*3600.0f);
        
        minLon=Double.parseDouble(String.valueOf(geoBuffer.readLine().trim()))+lonRes/3600.;
        minLat=Double.parseDouble(String.valueOf(geoBuffer.readLine().trim()))-nRows*latRes/3600.0;
        
        minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
        minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);
        
        geoBuffer.close();
        
        float[][] data=new float[nRows][nCols];
        
        if (nBytes == 16){
            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    //This is to swap to Little Endian
                    int low = dataBuffer.readByte() & 0xff;
                    int high = dataBuffer.readByte() & 0xff;
                    data[i][j] = (short)(high << 8 | low); 
                }
            }
        } 
        if (nBytes == 8){
            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    data[i][j] = (float)dataBuffer.readUnsignedByte(); 
                }
            }
        }
        
        dataBuffer.close();
        geoBuffer.close();
        headerBuffer.close();
        
        java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputFile));
        String rasterPath= outputFile.getPath().substring(0,outputFile.getPath().lastIndexOf(".metaDEM"))+".dem";
        java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(new java.io.File(rasterPath))));
        
        metaBuffer.write(parameters[0]+"\n");
        metaBuffer.write("DEM from The National Map Seamless Data Distribution System"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[1]+"\n");
        metaBuffer.write(minLatStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[2]+"\n");
        metaBuffer.write(minLonStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[3]+"\n");
        metaBuffer.write(lonRes+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[4]+"\n");
        metaBuffer.write(latRes+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[5]+"\n");
        metaBuffer.write(nCols+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[6]+"\n");
        metaBuffer.write(nRows+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[7]+"\n");
        metaBuffer.write("float"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[8]+"\n");
        metaBuffer.write("-9999"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[9]+"\n");
        metaBuffer.write("fix"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[10]+"\n");
        metaBuffer.write("m"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[11]+"\n");
        metaBuffer.write("This DEM comes from the USGS DEMs database."+"\n");
        metaBuffer.write("\n");
        
        metaBuffer.close();
        
        for (int i=0;i<nRows;i++){
            for (int j=0;j<nCols;j++){
                rasterBuffer.writeFloat(data[nRows-i-1][j]);
            }
        }
        
        rasterBuffer.close();
        
        
        
        
    }
    
    private boolean checkDirectoryContents(java.io.File inputDirectory){
        
        headerInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".HDR");
        geoInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".BLW");
        dataInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".BIL");
        
        if(headerInputFile.exists() && geoInputFile.exists() && dataInputFile.exists()) return true;
        
        headerInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".hdr");
        geoInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".blw");
        dataInputFile=new java.io.File(inputDirectory.getPath()+"/"+inputDirectory.getName()+".bil");
        
        if(headerInputFile.exists() && geoInputFile.exists() && dataInputFile.exists()) return true;
        
        return false;
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new hydroScalingAPI.io.BilToHSJ(new java.io.File("/home/furey/basin1/25941066"),new java.io.File("/tmp/25941066.metaDEM"));
        }catch(java.io.IOException ioe){
            System.err.println("error");
            ioe.printStackTrace();
        }
        
    }

}
