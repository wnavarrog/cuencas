/*
 * LogGaussianDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 *
 * @author Ricardo Mantilla
 */
public class LogGaussianDistribution implements hydroScalingAPI.util.probability.ContinuumDistribution{
    
    private float mean, stdev;
    private java.util.Random rn;
    
    /** Creates a new instance of UniformDistribution */
    public LogGaussianDistribution(float m,float s) {
        s=(float)Math.pow(s,2);
        stdev=(float)Math.log(1+s/Math.pow(m,2));
        mean=(float)(Math.log(m)-stdev/2.0);
        stdev=(float)Math.sqrt(stdev);
        rn=new java.util.Random();
        
    }
    
    public float sample() {
        return (float) Math.exp(rn.nextGaussian()*stdev+mean);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogGaussianDistribution myUD=new LogGaussianDistribution(0.1f,0.2f);
        float numSamples=10000;
        float total=0;
        float total2=0;
        for(int i=0;i<numSamples;i++){
            float val=myUD.sample();
            total+=val;
            total2+=Math.pow(val,2);
        }
        System.out.println(total/numSamples);
        System.out.println(Math.sqrt(total2/numSamples-Math.pow(total/numSamples,2)));
    }
    
}
