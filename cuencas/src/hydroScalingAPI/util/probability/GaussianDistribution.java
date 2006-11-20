/*
 * GaussianDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 *
 * @author  ricardo
 */
public class GaussianDistribution implements hydroScalingAPI.util.probability.ContinuumDistribution{
    
    private float mean, stdev;
    private java.util.Random rn;
    
    /** Creates a new instance of UniformDistribution */
    public GaussianDistribution(float m,float s) {
        mean=m;
        stdev=s;
        rn=new java.util.Random();
    }
    
    public float sample() {
        return (float) (rn.nextGaussian()*stdev+mean);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GaussianDistribution myUD=new GaussianDistribution(2,4);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
