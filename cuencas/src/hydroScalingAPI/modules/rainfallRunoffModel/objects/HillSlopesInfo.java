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
 *
 * @author Ricardo Mantilla
 */
public class HillSlopesInfo extends java.lang.Object {
    
    /*El proposito de este progrma es ser la base de datos de precipitacion, evaporacion, coductividad y en general todos los
           parametros asociados al sistema de laderas de la cuenca*/
    
    private float[][] areasArray,infilRateArray, SoArray, TsArray, TeArray;
    
    hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager thisStormEvent;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager thisInfiltManager;
    
    /** Creates new hillSlopesInfo */
    public HillSlopesInfo(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksCon) throws java.io.IOException{
        areasArray=linksCon.getVarValues(0);
    }
    
    public double Area(int i){
        return (double) areasArray[0][i];
        //return (double) 2.0; //false Geometry
    }
    
    public float[][] getAreasArray(){
        return areasArray;
    }
    
    public double precipitation(int HillNumber,double timeInMinutes){
        java.util.Calendar dateRequested =java.util.Calendar.getInstance(); 
        dateRequested.clear();
        dateRequested.setTimeInMillis((long) (timeInMinutes*60*1000));
        return thisStormEvent.getPrecOnHillslope(HillNumber,dateRequested);
    }
    
    public float maxPrecipitation(int HillNumber){
        return thisStormEvent.getMaxPrecOnHillslope(HillNumber);
    }
    
    public float meanPrecipitation(int HillNumber){
        return thisStormEvent.getMeanPrecOnHillslope(HillNumber);
    }
    
    public int Hilltype(int i){
        return 0;
    }
    
    //int type = Hilltype(i);
    //switch (Hilltype(i)) {
    //    case 0 :
    
    /*public double infiltRate(int HillNumber,double timeInMinutes){
        java.util.Calendar dateRequested =java.util.Calendar.getInstance(); dateRequested.clear();
        dateRequested.setTimeInMillis((long) (timeInMinutes*60*1000));
        return thisStormEvent.getPrecOnHillslope(HillNumber,dateRequested)*(1-thisInfiltManager.getInfiltrationOnHillslope(HillNumber));
    }*/
    
    public double infiltRate(int HillNumber,double timeInMinutes){
        return thisInfiltManager.getInfiltrationOnHillslope(HillNumber);
    }
    
    
    
    public double So(int i){
        return 1.0;          //So is max storage in the hillslope and i is the i-th link
    }
    
    public double Ts(int i){
        //return 10.0;         //Ts is the recesion constant in the hillslope and i is the i-th link
        return 10.0;                 // notice that in my work Ts is constant for all hillslopes so it doesn't depend on i
    }
    
    public double Te(int i){
        return 1e20;         //Te is the evaporation constant in the hillslope and i is the i-th link
        // notice that in my work Ts is constant for all hillslopes so it doesn't depend on i
    }
    
    public void setStormManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm){
        thisStormEvent=storm;
    }
    
    
    public void setInfManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infiltation){
        thisInfiltManager=infiltation;
    }
    
    //break;
    
    //case 1 :
    
    /* PF ADDITION - START ... */
    /* Working units are m, hr, .... */
    
        /* MEAN SATURATED DEPTH = the depth of s2 zone averaged
         * from hillslope ridge to stream. It is assumed constant in time
         * to make qs a linear function of s2. It could be time varying
         * to make qs a function of s2^2. */
    public double DepthMnSat(int i){
        double depth_m = 2.5;  // meters
        return depth_m;
    }
    
    public double Ks(int i){
        double ks_mpd = 1.023;
        double ks_mphr = ks_mpd*(1./24.);
        return ks_mphr;
    }
    
    public double MstExp(int i){
        return 11.0;  // this value dimensionless
    }
    
        /* In NetworkEquations, qs = (RecParam(i)*s2) . In this expression,
         * we can divide s2 by area (in denom of RecParam(i)). Given that
         * s2 = head * area, this shows that qs is proportional to head.
         * d3 here comes from Beta in Book 5 Notes. */
    public double RecParam(int i){
        double spec_yield = 0.01 ;  //dimensionless
        double area_km2 = Area(i); //areasArray[0][i]; //area km^2
        double area_m2 = area_km2*1e6; //km^2*1E6=m^2
        double d3_phr = (3.0*Ks(i)*DepthMnSat(i)) / (spec_yield*area_m2);
        return d3_phr; // [1/T]
    }
    
        /* In NetworkEquations, satsurf = (S2Param(i)*s2).
         * d4 comes from Duffy, via paper 3 */
    public double S2Param(int i){
        double porosity = 0.46 ;  //dimensionless
        double area_km2 = Area(i) ; //areasArray[0][i]; //area km^2
        double area_m2 = area_km2*1e6; //km^2*1E6=m^2
        double d4_pm3 = 0.905*(1./(porosity*DepthMnSat(i)*area_m2));
        return d4_pm3; // [1/L^3]
    }
    
    public double S2max(int i){
        double s2max_m3 = ( 1.0/ S2Param(i) ) ;
        return s2max_m3;
    }
    
    public double ETrate(int i){
        double etrate_mpd = 0.0034;
        double etrate_mphr = etrate_mpd*(1./24.);
        return etrate_mphr;
    }
    /* PF ADDITION - ... END */
    
    //break;
    //}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        System.out.println((( 1 == 0)?1:0));
        
    }
    
}
