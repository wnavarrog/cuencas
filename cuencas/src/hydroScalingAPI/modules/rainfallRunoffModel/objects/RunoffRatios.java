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


package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.io.*;

public class RunoffRatios {
   
     //String source_dir="/home/furey/Data/walnut_ks/Sb_Whitewater/";
    //String output_dir="/hidrosigDataBases/Whitewater_database/Sites/Gauges/Streamflow/";
    
    String file;
    float [] ratios = new float [552];
    int count, i;
    
    float []  Read_ratios(String file) {
        
        try{
        
        // Copy template to new data file
        FileReader file_in = new FileReader(file);
        BufferedReader buff_in = new BufferedReader(file_in); 
        boolean eof1 = false;
        count = 0;
        i = 0 ;
        
        while(!eof1){
            String line=buff_in.readLine();
            if (line==null) eof1=true;
            else {
                count = count+1;
                //System.out.println(count + line);
                if (count > 6) {
                    //System.out.println(i + line);
                    ratios[i] = Float.parseFloat(line);
                    i = i+1;
                }
                    //dataout.write(line+'\n');
            }
        }
        buff_in.close();
        
        } catch (java.io.IOException IOE){
          System.out.print(IOE);
          System.exit(0);   
        }

    return ratios ;
    }
    
}

