/*
 * GaugesManager.java
 *
 * Created on June 13, 2003, 2:28 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class GaugesManager {
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager informationManager;
    private hydroScalingAPI.util.database.DataBaseEngine mainDataBase;
    
    private java.util.Hashtable threadsRegistry=new java.util.Hashtable();
    
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
    
    private String[] gaugeTagsTypes={   "index",
                                        "alpha",
                                        "alpha",
                                        "object",
                                        "alpha",
                                        "alpha",
                                        "alpha",
                                        "object",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "object",
                                        "object",
                                        "object"};
    
    /** Creates a new instance of GaugesManager */
    public GaugesManager(hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager,hydroScalingAPI.util.database.DataBaseEngine dataBase) {
        informationManager=infoManager;
        mainDataBase=dataBase;
        mainDataBase.addTable("Gauges",gaugeTags,gaugeTagsTypes);
        GaugesLoader loaderDigerThread=new GaugesLoader(informationManager.dataBaseSitesGaugesPath,this);
        loaderDigerThread.start();
    }
    
    public void addData(Object[] thisRegister){
        String registerIndex=(String)thisRegister[0]+"."+(String)thisRegister[2];
        mainDataBase.addData("Gauges",registerIndex,thisRegister);
    }
    
    public hydroScalingAPI.io.MetaGauge getGauge(String code, String type) {
        hydroScalingAPI.util.database.DB_Register matchedValue = mainDataBase.getRegisterByMainIndex("Gauges",code+"."+type);
        
        if (matchedValue != null) {
            hydroScalingAPI.io.MetaGauge gaugeResult=new hydroScalingAPI.io.MetaGauge(matchedValue);
            return gaugeResult;
        }
        
        return null;
        
    }
    
    public hydroScalingAPI.io.MetaGauge[] getGauge(String code) {
        java.util.Vector gaugesResultVector=new java.util.Vector();
        java.util.Vector localTypes=getTypes();
        int numTypes=localTypes.size();
        hydroScalingAPI.util.database.DB_Register[] matchedValues=new hydroScalingAPI.util.database.DB_Register[numTypes];
        for (int i=0;i<numTypes;i++){
            matchedValues[i] = mainDataBase.getRegisterByMainIndex("Gauges",code+"."+(String)localTypes.get(i));
            if (matchedValues[i] != null) gaugesResultVector.add(new hydroScalingAPI.io.MetaGauge(matchedValues[i]));
        }
        if (gaugesResultVector.size() == 0) return null;
        hydroScalingAPI.io.MetaGauge[] gaugesResult=new hydroScalingAPI.io.MetaGauge[gaugesResultVector.size()];
        for (int i=0;i<gaugesResultVector.size();i++) gaugesResult[i]=(hydroScalingAPI.io.MetaGauge)gaugesResultVector.get(i);
        return gaugesResult;
    }
    
    public hydroScalingAPI.io.MetaGauge[] findGauges(java.util.Vector categories,java.util.Vector values,java.util.Vector conditions) {
        String[] cats=new String[categories.size()];
        Object[] vals=new Object[values.size()];
        int[] conds=new int[conditions.size()];
        
        for (int i=0;i<cats.length;i++){
            cats[i]=(String)categories.get(i);
            vals[i]=values.get(i);
            conds[i]=((Integer)conditions.get(i)).intValue();
        }
        
        hydroScalingAPI.util.database.DB_Register[] matchedValues = mainDataBase.compareFieldsTo("Gauges",cats,vals,conds);
        if (matchedValues == null) return null;
        
        hydroScalingAPI.io.MetaGauge[] foundGauges=new hydroScalingAPI.io.MetaGauge[matchedValues.length];
        
        for (int i=0;i<matchedValues.length;i++) foundGauges[i]=new hydroScalingAPI.io.MetaGauge(matchedValues[i]);

        java.util.Arrays.sort(foundGauges);
        return foundGauges;
        
    }
    
    public java.util.Vector getCodes(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[0]);
    }
    
    public java.util.Vector getAgencies(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[1]);
    }
    
    public java.util.Vector getTypes(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[2]);
    }
    
    public java.util.Vector getStreams(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[4]);
    }
    
    public java.util.Vector getCounties(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[5]);
    }
    
    public java.util.Vector getStates(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[6]);
    }
    
    public Double getSouthernmostLat(){
        return mainDataBase.getMinValue("Gauges",gaugeTags[8]);
    }
    public Double getNorthernmostLat(){
        return mainDataBase.getMaxValue("Gauges",gaugeTags[8]);
    }
    public Double getWesternmostLat(){
        return mainDataBase.getMinValue("Gauges",gaugeTags[9]);
    }
    public Double getEasternmostLat(){
        return mainDataBase.getMaxValue("Gauges",gaugeTags[9]);
    }
    
    public void threadReport(String threadName,String status){
        threadsRegistry.put(threadName,status);
    }
    
    public boolean isLoaded(){
        
        if ((threadsRegistry.size() != 0) && !threadsRegistry.containsValue("working")){
            threadsRegistry=null;
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        GaugesManager GM=new GaugesManager(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(),new hydroScalingAPI.util.database.DataBaseEngine());
        while(!GM.isLoaded()){
            new visad.util.Delay(5000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=GM.getCodes();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
        theVector=GM.getStates();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
    }
    
}

class GaugesLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.GaugesManager threadManager;
    
    private java.io.File gaugesMainDirectory;
    private hydroScalingAPI.util.fileUtilities.DirFilter directoriesFinder=new hydroScalingAPI.util.fileUtilities.DirFilter();
    private hydroScalingAPI.util.fileUtilities.DotFilter gaugeFileFinder=new hydroScalingAPI.util.fileUtilities.DotFilter("gz");
    
    public GaugesLoader(java.io.File directoryToDig, hydroScalingAPI.mainGUI.objects.GaugesManager theManager) {
        
        gaugesMainDirectory=directoryToDig;
        threadManager=theManager;
        
    }
    
    private void loadData(java.io.File directoryToDig){
        java.io.File[] subDirectories=directoryToDig.listFiles(directoriesFinder);
        for (int i=0;i<subDirectories.length;i++){
            loadData(subDirectories[i]);
        }
        java.io.File[] filesToAdd=directoryToDig.listFiles(gaugeFileFinder);
        
        for (int i=0;i<filesToAdd.length;i++){
            //new GaugeDataLoader(filesToAdd[i],threadManager).start();
            try{
                Object[] thisRegister=new hydroScalingAPI.io.GaugeReader(filesToAdd[i]).getRegisterForDataBase();
                threadManager.addData(thisRegister);
            }catch(java.io.IOException IOE){
                System.err.println("Failed trying to load File "+filesToAdd[i]);
                System.err.println(IOE);
            }
        }
    }
    
    public void run() {
        threadManager.threadReport("dataDigger","working");
        loadData(gaugesMainDirectory);
        threadManager.threadReport("dataDigger","done");
    }
    
}

class GaugeDataLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.GaugesManager threadManager;
    private java.io.File fileToLoad;
    
    public GaugeDataLoader(java.io.File file,hydroScalingAPI.mainGUI.objects.GaugesManager theManager) {
        
        threadManager=theManager;
        fileToLoad=file;
        
    }
    
    public void run() {
        
        try{
            threadManager.threadReport(fileToLoad.getName(),"working");
            Object[] thisRegister=new hydroScalingAPI.io.GaugeReader(fileToLoad).getRegisterForDataBase();
            threadManager.addData(thisRegister);
            threadManager.threadReport(fileToLoad.getName(),"done");
        }catch(java.io.IOException IOE){
            threadManager.threadReport(fileToLoad.getName(),"failed");
            System.err.println("Failed trying to load File "+fileToLoad);
            System.err.println(IOE);
        }
        
        return;
    }
    
}