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
public class RsnWFSToAsciiFile extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnStructure rsns;
    float rainIntensity;
    float rainDuration;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    
    hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnLinksAnalysis linksStructure;
    int basinOrder;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan;

    public RsnWFSToAsciiFile(hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR) throws java.io.IOException, VisADException{
        this(rsns_OR, rainIntensity_OR, rainDuration_OR, infiltRate_OR, routingType_OR, outputDirectory_OR,0.5f,-0.5f);
    }
    
    /** Creates new simulationsRep3 */
    public RsnWFSToAsciiFile(hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR,float exponentQ, float exponentA) throws java.io.IOException, VisADException{
        rsns=rsns_OR;
        rainIntensity=rainIntensity_OR;
        rainDuration=rainDuration_OR;
        infiltRate=infiltRate_OR;
        routingType=routingType_OR;
        outputDirectory=outputDirectory_OR;
        
        linksStructure=new hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnLinksAnalysis(rsns);
        basinOrder=linksStructure.getBasinOrder();
        
        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisNetworkGeom.setWidthsHG(3.4f, 0.5f, 0.2f);
        thisNetworkGeom.setSlopesHG(0.02f, -0.5f, 0.3f);
        thisNetworkGeom.setCheziHG(14.2f, -1/3.0f);
        
        thisNetworkGeom.setVqParams((float)(1.0/Math.pow(0.01, exponentA)), 0.0f, exponentQ, exponentA);

        thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        thisHillsInfo.setStormManager(storm);
        
        infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        thisHillsInfo.setInfManager(infilMan);
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
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
        String demName="RSN_result";
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
        
        java.io.File theFile1;
        
//        theFile1=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".rsn.csv");
//        System.out.println("Writing RSN Decoding - "+theFile1);
//        rsns.writeRsnTreeDecoding(theFile1);
//        System.out.println("Done writing results");
        
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs.csv");
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs=linksStructure.getTopologicWidthFunctions(linksStructure.completeStreamLinksArray);
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1)){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }
        
        newfile.close();
        bufferout.close();

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            subMain1(args);   //Geometrically Distributed generators
            
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
        
        String outDir="/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/widthFunctions/geometricRSNs/";
        
        int iniExperiment=1;
        int finExperiments=999;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
                
        for(double p_i=0.36;p_i<0.50;p_i+=0.04){
            for(double p_e=0.45;p_e<0.55;p_e+=0.04){
                
                String fileString=outDir+"/p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e);
                new java.io.File(fileString).mkdir();
                
                for(int sofi=2;sofi<=7;sofi++){

                    fileString=outDir+"/p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi;

                    new java.io.File(fileString).mkdir();

                    for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(p_i,0);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(p_e,1);
                        hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnStructure myRSN=new hydroScalingAPI.modules.rsnFlowSymulations.objects.RsnStructure(sofi-1,myUD_I,myUD_E);
                        new RsnWFSToAsciiFile(myRSN,0,1,experiment,5,new java.io.File(fileString)).executeSimulation();

                    }
                }
            }
        }
    }
        
}
