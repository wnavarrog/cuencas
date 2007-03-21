/*
 * FileIntegratedManager.java
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
public class FileIntegratedManager {
    
    private java.io.File pathToIntegrated;
    private int numVoi;
    private double[][] integData;
    private int countTimes=-1;
    
    
    /** Creates a new instance of FileIntegratedManager */
    public FileIntegratedManager(java.io.File pInt,int nv) {
        pathToIntegrated=pInt;
        numVoi=nv;
        try {
            
            java.io.BufferedReader fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToIntegrated));
            String fullLine;

            fullLine=fileMrf.readLine();
            String[] elements=fullLine.split(",");
            
            if(elements[0].equalsIgnoreCase("ID")){
                fullLine=fileMrf.readLine();
                countTimes-=2;
            }
            
            integData=new double[numVoi][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileMrf.readLine();
                elements=fullLine.split(",");
                for (int j = 0; j < integData.length; j++) {
                    integData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileMrf.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileIntegratedManager(new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/voronoi/smallbasin.0000_00i"),10);
    }
    
}


