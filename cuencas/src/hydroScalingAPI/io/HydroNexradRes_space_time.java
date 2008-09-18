/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// This program change the resolution of NEXRAD (ASCII) data
// the initial spatial resolution is 60 sec = 1 min
// the initial temporal resolution is 15 min
// the goal is evaluate the influence of rainfall data resolution in peak flow forecast
// 
package hydroScalingAPI.io;
import java.io.*;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
/**
 *
 * @author pmandapa
 */

import java.util.*;
 
public class HydroNexradRes_space_time extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  float[][]        Finalmatrix;
    private  int              columns,rows; 
    private  int              Finalcolumns,Finalrows; 
    private  float            IniResolution,FinalResolution; 
    
    public HydroNexradRes_space_time(java.io.File inputFile, java.io.File outputFile, java.io.File outputFileASC, int numRow, int numCol, float IniRes, float FinalRes,int Frow,int Fcol) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);         
        columns = numCol;
        rows = numRow;        
        IniResolution=IniRes;
        FinalResolution=FinalRes;
        matrix = new float[rows][columns];
        Finalcolumns = Fcol;
        Finalrows =  Frow;
        Finalmatrix = new float[Finalrows][Finalcolumns];
        // skip the headlines

        for (int i=0;i<11;i++) linea = buffer.readLine();         
        
  
        for (int i=0;i<rows;i++) {
            
            linea = buffer.readLine();
       
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{

                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;
                    System.out.println("exception");
                }
 
            }
        }
        buffer.close();
        float resolutitime=FinalResolution/IniResolution;
        int nInt= (int) java.lang.Math.round(resolutitime);
        
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
        String fileASCDir=outputFileASC.getAbsolutePath();
                
        
        float value = 0;
        int sum = 0;
       // System.out.println("CALCULATION PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
        int firstelemrow,firstelemcolumn;
        for (int i=0;i<Finalrows;i++) {
           for (int j=0;j<Finalcolumns;j++) { 
               firstelemrow=nInt*i;
               firstelemcolumn=nInt*j;
              // System.out.println("firstelemrow="+firstelemrow+"firstelemcolumn"+firstelemcolumn); 
               if(firstelemrow>(rows-nInt))firstelemrow=rows-nInt;
               if(firstelemcolumn>(columns-nInt))firstelemcolumn=columns-nInt;
               for (int k=firstelemrow;k<firstelemrow+nInt;k++) {
                   for (int l=firstelemcolumn;l<firstelemcolumn+nInt;l++) {
        //System.out.println("k = "+k+" and l = "+l+"matrix[k][l]= "+matrix[k][l]); 
                     if (matrix[k][l]>=0) 
                     {value = value + matrix[k][l];
                     sum=sum+1;
                     }
                   }
               }
          //     System.out.println("value = "+value+"sum = "+sum); 
               
               if(sum!=0) Finalmatrix[i][j]=value/sum;
               else Finalmatrix[i][j]=-99;
               //System.out.println("problem"); 
               sum = 0;
               value=0;
           }
        }
        
        //System.out.println("Create the matrix data"); 
        newfilebinary(new java.io.File(fileBinoutputDir));   
        newfileASC(new java.io.File(fileASCDir));   
    }
    
    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
 
      for (int i=Finalrows-1;i>-1;i--) for (int j=0;j<Finalcolumns;j++) {
            newfile.writeFloat(Finalmatrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
        private void newfileASC(java.io.File AscFile) throws java.io.IOException{
        
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

           
        newfile.write("ncols "+Finalcolumns+retorno);
        newfile.write("nrows "+Finalrows+retorno);
        newfile.write("xllcorner "+"-97.63333"+retorno);// basin mode
        newfile.write("yllcorner "+"37.300000"+retorno);//basin mode
        //newfile.write("xllcorner "+"-100.066667"+retorno);// radar mode
        //newfile.write("yllcorner "+"35.600000"+retorno);//radar mode
        int cellsize = (int) java.lang.Math.round(FinalResolution/60);
        newfile.write("cellsize "+cellsize+retorno);
        newfile.write("NODATA_value  "+"-99.0"+retorno);
        
     //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
           for (int i=0;i<Finalrows;i++) {
            for (int j=0;j<Finalcolumns;j++) {
                
                    newfile.write(Finalmatrix[i][j]+" ");
                }           
            newfile.write(retorno);
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
    
    public static void createMetaFile(File directory,int newsresol,int newtresol,int Finalrows,int Finalcolumns) {
        try{
            File saveFile=new File(directory.getPath()+File.separator+"/bin/"+"prec.metaVHC");
           // String F=directory.getPath()+File.separator+"/bin/"+"prec.metaVHC";
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
           // System.out.println("file = "+ F);
           // System.out.println("in metafile function new_s_res = "+ newsresol+"new_t_res = "+newtresol+"Frows = "+Finalrows + "Fcolumns = "+Finalcolumns);
            writer.println("[Name]");
            writer.println("Precipitation Radar Data From KICT - basin mode");
            //writer.println("Precipitation Radar Data From KICT");
            //writer.println("Precipitation Radar Data From KTLX");
            //writer.println("Precipitation Radar Data From KINX");
            writer.println("[Southernmost Latitude]");
            writer.println("37:38:00.00 N"); // KICT radar
            //writer.println("35:36:00.00 N"); // KICT radar
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");
            writer.println("97:18:00.00 W"); // KICT radar
            //writer.println("100:04:00.00 W"); // KICT radar
            //writer.println("99:19:00.00 W"); // KTLX radar
            //writer.println("98:08:00.00 W");// KINX radar
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println(newsresol);
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println(newsresol);
            writer.println("[# Columns]");
            //writer.println("305");// KTLX radar
            writer.println(Finalcolumns);// KINX radar
            writer.println("[# Rows]");
            //writer.println("250");// KTLX radar
            writer.println(Finalrows);// KINX radar
            writer.println("[Format]");
            writer.println("Float");
            writer.println("[Missing]");
            writer.println("-99.00");
            writer.println("[Temporal Resolution]");
            writer.println(newtresol+"-minutes");
            writer.println("[Units]");
            writer.println("mm");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
            writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }
        
    }
  
         public static String Outfilename(String fileName, String type) {
        //System.out.println("fileName");
            //System.out.println(fileName);
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
        //System.out.println(" to "+vhcFilename);
        if (type.equals("Bin")){vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";}
        else {vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".asc";}
        
       // System.out.println(" to "+vhcFilename);

        return vhcFilename;
    }
   
         

public static void main(String[] args) throws java.io.IOException{
  /*****DEFINE PARAMETERS*******/ 
        int NDecRes=1;    // number of time we would like to reduce the spatial resolution
        int orig_s_res=60; //original spatial resolution in ArcSec
        int ini_row=33;   // number of rows in the original file
        int ini_col=48;   // number of columns in the original file
        int orig_t_res=15; // Initial time resolution in minutes
        int new_t_res=15;
  /*****DEFINE DESIRED RESOLUTION IN SPACE (ARCMIN) AND TIME (MIN)*******/
        int[] space = {1,2,4,6,8,10,12,15};
        int[] time = {15,30,45,60,120,180,240,300};
        
        float missing = 0.0f;
        
     for (int is : space)
     {
     NDecRes=is;
      for (int it : time)
      {
            new_t_res=it;
 /*****DEFINE THE FOLDER WITH NEXRAD DATA AND OUTPUT FOLDER*******/           
        File folder_nexrad = new File("C:/CUENCAS/Whitewater_database/EventJun/15minAnalysis/NEXRAD/");
        String OutputDir="C:/CUENCAS/Whitewater_database/EventJun/15minAnalysis/"+NDecRes+"/";
        new File(OutputDir).mkdirs();
        OutputDir=OutputDir+"/"+new_t_res+"min/";
        new File(OutputDir).mkdirs();
        String OutputDirs=OutputDir+"/space/";       
        String OutputDirt=OutputDir+"/Time/";
        new File(OutputDirs).mkdirs();
        new File(OutputDirs+"Asc").mkdirs();
        new File(OutputDirs+"Bin").mkdirs();
        new File(OutputDirt).mkdirs();
        new File(OutputDirt+"asc").mkdirs();
        new File(OutputDirt+"bin").mkdirs();
        
 /*****CHANGE RESOLUTION IN SPACE*******/       
        
        int Fcolumns = (int) java.lang.Math.ceil((double)ini_col/(double)NDecRes);
        int Frows =  (int) java.lang.Math.ceil((double)ini_row/(double)NDecRes);
        System.out.println("ini_row" + ini_row + "  NDecRes" + NDecRes + "  Frows = "+Frows);
        int new_s_res=NDecRes*orig_s_res;
       java.io.File AsciiFile;
       
        try{
	ArrayList<File> files = HydroNexradRes_space_time.getFileList(folder_nexrad);
	Iterator i = files.iterator();
        
        createMetaFile(new java.io.File(OutputDirs),new_s_res,orig_t_res,Frows,Fcolumns);
        
	  while (i.hasNext()){
            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            AsciiFile = new java.io.File(FileAscIn);
            
            String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);          
            File BinaryOutName = new java.io.File(OutputDirs+"/bin/"+Outfilename(fileName,"Bin"));
            File ASCOutName = new java.io.File(OutputDirs+"/asc/"+Outfilename(fileName,"Asc"));
              try {                
              new HydroNexradRes_space_time(AsciiFile,BinaryOutName,ASCOutName,ini_row,ini_col,orig_s_res,new_s_res,Frows,Fcolumns);
              } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
              }
          }
        } catch (IOException e){
            System.err.println("problem creating file list:");
            e.printStackTrace();
        }
    
        
 /*****CHANGE RESOLUTION IN SPACE*******/       
        System.out.println("OutputDirs = "+OutputDirs);
        File folder_bin = new File(OutputDirs+"/asc/"+"prec.metaVHC");
      
 	java.io.File directorio=folder_bin.getParentFile();
        
        String baseName=folder_bin.getName().substring(0,folder_bin.getName().lastIndexOf("."));
        
        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"asc");
        java.io.File[] lasQueSi=directorio.listFiles(myFiltro);
        hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];      
        
        arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length]; // create an array with files (size equal to the number of files)
        
        for (int i=0;i<lasQueSi.length;i++) arCron[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName); // atribut the files to the array arcron
                      
        java.util.Arrays.sort(arCron); // sort the files
        createMetaFile(new java.io.File(OutputDirt),new_s_res,new_t_res,Frows,Fcolumns);
 System.out.println("before time = "+OutputDirs);
             try {  
                 new HydroNexradRes_time(arCron,OutputDirt,Frows,Fcolumns,new_s_res,orig_t_res,new_t_res,missing);
              
             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
 System.out.println("after time = "+OutputDirs);
        }     
     }    
}

}




