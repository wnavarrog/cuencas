/*
 * FileMrfManager.java
 *
 * Created on March 20, 2007, 8:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author ricardo
 */
public class FileMrfManager {
    
    private java.io.File pathToMRF;
    private double[][] mrfData;
    private int countTimes=-1;
    
    /** Creates a new instance of FileMrfManager */
    public FileMrfManager(java.io.File pMrf) {
        
        pathToMRF=pMrf;
        try {
            
            java.io.BufferedReader fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));
            String fullLine;
            do{
                fullLine=fileMrf.readLine();
                countTimes++;
            } while (fullLine!=null);
            fileMrf.close();
            
            fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));

            fullLine=fileMrf.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("time")){
                fullLine=fileMrf.readLine();
                countTimes-=2;
            } else {
                fileMrf.close();
                fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));
            }
            
            mrfData=new double[13][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileMrf.readLine();
                elements=fullLine.split("\t");
                for (int j = 0; j < mrfData.length; j++) {
                    mrfData[j][i]=Float.parseFloat(elements[j]);
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
    
    public double[] getTime(){
        return mrfData[0];
    }
    
    public double[] getSeries(int index){
        return mrfData[index];
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileMrfManager(new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/JEMEZ_TRIBS_RUM/Output/hyd/jm0009_00.mrf"));
    }
    
}
