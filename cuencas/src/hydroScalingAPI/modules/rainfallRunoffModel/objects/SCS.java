/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author lcunha
 */
public class SCS {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    int x;
    int y;
    int[][] magnitudes;
    java.io.File DemFile;
    java.io.File LandUseFile;
    java.io.File SoilDataFile;
    java.io.File outputDirectory;

   public SCS(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File DemFileOR, java.io.File LandUseFileOR,java.io.File SoilDataFileOR,java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir=direcc;
        metaDatos=md;

        x=xx;
        y=yy;

        DemFile=DemFileOR;
        magnitudes=magnitudesOR;
        LandUseFile=LandUseFileOR;
        SoilDataFile=SoilDataFileOR;
        outputDirectory=outputDirectoryOR;
   }

    public void ExecuteSCS() throws java.io.IOException {

        System.out.println("Start to run SCS \n");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;

        SCSObj=new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DemFile,LandUseFile,SoilDataFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);

        thisHillsInfo.setSCSManager(SCSObj);

           //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCSall"+".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        int linkID=0;
        /*Some particular links relevant to Wlanut Gulch, AZ
         linkID=linksStructure.getResSimID(822,964);
        //System.out.println("Little Hope Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        System.out.println("Little Hope Creek:    "+linkID);
        linkID=linksStructure.getResSimID(95,815);
        System.out.println("Little Sugar Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
         */

        int nLi=linksStructure.contactsArray.length;
        double[] Area_length=new double[linksStructure.contactsArray.length];
        System.out.println("Open the file");

        double RC = -9.9;
        double max_rel=0;
                System.out.println("n column:    "+metaDatos.getNumCols());
                System.out.println("n line:    "+metaDatos.getNumRows());
        for (int i=0;i<nLi;i++){
            //if(thisNetworkGeom.linkOrder(i) > 1){
                int matrix_pin=i+1;

                /*int yy=(int)Math.floor(linksStructure.contactsArray[i]/metaDatos.getNumCols());
                double temp=(double)metaDatos.getNumCols()*((linksStructure.contactsArray[i]/(double)metaDatos.getNumCols())-(double)yy);
                int xx=(int)(temp);
                newfile.write(xx+",");
                newfile.write(yy+",");*/
                newfile.write(matrix_pin+","); //1
                newfile.write(thisNetworkGeom.linkOrder(i)+","); //2
                newfile.write(thisHillsInfo.Area(i)+",");//8
                newfile.write(thisNetworkGeom.upStreamArea(i)+",");       //7
                newfile.write(SCSObj.getavehillBasedSlope(i)+",");//3
                newfile.write(thisNetworkGeom.Slope(i)+",");//4
                double Hchannel=thisNetworkGeom.Slope(i)*thisNetworkGeom.Length(i);
                double HillRelief=SCSObj.getHillRelief(i)-Hchannel;
                double FormParam=(HillRelief)*2*thisNetworkGeom.Length(i)/(thisHillsInfo.Area(i)*1000000);
                newfile.write(HillRelief+",");//5
                newfile.write(Hchannel+",");//6
                newfile.write(thisNetworkGeom.upStreamArea(i)+",");       //7
                newfile.write(thisHillsInfo.Area(i)+",");//8
                newfile.write(thisNetworkGeom.Length(i)+",");//9
                newfile.write(SCSObj.getHillRelief(i)+",");//10
                newfile.write(SCSObj.getHillReliefMin(i)+",");//11
                newfile.write(SCSObj.getHillReliefMax(i)+",");//12
                newfile.write(FormParam+",");
                newfile.write(SCSObj.getAverManning(i)+",");
                newfile.write(SCSObj.getminHillBasedManning(i)+",");
                newfile.write(SCSObj.getmaxHillBasedManning(i)+",");

                newfile.write(thisNetworkGeom.upStreamArea(i)+",");


                newfile.write(thisNetworkGeom.upStreamTotalLength(i)+",");
                newfile.write(thisHillsInfo.LandUseSCS(i)+",");
                newfile.write(thisHillsInfo.Soil_SCS(i)+",");
                newfile.write(thisHillsInfo.SCS_S1(i)+",");

                double dist=thisHillsInfo.Area(i)*1000000*0.5/(thisNetworkGeom.Length(i)); //(m)
                double tim_run=dist/100; //hour
                newfile.write(tim_run+",");
                double format;
                format=SCSObj.getHillReliefMin(i)+((SCSObj.getHillReliefMax(i)- SCSObj.getHillReliefMin(i))/2);
                double format_flag=-9.9;
                if(format<SCSObj.getHillReliefAve(i)) format_flag=1;
                if(format>SCSObj.getHillReliefAve(i)) format_flag=-1;
                if(format==SCSObj.getHillReliefAve(i)) format_flag=0;
                newfile.write(format_flag+",");



                //newfile.write(thisHillsInfo.SCS_IA2(i)+" ");
                //newfile.write(thisHillsInfo.SCS_S2(i)+" ");
                //newfile.write(thisHillsInfo.SCS_IA3(i)+" ");
                //newfile.write(thisHillsInfo.SCS_S3(i)+" ");
                //for (int j=0;j<5;j++){
                //newfile.write(SCSObj.getHillReliefClass(i,j) +" ");
                //newfile.write(SCSObj.getHillReliefPorc(i,j) +" ");
               //}

                newfile.write("\n");
                Area_length[i]=thisHillsInfo.Area(i)*1000000/thisNetworkGeom.Length(i);
                max_rel=Math.max(max_rel,Area_length[i]);

        }


        System.out.println("Termina escritura de LandUse \n");

        newfile.close();
        bufferout.close();

System.out.println("Start to run Width function");

        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCScompleteO"+".csv");

        System.out.println(theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);



        nLi=linksStructure.completeStreamLinksArray.length;
        Area_length=new double[linksStructure.completeStreamLinksArray.length];
        System.out.println("Open the file");


        for (int i=0;i<nLi;i++){
            //if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
                int matrix_pin=linksStructure.completeStreamLinksArray[i]+1;
                int ii=linksStructure.completeStreamLinksArray[i];
                newfile.write(matrix_pin+","); //1
                newfile.write(thisNetworkGeom.linkOrder(ii)+","); //2
                newfile.write(thisHillsInfo.Area(ii)+",");//8
                newfile.write(thisNetworkGeom.upStreamArea(ii)+",");       //7
                newfile.write(SCSObj.getavehillBasedSlope(ii)+",");//3
                newfile.write(thisNetworkGeom.Length(ii)+",");
                newfile.write(SCSObj.getHillRelief(ii)+",");
                newfile.write(SCSObj.getHillReliefMin(ii)+",");
                newfile.write(SCSObj.getHillReliefMax(ii)+",");
                double Hchannel=thisNetworkGeom.Slope(ii)*thisNetworkGeom.Length(ii);
                double HillRelief=SCSObj.getHillRelief(ii)-Hchannel;
                double FormParam=(HillRelief)*2*thisNetworkGeom.Length(ii)/(thisHillsInfo.Area(ii)*1000000);
                newfile.write(HillRelief+",");
                newfile.write(Hchannel+",");
                newfile.write(FormParam+",");
                newfile.write(SCSObj.getAverManning(ii)+",");
                newfile.write(SCSObj.getminHillBasedManning(ii)+",");
                newfile.write(SCSObj.getmaxHillBasedManning(ii)+",");
                newfile.write(SCSObj.getavehillBasedSlope(ii)+",");
                newfile.write(thisNetworkGeom.Slope(ii)+",");
                newfile.write(thisNetworkGeom.upStreamArea(ii)+",");


                newfile.write(thisNetworkGeom.upStreamTotalLength(ii)+",");
                newfile.write(thisHillsInfo.LandUseSCS(ii)+",");
                newfile.write(thisHillsInfo.Soil_SCS(ii)+",");
                newfile.write(thisHillsInfo.SCS_S1(ii)+",");

                double dist=thisHillsInfo.Area(ii)*1000000*0.5/(thisNetworkGeom.Length(ii)); //(m)
                double tim_run=dist/100; //hour
                newfile.write(tim_run+",");
                double format;
                format=SCSObj.getHillReliefMin(ii)+((SCSObj.getHillReliefMax(ii)- SCSObj.getHillReliefMin(ii))/2);
                double format_flag=-9.9;
                if(format<SCSObj.getHillReliefAve(ii)) format_flag=1;
                if(format>SCSObj.getHillReliefAve(ii)) format_flag=-1;
                if(format==SCSObj.getHillReliefAve(ii)) format_flag=0;
                newfile.write(format_flag+",");

                //newfile.write(thisHillsInfo.SCS_IA2(i)+" ");
                //newfile.write(thisHillsInfo.SCS_S2(i)+" ");
                //newfile.write(thisHillsInfo.SCS_IA3(i)+" ");
                //newfile.write(thisHillsInfo.SCS_S3(i)+" ");
                //for (int j=0;j<5;j++){
                //newfile.write(SCSObj.getHillReliefClass(i,j) +" ");
                //newfile.write(SCSObj.getHillReliefPorc(i,j) +" ");
               //}

                newfile.write("\n");
//                Area_length[ii]=thisHillsInfo.Area(ii)*1000000/thisNetworkGeom.Length(ii);
//                max_rel=Math.max(max_rel,Area_length[ii]);

        }


        System.out.println("Termina escritura de LandUse \n");

        newfile.close();
        bufferout.close();
    }

       /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try{
            //subMain(args);     //Case Whitewater
            subMain1(args);     //Case charlotte
           //subMain2(args);     //Case  Rio Puerco
           //subMain3(args);     //Case  11070208
           //subMain4(args);     //Case  11140102
           //subMain5(args);     //Iowa River
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

 public static void subMain(String args[]) throws java.io.IOException {

   String pathinput = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/";
   java.io.File DEMFile=new java.io.File(pathinput + "Whitewaters" + ".metaDEM");
   hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(DEMFile);
   metaModif.setLocationBinaryFile(new java.io.File(pathinput + "Whitewaters" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0,DEMFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 1063;
   int y = 496;

   String precname="LC2001_cliped.metaVHC";
   String Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
   new java.io.File(Dir+"/test/").mkdirs();
   String OutputDir=Dir+"/test/";


   String LandUse = Dir+precname;
   Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/soil_type/CUENCAS/";
   precname="hyd_group.metaVHC";
   String SoilData = Dir+precname;

   new SCS(x,y,matDirs,magnitudes,metaModif,DEMFile,new java.io.File(LandUse),new java.io.File(SoilData),new java.io.File(OutputDir)).ExecuteSCS();


    }

  public static void subMain1(String args[]) throws java.io.IOException {

        ///// DEM DATA /////

        String pathinput = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
        java.io.File DEMFile=new java.io.File(pathinput + "charlotte" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(DEMFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0,DEMFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   // main basin
        // main basin
        int x= 764;
        int y= 168;

   //String precname="raster_cliped.metaVHC";
   //String Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";
   //String precname="lc1992.metaVHC";
   //String Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover1992/";
   String precname="lcOri.metaVHC";
   String Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_original/";
   new java.io.File(Dir+"/Hill_data/").mkdirs();
   String OutputDir=Dir+"/Hill_data/";


   String LandUse = Dir+precname;
   Dir="C:/Documents and Settings/lcunha/My Documents/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_original/";
   precname="soilorifinal.metaVHC";
   String SoilData = Dir+precname;

   new SCS(x,y,matDirs,magnitudes,metaModif,DEMFile,new java.io.File(LandUse),new java.io.File(SoilData),new java.io.File(OutputDir)).ExecuteSCS();


    }
}
