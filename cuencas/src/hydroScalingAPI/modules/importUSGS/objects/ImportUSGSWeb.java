/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.importUSGS.objects;

/**
 *
 * @author Eric Osgood
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class ImportUSGSWeb {

    try {
        File theFile = new File(output_dir+gaugeid+filesuffix+".txt.gz");
        FileOutputStream outputLocal = new FileOutputStream(theFile);
        GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputLocal);
        BufferedWriter dataout= new BufferedWriter(new OutputStreamWriter(outputComprim));

    }
}
