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
public class ParallelSimulationToFile2 extends hydroScalingAPI.examples.rainRunoffSimulations.ParallelSimulationToFile {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    private int simulProcess=2;
    public int threadsRunning=0;
    java.util.Hashtable<String,Boolean> compNodeNames=new java.util.Hashtable<String,Boolean>();

    private java.util.Calendar zeroSimulationTime;
    
    /** Creates new ParallelSimulationToFile */
    public ParallelSimulationToFile2(){
        
    }
    public ParallelSimulationToFile2(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile2(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile2(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,horOrders,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,routingParams,outputDirectory,zST,MyCnodes,numNodes,dscale);
    }
    public ParallelSimulationToFile2(int x, int y, byte[][] direcc, int[][] magnitudes,byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster,float infiltRate, int routingType,java.util.Hashtable routingParams,java.io.File outputDirectory, java.util.Calendar zST,java.util.Hashtable MyCnodes,int numNodes,int dscale) throws java.io.IOException, VisADException{

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
                        System.out.print(">> Process "+indexProc+" is being launched");
                        externalExecutors[indexProc].setComputingNode(cn,indexProc);
                        activeThreads[indexProc] = new Thread(externalExecutors[indexProc]);
                        externalExecutors[indexProc].executing=true;
                        compNodeNames.put(cn, true);

                        activeThreads[indexProc].start();
                        threadsRunning++;
                        currentDtoO=Math.min(currentDtoO, thisDtoO);
                        if(threadsRunning == simulProcess) break;
                    } else {
                        System.out.print(indexProc+",");
                    }
                } else if(externalExecutors[indexProc].completed == true) activeThreads[indexProc]=null;
            }
            System.out.println();
            while(threadsRunning == simulProcess){
                System.out.println(">>>>>  CURRENTLY RUNNING "+threadsRunning+" THREADS. Out of "+simulProcess+".Percentage Completed: "+((1-currentDtoO/maxDtoO)*100)+"%");
                new visad.util.Delay(5000);
            }
            System.out.println(">>>>>  CURRENTLY RUNNING "+threadsRunning+" THREADS. Out of "+simulProcess+". Percentage Completed: "+((1-currentDtoO/maxDtoO)*100)+"%");

            System.out.print(">> Running");
            for (int i = 0; i < externalExecutors.length; i++){
                if(externalExecutors[i].executing) {
                    System.out.print(", "+i);
                }
            }
            System.out.println();

            new visad.util.Delay(5000);
            
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
            
            subMain0(args);  //Using Whitewaters
            
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

        java.util.Hashtable<String,Boolean> myNodeNames=new java.util.Hashtable<String,Boolean>();

        for (int i = 59; i <= 62; i++) {
            for (int j = 0; j <= 3; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 50; i <= 52; i++) {
            for (int j = 0; j <= 3; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 54; i <= 54; i++) {
            for (int j = 0; j <= 3; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }

        for (int i = 44; i <= 44; i++) {
            for (int j = 0; j <= 3; j++) {
                myNodeNames.put("c0"+Double.toString(i/100.0+0.00001).substring(2,4)+"-"+j, false);
            }
        }
        for (int i = 41; i <= 41; i++) {
            for (int j = 0; j <= 3; j++) {
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

        java.io.File outputDirectory;

        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/Data/prec.metaVHC");
        outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/WhitewatersData4/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile2(1063,496,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,2,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

        System.exit(0);

        outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/WhitewatersCase1n/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile2(1063,496,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/Case2/prec.metaVHC");
        outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/WhitewatersCase2n/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile2(1063,496,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);
        outputDirectory=new java.io.File("/usr/home/rmantill/temp/Parallel/WhitewatersCase2c/");
        outputDirectory.mkdirs();
        new ParallelSimulationToFile2(1063,496,matDirs,magnitudes,horOrders,metaModif,stormFile,0.0f,5,routingParams,outputDirectory,zeroSimulationTime,myNodeNames,numNodes,4);

    }
        
}
