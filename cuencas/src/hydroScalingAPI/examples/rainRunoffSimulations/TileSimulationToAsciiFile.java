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

import java.io.File;
import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class TileSimulationToAsciiFile extends java.lang.Object implements Runnable{
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir,hortonOrders;
    
    int x,y,xH,yH,scale;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    
    int[] usConnections;
    int basinOrder;
    float[] corrections;
    
    public boolean completed=false;
    
    hydroScalingAPI.examples.rainRunoffSimulations.ParallelSimulationToFile coordinatorProc;
    
    public TileSimulationToAsciiFile(   int xx, 
                                        int yy, 
                                        int xxHH, 
                                        int yyHH,
                                        int scaleO,
                                        byte[][] direcc, 
                                        int[][] magnitudesOR,
                                        byte[][] horOrders, 
                                        hydroScalingAPI.io.MetaRaster md, 
                                        float rainIntensityOR, 
                                        float rainDurationOR, 
                                        java.io.File stormFileOR ,
                                        hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, 
                                        float infiltRateOR, 
                                        int routingTypeOR, 
                                        java.util.Hashtable rP,
                                        java.io.File outputDirectoryOR,
                                        int[] connectionsO,
                                        hydroScalingAPI.examples.rainRunoffSimulations.ParallelSimulationToFile coordinator,
                                        float[] correctionsO) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        xH=xxHH;
        yH=yyHH;
        scale=scaleO;
        
        magnitudes=magnitudesOR;
        hortonOrders=horOrders;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        infiltMetaRaster=infiltMetaRasterOR;
        infiltRate=infiltRateOR;
        routingType=routingTypeOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
        usConnections=connectionsO;
        
        coordinatorProc=coordinator;
        
        corrections=correctionsO;
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        
        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(x,y,xH,yH,matDir,hortonOrders,metaDatos,scale);
        myCuenca.setXYBasin(myTileActual.getXYRsnTile());
        
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        basinOrder=linksStructure.getBasinOrder();
        
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

        float v_o=((Float)routingParams.get("v_o")).floatValue();

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
        
        int connectingLink=linksStructure.getLinkIDbyHead(xH, yH);
        
        if(connectingLink != -1){
            thisHillsInfo.setArea(connectingLink,corrections[0]);
            thisNetworkGeom.setLength(connectingLink, corrections[1]);
        }
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
        
        if(infiltMetaRaster == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_"+v_o+".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+".csv");
        
        System.out.println(theFile);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        java.io.File theFile1=new java.io.File(theFile.getAbsolutePath()+".Outlet");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);
        
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+i+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(thisNetworkGeom.linkOrder(i)+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(thisNetworkGeom.Length(i)+",");
        }
        
        newfile.write("\n");
        newfile.write("Link Outlet ID,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(i+",");
        }
        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+i+",");
        }
        
        
        java.io.File[] filesToAdd=new java.io.File[usConnections.length];
        for (int i = 0; i < filesToAdd.length; i++) {
            String[] file1=theFile.getAbsolutePath().split(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-");
            int xCon=usConnections[i]%metaDatos.getNumCols();
            int yCon=usConnections[i]/metaDatos.getNumCols();
            filesToAdd[i]=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+xCon+"_"+yCon+"-"+file1[1]+".Outlet");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.IncompleteNetworkEquations_HillDelay thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.IncompleteNetworkEquations_HillDelay(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,connectingLink,filesToAdd);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.0;
            initialCondition[i+linksStructure.contactsArray.length]=0.0;
        }
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        double outputTimeStep=Math.min(Math.pow(2.0D,(basinOrder-1)),storm.stormRecordResolutionInMinutes());
        double extraSimTime=120D*Math.pow(2.0D,(basinOrder-1));
        
        newfile1.write("TimeStep:" + outputTimeStep+"\n");
        newfile1.write("Time (minutes), Discharge [m3/s] \n");
        
        
        for (int k=0;k<numPeriods;k++) {
            System.out.println("Period "+(k+1)+" of "+numPeriods);
            rainRunoffRaining.jumpsRunCompleteToAsciiFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),outputTimeStep,initialCondition,newfile,linksStructure,thisNetworkGeom,newfile1);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);
        }

        java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
        System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");

        rainRunoffRaining.jumpsRunCompleteToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,outputTimeStep,initialCondition,newfile,linksStructure,thisNetworkGeom,newfile1);
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(maximumsAchieved[i]+",");
        }
        
        System.out.println("Inicia escritura de Resultados");
        newfile.write("\n");
        
        newfile1.close();
        bufferout1.close();
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
        new visad.util.Delay(1000);
        
        completed=true;
        coordinatorProc.threadsRunning--;
        
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
    

}
