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
public class SimulationToAsciiFilePradeep extends java.lang.Object implements Runnable{
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    int x;
    int y;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    
    /** Creates new simulationsRep3 */
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        infiltMetaRaster=infiltMetaRasterOR;
        infiltRate=infiltRateOR;
        routingType=routingTypeOR;
        outputDirectory=outputDirectoryOR;
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        //thisNetworkGeom.setWidthsHG(5.6f, 0.46f);
        //thisNetworkGeom.setChezysHG(14.2f, -1/3.0f);
        thisNetworkGeom.setWidthsHG(1.0f, 0.4f,0.0f);
        
        float chezLawExpon=-1/3f;
        float chezLawCoeff=200f/(float)Math.pow(0.000357911,chezLawExpon);
        thisNetworkGeom.setCheziHG(chezLawCoeff, chezLawExpon);
        
        //PARAMETERS FOR GK ROUTING
        float lam1=3.0f;
        float lam2=1.0f;
        float vo=(float)(1.5/Math.pow(200,lam1)/Math.pow(1100,lam2));
        thisNetworkGeom.setVqParams(vo,0.0f,lam1,lam2);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        
        if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        else
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
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
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
            case 5:     routingString="GK";
                        break;
        }
        
        java.io.File theFile;
        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs");
        System.out.println("Writing Width Functions - "+theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+"\t");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+"\t");
                newfile.write("\n");
        //    }
        }
        
        newfile.close();
        bufferout.close();
        
        
        
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+chezLawExpon+".hydrographs");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-Cuencas9"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_20.0_9.0_dat.hydrographs");
        
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #\t");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+"\t");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order\t");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+"\t");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2]\t");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+"\t");
        }
        
        newfile.write("\n");
        newfile.write("Link Outlet ID\t");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+"\t");
        }
        
        
        newfile.write("\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time\t");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+"\t");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_ChannelLosses thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_ChannelLosses(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]
        //double ic_sum = 0.0f;
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.0;
            //initialCondition[i]=( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;//0.0;
            //initialCondition[i]=0.07*thisNetworkGeom.upStreamArea(i);//0.0;//
            //System.out.println(areasHillArray[0][i]);
            initialCondition[i+linksStructure.contactsArray.length]=1;
            //System.out.println{"Sum of initial " + ic_sum};
            //ic_sum = ic_sum + initialCondition[i] ;
        }
        //System.out.println("Sum of initial q = " + ic_sum);
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = 1;
        
        if(stormFile == null)
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/rainDuration);
        else
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                //rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+8000,2,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),1,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+8000,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        System.out.println("Inicia escritura de Resultados");
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
        
    }
    
    public void run(){
        
        try{
            executeSimulation();
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        } catch (VisADException v){
            System.out.print(v);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //subMain0(args);   //To Run as a external program from shell
            //subMain1(args);   //The test case for TestDem
            subMain2(args);   //Case for Walnut Gulch
            //subMain3(args);     //Case Upper Rio Puerco
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/CuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("C:/Documents and Settings/pmandapa/My Documents/CuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif,150, 2,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif, 50, 6,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif, 10,30,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif,  5,60,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  25,360,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/Sc1IntensityORDuration")).executeSimulation();
        
        
        java.io.File stormFile;
        stormFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/Simulations/ForCuencas/Bin1Cuencas9_3/prec.metaVHC");
        new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/RainfallRealizations")).executeSimulation();
    }
    
}
