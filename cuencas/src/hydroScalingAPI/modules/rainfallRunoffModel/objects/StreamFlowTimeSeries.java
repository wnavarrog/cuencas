/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricardo
 */
public class StreamFlowTimeSeries {

    private float regInterval;
    private java.util.Hashtable recordTimeValue;
    
    private double firstTime;
    private double lastTime;
    
    private double[] timesRecorded;
    
    public StreamFlowTimeSeries(java.io.File theFile) {
        
        recordTimeValue=new java.util.Hashtable<String, Double>();
        
        try {

            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(theFile));
            String fullLine;
            fullLine=fileMeta.readLine();
            regInterval=Float.parseFloat(fullLine.split(":")[1]);
            fullLine=fileMeta.readLine();
            fullLine=fileMeta.readLine();
            
            String[] data={"",""};
            
            if(fullLine != null) {
                data=fullLine.split(",");
                firstTime=Double.parseDouble(data[0]);
            }
            
            while (fullLine != null) {
                data=fullLine.split(",");
                recordTimeValue.put(data[0],new Double(data[1]));
                fullLine=fileMeta.readLine();

            }
            
            lastTime=Double.parseDouble(data[0]);

            fileMeta.close();
            
            Object[] timesSet=recordTimeValue.keySet().toArray();
            timesRecorded=new double[timesSet.length];
            for (int i = 0; i < timesSet.length; i++) {
                timesRecorded[i]=Double.parseDouble(timesSet[i].toString());
            }
            java.util.Arrays.sort(timesRecorded);
            
        } catch (java.io.IOException ex) {
            System.out.println("<<<<<<<>>>>>>> !!!!!!! Failed While Reading "+theFile);
            Logger.getLogger(StreamFlowTimeSeries.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public double evaluate(double time){
        
//        System.out.println("Evaluating, Limits: "+firstTime+" "+lastTime);
//        System.out.println("Desired Time: "+time+" Test "+(time <= timesRecorded[0]));

        int k=0;
        while(time > timesRecorded[k] && k < timesRecorded.length-1){
            k++;
        }
        
        if(time < firstTime) return 0.0;
        if(time >= lastTime) return 0.0;
        if(k == timesRecorded.length-1) return 0.0;
        
        double leftT=timesRecorded[k];
        double rightT=timesRecorded[k+1];
        
        Double leftF=(Double)recordTimeValue.get(""+leftT);
        
        //System.out.println("Indexes "+time+" "+leftT+" "+rightT);
        
        while(leftF == null){
            leftT-=1; rightT-=1;
            leftF=(Double)recordTimeValue.get(""+leftT);
        }

        Double rightF=(Double)recordTimeValue.get(""+rightT);
        
        //System.out.println("Ready "+leftT+" "+rightT);
        
        if(leftT == rightT) return leftF; 

        if(leftF != null && rightF != null ) return leftF+(time-leftT)/(rightT-leftT)*(rightF.doubleValue()-leftF.doubleValue());
        
        return 0.0D;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String name="/Users/ricardo/simulationResults/Parallel/Walnut_Gulch/walnutGulchUpdated_971_250-UniformEvent_INT_20.0_DUR_5.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.5.csv.Outlet";
        StreamFlowTimeSeries timeSeries = new StreamFlowTimeSeries(new java.io.File(name));
        for(float time=786900.0f;time<787460f;time+=0.5f) System.out.println(timeSeries.evaluate(time));
    }

}
