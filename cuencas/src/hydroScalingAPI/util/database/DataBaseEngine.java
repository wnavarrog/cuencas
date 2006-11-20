/*
 * DataBaseEngine.java
 *
 * Created on June 12, 2003, 12:29 PM
 */

package hydroScalingAPI.util.database;

/**
 *
 * @author  ricardo
 */
public class DataBaseEngine {
    
    public static final int SMALLER_THAN=-1;
    public static final int EQUAL_TO=0;
    public static final int GREATER_THAN=1;
    
    java.util.Hashtable Tables;
    
    /** Creates a new instance of DataBaseEngine */
    public DataBaseEngine() {
        Tables = new java.util.Hashtable();
    }
    
    public void addTable(String tableName,String [] Fields,String [] Type){
        Tables.put(tableName,new DB_Table(Fields,Type));
    }
    
    public void addData(String tableName, String RegisterIndex,Object[] Values) {
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            addData(tableName,RegisterIndex,new DB_Register (thisTable.getFieldNames(),Values));
        }
    }
    
    public void addData(String tableName,String RegisterIndex,DB_Register Field){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            thisTable.addRegister(RegisterIndex,Field);
        }
    }
    
    public DB_Register getRegisterByMainIndex(String tableName,Object ValueToMatch){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getRegisterByMainIndex(ValueToMatch);
        }
        return null;
    }
    
    public DB_Register[] findRegister(String tableName,String[] FieldName,Object[] ValueToMatch){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.findRegister(FieldName,ValueToMatch);
        }
        return null;
    }
    
    public DB_Register[] compareFieldsTo(String tableName,String[] FieldName,Object[] ValueToCompare,int[] ConditionToCheckFor){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.compareFieldsTo(FieldName,ValueToCompare,ConditionToCheckFor);
        }
        return null;
    }
    
    public java.util.Vector getUniqueValues(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getUniqueValues(FieldName);
        }
        return null;
    }
    
    public Double getMaxValue(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getMaxValue(FieldName);
        }
        return null;
    }
    
    public Double getMinValue(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getMinValue(FieldName);
        }
        return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        hydroScalingAPI.util.database.DataBaseEngine DB=new hydroScalingAPI.util.database.DataBaseEngine();
        String[] states = {"colorado","alabama","N/A"};
        DB.addTable("Table1",new String[] {     "[code]",
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
                                                "[data accuracy]"},
                            new String[] {      "index",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "numeric",
                                                "numeric",
                                                "numeric",
                                                "numeric",
                                                "alpha",
                                                "alpha"}
                            );
                                                
        
            
        double realStart=java.util.Calendar.getInstance().getTimeInMillis();
        
        for (int j=0;j<1;j++){
            double Start=java.util.Calendar.getInstance().getTimeInMillis();
            for (int i=0;i<1000;i++) {
                DB.addData("Table1","000221"+i+(j*1000),new Object[] {  "000221"+i+(j*1000),
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        states[i%3],
                                                                        "N/A",
                                                                        new Double(10.0*Math.random()),
                                                                        new Double(10.0),
                                                                        new Double(1.0*(i+1)),
                                                                        new Double(6.0),
                                                                        "N/A",
                                                                        "N/A"});

            }
            double End=java.util.Calendar.getInstance().getTimeInMillis();
        
            double totalTime=(End-Start)/1000.;

            System.out.println("Ends loading this chunk of data in "+totalTime+" seconds");
        
        }
        
        double realEnd=java.util.Calendar.getInstance().getTimeInMillis();
        
        double realTotalTime=(realEnd-realStart)/1000.;

        System.out.println("Ends loading all the data in "+realTotalTime+" seconds");
        
        DB.addData("Table1","000221"+999999,new Object[] {  "000221"+999999,
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "colorado",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A"});
        
        System.out.println("Begin with queries");
        //System.out.println(DB.getRegisterByMainIndex("Table1","000221"+3));
        /*DB_Register[] result1=DB.findRegister("Table1",new String[] {"[state]","[longitude (deg:min:sec)]"},new Object[] {"alabama",new Double(10.0)});
        if (result1 != null) for (int i=0;i<result1.length;i++) System.out.println(result1[i]);
        java.util.Vector uniqueTest=DB.getUniqueValues("Table1","[state]");
        if (uniqueTest != null) for (int i=0;i<uniqueTest.size();i++) System.out.println(uniqueTest.get(i));*/
        DB_Register[] result2=DB.compareFieldsTo("Table1",new String[] {"[state]","[altitude ASL (m)]","[altitude ASL (m)]"},new Object[] {"alabama",new Double(10),new Double(50)},new int[] {0,1,-1});
        if (result2 != null) for (int i=0;i<result2.length;i++) System.out.println(result2[i]);
        /*Double result3=DB.getMinValue("Table1","[latitude (deg:min:sec)]");
        if (result3 != null) System.out.println(result3);
        Double result4=DB.getMaxValue("Table1","[latitude (deg:min:sec)]");
        if (result4 != null) System.out.println(result4);*/
        System.out.println("Done with queries");
    }
    
}

