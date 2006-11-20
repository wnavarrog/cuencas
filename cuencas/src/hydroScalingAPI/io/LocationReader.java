/*
 * LocationReader.java
 *
 * Created on June 14, 2003, 11:14 AM
 */

package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class LocationReader {
    
    Object[] register;
    String[] parameters={   "[type]",
                            "[source]",
                            "[site name]",
                            "[county]",
                            "[state]",
                            "[latitude (deg:min:sec)]",
                            "[longitude (deg:min:sec)]",
                            "[altitude ASL (m)]",
                            "[images]",
                            "[information]"};
    
    
    /** Creates a new instance of LocationReader */
    public LocationReader(java.io.File locationFile) throws java.io.IOException {
        
        register=new Object[parameters.length+1];
        
        java.io.FileInputStream inputLocal=new java.io.FileInputStream(locationFile);
        java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

        String fullLine;
        int hemisphereFactor;

        for (int i=0;i<4;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            register[i]=fileMeta.readLine();
        }
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[4]));
        register[4]=hydroScalingAPI.tools.StateName.StateName(fileMeta.readLine());
        
        for (int i=5;i<7;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase("n/a")) register[i]=fullLine;
                else register[i]=hydroScalingAPI.tools.DMSToDegrees.getDoubleDegrees(fullLine);
        }
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[7]));
        fullLine=fileMeta.readLine();
        if (fullLine.equalsIgnoreCase("n/a")) register[7]=fullLine;
            else register[7]=new Double(fullLine);
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[8]));
        java.util.Vector images=new java.util.Vector();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            images.add(fullLine);
        }
        register[8]=images;
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[9]));
        
        String informationString=fileMeta.readLine();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            informationString+="\n"+fullLine;
        }
        register[9]=informationString;
        
        fileMeta.close();
        inputComprim.close();
        inputLocal.close();
        
        register[register.length-1]=locationFile;
        
    }
    
    public Object[] getRegisterForDataBase(){
        return register;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            Object[] testRegister=new LocationReader(new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/Kansas/Surveyed Location/500.txt.gz")).getRegisterForDataBase();
            for (int i=0;i<testRegister.length;i++) System.out.println(testRegister[i]);
        }catch(java.io.IOException IOE){
            System.err.println("Failed trying to load File");
            System.err.println(IOE);
        }
        
    }
    
}
