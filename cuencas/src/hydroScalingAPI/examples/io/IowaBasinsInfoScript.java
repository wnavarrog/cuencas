/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

/**
 *
 * @author Ricardo Mantilla
 */
import java.io.*;
import java.util.zip.*;
import java.net.*;

public class IowaBasinsInfoScript{

    java.io.FileInputStream dataPath;
    java.io.BufferedInputStream dataBuffer;
    java.io.DataInputStream dataDataStream;

    float[][] matrix = new float[1057][1741];

    int numCol_Rain=1741;
    int numRow_Rain=1057;

    double minLon_Rain=-97.154167;
    double minLat_Rain=40.133331;
    double matRes_Rain=0.004167;

    int numCol_DEM=7000;
    int numRow_DEM=4645;

    double minLon_DEM=-97.57256944;
    double minLat_DEM=39.67363889;
    double matRes_DEM=0.001179728;

    java.io.File fileSalida;
    java.io.FileOutputStream        outputDir;
    java.io.OutputStreamWriter      newfile;
    java.io.DataOutputStream        newOutputStream;
    java.io.BufferedOutputStream    bufferout;
    String                          ret="\n";

    java.io.File dirOut=new java.io.File("/Users/ricardo/rawData/RainfallAccumulations");

    String KMLsPath, OutputPath;


    public IowaBasinsInfoScript(int action) throws IOException{
        switch (action){
            case 0:Reset(); break;
            case 1:ReloadRealTimeRainfall(); break;
        }

    }

    public IowaBasinsInfoScript(String KMLsPath1,String OutputPath1) throws IOException {
        KMLsPath=KMLsPath1;
        OutputPath=OutputPath1;

        RecreateKMLs();
        System.out.println(">> Waiting for next real-time rainfall field");
        
        System.out.println(">> Process Completed");
        
    }

    public void Reset() throws java.io.IOException{

        System.out.println(">> Reseting Rainfall Accumulation Files");

        System.out.println(">> Working on File # 1");

        outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum1");
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
            if(matrix[i][j] != -99.0) {
                newOutputStream.writeFloat(matrix[i][j]);
            } else{
                newOutputStream.writeFloat(0);
            }
        }

        newOutputStream.close();
        bufferout.close();
        outputDir.close();

        for(int k=2;k<=500;k++){

            System.out.println(">> Working on File # "+k);

            java.nio.channels.FileChannel inChannel = new FileInputStream(new java.io.File(dirOut.getPath()+"/Current/accum1")).getChannel();
            java.nio.channels.FileChannel outChannel = new FileOutputStream(new java.io.File(dirOut.getPath()+"/Current/accum"+k)).getChannel();
            
            inChannel.transferTo(0, inChannel.size(),outChannel);

            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
            
            
        }

        System.out.println(">> Rainfall Accumulation Files Ready");

    }

    public void ReloadRealTimeRainfall() throws IOException {

        try{
            
            URLConnection urlConn = null;
            URL file;
            GZIPInputStream gzis;
            InputStreamReader xover;
            BufferedReader is;
            String line;

            file = new URL("http://s-iihr52.iihr.uiowa.edu/ricardo/quality/60/latest.txt");
            urlConn = file.openConnection();


            xover = new InputStreamReader(urlConn.getInputStream());
            is = new BufferedReader(xover);

            String mostRecentFile = is.readLine();
            
            is.close();
            xover.close();

            System.out.println(">> Opening connection: "+"http://s-iihr52.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
            file = new URL("http://s-iihr52.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
            urlConn = file.openConnection();


            xover = new InputStreamReader(urlConn.getInputStream());
            is = new BufferedReader(xover);

            is.readLine();//# file name: H99999999_R6003_G_31MAR2010_221000.out
            is.readLine();//# Accumulation map [mm]
            is.readLine();//# Accumulation time [sec]: 3300
            is.readLine();//# number of columns: 1741
            is.readLine();//# number of rows: 1057
            is.readLine();//# grid: LATLON
            is.readLine();//# upper-left LATLONcorner(x,y): 6924 5409
            is.readLine();//# xllcorner [lon]: -97.154167
            is.readLine();//# yllcorner [lat]: 40.133331
            is.readLine();//# cellsize [dec deg]: 0.004167
            is.readLine();//# no data value: -99.0

            System.out.println(">> Reading Real-Time Rainfall Data");

            matrix = new float[numRow_Rain][numCol_Rain];
            
            for (int i = 0; i < numRow_Rain; i++) {

                line = is.readLine().trim();
                java.util.StringTokenizer linarray = new java.util.StringTokenizer(line);


                for (int j = 0; j < numCol_Rain; j++) {

                    float f = 0;
                    try {
                        f = Float.valueOf(linarray.nextToken()).floatValue();
                    } catch (NumberFormatException nfe) {
                        System.out.println("NFE" + nfe.getMessage());
                    }

                    if(f != -99.0) matrix[i][j] = f;

                }
            }

            is.close();
            xover.close();
            
            System.out.println(">> Matrix Read!");

            System.out.println(">> Updating Accumulations Files");

            float[][] dataFloat=new float[numRow_Rain][numCol_Rain];

            for (int k=500;k>1;k--){

                System.out.println(">> Working on File # "+k);

                dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+(k-1));
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
                    dataFloat[i][j]=dataDataStream.readFloat();
                }


                dataBuffer.close();
                dataDataStream.close();

                outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum"+k);
                bufferout=new BufferedOutputStream(outputDir);
                newOutputStream=new DataOutputStream(bufferout);

                for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
                    newOutputStream.writeFloat(dataFloat[i][j]+matrix[i][j]);
                }

                newOutputStream.close();
                bufferout.close();
                outputDir.close();

            }

             System.out.println(">> Working on File # 1");

            outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum1");
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
               newOutputStream.writeFloat(matrix[i][j]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            System.out.println(">> Accumulations Files Update Completed");

        }
        catch(MalformedURLException e){
           e.printStackTrace();
        }

    }

    public void RecreateKMLs() throws IOException {

        System.out.println(">> Recreating KMLs");


        new File(OutputPath).mkdirs();
        new File(OutputPath+"/polygons/").mkdirs();
        new File(OutputPath+"/networks/").mkdirs();

        File dir = new File(KMLsPath);

        File[] files = dir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
        File[] inCity1,inCity2,inCity3,inCity4;

        String tableRadek="City Name;River Name;Latitude;Longitude;File Polygon;File Network;Westernmost Longitude;Southernmost Longitude;Easternmost Longitude;Northernmost Latitude"+ret;
        
        for (int i = 0; i < files.length; i++) {
        

            inCity1 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("Basin","txt.gz"));
            inCity2 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("Divide","kml"));
            inCity3 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("RiverNetworkLowRes","kml"));
            inCity4 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("InfoFile","txt"));

            for (int j = 0; j < inCity1.length; j++) {

                FileInputStream fin;
                GZIPInputStream gzis;
                InputStreamReader xover;
                BufferedReader is;

                fin = new FileInputStream(inCity4[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);

                is.readLine();

                double latitude=hydroScalingAPI.tools.DMSToDegrees.getDegrees(is.readLine().split(">")[2].trim());
                double longitude=hydroScalingAPI.tools.DMSToDegrees.getDegrees(is.readLine().split(">")[2].trim());

                is.readLine();
                is.readLine();

                double responseTime=Double.parseDouble(is.readLine().split(">")[2].trim());

                is.close();
                xover.close();
                fin.close();

                long fileIndex=Math.max(1L,Math.round(responseTime));

                System.out.println("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);

                dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int ii=0;ii<numRow_Rain;ii++) for(int jj=0;jj<numCol_Rain;jj++) {
                    matrix[ii][jj]=dataDataStream.readFloat();
                    //matrix[ii][jj] = (float)Math.random()+(float)ii/(float)numRow_Rain*10.0f;
                }


                dataBuffer.close();
                dataDataStream.close();

                float averageValue = 0.0f;
                float numElements = 0.0f;

                //Open the File for reading

                fin = new FileInputStream(inCity1[j]);
                gzis = new GZIPInputStream(fin);
                xover = new InputStreamReader(gzis);
                is = new BufferedReader(xover);

                String line2;
                String line3;
                String linarray2[] = null;

                line3 = is.readLine();

                float minX=Float.MAX_VALUE;
                float minY=Float.MAX_VALUE;
                float maxX=-Float.MAX_VALUE;
                float maxY=-Float.MAX_VALUE;

                while ((line2 = is.readLine()) != null) {
                    linarray2 = line2.split(",");

                    float xLon=Float.parseFloat(linarray2[0].trim());
                    float yLat=Float.parseFloat(linarray2[1].trim());

                    int xxx = (int)((xLon-minLon_Rain)/matRes_Rain);
                    int yyy = (int)((yLat-minLat_Rain)/matRes_Rain);

                    averageValue += matrix[yyy][xxx];
                    numElements++;

                    minX=Math.min(minX, xLon);
                    minY=Math.min(minY, yLat);
                    maxX=Math.max(maxX, xLon);
                    maxY=Math.max(maxY, yLat);

                }

                averageValue/=numElements;

                averageValue*=100;
                averageValue=Math.round(averageValue)/100.0f;

                System.out.println(line3 + " " + averageValue);

                is.close();
                xover.close();
                gzis.close();
                fin.close();

                String[] uniqueIdentifier=inCity1[j].getName().split("_");
                String[] cityName=uniqueIdentifier[1].split(" \\(");
                String[] riverName=cityName[1].split("\\)");

                String webSafeName=cityName[0].replaceAll(" ", "_").toLowerCase()+"_"+riverName[0].replaceAll(" ", "_").replaceAll(",","").toLowerCase();

                fin = new FileInputStream(inCity2[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);

                fileSalida=new java.io.File(OutputPath+"/polygons/"+webSafeName+".kmz");
                String polyWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data/"+new java.io.File(OutputPath).getName()+"/polygons/"+webSafeName+".kmz";

                outputDir = new java.io.FileOutputStream(fileSalida);
                java.util.zip.ZipOutputStream outputComprim=new java.util.zip.ZipOutputStream(outputDir);
                bufferout=new java.io.BufferedOutputStream(outputComprim);

                outputComprim.putNextEntry(new ZipEntry(webSafeName+".kml"));

                while ((line2 = is.readLine()) != null) {

                    if(line2.equalsIgnoreCase("<PolyStyle><color>00000000</color></PolyStyle></Style>")){

                        //Green
                        if(averageValue > 0.00) line2="<PolyStyle><color>be22c122</color></PolyStyle></Style>";
                        //Orange
                        if(averageValue > 5.00) line2="<PolyStyle><color>be0062ff</color></PolyStyle></Style>";
                        //Red
                        if(averageValue > 7.00) line2="<PolyStyle><color>be2023ff</color></PolyStyle></Style>";
                        
                    }

                    if(line2.startsWith("<Placemark><description>")){
                        int indOfString=line2.indexOf("]]></description>");
                        line2=line2.substring(0, indOfString)+"<br><br><b>Rain Rate</b><br>"+averageValue+" mm"+"]]></description>";
                    }

                    bufferout.write(line2.getBytes());

                }

                bufferout.close();
                outputComprim.close();
                outputDir.close();

                is.close();
                xover.close();
                fin.close();

                fin = new FileInputStream(inCity3[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);


                fileSalida=new java.io.File(OutputPath+"/networks/"+webSafeName+".kmz");
                String netWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data/"+new java.io.File(OutputPath).getName()+"/networks/"+webSafeName+".kmz";

                outputDir = new java.io.FileOutputStream(fileSalida);
                outputComprim=new java.util.zip.ZipOutputStream(outputDir);
                bufferout=new java.io.BufferedOutputStream(outputComprim);

                outputComprim.putNextEntry(new ZipEntry(webSafeName+".kml"));

                while ((line2 = is.readLine()) != null) {

                    bufferout.write(line2.getBytes());

                }

                bufferout.close();
                outputComprim.close();
                outputDir.close();

                is.close();
                xover.close();
                fin.close();

                tableRadek+=cityName[0]+";"+riverName[0]+";"+latitude+";"+longitude+";"+polyWebAddress+";"+netWebAddress+";"+minX+";"+minY+";"+maxX+";"+maxY+ret;

                
            }

        }

        System.out.println(">> KMLs updated");

        System.out.println(">> Updating Description Table");

        fileSalida=new java.io.File(OutputPath+"/description.txt");
        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        newfile.write(tableRadek);

        newfile.close();
        bufferout.close();
        outputDir.close();

        System.out.println(">> Current Update Completed");
    }

    public static void main(String[] args) throws IOException {

        String[] command;
        java.lang.Process localProcess0,localProcess1,localProcess2;
        
        if(args.length == 0) {

//            command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
//                                    "-Xmx1500m",
//                                    "-Xrs",
//                                    "-cp",
//                                    System.getProperty("java.class.path"),
//                                    "hydroScalingAPI.examples.io.IowaBasinsInfoScript",
//                                    "reload-rain"};
//            localProcess0=java.lang.Runtime.getRuntime().exec(command);
//
//            String concat0="";
//
//            boolean monitor0=true;
//
//            while(monitor0){
//                String s1=new String(new byte[] {Byte.parseByte(""+localProcess0.getInputStream().read())});
//                concat0+=s1;
//                if(s1.equalsIgnoreCase("\n")) {
//                    System.out.print("Processor 0: "+concat0);
//                    if(concat0.substring(0, Math.min(39,concat0.length())).equalsIgnoreCase(">> Accumulations Files Update Completed")) monitor0=false;
//                    concat0="";
//                }
//            }

            command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                    "-Xmx1500m",
                                    "-Xrs",
                                    "-cp",
                                    System.getProperty("java.class.path"),
                                    "hydroScalingAPI.examples.io.IowaBasinsInfoScript",
                                    "/Users/ricardo/rawData/BasinMasks/usgs_gauges/",
                                    "/Volumes/ricardo/temp/iowa_basins_data/usgs_gauges/"};
            localProcess1=java.lang.Runtime.getRuntime().exec(command);

            command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                    "-Xmx1500m",
                                    "-Xrs",
                                    "-cp",
                                    System.getProperty("java.class.path"),
                                    "hydroScalingAPI.examples.io.IowaBasinsInfoScript",
                                    "/Users/ricardo/rawData/BasinMasks/large_cities/",
                                    "/Volumes/ricardo/temp/iowa_basins_data/large_cities/"};
            localProcess2=java.lang.Runtime.getRuntime().exec(command);

            String concat1="";
            String concat2="";

            boolean monitor1=true,monitor2=true;

            while(monitor1 || monitor2){
                String s1=new String(new byte[] {Byte.parseByte(""+localProcess1.getInputStream().read())});
                concat1+=s1;
                if(s1.equalsIgnoreCase("\n") && monitor1) {
                    System.out.print("Processor 1: "+concat1);
                    if(concat1.substring(0, Math.min(20,concat1.length())).equalsIgnoreCase(">> Process Completed")) monitor1=false;
                    concat1="";
                }
                String s2=new String(new byte[] {Byte.parseByte(""+localProcess2.getInputStream().read())});
                concat2+=s2;
                if(s2.equalsIgnoreCase("\n") && monitor2) {
                    System.out.print("Processor 2: "+concat2);
                    if(concat2.substring(0, Math.min(20,concat2.length())).equalsIgnoreCase(">> Process Completed")) monitor2=false;
                    concat2="";
                }
            }
            System.exit(0);

        } else {

            if(args[0].equalsIgnoreCase("reset")){
                new IowaBasinsInfoScript(0);
                System.exit(0);
            }

            if(args[0].equalsIgnoreCase("reload-rain")){
                new IowaBasinsInfoScript(1);
                System.exit(0);
            }
            new IowaBasinsInfoScript(args[0],args[1]);
            System.exit(0);
        }

    }

}
