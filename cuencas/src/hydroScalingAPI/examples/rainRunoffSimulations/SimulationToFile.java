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
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class SimulationToFile extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    /** Creates new simulationsRep3 */
    public SimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,stormFile,infiltMetaRaster,0.0f,routingType,routingParams);
    }
    public SimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int routingType,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,stormFile,null,infiltRate,routingType,routingParams);
    }
    public SimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster,float infiltRate, int routingType,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        float widthCoeff=((Float)routingParams.get("widthCoeff")).floatValue();
        float widthExponent=((Float)routingParams.get("widthExponent")).floatValue();
        float widthStdDev=((Float)routingParams.get("widthStdDev")).floatValue();
        
        float chezyCoeff=((Float)routingParams.get("chezyCoeff")).floatValue();
        float chezyExponent=((Float)routingParams.get("chezyExponent")).floatValue();
        
        thisNetworkGeom.setWidthsHG(widthCoeff,widthExponent,widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);
        
        int linkID=0;
        
        /*
        //Some particular links relevant to Wlanut Gulch, AZ
        linkID=linksStructure.getResSimID(194,281);
        System.out.println("WS63001:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(420,303);
        System.out.println("WS63002:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(574,298);
        System.out.println("WS63003:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(617,317);
        System.out.println("WS63004:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(665,213);
        System.out.println("WS63005:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(572,273);
        System.out.println("WS63006:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(420,302);
        System.out.println("WS63007:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(583,266);
        System.out.println("WS63008:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(686,249);
        System.out.println("WS63009:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(683,252);
        System.out.println("WS63010:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(777,324);
        System.out.println("WS63011:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(631,231);
        System.out.println("WS63015:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(592,326);
        System.out.println("WS63104:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(947,302);
        System.out.println("WS63111:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(982,317);
        System.out.println("WS63113:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(647,288);
        System.out.println("WS63121:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        System.exit(0);*/

        /*
         //Some particular links relevant to Whitewaters, KS
        
        //Rock Creek and Towanda
        linkID=linksStructure.getResSimID(3060,	2200);
        System.out.println("Rock Creek:     "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1)+"  "+thisNetworkGeom.Slope(linkID-1));
        linkID=linksStructure.getResSimID(3191, 1460);
        System.out.println("Towanda:        "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1)+"  "+thisNetworkGeom.Slope(linkID-1));
         
        
        //Potential Gauges Locations 
        linkID=linksStructure.getResSimID(2996,	1790);
        System.out.println("001:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(3123,	3537);
        System.out.println("002:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(4177,	4285);
        System.out.println("003:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(4177,	4284);
        System.out.println("004:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(3229,	3959);
        System.out.println("005:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        linkID=linksStructure.getResSimID(3139,	3926);
        System.out.println("006:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        System.exit(0);*/
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,0.0f);
        
        if(infiltMetaRaster == null)
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        else
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca,linksStructure,infiltMetaRaster,matDir,magnitudes);
        
        thisHillsInfo.setInfManager(infilMan);
        
            /*
                Escribo en un theFile lo siguiente:
                        Numero de links
                        Numero de links Completos
                        lista de Links Completos
                        Area aguas arriba de los Links Completos
                        Orden de los Links Completos
                        maximos de la WF para los links completos
                        Longitud simulacion
                        Resulatdos
             
             */
        //String Directory="/tmp/";
        String Directory="/ResultsSimulations/simulationsWalnutGulch/";
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        String routingString="";
        switch (routingType) {
            case 0:     routingString="VC";
                        break;
            case 1:     routingString="CC";
                        break;
            case 2:     routingString="CV";
                        break;
            case 3:     routingString="CM";
                        break;
            case 4:     routingString="VM";
                        break;
        }
        java.io.File theFile=new java.io.File(Directory+demName+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+".dat");
        if(infiltMetaRaster == null)
            theFile=new java.io.File(Directory+demName+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+".dat");
        else
            theFile=new java.io.File(Directory+demName+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        System.out.println(theFile);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
        
        newfile.writeInt(linksStructure.contactsArray.length);
        newfile.writeInt(linksStructure.completeStreamLinksArray.length);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeInt(linksStructure.completeStreamLinksArray[i]);
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            //newfile.writeFloat((float) thisNetworkGeom.Length(linksStructure.completeStreamLinksArray[i]));
	    newfile.writeFloat((float) thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]));
            //newfile.writeFloat((float) linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]]);
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            //newfile.writeFloat((float) thisNetworkGeom.upStreamTotalLength(linksStructure.completeStreamLinksArray[i]));
            newfile.writeFloat((float) thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i]));
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeFloat((float) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));
        }
        
        /*newfile.close();
        bufferout.close();
        System.exit(0);*/
        
        System.out.println("Starting Width Functions Calculation");
        
        hydroScalingAPI.util.geomorphology.objects.Basin itsCuenca;
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis itsLinksStructure;
        int thisX,thisY,metric=1,numBins;
        
        float binsize=1;
        
        double[][] laWFunc;
        
        RealType numLinks= RealType.getRealType("numLinks"), distanceToOut = RealType.getRealType("distanceToOut");
        FlatField vals_ff_W,hist;
        Linear1DSet binsSet;
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] > 1) {
                /*thisX=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaDatos.getNumCols();
                thisY=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaDatos.getNumCols();
                itsCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(thisX,thisY,matDir,metaDatos);
                itsLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(itsCuenca, metaDatos, matDir);
                float[][] wFunc=itsLinksStructure.getDistancesToOutlet();
                java.util.Arrays.sort(wFunc[metric]);
                 
                float[][] gWFunc=new float[1][wFunc[metric].length];
                 
                for (int j=0;j<wFunc[0].length;j++)
                    gWFunc[0][j]=(float) wFunc[metric][j];
                
                vals_ff_W = new FlatField( new FunctionType(distanceToOut, numLinks), new Linear1DSet(distanceToOut,0,gWFunc[0].length,gWFunc[0].length));
                vals_ff_W.setSamples( gWFunc );
                
                if (metric == 0) {
                    float[][] varValues=itsLinksStructure.getVarValues(1);
                    binsize=new hydroScalingAPI.tools.Stats(varValues).meanValue;
                }
                numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;
                binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);

                hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);
                 
                laWFunc=hist.getValues();
                 
                hydroScalingAPI.tools.Stats widthStat=new hydroScalingAPI.tools.Stats(laWFunc[0]);
                
                int maxLocation=laWFunc[0].length;
                for (int k=0;k<laWFunc[0].length;k++) if(laWFunc[0][k] == widthStat.maxValue) maxLocation=k;
                
                float[] maxWF={widthStat.maxValue,maxLocation};
                newfile.writeFloat(maxWF[0]);
                newfile.writeFloat(maxWF[1]);*/

                newfile.writeFloat(1.0f);
                newfile.writeFloat(1.0f);
            } else {
                newfile.writeFloat(1.0f);
                newfile.writeFloat(1.0f);
            }
        }
        
        System.out.println("Termina calculo de WFs");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.0;//0.07*thisNetworkGeom.upStreamArea(i);
            //initialCondition[i]=( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;//Initial condition equivalent to instant rain
            initialCondition[i+linksStructure.contactsArray.length]=1;
        }
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        /*javaSIH.util.DiffEqSolver.RKF rainRunoffRaining1=new javaSIH.util.DiffEqSolver.RKF(thisBasinEqSys,1e-5,1.0e-3);
        double[][] thisStep=rainRunoffRaining1.step(storm.stormInitialTimeInMinutes(), initialCondition, 1e-3, false);
         
        int nLi=linksStructure.headsArray.length;
         
        for (int i=0;i<nLi;i++){
            double V_l= Math.pow(initialCondition[i],1/3.)
         *Math.pow(thisNetworkGeom.Chezi(i),2/3.)
         *Math.pow(thisNetworkGeom.Width(i),-1/3.)
         *Math.pow(thisNetworkGeom.Slope(i),1/3.);
            System.out.println(V_l+" "+thisNetworkGeom.Chezi(i)+" "+initialCondition[i]+" "+thisNetworkGeom.Width(i)+" "+thisNetworkGeom.Length(i)+" "+thisNetworkGeom.upStreamArea(i));
        }
        System.out.println();
         
        System.exit(0);*/
        
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        newfile.writeInt(numPeriods);
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        for (int k=0;k<numPeriods;k++) {
            System.out.println("Period "+(k+1)+" of "+numPeriods);
            rainRunoffRaining.jumpsRunToFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),1,initialCondition,newfile);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);
        }
        java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
        System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
        
        rainRunoffRaining.jumpsRunToFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+1000,5,initialCondition,newfile);
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        System.out.println("Inicia escritura de Resultados");
        
        System.out.println("Inicia escritura de Precipitaciones");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        
        newfile.writeInt(numPeriods);
        
        for (int k=0;k<numPeriods;k++) {
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));
                
            }
            
        }
        
        System.out.println("Termina escritura de Precipitaciones");
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            newfile.writeInt(linksStructure.connectionsArray[i].length);
            if (linksStructure.connectionsArray[i].length > 0) {
                for (int j=0;j<linksStructure.connectionsArray[i].length;j++){
                    newfile.writeInt(linksStructure.connectionsArray[i][j]);
                }
            }
        }
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //Uniform Rain
            //subMain1(args);  //The test case for Whitewater
            subMain3(args);  //The test case for Walnut Gulch 30m
            //subMain4(args);   //The test case for TestDem
            //subMain5(args);   //The Man-Vis Tree
            //subMain6(args);   //The Peano Tree
            //subMain8(args);   //The Test Case for Walnut Creek
            //subMain9(args);  //The test case for Walnut Gulch 10m
            
            
            //Rainfields from data
            //subMain2(args);   //using constant infiltration in space
            //subMain7(args);   //using a map to set infiltration values
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
        //java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dir"));

        java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        /*Basins of Interest:
          Towanda: 1063,496
          Rock Creek: */
        
        //stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/uniform_100_01.metaVHC");
        //new SimulationToFile(1063,496,matDirs,magnitudes,metaModif,stormFile,0.0f,0);
        
        //stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/random_45_10.metaVHC");
        //new SimulationToFile(1063,496,matDirs,magnitudes,metaModif,stormFile,0.0f,0);
        
        /*stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/uniform_100_01.metaVHC");
        new SimulationToFile(3162,1456,matDirs,magnitudes,metaModif,stormFile,0.0f,2);*/

        /*stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/uniform_100_10.metaVHC");
        new SimulationToFile(3162,1456,matDirs,magnitudes,metaModif,stormFile,0.0f,2);*/

        stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/uniform_100_120.metaVHC");
        new SimulationToFile(3162,1456,matDirs,magnitudes,metaModif,stormFile,0.0f,2,null);

    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        String evNUM,evStamp;
        for (int evID=7;evID<20;evID++){
            evNUM=(""+(evID/100.+0.001)).substring(2,4);
            evStamp="_ev"+evNUM;
            stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            new SimulationToFile(82,260,matDirs,magnitudes,metaModif,stormFile,0.0f,0,routingParams);
        }
        
    }
    
    public static void subMain3(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/simulated_events/uniform_100_01.metaVHC");
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);

        for(float ce=0f;ce<=0.5;ce+=0.05){
            routingParams.put("chezyExponent",ce);
            new SimulationToFile(82,260,matDirs,magnitudes,metaModif,stormFile,0.0f,2,routingParams);
        }

        
    }
    
    public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        stormFile=new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Hydrology/storms/simulated_events/randomMean100.metaVHC");
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        new SimulationToFile(3,96,matDirs,magnitudes,metaModif,stormFile,0.0f,0,routingParams);
        
    }
    
    public static void subMain5(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        stormFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Hydrology/storms/multifractal_events/event02/multifractal_prec.metaVHC");
        //new SimulationToFile(128,128,matDirs,magnitudes,metaModif,stormFile,0.0f,2);
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        new SimulationToFile(128,128,matDirs,magnitudes,metaModif,stormFile,0.0f,1,routingParams);
        
    }
    
    public static void subMain6(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/peano/peano.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/peano/peano.dir"));
        
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        stormFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Hydrology/storms/simulated_events/uniform_500_01.metaVHC");
        new SimulationToFile(256,256,matDirs,magnitudes,metaModif,stormFile,0.0f,2,null);
        
        stormFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Hydrology/storms/simulated_events/uniform_100_01.metaVHC");
        new SimulationToFile(256,256,matDirs,magnitudes,metaModif,stormFile,0.0f,2,null);
        
    }
    
    public static void subMain7(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        String evNUM,evStamp;
        for (int evID=0;evID<20;evID++){
            evNUM=(""+(evID/100.+0.001)).substring(2,4);
            evStamp="_ev"+evNUM;
            stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            
            hydroScalingAPI.io.MetaRaster infiltMeta=new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/infiltrationRates/event"+evNUM+"/infiltrationRandomRate_event"+evNUM+".metaVHC"));
            new SimulationToFile(82,260,matDirs,magnitudes,metaModif,stormFile,infiltMeta,0,routingParams);
            
            infiltMeta=new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/infiltrationRates/event"+evNUM+"/infiltrationRate_event"+evNUM+".metaVHC"));
            new SimulationToFile(82,260,matDirs,magnitudes,metaModif,stormFile,infiltMeta,0,routingParams);

        }
                
    }
    
    public static void subMain8(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        //x: 999, y: 596 
        stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/WCuniform_45_10.metaVHC");
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        new SimulationToFile(1309,312,matDirs,magnitudes,metaModif,stormFile,0.0f,0,routingParams);
        //new SimulationToFile( 1239,3077,matDirs,magnitudes,metaModif,stormFile,0.0f,0);
        
    }
    
    public static void subMain9(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/0.3_ArcSec_USGS/75845176.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/0.3_ArcSec_USGS/75845176.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.3f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/simulated_events/uniform_050_01.metaVHC");
        new SimulationToFile(259,744,matDirs,magnitudes,metaModif,stormFile,0.0f,0,routingParams);

    }
    
}
