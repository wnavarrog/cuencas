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

