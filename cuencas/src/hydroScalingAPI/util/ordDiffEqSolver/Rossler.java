/*
 * Rossler.java
 *
 * Created on May 25, 2001, 11:45 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  ricardo
 * @version 
 */
public class Rossler implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    float[] parameters;
    /** Creates new Rossler */
    public Rossler(float a,float b,float c) {
        parameters=new float[3];
        parameters[0]=a;
        parameters[1]=b;
        parameters[2]=c;
        
    }

    public float[] eval(float[] input) {
        float[] result=new float[input.length];
        result[0]=-(input[1]+input[2]);
        result[1]=input[0]+parameters[0]*input[1];
        result[2]=parameters[1]+input[2]*(input[0]-parameters[2]);
        return result;
    }
    
    public float[] eval(float[] input, float time) {
        return eval(input);
    }
    
    public double[] eval(double[] input) {
        double[] result=new double[input.length];
        result[0]=-(input[1]+input[2]);
        result[1]=input[0]+parameters[0]*input[1];
        result[2]=parameters[1]+input[2]*(input[0]-parameters[2]);
        return result;
    }
    
    public double[] eval(double[] input, double time) {
        return eval(input);
    }
    
}
