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
 * HSJToGrass.java
 *
 * Created on September 21, 2003, 2:01 PM
 */

package hydroScalingAPI.io;

/**
 *
 * @author Ricardo Mantilla
 */
public class HSJToGrass {
    
    private hydroScalingAPI.io.MetaRaster myMetaInfo;
    private float[][] data;
    
    private java.io.File dirOut,fileName;
    
    /** Creates a new instance of HSJToGrass */
    public HSJToGrass(java.io.File file, java.io.File salida) throws java.io.IOException{
        
        dirOut=salida;
        fileName=file;
        
        myMetaInfo=new hydroScalingAPI.io.MetaRaster(fileName);
    }
    
    public void fileToExport(java.io.File file) throws java.io.IOException{
        myMetaInfo.setLocationBinaryFile(file);
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    public void fileToExport(hydroScalingAPI.io.MetaRaster thisMetaInfo) throws java.io.IOException{
        myMetaInfo=thisMetaInfo;
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    public void writeGrassFile() throws java.io.IOException{
        
        String fileAscSalida=dirOut.getPath()+"/"+myMetaInfo.getLocationBinaryFile().getName()+".grass";
        
        java.io.FileOutputStream        salida;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        salida = new java.io.FileOutputStream(fileAscSalida);
        bufferout=new java.io.BufferedOutputStream(salida);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        int nc=myMetaInfo.getNumCols();
        int nr=myMetaInfo.getNumRows();
        
        float missing=Float.parseFloat(myMetaInfo.getMissing());
        
        newfile.write("north: "+myMetaInfo.getMaxLat()+retorno);
        newfile.write("south: "+myMetaInfo.getMinLat()+retorno);
        newfile.write("east: "+myMetaInfo.getMaxLon()+retorno);
        newfile.write("west: "+myMetaInfo.getMinLon()+retorno);
        newfile.write("rows: "+myMetaInfo.getNumRows()+retorno);
        newfile.write("cols: "+myMetaInfo.getNumCols()+retorno);

        
        for (int i=(nr-1);i>=0;i--) {
            for (int j=0;j<nc;j++) {
                if (data[i][j] == missing) {
                    newfile.write("* ");
                } else {
                    newfile.write(data[i][j]+" ");
                }
            }
            newfile.write(retorno);
        }
        
        newfile.close();
        bufferout.close();
        salida.close();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            HSJToGrass exporter=new HSJToGrass(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.metaDEM"),
                                               new java.io.File("/home/ricardo/temp/"));
            exporter.fileToExport(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.dem"));
            exporter.writeGrassFile();
            
            /*new GrassToHSJ(new java.io.File("/home/ricardo/garbage/testsGrass/1630327a.grass"),
                           new java.io.File("/home/ricardo/garbage/testsGrass/"));*/
           
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
}
