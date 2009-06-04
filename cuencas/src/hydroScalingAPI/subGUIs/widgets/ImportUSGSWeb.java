package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author Eric Osgood
 */
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import javax.imageio.*;
import javax.swing.*;

public class ImportUSGSWeb {

    private boolean connSuccess = false;

    public ImportUSGSWeb() {
        //Test connection with water.USGS.gov
        try {
            String urlName;
            urlName = "http://water.usgs.gov/";
            URL url = new URL(urlName);
            URLConnection connection = url.openConnection();
            connection.connect();
            connSuccess = true;
        } catch (IOException e) {
            System.out.println("NO INTERNET CONNECTION FOUND " + e);
        }
    }

    public boolean connectionTest() {
        return connSuccess;
    }

    public Vector<String> filterByState(String[][] statesparam) {
        //makes URL to show what data is available for sites from USGS
        Vector filteredList = new java.util.Vector(3000);
        /*statesparam = new String[][] {{"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL",
        "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME",
        "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
        "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI",
        "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI",
        "WY"},{"Stream", "Gage Height", "Volume"}};*/
        try {
            for (int i = 0; i < statesparam[0].length; i++) {
                System.out.println(statesparam[0][i]);  //test
                URL sitedata = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=" + statesparam[0][i] + "&site_tp_cd=OC&site_tp_cd=OC-CO&site_tp_cd=ES&site_tp_cd=LK&site_tp_cd=ST&site_tp_cd=ST-CA&site_tp_cd=ST-DCH&site_tp_cd=ST-TS&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,site_tp_cd,realtime_parameter_selection");
                System.out.println("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=" + statesparam[0][i] + "&site_tp_cd=OC&site_tp_cd=OC-CO&site_tp_cd=ES&site_tp_cd=LK&site_tp_cd=ST&site_tp_cd=ST-CA&site_tp_cd=ST-DCH&site_tp_cd=ST-TS&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,site_tp_cd,realtime_parameter_selection");

                BufferedReader in = new BufferedReader(new InputStreamReader(sitedata.openStream()));
                //BufferedWriter fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(statesparam[0][i] + "out.txt"))));
                String str;
                while ((str = in.readLine()) != null) {
                    //fileOut.write(str);
                    //fileOut.write("\n");
                    if (str.charAt(0) == 'U') {
                        filteredList.add(str);
                    //System.out.println(str);
                    }
                }
                in.close();
            //fileOut.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return filteredList;
    }

    public Vector<String> filterByLatLon(String[] bound) {
        Vector latLonList = new Vector();
        System.out.println(bound);

        try {
            URL latlonsite = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&nw_longitude_va="+ bound[0] +"&nw_latitude_va=" + bound[1] + "&se_longitude_va=" + bound[2] + "&se_latitude_va=" + bound[3] + "&coordinate_format=decimal_degrees&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=lat_long_bounding_box,realtime_parameter_selection");
            System.out.println("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&nw_longitude_va="+ bound[0] +"&nw_latitude_va=" + bound[1] + "&se_longitude_va=" + bound[2] + "&se_latitude_va=" + bound[3] + "&coordinate_format=decimal_degrees&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=lat_long_bounding_box,realtime_parameter_selection");
            BufferedReader in = new BufferedReader(new InputStreamReader(latlonsite.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.charAt(0) == 'U') {
                    
                    latLonList.add(str);
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return latLonList;
    }

    public void getData(String[] s, Vector v) throws IOException {

        for (int i = 0; i < v.size(); i++) {

            String thisCode = (String) v.get(i);
            String startDate, endDate;
            startDate = s[0];
            endDate = s[1];

            System.out.println(startDate + endDate);

            String outputDir1, outputDir2;
            outputDir1 = "C:\\Users\\StreamFlow";
            outputDir2 =  "C:\\Users\\Stage";
            String outputDirs;
            outputDirs = outputDir1 + outputDir2;

            boolean success = (new File(outputDirs)).mkdirs();
            if (success) {
                System.out.println("Directories: " + outputDirs + " created");
            } else{
                System.out.println("Directories not created");
            }
           




            File theFile = new File(outputDirs + thisCode + ".txt.gz");
            FileOutputStream outputLocal = new FileOutputStream(theFile);
            GZIPOutputStream outputCompressed = new java.util.zip.GZIPOutputStream(outputLocal);
            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(outputCompressed));

            URL siteinfo = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&index_pmcode_00042=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=site_tp_cd&column_name=lat_va&column_name=long_va&column_name=state_cd&column_name=alt_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            System.out.println("SITEINFO URL:  " + siteinfo);

            BufferedReader in2 = new BufferedReader(new InputStreamReader(siteinfo.openStream()));

            boolean eof1 = false;
            while (!eof1) {
                String line = in2.readLine();
                if (line == null) {
                    eof1 = true;
                } else {
                    if (line.charAt(0) == 'U') {
                        String[] linarray = line.split("\t");
                        float area;
                        float altitude;
                        dataOut.write("[code]" + '\n');
                        dataOut.write(linarray[1] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[agency]" + '\n');
                        dataOut.write(linarray[0] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[type]" + '\n');
                        dataOut.write("Streamflow" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[site name]" + '\n');
                        dataOut.write(linarray[2] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[stream name]" + '\n');
                        dataOut.write(linarray[2] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[county]" + '\n');
                        dataOut.write('\n');
                        dataOut.write('\n');
                        dataOut.write("[state]" + '\n');

                        //STATE METHOD!!!
                        String state;
                        String st = linarray[8];
                        state = states(st);
                         //each state has a # assigned to it.  http://water.usgs.gov/watuse/data/ascii/codata_bystate/ma90co
                        dataOut.write(state);
                        dataOut.write('\n');



                        dataOut.write('\n');
                        dataOut.write("[data source]" + '\n');
                        dataOut.write("URL: http://nwis.waterdata.usgs.gov/nm/nwis/inventory" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[latitude (deg:min:sec)]" + '\n');
                        String lat = linarray[4];
                        lat = lat.substring(0, 2) + ":" + lat.substring(2, 4) + ":" + lat.substring(4, 6) + " N";
                        dataOut.write(lat + '\n');
                        dataOut.write('\n');
                        dataOut.write("[longitude (deg:min:sec)]" + '\n');
                        String lon = linarray[5];
                        lon = lon.substring(0, 3) + ":" + lon.substring(3, 5) + ":" + lon.substring(5, 7) + " W";
                        dataOut.write(lon + '\n');
                        dataOut.write('\n');
                        dataOut.write("[altitude ASL (m)]" + '\n');

                        if (linarray.length ==10) {
                        altitude = Float.parseFloat(linarray[9]);
                        altitude = altitude * 0.3048f;      // *0.3048 to m
                        dataOut.write(String.valueOf(altitude) + '\n');
                        } else {
                            dataOut.write("N / A" + '\n');
                        }
                        
                        dataOut.write('\n');
                        dataOut.write("[drainage area (km^2)]" + '\n');   // raw USGS drainage area in mi^2

                        if (linarray.length == 13) {
                        area = Float.parseFloat(linarray[12]);
                        area = area * 2.5899f;        // *2.5899 to km^2
                        dataOut.write(String.valueOf(area) + '\n');
                        
                        } else {
                            dataOut.write("N / A" + '\n');
                        }


                        dataOut.write('\n');
                        dataOut.write("[data units]" + '\n');
                        dataOut.write("ft^3/s" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[data accuracy]" + '\n');
                        dataOut.write('\n');
                        dataOut.write('\n');
                        dataOut.write("[data (yyyy.mm.dd.hh.mm.ss    value)]" + "\n");
                    }
                }
            }

            URL sitedata = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&sitefile_output_format=html_table&column_name=agency_cd&column_name=site_no&column_name=station_nm&range_selection=date_range&begin_date=" + startDate + "&end_date=" + endDate + "&format=rdb&date_format=YYYY.MM.DD&rdb_compression=value&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            System.out.println("SITEDATA URL:  " + sitedata);

            BufferedReader buff2 = new BufferedReader(new InputStreamReader(sitedata.openStream()));

            boolean eof2 = false;
            while (!eof2) {
                String line = buff2.readLine();
                if (line == null) {
                    eof2 = true;
                    System.out.println("Created new data file!!! " + thisCode + ".txt.gz");
                    System.out.println('\n');
                    System.out.println('\n');
                } else {
                    if (line.charAt(0) == 'U') {
                        line = line.substring(14);
                        if (line.length() > 11) { //11 is length of data + tab....(no data)

                            try {
                                if (line.substring(11).split("\t").length > 0) {
                                    Float.parseFloat(line.substring(11).split("\t")[0]);
                                    String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + line.substring(11).split("\t")[0];
                                    dataOut.write(lineToWrite + '\n');
                                } else {
                                    String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + "-9999";
                                    dataOut.write(lineToWrite + "\n");
                                }
                            } catch (NumberFormatException nfe) {
                                String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + "-9999";
                                dataOut.write(lineToWrite + "\n");
                            }
                        }
                    }
                }
            }
            in2.close();
            dataOut.close();
        }
    }

    public String states(String st) {

        int sn = Integer.parseInt(st);

        String state = null;
        String[] stABR = {"Alabama", "Alaska", "", "Arizona", "Arkansas", "California", "", "Colorado", "Connecticut","Delaware","District of Columbia",
        "Florida", "Georgia", "", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Massachusetts", "Maine","MaryLand", "", "Michigan",
        "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York",
        "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsilvania", "", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas",
        "Utah", "Vermont", "Virginia", "", "Washington", "West Virginia", "Wisconsin", "Wyoming"};
        
        state = stABR[sn-1];
        
        return state;
    }

    static void main(String[] arguments) {
        ImportUSGSWeb test = new ImportUSGSWeb();

        String[][] statesparam = new String[][]{{"WI"}, {"Stream", "Gage Height", "Volume"}};


        Vector myVector = test.filterByState(statesparam);
        for (Iterator it = myVector.iterator(); it.hasNext();) {
            System.out.println(it.next());
        }
        System.out.println(test.connectionTest());
    }
}
