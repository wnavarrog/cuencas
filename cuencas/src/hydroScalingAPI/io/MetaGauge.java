package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class MetaGauge extends Object implements Comparable{
    
    private hydroScalingAPI.util.database.DB_Register gaugeRegister;
    private String[] gaugeTags={"[code]",
                                "[agency]",
                                "[type]",
                                "[site name]",
                                "[stream name]",
                                "[county]",
                                "[state]",
                                "[data source]",
                                "[latitude (deg:min:sec)]",
                                "[longitude (deg:min:sec)]",
                                "[altitude ASL (m)]",
                                "[drainage area (km^2)]",
                                "[data units]",
                                "[data accuracy]",
                                "[file location]"};
                                
    private double firstYearInSeries=0;
    
    /** Creates a new instance of MetaGauge */
    public MetaGauge(hydroScalingAPI.util.database.DB_Register register) {
        gaugeRegister=register;
    }
    
    public Object getProperty(String key){
        return gaugeRegister.getProperty(key);
    }
    
    public String toString(){
        return (String)gaugeRegister.getProperty("[code]")+" - "+(String)gaugeRegister.getProperty("[site name]")+" - "+(String)gaugeRegister.getProperty("[type]");
    }
    
    public int compareTo(Object obj) {
        return (this.toString()).compareToIgnoreCase(obj.toString());
    }
    
    public java.io.File getFileLocation(){
        return (java.io.File)gaugeRegister.getProperty("[file location]");
    }
    
    public String getLabel(){
        return (String)gaugeRegister.getProperty("[type]")+" ["+(String)gaugeRegister.getProperty("[data units]")+"]";
    }
    
    public visad.RealTuple getPositionTuple() throws visad.VisADException, java.rmi.RemoteException{
        double xx=((Double)gaugeRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)gaugeRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy)};
        return new visad.RealTuple(rtd1);
    }
    
    public visad.Tuple getTextTuple()  throws visad.VisADException, java.rmi.RemoteException{
        visad.TextType t = visad.TextType.getTextType("text");
        double xx=((Double)gaugeRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)gaugeRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Data[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy),
                             new visad.Text(t, (String)gaugeRegister.getProperty("[code]"))};
        return new visad.Tuple(rtd1);
    }
    
   public double[][] getTimeAndData() throws java.io.IOException{
        
        float[] factors={1.0f,1.0f/12.0f,1.0f/365.0f};
        
        java.util.Calendar dateComposite=java.util.Calendar.getInstance();
        dateComposite.set(00,00,00,00,00,00);
        firstYearInSeries=-dateComposite.getTimeInMillis()/1000.0/3600.0/24.0/365.25;
        
        java.io.FileInputStream inputLocal=new java.io.FileInputStream(getFileLocation());
        java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

        String fullLine;
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase("[data (yyyy.mm.dd.hh.mm.ss    value)]"));
        
        java.util.Vector theTextData=new java.util.Vector();
        fullLine=fileMeta.readLine();
        while (fullLine != null){
            theTextData.add(fullLine);
            fullLine=fileMeta.readLine();
        }
        
        fileMeta.close();
        inputComprim.close();
        inputLocal.close();
        
        double[][] theData=new double[2][theTextData.size()];
        java.util.StringTokenizer tokens;
        for (int i=0;i<theData[0].length;i++){
            tokens=new java.util.StringTokenizer((String)theTextData.get(i));
            theData[0][i]=firstYearInSeries+new hydroScalingAPI.tools.DateToElapsedTime(tokens.nextToken()).getYears();
            theData[1][i]=Float.parseFloat(tokens.nextToken());
        }
        
        return theData;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    }
    
}
