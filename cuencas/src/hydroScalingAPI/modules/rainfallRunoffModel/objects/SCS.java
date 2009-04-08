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
    java.io.File LandUseFile;
    java.io.File SoilDataFile;
    java.io.File outputDirectory;

   public SCS(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File LandUseFileOR,java.io.File SoilDataFileOR,java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir=direcc;
        metaDatos=md;

        x=xx;
        y=yy;
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

        SCSObj=new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(LandUseFile,SoilDataFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);

        thisHillsInfo.setSCSManager(SCSObj);

           //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);


 //     newfile.write("Information of precipition for each link\n");
 //     newfile.write("Links at the bottom of complete streams are:\n");
 //     newfile.write(1,");
        System.out.println("Open the file \n");
        double RC = -9.9;
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+" ");
                newfile.write(linksStructure.completeStreamLinksArray[i]+" ");
                newfile.write(thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+" ");
                newfile.write(thisHillsInfo.Area(i)+" ");
                newfile.write(thisHillsInfo.SCS_CN1(i)+" ");
                newfile.write(thisHillsInfo.SCS_CN2(i)+" ");
                newfile.write(thisHillsInfo.SCS_CN3(i)+" ");
                newfile.write(thisHillsInfo.MinHillBasedCN(i)+" ");
                newfile.write(thisHillsInfo.MaxHillBasedCN(i)+" ");
                newfile.write(thisHillsInfo.SCS_IA2(i)+" ");
                newfile.write(thisHillsInfo.SCS_S2(i)+" ");
                newfile.write(thisHillsInfo.LandUseSCS(i)+" ");
                newfile.write(thisHillsInfo.LandUsePercSCS(i)+" ");
                newfile.write(thisHillsInfo.Soil_SCS(i)+" ");
                newfile.write(thisHillsInfo.Soil_PercSCS(i)+" ");
                newfile.write("\n");
            }
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
           // subMain(args);     //Case Whitewater
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

   String pathinput = "C:/CUENCAS/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/";
   java.io.File theFile=new java.io.File(pathinput + "Whitewaters" + ".metaDEM");
   hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
   metaModif.setLocationBinaryFile(new java.io.File(pathinput + "Whitewaters" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 1063;
   int y = 496;

   String precname="LC2001_cliped.metaVHC";
   String Dir="C:/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
   new java.io.File(Dir+"/test/").mkdirs();
   String OutputDir=Dir+"/test/";


   String LandUse = Dir+precname;
   Dir="C:/CUENCAS/Whitewater_database/Rasters/Hydrology/soil_type/CUENCAS/";
   precname="hyd_group.metaVHC";
   String SoilData = Dir+precname;

   new SCS(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(SoilData),new java.io.File(OutputDir)).ExecuteSCS();


    }

  public static void subMain1(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/Charlote/Rasters/Topography/";
        java.io.File theFile=new java.io.File(pathinput + "charlotte_dem" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte_dem" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 1929;
   int y = 2046;

   String precname="raster_cliped.metaVHC";
   String Dir="C:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";
   new java.io.File(Dir+"/test/").mkdirs();
   String OutputDir=Dir+"/test/";


   String LandUse = Dir+precname;
   Dir="C:/CUENCAS/Charlote/Rasters/Land_surface_Data/soil_nc119/";
   precname="soil_data.metaVHC";
   String SoilData = Dir+precname;


   new SCS(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(SoilData),new java.io.File(OutputDir)).ExecuteSCS();


    }
}
