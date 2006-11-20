/*
 * basicFunction.java
 *
 * Created on May 25, 2001, 11:29 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  ricardo
 * @version 
 */
public interface BasicFunction{
    
    public float[] eval(float[] input);
    
    public float[] eval(float[] input, float time);
    
    public double[] eval(double[] input);
    
    public double[] eval(double[] input, double time);

}

