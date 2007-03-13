/*
 * XYStats.java
 *
 * Created on March 12, 2007, 10:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.tools;

/**
 * An object that calculates statistics between two time series
 * @author Ricardo Mantilla
 */
public class XYStats {
    
    public float Covariance;
    public float slope;
    public float intercept;
    public float R2;
    public hydroScalingAPI.util.statistics.Stats yStats,xStats;
    
    /** Creates a new instance of XYStats */
    public XYStats(float[] Xs, float[] Ys,boolean takeLogs) {
        if (takeLogs){
            for (int i=0; i<Xs.length; i++){
                Xs[i]=(float) Math.log(Xs[i]);
                Ys[i]=(float) Math.log(Ys[i]);
            }
        }
        
        xStats = new hydroScalingAPI.util.statistics.Stats(Xs);
        yStats = new hydroScalingAPI.util.statistics.Stats(Ys);
        
        Covariance=0.0f;
        
        for (int i=0; i<Xs.length; i++) Covariance+=(Xs[i]-xStats.meanValue)*(Ys[i]-yStats.meanValue);
        
        slope = Covariance/(float) Xs.length/(float) Math.pow(xStats.standardDeviation,2);
        intercept = yStats.meanValue-slope*xStats.meanValue;
        
        R2=(float) Math.pow(Covariance/Math.sqrt(Math.pow(xStats.standardDeviation,2)*Math.pow(yStats.standardDeviation,2)*Math.pow(Xs.length,2)),2);
        
    }
    
    /**
     * Composes a String with the statistics for the data
     * @return The string with statistics
     */
    public String toString(){
        String text=      "Covariance: "+   Covariance+"\n"+
                          "Slope: "+   slope+"\n"+
                          "Intercept: "+   intercept+"\n"+
                          "R2: "+   R2+"\n";
        return text;
    }
    
    /**
     * Tests for the class
     * @param args The command line parameters
     */
    public static void main (String args[]) {
        float[] estat1={1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f};
        float[] estat2={1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f};
                         
        hydroScalingAPI.tools.XYStats algo= new XYStats(estat1,estat2,false);
        System.out.println(algo.toString());
    }
}
