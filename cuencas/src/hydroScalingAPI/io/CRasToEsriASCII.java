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
 * CRasToEsriASCII.java
 *
 * Created on September 21, 2003, 2:01 PM
 */

package hydroScalingAPI.io;

/**
 * This class takes CUENCAS raster and creates a ESRI GRID raster inputFile, which can
 * be DEM or hydrometeorological fields.  IMPORTANT:  The Class will write the
 * GRID inputFile using a gloabal LAT-LONG WGS-84 projection.
 * @author Ricardo Mantilla
 */
public class CRasToEsriASCII {
    
    private hydroScalingAPI.io.MetaRaster myMetaInfo;
    private float[][] data;
    
    private java.io.File dirOut,fileName;
    
    /**
     * Creates a new instance of CRasToEsriASCII
     * @param inputFile The Raster file to be exported
     * @param outputDir The destination directory
     * @throws java.io.IOException Captures error in the read/write process
     */
    public CRasToEsriASCII(java.io.File inputFile, java.io.File outputDir) throws java.io.IOException{
        
        dirOut=outputDir;
        fileName=inputFile;
        
        myMetaInfo=new hydroScalingAPI.io.MetaRaster(fileName);
    }
    
    public void fileToExport(java.io.File inputFile) throws java.io.IOException{
        myMetaInfo.setLocationBinaryFile(inputFile);
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    public void fileToExport(hydroScalingAPI.io.MetaRaster thisMetaInfo) throws java.io.IOException{
        myMetaInfo=thisMetaInfo;
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    public void writeEsriFile() throws java.io.IOException{
        
        //Warning sign
        if(myMetaInfo.getResLat() != myMetaInfo.getResLon()){
            Object[] options = { "OK" };
            javax.swing.JOptionPane.showOptionDialog(null, "Warning: The grid resolution is different in the latitudinal and longitudinal directions, Esri ASCII does not support this kind of grid.", "Error", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE,null, options, options[0]);
            return;
        }
        
        String fileAscSalida=dirOut.getPath()+"/"+myMetaInfo.getLocationBinaryFile().getName()+".asc";
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        outputDir = new java.io.FileOutputStream(fileAscSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        int nc=myMetaInfo.getNumCols();
        int nr=myMetaInfo.getNumRows();
        
        float missing=Float.parseFloat(myMetaInfo.getMissing());
        
        newfile.write("ncols         "+myMetaInfo.getNumCols()+retorno);
        newfile.write("nrows         "+myMetaInfo.getNumRows()+retorno);
        newfile.write("xllcorner     "+myMetaInfo.getMinLon()+retorno);
        newfile.write("yllcorner     "+myMetaInfo.getMinLat()+retorno);
        newfile.write("cellsize      "+(myMetaInfo.getResLat()/3600.0D)+retorno);
        newfile.write("NODATA_value  "+myMetaInfo.getMissing()+retorno);

        
        for (int i=(nr-1);i>=0;i--) {
            for (int j=0;j<nc;j++) {
                if (data[i][j] == missing) {
                    newfile.write(myMetaInfo.getMissing()+" ");
                } else {
                    newfile.write(data[i][j]+" ");
                }
            }
            newfile.write(retorno);
        }
        
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            CRasToEsriASCII exporter=new CRasToEsriASCII(new java.io.File("C:/Documents and Settings/Administrator/My Documents/databases/Gila River DB/Rasters/Topography/mogollon.metaDEM"),
                                                       new java.io.File("/tmp/"));
            exporter.fileToExport(new java.io.File("C:/Documents and Settings/Administrator/My Documents/databases/Gila River DB/Rasters/Topography/mogollon.dem"));
            exporter.writeEsriFile();
            
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
