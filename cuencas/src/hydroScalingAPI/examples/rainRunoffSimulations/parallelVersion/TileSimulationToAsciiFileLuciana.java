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

package hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion;

import visad.*;
import java.text.DecimalFormat;

/**
 *
 * @author Ricardo Mantilla
 */
public class TileSimulationToAsciiFileLuciana extends java.lang.Object implements Runnable{
    
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

    private java.util.Calendar zeroSimulationTime;

    public TileSimulationToAsciiFileLuciana(   int xx,
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
                                        float[] correctionsO,
                                        long zST) throws java.io.IOException, VisADException{

        zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.setTimeInMillis(zST);
        
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
        
        corrections=correctionsO;

    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
	//Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        
        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(x,y,xH,yH,matDir,hortonOrders,metaDatos,scale);
        myCuenca.setXYBasin(myTileActual.getXYRsnTile());
        
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        basinOrder=linksStructure.getBasinOrder();

        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        java.io.File theFile2=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+".complete.csv");
        java.io.OutputStreamWriter compnewfile = new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(theFile2)));
        for(int i=0;i<linksStructure.completeStreamLinksArray.length;i++) compnewfile.write(linksStructure.completeStreamLinksArray[i]+",");

        compnewfile.close();

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
        
        //System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        
        if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        else
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);


        storm.setStormInitialTime(zeroSimulationTime);
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

        if(theFile.exists()){
            //ATTENTION
            //The followng print statement announces the completion of the program.
            //DO NOT modify!  It tells the queue manager that the process can be
            //safely killed.
            System.out.println("Termina escritura de Resultados");
            return;
        }
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        java.io.File theFile1=new java.io.File(theFile.getAbsolutePath()+".Outlet.csv");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);

        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");
        DecimalFormat df10 = new DecimalFormat("###.##########");

        newfile.write("Information on order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+df.format(i)+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(df.format(thisNetworkGeom.linkOrder(i))+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(df3.format(thisNetworkGeom.upStreamArea(i))+",");
        }


        newfile.write("\n");
        newfile.write("Link Outlet ID,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(df.format(i)+",");
        }

        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+df.format(i)+",");
        }
        
        
        java.io.File[] filesToAdd=new java.io.File[usConnections.length];
        for (int i = 0; i < filesToAdd.length; i++) {
            String[] file1=theFile.getAbsolutePath().split(demName+"_"+x+"_"+y+"-");
            int xCon=usConnections[i]%metaDatos.getNumCols();
            int yCon=usConnections[i]/metaDatos.getNumCols();
            filesToAdd[i]=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+xCon+"_"+yCon+"-"+file1[1]+".Outlet.csv");
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
        
        int numPeriods = Math.round(((float)storm.stormFinalTimeInMinutes()-(float)storm.stormInitialTimeInMinutes())/(float)storm.stormRecordResolutionInMinutes());

        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        double outputTimeStep=storm.stormRecordResolutionInMinutes();
        double extraSimTime=240D*Math.pow(2.0D,(basinOrder-1));
        
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


        outputTimeStep=5*Math.pow(2.0D,(basinOrder-1));
        rainRunoffRaining.jumpsRunCompleteToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,outputTimeStep,initialCondition,newfile,linksStructure,thisNetworkGeom,newfile1);
        
        newfile1.close();
        bufferout1.close();

        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        double[] timeToMaximumsAchieved=rainRunoffRaining.getTimeToMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(df3.format(maximumsAchieved[i])+",");
        }
        newfile.write("\n");
        newfile.write("Time to Maximum Discharge [minutes],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(df2.format(timeToMaximumsAchieved[i])+",");
        }
        newfile.write("\n");
        newfile.write("\n");


        newfile.write("Precipitation Rates [mm/hr],");

        newfile.write("\n");

        for (int k=0;k<numPeriods;k++) {
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            newfile.write(currTime+",");
            for (int i=0;i<linksStructure.contactsArray.length;i++){

                newfile.write(df2.format(thisHillsInfo.precipitation(i,currTime))+",");

            }

            newfile.write("\n");

        }

        System.out.println("Inicia escritura de Resultados de precipitacao");
        
        newfile.close();
        bufferout.close();


        /// Print Rainfall Statistics - in space - variability among hillslopes
        
        theFile2=new java.io.File(theFile.getAbsolutePath()+".Prec.csv");
        java.io.FileOutputStream salida2 = new java.io.FileOutputStream(theFile2);
        java.io.BufferedOutputStream bufferout2 = new java.io.BufferedOutputStream(salida2);
        java.io.OutputStreamWriter newfile2 = new java.io.OutputStreamWriter(bufferout2);



        System.out.println("comeca arquivo de chuva - parte 1!!");
        newfile2.write("Information on order Streams\n");
        newfile2.write("Links at the bottom of complete streams are:\n");
        newfile2.write("Link #,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile2.write("Link-"+df.format(i)+",");
        }

        newfile2.write("\n");
        newfile2.write("Horton Order,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile2.write(df.format(thisNetworkGeom.linkOrder(i))+",");
        }

        newfile2.write("\n");
        newfile2.write("Upstream Area [km^2],");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile2.write(df2.format(thisNetworkGeom.upStreamArea(i))+",");
        }


        newfile2.write("\n");
        newfile2.write("Link Outlet ID,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df.format(i)+",");
        }

System.out.println("passa primeiras linhas5");
        newfile2.write("\n");
        newfile2.write("Link Outlet X,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
        int xCon=linksStructure.contactsArray[i]%metaDatos.getNumCols();
        newfile2.write(df.format(xCon)+",");
        }

        newfile2.write("\n");
        newfile2.write("Link Outlet Y,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
        int yCon=linksStructure.contactsArray[i]/metaDatos.getNumCols();
        newfile2.write(df.format(yCon)+",");
        }

        newfile2.write("\n");
        newfile2.write("Link Outlet dist,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
        int yCon=linksStructure.contactsArray[i]/metaDatos.getNumCols();
        int xCon=linksStructure.contactsArray[i]%metaDatos.getNumCols();
        double dist=Math.pow(Math.pow((xCon-x),2)+Math.pow((yCon-y),2),0.5);
        newfile2.write(df2.format(dist)+",");
        }


        System.out.println("comeca arquivo de chuva - parte 2!");
        double [] coverTime=new double[linksStructure.contactsArray.length];
        double [] mean=new double[linksStructure.contactsArray.length];
        double [] stdev=new double[linksStructure.contactsArray.length];
        double [] max=new double[linksStructure.contactsArray.length];
        double [] timemax=new double[linksStructure.contactsArray.length];
        double [] acum=new double[linksStructure.contactsArray.length];

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            mean[i]=0.0;
            stdev[i]=0;
            max[i]=0;
            timemax[i]=0;
            acum[i]=0;
            coverTime[i]=0;
            for (int k=0;k<numPeriods;k++) {
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            double prevcurrTime=0;
            if(k>0)
               {prevcurrTime=storm.stormInitialTimeInMinutes()+(k-1)*storm.stormRecordResolutionInMinutes();
               acum[i]=acum[i]+thisHillsInfo.precipitation(i,currTime)*(currTime-prevcurrTime)/60;}
            mean[i]=mean[i]+thisHillsInfo.precipitation(i,currTime);
            if(max[i]<thisHillsInfo.precipitation(i,currTime))
               {max[i]=thisHillsInfo.precipitation(i,currTime);
                timemax[i]=currTime;}
            if(thisHillsInfo.precipitation(i,currTime)>0)  coverTime[i]=coverTime[i]+1;

            }
            mean[i]=mean[i]/(numPeriods);
            coverTime[i]=coverTime[i]*100/(numPeriods);

            for (int k=0;k<numPeriods;k++)
               {double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
                stdev[i]=stdev[i]+Math.pow((mean[i]-thisHillsInfo.precipitation(i,currTime)),2);
                }
            stdev[i]=stdev[i]/(numPeriods);

        }

        newfile2.write("\n");
        newfile2.write("Mean Rain,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df2.format(mean[i])+",");
        }

        newfile2.write("\n");
        newfile2.write("Acum Rain,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df2.format(acum[i])+",");
        }

        newfile2.write("\n");
        newfile2.write("max Rain,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df2.format(max[i])+",");
        }


        newfile2.write("\n");
        newfile2.write("time to max,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df2.format(timemax[i])+",");
        }

        newfile2.write("\n");
        newfile2.write("coverage on time,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile2.write(df2.format(coverTime[i])+",");
        }
        System.out.println("comeca arquivo de chuva - parte 3!");
        newfile2.write("\n");
        newfile2.write("\n");
        newfile2.write("Statistics by time interval");
        newfile2.write("\n");
        /// Print Rainfall Statistics - in time
        double [] coverSpace=new double[numPeriods];
        double [] means=new double[numPeriods];
        double [] stdevs=new double[numPeriods];
        double [] maxs=new double[numPeriods];
        double [] acums=new double[numPeriods];
        double [] CMP=new double[numPeriods];
        double [] CMB=new double[numPeriods];

        for (int k=0;k<numPeriods;k++)
           {CMP[k]=0;
            CMB[k]=0;
            means[k]=0;
            acums[k]=0;
            coverSpace[k]=0;
            stdevs[k]=0;
            maxs[k]=0;}
System.out.println("passa primeiras linhas1");

        for (int k=0;k<numPeriods;k++)
           {double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();

            for (int i=0;i<linksStructure.contactsArray.length;i++){

               int xCon=linksStructure.contactsArray[i]%metaDatos.getNumCols();
               int yCon=linksStructure.contactsArray[i]/metaDatos.getNumCols();

               double dist=Math.pow(Math.pow((xCon-x),2)+Math.pow((yCon-y),2),0.5);
               
               CMP[k]=CMP[k]+dist*thisHillsInfo.precipitation(i,currTime);
               CMB[k]=CMB[k]+dist;
               means[k]=means[k]+thisHillsInfo.precipitation(i,currTime);
               acums[k]=acums[k]+thisHillsInfo.precipitation(i,currTime);

            if(maxs[k]<thisHillsInfo.precipitation(i,currTime)) maxs[k]=thisHillsInfo.precipitation(i,currTime);

            if(thisHillsInfo.precipitation(i,currTime)>0)  coverSpace[k]=coverSpace[k]+1;

            }
            means[k]=means[k]/(linksStructure.contactsArray.length);
            coverSpace[k]=coverSpace[k]*100/(linksStructure.contactsArray.length);

            for (int i=0;i<linksStructure.contactsArray.length;i++) stdevs[k]=stdevs[k]+Math.pow((means[k]-thisHillsInfo.precipitation(i,currTime)),2);
            stdevs[k]=Math.pow((stdevs[k]/(linksStructure.contactsArray.length)),0.5);

 }

        System.out.println("passa primeiras linhas2");
         newfile2.write("Time"+","+"Coverage"+","+"Mean"+","+"Stdev"+","+"Max"+","+"Acum"+","+"Prec_CM"+","+"basin_CM");
         newfile2.write("\n");
         for (int k=0;k<numPeriods;k++)
           {double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
     
            newfile2.write(df2.format(currTime)+","+df2.format(coverSpace[k])+","+df2.format(means[k])
            +","+df2.format(stdevs[k])+","+df2.format(maxs[k])+","+df2.format(acums[k])+","+df2.format(CMP[k])
                    +","+df2.format(CMB[k]));
            newfile2.write("\n");
         }

        System.out.println("arquivo de chuva - done2!!");
        newfile2.close();
        bufferout2.close();
        System.out.println("arquivo de chuva - done3!!");

        //ATTENTION
        //The followng print statement announces the completion of the program.
        //DO NOT modify!  It tells the queue manager that the process can be
        //safely killed.
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
            
            subMain0(args);
            //subMain1(args);  //The test case for Walnut Gulch 30m
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void subMain0(String args[]) throws java.io.IOException, VisADException {


        java.io.File theFile=new java.io.File(args[0]);
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

        int routingType=Integer.parseInt(args[6]);
        
        routingParams.put("lambda1",Float.parseFloat(args[7]));
        routingParams.put("lambda2",Float.parseFloat(args[8]));
        
        routingParams.put("v_o",Float.parseFloat(args[9]));
        
        stormFile=new java.io.File(args[10]);

        float infilRate=Float.parseFloat(args[11]);
        
        java.io.File outputDirectory=new java.io.File(args[12]);
        
        int[] connO=new int[0];
        float[] corrO=new float[0];
        
        if(!args[13].equalsIgnoreCase("C")){

            args[13]=args[13].substring(2);
            args[14]=args[14].substring(2);

            System.out.println(args[13]);
            System.out.println(args[14]);

            String[] conn=args[13].split(",");
            String[] corr=args[14].split(",");
            
            System.out.println(java.util.Arrays.toString(conn));
            System.out.println(java.util.Arrays.toString(corr));

            connO=new int[conn.length]; for (int i = 0; i < connO.length; i++) connO[i]=Integer.parseInt(conn[i].trim());
            corrO=new float[corr.length]; for (int i = 0; i < corrO.length; i++) corrO[i]=Float.parseFloat(corr[i].trim());
        }
        
        new TileSimulationToAsciiFileLuciana(Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]),matDirs,magnitudes,horOrders,metaModif,0.0f,0.0f,stormFile,null,infilRate,routingType,routingParams,outputDirectory,connO,corrO,Long.parseLong(args[15])).executeSimulation();
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
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
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        
        routingParams.put("v_o",0.5f);
        
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_02/precipitation_interpolated_ev02.metaVHC");
        
        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        zeroSimulationTime.set(1971,7, 18, 20, 00, 0);

        java.io.File outputDirectory=new java.io.File("/Users/ricardo/simulationResults/Parallel/WalnutGulch/");
        
        new TileSimulationToAsciiFileLuciana(729,130,-1,-1,4,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,stormFile,null,0.0f,2,routingParams,outputDirectory,new int[] {},new float[] {},zeroSimulationTime.getTimeInMillis()).executeSimulation();
        //new TileSimulationToAsciiFile(229,286,-1,-1,3,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,stormFile,null,0.0f,2,routingParams,outputDirectory,new int[] {314711, 315971},monitor,new float[] {0.027654171f, 0.20069313f}).executeSimulation();
            
    }

}
