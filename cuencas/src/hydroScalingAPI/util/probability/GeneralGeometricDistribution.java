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
public class GeneralGeometricDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private double coeff, base;
    private int minValue;
    
    private double p0;
    
    /** Creates a new instance of UniformDistribution */
    public GeneralGeometricDistribution(double theCoeff, double theBase, int theMinVal) {
        
        /*Results for Walnut Creek
         *
         *For interior generators
         *0.59062127 - theBase
         *0.25756657 - theCoeff
         *
         *For exterior generators
         *0.57253316 - theBase
         *0.19803630 - theCoeff
         *
        */

        
        coeff=theCoeff;
        base=theBase;
        minValue=theMinVal;
        
        p0=1-coeff/(1-base);
        
        System.out.println(p0);
        
    }
    
    public int sample() {
        double ranNum=Math.random();
        if (ranNum > p0) return minValue;
        return (int)Math.floor(Math.log(ranNum/coeff)/Math.log(base)+minValue+1);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GeneralGeometricDistribution myUD=new GeneralGeometricDistribution(0.25756657,0.59062127,0);
        for(int i=0;i<10000;i++) {
            System.out.print(myUD.sample()+",");
            if(i % 100 == 0) System.out.println("$");
        }
        System.out.println(myUD.sample()+"$");
    }
    
}
