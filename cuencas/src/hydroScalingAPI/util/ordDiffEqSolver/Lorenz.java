/*
 * Lorenz.java
 *
 * Created on May 25, 2001, 11:35 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  Ricardo Mantilla 
 */
public class Lorenz implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    float[] parameters;
    /** Creates new Lorenz */
    public Lorenz(float a,float r,float b) {
        parameters=new float[3];
        parameters[0]=a;
        parameters[1]=r;
        parameters[2]=b;
        
    }

    public float[] eval(float[] input) {
        float[] result=new float[input.length];
        result[0]=parameters[0]*(input[1]-input[0]);
        result[1]=parameters[1]*input[0]-input[1]-input[0]*input[2];
        result[2]=input[0]*input[1]-parameters[2]*input[2];
        return result;
    }
    
    public float[] eval(float[] input, float time) {
        return eval(input);
    }
    
    public double[] eval(double[] input) {
        double[] result=new double[input.length];
        result[0]=parameters[0]*(input[1]-input[0]);
        result[1]=parameters[1]*input[0]-input[1]-input[0]*input[2];
        result[2]=input[0]*input[1]-parameters[2]*input[2];
        return result;
    }
    
    public double[] eval(double[] input, double time) {
        return eval(input);
    }
    
}
