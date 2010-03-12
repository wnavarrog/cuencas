/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

/**
 *
 * @author Eric Osgood
 */
import hydroScalingAPI.mainGUI.objects.LocationsManager;
import hydroScalingAPI.util.fileUtilities.NameDotFilter;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;

public class BasinMaskXY {

    float[][] matrix;

    public BasinMaskXY() throws IOException {

    URLConnection urlConn = null;
        try{

        System.out.println("Opening connection");
        URL file = new URL("http://bridges.icuencas.net/fakeData/res.dem.asc.gz");
        urlConn = file.openConnection();


        GZIPInputStream gzis = new GZIPInputStream(urlConn.getInputStream());
        InputStreamReader xover = new InputStreamReader(gzis);
        BufferedReader is = new BufferedReader(xover);

        System.out.println("Opening connection");
        matrix = new float[464][700];
        String line;

        for (int i = 0; i < 464; i++) {

            String[] linarray = null;
            line = is.readLine();
            linarray = line.split(" ");

            for (int j = 0; j < 700; j++) {

                float f = 0;
                try {
                    f = Float.valueOf(linarray[j].trim()).floatValue();
                } catch (NumberFormatException nfe) {
                    System.out.println("NFE" + nfe.getMessage());
                }

                matrix[i][j] = f;

            }
        }

        is.close();
        xover.close();
        gzis.close();

        System.out.println("Matrix Read!");
        }
        catch(MalformedURLException e){
           e.printStackTrace();
        }
        CountFolder();
    }

    public void CountFolder() throws IOException {

        matrix = new float[464][700];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = (float) Math.random();
            }
        }

        File dir = new File("C:\\Users\\Eric Osgood.EricOsgood-PC\\Documents\\Iowa Flood Center\\Cities");
        int filecount = dir.list().length;
        System.out.println(filecount);
        File[] files = dir.listFiles();
        File[] inCity = new File[filecount];
        File[] XYBasins = new File[filecount];

        for (int i = 0; i < files.length; i++) {
            //System.out.println(files[i]);
            inCity = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("Basin","txt.zip"));

            for (int j = 0; j < inCity.length; j++) {

                float averageValue = 0.0f;
                float numElements = 0.0f;

                //Open the File for reading

                FileInputStream fin = new FileInputStream(inCity[j]);
                GZIPInputStream gzis = new GZIPInputStream(fin);
                InputStreamReader xover = new InputStreamReader(gzis);
                BufferedReader is = new BufferedReader(xover);

                String line2;
                String line3;
                String linarray2[] = null;

                line3 = is.readLine();

                while ((line2 = is.readLine()) != null) {
                    linarray2 = line2.split(",");
                    int xxx = Integer.parseInt(linarray2[0].trim());
                    int yyy = Integer.parseInt(linarray2[1].trim());

                    averageValue += matrix[yyy / 10][xxx / 10];
                    numElements++;

                }
                System.out.println(line3 + " " + averageValue / numElements);

                is.close();
                xover.close();
                gzis.close();
                fin.close();

            }
        //System.out.println(XYBasins[i]);

        }
    }

    public static void main(String[] args) throws IOException {

        BasinMaskXY mask = new BasinMaskXY();

    }
}
