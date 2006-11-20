/*
 * DB_Table.java
 *
 * Created on June 16, 2003, 1:34 AM
 */

package hydroScalingAPI.util.database;

/**
 *
 * @author  ricardo
 */
public class DB_Table {
    
    private String[] FieldNames;
    private String[] FieldTypes;
    private java.util.Hashtable nameToTypeMap=new java.util.Hashtable();
    private java.util.Vector indexNames=new java.util.Vector();
    private java.util.Hashtable uniqueValues=new java.util.Hashtable();
    private java.util.Hashtable FieldsInformation=new java.util.Hashtable();
    private int indexField = -1;
    private int lastIndexUsed = 0;
    
    private boolean first=true;
    
    public DB_Table(String[] Fields,String[] Type) {
        
        FieldNames=Fields;
        FieldTypes=Type;
        for (int i=0;i<Fields.length;i++){
            
            nameToTypeMap.put(FieldNames[i],FieldTypes[i]);
            
            if(FieldTypes[i].equalsIgnoreCase("index")) indexField=i;
            
            if (FieldTypes[i].equalsIgnoreCase("alpha")) {
                java.util.Vector thisNewVector=new java.util.Vector();
                uniqueValues.put(FieldNames[i],thisNewVector);
            }
            
            if (FieldTypes[i].equalsIgnoreCase("numeric")) uniqueValues.put(FieldNames[i],new Double[] {new Double(Double.MAX_VALUE),new Double(-Double.MAX_VALUE)});
            
        }
        
    }
    
    public void addRegister(String RegisterIndex,DB_Register Register) {
        indexNames.addElement(RegisterIndex);
        FieldsInformation.put(RegisterIndex,Register);
        checkUniqueness(Register);
    }
    
    public void addRegister(DB_Register Register) {
        String RegisterIndex="";
        if (indexField == -1) 
            RegisterIndex=""+(lastIndexUsed++);
        else
            RegisterIndex=(String) (Register.getProperty((String)FieldNames[indexField]));
        
        addRegister(RegisterIndex,Register);
    }
    
    public void checkUniqueness(DB_Register Register) {
        double numericValue=0.0;
        for (int i=0;i<FieldNames.length;i++){
            if (i != indexField){
                Object theProperty=Register.getProperty(FieldNames[i]);
                if (FieldTypes[i].equalsIgnoreCase("alpha")){
                    if (!theProperty.toString().equalsIgnoreCase("n/a")){
                        java.util.Vector theVector=(java.util.Vector)uniqueValues.get(FieldNames[i]);
                        if (!theVector.contains(theProperty)){
                            theVector.add(theProperty);
                        }
                    }
                }
                if (FieldTypes[i].equalsIgnoreCase("numeric")){
                    double minValue=((Double[])uniqueValues.get(FieldNames[i]))[0].doubleValue();
                    double maxValue=((Double[])uniqueValues.get(FieldNames[i]))[1].doubleValue();
                    if (!theProperty.toString().equalsIgnoreCase("n/a")){
                        try{
                            numericValue = ((Double) theProperty).doubleValue();
                        } catch(java.lang.ClassCastException CE_D){
                            try{
                                numericValue=((Float) theProperty).doubleValue();
                            } catch(java.lang.ClassCastException CE_F){
                                try{
                                    numericValue=((Integer) theProperty).doubleValue();
                                } catch(java.lang.ClassCastException CE_I){
                                    
                                }
                            }
                        }
                        
                        minValue=Math.min(numericValue,minValue);
                        maxValue=Math.max(numericValue,maxValue);
                        uniqueValues.put(FieldNames[i],new Double[] {new Double(minValue),new Double(maxValue)});
                        
                    }
                }
            }
        }
    }
    
    public DB_Register getRegisterByMainIndex(Object ValueToMatch){
        return (DB_Register) FieldsInformation.get((String) ValueToMatch);
    }
    
    public DB_Register[] findRegister(String[] FieldName,Object[] ValueToCompare){
        
        int[] ConditionToCheckFor=new int[FieldName.length];
        java.util.Arrays.fill(ConditionToCheckFor,0);
        return compareFieldsTo(FieldName,ValueToCompare,ConditionToCheckFor);
    }
    
    public DB_Register[] compareFieldsTo(String[] FieldName,Object[] ValueToCompare,int[] ConditionToCheckFor){
        for (int i=0;i<FieldName.length;i++){
             if (nameToTypeMap.get(FieldName[i]) == "alpha" && ConditionToCheckFor[i] != 0) return null; 
        }
        
        java.util.Vector serchAt=(java.util.Vector) indexNames.clone();
        for (int i=0;i<FieldName.length;i++){
            for (int j=0;j<serchAt.size();j++){
                DB_Register thisField = (DB_Register) FieldsInformation.get(serchAt.get(j));

                if (nameToTypeMap.get(FieldName[i]) == "numeric"){
                    double numericValue=0;
                    try{
                        numericValue = ((Double) thisField.getProperty(FieldName[i])).doubleValue();
                    } catch(java.lang.ClassCastException CE_D){
                        try{
                            numericValue=((Float) thisField.getProperty(FieldName[i])).doubleValue();
                        } catch(java.lang.ClassCastException CE_F){
                            try{
                                numericValue=((Integer) thisField.getProperty(FieldName[i])).doubleValue();
                            } catch(java.lang.ClassCastException CE_I){
                                
                            }
                        }
                    }
                    Double thisProperty=new Double(numericValue);
                    
                    switch (ConditionToCheckFor[i]){
                        case 0:  
                            if (!thisProperty.equals(ValueToCompare[i])){
                                serchAt.remove(j);
                                j--;
                            }
                            break;
                        case 1:  
                            if (thisProperty.compareTo((Double)ValueToCompare[i]) <= 0){
                                serchAt.remove(j);
                                j--;
                            }
                            break;
                        case -1:
                            if (thisProperty.compareTo((Double)ValueToCompare[i]) >= 0){
                                serchAt.remove(j);
                                j--;
                            }
                            break;

                    }


                } else {
                    if (!thisField.getProperty(FieldName[i]).equals(ValueToCompare[i])){
                        serchAt.remove(j);
                        j--;
                    }
                }
            }
        }
        
        if (serchAt.size() > 0){
        
            DB_Register[] results=new DB_Register[serchAt.size()];
            for (int j=0;j<serchAt.size();j++)
                    results[j]= (DB_Register) FieldsInformation.get((String)serchAt.get(j));

            return results;
        } else {
            return null;
        }
    }
    
    public Double getMaxValue(String FieldName) {
        return((Double[])uniqueValues.get(FieldName))[1];
    }
    
    public Double getMinValue(String FieldName) {
        return((Double[])uniqueValues.get(FieldName))[0];
    }
    
    public String[] getFieldNames(){
        return FieldNames;
    }
    
    public java.util.Vector getUniqueValues(String FieldName){
        
        if (first){
            for (int i=0;i<FieldNames.length;i++){

                if (FieldTypes[i].equalsIgnoreCase("alpha")) {
                    java.util.Vector unsortedVector=(java.util.Vector)uniqueValues.get(FieldNames[i]);
                    java.util.Collections.sort(unsortedVector);
                    unsortedVector.add(0,"--------");
                }
            }
            first=false;
        }
        
        if(FieldName.equalsIgnoreCase(FieldNames[indexField])) return indexNames;
        
        return (java.util.Vector)uniqueValues.get(FieldName);
    }
    
}