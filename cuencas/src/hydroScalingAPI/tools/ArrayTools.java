/*
 * ArrayTools.java
 *
 * Created on September 11, 2003, 4:32 PM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author  ricardo
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
     * @return 
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
