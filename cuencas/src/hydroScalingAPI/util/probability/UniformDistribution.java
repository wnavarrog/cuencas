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
