/*
 * HillslopeRainAndNetworkToFile.java
 *
 * Created on December 21, 2007, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class HillslopeRainAndNetworkToFile {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    /** Creates a new instance of HillslopeRainAndNetworkToFile */
    public HillslopeRainAndNetworkToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
        
        String Directory="/tmp/";
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        java.io.File theFile=new java.io.File(Directory+demName+"-"+storm.stormName()+".dat");
        System.out.println(theFile);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
        
        newfile.writeInt(linksStructure.contactsArray.length);
        
        System.out.println("Writing Total Hillslope Areas");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        
        System.out.println("Writing Link Magnitude");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(linksStructure.magnitudeArray[i]);
        }
        
        System.out.println("Writing Distance to Outlet");
        float[][] dToOutlet=linksStructure.getDistancesToOutlet();
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(dToOutlet[0][i]);
        }
        
        System.out.println("Writing Precipitations");
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        newfile.writeInt(numPeriods);
        
        java.util.Date startTime1=new java.util.Date();
        for (int k=0;k<numPeriods;k++) {
            System.out.println("Initiating time step "+k);
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            java.util.Date startTime=new java.util.Date();
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));
            }
            java.util.Date endTime=new java.util.Date();
            System.out.println("Time Getting "+linksStructure.contactsArray.length+" records :"+((endTime.getTime()-startTime.getTime()))+" milliseconds");
            System.out.println("Finishig up time step "+k);
        }
        java.util.Date endTime1=new java.util.Date();
        System.out.println("Time Getting "+(linksStructure.contactsArray.length*numPeriods)+" records :"+((endTime1.getTime()-startTime1.getTime()))+" milliseconds");
        
        System.out.println("Done Writing Precipitations");

        newfile.close();
        bufferout.close();
        
        System.out.println("File Completed");
    }
    
    public static void main(String args[]) {
        //main0(args); //The mogollon test case
        //main1(args); //Whitewater 15-minute Rain
        main2(args); //Nexrad Whitewater
    }
    
    public static void main0(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Hydrology/NexradPrecipitation/wholeSummer2003/nexrad_prec.metaVHC");

            new HillslopeRainAndNetworkToFile(282, 298,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void main1(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/observed_events/EventLuc/prec.metaVHC");

        
            new HillslopeRainAndNetworkToFile(1063,496,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void main2(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/observed_events/EventLuc/prec.metaVHC");

        
            new HillslopeRainAndNetworkToFile(1063,496,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }

}
