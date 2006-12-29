/*
 * UniformDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 *
 * @author Ricardo Mantilla
 */
public class UniformDistribution implements hydroScalingAPI.util.probability.ContinuumDistribution{
    
    private float lowerLimit, upperLimit;
    
    /** Creates a new instance of UniformDistribution */
    public UniformDistribution(float ll,float ul) {
        lowerLimit=ll;
        upperLimit=ul;
    }
    
    public float sample() {
        return (float) (Math.random()*(upperLimit-lowerLimit+1)+lowerLimit);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UniformDistribution myUD=new UniformDistribution(0,10);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
