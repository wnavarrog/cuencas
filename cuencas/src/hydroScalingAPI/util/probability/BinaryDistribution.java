/*
 * UniformDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 *
 * @author  ricardo
 */
public class BinaryDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private int value0, value1;
    private double p_value0;
    
    /** Creates a new instance of UniformDistribution */
    public BinaryDistribution(int n0, int n1, double p0) {
        value0=n0;
        value1=n1;
        p_value0=p0;
    }
    
    public int sample() {
        return (Math.random()<p_value0)?value0:value1;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BinaryDistribution myUD=new BinaryDistribution(1,2,0.2);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
