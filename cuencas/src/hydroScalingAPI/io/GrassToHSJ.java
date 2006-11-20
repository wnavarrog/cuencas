package hydroScalingAPI.io;

import java.io.*;
import java.util.*;

/**
 *
 * @author  Ricardo Mantilla
 * @author  Matt Luck
 */
public class GrassToHSJ extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows;
    
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
    
    
    
    public GrassToHSJ(java.io.File archivo, java.io.File salida) throws java.io.IOException{
        
        java.io.FileReader         ruta;
        java.io.BufferedReader     buffer;

        java.util.StringTokenizer       tokens;             
        String                          linea=null,
                                        basura,nexttoken;
        
        ruta = new FileReader(archivo);
        buffer=new BufferedReader(ruta);
        
        linea = buffer.readLine();
        tokens = new StringTokenizer(linea);

        basura = tokens.nextToken();
        
        if (basura.equalsIgnoreCase("proj:")){
            
            variables[0] = tokens.nextToken(); 
        
            for (int i=1;i<8;i++) {
                linea = buffer.readLine();
                tokens = new StringTokenizer(linea);

                basura = tokens.nextToken();
                variables[i] = tokens.nextToken();
            }
            
            columns = Integer.parseInt(variables[6]);
            rows = Integer.parseInt(variables[7]);
            
        } else {
            
            variables[2] = tokens.nextToken(); 
            
            for (int i=3;i<8;i++) {
                linea = buffer.readLine();
                tokens = new StringTokenizer(linea);

                basura = tokens.nextToken();
                variables[i] = tokens.nextToken();
            }
            
            columns = Integer.parseInt(variables[7]);
            rows = Integer.parseInt(variables[6]);
            
        }
            
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
        String fileBinSalida=salida.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+".dem";
        String fileAscSalida=salida.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+".metaDEM";
        
        calculaMetadatos(variables);
        newFileBinary(new java.io.File(fileBinSalida));
        newFileMetaInfo(new java.io.File(fileAscSalida));
    }
    
    private void newFileBinary(java.io.File archivo) throws java.io.IOException{
        
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
    
    private void newFileMetaInfo(java.io.File archivo) throws java.io.IOException{
        
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
    
    private void calculaMetadatos(String args[]) {
               
        String      nombre,minlat,minlon,esclat,esclon,
                    formato,faltante,esctemp,unidades,info;
        
        metaInfo[0] = "This file was created imported from "+ fileName;
        
        minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[3]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
        metaInfo[1] = minlat;
        
        minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[2]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
        metaInfo[2] = minlon;
                
        esclon = String.valueOf(3600*(Float.parseFloat(variables[4])-Float.parseFloat(variables[5]))/(float)columns);
        metaInfo[3] = esclon;
        
        esclat = String.valueOf(3600*(Float.parseFloat(variables[2])-Float.parseFloat(variables[3]))/(float)rows);
        metaInfo[4] = esclat;
        
        metaInfo[5] = variables[7];       
        metaInfo[6] = variables[6];
        metaInfo[7] = "float";
        metaInfo[8] = "-9999";
        metaInfo[9] = "fix";
        metaInfo[10] = "N/A";
        metaInfo[11] = "Imported from Grass format";
    }
    
    public static void main (String args[]) {
        
        try{
            new GrassToHSJ(new java.io.File("/Documents and Settings/ricardo/My Documents/temp/spearfish.grass"),
                           new java.io.File("/Documents and Settings/ricardo/My Documents/temp/"));
            
            /*new GrassToHSJ(new java.io.File("/home/ricardo/garbage/testsGrass/1630327a.grass"),
                           new java.io.File("/home/ricardo/garbage/testsGrass/"));*/
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }

}