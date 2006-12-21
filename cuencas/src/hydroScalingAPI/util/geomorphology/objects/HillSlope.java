/*
 * hillSlope.java
 *
 * Created on November 13, 2001, 11:25 AM
 */

package hydroScalingAPI.util.geomorphology.objects;

/**
 *
 * @author  Ricardo Mantilla 
 */
public class HillSlope extends java.lang.Object {
    
    private java.util.Vector idsHill=new java.util.Vector(), dToOutlet=new java.util.Vector();
    
    private int[][] xyHillSlope;
    private float[][] LonLatHillSlope;
    private float[][] xyContorno;
    private int minX,maxX,minY,maxY;
    private int thisLinkMagn;
    private hydroScalingAPI.io.MetaRaster localMetaRaster;

    /** Creates new hillSlope */
    public HillSlope(int x, int y, byte[][] fullDirMatrix, int[][] magnitudes, hydroScalingAPI.io.MetaRaster mr) {
        
        //assumes unpruned directions matrix
        
        localMetaRaster=mr;
        
        minX=fullDirMatrix[0].length-1;
        maxX=0;
        minY=fullDirMatrix.length-1;
        maxY=0;
        
        thisLinkMagn=magnitudes[y][x];
        //System.out.println("Limits: "+magnitudes.length+" "+magnitudes[0].length);
        //System.out.println("Hill Outlet: "+x+" "+y+" "+thisLinkMagn);
        
        idsHill.add(new int[] {x,y});
        getHill(y,x,fullDirMatrix,magnitudes);
        
        LonLatHillSlope=new float[2][idsHill.size()];
        xyHillSlope=new int[2][idsHill.size()];
        
        for (int k=0; k <idsHill.size(); k++){
            int[] xys=(int[]) idsHill.elementAt(k);
            LonLatHillSlope[0][k]=(float) (xys[0]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            xyHillSlope[0][k]=xys[0];
            minX=Math.min(minX,xys[0]); maxX=Math.max(maxX,xys[0]); 
            LonLatHillSlope[1][k]=(float) (xys[1]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
            xyHillSlope[1][k]=xys[1];
            minY=Math.min(minY,xys[1]); maxY=Math.max(maxY,xys[1]); 
        }
        
        idsHill=null;
    }
    
    public void getHill(int i,int j,byte [][] fullDirMatrix,int[][] magnitudes){
        for (int k=0; k <= 8; k++){
           if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && (magnitudes[i+(k/3)-1][j+(k%3)-1] == thisLinkMagn || magnitudes[i+(k/3)-1][j+(k%3)-1]<=0)){
             idsHill.add(new int[] {j+(k%3)-1,i+(k/3)-1});
             //System.out.println("   Now Asks to: "+j+" "+i+" "+magnitudes[i][j]);
             //System.out.println("       Following: "+(j+(k%3)-1)+" "+(i+(k/3)-1));//+" "+magnitudes[i+(k/3)-1][+(k%3)-1]);
             getHill(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,magnitudes); 
           }
        }
    }
    
    public float[][] getLonLatHillSlope(){
        return LonLatHillSlope;
    }
    
    public int[][] getXYHillSlope(){
        return xyHillSlope;
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulch.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulch.dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulch.magn"));
            metaModif.setFormat("Integer");
            int [][] matMagns=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            
            hydroScalingAPI.util.geomorphology.objects.HillSlope theHillSlope=new hydroScalingAPI.util.geomorphology.objects.HillSlope(186,120,matDirs,matMagns,metaModif);
            
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }

}
