/*
 * CopyFile.java
 *
 * Created on July 12, 2003, 11:26 AM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author  ricardo
 */
public abstract class FileManipulation {
    
    /** Creates a new instance of CopyFile */
    public static void  CopyFile(java.io.File sourceFile, java.io.File destinationFile) {
        
        try {
            java.nio.channels.FileChannel sourceChannel =new java.io.FileInputStream(sourceFile).getChannel();
            java.nio.channels.FileChannel destinationChannel =new java.io.FileOutputStream(destinationFile).getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destinationChannel.close();
        }
        catch (java.io.IOException e) { // handle any IOException
        }
        
    }
    
}
