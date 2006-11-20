/*
 * read_and_plot.java
 *
 * Created on March 7, 2003, 3:25 PM
 */

package hydroScalingAPI.examples.netCDF;

/**
 *
 * @author  ricardo
 */
public class read_and_plot {
    
    /** Creates a new instance of read_and_plot */
    public read_and_plot() {
        
        visad.Field imagesNC = null;
        
        try {
            imagesNC = (visad.Field) (new visad.data.netcdf.Plain().open("/home/ricardo/temp/firstTest.nc"));
        } catch (java.io.IOException exc) {
            String s =  "To run this example, the images.nc file must be "
                        +"present in\nyour visad/examples directory."
                        +"You can obtain this file from:\n"
                        +"  ftp://ftp.ssec.wisc.edu/pub/visad-2.0/images.nc.Z";
            System.out.println(s);
            System.exit(0);
        } catch (visad.VisADException VisE) {
            System.err.println(VisE);
        }
        
        System.out.println(imagesNC);
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        new read_and_plot();
        
    }
    
}
