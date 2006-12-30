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
 * ArrayTools.java
 *
 * Created on September 11, 2003, 4:32 PM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author Ricardo Mantilla
 */
public abstract class ArrayTools {
    
    /**
     * 
     * @param theFloatArray 
     * @return A new array of integers
     */
    public static int[] convertFloatArrayToIntArray(float[] theFloatArray){
        int[] theIntArray = new int[theFloatArray.length];
        for (int i=0;i<theFloatArray.length;i++){
            theIntArray[i] = (int) theFloatArray[i];
        }
        return theIntArray;
    }
    
    /**
     * 
     * @param theArray 
     */
    public static void printArray(int[] theArray){
        //method prints array values to standard output
        String arrayString = "";
        for (int i=0;i<theArray.length;i++){
            arrayString = arrayString +  theArray[i]+" ";
        }
        System.out.println(arrayString);
    }
    
    /**
     * 
     * @param twoArray 
     * @throws java.io.IOException 
     * @return oneArray
     */
    public static float[] convertTwoToOne(float[][] twoArray) throws java.io.IOException{
        float[] oneArray = new float[twoArray[0].length];
        for (int i = 0;i<twoArray[0].length;i++){
             oneArray[i] = twoArray[0][i];
        }
        return oneArray;
    }
    
    

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    }
    
}
