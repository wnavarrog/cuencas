/*
 * UniformDiscreteDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 *
 * @author  ricardo
 */
public class UniformDiscreteDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private int lowerLimit, upperLimit;
    
    /** Creates a new instance of UniformDistribution */
    public UniformDiscreteDistribution(int ll,int ul) {
        lowerLimit=ll;
        upperLimit=ul;
    }
    
    public int sample() {
        return (int) (Math.random()*(upperLimit-lowerLimit+1)+lowerLimit);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UniformDiscreteDistribution myUD=new UniformDiscreteDistribution(0,10);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
