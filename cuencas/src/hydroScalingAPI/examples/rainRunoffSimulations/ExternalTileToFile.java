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


/*
 * ExternalNetworkExtractor.java
 *
 * Created on September 5, 2003, 4:50 PM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

/**
 * This is the Thread used by the {@link
 * hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionOptimizer} to
 * call external procedures that process pieces of the original DEM
 * @author Ricardo Mantilla
 */
public class ExternalTileToFile extends Thread{
    
    private String[] command;
    
    private String procName;
    public boolean completed = false; 
    public boolean executing = false;
    private java.lang.Process localProcess;
    private int managerProcIndex;
    
    hydroScalingAPI.examples.rainRunoffSimulations.ParallelSimulationToFile coordinatorProc;
    
    public ExternalTileToFile(      String pn,
                                    String mFN, 
                                    int xx, 
                                    int yy, 
                                    int xxHH, 
                                    int yyHH,
                                    int scaleO,
                                    int routingType,
                                    float lambda1,
                                    float lambda2,
                                    float v_o,
                                    String stormFile,
                                    float infilRate,
                                    String outputDirectory,
                                    String connectionsO,
                                    String correctionsO,
                                    hydroScalingAPI.examples.rainRunoffSimulations.ParallelSimulationToFile coordinator,
                                    long iniTimeInMilliseconds){
                                        
        procName=pn;
        
        command=new String[] {  "/usr/bin/ssh",
                                "NODENAME",
                                System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                "-Xmx1500m",
                                "-Xrs",
                                "-cp",
                                System.getProperty("java.class.path"),
                                "hydroScalingAPI.examples.rainRunoffSimulations.TileSimulationToAsciiFile",
                                mFN,
                                ""+xx,
                                ""+yy, 
                                ""+xxHH, 
                                ""+yyHH,
                                ""+scaleO,
                                ""+routingType,
                                ""+lambda1,
                                ""+lambda2,
                                ""+v_o,
                                stormFile,
                                ""+infilRate,
                                outputDirectory,
                                connectionsO,
                                correctionsO,
                                ""+iniTimeInMilliseconds};
        coordinatorProc=coordinator;
        System.out.println(">>>> Original state: "+coordinatorProc.threadsRunning);

    }

    public void setComputingNode(String cn, int indexProc){
        procName=cn;
        command[1]=cn.split("-")[0];
        managerProcIndex=indexProc;
    }

    public void run(){
        
        try{
            System.out.println("Manager Created Process "+managerProcIndex+" executes: "+java.util.Arrays.toString(command));

            localProcess=java.lang.Runtime.getRuntime().exec(command);
            System.out.println(">> The command was sent");

            boolean monitor = true;
            String concat="";
            while(monitor){
                String s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
                concat+=s;
                if(s.equalsIgnoreCase("\n")) {
                    //System.out.print(concat);
                    if(concat.substring(0, Math.min(31,concat.length())).equalsIgnoreCase("Termina escritura de Resultados")) break;
                    concat="";
                }
            }
            
            completed=true;
            executing=false;

            coordinatorProc.compNodeNames.put(procName, false);
            System.out.println(">>>> Previous state: "+coordinatorProc.threadsRunning);
            coordinatorProc.threadsRunning--;
            System.out.println(">>>> New state: "+coordinatorProc.threadsRunning);

            localProcess.destroy();
            System.out.println("Command completed for "+procName);
            


        }catch(java.io.IOException ioe){
            System.err.println("Failed launching external process");
            System.err.println(ioe);
        }
        
    }
    
    
}
