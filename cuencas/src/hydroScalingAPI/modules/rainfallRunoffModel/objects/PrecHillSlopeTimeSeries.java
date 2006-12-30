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
 *
 * @author Ricardo Mantilla 
 */
public class PrecHillSlopeTimeSeries {
    
    private float timeInterval;
    private int regInterval; //Register interval in milliseconds
    
    private java.util.Vector recordValue;
    private java.util.Vector recordTime;
    
    private float maxPrec=0.0f;
    private float totalPrec=0.0f;
    private int numRecordsWithRain=0;

    /** Creates new precHillSlopeTimeSeries */
    public PrecHillSlopeTimeSeries(int regIn) {
        recordValue=new java.util.Vector();
        recordTime=new java.util.Vector();
        regInterval=regIn;
    }
    
    public void addDateAndValue(java.util.Calendar newRecordTime, Float newRecordValue){
        recordTime.add(newRecordTime);
        recordValue.add(newRecordValue);
        
        numRecordsWithRain++;
        maxPrec=Math.max(maxPrec,newRecordValue.floatValue());
        totalPrec+=newRecordValue.floatValue();
    }
    
    public float getPrec(java.util.Calendar atThisTime){
        
        java.util.Calendar serchForTime = java.util.Calendar.getInstance(); 
        serchForTime.setTimeInMillis(atThisTime.getTimeInMillis());
        
        if (recordTime.size() == 0) return 0.0f;
        if (serchForTime.before(recordTime.firstElement())) return 0.0f;
        
        long iniTimeMill=((java.util.Calendar)recordTime.firstElement()).getTimeInMillis();
        
        serchForTime.setTimeInMillis(iniTimeMill+regInterval*((atThisTime.getTimeInMillis()-iniTimeMill)/regInterval)); //To round the time to the previous exact record time

        if (serchForTime.after(recordTime.lastElement())) return 0.0f;

        /*
        System.out.println(atThisTime.getTimeInMillis());
        System.out.println(((java.util.Calendar)recordTime.firstElement()).getTimeInMillis());
        System.out.println(((java.util.Calendar)recordTime.lastElement()).getTimeInMillis());
        System.exit(0);
        */
        
        //System.out.println(serchForTime);
        //System.out.println(iniTimeMill);
        
        int k=recordTime.indexOf(serchForTime);
        //System.out.println(k);
        //System.exit(0);
        
        if (k == -1) return 0.0f;
        
        return ((Float) recordValue.get(k)).floatValue();
    }
    
    public float getSize(){
        return recordValue.size();
    }
    
    public float getMaxPrec(){
        return maxPrec;
    }
    
    public float getMeanPrec(){
        if(numRecordsWithRain == 0) return 0.0f;
        return totalPrec/(float)numRecordsWithRain;
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
    }

}
