/*
 * LocationsManager.java
 *
 * Created on June 13, 2003, 2:46 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class LocationsManager {
    
    /** Creates a new instance of LocationsManager */
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager informationManager;
    private hydroScalingAPI.util.database.DataBaseEngine mainDataBase;
    
    private java.util.Hashtable threadsRegistry=new java.util.Hashtable();
    
    private String[] locatTags={"[type]",
                                "[source]",
                                "[site name]",
                                "[county]",
                                "[state]",
                                "[latitude (deg:min:sec)]",
                                "[longitude (deg:min:sec)]",
                                "[altitude ASL (m)]",
                                "[images]",
                                "[information]",
                                "[file location]"};
    private String[] locatTagsTypes={   "alpha",
                                        "alpha",
                                        "index",
                                        "alpha",
                                        "alpha",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "object",
                                        "object",
                                        "object"};
    
    /** Creates a new instance of LocationsManager */
    public LocationsManager(hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager,hydroScalingAPI.util.database.DataBaseEngine dataBase) {
        informationManager=infoManager;
        mainDataBase=dataBase;
        mainDataBase.addTable("Locations",locatTags,locatTagsTypes);
        LocationsLoader loaderDigerThread=new LocationsLoader(informationManager.dataBaseSitesLocationsPath,this);
        loaderDigerThread.start();
    }
    
    public void addData(Object[] thisRegister){
        String registerIndex=(String)thisRegister[2]+"."+(String)thisRegister[0];
        mainDataBase.addData("Locations",registerIndex,thisRegister);
    }
    
    public hydroScalingAPI.io.MetaLocation getLocation(String code, String type) {
        hydroScalingAPI.util.database.DB_Register matchedValue = mainDataBase.getRegisterByMainIndex("Locations",code+"."+type);
        
        if (matchedValue != null) {
            hydroScalingAPI.io.MetaLocation locationResult=new hydroScalingAPI.io.MetaLocation(matchedValue);
            return locationResult;
        }
        
        return null;
        
    }
    
    public hydroScalingAPI.io.MetaLocation[] getLocation(String code) {
        java.util.Vector locationsResultVector=new java.util.Vector();
        java.util.Vector localTypes=getTypes();
        int numTypes=localTypes.size();
        hydroScalingAPI.util.database.DB_Register[] matchedValues=new hydroScalingAPI.util.database.DB_Register[numTypes];
        for (int i=0;i<numTypes;i++){
            matchedValues[i] = mainDataBase.getRegisterByMainIndex("Locations",code+"."+(String)localTypes.get(i));
            if (matchedValues[i] != null) locationsResultVector.add(new hydroScalingAPI.io.MetaLocation(matchedValues[i]));
        }
        if (locationsResultVector.size() == 0) return null;
        hydroScalingAPI.io.MetaLocation[] locationsResult=new hydroScalingAPI.io.MetaLocation[locationsResultVector.size()];
        for (int i=0;i<locationsResultVector.size();i++) locationsResult[i]=(hydroScalingAPI.io.MetaLocation)locationsResultVector.get(i);
        return locationsResult;
        
    }
    
    public hydroScalingAPI.io.MetaLocation[] findLocations(java.util.Vector categories,java.util.Vector values,java.util.Vector conditions) {
        String[] cats=new String[categories.size()];
        Object[] vals=new Object[values.size()];
        int[] conds=new int[conditions.size()];
        
        for (int i=0;i<cats.length;i++){
            cats[i]=(String)categories.get(i);
            vals[i]=values.get(i);
            conds[i]=((Integer)conditions.get(i)).intValue();
        }
        
        hydroScalingAPI.util.database.DB_Register[] matchedValues = mainDataBase.compareFieldsTo("Locations",cats,vals,conds);
        if (matchedValues == null) return null;
        
        hydroScalingAPI.io.MetaLocation[] foundLocations=new hydroScalingAPI.io.MetaLocation[matchedValues.length];
        
        for (int i=0;i<matchedValues.length;i++) foundLocations[i]=new hydroScalingAPI.io.MetaLocation(matchedValues[i]);

        java.util.Arrays.sort(foundLocations);
        return foundLocations;
        
    }
    
    public java.util.Vector getNames(){
        return mainDataBase.getUniqueValues("Locations",locatTags[2]);
    }
    
    public java.util.Vector getAgencies(){
        return mainDataBase.getUniqueValues("Locations",locatTags[1]);
    }
    
    public java.util.Vector getTypes(){
        return mainDataBase.getUniqueValues("Locations",locatTags[0]);
    }
    
    public java.util.Vector getCounties(){
        return mainDataBase.getUniqueValues("Locations",locatTags[3]);
    }
    
    public java.util.Vector getStates(){
        return mainDataBase.getUniqueValues("Locations",locatTags[4]);
    }
    
    public Double getSouthernmostLat(){
        return mainDataBase.getMinValue("Locations",locatTags[5]);
    }
    public Double getNorthernmostLat(){
        return mainDataBase.getMaxValue("Locations",locatTags[5]);
    }
    public Double getWesternmostLat(){
        return mainDataBase.getMinValue("Locations",locatTags[6]);
    }
    public Double getEasternmostLat(){
        return mainDataBase.getMaxValue("Locations",locatTags[6]);
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
        
        LocationsManager GM=new LocationsManager(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(),new hydroScalingAPI.util.database.DataBaseEngine());
        while(!GM.isLoaded()){
            new visad.util.Delay(1000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=GM.getTypes();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
        theVector=GM.getStates();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
    }
    
}

class LocationsLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.LocationsManager threadManager;
    
    private java.io.File locationsMainDirectory;
    private hydroScalingAPI.util.fileUtilities.DirFilter directoriesFinder=new hydroScalingAPI.util.fileUtilities.DirFilter();
    private hydroScalingAPI.util.fileUtilities.DotFilter locationFileFinder=new hydroScalingAPI.util.fileUtilities.DotFilter("gz");
    
    public LocationsLoader(java.io.File directoryToDig, hydroScalingAPI.mainGUI.objects.LocationsManager theManager) {
        
        locationsMainDirectory=directoryToDig;
        threadManager=theManager;
        
    }
    
    private void loadData(java.io.File directoryToDig){
        java.io.File[] subDirectories=directoryToDig.listFiles(directoriesFinder);
        for (int i=0;i<subDirectories.length;i++){
            loadData(subDirectories[i]);
        }
        java.io.File[] filesToAdd=directoryToDig.listFiles(locationFileFinder);
        for (int i=0;i<filesToAdd.length;i++){
            //new LocationDataLoader(filesToAdd[i],threadManager).start();
            try{
                Object[] thisRegister=new hydroScalingAPI.io.LocationReader(filesToAdd[i]).getRegisterForDataBase();
                threadManager.addData(thisRegister);
            }catch(java.io.IOException IOE){
                System.err.println("Failed trying to load File "+filesToAdd[i]);
                System.err.println(IOE);
            }
        }
    }
    
    public void run() {
        threadManager.threadReport("dataDigger","working");
        loadData(locationsMainDirectory);
        threadManager.threadReport("dataDigger","done");
    }
    
}

class LocationDataLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.LocationsManager threadManager;
    private java.io.File fileToLoad;
    
    public LocationDataLoader(java.io.File file,hydroScalingAPI.mainGUI.objects.LocationsManager theManager) {
        
        threadManager=theManager;
        fileToLoad=file;
        
    }
    
    public void run() {
        
        try{
            threadManager.threadReport(fileToLoad.getName(),"working");
            Object[] thisRegister=new hydroScalingAPI.io.LocationReader(fileToLoad).getRegisterForDataBase();
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