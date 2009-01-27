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
 * Created on January 26, 2009, 8:00 AM
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
    
    final int simulProcess=2;
    public int threadsRunning=0;
    
    /** Creates new ParallelSimulationToFile */
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,routingParams,outputDirectory);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,routingParams,outputDirectory);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,routingParams,outputDirectory);
    }
    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster,float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        //Fractioning
        
        java.util.Hashtable<String,Integer> processList=new java.util.Hashtable();
        java.util.Hashtable<Integer,Integer> topoMapping=new java.util.Hashtable();
        
        hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaDatos, matDir);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen=new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(mylinksAnalysis);
        
        float[][] linkAreas=mylinksAnalysis.getVarValues(0);
        float[][] linkLenghts=mylinksAnalysis.getVarValues(1);
        
        int decompScale=4;
        
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
        
        hydroScalingAPI.examples.rainRunoffSimulations.TileSimulationToAsciiFile[] simThreads=new hydroScalingAPI.examples.rainRunoffSimulations.TileSimulationToAsciiFile[connectionTopology.length];
        
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
            
            int[] connectionIDs=new int[connectionTopology[i].length];
            
            for (int j = 0; j < connectionTopology[i].length; j++) {
                System.out.print(topoMapping.get(connectionTopology[i][j])+" ");
                connectionIDs[j]=headsTails[0][topoMapping.get(connectionTopology[i][j])];
            }
            System.out.println();
            
            //Setting up threads
            
            float[] corrections=new float[2];
            
            int connectingLink=mylinksAnalysis.getLinkIDbyHead(xSource, ySource);
            if(connectingLink != -1){
                corrections[0]=linkAreas[0][connectingLink];
                corrections[1]=linkLenghts[0][connectingLink];
            }
            System.out.println(java.util.Arrays.toString(corrections));

            simThreads[i]=new hydroScalingAPI.examples.rainRunoffSimulations.TileSimulationToAsciiFile(xOutlet, yOutlet,xSource, ySource,decompScale, direcc, magnitudes, horOrders, md, rainIntensity, rainDuration, stormFile, infiltMetaRaster, infiltRate, routingType, routingParams, outputDirectory,connectionIDs,this,corrections);
            
        }
        
        boolean allNodesDone=true;
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Parallel Code Begins");
        
        do{
            for (int i = 0; i < simThreads.length; i++) {
                int indexProc=processList.get((String)v.get(i));
                
                if(simThreads[indexProc].completed == false){
                    System.out.println(">> Checking Process "+indexProc+" depends on");
                    for (int j = 0; j < connectionTopology[indexProc].length; j++) {
                        System.out.println(">> Process "+((Integer)topoMapping.get(connectionTopology[indexProc][j])).intValue());
                    }
                    boolean required=true;
                    for (int j = 0; j < connectionTopology[indexProc].length; j++) {
                        required&=simThreads[((Integer)topoMapping.get(connectionTopology[indexProc][j])).intValue()].completed;
                    }
                    if (required) {
                        System.out.println(">> Process "+indexProc+" is being launched");
                        Thread t = new Thread(simThreads[indexProc]);
                        t.start();
                        threadsRunning++;
                        if(threadsRunning == simulProcess) break;
                    } else {
                        System.out.println(">> Process "+indexProc+" is on hold");
                    }
                }
            }
            
            while(threadsRunning == simulProcess){
                System.out.println(">>>>>  CURRENTLY RUNNING "+threadsRunning+" THREADS");
                new visad.util.Delay(1000);
            }
            System.out.println(threadsRunning+" "+simulProcess);
            
            allNodesDone=true;
            for (int i = 0; i < simThreads.length; i++) allNodesDone&=simThreads[i].completed;
            
        } while(!allNodesDone);
        
        System.out.println("Parallel Code Ends");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        //Creating Hydrograph files
        //Creating Peakflow files
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //Uniform Rain
            subMain0(args);  //The test case for Walnut Gulch 30m
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain0(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
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
        
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/simulated_events/uniform_020_01.metaVHC");
        
        java.io.File outputDirectory=new java.io.File("/Users/ricardo/simulationResults/Parallel/Walnut_Gulch/");
        
        new ParallelSimulationToFile(194,281,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,0.0f,2,routingParams,outputDirectory);
            
    }
    

    
}
