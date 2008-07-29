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
 
public class ascTRMMToCRas1 extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  double[][]       matrix;
    private  double           columns,rows;
    private  int              inicol,inirow,fincol,finrow;
    private  String           south,west;
     
    
    public ascTRMMToCRas1(java.io.File inputFile, java.io.File outputFileBi, java.io.File outputFileASC,double LimWest,double LimEast,double LimNorth,double LimSouth) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        //define the ASCII properties
        inicol=(int)(-(-180-LimWest)/0.25)-1;
        fincol=(int)(-(-180-LimEast)/0.25)-1;
        inirow=(int)((50-LimNorth)/0.25)-1;
        finrow=(int)((50-LimSouth)/0.25)-1;
        columns = fincol-inicol+1;
        rows =  finrow-inirow+1;
        
        System.out.println("buffer the data"+"column=" +columns+"rows="+rows);  
        south= "33:13:00.00 N";
        west= "97:30:00.00 W";
        
        //String temp = Double.toString(LimSouth);
        //////////////////// parei aqui//////////////////////
        
        int oricolumns = 1440;
        int orirows = 400;        
        matrix = new double[orirows][oricolumns];

        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);     
        String data = buffer.readLine();
        int i=0;
        int j=0;  
// south to north 
//        while (data != null)
//        {
//            tokens = new StringTokenizer(data,"     ");
//            while (tokens.hasMoreTokens()) {
//             if(i==(orirows))
//             {j=j+1;
//              i=0;}
//              matrix[i][j] = new Double(tokens.nextToken());
//             // matrix[i][j] = new Double(tokens.nextToken()).doubleValue();
//              i=i+1; 
//            }
//        data = buffer.readLine();
//        }       

// north to south 
        i=orirows-1;
        j=0;
        while (data != null)
        {
            tokens = new StringTokenizer(data,"     ");
            while (tokens.hasMoreTokens()) {
             if(i==-1)
             {j=j+1;
              i=orirows-1;}
//             System.out.println("matrix bounds"+"column=" +j+"rows="+i);  
             matrix[i][j] = new Double(tokens.nextToken());
             // matrix[i][j] = new Double(tokens.nextToken()).doubleValue();
              i=i-1; 
            }
        data = buffer.readLine();
        }       
        
        //for (int i=0;i<11;i++) buffer.readLine();
        
        //for (int i=0;i<rows;i++) {
            
        //    linea = buffer.readLine();
        
        //    tokens = new StringTokenizer(linea);
        //    for (int j=0;j<columns;j++) {
       //         try{
       //             matrix[i][j] = new Float(tokens.nextToken()).floatValue();
       //         } catch (NumberFormatException NFE){
       //             matrix[i][j] = -9999;
       //         }
       //     }
       // }
       
        buffer.close();
        
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFileBi.getAbsolutePath();
        String fileAscoutputDir=outputFileASC.getAbsolutePath();
        newfilebinary(new java.io.File(fileBinoutputDir)); 
        writeEsriFile(new java.io.File(fileAscoutputDir),LimWest,LimSouth);      
    }
        
        
    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
        double missing=-9999.9;
        
//        for (int i=(finrow-1);i>=inirow;i--){ 
//            for (int j=inicol;j<fincol;j++) {
//            if (matrix[i][j] == missing) {newfile.writeDouble(-99.00);} 
//            else {newfile.writeDouble(matrix[i][j]);}
//            }
//        }

        for (int i=finrow;i>=inirow;i--){ 
            for (int j=inicol;j<=fincol;j++) {
            if (matrix[i][j] == missing) {newfile.writeDouble(-99.00);} 
            else {newfile.writeDouble(matrix[i][j]);}
            }
        }

        
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
        public void writeEsriFile(java.io.File AscFile,double LimWest,double LimSouth) throws java.io.IOException{
        
      
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;        
        String                          retorno="\n";
        
        outputDir = new FileOutputStream(AscFile);
        bufferout=new BufferedOutputStream(outputDir);

        newfile=new java.io.OutputStreamWriter(bufferout);
                
        double missing=-9999.9;
        columns = fincol-inicol+1;
        rows =  finrow-inirow+1;
        newfile.write("ncols         "+columns+retorno);
        newfile.write("nrows         "+rows+retorno);
        newfile.write("xllcorner     "+LimWest+retorno);
        newfile.write("yllcorner     "+LimSouth+retorno);
        newfile.write("cellsize      "+"15"+retorno);
        newfile.write("NODATA_value  "+-99.00+retorno);

        
        for (int i=inirow;i<=finrow;i++){ 
            for (int j=inicol;j<=fincol;j++) {
                if (matrix[i][j] == missing) {
                    newfile.write(-99.00+" ");
                } else {
                    newfile.write(matrix[i][j]+" ");
                }
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
    
    public static void createMetaFile(File directory) {
        try{
            
            File saveFile=new File(directory.getPath()+File.separator+"prec.metaVHC");
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            writer.println("[Name]");
            writer.println("Precipitation from Satelite - TRMM 3B42  ");
            writer.println("[Southernmost Latitude]");
            writer.println("33:13:00.00 N");
            writer.println("[Westernmost Longitude]");
            writer.println("97:30:00.00 W");
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println("900");
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println("900");
            writer.println("[# Columns]");
            writer.println("26");
            writer.println("[# Rows]");
            writer.println("34");
            writer.println("[Format]");
            writer.println("Double");
            writer.println("[Missing]");
            writer.println("-99.00");
            writer.println("[Temporal Resolution]");
            writer.println("180-minutes");
            writer.println("[Units]");
            writer.println("mm/h");
            writer.println("[Information]");
            writer.println("TRMM 3B42 Precipitation data");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }
        
    }
  
        public static String Outfilename(String fileName, String type) {
        System.out.println("fileName");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
            String[] hourt={"22","01","04","07","13","16","19","22"}; 
            int[] codification={0,3,6,9,12,15,18,21};
            String monthString;
            //3B42.000619.18.6.precipitation.ascii
                    
        String[] timeStamp = new String[5];
        //timeStamp[0]=fileName.substring(32,36); // year
        timeStamp[0]=fileName.substring(5,8); // year
        int yr = Integer.valueOf(timeStamp[0]).intValue();
        if(yr<900) timeStamp[0]="2"+timeStamp[0];
        if(yr>900) timeStamp[0]="1"+timeStamp[0];
        
        timeStamp[1]=fileName.substring(7,9);  // month
        timeStamp[2]=fileName.substring(9,11);  // day
        
      
        System.out.println("month = "+ fileName.substring(7,9)); 
        System.out.println("day = "+ fileName.substring(9,11)); 
        System.out.println("flag = "+ fileName.substring(12,14)); 
        
        int flag;
        if((fileName.substring(12,14)).endsWith(".")){flag = Integer.valueOf(fileName.substring(12,13)).intValue(); }
        else {flag = Integer.valueOf(fileName.substring(12,14)).intValue();}  
        
        for (int ii=0;ii<=7;ii++)
        {
            if(flag==codification[ii]) 
                {timeStamp[3]=hourt[ii];}
        }
        
        timeStamp[4]="30"; // min
        monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        String vhcFilename;
        if(type=="Bin") {vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";}
        else {vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".asc";}
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
   public static void main(String[] args) throws java.io.IOException{
        
        
        java.io.File OriginalFile;
        File folder = new File("C:/CUENCAS/11110103/Data/07197000/sat/raw"); // input folder
        String OutputDirBin="C:/CUENCAS/11110103/Data/07197000/sat/Bin"; // output folder - Binary files
        String OutputDirAsc="C:/CUENCAS/11110103/Data/07197000/sat/Asc"; // output folder - Asc files
        double LimSouth = 33.75;
        double LimNorth = 42.00;
        double LimEast = -91.25;
        double LimWest = -97.50;               

            try{
	ArrayList<File> files = ascTRMMToCRas1.getFileList(folder);
	Iterator i = files.iterator();
        
        
        createMetaFile(new java.io.File(OutputDirBin));
        createMetaFile(new java.io.File(OutputDirAsc));
                
	while (i.hasNext()){
            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            
            OriginalFile = new java.io.File(FileAscIn);
            
            
        
  ///////////////////////////////
  /// Define the name of the output file          
        String fileName=OriginalFile.getPath().substring(OriginalFile.getPath().lastIndexOf(File.separator)+1);
       System.out.println(temp.getAbsolutePath());           
        File BinaryOutName = new java.io.File(OutputDirBin+File.separator+Outfilename(fileName,"Bin"));
        File AscOutName = new java.io.File(OutputDirAsc+File.separator+Outfilename(fileName,"Asc"));        
  ////////////////////////////////////////
            try {  
                 System.out.println(temp.getAbsolutePath());
                 new ascTRMMToCRas1(OriginalFile,BinaryOutName,AscOutName,LimWest,LimEast,LimNorth,LimSouth);
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




