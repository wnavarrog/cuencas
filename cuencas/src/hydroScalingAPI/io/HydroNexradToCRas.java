/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.io;
import java.io.*;
/**
 *
 * @author pmandapa
 */

import java.io.*;
import java.util.*;
 
public class HydroNexradToCRas extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows; 
    
    public HydroNexradToCRas(java.io.File inputFile, java.io.File outputFile, int numCol, int numRow) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);         
        
        columns = numCol;
        rows = numRow;        
        matrix = new float[rows][columns];
        
        for (int i=0;i<11;i++) buffer.readLine();
        
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
        
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
                
        newfilebinary(new java.io.File(fileBinoutputDir));        
    }
    
    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
        
        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            newfile.writeFloat(matrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
    public static ArrayList<File> getFileList(File dir) throws FileNotFoundException{
        ArrayList<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        List<File> tempfiles = Arrays.asList(files);
        for(File file : files){
            result.add(file);
        }        
        return result;
    }
    
    public static void createMetaFile(File directory) {
        try{
            File saveFile=new File(directory.getPath()+File.separator+"prec.metaVHC");
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            writer.println("[Name]");
            writer.println("Precipitation Radar Data From KTLX");
            writer.println("[Southernmost Latitude]");
            writer.println("33:13:00.00 N");
            writer.println("[Westernmost Longitude]");
            writer.println("99:19:00.00 W");
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println("60");
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println("60");
            writer.println("[# Columns]");
            writer.println("305");
            writer.println("[# Rows]");
            writer.println("250");
            writer.println("[Format]");
            writer.println("Float");
            writer.println("[Missing]");
            writer.println("-99.00");
            writer.println("[Temporal Resolution]");
            writer.println("60-minutes");
            writer.println("[Units]");
            writer.println("mm");
            writer.println("[Information]");
            writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }
        
    }
  
        public static String Outfilename(String fileName) {
        System.out.println("fileName");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[5];
        timeStamp[0]=fileName.substring(32,36); // year
        timeStamp[1]=fileName.substring(29,32);  // month
        monthString=months[0];
        for (int ii=0;ii<=11;ii++)
        {        if(timeStamp[1].compareTo(months2[ii])==0)                 
                 {monthString=months[ii];}
        }
        
        timeStamp[2]=fileName.substring(27,29);  // day
        timeStamp[3]=fileName.substring(37,39); // hour 
        timeStamp[4]=fileName.substring(39,41); // min
//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        
        String vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
    public static void main(String[] args) throws java.io.IOException{
        
        java.io.File AsciiFile;
        File folder = new File("C:/CUENCAS/11140102/data/radar/event3");
        
 	try{
	ArrayList<File> files = HydroNexradToCRas.getFileList(folder);
	Iterator i = files.iterator();
        
        String OutputDir="C:/CUENCAS/11140102/data/radar/event3";
        createMetaFile(new java.io.File(OutputDir));
        
	while (i.hasNext()){
            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            AsciiFile = new java.io.File(FileAscIn);
            
  ///////////////////////////////
  /// Define the name of the output file          
        String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);
       System.out.println(temp.getAbsolutePath());           
        File BinaryOutName = new java.io.File(OutputDir+File.separator+Outfilename(fileName));
  ////////////////////////////////////////
            try {  
                 System.out.println(temp.getAbsolutePath());
                 new HydroNexradToCRas(AsciiFile,BinaryOutName,305,250);
             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
        }
        } catch (IOException e){
            System.err.println("problem creating file list:");
            e.printStackTrace();
        }
    }    
 }




