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


package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class DataRaster extends Object {

    private hydroScalingAPI.io.MetaRaster localMetaData;
    
    private java.io.FileInputStream dataPath;
    private java.io.BufferedInputStream dataBuffer;
    private java.io.DataInputStream dataDataStream;
    
    private byte[][] dataByte;
    private int[][] dataInteger;
    private float[][] dataFloat;
    private double[][] dataDouble;
    float[] FDA;
    double[] FDAD;
    int noFaltantes;
    
    /** Creates new dataRaster */
    public DataRaster(hydroScalingAPI.io.MetaRaster metaInfo) throws java.io.IOException{
        localMetaData=metaInfo;
                    
        dataPath=new java.io.FileInputStream(localMetaData.getLocationBinaryFile());
        dataBuffer=new java.io.BufferedInputStream(dataPath);
        dataDataStream=new java.io.DataInputStream(dataBuffer);
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte")){
            dataByte=new byte[localMetaData.getNumRows()][localMetaData.getNumCols()];
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) dataByte[i][j]=dataDataStream.readByte();
        }
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer")){
            dataInteger=new int[localMetaData.getNumRows()][localMetaData.getNumCols()];
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) dataInteger[i][j]=dataDataStream.readInt();
        }
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float")){
            dataFloat=new float[localMetaData.getNumRows()][localMetaData.getNumCols()];
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) dataFloat[i][j]=dataDataStream.readFloat();
        }
       
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double")){
            dataDouble=new double[localMetaData.getNumRows()][localMetaData.getNumCols()];
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) dataDouble[i][j]=dataDataStream.readDouble();
        }
        
        dataBuffer.close();
        dataDataStream.close();
    }
    
    public byte[][] getByte(){
        if (dataByte != null) return dataByte;
        
        byte[][] aDatosByte=new byte[localMetaData.getNumRows()][localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[i][j]=(byte) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[i][j]=(byte) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[i][j]=(byte) dataDouble[i][j];

        return aDatosByte;
    }
    
    public byte[][] getByteLine(){
        
        byte[][] aDatosByte=new byte[1][localMetaData.getNumRows()*localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[0][i*localMetaData.getNumCols()+j]=(byte) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[0][i*localMetaData.getNumCols()+j]=(byte) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[0][i*localMetaData.getNumCols()+j]=(byte) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosByte[0][i*localMetaData.getNumCols()+j]=(byte) dataDouble[i][j];

        return aDatosByte;
    }
    
    public int[][] getInt(){
        if (dataInteger != null) return dataInteger;
        
        int[][] aDatosInteger=new int[localMetaData.getNumRows()][localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[i][j]=(int) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[i][j]=(int) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[i][j]=(int) dataDouble[i][j];
        
        return aDatosInteger;
    }
    
    public int[][] getIntLine(){
        
        int[][] aDatosInteger=new int[1][localMetaData.getNumRows()*localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[0][i*localMetaData.getNumCols()+j]=(int) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[0][i*localMetaData.getNumCols()+j]=(int) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[0][i*localMetaData.getNumCols()+j]=(int) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosInteger[0][i*localMetaData.getNumCols()+j]=(int) dataDouble[i][j];

        return aDatosInteger;
    }
    
    public float[][] getFloat(){
        if (dataFloat!=null) return dataFloat;
        
        float[][] aDatosFloat=new float[localMetaData.getNumRows()][localMetaData.getNumCols()];;

        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[i][j]=(float) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[i][j]=(float) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[i][j]=(float) dataDouble[i][j];

        return aDatosFloat;
    }
    
    public float[][] getFloatLine(){
        
        float[][] aDatosFloat=new float[1][localMetaData.getNumRows()*localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[0][i*localMetaData.getNumCols()+j]=(float) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[0][i*localMetaData.getNumCols()+j]=(float) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[0][i*localMetaData.getNumCols()+j]=(float) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosFloat[0][i*localMetaData.getNumCols()+j]=(float) dataDouble[i][j];

        return aDatosFloat;
    }
    
    public float[][] getFloatLineEqualized(){
        
        float[][] aDatosFloat=getFloatLine();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDA=new float[statDatos.dataCount];
        int conDat=0;
        
        for (int j=0;j<aDatosFloat[0].length;j++) 
            if (aDatosFloat[0][j] != faltante){ 
                FDA[conDat]=aDatosFloat[0][j];
                conDat++;
            }

        java.util.Arrays.sort(FDA);
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[0][j])/(float) (statDatos.dataCount-1)*254);
            } else {
                aDatosFloat[0][j]=Float.NaN;
            }
        }

        return aDatosFloat;
    }
    
    public float[][] getFloatEqualized(){
        
        float[][] aDatosFloat=getFloat();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDA=new float[statDatos.dataCount];
        int conDat=0;
        for (int i=0;i<aDatosFloat.length;i++) {
            for (int j=0;j<aDatosFloat[i].length;j++) {
                if (aDatosFloat[i][j] != faltante){ 
                    FDA[conDat]=aDatosFloat[i][j];
                    conDat++;
                }
            }
        }

        java.util.Arrays.sort(FDA);
        for (int i=0;i<aDatosFloat.length;i++){
            for (int j=0;j<aDatosFloat[i].length;j++){
                if (aDatosFloat[i][j] != faltante){
                    aDatosFloat[i][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[i][j])/(float) (statDatos.dataCount-1)*254);
                } else {
                    aDatosFloat[i][j]=Float.NaN;
                }
            }
        }

        return aDatosFloat;
    }
    
    public float getEqualized(float f){
        try{
            if(new Float(localMetaData.getProperty("[Missing]")).floatValue() == f)
                return 0f;
            int i=0;
            while(FDA[i] < f)
                i++;
            return (1+i/(float)(noFaltantes-1)*254);
        }catch(Exception ex){
            return 255;
        }
    }
    
    public float[][] getFloatLineEqualized(float[] FDA){
        
        //FDA es un vector con datos ordendados, es util cuando se va a equalizar un mapa basado en un conjunto de mapas
        
        float[][] aDatosFloat=getFloatLine();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[0][j])/(float) (FDA.length-1)*254);
            } else {
                aDatosFloat[0][j]=0.0f;
            }
        }

        return aDatosFloat;
    }
    
    public double[][] getDouble(){
        if (dataDouble != null) return dataDouble;
        
        double[][] aDatosDouble=new double[localMetaData.getNumRows()][localMetaData.getNumCols()];;
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[i][j]=(double) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[i][j]=(double) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[i][j]=(double) dataFloat[i][j];
        
        return aDatosDouble;
    }
    
    public double[][] getDoubleLine(){
        
        double[][] aDatosDouble=new double[1][localMetaData.getNumRows()*localMetaData.getNumCols()];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[0][i*localMetaData.getNumCols()+j]=(double) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[0][i*localMetaData.getNumCols()+j]=(double) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[0][i*localMetaData.getNumCols()+j]=(double) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<localMetaData.getNumRows();i++) for(int j=0;j<localMetaData.getNumCols();j++) aDatosDouble[0][i*localMetaData.getNumCols()+j]=(double) dataDouble[i][j];

        return aDatosDouble;
    }
    
    /*public float getEqualized(double f){
        try{
            if(new Double(localMetaData.getProperty("[Missing]")).doubleValue() == f)
                return 0f;
            int i=0;
            while(FDA[i] < f)
                i++;
            return (1+i/(float)(noFaltantes-1)*254);
        }catch(Exception ex){
            return 255;
        }
    }
    
    public double[][] getDoubleEqualized(){
        
        double[][] aDatosFloat=getDouble();
        
        double faltante=new Double(localMetaData.getProperty("[Missing]")).doubleValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDAD=new double[statDatos.dataCount];
        int conDat=0;
        for (int i=0;i<aDatosFloat.length;i++) {
            for (int j=0;j<aDatosFloat[i].length;j++) {
                if (aDatosFloat[i][j] != faltante){ 
                    FDAD[conDat]=aDatosFloat[i][j];
                    conDat++;
                }
            }
        }

        java.util.Arrays.sort(FDAD);
        for (int i=0;i<aDatosFloat.length;i++){
            for (int j=0;j<aDatosFloat[i].length;j++){
                if (aDatosFloat[i][j] != faltante){
                    aDatosFloat[i][j]=(1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[i][j])/(double) (statDatos.dataCount-1)*254);
                } else {
                    aDatosFloat[i][j]=Double.NaN;
                }
            }
        }

        return aDatosFloat;
    }
    
    public double[][] getDoubleLineEqualized(){
        
        double[][] aDatosFloat=getDoubleLine();
        
        double faltante=new Float(localMetaData.getProperty("[Missing]")).doubleValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDAD=new double[statDatos.dataCount];
        int conDat=0;
        
        for (int j=0;j<aDatosFloat[0].length;j++) 
            if (aDatosFloat[0][j] != faltante){ 
                FDAD[conDat]=aDatosFloat[0][j];
                conDat++;
            }

        java.util.Arrays.sort(FDAD);
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(double) (1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[0][j])/(double) (statDatos.dataCount-1)*254);
            } else {
                aDatosFloat[0][j]=Double.NaN;
            }
        }

        return aDatosFloat;
    }
    
    public double[][] getDoubleLineEqualized(double[] FDAD){
        
        //FDAD es un vector con datos ordendados, es util cuando se va a equalizar un mapa basado en un conjunto de mapas
        
        double[][] aDatosFloat=getDoubleLine();
        
        double faltante=new Double(localMetaData.getProperty("[Missing]")).doubleValue();
        
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(double) (1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[0][j])/(double) (FDAD.length-1)*254);
            } else {
                aDatosFloat[0][j]=0.0f;
            }
        }

        return aDatosFloat;
    }*/

    public static void main (String args[]) {
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/randomField/randomMean100.metaVHC"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/randomField/randomMean100.180000.01.July.1971.vhc"));

            hydroScalingAPI.io.DataRaster datosRaster=new hydroScalingAPI.io.DataRaster (metaRaster1);
            float[][] theData=datosRaster.getFloat();
            for (int i=0;i<metaRaster1.getNumRows();i++) {
                for(int j=0;j<metaRaster1.getNumCols();j++) System.out.print(theData[i][j]+" ");
                System.out.println("");
            }
            
            System.gc();
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        System.exit(0);
    }

}
