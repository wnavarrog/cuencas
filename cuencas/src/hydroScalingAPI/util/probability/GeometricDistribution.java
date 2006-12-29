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
public class GeometricDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private double base;
    private int minValue;
    
    /** Creates a new instance of UniformDistribution */
    public GeometricDistribution(double theBase, int theMinVal) {
        
        base=theBase;
        minValue=theMinVal;
        
    }
    
    public int sample() {
        double ranNum=Math.random();
        return (int)Math.floor(Math.log(ranNum)/Math.log(1-base))+minValue;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GeometricDistribution myUD=new GeometricDistribution(0.5,0);
        for(int i=0;i<100;i++) {
            System.out.println(myUD.sample());//+",");
            //if(i % 100 == 0) System.out.println("$");
        }
        //System.out.println(myUD.sample()+"$");
    }
    
}
