/*
 * BasinsLogReader.java
 *
 * Created on August 11, 2003, 3:18 PM
 */

package hydroScalingAPI.io;

/**
 *
 * @author  ricardo
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
