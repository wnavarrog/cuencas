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
 * MetaPolygon.java
 *
 * Created on October 10, 2003, 3:08 PM
 */

package hydroScalingAPI.io;

/**
 * This class interacts with *.poly files.  It reads their contents and produce
 * appropriate outputs for graphic and other purposes.
 * @author Ricardo Mantilla
 */
public class MetaPolygon {
    
    private java.io.BufferedReader fileMeta;

    private java.io.File locationMeta;
    private java.io.File locationBinaryFile;
    private boolean completed;
    
    private java.util.Hashtable properties;

    private String[] parameters= { 
                                    "[name]",
                                    "[coordinates lon/lat (deg)]",
                                    "[information]"
                                    
                                };
    /**
     * Creates a new instance of MetaPolygon with an empty set of properties
     */
    public MetaPolygon() {
        
        properties=new java.util.Hashtable();
        
    }
    
    /**
     *  Creates a new instance of MetaPolygon with a set of properties determined by a
     * file
     * @param file A String with the path to the file that contains the properties of the Polygon
     * @throws java.io.IOException Errors while reading the *.poly file
     */
    public MetaPolygon(String file) throws java.io.IOException{
        this(new java.io.File(file));
    }
    
    /**
     *  Creates a new instance of MetaPolygon with a set of properties determined by a
     * file
     * @param file The file that contains the properties of the Polygon
     * @throws java.io.IOException Errors while reading the *.poly file
     */
    public MetaPolygon(java.io.File file) throws java.io.IOException{

        locationMeta=file;

        checkParameters(file);
        
        properties=new java.util.Hashtable();
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int hemisphereFactor;

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[0]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[1]));
        String coordLabel=fullLine;
        java.util.Vector xyCoords=new java.util.Vector();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            java.util.StringTokenizer xyCouple=new java.util.StringTokenizer(fullLine);;
            xyCoords.add(new float[] {Float.parseFloat(xyCouple.nextToken()),Float.parseFloat(xyCouple.nextToken())});
        }
        float[][] xyContour=new float[2][xyCoords.size()];
        for (int i=0;i<xyContour[0].length;i++){
            float[] xyPair=(float[])xyCoords.get(i);
            xyContour[0][i]=xyPair[0];
            xyContour[1][i]=xyPair[1];
        }
        properties.put(coordLabel,xyContour);

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[2]));
        properties.put(fullLine,fileMeta.readLine());

        completed=true;
        fileMeta.close();
    }

    private boolean checkParameters(java.io.File file) throws java.io.IOException{
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int i=0;

        do{
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase(parameters[i])) i++;
        } while (i<parameters.length && fullLine != null);

        fileMeta.close();

        if (i == parameters.length) return true;
        else return false;

    }
    
    /**
     * Returns the value of the property.  Available properties are [name] and [information].
     * @param prop The desired property
     * @return Returns the value of the property
     */
    public String getProperty(String prop){
        return (String) properties.get(prop);
    }
    
    /**
     * Creates and returns a visad.Gridded2DSet ready to be added into a visad.Display
     * @return The Gridded2DSet
     * @throws visad.VisADException Captures errors while creating the Gidded2DSet
     */
    public visad.Gridded2DSet getPolygon() throws visad.VisADException {
        
        float[][] xyContour=(float[][])properties.get("[coordinates lat/lon (deg)]");
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet polygonCountour=new visad.Gridded2DSet(domain,xyContour,xyContour[0].length);
        
        return polygonCountour;
    }
    
    /**
     * Returns a float array with the longitudes and latitudes of the points that
     * describe the polygon
     * @return A float[2][Number of Points]
     */
    public float[][] getLonLatPolygon(){
        return (float[][])properties.get("[coordinates lon/lat (deg)]");
    }
    
    /**
     * Returns the Name associated with the polygon
     * @return The Name of the polygon
     */
    public String getName(){
        return (String) properties.get("[name]");
    }
    
    /**
     * Returns the File with information used to create the MetaPolygon
     * @return A File descriptor of the origin data
     */
    public java.io.File getLocationMeta(){
        return locationMeta;
    }
    
    /**
     * Returns a string describing this MetaPolygon
     * @return A string describing the MetaPolygon
     */
    public String toString(){
        return getName()+" - "+getLocationMeta().getName();
    }
    
    /**
     * Sets the [name] property to the specified value
     * @param newName The value to assign to the [name] field
     */
    public void setName(String newName){
        properties.put("[name]",newName);
    }
    
    /**
     * Sets the coordinates of the polygon to using a float array
     * @param newXYCoords The array with coordinates
     */
    public void setCoordinates(float[][] newXYCoords){
        properties.put("[coordinates lon/lat (deg)]",newXYCoords);
    }
    
    /**
     * Sets the [information] property to the specified value
     * @param newInformation The value to assign to the [information] field
     */
    public void setInformation(String newInformation){
        properties.put("[information]",newInformation);
    }
    
    /**
     * Writes an *.poly file in the specified path
     * @param newMetaLocation The File descriptor where the information will be written.
     * @throws java.io.IOException Captures errors while writing the file
     */
    public void writePolygon(java.io.File newMetaLocation) throws java.io.IOException{
        
        java.io.BufferedWriter writerMeta = new java.io.BufferedWriter(new java.io.FileWriter(newMetaLocation));
        
        writerMeta.write(parameters[0]+"\n");
        writerMeta.write((String)properties.get(parameters[0])+"\n");
        writerMeta.write("\n");
        
        writerMeta.write(parameters[1]+"\n");
        float[][] xyContour=(float[][])properties.get("[coordinates lat/lon (deg)]");
        for (int i=0;i<xyContour[0].length;i++){
            writerMeta.write(xyContour[0][i]+"\t"+xyContour[1][i]+"\n");
        }
        writerMeta.write("\n");

        writerMeta.write(parameters[2]+"\n");
        writerMeta.write((String)properties.get(parameters[2])+"\n");
        writerMeta.write("\n");
        
        writerMeta.close();
        
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            hydroScalingAPI.io.MetaPolygon metaPoly1=new hydroScalingAPI.io.MetaPolygon (new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Polygons/walnutGulch_Divide.poly"));
            System.out.println(metaPoly1.getProperty("[name]"));
            float[][] xyContour=metaPoly1.getLonLatPolygon();
            System.out.println(xyContour[0][0]+" "+xyContour[1][0]);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
    }
    
}
