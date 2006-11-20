/*
 * kansasWidthsAnalysis.java
 *
 * Created on April 20, 2004, 11:06 AM
 */

package hydroScalingAPI.examples.dataAnalysis;

import visad.*;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;

/**
 *
 * @author  ricardo
 */
public class kansasWidthsAnalysis {
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager localInfoManager;
    private hydroScalingAPI.util.database.DataBaseEngine localDatabaseEngine;
    private hydroScalingAPI.mainGUI.objects.LocationsManager localLocationsManager;
    private hydroScalingAPI.io.BasinsLogReader associatedBasins;
    private hydroScalingAPI.io.MetaRaster metaData;
    private byte[][] dirMatrix;
    private RealType distanceToOut,numLinks;
    private FunctionType func_distanceToOut_numLinks;
    
    /** Creates a new instance of kansasWidthsAnalysis */
    public kansasWidthsAnalysis() {
        
        //Load Whitewater database
        localInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(new java.io.File("/hidrosigDataBases/Whitewater_database"));
        localDatabaseEngine=new hydroScalingAPI.util.database.DataBaseEngine();
        localLocationsManager=new hydroScalingAPI.mainGUI.objects.LocationsManager(localInfoManager,localDatabaseEngine);
        
        while(!localLocationsManager.isLoaded()){
            new visad.util.Delay(1000);
            System.out.print("/");
        }
        System.out.println("/");
        
        try{
            
            metaData=new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM"));
            metaData.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dir"));
            metaData.setFormat("Byte");

            dirMatrix=new hydroScalingAPI.io.DataRaster(metaData).getByte();
            
            associatedBasins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.log"));
            String[] basinsToAnalyze=associatedBasins.getPresetBasins();
            
            for(int i=0;i<basinsToAnalyze.length;i++){
                String[] cooLabel=basinsToAnalyze[i].split("; Basin Code");
                hydroScalingAPI.io.MetaLocation locationInfo=localLocationsManager.getLocation(cooLabel[1].trim(),"Hydraulic Geometry Measurement");
                if(locationInfo != null) {
                    
                    String[] siteInformation=locationInfo.getInformation().split("\n");
                    String siteWidthStr=siteInformation[1].split("\t")[1].split(" ")[0];
                    
                    float siteWidth=Float.parseFloat(siteWidthStr.trim());
                    
                    String siteDepthStr=siteInformation[2].split("\t")[1].split(" ")[0];
                    
                    float siteDepth=Float.parseFloat(siteDepthStr.trim());
                    
                    int basinOutletX=Integer.parseInt(cooLabel[0].split(", ")[0].split(" ")[1]);
                    int basinOutletY=Integer.parseInt(cooLabel[0].split(", ")[1].split(" ")[1]);
                    
                    hydroScalingAPI.util.geomorphology.objects.Basin thisBasin=new hydroScalingAPI.util.geomorphology.objects.Basin(basinOutletX,basinOutletY,dirMatrix,metaData);
                    hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(thisBasin, metaData, dirMatrix);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
                    
                    int metric=1;
                    float binsize=1.0f;
                    
                    float[][] wFunc=linksStructure.getDistancesToOutlet();
                    java.util.Arrays.sort(wFunc[metric]);

                    float[][] gWFunc=new float[1][wFunc[metric].length];

                    for (int k=0;k<wFunc[0].length;k++)
                        gWFunc[0][k]=(float) wFunc[metric][k];
                    
                    distanceToOut = RealType.getRealType("distanceToOut");
                    numLinks= RealType.getRealType("numLinks");
                    func_distanceToOut_numLinks= new FunctionType(distanceToOut, numLinks);

                    FlatField vals_ff_W = new FlatField( func_distanceToOut_numLinks, new Linear1DSet(distanceToOut,1,gWFunc[0].length,gWFunc[0].length));
                    vals_ff_W.setSamples( gWFunc );

                    int numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;

                    Linear1DSet binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);
                    FlatField hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);

                    double[][] laWFunc=hist.getValues();
                    
                    float twfMax=new hydroScalingAPI.tools.Stats(laWFunc[0]).maxValue;
                    
                    metric=0;
                    float[][] varValues=linksStructure.getVarValues(1);
                    binsize=new hydroScalingAPI.tools.Stats(varValues).meanValue;
                    
                    wFunc=linksStructure.getDistancesToOutlet();
                    java.util.Arrays.sort(wFunc[metric]);

                    gWFunc=new float[1][wFunc[metric].length];

                    for (int k=0;k<wFunc[0].length;k++)
                        gWFunc[0][k]=(float) wFunc[metric][k];
                    
                    vals_ff_W = new FlatField( func_distanceToOut_numLinks, new Linear1DSet(distanceToOut,1,gWFunc[0].length,gWFunc[0].length));
                    vals_ff_W.setSamples( gWFunc );

                    numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;

                    binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);
                    hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);

                    laWFunc=hist.getValues();
                    
                    float gwfMax=new hydroScalingAPI.tools.Stats(laWFunc[0]).maxValue;
                    
                    
                    System.out.println(cooLabel[1].trim()+","+linksStructure.basinMagnitude+","+thisNetworkGeom.basinArea()+","+twfMax+","+gwfMax+","+siteWidth+","+siteDepth);
                    
                    
                }
            }
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        } catch (VisADException v){
            System.out.print(v);
        }
        
        
            
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new kansasWidthsAnalysis();
    }
    
}
