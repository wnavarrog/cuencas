/*
 * FileRftManager.java
 *
 * Created on March 20, 2007, 8:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Reads the contents of the Rft file

 * @author ricardo
 */
public class FileRftManager {
    
    private java.io.File pathToRft;
    private double[][] RftData;
    private int countTimes=-1;
    
    /** Creates a new instance of FileMrfManager */
    public FileRftManager(java.io.File pRft) {
        
        pathToRft=pRft;
        System.out.println(pathToRft);
        try {
            
            java.io.BufferedReader fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToRft));
            String fullLine;
            do{
                fullLine=fileRft.readLine();
                countTimes++;
            } while (fullLine!=null);
            fileRft.close();
            
            fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToRft));

            fullLine=fileRft.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("time")){
                fullLine=fileRft.readLine();
                countTimes-=2;
            }
            
            RftData=new double[5][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileRft.readLine();
                elements=fullLine.split("\t");
                for (int j = 0; j < RftData.length; j++) {
                    RftData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileRft.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public double[] getTime(){
        return RftData[0];
    }
    
    public double[] getSeries(int index){
        return RftData[index];
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileRftManager(new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin0018_00.rft"));
    }
}
