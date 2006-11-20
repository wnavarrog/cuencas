/*
 * HSJToEsriASCII.java
 *
 * Created on September 21, 2003, 2:01 PM
 */

package hydroScalingAPI.io;

/**
 *
 * @author  ricardo
 */
public class HSJToEsriASCII {
    
    private hydroScalingAPI.io.MetaRaster myMetaInfo;
    private float[][] data;
    
    private java.io.File dirOut,fileName;
    
    /** Creates a new instance of HSJToEsriASCII */
    public HSJToEsriASCII(java.io.File file, java.io.File salida) throws java.io.IOException{
        
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
    
    public void writeEsriFile() throws java.io.IOException{
        
        //Warning sign
        if(myMetaInfo.getResLat() != myMetaInfo.getResLon()){
            Object[] options = { "OK" };
            javax.swing.JOptionPane.showOptionDialog(null, "Warning: The grid resolution is different in the latitudinal and longitudinal directions, Esri ASCII does not support this kind of grid.", "Error", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE,null, options, options[0]);
            return;
        }
        
        String fileAscSalida=dirOut.getPath()+"/"+myMetaInfo.getLocationBinaryFile().getName()+".asc";
        
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
        salida.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            HSJToEsriASCII exporter=new HSJToEsriASCII(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.metaDEM"),
                                                       new java.io.File("/tmp/"));
            exporter.fileToExport(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dem"));
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
