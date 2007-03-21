/*
 * FileQoutManager.java
 *
 * Created on March 20, 2007, 8:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import ij.util.Java2;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 *
 * @author ricardo
 */
public class FileQoutManager {
    
    private java.util.Hashtable availableGauges;
    
    /** Creates a new instance of FileQoutManager */
    public FileQoutManager(java.io.File[] avGauges) {
        
        availableGauges=new java.util.Hashtable();
        for (int i = 0; i < avGauges.length; i++) {
            availableGauges.put(avGauges[i].getName().substring(avGauges[i].getName().lastIndexOf("_")+1,avGauges[i].getName().lastIndexOf(".")),new FileQout(avGauges[i]));
        }

    }
    
    public double[] getTime(Object theKey){
        FileQout theNode=(FileQout)availableGauges.get(theKey);
        return theNode.getTime();
    }
    
    public double[] getSeries(Object theKey,int index){
        FileQout theNode=(FileQout)availableGauges.get(theKey);
        return theNode.getSeries(index);
    }
    
    public Object[] getKeys(){
        Object[] toReturn=availableGauges.keySet().toArray();
        java.util.Arrays.sort(toReturn);
        return toReturn;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.io.File[] files = {new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_Outlet.qout"),
                                new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_1148.qout"),
                                new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_1166.qout")};
        new FileQoutManager(files);
    }
    
}

class FileQout{
    
    private java.io.File pathToQ;
    private int countTimes=-1;
    private double[][] QhData;
    
    public FileQout(java.io.File qf) {
        
        pathToQ=qf;
        try {
            
            java.io.BufferedReader fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToQ));
            String fullLine;
            do{
                fullLine=fileRft.readLine();
                countTimes++;
            } while (fullLine!=null);
            fileRft.close();
            
            fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToQ));

            fullLine=fileRft.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("1-Time,hr")){
                countTimes--;
            }
            
            QhData=new double[3][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileRft.readLine();
                elements=fullLine.split("\t");
                elements=fullLine.split("\t");
                for (int j = 0; j < QhData.length; j++) {
                    QhData[j][i]=Float.parseFloat(elements[j]);
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
        return QhData[0];
    }
    
    public double[] getSeries(int index){
        return QhData[index];
    }
}
