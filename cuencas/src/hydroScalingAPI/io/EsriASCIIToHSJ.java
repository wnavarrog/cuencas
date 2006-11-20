package hydroScalingAPI.io;

import java.io.*;
import java.util.*;
import hydroScalingAPI.tools.*;

/**
 *
 * @author  Ricardo Mantilla
 * @author  Matt Luck
 */
public class EsriASCIIToHSJ extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows;
    private  String[][]       extensionPairs = {{"dem","metaDEM"},{"vhc","metaVHC"}};
    
    private  String[] parameters = {    "[Name]",
                                        "[Southernmost Latitude]",
                                        "[Westernmost Longitude]",
                                        "[Longitudinal Resolution (ArcSec)]",
                                        "[Latitudinal Resolution (ArcSec)]",
                                        "[# Columns]",
                                        "[# Rows]",
                                        "[Format]",
                                        "[Missing]",
                                        "[Temporal Resolution]",
                                        "[Units]",
                                        "[Information]"};
    /*Type 0 = DEM, 1= VHC*/
    public EsriASCIIToHSJ(java.io.File archivo, java.io.File salida, int type) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        ruta = new FileReader(archivo);
        buffer=new BufferedReader(ruta);
        
        for (int i=0;i<6;i++) {
            linea = buffer.readLine();
            tokens = new StringTokenizer(linea);
            
            basura = tokens.nextToken();
            variables[i] = tokens.nextToken();
            
        }
        
        columns = Integer.parseInt(variables[0]);
        rows = Integer.parseInt(variables[1]);
        
        matrix = new float[rows][columns];
        
        for (int i=0;i<rows;i++) {
            linea = buffer.readLine();
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;
                }
            }
        }
        buffer.close();
        
        fileName=archivo.getName();
        String fileBinSalida=salida.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+"."+extensionPairs[type][0];
        String fileAscSalida=salida.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+"."+extensionPairs[type][1];
        
        calculateMetadata(variables);
        newfilebinary(new java.io.File(fileBinSalida));
        newfileMetadata(new java.io.File(fileAscSalida));
    }
    
    private void newfilebinary(java.io.File archivo) throws java.io.IOException{
        
        java.io.FileOutputStream        salida;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        salida = new FileOutputStream(archivo);
        bufferout=new BufferedOutputStream(salida);
        newfile=new DataOutputStream(bufferout);
        
        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            newfile.writeFloat(matrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        salida.close();
    }
    
    private void newfileMetadata(java.io.File archivo) throws java.io.IOException{
        
        java.io.FileOutputStream        salida;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        salida = new FileOutputStream(archivo);
        bufferout=new BufferedOutputStream(salida);
        newfile=new OutputStreamWriter(bufferout);
        
        for (int i=0;i<12;i++) {
            newfile.write(parameters[i],0,parameters[i].length());
            newfile.write(""+retorno,0,1);
            newfile.write(metaInfo[i],0,metaInfo[i].length());
            newfile.write(""+retorno,0,1);
            newfile.write(""+retorno,0,1);
        }
        
        newfile.close();
        bufferout.close();
        salida.close();
        
    }
    
    private void calculateMetadata(String args[]) {
        
        String nombre, minlat, minlon, cols, rows, cellsize, formato, faltante, esctemp, unidades, info;
        
        metaInfo[0] = "This file was created imported from "+ fileName;
        
        minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[3]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
        metaInfo[1] = minlat;
        
        minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[2]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
        metaInfo[2] = minlon;
                
        cellsize = String.valueOf(3600 * (new Float(variables[4]).floatValue()));
        metaInfo[3] = cellsize;
        metaInfo[4] = cellsize;
        metaInfo[5] = variables[0];
        metaInfo[6] = variables[1];
        metaInfo[7] = "float";
        metaInfo[8] = variables[5];
        metaInfo[9] = "fix";
        metaInfo[10] = "N/A";
        metaInfo[11] = "Imported from Esri - Ascii format";
    }
    
    public static void main(String args[]) {
        
        try {
            new EsriASCIIToHSJ(new java.io.File("/home/matt/demtest/dem48states-1km.asc"),
            new java.io.File("/home/matt/demtest/"),0);
            
        } catch (Exception IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }
    
}