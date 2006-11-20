package hydroScalingAPI.tools;

public class Stats{
    
    public float meanValue=0.0f;
    public float standardDeviation=0.0f;
    public float skewness=0.0f;
    public float kurtosis=0.0f;
    public float maxValue=Float.MIN_VALUE;
    public float minValue=Float.MAX_VALUE;
    public float total=0.0f;
    public int dataCount=0;

    /** Creates new estadisticos */
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
            
                    maxValue = (float)Math.max(maxValue,data[j]);
                    meanValue += (float)data[j];
                    standardDeviation+= (float) Math.pow(data[j],2);
                    dataCount++;
      
            
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-Math.pow(meanValue,2));
        
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
        System.out.println(algo.meanValue+" "+algo.standardDeviation+" "+algo.minValue+" "+algo.maxValue+" "+algo.dataCount+" "+algo.total);
    }
    

}