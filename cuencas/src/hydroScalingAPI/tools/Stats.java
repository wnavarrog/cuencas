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


package hydroScalingAPI.tools;

/**
 *
 * @author Ricardo Mantilla
 */
public class Stats{
    
    public float meanValue=0.0f;
    public float standardDeviation=0.0f;
    public float skewness=0.0f;
    public float kurtosis=0.0f;
    public float maxValue=Float.MIN_VALUE;
    public float minValue=Float.MAX_VALUE;
    public float total=0.0f;
    public int dataCount=0;

    /** Creates new Stats */
    public Stats(float[][] matrix) {
        for (int j=0;j<matrix.length;j++){
            for (int k=0;k<matrix[j].length;k++){
                minValue=(float) Math.min(minValue,matrix[j][k]);
                maxValue=(float) Math.max(maxValue,matrix[j][k]);
                meanValue+=matrix[j][k];
                standardDeviation+=(float) Math.pow(matrix[j][k],2);
                dataCount++;
            }
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
    }

    public Stats(double[] data) {
        
        for (int j=0;j<data.length;j++){
            minValue = (float) Math.min(minValue,data[j]);
            maxValue = (float)Math.max(maxValue,data[j]);
            meanValue += (float)data[j];
            standardDeviation+= (float) Math.pow(data[j],2);
            dataCount++;
      
            
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
        
    }
    
    
    public Stats(float[][] matrix, float missing) {
        for (int j=0;j<matrix.length;j++){
            for (int k=0;k<matrix[j].length;k++){
                if (matrix[j][k] != missing && !(new Float(matrix[j][k]).isNaN())) {
                    minValue=(float) Math.min(minValue,matrix[j][k]);
                    maxValue=(float) Math.max(maxValue,matrix[j][k]);
                    meanValue+=matrix[j][k];
                    standardDeviation+=(float) Math.pow(matrix[j][k],2);
                    dataCount++;
                }
            }
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
    }
    
    public Stats(double[][] matrix, double missing) {
        for (int j=0;j<matrix.length;j++){
            for (int k=0;k<matrix[j].length;k++){
                if (matrix[j][k] != missing && !(new Float(matrix[j][k]).isNaN())) {
                    minValue=(float) Math.min(minValue,matrix[j][k]);
                    maxValue=(float) Math.max(maxValue,matrix[j][k]);
                    meanValue+=matrix[j][k];
                    standardDeviation+=(float) Math.pow(matrix[j][k],2);
                    dataCount++;
                }
            }
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
    }
    
    public Stats(float[] data) {
        
        for (int j=0;j<data.length;j++){
            
            minValue = (float) Math.min(minValue,data[j]);
            maxValue = (float)Math.max(maxValue,data[j]);
            meanValue += (float)data[j];
            standardDeviation+= (float) Math.pow(data[j],2);
            dataCount++;
      
            
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-Math.pow(meanValue,2));
        
    }
    
    public String toString(){
        String text=      "Mean Value: "+   meanValue+"\n"+
                          "Standard Deviation: "+   standardDeviation+"\n"+
                          "Skewness: "+   skewness+"\n"+
                          "Kurtosis: "+   kurtosis+"\n"+
                          "Max Value: "+   maxValue+"\n"+
                          "Min Value: "+   minValue+"\n"+
                          "Total: "+   total+"\n"+
                          "Data Count: "+   dataCount+"\n";
        return text;
    }
    
    public static void main (String args[]) {
        float[][] estat={{1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f}};
                         
        /*float[][] estat={{0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f}};*/
        hydroScalingAPI.tools.Stats algo= new Stats(estat,0);
        System.out.println(algo.toString());
    }
    

}
