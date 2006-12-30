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
 * NetworkEquations_FlowOnly.java
 *
 * Created on December 1, 2005, 3:10 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author  John Mobley
 */
public class NetworkEquations_FlowOnly implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    private int routingType;
    
    private double qd, effPrecip, qs, qe, Q_trib, K_Q;
    private double[] output;
    
    private float[][] cheziArray, widthArray, lengthArray, slopeArray;
    private float[][] areasHillArray;
    private double So,Ts,Te; // not an array because I assume uniform soil properties
    
    /** Creates new NetworkEquations */
    public NetworkEquations_FlowOnly(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, int rt){
        linksConectionStruct=links;
        basinHillSlopesInfo=hillinf;
        linksHydraulicInfo=linkIn;
        routingType=rt;
        
        cheziArray=linksHydraulicInfo.getCheziArray();
        widthArray=linksHydraulicInfo.getWidthArray();
        lengthArray=linksHydraulicInfo.getLengthInKmArray();
        for (int i=0;i<lengthArray[0].length;i++) lengthArray[0][i]=lengthArray[0][i]*1000;
        slopeArray=linksHydraulicInfo.getSlopeArray();
        
        areasHillArray=basinHillSlopesInfo.getAreasArray();
        So=basinHillSlopesInfo.So(0);
        Ts=basinHillSlopesInfo.Ts(0);
        Te=basinHillSlopesInfo.Te(0);
        
    }
    
    public float[] eval(float[] input, float time) {
        return new float[0];  // dummy
    }    
    
    public float[] eval(float[] input) {
        return new float[0];  // dummy
    }    

    public double[] eval(double[] input) {
        return new double[0];  // dummy
    }
    
    public double[] eval(double[] input, double time) {
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes storage

        for (int i=0;i<input.length;i++){
            if (input[i] < 0) input[i]=0;
        }

        int nLi=linksConectionStruct.connectionsArray.length;

        output=new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(time*60.*1000.0));
        System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/
        
        double maxInt=0;
        
        for (int i=0;i<nLi;i++){
            
            if (input[i] < 0) input[i]=0;
            
            double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);
            
            maxInt=Math.max(maxInt,hillPrecIntensity);
            
            qd=Math.max(hillPrecIntensity-basinHillSlopesInfo.infiltRate(i,time),0.0);
            effPrecip=hillPrecIntensity-qd;
            
            qs=0.0;//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qe=((input[i+nLi] > 0)?1:0)*(1/Te*(input[i+nLi]));
            
            Q_trib=0.0;
            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }
            
            switch (routingType) {
                
                case 0:     K_Q=3/2.*Math.pow(input[i],1/3.)
                                *Math.pow(cheziArray[0][i],2/3.)
                                *Math.pow(widthArray[0][i],-1/3.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],1/3.);

                            break;    

                case 1:     K_Q=3/2.*Math.pow(input[i],1/3.)
                                *Math.pow(20.0,2/3.)
                                *Math.pow(widthArray[0][i],-1/3.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],1/3.);

                            break;    

                case 2:     K_Q=3.0/lengthArray[0][i];
                            break;
            
            }
            
            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);
            
            /*if (Math.random() > 0.5) {
                float typDisch=Math.round(input[i]*10000)/10000.0f;
                float typVel=Math.round(K_Q*lengthArray[0][i]*100)/100.0f;
                long typDepth=Math.round(input[i]/typVel/widthArray[0][i]*100);
                long typWidth=Math.round(widthArray[0][i]);
                System.out.println("  --> !!  When Discharge is "+typDisch+"m3/s, A typical Velocity-Depth-Width triplet is "+typVel+" m/s - "+typDepth+" cm - "+typWidth+" cm >> for Link "+i);
            }*/
            
            //the links
            output[i]=60*K_Q*(1/3.6*areasHillArray[0][i]*(qd+qs)+Q_trib-input[i]);
            
            //the hillslopes
            output[i+linksConectionStruct.connectionsArray.length]=1/60.*(effPrecip-qs-qe);
            
        }
        
        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        

        return output;
    }
    
}
