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


/*
 * LandUseManager.java
 * Adapted from LandUseManager
 * Luciana Cunha
 * Created on October 08, 2008
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * This class estimates the average Curve Number for each hillslope based on Land Cover and
 * Soil information.
 * Land Cover is obtained from the National Map Seamless Server.
 * Soil data is taken from STATSCO or SSURGO Database.
 * This class handles the physical information over a basin.
 * It takes in a group of raster files (land cover and Soil data) and calculate the average
 * Curve Number for each hilsslope.
 *
 * @author Luciana Cunha
 * Adapted from LandUseManager.java(@author Ricardo Mantilla)
 */
public class SCSManager {

//    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] LandUseOnBasin;

    double[] Hyd_Group;  // [link]//
    double[] CN;  // [link]//
    double[] maxHillBasedCN;  // [link]//
    double[] minHillBasedCN;  // [link]//

    private boolean success=false,veryFirstDrop=true;
    private hydroScalingAPI.io.MetaRaster metaLandUse;
    private hydroScalingAPI.io.MetaRaster metaSoilData;
    private java.util.Calendar InfoDate;


    float[][] LandUseOnBasinArray;
    private int[] MaxHillBasedLandUse;
    private float[] MaxHillBasedLandUsePerc;
    int[][] matrizPintada;
    float[][] SOILOnBasinArray;
    private int[] MaxHillBasedSOIL;
    private float[] MaxHillBasedSOILPerc;

    private double recordResolutionInMinutes;

    private String thisLandUseName;
    private String thisSoilData;

     /**
     * Creates a new instance of LandUseManager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param LandUse The path to the Land Cover files
     * @param SoilPro The path to the Land Cover files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public SCSManager(java.io.File LandUse,java.io.File SoilData, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes) {

        System.out.println("START THE SCS MANAGER \n");
        java.io.File LCFile=LandUse;
        java.io.File SoilDataFile=SoilData;
        System.out.println("LandUse File = " + LandUse +"\n");
        System.out.println("SoilData File = " + SoilData +"\n");
        java.io.File directorio=LandUse.getParentFile();
        String baseNameLC=directorio+"\\"+(LandUse.getName().substring(0,LandUse.getName().lastIndexOf(".")))+".vhc";
        directorio=SoilData.getParentFile();
        String baseNameSOIL=directorio+"\\"+(SoilData.getName().substring(0,SoilData.getName().lastIndexOf(".")))+".vhc";

//        arCronLC=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];
//        arCronSoil=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];
//        for (int i=0;i<lasQueSi.length;i++) arCron[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName);

//        java.util.Arrays.sort(arCron);

        //Una vez leidos los archivos:
        //Lleno la matriz de direcciones

        // for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}
        // the maximum column and line of a box involving the basin
        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        // ?????
        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

        try{
            // Create the metaraster for land use
            System.out.println("CREATE THE META RASTER _ LU \n");
            metaLandUse=new hydroScalingAPI.io.MetaRaster(LandUse);
            System.out.println("CREATE THE META RASTER _ SOIL DATA \n");
            metaSoilData=new hydroScalingAPI.io.MetaRaster(SoilData);
            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaLandUse.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaLandUse.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaLandUse.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaLandUse.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
                    System.out.println("Not Area Coverage for LC");
                    return;
            }
            // check if soil data and LC data have the same properties (resolution, ncol, nline,...)
            if (metaSoilData.getNumRows() != metaLandUse.getNumRows() ||
                metaSoilData.getNumCols() != metaLandUse.getNumCols() ||
                metaSoilData.getResLat() != metaLandUse.getResLat() ||
                metaSoilData.getMinLat() != metaLandUse.getMinLat() ||
                metaSoilData.getMinLon() != metaLandUse.getMinLon()) {
                    System.out.println("Soil and LC data information are different");
                    return;
            }

             if (metaSoilData.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaSoilData.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaSoilData.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaSoilData.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
                    System.out.println("Not Area Coverage for Soil data");
                    return;
            }


            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){

                    xOulet=linksStructure.contactsArray[i]%metaDatos.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaDatos.getNumCols();

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                } else {

                    xOulet=myCuenca.getXYBasin()[0][0];
                    yOulet=myCuenca.getXYBasin()[1][0];

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                }
            }

//            int regInterval=metaLandUse.getTemporalScale();

//            System.out.println("Time interval for this file: "+regInterval);

 //           for (int i=0;i<LandUseOnBasin.length;i++){
 //               LandUseOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval);
 //           }

            double[] evalSpotLC,evalSpotSOIL;
            double [][] dataSnapShotLC, dataSectionLC;
            double [][] dataSnapShotSOIL, dataSectionSOIL;
            int MatXLC,MatYLC;
            int MatXSOIL,MatYSOIL;
            //System.out.println("-----------------Start of Files Reading - LC----------------");
            Hyd_Group=new double[linksStructure.contactsArray.length];  // [link][SoilData]//
            CN=new double[linksStructure.contactsArray.length];
            maxHillBasedCN=new double[linksStructure.contactsArray.length];
            minHillBasedCN=new double[linksStructure.contactsArray.length];

            int nclasses=10;
            int[] currentHillNumPixels=new int[linksStructure.tailsArray.length];
            double[] currentHillBasedCN=new double[linksStructure.tailsArray.length];
            int[][] currentHillBasedLandUse=new int[linksStructure.tailsArray.length][nclasses];

            int[][] currentSoilType=new int[linksStructure.tailsArray.length][4];

            LandUseOnBasinArray=new float[linksStructure.contactsArray.length][nclasses];
            MaxHillBasedLandUse=new int[linksStructure.contactsArray.length];
            MaxHillBasedLandUsePerc=new float[linksStructure.contactsArray.length];
            SOILOnBasinArray=new float[linksStructure.contactsArray.length][4];
            MaxHillBasedSOIL=new int[linksStructure.contactsArray.length];
            MaxHillBasedSOILPerc=new float[linksStructure.contactsArray.length];

            double PixelCN;
            PixelCN=-9.9;
  //          System.out.println("arCron.length: "+arCron.length);
  //          for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                System.out.println("--> Loading data from LC = "+baseNameLC+"\n");
                metaLandUse.setLocationBinaryFile(new java.io.File((baseNameLC)));
                System.out.println("--> Loading data from LC = "+baseNameSOIL+"\n");
                metaSoilData.setLocationBinaryFile(new java.io.File((baseNameSOIL)));
                System.out.println("--> Start to load the data \n");
                dataSnapShotSOIL=new hydroScalingAPI.io.DataRaster(metaSoilData).getDouble();
                System.out.println("--> Start to load the data 2\n");
                dataSnapShotLC=new hydroScalingAPI.io.DataRaster(metaLandUse).getDouble();
                System.out.println("--> Start to load the data 2\n");

                System.out.println("Finish load the data \n");
                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShotLC,new Double(metaLandUse.getMissing()).doubleValue());
                System.out.println("    --> LC Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);
                rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShotSOIL,new Double(metaSoilData.getMissing()).doubleValue());
                System.out.println("    --> SOIL Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);

                //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)

                double demMinLon=metaDatos.getMinLon();
                double demMinLat=metaDatos.getMinLat();
                double demResLon=metaDatos.getResLon();
                double demResLat=metaDatos.getResLat();

                int basinMinX=myCuenca.getMinX();
                int basinMinY=myCuenca.getMinY();

                double LandUseMinLonLC=metaLandUse.getMinLon();
                double LandUseMinLatLC=metaLandUse.getMinLat();
                double LandUseResLonLC=metaLandUse.getResLon();
                double LandUseResLatLC=metaLandUse.getResLat();
                double LandUseMinLonSOIL=metaSoilData.getMinLon();
                double LandUseMinLatSOIL=metaSoilData.getMinLat();
                double LandUseResLonSOIL=metaSoilData.getResLon();
                double LandUseResLatSOIL=metaSoilData.getResLat();

                for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){

                   evalSpotLC=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                   evalSpotSOIL=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                    MatXLC=(int) Math.floor((evalSpotLC[0]-LandUseMinLonLC)/LandUseResLonLC*3600.0);
                    MatYLC=(int) Math.floor((evalSpotLC[1]-LandUseMinLatLC)/LandUseResLatLC*3600.0);

                    MatXSOIL=(int) Math.floor((evalSpotSOIL[0]-LandUseMinLonSOIL)/LandUseResLonSOIL*3600.0);
                    MatYSOIL=(int) Math.floor((evalSpotSOIL[1]-LandUseMinLatSOIL)/LandUseResLatSOIL*3600.0);

                    if (matrizPintada[j][k] > 0){

///////////////////// land use ///////////////////

                        if(dataSnapShotLC[MatYLC][MatXLC]>10 && dataSnapShotLC[MatYLC][MatXLC]<20 ) currentHillBasedLandUse[matrizPintada[j][k]-1][0]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>20 && dataSnapShotLC[MatYLC][MatXLC]<30 ) currentHillBasedLandUse[matrizPintada[j][k]-1][1]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>30 && dataSnapShotLC[MatYLC][MatXLC]<40 ) currentHillBasedLandUse[matrizPintada[j][k]-1][2]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>40 && dataSnapShotLC[MatYLC][MatXLC]<50 ) currentHillBasedLandUse[matrizPintada[j][k]-1][3]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>50 && dataSnapShotLC[MatYLC][MatXLC]<60 ) currentHillBasedLandUse[matrizPintada[j][k]-1][4]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>60 && dataSnapShotLC[MatYLC][MatXLC]<70 ) currentHillBasedLandUse[matrizPintada[j][k]-1][5]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>70 && dataSnapShotLC[MatYLC][MatXLC]<80) currentHillBasedLandUse[matrizPintada[j][k]-1][6]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]==82 ) currentHillBasedLandUse[matrizPintada[j][k]-1][7]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>80 && dataSnapShotLC[MatYLC][MatXLC]<90  && dataSnapShotLC[MatYLC][MatXLC]!=82 ) currentHillBasedLandUse[matrizPintada[j][k]-1][8]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]>90 && dataSnapShotLC[MatYLC][MatXLC]<100 ) currentHillBasedLandUse[matrizPintada[j][k]-1][9]++;
                        if(dataSnapShotLC[MatYLC][MatXLC]<90 && dataSnapShotLC[MatYLC][MatXLC]>100 ) currentHillBasedLandUse[matrizPintada[j][k]-1][1]++;
///////////////////// Hyd_group ///////////////////
                        if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==1) currentSoilType[matrizPintada[j][k]-1][0]++;
                        if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==2) currentSoilType[matrizPintada[j][k]-1][1]++;
                        if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==3) currentSoilType[matrizPintada[j][k]-1][2]++;
                        if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==4) currentSoilType[matrizPintada[j][k]-1][3]++;
                        if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]<=0)
                        {   currentSoilType[matrizPintada[j][k]-1][3]++;
                            dataSnapShotSOIL[MatYSOIL][MatXSOIL]=4;}


//  if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]<90 && dataSnapShotSOIL[MatYSOIL][MatXSOIL]>100 ) currentSoilType[matrizPintada[j][k]-1][0]++;
///////////////////// Adapt for land use ///////////////////
                      if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==1)
                        {if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=64;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==41 ||
                            dataSnapShotLC[MatYLC][MatXLC]==42 ||
                            dataSnapShotLC[MatYLC][MatXLC]==43 ) PixelCN=30;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==31 || dataSnapShotLC[MatYLC][MatXLC]==32 ||
                            dataSnapShotLC[MatYLC][MatXLC]==51 || dataSnapShotLC[MatYLC][MatXLC]==52 ||
                            dataSnapShotLC[MatYLC][MatXLC]==71 || dataSnapShotLC[MatYLC][MatXLC]==72 ||
                            dataSnapShotLC[MatYLC][MatXLC]==73 || dataSnapShotLC[MatYLC][MatXLC]==74 ||
                            dataSnapShotLC[MatYLC][MatXLC]==81 || dataSnapShotLC[MatYLC][MatXLC]==61 )PixelCN=39;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==21) PixelCN=49;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=57;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==23) PixelCN=61;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==24) PixelCN=77;
                         else PixelCN=40;
                      }

                      if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==2)
                        {if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=75;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==41 ||
                            dataSnapShotLC[MatYLC][MatXLC]==42 ||
                            dataSnapShotLC[MatYLC][MatXLC]==43 ) PixelCN=55;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==31 || dataSnapShotLC[MatYLC][MatXLC]==32 ||
                            dataSnapShotLC[MatYLC][MatXLC]==51 || dataSnapShotLC[MatYLC][MatXLC]==52 ||
                            dataSnapShotLC[MatYLC][MatXLC]==71 || dataSnapShotLC[MatYLC][MatXLC]==72 ||
                            dataSnapShotLC[MatYLC][MatXLC]==73 || dataSnapShotLC[MatYLC][MatXLC]==74 ||
                            dataSnapShotLC[MatYLC][MatXLC]==81 || dataSnapShotLC[MatYLC][MatXLC]==61 ) PixelCN=61;
                        else if(dataSnapShotLC[MatYLC][MatXLC]==21) PixelCN=69;
                        else if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=72;
                        else if(dataSnapShotLC[MatYLC][MatXLC]==23) PixelCN=75;
                        else if(dataSnapShotLC[MatYLC][MatXLC]==24) PixelCN=85;
                        else PixelCN=42;
                      }

                      if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==3)
                        {if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=82;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==41 ||
                            dataSnapShotLC[MatYLC][MatXLC]==42 ||
                            dataSnapShotLC[MatYLC][MatXLC]==43 ) PixelCN=70;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==31 || dataSnapShotLC[MatYLC][MatXLC]==32 ||
                            dataSnapShotLC[MatYLC][MatXLC]==51 || dataSnapShotLC[MatYLC][MatXLC]==52 ||
                            dataSnapShotLC[MatYLC][MatXLC]==71 || dataSnapShotLC[MatYLC][MatXLC]==72 ||
                            dataSnapShotLC[MatYLC][MatXLC]==73 || dataSnapShotLC[MatYLC][MatXLC]==74 ||
                            dataSnapShotLC[MatYLC][MatXLC]==81 || dataSnapShotLC[MatYLC][MatXLC]==61 )PixelCN=74;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==21) PixelCN=79;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=81;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==23) PixelCN=83;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==24) PixelCN=90;
                         else PixelCN=46;
                      }

                      if(dataSnapShotSOIL[MatYSOIL][MatXSOIL]==4)
                        {if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=85;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==41 ||
                            dataSnapShotLC[MatYLC][MatXLC]==42 ||
                            dataSnapShotLC[MatYLC][MatXLC]==43 ) PixelCN=77;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==31 || dataSnapShotLC[MatYLC][MatXLC]==32 ||
                            dataSnapShotLC[MatYLC][MatXLC]==51 || dataSnapShotLC[MatYLC][MatXLC]==52 ||
                            dataSnapShotLC[MatYLC][MatXLC]==71 || dataSnapShotLC[MatYLC][MatXLC]==72 ||
                            dataSnapShotLC[MatYLC][MatXLC]==73 || dataSnapShotLC[MatYLC][MatXLC]==74 ||
                            dataSnapShotLC[MatYLC][MatXLC]==81 || dataSnapShotLC[MatYLC][MatXLC]==61 ) PixelCN=80;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==21) PixelCN=84;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==22) PixelCN=86;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==23) PixelCN=87;
                         else if(dataSnapShotLC[MatYLC][MatXLC]==24) PixelCN=92;
                         else PixelCN=50;
                      }
                       currentHillBasedCN[matrizPintada[j][k]-1]=currentHillBasedCN[matrizPintada[j][k]-1]+PixelCN;
                       if(PixelCN>maxHillBasedCN[matrizPintada[j][k]-1]) maxHillBasedCN[matrizPintada[j][k]-1]=PixelCN;
                       if(PixelCN<minHillBasedCN[matrizPintada[j][k]-1] || PixelCN>0) minHillBasedCN[matrizPintada[j][k]-1]=PixelCN;
                        currentHillNumPixels[matrizPintada[j][k]-1]++;
                    }

                }
System.out.println("-----------------sTART TO CALCULATE THE FINAL VALUES----------------");
        java.io.File theFile;
        theFile=new java.io.File(LandUse.getParentFile()+"/"+"classes"+".csv");
         System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

for (int j=0;j<linksStructure.contactsArray.length;j++)
               {
    newfile.write(linksStructure.contactsArray[j]+" ");
    newfile.write(currentHillNumPixels[j]+" ");
                   CN[j]=currentHillBasedCN[j]/currentHillNumPixels[j];

    newfile.write(CN[j]+" ");

                   for (int n=0;n<nclasses;n++){
                        LandUseOnBasinArray[j][n]= (100*currentHillBasedLandUse[j][n]/currentHillNumPixels[j]);
                        MaxHillBasedLandUse[j]=-9;
                        MaxHillBasedLandUsePerc[j]=0.0f;
       newfile.write(LandUseOnBasinArray[j][n]+" ");
                        }

                    for (int n=0;n<nclasses;n++){
                        if (LandUseOnBasinArray[j][n]>MaxHillBasedLandUsePerc[j]){
                            MaxHillBasedLandUsePerc[j]=LandUseOnBasinArray[j][n];
                            MaxHillBasedLandUse[j]=n;
                        }
                        }
             newfile.write(MaxHillBasedLandUse[j]+" ");
             newfile.write(MaxHillBasedLandUsePerc[j]+" ");
                    for (int n=0;n<4;n++){
                     SOILOnBasinArray[j][n]=(100*currentSoilType[j][n]/currentHillNumPixels[j]);
                     MaxHillBasedSOIL[j]=-9;
                     MaxHillBasedSOILPerc[j]=0.0f;
     newfile.write(SOILOnBasinArray[j][n]+" ");
                    }
                     for (int n=0;n<4;n++){
                        if (SOILOnBasinArray[j][n]>MaxHillBasedSOILPerc[j]){
                            MaxHillBasedSOILPerc[j]=SOILOnBasinArray[j][n];
                            MaxHillBasedSOIL[j]=n;
                        }
                     }
             newfile.write(MaxHillBasedSOIL[j]+" ");
             newfile.write(MaxHillBasedSOILPerc[j]+" ");
             newfile.write("\n");
               }







                //System.out.println("-----------------Done with this snap-shot----------------");



            System.out.println(metaLandUse.getLocationBinaryFile().getName().lastIndexOf("."));
            thisLandUseName=metaLandUse.getLocationBinaryFile().getName().substring(0,metaLandUse.getLocationBinaryFile().getName().lastIndexOf("."));
            thisSoilData=metaLandUse.getLocationBinaryFile().getName().substring(0,metaSoilData.getLocationBinaryFile().getName().lastIndexOf("."));
            success=true;

            System.out.println("-----------------Done with Files Reading----------------");

            recordResolutionInMinutes=metaLandUse.getTemporalScale()/1000.0/60.0;

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        }

    }

    /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param dateRequested The time for which the rain is desired
     * @return Returns the rainfall rate in mm/h
     */
//    public float getLandUseOnHillslope(int HillNumber,java.util.Calendar dateRequested){
//
//        return LandUseOnBasin[HillNumber].getRecord(dateRequested);
//
//    }

    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The maximum rainfall rate in mm/h
     */
//    public float getMaxLandUseOnHillslope(int HillNumber){
//
//        return LandUseOnBasin[HillNumber].getMaxRecord();
//
//    }

    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
//    public float getMeanLandUseOnHillslope(int HillNumber){
//
//        return LandUseOnBasin[HillNumber].getMeanRecord();
//
//    }

    /**
     *  A boolean flag indicating if the precipitation files were fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted(){
        return success;
    }

    /**
     * Returns the name of this LandUse event
     * @return A String that describes this LandUse
     */
    public String LandUseName(){
        return thisLandUseName;
    }

   public String SoilName(){
        return thisSoilData;
    }
    /**
     * The LandUse temporal resolution in milliseconds
     * @return A float with the temporal resolution
     */
    public float SCSResolution(){
        return metaLandUse.getTemporalScale();
    }

    /**
     * The initial LandUse time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public java.util.Calendar SCSInitialTime(){

        return InfoDate;
    }

    /**
     * The initial LandUse time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the first drop of water fell over the basin
     * on the basin
     */
    public double SCSInitialTimeInMinutes(){

        return InfoDate.getTimeInMillis()/1000./60.;
    }

    /**
     * The LandUse record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double SCSRecordResolutionInMinutes(){

        return recordResolutionInMinutes;

    }

    /**
     * The total rainfall over a given pixel of the original raster fields
     * @param i The row number of the desired location
     * @param j The column number of the desired location
     * @return The accumulated rain over the entire LandUse period
     */


    public Double getAverCN2(int HillNumber){

        return CN[HillNumber];
    }

    public Double getAverCN1(int HillNumber){
        return CN[HillNumber]-(20*(100-CN[HillNumber])/(100-CN[HillNumber]+Math.exp(2.533-0.0636*(100-CN[HillNumber]))));
    }

    public Double getAverCN3(int HillNumber){
        return CN[HillNumber]*Math.exp(0.00673*(100-CN[HillNumber]));
    }

    /**
     * The
     * @param HillNumber The index of the desired hillslope
     * @return The value of Land cover
     */
    public double minHillBasedCN(int HillNumber){
        return minHillBasedCN[HillNumber];

    }


    public double maxHillBasedCN(int HillNumber){
        return maxHillBasedCN[HillNumber];

    }

    public int getMaxHillLU(int HillNumber){
        return MaxHillBasedLandUse[HillNumber];

    }
    public float getMaxHillLUPerc(int HillNumber){

        return MaxHillBasedLandUsePerc[HillNumber];

    }

    public int getMaxHillSOIL(int HillNumber){
        return MaxHillBasedSOIL[HillNumber];

    }
    public float getMaxHillSOILPerc(int HillNumber){

        return MaxHillBasedSOILPerc[HillNumber];

    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {

    }

}
