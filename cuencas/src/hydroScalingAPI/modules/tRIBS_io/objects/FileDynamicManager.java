/*
 * FileDynamicManager.java
 *
 * Created on March 20, 2007, 8:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author ricardo
 */
public class FileDynamicManager {
    
    private java.util.Hashtable availableDynamic;
    
    
    /** Creates a new instance of FileIntegratedManager */
    public FileDynamicManager(java.io.File[] pInt,int nv) {
        availableDynamic=new java.util.Hashtable();
        for (int i = 0; i < pInt.length; i++) {
            availableDynamic.put(pInt[i].getName(),new FileDynamic(pInt[i],nv));
        }
    }
    
    public float[] getValues(Object theKey,int varIndex){
        FileDynamic theNode=(FileDynamic)availableDynamic.get(theKey);
        return theNode.getValues(varIndex);
    }
    
    public Object[] getKeys(){
        Object[] toReturn=availableDynamic.keySet().toArray();
        java.util.Arrays.sort(toReturn);
        return toReturn;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileDynamicManager(new java.io.File[] {new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/voronoi/smallbasin.0000_00d")},10);
    }
    
}

class FileDynamic {
    
    private java.io.File pathToIntegrated;
    private int numVoi;
    private float[][] integData;
    private int countTimes=-1;
    
    
    /** Creates a new instance of FileIntegratedManager */
    public FileDynamic(java.io.File pInt,int nv) {
        pathToIntegrated=pInt;
        numVoi=nv;
        try {
            
            java.io.BufferedReader fileDyna = new java.io.BufferedReader(new java.io.FileReader(pathToIntegrated));
            String fullLine;

            fullLine=fileDyna.readLine();
            String[] elements=fullLine.split(",");
            countTimes=elements.length;
            
            if(!elements[0].equalsIgnoreCase("ID")){
                fileDyna.close();
                fileDyna = new java.io.BufferedReader(new java.io.FileReader(pathToIntegrated));
            }
            
            integData=new float[countTimes][numVoi];
            
            for (int i = 0; i < numVoi; i++) {
                fullLine=fileDyna.readLine();
                elements=fullLine.split(",");
                for (int j = 0; j < integData.length; j++) {
                    integData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileDyna.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public float[] getValues(int varIndex){
        return integData[varIndex];
    }
    
    
}
