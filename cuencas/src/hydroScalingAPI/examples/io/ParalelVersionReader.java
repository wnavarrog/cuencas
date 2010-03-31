/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;
import java.io.*;
/**
 *
 * @author pmandapa
 */

import java.io.*;
import java.util.*;
 
public class ParalelVersionReader extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    float[]          UpsArea;
    float[]          HortonO;
    float[]          Qmax;
    float[]          Tmax;
    float[]          LinkNumber;
    int              nlinks;
    int              nhydro;
    int              ntime;
    String[]         IDJ;
    float[][]        hydrographs;
    float[]        AvePrec;
    float[]        nelem;
    float[][]        HTime;
    
    float[]        RTime;
    private  int              columns,rows;
    
    public ParalelVersionReader(java.io.File InputFile)
            throws java.io.IOException {
        

         java.io.File[] lasQueSi=InputFile.listFiles();
         UpsArea = new float[200000];
         HortonO = new float[200000];
         Qmax = new float[200000];
         LinkNumber = new float[200000];
         Tmax = new float[200000];
         hydrographs=new float[1000][8000];
         AvePrec=new float[8000];
         nelem=new float[8000];
         HTime=new float[1000][8000];
         RTime=new float[8000];
         IDJ = new String[200000];
         nlinks=0;
         nhydro=0;
         ntime=0;

         System.out.println("lenght - "+lasQueSi.length);
         for (int i=0;i<lasQueSi.length;i++)
             {
             System.out.println("lasQueSi[i] - "+lasQueSi[i]);


                 int flag = checkfile(lasQueSi[i]);
                 if(flag==-1) readFile1(lasQueSi[i]);
                 if(flag==1) readFile2(lasQueSi[i]);
             }
         
         String FileName=lasQueSi[0].getName();
         java.io.File theFile;

         theFile=new java.io.File(InputFile+"/sumary.csv");
         System.out.println("Writing disc1 - "+theFile);

         java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
         java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
         java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
         newfile.write("order,"+"area,"+"Qmax,"+ "Tmax" + "fileID"+"\n");
         for (int ii=0;ii<nlinks;ii++){
             newfile.write(HortonO[ii]+","+UpsArea[ii]+","+Qmax[ii]+","+Tmax[ii]+","+IDJ[ii]+"\n");
         }
         newfile.close();
         bufferout.close();

         // write output file


         theFile=new java.io.File(InputFile+"/hydrograph.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         int j=0;
         //IOWA
         //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
         //Cedar River
         int[] hyd = {1,622,208,550,600,650,700,nhydro-1};
         double[] matmax=new double[nlinks];

         for (int ii=0;ii<ntime;ii++){
           //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nhydro;it++)
           {
            int new_t_res=it;  
            newfile.write(hydrographs[it][ii]+",");
             //j=jj;
           }
         
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();


          theFile=new java.io.File(InputFile+"/Time_hydrograph.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         j=0;
         //IOWA
         //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
         //Cedar River

         for (int ii=0;ii<ntime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nhydro;it++)
           {
            int new_t_res=it;
            newfile.write(HTime[it][ii]+",");
             //j=jj;

           }

          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();

         theFile=new java.io.File(InputFile+"/rainfall.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int i=0;i<AvePrec.length;i++) {AvePrec[i]=AvePrec[i]/nelem[i];}
         for (int ii=0;ii<ntime;ii++){
           newfile.write(RTime[ii]+","+AvePrec[ii]+"\n");
           }

         newfile.close();
         bufferout.close();

         // write output file

    }
    
      public void readFile1(java.io.File InputFile) throws java.io.IOException{

          java.io.FileReader ruta;
          java.io.BufferedReader buffer;

          java.util.StringTokenizer tokens;
          String linea=null, basura, nexttoken;
          //System.out.println("InputFile" + InputFile);
          ruta = new FileReader(InputFile);
          buffer=new BufferedReader(ruta);
          String data = buffer.readLine(); // JUMP 3 LINES IN THE BEGINING
          data = buffer.readLine();
          data = buffer.readLine();
          data = buffer.readLine();
          //READ Horton order
          //System.out.println("data" + data);
          tokens = new StringTokenizer(data,",");
          String temp=new String(tokens.nextToken());
          //System.out.println("temp   " + temp);
          int ii=nlinks;
          //int fileid=InputFile.getName().indexOf("_");
          //int fileid2=InputFile.getName().indexOf("_",fileid+1);
         // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2);
         // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2 + "   " + InputFile.getName().substring(fileid+1,fileid2));
          //String tt=InputFile.getName().substring(fileid+1,fileid2);
          while (tokens.hasMoreTokens())
            {
            HortonO[ii]=new Float(tokens.nextToken());
            IDJ[ii]="1";
            ii=ii+1;
            }
            data = buffer.readLine();
           //READ Horton order
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           while (tokens.hasMoreTokens())
             {
             UpsArea[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }
           for (int j=0;j<7;j++) data = buffer.readLine();
           //READ Upstream Area
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           while (tokens.hasMoreTokens())
             {
             Qmax[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }
           data = buffer.readLine();
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           double Tinit=2.10387600106333*Math.pow(10, 7);

           while (tokens.hasMoreTokens())
             {
             double number1;
             double number2;
             temp=new String(tokens.nextToken());
             double TTemp=0;
             if(temp.indexOf("E")<1) {TTemp=Double.valueOf(temp);}
             else {
             number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
             number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
             TTemp=number1 * Math.pow(10,number2);
             }

//System.out.println(InputFile + "       string " + temp);
             
            
             TTemp=(TTemp-Tinit)/(24*60);
             Tmax[ii]=(float) TTemp;
             ii=ii+1;
             }
             nlinks=ii;
           // READ RAIN
           data = buffer.readLine();
           data = buffer.readLine();
           data = buffer.readLine();
           int j=0;
           
           while (data != null)
             {
             double number1;
             double number2;
             tokens = new StringTokenizer(data,",");
             
             temp=new String(tokens.nextToken());
             double TTemp=0;
             if(temp.indexOf("E")<1) {TTemp=Double.valueOf(temp);}
             else {
             number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
             number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
             TTemp=number1 * Math.pow(10,number2);}

             TTemp=(TTemp-Tinit)/(24*60);
             RTime[j]=(float) TTemp;
             while (tokens.hasMoreTokens())
             {
              AvePrec[j]=AvePrec[j]+new Float(tokens.nextToken());
              nelem[j]=nelem[j]+1;
             }
             
             j=j+1;
             data = buffer.readLine();
             if(j>=8000)data =null;

             }
                    

    }

      public void readFile2(java.io.File InputFile) throws java.io.IOException{

          java.io.FileReader ruta;
          java.io.BufferedReader buffer;

          java.util.StringTokenizer tokens = null;
          String linea=null, basura, nexttoken;

          ruta = new FileReader(InputFile);
          buffer=new BufferedReader(ruta);
          String data = "test"; // JUMP 1 LINE IN THE BEGINING
          data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
          data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
          String temp;
           int ii=nhydro;
           double Tinit=2.10387600106333*Math.pow(10, 7);

           int j=0;
              data = buffer.readLine();
          //READ Horton order

           while (data != null)
           {
               double number1;
               double number2;
               tokens = new StringTokenizer(data,",");

               temp=new String(tokens.nextToken());
               number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
               number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));

               double TTemp=number1 * Math.pow(10,number2);
               TTemp=(TTemp-Tinit);
               HTime[ii][j]=(float) TTemp;
               temp=new String(tokens.nextToken());
               // check if there is an E


             if(temp.indexOf("E")<1) {hydrographs[ii][j]=Float.valueOf(temp);}
             else {
              number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
              number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
              hydrographs[ii][j]=(float)(number1 * Math.pow(10,number2));}

                j=j+1;
                data = buffer.readLine();
                if(j>=1000)data =null;
//System.out.println(ii + "temp" +temp+ "j" + j);
           
           }
            nhydro=nhydro+1;
            ntime=Math.max(ntime,j);

}

public static int checkfile(java.io.File OutputFile) throws java.io.IOException{

        String FileName=OutputFile.getName();

        int flag=FileName.indexOf("Outlet");
        if(flag!=-1) flag=1;
        if(flag==-1) 
           {flag=FileName.indexOf("complete");   
            if (flag!=-1) flag=2;}
        if(flag==-1) 
           {flag=FileName.indexOf("sumary");   
            if (flag!=-1) flag=3;}
        if(flag==-1)
           {flag=FileName.indexOf("rainfall");
            if (flag!=-1) flag=4;}
        if(flag==-1)
           {flag=FileName.indexOf("hydrograph");
            if (flag!=-1) flag=3;}

        return flag;
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
    
    public static void main(String[] args) throws IOException {
      
        int[] space = {25,40};
        //int[] time = {};
        int[] time = {1,3,7};
        float[][] matrix_temp;
        int NDecRes;    // number of time we would like to reduce the spatial resolution
        int new_t_res;

        for (int is : space)
           {
           NDecRes=is;
           for (int it : time)
           {
            new_t_res=it;
           // File folder = new File("D:/CUENCAS/CedarRapids/simulation/RadarAgreg_v2/CedarRapids_SatRC1.00/AveragedIowaRiver_0.4_0.2_-0.1/");
            File folder = new File("E:/CUENCAS/CedarRapids/snow/simulation/snow/3IowaRiver_6h/" +NDecRes+"/"+ new_t_res +"/");


            System.out.println("folder - "+folder);
            System.out.print(folder);
            try{

            new ParalelVersionReader(folder);
            } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);}
           }
          }
   }

}




