package hydroScalingAPI.examples;

import java.lang.*;
import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;

public class test_utm_gdc
{
    static final int MAX_POINTS = 1; // total number of points

    /**
     * 
     * @param argv 
     */
    public static void main(String argv[])
    {
        int i; // iterator

        Gdc_Coord_3d gdc[] = new Gdc_Coord_3d[MAX_POINTS]; // these need to be the same length.
        Utm_Coord_3d utm[] = new Utm_Coord_3d[MAX_POINTS];


        for (i = 0;i<gdc.length;i++)
        {
            gdc[i] = new Gdc_Coord_3d();
	    //utm[i] = new Utm_Coord_3d(677381.97,4124276.57 ,100,(byte)13,true);
            utm[i] = new Utm_Coord_3d( 677382.0,4124276.5 ,0,(byte)13,true);

        }

        Utm_To_Gdc_Converter.Init(new WE_Ellipsoid());

        // convert the points.

        Utm_To_Gdc_Converter.Convert(utm,gdc);

        // print out the sample data
        for (i=0;i<gdc.length;i++)
        {
            System.out.println("\nUtm[" + i + "].x: " + utm[i].x);
            System.out.println("Utm[" + i +"].y: " + utm[i].y);
            System.out.println("Utm[" + i + "].z: " + utm[i].z);
            System.out.println("Utm[" +i + "].zone: " + utm[i].zone);
            System.out.println("Utm[" + i + "].hemisphere_north: " + utm[i].hemisphere_north);

            System.out.println("\nGdc[" + i + "].latitude: " + gdc[i].latitude);
            System.out.println("Gdc[" + i + "].longitude: " + gdc[i].longitude);
            System.out.println("Gdc[" + i + "].elevation: " + gdc[i].elevation);


        } // end for
    } // end main
}// end test
