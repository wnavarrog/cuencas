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
 * BasinsLogReader.java
 *
 * Created on August 11, 2003, 3:18 PM
 */

package hydroScalingAPI.io;

/**
 *
 * @author Ricardo Mantilla
 */
public class BasinsLogReader {
    
    private java.util.Vector availableBasins=new java.util.Vector();
    private java.io.File basinsLog;
    
    /** Creates a new instance of BasinsLogReader */
    public BasinsLogReader(java.io.File bl) throws java.io.IOException {
        basinsLog=bl;
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(basinsLog));
        String fullLine;
        fullLine=fileMeta.readLine();
        while (fullLine != null) {
            availableBasins.add(fullLine);
            fullLine=fileMeta.readLine();
        }
        
        fileMeta.close();
    }
    
    public String[] getPresetBasins(){
        String[] names=new String[availableBasins.size()];
        for(int i=0;i<names.length;i++) names[i]=(String)availableBasins.get(i);
        return names;
    }
    
    public void rewriteFile(String[] newInfoForFile) throws java.io.IOException {
        java.io.OutputStreamWriter newfile=new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(basinsLog)));
        for(int i=0;i<newInfoForFile.length;i++) newfile.write(newInfoForFile[i]+"\n");
        newfile.close();
    }
    
    public void addBasinToFile(String newBasin) throws java.io.IOException {
        java.io.OutputStreamWriter newfile=new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(basinsLog)));
        availableBasins.add(newBasin);
        for(int i=0;i<availableBasins.size();i++) newfile.write((String)availableBasins.get(i)+"\n");
        newfile.close();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    }
    
}
