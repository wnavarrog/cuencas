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
 * writeUSGSdataWeb.java
 *
 * Created on July 7, 2003, 3:19 PM
 */

package hydroScalingAPI.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class was developed to import daily streamflow time series from the USGS
 * website.  A Gauge-type site is created in the database. Currnently, only
 * streamflow time series can be imported,  Future implementations will include
 * flow depth and other variables
 * @author Peter Furey
 */
public class ImportUSGS_StremMeasubyState {
    
    String output_dir;
    String state;
    String gaugeid;
    char record_type;
    String filesuffix;
    java.util.StringTokenizer str1;
    
    /**
     * Creates an instance of the ImportUSGS_waterdataWeb
     * @param oDir The directory where the ouput file will be stored
     */
    public ImportUSGS_StremMeasubyState(String oDir) {
        output_dir=oDir;
    }
    
    /**
     * This method imports streamflow data and creates a Gauge-type site file in the
     * database
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param gaugeid The USGS gauge code (e.g. 09430500 for the Gila River at Gila gauge)
     * @param record_type "H": for historical and "R": for recent.
     */

    /**
     * This method imports streamflow data and creates a Gauge-type site file in the
     * database
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param gaugeid The USGS gauge code (e.g. 09430500 for the Gila River at Gila gauge)
     * @param record_type "H": for historical and "R": for recent.
     */



    @SuppressWarnings("empty-statement")
    public void StreamFlow(String state, int StateCd) throws FileNotFoundException, IOException {
        
          
            //new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
            java.io.File theFile = new java.io.File(output_dir+state+"_sites.txt");
            java.io.File theFile2 = new java.io.File(output_dir+state+"_data.txt");
            java.io.File theFile3 = new java.io.File(output_dir+state+"_data_qual.txt");
            java.io.FileOutputStream outputLocal=new java.io.FileOutputStream(theFile);
            java.io.FileOutputStream outputLocal2=new java.io.FileOutputStream(theFile2);
            java.io.FileOutputStream outputLocal3=new java.io.FileOutputStream(theFile3);
            java.io.BufferedWriter dataoutsite= new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputLocal));
            java.io.BufferedWriter dataoutdata= new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputLocal2));
            java.io.BufferedWriter dataoutdata2= new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputLocal3));

            
            // Copy site info from USGS file to new data file

            java.net.URL remotePath1=new java.net.URL("http://waterdata.usgs.gov/nwis/measurements?state_cd="+state+"&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=dec_lat_va&column_name=dec_long_va&column_name=drain_area_va&column_name=sv_count_nu&column_name=basin_cd&column_name=huc_cd&set_logscale_y=1&channel_html_info=0&date_format=YYYY-MM-DD&channel_rdb_info=0&rdb_compression=file&list_of_search_criteria=state_cd");
            
            //java.net.URL remotePath1=new java.net.URL("http://waterdata.usgs.gov/nwis/inventory/?site_no="+gaugeid+"&amp");
            System.out.println("http://waterdata.usgs.gov/nwis/measurements?state_cd="+state+"&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=dec_lat_va&column_name=dec_long_va&column_name=drain_area_va&column_name=sv_count_nu&column_name=basin_cd&column_name=huc_cd&set_logscale_y=1&channel_html_info=0&date_format=YYYY-MM-DD&channel_rdb_info=0&rdb_compression=file&list_of_search_criteria=state_cd");

            java.io.InputStream inputRemote1=remotePath1.openStream();
            java.io.BufferedReader buff1 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote1));
                     String lineSite=buff1.readLine();
                     
            boolean eof1 = false;
            //dataout.write("#Agency "+ "," + "site_no"+ "," + "Site_name"+ "," + "lat"+ "," + "long"+ "," + "alt_va"+ "," + "alt_acu"+ "," + "drainArea" + '\n');

            double [] drainarea = new double[200000];
            double [] lat = new double[200000];
            double [] lon = new double[200000];
            int [] sitecd  = new int[200000];
            String [] siteSt  = new String[200000];
            int [] HU = new int[200000];
            int count=0;
            int count_mea=0;

            while(!eof1){
                lineSite=buff1.readLine();
                if (lineSite==null) {
                    eof1=true;
                } else {
                    if (lineSite.charAt(0)=='U'){    // U for USGS
                    System.out.println(lineSite);
                    
                    String [] linarray=lineSite.split("\t");

                    try{
                    FileWriter fstream = new FileWriter("D:/Lu_docs/PHD/hyd_geome/AllSites.txt",true);
                    BufferedWriter out = new BufferedWriter(fstream);
                     gaugeid=linarray[1];

                     if(gaugeid!=null){
                     for (int i=0;i<10;i++) if(linarray[i].equals("")) linarray[i]="-9.9";

                     out.write(StateCd+
                               "," + linarray[1]+ // Site code
                               "," + linarray[2]+ // Site code
                               "," + linarray[3]+ // Site code
                               "," + linarray[6]+ // Site code
                               "," + linarray[9]+ // Site code
                               "\n");


                     dataoutsite.write(StateCd+
                               "," + linarray[1]+ // Site code
                               "," + linarray[2]+ // Site code
                               "," + linarray[3]+ // Site code
                               "," + linarray[6]+ // Site code
                               "," + linarray[9]+ // Site code
                               "\n");
                        

                    System.out.println( linarray[1] + "linarray[2]   " + linarray[2] + "linarray[3] " +linarray[3]+"llinarray[4]   "+linarray[4]+"llinarray[5]   "+linarray[5]+"llinarray[6]   "+linarray[6]+"llinarray[7]   "+linarray[7]+"llinarray[8]   "+linarray[8]+"llinarray[9]   "+linarray[9]+"/n");
                    siteSt[count]=linarray[1];
                    sitecd[count]=Integer.parseInt(linarray[1]);
                    lat[count]=Double.parseDouble(linarray[2]);
                    lon[count]=Double.parseDouble(linarray[3]);
                    HU[count]=Integer.parseInt(linarray[9]);

                    drainarea[count]=Double.parseDouble(linarray[6]);
                    //System.out.println( count + "checking" + siteSt[count] + "sitecd   " + sitecd + "drainarea[count] " +drainarea[count]);
                    count=count+1;
                    //Close the output stream
                    out.close();}

                     }catch (Exception e){//Catch exception if any
                     System.err.println("Error: do not write info in the site" + e.getMessage() +" "+ sitecd[count]+" "+lat[count]+" "+lon[count]+" "+drainarea[count] +" "+HU[count]);
                     }
                    //INSERT THE SEARCH FOR SITE DATA                          
                    }
                }

            }
        
    
   for (int is=0;is<count;is++){
       int isite=is+1;    
        count_mea=0;
        System.out.println("state" + state + "site" + siteSt[is] +"is="+ is +"nsites" + count);
        java.net.URL remotePath2=new java.net.URL("http://waterdata.usgs.gov/nwis/measurements?site_no="+siteSt[is]+"&agency_cd=USGS&format=rdb_expanded&date_format=YYYY");
        System.out.println("http://waterdata.usgs.gov/nwis/measurements?site_no="+siteSt[is]+"&agency_cd=USGS&format=rdb_expanded&date_format=YYYY");
        java.io.InputStream inputRemote2=remotePath2.openStream();
        java.io.BufferedReader buff2 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote2));
        boolean eof2 = false;

        while(!eof2){
              String line=buff2.readLine();

              if (line==null) {
                  eof2=true;
              } else {
                  if (line.charAt(0)=='U'){    // U for USGS
                            
                  String [] linarray2=line.split("\t");   // System.out.println(line);
                  int control=-9;
                  int qual=-9;
                  int meas_type=-9;
                  int q_type=-9;
                  int v_type=-9;
                  String year = linarray2[3].substring(0,4);
                     //String year = linarray2[3].substring((linarray2[3].lastIndexOf("/",0)+1),linarray2[3].lastIndexOf("/",0)+5);
                  if(linarray2.length>=23) {for (int i=0;i<=23;i++) {if(linarray2[i].equals("")) linarray2[i]="-9.9";}

                  if(linarray2[10].equals("UNSP")) qual=0;
                  if(linarray2[10].equals("POOR")) qual=1;
                  if(linarray2[10].equals("FAIR")) qual=2;
                  if(linarray2[10].equals("GOOD")) qual=3;
                  if(linarray2[10].equals("EXCL")) qual=4;


                     if(linarray2[13].equals("CLER")) control = 0;
                     if(linarray2[13].equals("CICE")) control = 1;
                     if(linarray2[13].equals("SICE")) control = 2;
                     if(linarray2[13].equals("ALGA")) control = 3;
                     if(linarray2[13].equals("SUBM")) control = 4;
                     if(linarray2[13].equals("HVDB")) control = 5;
                     if(linarray2[13].equals("UNSP")) control = 6;
                     if(linarray2[13].equals("FILL")) control = 7;
                     if(linarray2[13].equals("MALT")) control = 8;
                     if(linarray2[13].equals("MAMD")) control = 9;
                     if(linarray2[13].equals("AICE")) control = 10;
                     if(linarray2[13].equals("SCUR")) control = 11;

if(linarray2[17].equals("WADE")) meas_type=0;
if(linarray2[17].equals("UNSP")) meas_type=1;
if(linarray2[17].equals("ICE")) meas_type=2;
if(linarray2[17].equals("CRAN")) meas_type=3;
if(linarray2[17].equals("BRDS")) meas_type=4;
if(linarray2[17].equals("MBOT")) meas_type=5;
if(linarray2[17].equals("OTHR")) meas_type=6;
if(linarray2[17].equals("BRUS")) meas_type=7;
if(linarray2[17].equals("CWAY")) meas_type=8;
if(linarray2[17].equals("BOAT")) meas_type=9;
if(linarray2[17].equals("SBOT")) meas_type=10;



                     if(linarray2[18].equals("QUNSP")) q_type = 1;
                     if(linarray2[18].equals("QSCMM")) q_type = 2;
                     if(linarray2[18].equals("QADCP")) q_type = 3;
                     if(linarray2[18].equals("QIDIR")) q_type = 4;
                     if(linarray2[18].equals("QFLUM")) q_type = 5;
                     if(linarray2[18].equals("Q-EST")) q_type = 6;

if(linarray2[19].equals("VADCP")) v_type = 1;
if(linarray2[19].equals("VADV")) v_type = 2;
if(linarray2[19].equals("VPAA")) v_type = 3;
if(linarray2[19].equals("VICE")) v_type = 4;
if(linarray2[19].equals("VIPAA")) v_type =5;
if(linarray2[19].equals("VPYG")) v_type = 6;
if(linarray2[19].equals("VRAD")) v_type = 7;}


                   try{

                    // FileWriter fstream = new FileWriter("D:/Lu_docs/PHD/hyd_geome//AllMeas.txt",true);
                     //BufferedWriter out = new BufferedWriter(fstream);
                    FileWriter fstream2 = new FileWriter("D:/Lu_docs/PHD/hyd_geome//AllMeasEG_Cl.txt",true);
                    BufferedWriter out2 = new BufferedWriter(fstream2);
                    //out.write(line+ lineSite2+ "\n");
                    double q=Double.parseDouble(linarray2[20]);
                    double w=Double.parseDouble(linarray2[21]);               
                    double a=Double.parseDouble(linarray2[22]);
                    double v=Double.parseDouble(linarray2[23]);
                           
                     if(qual>=3 &&  control == 0 && q>0 && w>0 && v>0 && a>0 && linarray2.length>=23)
                     {          count_mea=count_mea+1;
                                out2.write(
                                StateCd+
                                 "," + linarray2[1]+ // Site code
                                 "," + isite +
                                 "," + lat[is]+ //lat
                                 "," + lon[is]+ //lon
                                 "," + drainarea[is]+ //drainage area
                                 "," + HU[is]+ //drainage area
                                 "," + linarray2[2]+ //meas_no
                                 "," + year+ // meas_year_fix
                                 "," + linarray2[5]+ // diachrage_VA
                                 "," + linarray2[6]+ // height
                                 "," + qual+ // measu _quality_fix
                                 "," + control+ // control_fix
                                 "," + q_type+ // meas_type_fix
                                 "," + v_type+ // streamflow method_fix
                                 "," + linarray2[20]+ // Discharge
                                 "," + linarray2[21]+ // width
                                 "," + linarray2[22]+ // velocity
                                 "," + linarray2[23]+ // area
                                 "\n");

                                dataoutdata2.write(
                                StateCd+
                                 "," + linarray2[1]+ // Site code
                                 "," + isite +
                                 "," + lat[is]+ //lat
                                 "," + lon[is]+ //lon
                                 "," + drainarea[is]+ //drainage area
                                 "," + HU[is]+ //drainage area
                                 "," + linarray2[2]+ //meas_no
                                 "," + year+ // meas_year_fix
                                 "," + linarray2[5]+ // diachrage_VA
                                 "," + linarray2[6]+ // height
                                 "," + qual+ // measu _quality_fix
                                 "," + control+ // control_fix
                                 "," + q_type+ // meas_type_fix
                                 "," + v_type+ // streamflow method_fix
                                 "," + linarray2[20]+ // Discharge
                                 "," + linarray2[21]+ // width
                                 "," + linarray2[22]+ // velocity
                                 "," + linarray2[23]+ // area
                                 "\n");
                                  }
                     out2.close();

                     if(linarray2.length>=23){
                     dataoutdata.write(
                                StateCd+
                                 "," + linarray2[1]+ // Site code
                                 "," + isite +
                                 "," + lat[is]+ //lat
                                 "," + lon[is]+ //lon
                                 "," + drainarea[is]+ //drainage area
                                 "," + HU[is]+ //drainage area
                                 "," + linarray2[2]+ //meas_no
                                 "," + year+ // meas_year_fix
                                 "," + linarray2[5]+ // diachrage_VA
                                 "," + linarray2[6]+ // height
                                 "," + qual+ // measu _quality_fix
                                 "," + control+ // control_fix
                                 "," + q_type+ // meas_type_fix
                                 "," + v_type+ // streamflow method_fix
                                 "," + linarray2[20]+ // Discharge
                                 "," + linarray2[21]+ // width
                                 "," + linarray2[22]+ // velocity
                                 "," + linarray2[23]+ // area
                                 "\n");}

                     }catch (Exception e){//Catch exception if any
                     if (linarray2.length>=23)System.err.println("Error: do not write disch" + e.getMessage() + " " + linarray2[20] + " " + linarray2[21]+ " " + linarray2[22]+ " " + linarray2[23]);
                     if (linarray2.length<23)System.err.println("Error: do not write disch" + e.getMessage() + "not enough elements");
                     }


                }
            }
          }

          buff2.close();         

           try{
              FileWriter fstream = new FileWriter("D:/Lu_docs/PHD/hyd_geome/AllSiteswithdata.txt",true);
              BufferedWriter out = new BufferedWriter(fstream);
              if(count_mea>0){
              out.write(StateCd+
                               "," + sitecd[is]+
                                 "," + isite +
                                 "," + lat[is]+ //lat
                                 "," + lon[is]+ //lon
                                 "," + drainarea[is]+ //drainage area
                                 "," + HU[is]+ //drainage area
                               "," + count_mea+ // Site code
                               "\n");}


                    //Close the output stream
              out.close();

              }catch (Exception e){//Catch exception if any
              System.err.println("Error: site with meas " + e.getMessage() + " " + sitecd );
              }


                
            }

            buff1.close();
            dataoutsite.close();
            dataoutdata.close();
            dataoutdata2.close();
            // Copy data from USGS file to new data file ...
            // Raw USGS discharge values in ft^3/s
           //System.out.println("http://waterdata.usgs.gov/nwis/measurements?site_no="+gaugeid+"&agency_cd=USGS&format=rdb_expanded");
    }

    
    /**
     * Tests for the class
     * @param arguments Comand line arguments
     */
    public static void main(String[ ] arguments) throws FileNotFoundException, IOException { //throws java.io.IOException IOE{

        String[] states={"al","ak","az","ar","ca","co","ct","de","dc","fl","ga","hi","id","il","in","ia","ks","ky","la","me","md","ma","mi","mn","ms","mo","mt","ne","nv","nh","nj","nm","ny","nc","nd","oh","ok","or","pa","ri","sc","sd","tn","tx","ut","vt","va","wa","wv","wi","wy"};
        int[] st_array= {4,5,9,15,16,32,33};
     //        for (int i : st_array)
     //{
        for (int i=43;i>=0;i--){
              String st=states[i];
              String OutputDir="D:/Lu_docs/PHD/hyd_geome/"+st+"/";
              new File(OutputDir).mkdirs();
              System.out.println(OutputDir);
              ImportUSGS_StremMeasubyState data = new ImportUSGS_StremMeasubyState(OutputDir);
              data.StreamFlow(st,i);
        }
        
        

    }
    
}
