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
 * hillSlopesInfo.java
 *
 * Created on November 11, 2001, 10:34 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * The purpose of this class is to be a centralized database for all the
 * inforamtion related to the system of hillslopes that compose the basin.  In the
 * current implementation this class produces aggregated outuput for the two
 * hillslopes draining into a stream link.  Future implementations will consider
 * the variability of the two hillslopes.  Information such as precipitaiton,
 * evaporation, soil parameters can be requested to this class.   Note:  In order
 * to implement new hillslope models this class must be updated to provide the
 * information for the hillslopes
 * @author Ricardo Mantilla
 */
public class HillSlopesInfo extends java.lang.Object {
    

    
    private float[][] areasArray,infilRateArray, SoArray, TsArray, TeArray;
    
    hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager thisStormEvent;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager thisInfiltManager;
    
    /**
     * Creates new instnace of hillSlopesInfo
     * @param linksCon The object describing the topologic connectivity of the river network
     * @throws java.io.IOException Captures errors while retreiving information
     */
    public HillSlopesInfo(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksCon) throws java.io.IOException{
        areasArray=linksCon.getVarValues(0);
    }
    
    /**
     * The total area of the hillslopes draining to the HillNumber-th link
     * @param HillNumber The index of the desired hillslope
     * @return The area in km^2
     */
    public double Area(int HillNumber){
        return (double) areasArray[0][HillNumber];
    }
    
    /**
     * The total area of the hillslopes draining to the HillNumber-th link
     * @param HillNumber The index of the desired hillslope
     * @return The area in km^2
     */
    public void setArea(int HillNumber,float newArea){
        areasArray[0][HillNumber]=newArea;
    }
    
    /**
     * The array of total area of the hillslopes draining corresponding link
     * @return An array with the hillslopes areas
     */
    public float[][] getAreasArray(){
        return areasArray;
    }
    
    /**
     * Returns the value of precipitation intensity in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param timeInMinutes The time in minutes
     * @return The value of rainfall intensity
     */
    public double precipitation(int HillNumber,double timeInMinutes){
        java.util.Calendar dateRequested =java.util.Calendar.getInstance(); 
        dateRequested.clear();
        dateRequested.setTimeInMillis((long) (timeInMinutes*60*1000));
        return thisStormEvent.getPrecOnHillslope(HillNumber,dateRequested);
    }
    
    /**
     * The maximum precipitatation intesity recorded over the hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of the maximum precipitation intensity
     */
    public float maxPrecipitation(int HillNumber){
        return thisStormEvent.getMaxPrecOnHillslope(HillNumber);
    }
    
    /**
     * The mean precipitatation intesity recorded over the hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of the mean precipitation intensity
     */
    public float meanPrecipitation(int HillNumber){
        return thisStormEvent.getMeanPrecOnHillslope(HillNumber);
    }
    
    /**
     * Returns the value of infiltration rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param timeInMinutes The time in minutes
     * @return The value of infiltration intensity
     */
    public double infiltRate(int HillNumber,double timeInMinutes){
        return thisInfiltManager.getInfiltrationOnHillslope(HillNumber);
    }
    
    /**
     * The Storage capacity of the hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of the storage capacity in m^3
     */
    public double So(int HillNumber){
        return 1.0;          //So is max storage in the hillslope and i is the i-th link
    }
    
    /**
     * The recesion rate in the hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of the recesion constant in 1/s
     */
    public double Ts(int HillNumber){
        return 10.0;         
    }
    
    /**
     * The evaporation rate in the hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of the evaporation rate in 1/s
     */
    public double Te(int HillNumber){
        return 1e20;
    }
    
    /**
     * Assigns a {@link hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager}
     * to handle precipitation for the group of hillslopes
     * @param storm The {@link hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager}
     */
    public void setStormManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm){
        thisStormEvent=storm;
    }
    
    /**
     * 
     * @param infiltation 
     */
    /**
     * Assigns a {@link hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager}
     * to handle infiltration rates for the group of hillslopes
     * @param infiltation The {@link hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager}
     */
    public void setInfManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infiltation){
        thisInfiltManager=infiltation;
    }
    
    
    /* PF ADDITION - START ... */
    /* Working units are m, hr, .... */
    
    /**
     * By Peter Furey:  MEAN SATURATED DEPTH = the depth of s2 zone averaged from hillslope
     * ridge to stream. It is assumed constant in time to make qs a linear function of s2.
     * It could be time varying to make qs a function of s2^2.
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double DepthMnSat(int HillNumber){
        double depth_m = 2.5;  // meters
        return depth_m;
    }
    
    /**
     * By Peter Furey:  Soil Hydraulic condictivity
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double Ks(int HillNumber){
        double ks_mpd = 1.023;
        double ks_mphr = ks_mpd*(1./24.);
        return ks_mphr;
    }
    
    /**
     * By Peter Furey:  undocumented
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double MstExp(int HillNumber){
        return 11.0;  // this value dimensionless
    }
    
    /**
     * By Peter Furey:  In NetworkEquations, qs = (RecParam(i)*s2) . In this expression,
     * we can divide s2 by area (in denom of RecParam(i)). Given that s2 = head * area,
     * this shows that qs is proportional to head.   d3 here comes from Beta in Book 5 Notes.
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double RecParam(int HillNumber){
        double spec_yield = 0.01 ;  //dimensionless
        double area_km2 = Area(HillNumber); //areasArray[0][i]; //area km^2
        double area_m2 = area_km2*1e6; //km^2*1E6=m^2
        double d3_phr = (3.0*Ks(HillNumber)*DepthMnSat(HillNumber)) / (spec_yield*area_m2);
        return d3_phr; // [1/T]
    }
    
    /**
     * By Peter Furey:  In NetworkEquations, satsurf = (S2Param(i)*s2). d4 comes from Duffy, via paper 3
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double S2Param(int HillNumber){
        double porosity = 0.46 ;  //dimensionless
        double area_km2 = Area(HillNumber) ; //areasArray[0][i]; //area km^2
        double area_m2 = area_km2*1e6; //km^2*1E6=m^2
        double d4_pm3 = 0.905*(1./(porosity*DepthMnSat(HillNumber)*area_m2));
        return d4_pm3; // [1/L^3]
    }
    
    /**
     * By Peter Furey:  undocumented
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double S2max(int HillNumber){
        double s2max_m3 = ( 1.0/ S2Param(HillNumber) ) ;
        return s2max_m3;
    }
    
    /**
     * By Peter Furey:  undocumented
     * @param HillNumber The index of the desired hillslope
     * @return NOTE: units undocumented
     */
    public double ETrate(int HillNumber){
        double etrate_mpd = 0.0034;
        double etrate_mphr = etrate_mpd*(1./24.);
        return etrate_mphr;
    }
    /* PF ADDITION - ... END */
    
}
