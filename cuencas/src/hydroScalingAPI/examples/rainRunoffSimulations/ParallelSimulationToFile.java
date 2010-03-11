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


/*
 * ParallelSimulationToFile.java
 *
 * Created on January 27, 2009, 8:00 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import java.util.Iterator;
import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class ParallelSimulationToFile extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    private int simulProcess=2;
    public int threadsRunning=0;
    java.util.Hashtable<String,Boolean> compNodeNames=new java.util.Hashtable<String,Boolean>();

    private java.util.Calendar zeroSimulationTime;
    
    /** Creates new ParallelSimulationToFile */
    public ParallelSimulationToFile(){
        
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster,float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{

        zeroSimulationTime=zST;
        matDir=direcc;
        metaDatos=md;

        float lam1=((Float)routingParams.get("lambda1")).floatValue();
        float lam2=((Float)routingParams.get("lambda2")).floatValue();

        float v_o=((Float)routingParams.get("v_o")).floatValue();

        compNodeNames=MyCnodes;
        simulProcess=numNodes;

        //Fractioning
        
        java.util.Hashtable<String,Integer> processList=new java.util.Hashtable();
        java.util.Hashtable<Integer,Integer> topoMapping=new java.util.Hashtable();
        
        hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaDatos, matDir);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen=new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(mylinksAnalysis);
        
        float[][] linkAreas=mylinksAnalysis.getVarValues(0);
        float[][] linkLenghts=mylinksAnalysis.getVarValues(1);
        
        int decompScale=dscale;
        
        int[][] headsTails=myRsnGen.getHeadsAndTails(decompScale);
        int[][] connectionTopology=myRsnGen.getPrunedConnectionStructure(decompScale);
        int[] connectionLinks=myRsnGen.getConnectingLinks(decompScale);
        float[][] dToO=mylinksAnalysis.getDistancesToOutlet();
        
        int ncols=md.getNumCols();
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(9);

        for(int i=0;i<connectionTopology.length;i++) {
            processList.put(labelFormat.format(dToO[0][connectionLinks[i]])+"-"+labelFormat.format(i),i);
            topoMapping.put(connectionLinks[i],i);
        }
        
        // Sort hashtable.
        java.util.Vector v = new java.util.Vector(processList.keySet());
        java.util.Collections.sort(v);
        java.util.Collections.reverse(v);
        
        for (Iterator it = v.iterator(); it.hasNext();) {
            String key = (String)it.next();
            Integer val = (Integer)processList.get(key);
            System.out.println("Key: " + key + "     Val: " + val);
        }

        Thread[] activeThreads=new Thread[connectionTopology.length];
        hydroScalingAPI.examples.rainRunoffSimulations.ExternalTileToFile[] externalExecutors=new hydroScalingAPI.examples.rainRunoffSimulations.ExternalTileToFile[connectionTopology.length];

        float maxDtoO=Float.parseFloat(((String)v.get(0)).substring(0, 9));

        for(int i=0;i<headsTails[0].length;i++){
            int xOutlet=headsTails[0][i]%ncols;
            int yOutlet=headsTails[0][i]/ncols;

            int xSource=headsTails[2][i]%ncols;
            int ySource=headsTails[2][i]/ncols;
            
            if(headsTails[3][i] == 0){
                 xSource=-1;
                 ySource=-1;
            }
            
            System.out.println("Process "+i+" "+xOutlet+" "+yOutlet+" "+xSource+" "+ySource);
            System.out.println(java.util.Arrays.toString(connectionTopology[i]));

            String connectionString="C";
            String correctionString="F";

            
            int[] connectionIDs=new int[connectionTopology[i].length];
            
            for (int j = 0; j < connectionTopology[i].length; j++) {
                System.out.print(topoMapping.get(connectionTopology[i][j])+" ");
                connectionIDs[j]=headsTails[0][topoMapping.get(connectionTopology[i][j])];
                connectionString+=","+connectionIDs[j];
            }
            System.out.println();
            
            //Setting up threads
            
            float[] corrections=new float[2];
            
            int connectingLink=mylinksAnalysis.getLinkIDbyHead(xSource, ySource);
            if(connectingLink != -1){
                corrections[0]=linkAreas[0][connectingLink];
                corrections[1]=linkLenghts[0][connectingLink];
                correctionString+=","+corrections[0]+","+corrections[1];
            }
            System.out.println(java.util.Arrays.toString(connectionIDs));
            System.out.println(java.util.Arrays.toString(corrections));

            externalExecutors[i]=new hydroScalingAPI.examples.rainRunoffSimulations.ExternalTileToFile("Element "+i,md.getLocationMeta().getAbsolutePath(),xOutlet, yOutlet,xSource, ySource,decompScale,routingType,lam1,lam2,v_o,stormFile.getAbsolutePath(),infiltRate,outputDirectory.getAbsolutePath(),connectionString,correctionString,this,zeroSimulationTime.getTimeInMillis());
        }
        
        boolean allNodesDone=true;

        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Parallel Code Begins");

        float currentDtoO=maxDtoO;
        
        do{
            System.out.print(">> Processes on Hold: ");

            for (int i = 0; i < externalExecutors.length; i++) {
                int indexProc=processList.get((String)v.get(i));

                float thisDtoO=Float.parseFloat(((String)v.get(i)).substring(0, 9));
                
                if(externalExecutors[indexProc].completed == false && externalExecutors[indexProc].executing == false){
                    boolean required=true;
                    for (int j = 0; j < connectionTopology[indexProc].length; j++) {
                        required&=externalExecutors[((Integer)topoMapping.get(connectionTopology[indexProc][j])).intValue()].completed;
                    }
                    if (required) {
                        
                        //simThreads[indexProc].executeSimulation();

                        String cn=findFreeNode();

                        System.out.println();
                        System.out.println(">> Process "+indexProc+" is being launched");
                        externalExecutors[indexProc].setComputingNode(cn,indexProc);
                        activeThreads[indexProc] = new Thread(externalExecutors[indexProc]);
                        externalExecutors[indexProc].executing=true;
                        compNodeNames.put(cn, true);

                        threadsRunning++;
                        activeThreads[indexProc].start();
                        currentDtoO=Math.min(currentDtoO, thisDtoO);
                        if(threadsRunning == simulProcess) break;
                    } else {
                        System.out.print(indexProc+",");
                    }
                } else if(externalExecutors[indexProc].completed == true) activeThreads[indexProc]=null;
            }
            System.out.println();
            int counterSeconds=0;
            while(threadsRunning == simulProcess){
                System.out.println(">>>>>  CURRENTLY RUNNING "+threadsRunning+" THREADS. Out of "+simulProcess+".Percentage Completed: "+((1-currentDtoO/maxDtoO)*100)+"%");
                new visad.util.Delay(1000);
            }

            //System.exit(0);

            System.out.println(">>>>>  CURRENTLY RUNNING "+threadsRunning+" THREADS. Out of "+simulProcess+". Percentage Completed: "+((1-currentDtoO/maxDtoO)*100)+"%");

            System.out.print(">> Running");
            for (int i = 0; i < externalExecutors.length; i++){
                if(externalExecutors[i].executing) {
                    System.out.print(", "+i);
                }
            }
            System.out.println();

            new visad.util.Delay(1000);
            
            allNodesDone=true;
            for (int i = 0; i < externalExecutors.length; i++) allNodesDone&=externalExecutors[i].completed;
            
        } while(!allNodesDone);
        
        System.out.println("Parallel Code Ends");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
    }

    private String findFreeNode(){
        for (Iterator it = compNodeNames.keySet().iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            Boolean nodeState=(Boolean)compNodeNames.get(keyName);
            if(!nodeState.booleanValue()) return keyName;
        }
        return "NA";
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try{
            //subMain_1(args);  //Using Walnut Gulch, AZ
            //subMain0(args);  //Using AveragedIowaRiver
            //subMain1(args);  //Using 30m DEMs
            //subMain3(args);
            subMainLUCIANA_IOWA3(args);
            subMainLUCIANA_CEDAR3(args);
            subMainLUCIANA_IOWA1(args);
            subMainLUCIANA_CEDAR1(args);
            //subMain4(args);
            //subMain5(args);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }

    public static void subMain_1(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();
        for (int j = 0; j <= 7; j++) {
            myNodeNames.put("localhost"+"-"+j, false);
        }

        int numNodes=myNodeNames.size();

        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        int routingType=Integer.parseInt("5");

        routingParams.put("v_o",0.5f);
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(1971,7, 18, 20, 00, 0);

        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_02/precipitation_interpolated_ev02.metaVHC");
        java.io.File outputDirectory=new java.io.File("/Users/ricardo/simulationResults/Parallel/WalnutGulch/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile(194, 281,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,routingType,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
    }

    public static void subMain0(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 42; i <= 60; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);

        routingParams.put("v_o",0.5f);

        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(2008,4, 29, 00, 00, 0);

        java.io.File outputDirectory;

//        for (float v0 = 0.75f; v0 <= 0.75f; v0+=0.25f) {
//
//            routingParams.put("v_o",v0);
//
//            for (float infil = 0.0f; infil <= 0.0f; infil+=5.0f) {
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/AveragedIowaRiver_"+v0+"_"+infil+"/");
//                outputDirectory.mkdirs();
//
//                new ParallelSimulationToFile(2734, 1069 ,matDirs,magnitudes,horOrders,metaModif,stormFile,infil,2,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
//            }
//
//        }
        
        float v0=0.40f;
        routingParams.put("v_o",v0);
        float lam1 = 0.2f;

        //for (float lam1 = 0.2f; lam1 <= 0.5f; lam1+=0.1f) {
            for (float lam2 = -0.1f; lam2 <= -0.1f; lam2+=0.05f) {

                routingParams.put("lambda1",lam1);
                routingParams.put("lambda2",lam2);

//                stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/AveragedEventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"_AveragedRain_to26/");
//                outputDirectory.mkdirs();
//                //new ParallelSimulationToFile(1768,1987,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
//                //new ParallelSimulationToFile(1637,1955,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
//                new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);

                //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
                //outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/Wapello/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"to26/");
                //outputDirectory.mkdirs();
                //new ParallelSimulationToFile(1768,1987,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
                //new ParallelSimulationToFile(1637,1955,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
                //new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,5);
                //new ParallelSimulationToFile(3316,116,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/ClearCreek/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
//                outputDirectory.mkdirs();
//                new ParallelSimulationToFile(2817,713,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
                //new ParallelSimulationToFile(2817,713,matDirs,magnitudes,horOrders,metaModif,stormFile,10.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);
                //new ParallelSimulationToFile(2817,713,matDirs,magnitudes,horOrders,metaModif,stormFile,5.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames);

                //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/15_arcmin/prec.metaVHC");
//                stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/ResampledNexrad/Radar_15_180min/H00070802_R1504_G_.metaVHC");
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids_Res/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
//                outputDirectory.mkdirs();
//                new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,5);

//                stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneWRF/1-Hour/precWRF.metaVHC");
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids_WRF/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
//                outputDirectory.mkdirs();
//                zeroSimulationTime.set(2008,5, 1, 00, 00, 0);
//                new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,5);

//                stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHE-ASC/precHydroEstimator.metaVHC");
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids_HE/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
//                outputDirectory.mkdirs();
//                zeroSimulationTime.set(2008,5, 1, 00, 00, 0);
//                new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,5);


//                stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain/prec.metaVHC");
//                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids_SatRC1.00/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
//                outputDirectory.mkdirs();
//                zeroSimulationTime.set(2008,5, 1, 00, 00, 0);
//                new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


                java.io.File[] stormRealizationsSpace=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/AggregatedEventHydroNexrad/").listFiles();

                //for (int k = 0; k < stormRealizationsSpace.length; k++){
                for (int k = 2; k < 3; k++){
                    java.io.File[] stormRealizationsTime=new java.io.File(stormRealizationsSpace[k].getAbsolutePath()).listFiles();

                    //for (int l = 2; l < stormRealizationsTime.length; l++){
                    for (int l = 2; l < 3; l++){
                        stormFile=new java.io.File(stormRealizationsTime[l].getAbsolutePath()+"/Time/Bin/H00070802_R1504_G_.metaVHC");

                        outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/CedarRapids_"+stormRealizationsSpace[k].getName()+"_"+stormRealizationsTime[l].getName()+"/AveragedIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
                        outputDirectory.mkdirs();
                        zeroSimulationTime.set(2008,5, 1, 00, 00, 0);
                        new ParallelSimulationToFile(2734, 1069,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
                    }
                }

            }
        //}
    }

    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 42; i <= 64; i++) {
            for (int j = 0; j <= 0; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        int dScale=4;

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/05454300/NED_00159011.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);

        routingParams.put("v_o",0.5f);

        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(2008,4, 29, 0, 0, 0);

        java.io.File outputDirectory;

        float v0=0.40f;
        routingParams.put("v_o",v0);
        float lam1 = 0.2f;

        //for (float lam1 = 0.2f; lam1 <= 0.5f; lam1+=0.1f) {
            for (float lam2 = -0.1f; lam2 <= -0.1f; lam2+=0.05f) {

                routingParams.put("lambda1",lam1);
                routingParams.put("lambda2",lam2);

                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/ClearCreek/FullIowaRiver_"+v0+"_"+lam1+"_"+lam2+"/");
                outputDirectory.mkdirs();
                new ParallelSimulationToFile(1570,127,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

            }

        //}
        routingParams.put("v_o",0.5f);
        outputDirectory=new java.io.File("/home/ricardo/simulationResults/Parallel/AveragedIowaRiver_NonLinear/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile(2734, 1069 ,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,dScale);

    }

    public static void subMain3(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 43; i <= 64; i++) {
            for (int j = 0; j <= 1; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        int xOut=2734;
        int yOut=1069;
        int dScale=4;

        if(args.length == 4){
            xOut=Integer.parseInt(args[0]);
            yOut=Integer.parseInt(args[1]);
            numNodes=Integer.parseInt(args[2]);
            dScale=Integer.parseInt(args[3]);
        }

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");

        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);

        routingParams.put("v_o",0.4f);

        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        
        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(2008,4, 29, 0, 0, 0);

        java.io.File outputDirectory;

        outputDirectory=new java.io.File("/usr/home/rmantill/Luciana/test/AveragedIowaRiver_"+xOut+"_"+yOut+"_NP"+numNodes+"_DL"+dScale+"/");
        //outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/AveragedIowaRiver_"+xOut+"_"+yOut+"_NP"+numNodes+"_DL"+dScale+"/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,dScale);
    }

    public static void subMain4(String args[]) throws java.io.IOException, VisADException {

        int pl=0;
        String folder="";

        if(args[0].equalsIgnoreCase("0")){
            pl = 0;
            folder="Data";
        }

        if(args[0].equalsIgnoreCase("1")){
            pl = 0;
            folder="sim001-050";
        }

        if(args[0].equalsIgnoreCase("2")){
            pl = 1;
            folder="sim051-100";
        }

        if(args[0].equalsIgnoreCase("3")){
            pl = 2;
            folder="sim101-150";
        }

        if(args[0].equalsIgnoreCase("4")){
            pl = 3;
            folder="sim151-200";
        }

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 42; i <= 60; i++){
            for (int j = pl; j <= pl; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = pl; j <= pl; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o",0.5f);
        
        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(2006,6, 26, 20, 0, 0);

        java.io.File[] stormRealizations=new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/BinKICT_2006_07_26t27/CondUncorr/"+folder+"/").listFiles();

        java.io.File outputDirectory;

        for (int k = 0; k < 50; k++){

                stormFile=new java.io.File(stormRealizations[k].getAbsolutePath()+"/prec.metaVHC");
                outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/Whitewaters/CV/"+stormRealizations[k].getName());
                outputDirectory.mkdirs();
                new ParallelSimulationToFile(1063,496,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,2,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
        }
    }

    public static void subMain5(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 42; i <= 64; i++) {
            for (int j = 0; j <= 0; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
        //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);

        routingParams.put("v_o",0.4f);

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        
        java.io.File outputDirectory;

        java.io.File[] stormRealizationsSpace=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/FromPradeep/a5/").listFiles();

        for (int k = 0; k < stormRealizationsSpace.length; k++){

            stormFile=new java.io.File(stormRealizationsSpace[k].getAbsolutePath()+"/prec.metaVHC");

            outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/Marengo_NHD_"+stormRealizationsSpace[k].getName()+"/IowaRiverMarengo_"+0.4+"_"+0.2+"_"+-0.1+"/");
            outputDirectory.mkdirs();
            zeroSimulationTime.set(2005,7, 24, 21, 00, 00);
            new ParallelSimulationToFile(6602,1539,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            new ParallelSimulationToFile(2256,876,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            new ParallelSimulationToFile(7875, 1361,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            new ParallelSimulationToFile(3186,392,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            new ParallelSimulationToFile(2817,713,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            new ParallelSimulationToFile(1570,127,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
//            System.exit(0);
        }
    }


    // MIN MOSIDIED BY LUCIANA

    public static void subMainLUCIANA_IOWA3(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();
        // DEFINE THE PROCESSORS TO BE USED IN THE KENNEDY MACHINE
        // CHECK WHAT IS AVAILABLE
        // USE ALL  NODES BUT NOT ALL PROCESSORS

        for (int i = 44; i <= 60; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        // DEFINE THE DEM
java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
     //           java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();

        // DEFINE BASIN PARAMETERS
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        // DEFINE - CHECK IF RICARDO FIX THE 1/(1-LAMBDA1)
        routingParams.put("v_o",0.4f);
        // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        // DEFINE THE INITIAL TIME OF THE SIMULATION

        //zeroSimulationTime.set(2008,4, 29, 00, 00, 0);
        
        java.io.File outputDirectory;

       // for (float vd = 1.00f; vd <= 14.0f; vd+=2.0f) {
//      
        int xOut=2885;
        int yOut=690; // Iowa River at Iowa City
        outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/3IowaRiver_1day/");
        outputDirectory.mkdirs();       
        stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/1day/vhc/snow_1day.metaVHC");
        zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
        new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/3IowaRiver_7day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_7day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


            //        }
    }
    public static void subMainLUCIANA_CEDAR3(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();
        // DEFINE THE PROCESSORS TO BE USED IN THE KENNEDY MACHINE
        // CHECK WHAT IS AVAILABLE
        // USE ALL  NODES BUT NOT ALL PROCESSORS

        for (int i = 44; i <= 60; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        // DEFINE THE DEM

        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();

        // DEFINE BASIN PARAMETERS
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        // DEFINE - CHECK IF RICARDO FIX THE 1/(1-LAMBDA1)
        routingParams.put("v_o",0.5f);
        // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        // DEFINE THE INITIAL TIME OF THE SIMULATION

        //zeroSimulationTime.set(2008,4, 29, 00, 00, 0);
        zeroSimulationTime.set(2010,2, 27, 00, 00, 0);
        java.io.File outputDirectory;

//        for (float vd = 1.00f; vd <= 14.0f; vd+=2.0f) {
//
//        float rt=25/(1*24);
        
        
        
       
       int xOut=2734;
       int yOut=1069;

       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/3CedarRiver_1day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_1day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/3CedarRiver_7day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_7day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

 //       }//        }


   }

   public static void subMainLUCIANA_IOWA1(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();
        // DEFINE THE PROCESSORS TO BE USED IN THE KENNEDY MACHINE
        // CHECK WHAT IS AVAILABLE
        // USE ALL  NODES BUT NOT ALL PROCESSORS

        for (int i = 44; i <= 60; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        // DEFINE THE DEM
                java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
     //           java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();

        // DEFINE BASIN PARAMETERS
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        // DEFINE - CHECK IF RICARDO FIX THE 1/(1-LAMBDA1)
        routingParams.put("v_o",0.4f);
        // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        // DEFINE THE INITIAL TIME OF THE SIMULATION

        //zeroSimulationTime.set(2008,4, 29, 00, 00, 0);

        java.io.File outputDirectory;

       // for (float vd = 1.00f; vd <= 14.0f; vd+=2.0f) {
//
        int xOut=6602;
        int yOut=1539; // Iowa River at Iowa City
        outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/1IowaRiver_1day/");
        outputDirectory.mkdirs();
        stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/1day/vhc/snow_1day.metaVHC");
        zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
        new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/1IowaRiver_7day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_7day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


            //        }
    }
    public static void subMainLUCIANA_CEDAR1(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();
        // DEFINE THE PROCESSORS TO BE USED IN THE KENNEDY MACHINE
        // CHECK WHAT IS AVAILABLE
        // USE ALL  NODES BUT NOT ALL PROCESSORS

        for (int i = 44; i <= 60; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 62; i <= 64; i++){
            for (int j = 0; j <= 1; j++){
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        int numNodes=myNodeNames.size();
        // DEFINE THE DEM

              java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();

        // DEFINE BASIN PARAMETERS
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        // DEFINE - CHECK IF RICARDO FIX THE 1/(1-LAMBDA1)
        routingParams.put("v_o",0.5f);
        // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        // DEFINE THE INITIAL TIME OF THE SIMULATION

        //zeroSimulationTime.set(2008,4, 29, 00, 00, 0);
        zeroSimulationTime.set(2010,2, 27, 00, 00, 0);
        java.io.File outputDirectory;

//        for (float vd = 1.00f; vd <= 14.0f; vd+=2.0f) {
//
//        float rt=25/(1*24);




       int xOut=3164;
       int yOut=7352;

       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/1CedarRiver_1day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_1day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);


       outputDirectory=new java.io.File("/usr/home/rmantill/luciana/Parallel/snow/1CedarRiver_7day/");
       outputDirectory.mkdirs();
       stormFile=new java.io.File("usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/7day/vhc/snow_7day.metaVHC");
       zeroSimulationTime.set(2010,1, 1, 00, 00, 00);
       new ParallelSimulationToFile(xOut,yOut,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

 //       }//        }


   }


        
}

