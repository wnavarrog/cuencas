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
public class SimulationToAsciiFile_luciana extends java.lang.Object implements Runnable{
    
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
    java.util.Hashtable routingParams;
    
    /** Creates new simulationsRep3 */
    public SimulationToAsciiFile_luciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile_luciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile_luciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile_luciana(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR,java.util.Hashtable rP) throws java.io.IOException, VisADException{
        
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
       
        System.out.println("Simulation to ask Luciana -infiltration"+infiltRate);
        routingType=routingTypeOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
        
        
        
   }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        float widthCoeff=((Float)routingParams.get("widthCoeff")).floatValue();
        float widthExponent=((Float)routingParams.get("widthExponent")).floatValue();
        float widthStdDev=((Float)routingParams.get("widthStdDev")).floatValue();
        
        float chezyCoeff=((Float)routingParams.get("chezyCoeff")).floatValue();
        float chezyExponent=((Float)routingParams.get("chezyExponent")).floatValue();
        
        thisNetworkGeom.setWidthsHG(widthCoeff,widthExponent,widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);
        
        float lam1=((Float)routingParams.get("lambda1")).floatValue();
        float lam2=((Float)routingParams.get("lambda2")).floatValue();

        float v_o=(float)(1.5/Math.pow(200,lam1)/Math.pow(1100,lam2));

        thisNetworkGeom.setVqParams(v_o,0.0f,lam1,lam2);
        
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
        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        System.out.println("infiltRate = "+infiltRate);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
//        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
//        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
//            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
//                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
//                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
//                newfile.write("\n");
//            }
//        }
        
        newfile.close();
        bufferout.close();
        
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+"_"+lam1+"_"+lam2+".csv");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+lam1+"_"+lam2+".csv");
        
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
 //       newfile.write("Information on Complete order Streams\n");
 //       newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("1,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
                newfile.write(linksStructure.completeStreamLinksArray[i]+",");
        }
        
        newfile.write("\n");
        newfile.write("2,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("3,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+",");
        }
        
         newfile.write("\n");
       newfile.write("4,");       
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
            newfile.write(thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("5,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        
 //       newfile.write("\n\n\n");
 //       newfile.write("Results of flow simulations in your basin");
         newfile.write("\n");       
         newfile.write("\n");
        newfile.write("6,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
                newfile.write(linksStructure.completeStreamLinksArray[i]+",");
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
        
        newfile.write(numPeriods);
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                //rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
          System.out.println("Inicia escritura de Resultados");         
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+800,2,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),5,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            // define duration - as same duration as rainfall +350 minutes
            // incremental time is 5 - can i use 15????
            
            //rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+350,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
            double ndays = 5;
            double durevent = ndays*24*60 - (storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes());
            rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),durevent,15,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        
        System.out.println("Termina simulacion RKF");
        ///////////////////////////////////////////////////
        
        //////////////////////////////////////////////////
        
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
 
                System.out.println("termina escritura de Resultados");
        
        newfile.close();
        bufferout.close();
        System.out.println("Inicia escritura de Precipitaciones");
        
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"precipitation_zero"+".csv");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"precipitation_"+stormFile.getName()+".csv");
        
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        

        
 //       newfile.write("Information of precipition for each link\n");
 //       newfile.write("Links at the bottom of complete streams are:\n");
 //       newfile.write(1,");
        newfile.write("1,");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
                newfile.write(linksStructure.completeStreamLinksArray[i]+",");
        }
       newfile.write("\n");
 // precipitation
        
        for (int k=0;k<numPeriods;k++) {

           double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
           newfile.write(currTime+",");
           java.util.Calendar thisDate1=java.util.Calendar.getInstance();
           thisDate1.setTimeInMillis((long)(currTime*60.*1000.0));
   
           newfile.write("\n");
           newfile.write(thisDate1.getTime()+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
              if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisHillsInfo.precipitation(i,currTime)+",");
                
            }
                    newfile.write("\n");
        }
        
        System.out.println("Termina escritura de Precipitaciones");       
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
        newfile.close();
        bufferout.close();    
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
            
            subMain1(args);   //11140102 - Blue River       
     
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        try{
            
            subMain2(args);   //11110103 - Illinois, Arkansas
      
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        try{
            
            subMain3(args);   //11070208 - Elk River Near Tiff   
     
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
       try{
            
            subMain4(args);   //Clear Creek   
     
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
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.285f);
        routingParams.put("lambda2",-0.10f); 
        
       String pathinput = "C:/CUENCAS/11140102/Rasters/Topography/";
        java.io.File theFile=new java.io.File(pathinput + "NED_20864854" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_20864854" + ".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        
        String path = "C:/CUENCAS/11140102/results/event1/";
        String rain = "C:/CUENCAS/11140102/data/radar/event1/prec.metaVHC";
        int x = 2444;
        int y = 1611;
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,2,new java.io.File(path),routingParams).executeSimulation();
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,2,new java.io.File(path),routingParams).executeSimulation();
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,2,new java.io.File(path),routingParams).executeSimulation();        
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,5,new java.io.File(path),routingParams).executeSimulation();    
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,5,new java.io.File(path),routingParams).executeSimulation();          
 //       routingParams.put("lambda1",0.285f);
 //       routingParams.put("lambda2",-0.10f); 
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,5,new java.io.File(path),routingParams).executeSimulation();          
        
 //       routingParams.put("lambda1",0.350f);
 //       routingParams.put("lambda2",-0.20f);
 
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,5,new java.io.File(path),routingParams).executeSimulation();          
 //       path = "C:/CUENCAS/11140102/results/event2/";
 //       rain = "C:/CUENCAS/11140102/data/radar/event2/prec.metaVHC";
      
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,2,new java.io.File(path),routingParams).executeSimulation();
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,2,new java.io.File(path),routingParams).executeSimulation();
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,2,new java.io.File(path),routingParams).executeSimulation();        
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,5,new java.io.File(path),routingParams).executeSimulation();    
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,5,new java.io.File(path),routingParams).executeSimulation();          
       //new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,5,new java.io.File(path),routingParams).executeSimulation();          

      path = "C:/CUENCAS/11140102/results/event3/";
      rain = "C:/CUENCAS/11140102/data/radar/event3/prec.metaVHC";
   
       routingParams.put("lambda1",0.285f);
       routingParams.put("lambda2",-0.10f); 
    
  //     new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,2,new java.io.File(path),routingParams).executeSimulation();
       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,2,new java.io.File(path),routingParams).executeSimulation();
  //     new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,2,new java.io.File(path),routingParams).executeSimulation();        
  //     new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,5,new java.io.File(path),routingParams).executeSimulation();    
       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,5,new java.io.File(path),routingParams).executeSimulation();          
       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,5,new java.io.File(path),routingParams).executeSimulation();          

       
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
       
             java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.20f);
        routingParams.put("lambda2",-0.35f); 
        
        String pathinput = "C:/CUENCAS/11110103/Rasters/Topography/";
        java.io.File theFile=new java.io.File(pathinput + "NED_71821716" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_71821716" + ".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        String path = "C:/CUENCAS/11110103/results/07196500/GK_0.20_0.35/";
        int x = 497;
        int y = 773;
        
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 10,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 20,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,60,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,120,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,360,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
        
        path = "C:/CUENCAS/11110103/results/07197000/GK_0.20_0.35/";
        x = 804;
        y = 766;
        
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 10,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 20,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,60,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,120,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,360,0.0f,5,new java.io.File(path),routingParams).executeSimulation();

    }
   
      public static void subMain3(String args[]) throws java.io.IOException, VisADException {
        
               java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.20f);
        routingParams.put("lambda2",-0.35f); 
        
        String pathinput = "C:/CUENCAS/11070208/Rasters/Topography/";
        java.io.File theFile=new java.io.File(pathinput + "NED_23370878" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_23370878" + ".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        String path = "C:/CUENCAS/11070208/results/GK_0.20_0.35/";
        int x = 296;
        int y = 1167;
        
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 10,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20, 20,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,60,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,120,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
 //       new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,20,360,0.0f,5,new java.io.File(path),routingParams).executeSimulation();
    }
      
      
   public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.375f);
        routingParams.put("lambda2",-0.20f); 
        
        String pathinput = "C:/CUENCAS/Clear_Creek/Rasters/Topography/";
        java.io.File theFile=new java.io.File(pathinput + "NED_97552106" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_97552106" + ".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        String path = "C:/CUENCAS/Clear_Creek/results/event2/";
        String rain = "C:/CUENCAS/Clear_Creek/data/radar/event2/prec.metaVHC";
        int x = 1623;
        int y = 221;
        routingParams.put("lambda1",0.375f);
        routingParams.put("lambda2",-0.10f); 
        
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,2,new java.io.File(path),routingParams).executeSimulation();
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,2,new java.io.File(path),routingParams).executeSimulation();
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,2,new java.io.File(path),routingParams).executeSimulation();        
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),0.0f,5,new java.io.File(path),routingParams).executeSimulation();    
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),10.0f,5,new java.io.File(path),routingParams).executeSimulation();          
  //      new SimulationToAsciiFile_luciana(x,y,matDirs,magnitudes,metaModif,new java.io.File(rain),20.0f,5,new java.io.File(path),routingParams).executeSimulation();          
    }
    
}
