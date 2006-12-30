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
 * basicFunction.java
 *
 * Created on May 25, 2001, 11:29 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  Ricardo Mantilla
 */
public interface BasicFunction{
    
    public float[] eval(float[] input);
    
    public float[] eval(float[] input, float time);
    
    public double[] eval(double[] input);
    
    public double[] eval(double[] input, double time);

}

