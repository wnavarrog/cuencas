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
 * precHillSlopeTimeSeries.java
 *
 * Created on June 26, 2002, 3:02 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * This class manages a time series of parameters associated to the hillslope.  For
 * example rainfall, or any other temporaly varaible parameter.
 * @author Ricardo Mantilla
 */
public class HillSlopeTimeSeries {
    
    private int regInterval;
    
//    private java.util.Vector recordValue;
//    private java.util.Vector recordTime;
    
    private java.util.Hashtable recordTimeValue;
    
    private float maxPrec=0.0f;
    private float totalPrec=0.0f;
    private int numRecordsWithRain=0;
    
    private boolean first=true;
    private long iniTimeMill;

    /**
     * Creates new HillSlopeTimeSeries
     * @param regIn Register interval in milliseconds
     */
    public HillSlopeTimeSeries(int regIn) {
//        recordValue=new java.util.Vector();
//        recordTime=new java.util.Vector();
        
        recordTimeValue=new java.util.Hashtable<java.util.Calendar, Float>();
        
        regInterval=regIn;
    }
    
    /**
     * Adds a new date and value to the time series
     * @param newRecordTime The time at which the record was taken
     * @param newRecordValue The value of the record taken
     */
    public void addDateAndValue(java.util.Calendar newRecordTime, Float newRecordValue){
//        recordTime.add(newRecordTime);
//        recordValue.add(newRecordValue);
        
        if(first){
            iniTimeMill=newRecordTime.getTimeInMillis();
            first=false;
        }
        
        recordTimeValue.put(newRecordTime,newRecordValue);
        
        numRecordsWithRain++;
        maxPrec=Math.max(maxPrec,newRecordValue.floatValue());
        totalPrec+=newRecordValue.floatValue();
    }
    
    /**
     * Returns a value for the record for a given time
     * @param atThisTime The desired time to evaluate
     * @return A float with the record value
     */
    public float getRecord(java.util.Calendar atThisTime){
        
        java.util.Calendar serchForTime = java.util.Calendar.getInstance();
        serchForTime.setTimeInMillis(iniTimeMill+regInterval*((atThisTime.getTimeInMillis()-iniTimeMill)/regInterval)); //To round the time to the previous exact record time
        Float valueToReturn=((Float)recordTimeValue.get(serchForTime));
        
        if (valueToReturn == null){
            return 0.0f;
        } else return valueToReturn;
        

//        java.util.Calendar serchForTime = java.util.Calendar.getInstance(); 
//        serchForTime.setTimeInMillis(atThisTime.getTimeInMillis());
//        
//        if (recordTime.size() == 0) return 0.0f;
//        if (serchForTime.before(recordTime.firstElement())) return 0.0f;
//        
//        long iniTimeMill=((java.util.Calendar)recordTime.firstElement()).getTimeInMillis();
//        
//        serchForTime.setTimeInMillis(iniTimeMill+regInterval*((atThisTime.getTimeInMillis()-iniTimeMill)/regInterval)); //To round the time to the previous exact record time
//
//        if (serchForTime.after(recordTime.lastElement())) return 0.0f;
//
//        int k=recordTime.indexOf(serchForTime);
//        
//        if (k == -1) return 0.0f;
//        
//        return ((Float) recordValue.get(k)).floatValue();
        
    }
    
    /**
     * The record lenght
     * @return The record lenght
     */
    public float getSize(){
//        return recordTime.size();
        return recordTimeValue.size();
    }
    
    /**
     * The maximum value observed in the time series
     * @return The maximum recorded value
     */
    public float getMaxRecord(){
        return maxPrec;
    }
    
    /**
     * The average value observed in the time series
     * @return The mean recorded value
     */
    public float getMeanRecord(){
        if(numRecordsWithRain == 0) return 0.0f;
        return totalPrec/(float)numRecordsWithRain;
    }
    
}
