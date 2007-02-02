/*
 * createLaplacianFromDEM.java
 *
 * Created on January 30, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.artifitialFields;

/**
 *
 * @author ricardo
 */
public class createLaplacianFromDEM {
    
    /** Creates a new instance of createLaplacianFromDEM */
    public createLaplacianFromDEM(java.io.File metaFile, java.io.File outputDir) throws java.io.IOException{
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaFile);
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".corrDEM"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        float[][] DEM=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".dir"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        byte[][] DIR=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".magn"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".magn"));
        int[][] MAG=new hydroScalingAPI.io.DataRaster(metaData).getInt();
        
        float[][] LAP=new float[DEM.length][DEM[0].length];
        
        float counterZeros=0;
        
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(DIR[i][j] == 1 || DIR[i][j] == 3 ||DIR[i][j] == 7 ||DIR[i][j] == 9)
                LAP[i][j]=(DEM[i-1][j]-2*DEM[i][j]+DEM[i+1][j])/30.0f+(DEM[i][j-1]-2*DEM[i][j]+DEM[i][j+1])/30.0f;
            else
                LAP[i][j]=(DEM[i-1][j-1]-2*DEM[i][j]+DEM[i+1][j+1])/30.0f+(DEM[i-1][j+1]-2*DEM[i][j]+DEM[i+1][j-1])/30.0f;
            if(Math.abs(LAP[i][j]) < 1e-4) {
                LAP[i][j]=0;
                counterZeros++;
            }
            if(MAG[i][j] > 0) LAP[i][j]=1;
        }
        System.out.println(counterZeros+" "+(counterZeros/(float)(DEM.length*DEM[0].length)));
        hydroScalingAPI.io.MetaRaster metaOut=new hydroScalingAPI.io.MetaRaster(metaData);
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_LAPLACIAN.metaVHC"));
        java.io.File saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_LAPLACIAN.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(LAP[i][j]);
        }
        
        writer.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new createLaplacianFromDEM(new java.io.File("/Users/ricardo/Documents/databases/Test_DB/Rasters/Topography/58447060.metaDEM"),
                                       new java.io.File("/Users/ricardo/Documents/databases/Test_DB/Rasters/Hydrology/"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}
