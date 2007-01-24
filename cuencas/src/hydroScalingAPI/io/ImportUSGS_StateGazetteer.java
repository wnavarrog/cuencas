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
 * ImportUSGS_StateGazetteer.java
 *
 * Created on June 14, 2003, 12:13 AM
 */

package hydroScalingAPI.io;

/**
 * This class populates the site database
 * @author Ricardo Mantilla
 */
public class ImportUSGS_StateGazetteer {
    
    /*
        1-2 State Alpha Code 
        4-53 Feature Name 
        55-63  Feature Type  
        65-79 County Name 
        81-96 Geographic Coordinates 
        98-113 Source Coordinates for Linear Features 
        115-120 Elevation  
        122-148 Cell Name 
    */

    
    /** Creates a new instance of ImportUSGS_StateGazetteer */
    public ImportUSGS_StateGazetteer(String state,String[] validFeatures, String outputDir, float[] limits) {
        
        java.util.Vector validFeatureTypes=new java.util.Vector();
        for (int i=0;i<validFeatures.length;i++){
            validFeatureTypes.addElement(validFeatures[i]);
        }
        
        try{
            java.net.URL remotePath=new java.net.URL("http://geonames.usgs.gov/geonames/stategaz/"+state);
            java.io.InputStream inputRemoto=remotePath.openStream();
            java.io.BufferedReader bufferRemoto = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemoto));

            String locationInfo=bufferRemoto.readLine();
            int i=1;
            //while (i<1000) {
            while (locationInfo != null){
                
                USGS_LocationProperties thisLocation=new USGS_LocationProperties(state);
                thisLocation.StateAlphaCode=locationInfo.substring(0,2);
                thisLocation.State=hydroScalingAPI.tools.StateName.StateName(state);
                thisLocation.Name=locationInfo.substring(3,52).trim();
                thisLocation.Type=locationInfo.substring(54,63).trim();
                thisLocation.County=locationInfo.substring(64,79).trim();
                thisLocation.latitude=locationInfo.substring(80,87);
                if (thisLocation.latitude.equalsIgnoreCase("UNKNOWN")) {
                    thisLocation.latitude="N/A";
                    thisLocation.latitudeNumeric=0;
                } else {
                    thisLocation.latitude=locationInfo.substring(80,82)+":"+locationInfo.substring(82,84)+":"+locationInfo.substring(84,86)+" N";
                    thisLocation.latitudeNumeric=Float.parseFloat(locationInfo.substring(80,82))+Float.parseFloat(locationInfo.substring(82,84))/60.0f+Float.parseFloat(locationInfo.substring(84,86))/3600.0f;
                    
                }
                thisLocation.longitude=locationInfo.substring(88,95);
                if (thisLocation.longitude.equalsIgnoreCase("UNKNOWN")) {
                    thisLocation.longitude="N/A";
                    thisLocation.longitudeNumeric=0;
                } else {
                    thisLocation.longitude=locationInfo.substring(88,91)+":"+locationInfo.substring(91,93)+":"+locationInfo.substring(93,95)+" W";
                    thisLocation.longitudeNumeric=-(Float.parseFloat(locationInfo.substring(88,91))+Float.parseFloat(locationInfo.substring(91,93))/60.0f+Float.parseFloat(locationInfo.substring(93,95))/3600.0f);
                }
                thisLocation.Elevation=locationInfo.substring(114,119).trim();
                if (thisLocation.Elevation.equalsIgnoreCase("")) thisLocation.Elevation="N/A";
                
                if (validFeatureTypes.contains(thisLocation.Type)){
                    if(thisLocation.latitudeNumeric > limits[0] & thisLocation.latitudeNumeric < limits[1] & thisLocation.longitudeNumeric > limits[2] & thisLocation.latitudeNumeric <limits[3]){
                        new java.io.File(outputDir+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
                        java.io.File theFile = new java.io.File(outputDir+thisLocation.State+"/"+thisLocation.Type+"/"+thisLocation.Name.toLowerCase()+".txt.gz");
                        java.io.FileOutputStream inputLocal=new java.io.FileOutputStream(theFile);
                        java.util.zip.GZIPOutputStream inputComprim=new java.util.zip.GZIPOutputStream(inputLocal);
                        java.io.BufferedWriter bufferLocalW= new java.io.BufferedWriter(new java.io.OutputStreamWriter(inputComprim));

                        System.out.println(theFile);

                        bufferLocalW.write("[type]"+"\n");
                        bufferLocalW.write(thisLocation.Type+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[source]"+"\n");
                        bufferLocalW.write("USGS"+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[site name]"+"\n");
                        bufferLocalW.write(thisLocation.Name+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[county]"+"\n");
                        bufferLocalW.write(thisLocation.County+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[state]"+"\n");
                        bufferLocalW.write(thisLocation.State+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[latitude (deg:min:sec)]"+"\n");
                        bufferLocalW.write(thisLocation.latitude+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[longitude (deg:min:sec)]"+"\n");
                        bufferLocalW.write(thisLocation.longitude+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[altitude ASL (m)]"+"\n");
                        bufferLocalW.write(thisLocation.Elevation+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[images]"+"\n");
                        for(int j=0;j<thisLocation.Images.length;j++) bufferLocalW.write(thisLocation.Images[j]+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[information]"+"\n");
                        bufferLocalW.write(thisLocation.Source+"\n");
                        bufferLocalW.write("\n");

                        bufferLocalW.close();
                        inputComprim.close();
                        inputLocal.close();
                    }
                }
                locationInfo=bufferRemoto.readLine();
                i++;
            }
            bufferRemoto.close();
            inputRemoto.close();
            
            
        }catch(java.net.MalformedURLException MUE){
            System.err.println(MUE);
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        /*
            airport - manmade facility maintained for the use of aircraft (airfield, airstrip, landing field, landing strip).
            arch - natural arch-like opening in a rock mass (bridge, natural bridge, sea arch).
            area - any one of several areally extensive natural features not included in other categories (badlands, barren, delta, fan, garden).
            arroyo - watercourse or channel through which water may occasionally flow (coulee, draw, gully, wash).
            bar - natural accumulation of sand, gravel, or alluvium forming an underwater or exposed embankment (ledge, reef, sandbar, shoal, spit).
            basin - natural depression or relatively low area enclosed by higher land (amphitheater, cirque, pit, sink).
            bay - indentation of a coastline orshoreline enclosing a part of a body of water; a body of water partly surrounded by land (arm, bight, cove, estuary, gulf, inlet, sound).
            beach - the sloping shore along a body of water that is washed by waves or tides and is usually covered by sand or gravel (coast, shore, strand).
            bench - area of relatively level land on the flank of an elevation such as a hill, ridge, or mountain where the slope of the land rises on one side and descends on the opposite side (level).
            bend - curve in the course of a stream and (or) the land within the curve; a curve in a linear body of water (bottom, loop, meander).
            bridge - manmade structure carrying a trail, road, or other transportation system across a body of water or depression (causeway, overpass, trestle).
            building - a manmade structure with walls and a roof for protection of people and (or) materials, but not including church, hospital, or school.
            canal - manmade waterway used by watercraft or for drainage, irrigation, mining, or water power (ditch, lateral).
            cape - projection of land extending into a body of water (lea, neck, peninsula, point).
            cave - natural underground passageway or chamber, or a hollowed out cavity in the side of a cliff (cavern, grotto).
            cemetery - a place or area for burying the dead (burial, burying ground, grave, memorial garden).
            channel - linear deep part of a body of water through which the main volume of water flows and is frequently used as aroute for watercraft (passage, reach, strait, thoroughfare, throughfare).
            church - building used for religious worship (chapel, mosque, synagogue, tabernacle, temple).
            civil - a political division formed for administrative purposes (borough, county, municipio, parish, town, township).
            cliff - very steep or vertical slope (bluff, crag, head, headland, nose, palisades, precipice, promontory, rim, rimrock).
            crater - circular-shaped depression at the summit of a volcanic cone or one on the surface of the land caused by the impact of a meteorite; a manmade depression caused by an explosion (caldera, lua).
            crossing - a place where two or more routes of transportation form a junction or intersection (overpass, underpass).
            dam - water barrier or embankment built across the course of a stream or into a body of water to control and (or) impound the flow of water (breakwater, dike, jetty).
            falls - perpendicular or very steep fall of water in the course of a stream (cascade, cataract, waterfall).
            flat - relative level area within a region of greater relief (clearing, glade, playa).
            forest - bounded area of woods, forest, or grassland under the administration of a political agency (see woods) (national forest, national grasslands, State forest).
            gap - low point or opening between hills or mountains or in a ridge or mountain range (col, notch, pass, saddle, water gap, wind gap).
            geyser - eruptive spring from which hot water and (or) steam and in some cases mud are periodically thrown.
            glacier - body or stream of ice moving outward and downslope from an area of accumulation; an area of relatively permanent snow or ice on the top or side of a mountain or mountainous area (icefield, ice patch, snow patch).
            gut - relatively small coastal waterway connecting larger bodies of water or other waterways (creek, inlet, slough).
            harbor - sheltered area of water where ships or other watercraft can anchor or dock (hono, port, roads, roadstead).
            hospital - building where the sick or injured may receive medical or surgical attention (infirmary).
            island - area of dry or relatively dry land surrounded by water or low wetland (archipelago, atoll, cay, hammock, hummock, isla, isle, key, moku, rock).
            isthmus - narrow section of land in a body of water connecting two larger land areas.
            lake - natural body of inland water (backwater, lac, lagoon, laguna, pond, pool, resaca, waterhole).
            lava - formations resulting from the consolidation of molten rock on the surface of the Earth (kepula, lava flow).
            levee - natural or manmade embankment flanking a stream (bank, berm).
            locale - place at which there is or was human activity; it does not include populated places, mines, and dams (battlefield, crossroad, camp, farm, ghost town, landing, railroad siding, ranch, ruins, site, station, windmill).
            mine - place or area from which commercial minerals are or were removed from the Earth; not including oilfield (pit, quarry, shaft).
            oilfield - area where petroleum is or was removed from the Earth.
            other - category for miscellaneous named manmade entities that cannot readily be placed in the other feature classes listed here.
            park - place or area set aside for recreation or preservation of a cultural or natural resource and under some form of government administration; not including National or State forests or Reserves (national historical landmark, national park, State park, wilderness area).
            pillar - vertical, standing, often spire-shaped, natural rock formation (chimney, monument, pinnacle, pohaku, rock tower).
            plain - a region of general uniform slope, comparatively level and of considerable extent (grassland, highland, kula, plateau, upland).
            ppl - (populated place) place or area with clustered or scattered buildings and a permanent human population (city, settlement, town, village).
            rapids - fast-flowing section of a stream, often shallow and with exposed rock or boulders (riffle, ripple).
            reserve - a tract of land set aside for a specific use (does not include forests, civil divisions, parks).
            reservoir - artificially impounded body of water (lake, tank).
            ridge - elevation with a narrow, elongated crest which can be part of a hill or mountain (crest, cuesta, escarpment, hogback, lae, rim, spur).
            school - building or group of buildings used as an institution for study, teaching, and learning (academy, college, high school, university).
            sea - large body of salt water (gulf, ocean).
            slope - a gently inclined part of the Earth's surface (grade, pitch).
            spring - place where underground water flows naturally to the surface of the Earth (seep).
            stream - linear body of water flowing on the Earth's surface (anabranch, awawa, bayou, branch, brook, creek, distributary, fork, kill, pup, rio, river, run, slough).
            summit - prominent elevation rising above the surrounding level of the Earth's surface; does not include pillars, ridges, or ranges (ahu, berg, bald, butte, cerro, colina, cone, cumbre, dome, head, hill, horn, knob, knoll, mauna, mesa, mesita, mound, mount, mountain, peak, puu, rock, sugarloaf, table, volcano).
            swamp - poorly drained wetland, fresh or saltwater, wooded or grassy, possibly covered with open water (bog, cienega, marais, marsh, pocosin).
            trail - route for passage from one point to another; does not include roads or highways (jeep trail, path, ski trail). 
            tower - a manmade structure, higher than its diameter, generally used for observation, storage, or electronic transmission.
            tunnel - linear underground passageway open at both ends.
            valley - linear depression in the Earth's surface that generally slopes from one end to the other (barranca, canyon, chasm, cove, draw, glen, gorge, gulch, gulf, hollow, ravine).
            well - manmade shaft or hole in the Earth's surface used to obtain fluid or gaseous materials.
            woods - small area covered with a dense growth of trees; does not include an area of trees under the administration of a political agency (see forest).
         */
        
        float[] limits = {35,40,-115,-110};
        new ImportUSGS_StateGazetteer("arizona",new String[] {"ppl"},"/hidrosigDataBases/Continental_US_database/Sites/Locations/",limits);
    }
    
}

class USGS_LocationProperties {
    public String StateAlphaCode;
    public String State;
    public String Name;
    public String Type;
    public String County;
    public String latitude;
    public float latitudeNumeric;
    public String longitude;
    public float longitudeNumeric;
    public String Source="United States Geological Survey - Geographic Names Information System - Downloadable State and Topical Gazetteer Files - http://geonames.usgs.gov/geonames/stategaz/index.html";
    public String Elevation;
    public String[] Images={"N/A"};
    public String CellName;
    
    public USGS_LocationProperties(String st) {
        State=st;
    }
   
}

