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
 *
 * @author Ricardo Mantilla
 */
public class MetaPolygon {
    
    private java.io.BufferedReader fileMeta;

    private java.io.File locationMeta;
    private java.io.File locationBinaryFile;
    private boolean completed;
    
    private java.util.Hashtable properties;

    public String[] parameters= { 
                                    "[name]",
                                    "[coordinates lat/lon (deg)]",
                                    "[information]"
                                    
                                };
    /** Creates a new instance of MetaPolygon */
    public MetaPolygon() {
        
        properties=new java.util.Hashtable();
        
    }
    
    public MetaPolygon(String file) throws java.io.IOException{
        this(new java.io.File(file));
    }
    
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

    public boolean checkParameters(java.io.File file) throws java.io.IOException{
        
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
    
    public String getProperty(String prop){
        return (String) properties.get(prop);
    }
    
    public visad.Gridded2DSet getPolygon() throws visad.VisADException {
        
        float[][] xyContour=(float[][])properties.get("[coordinates lat/lon (deg)]");
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet polygonCountour=new visad.Gridded2DSet(domain,xyContour,xyContour[0].length);
        
        return polygonCountour;
    }
    
    public float[][] getLonLatPolygon(){
        return (float[][])properties.get("[coordinates lat/lon (deg)]");
    }
    
    public String getName(){
        return (String) properties.get("[name]");
    }
    
    public java.io.File getLocationMeta(){
        return locationMeta;
    }
    
    public String toString(){
        return getName()+" - "+getLocationMeta().getName();
    }
    
    public void setName(String newName){
        properties.put("[name]",newName);
    }
    
    public void setCoordinates(float[][] newXYCoords){
        properties.put("[coordinates lat/lon (deg)]",newXYCoords);
    }
    
    public void setInformation(String newInformation){
        properties.put("[information]",newInformation);
    }
    
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
