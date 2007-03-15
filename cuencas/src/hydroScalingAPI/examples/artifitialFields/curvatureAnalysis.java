/*
 * curvatureAnalysis.java
 *
 * Created on March 13, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.artifitialFields;

import ij.util.Java2;


/**
 *
 * @author jesusgomez
 */


public class curvatureAnalysis {

    
public int xB, yB;
public hydroScalingAPI.io.MetaRaster  metaOrig;
public hydroScalingAPI.io.DataRaster  dataOrig;
public hydroScalingAPI.util.geomorphology.objects.Basin basinOrig;   
public java.util.Vector hypsoData;
public String path;

public float[][] DEM;
public byte[][] DIR;
public int[][] MAG;
public float[][] GDO;
public float[][] dToNearChannel;
public float Dmn = -1;
public float Dmh = -1;
public float Hmn = -1;
public float Hmh = -1;
    
    
    /** Creates a new instance of curvatureAnalysis */
    public curvatureAnalysis(int xBasin, int yBasin, hydroScalingAPI.io.MetaRaster metaDEM_Orig, hydroScalingAPI.io.DataRaster dataDEM_Orig)throws java.io.IOException {
        
        metaOrig = metaDEM_Orig;
        dataOrig = dataDEM_Orig;
        xB = xBasin;
        yB = yBasin;
        hypsoData = new java.util.Vector();
        
        new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(metaOrig, dataOrig);
        
        java.io.File fileMeta =  metaOrig.getLocationMeta();
        path = fileMeta.getPath().substring(0,fileMeta.getPath().lastIndexOf("."));

        getInitialParameters();
        
      
    }
    
    public void getInitialParameters()throws java.io.IOException{
    
        //Read DEM derived maps
        
        metaOrig.setLocationBinaryFile(new java.io.File(path+".corrDEM"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        DEM = new hydroScalingAPI.io.DataRaster(metaOrig).getFloat();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".dir"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        DIR = new hydroScalingAPI.io.DataRaster(metaOrig).getByte();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".magn"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".magn"));
        MAG = new hydroScalingAPI.io.DataRaster(metaOrig).getInt();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".gdo"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".gdo"));
        GDO = new hydroScalingAPI.io.DataRaster(metaOrig).getFloat();
        
        basinOrig = new hydroScalingAPI.util.geomorphology.objects.Basin(xB,yB,DIR,metaOrig); 
        java.util.Vector tmp = new java.util.Vector();
        tmp.addElement(-1);
        tmp.addElement(basinOrig.getHypCurve());
        hypsoData.addElement(tmp);
        
        
         //Calculates distance to nearest stream
        dToNearChannel=new float[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0){
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                dToNearChannel[i][j]=GDO[i][j]-GDO[iPn][jPn];
                
            }
                
        }
        
        getConstrainsTopography();    
        
    }
    
    
    public void getSyntheticDEM(float b1,float b2){
    
        //Calculates a synthetic DEM
        //b1 Network
        //b2 Hillslope
        
        float[][] fakeDEM=DEM.clone();
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0){
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                fakeDEM[i][j]=(float)(Hmh*Math.pow(dToNearChannel[i][j]/Dmh,b2))+(float)(Hmn*Math.pow(GDO[iPn][jPn]/Dmn,b1));
            } else{
                fakeDEM[i][j]=(float)(Hmn*Math.pow(GDO[i][j]/Dmn,b1));;
            }
        }
        
        java.io.File out = new java.io.File(path);
        String folderName = "b1_"+Float.toString(b1)+"_b2_"+Float.toString(b2);
        out.mkdir();
        
//        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_FakeDEM.metaDEM"));
//        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_FakeDEM.dem");
//        metaOut.setLocationBinaryFile(saveFile);
//        metaOut.setFormat("Float");
//        metaOut.writeMetaRaster(metaOut.getLocationMeta());
//        
//        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
//        
//        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
//            writer.writeFloat(fakeDEM[i][j]);
//        }
//        
//        writer.close();
    }
    
    public void getConstrainsTopography()throws java.io.IOException{
        
        byte [][] maskNet = basinOrig.getNetworkMask();
        byte [][] maskHill = basinOrig.getBasinMask();
        
        int ncol = metaOrig.getNumCols(); 
        int nrow = metaOrig.getNumRows();
        
        
        for(int i = 0; i<nrow; i++){
            for(int j = 0; j<ncol; j++){
                
                if(MAG[i][j]!=-1 && GDO[i][j]>Dmn)
                    Dmn = GDO[i][j];

                if(MAG[i][j]!=-1 && DEM[i][j]>Hmn)
                    Hmn = DEM[i][j];
                
                if(MAG[i][j]==-1 && dToNearChannel[i][j]>Dmh)
                    Dmh = dToNearChannel[i][j];

                if(MAG[i][j]==-1 && DEM[i][j]>Hmn)
                    Hmn = DEM[i][j];
            
            }
        
        }
        
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            
            args=new String[] { "/Users/jesusgomez/Documents/ensayo/walnutGulchUpdated.metaDEM",
                                 "/Users/jesusgomez/Documents/ensayo/walnutGulchUpdated.dem"};
            
            hydroScalingAPI.io.MetaRaster metaRaster= new hydroScalingAPI.io.MetaRaster(new java.io.File(args[0]));
            metaRaster.setLocationBinaryFile(new java.io.File(args[1]));
            hydroScalingAPI.io.DataRaster datosRaster = new hydroScalingAPI.io.DataRaster(metaRaster);

            new curvatureAnalysis(194, 281,metaRaster,datosRaster);
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
}   
