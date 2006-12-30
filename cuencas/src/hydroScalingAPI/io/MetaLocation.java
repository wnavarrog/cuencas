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


package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class MetaLocation extends Object implements Comparable{
    
    private hydroScalingAPI.util.database.DB_Register locationRegister;
    private String[] locatTags={"[type]",
                                "[source]",
                                "[site name]",
                                "[county]",
                                "[state]",
                                "[latitude (deg:min:sec)]",
                                "[longitude (deg:min:sec)]",
                                "[altitude ASL (m)]",
                                "[images]",
                                "[information]",
                                "[file location]"};
    
    /** Creates a new instance of MetaLocation */
    public MetaLocation(hydroScalingAPI.util.database.DB_Register register) {
        locationRegister=register;
    }
    
    public Object getProperty(String key){
        return locationRegister.getProperty(key);
    }
    
    public String toString(){
        return (String)locationRegister.getProperty("[site name]")+" - "+(String)locationRegister.getProperty("[type]");
    }
    
    public int compareTo(Object obj) {
        return (this.toString()).compareToIgnoreCase(obj.toString());
    }
    
    public String getInformation(){
        return (String)locationRegister.getProperty("[information]");
    }
    
    public boolean hasImages(){
        String locationPath=((java.io.File)locationRegister.getProperty("[file location]")).getParent();
        java.util.Vector imagesVector=(java.util.Vector)locationRegister.getProperty("[images]");
        
        if (((String)imagesVector.get(0)).equalsIgnoreCase("n/a")) return false;
        
        return true;
    }
    
    public java.util.Vector[] getImagesList(){
        String locationPath=((java.io.File)locationRegister.getProperty("[file location]")).getParent();
        java.util.Vector imagesVector=(java.util.Vector)locationRegister.getProperty("[images]");
        
        if (((String)imagesVector.get(0)).equalsIgnoreCase("n/a")) return null;
        
        java.util.Vector[] imagesNameAndInfo={new java.util.Vector(),new java.util.Vector()};
        
        java.util.StringTokenizer tokens;
        
        for (int i=0;i<imagesVector.size();i++){
            String pictureNameAndInfo=(String)imagesVector.get(i);
            tokens=new java.util.StringTokenizer(pictureNameAndInfo,";");
            String thisPictureName=tokens.nextToken().trim();
            String thisPictureInfo=tokens.nextToken().trim();
            if(thisPictureName.indexOf("http://") == -1){
                imagesNameAndInfo[0].add(locationPath+"/"+thisPictureName);
            } else {
                imagesNameAndInfo[0].add(thisPictureName);
            }
            imagesNameAndInfo[1].add(thisPictureInfo);
        }
        return imagesNameAndInfo;
    }
    
    public visad.RealTuple getPositionTuple() throws visad.VisADException, java.rmi.RemoteException{
        double xx=((Double)locationRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)locationRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy)};
        return new visad.RealTuple(rtd1);
    }
    
    public visad.Tuple getTextTuple()  throws visad.VisADException, java.rmi.RemoteException{
        visad.TextType t = visad.TextType.getTextType("text");
        double xx=((Double)locationRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)locationRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Data[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy),
                             new visad.Text(t, (String)locationRegister.getProperty("[site name]"))};
        return new visad.Tuple(rtd1);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        
    }
    
}
