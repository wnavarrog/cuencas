/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


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
