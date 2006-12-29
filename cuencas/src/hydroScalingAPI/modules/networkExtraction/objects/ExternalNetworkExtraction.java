/*
 * ExternalNetworkExtractor.java
 *
 * Created on September 5, 2003, 4:50 PM
 */

package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class ExternalNetworkExtraction extends Thread{
    
    private String executorName;
    private String metaFileName, demFileName;
    private String[] command;
    private boolean running=false;
    private int timeRunning=0;
    private java.lang.Process localProcess;
    
    public ExternalNetworkExtraction(String name){
        executorName=name;
    }
    
    public void setTargetFiles(String mFN, String dFN){
        
        running=true;
        metaFileName=mFN;
        demFileName=dFN;
        
    }
    
    public boolean isBusy(){
        if(timeRunning > 100){
            localProcess.destroy();
            running=false;
            System.out.println("Failed: "+metaFileName+" form "+executorName);
        }
        timeRunning++;
        return running;
    }
    
    public void run(){
        
        command=new String[] {  System.getProperty("java.home")+"/bin/java",
                                "-Xmx256m",
                                "-Xrs",
                                "-cp",
                                System.getProperty("java.class.path"),
                                "hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule",
                                metaFileName,
                                demFileName};
        try{
            System.out.println("Throws the command "+metaFileName+" form "+executorName);
            localProcess=java.lang.Runtime.getRuntime().exec(command);
            System.out.println("The command "+metaFileName+" was thrown"+" form "+executorName);
            
            localProcess.getOutputStream().flush();
            localProcess.waitFor();
            System.out.println("The command "+demFileName+" is over"+" form "+executorName);
            localProcess.destroy();
            running=false;
        }catch(java.io.IOException ioe){
            System.err.println("Failed launching external process");
            System.err.println(ioe);
        }catch(java.lang.InterruptedException ie){
            System.err.println("Failed waiting for external process");
            System.err.println(ie);
        }
        
    }
    
    
}
