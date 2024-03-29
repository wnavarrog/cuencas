/*
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
 * NetworkEquations_ChannelLosses.java
 *
 * Created on July 3, 2007, 9:45 AM
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This calss implements the set of non-linear ordinary differential equations used
 * to simulate flows along the river network.  The function is writen as a
 * {@link hydroScalingAPI.util.ordDiffEqplver.BasicFunction}
 * that is used by the
 * {@link hydroScalingAPI.util.ordDiffEqplver.RKF}
 * @author Ricardo Mantilla
 */
public class NetworkEquations_AllMethod3 implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {

    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    java.util.Hashtable routingParams;
    private int routingType, HillType, HillVelType;
    private float rr;
    private double qp, qp_u, qp_l, qu_s, qe, Q_trib, K_Q, qs_l, q_in_res, q_out_res;
    private double effPrecip, qs;
    private double[] output;
    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopeArray, CkArray;
    private float[][] aH, upAreasArray, landCoversArray, slopesArray, Hill_K_NRCSArray;
    private float[][] hb, hH, Ht, Vt;
    private float[][] Area_Relief_Param;
    private double So, Ts, Te, vh; // not an array because I assume uniform soil properties
    private double lamda1, lamda2;
    private double vHunoff, vsub, SM, lambdaSCS; // velocity of the direct runoff and subsurface runoff - m/s
    private float[][] Vo, Gr;
    private int connectingLink;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries[] upFlows;
    private double change = 0.0;

    /**
     * Creates new NetworkEquations_Simple
     * @param links The topologic structure of the river network
     * @param hillinf The parameters manager for the system of hillsopes
     * @param linkIn The parameters manager for links in the network
    (parallel version)
     * @param connLink The parameters that define the upstream links in the network
     * @param inputFiles The parameters that define the files related to eac upstream links
     * @param (Hashtable) rP All the other parameters necessary for the rainfall-runoff model
     *                       - depends on the hilltype being used
     * CHANNEL ROUTING SCHEME.  Available schemes in this version are <br>
     *          <p>2: Constant Velocity</p>
     *          <p>5: Velocity based on parametrization v=Ck*q^lambda1*A^lambda2</p>

     * RAINFALL-RUNOFF TRANSFORMATION.  Available schemes in this version are <br>
     *          <p>0: Runoff coefficient
     *          <p>5: SCS Method

     * HILLSOPE ROUTING SCHEME.  Available schemes in this version are <br>
     *          <p>0: with delay - constant velocity in hillslope
     *          <p>5: SCS Method

     */
    public NetworkEquations_AllMethod3(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, int connLink, java.io.File[] inputFiles, java.util.Hashtable rP) {
        linksConectionStruct = links;
        basinHillSlopesInfo = hillinf;
        linksHydraulicInfo = linkIn;
        routingParams = rP;

        upAreasArray = linksHydraulicInfo.getUpStreamAreaArray(); //[km]
        cheziArray = linksHydraulicInfo.getCheziArray();
        manningArray = linksHydraulicInfo.getManningArray();
        widthArray = linksHydraulicInfo.getWidthArray();
        lengthArray = linksHydraulicInfo.getLengthInKmArray(); //[km]
        aH = basinHillSlopesInfo.getAreasArray();//[km2]

        for (int i = 0; i < lengthArray[0].length; i++) {
            lengthArray[0][i] = lengthArray[0][i] * 1000; //[m]
        }
        slopeArray = linksHydraulicInfo.getSlopeArray();

        landCoversArray = new float[1][lengthArray[0].length];
        slopesArray = new float[1][lengthArray[0].length];
        Hill_K_NRCSArray = new float[1][lengthArray[0].length];
        Area_Relief_Param = new float[4][lengthArray[0].length]; //Area in km and depth in m
        hb = new float[1][lengthArray[0].length]; // mm
        hH = new float[1][lengthArray[0].length]; // mm
        Ht = new float[1][lengthArray[0].length]; // mm
        Vt  = new float[1][lengthArray[0].length]; //[m3]


        float HD=-9.f;
        if (routingParams.get("ConstSoilStorage") != null) {
               HD = ((Float) routingParams.get("ConstSoilStorage")).floatValue();
        }
        //System.out.println("ConstSoilStorage"+HD);
        // HILLSLOPE GEOMETRY RELATIONSHIP
        for (int i = 0; i < lengthArray[0].length; i++) {
            landCoversArray[0][i] = (float) basinHillSlopesInfo.LandUseSCS(i);
            slopesArray[0][i] = (float) basinHillSlopesInfo.getHillslope(i);
            Hill_K_NRCSArray[0][i] = (float) basinHillSlopesInfo.Hill_K_NRCS(i);
            if(HD<0) hb[0][i] = (float) basinHillSlopesInfo.SCS_S2(i); // [mm] maximum storage - considering basin was dry
            else hb[0][i] = HD; // [mm] maximum storage - considering basin was dry
            hH[0][i] = (float) basinHillSlopesInfo.HillRelief(i)*1000; // [mm] hillslope relief
            Ht[0][i] = hb[0][i] + hH[0][i]; // [mm] hillslope relief
            Vt[0][i] = (float) ((hb[0][i]) * aH[0][i] * 1e3); // in m3
            Area_Relief_Param[0][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 0);
            Area_Relief_Param[1][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 1);
            Area_Relief_Param[2][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 2);
            Area_Relief_Param[3][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 3);
        }



        //So=basinHillSlopesInfo.So(0);
        //Ts=basinHillSlopesInfo.Ts(0);
        //Te=basinHillSlopesInfo.Te(0);
        //System.out.println("equation object 3");
        lamda1 = linksHydraulicInfo.getLamda1();
        lamda2 = linksHydraulicInfo.getLamda2();
        CkArray = linksHydraulicInfo.getCkArray();

        vh = 0.1;

        connectingLink = connLink;

        upFlows = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries[inputFiles.length];
        for (int i = 0; i < inputFiles.length; i++) {
            upFlows[i] = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries(inputFiles[i]);
        }

        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(2008, 5, 7, 0, 0, 0);
        change = date.getTimeInMillis() / 1000.0 / 60.0;

        // SCS implementation




        if (routingParams.get("RoutingT") != null) {
            routingType = (int) ((Float) routingParams.get("RoutingT")).floatValue();
        }
        if (routingParams.get("HillT") != null) {
            HillType = (int) ((Float) routingParams.get("HillT")).floatValue();
        }
        if (routingParams.get("HillVelocityT") != null) {
            HillVelType = (int) ((Float) routingParams.get("HillVelocityT")).floatValue();
        }
        //System.out.println("equation object 8");cc
        if (routingParams.get("RunoffCoefficient") != null) {
            rr = ((Float) routingParams.get("RunoffCoefficient")).floatValue();
        }

        // Future implementation (greenroof and reservoir)
        //Vo=basinHillSlopesInfo.getVolResArray();
        //Gr=basinHillSlopesInfo.getGreenRoofAreaArray(); // area of building in the hillslope (m2)
        System.out.println("routingType" + routingType);
        //for (int i=0;i<lengthArray[0].length;i++) Gr[0][i]=Gr[0][i]/(aH[0][i]*1000000);
        float vconst = ((Float) routingParams.get("Vconst")).floatValue();
        if (routingParams.get("vssub") != null) {
            vsub = ((Float) routingParams.get("vssub")).floatValue();
        }
        if (routingParams.get("vHunoff") != null) {
            vHunoff = ((Float) routingParams.get("vHunoff")).floatValue(); //[m/h]
        }        //System.out.println("equation object 10");
        if (routingParams.get("SoilMoisture") != null) {
            SM = ((Float) routingParams.get("SoilMoisture")).floatValue();
        }
        lambdaSCS=0.0;
        if (routingParams.get("lambdaSCSMethod") != null) {
            lambdaSCS = ((Float) routingParams.get("lambdaSCSMethod")).floatValue();
        }
        //System.out.println("equation object 11");
        System.out.println("Generate object NetworkEquations_AllMethod with parameters - routingType" + routingType + "HillType" + HillType + "HillVelType" + HillVelType);


    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input, float time) {
        return null;
    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public float[] eval(float[] input) {
        return null;
    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public double[] eval(double[] input) {
        return null;
    }

    /**
     * The acual evaluate method for this function.  The equation implemented here is
     *
     * dq/dt=sum(q_trib)-q
     * ds/dt=P-E-q_surf-q_sub
     *
     * Where q_trib is the flow for the incoming links, s is the storage in the
     * hillslope, P is precipitation, E is evaporation, q_surf is the overland runoff
     * and q_sub is the subsurface runoff
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input, double time) {
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes storage



        int nLi = linksConectionStruct.connectionsArray.length;

        output = new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(time*60.*1000.0));
        System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/

        switch (HillType) {
            case 0:    /* RUNOFF = RAINFALL - NO DELAY DELLAY FUNCTION */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }
                    double rrr = 1.0;

                    double hillPrec =  basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;
                    double p = rr * basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;

                    effPrecip = Math.max(p - basinHillSlopesInfo.infiltRate(i, time), 0.0);
                    qp = 0;
                    qp_u = 0;
                    qp_l = effPrecip; //Uncomment to eliminate the effect of hillslope routing
                    qs_l = 0.0;

                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }

                    K_Q = RoutingType(routingType, i, input[i]);

                    double ks = 0 / 3.6e6;

                    double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * aH[0][i] * (qp_l + qs_l)) + Q_trib - input[i] - chanLoss); //[m3/s]/min
/* hillslope reservoir*/ output[i + nLi] = 0.0;//(1/60.)*(qp-qp_l); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = 0.0;//(1/60.)*(qp_u-qs_l); //output[mm/min] effPrecip and qss [mm/hour]
/* runoff rate*/            output[i+3*nLi]=0.0; // runoff rate mm/min
                 output[i + 4 * nLi] = (1 / 60.) *  (qp_l);
                                output[i + 5 * nLi] = (1 / 60.) *  (qs_l);
                                output[i + 6 * nLi] = (1 / 60.) *  (hillPrec);


                }

                break;

            case 1:    /* RUNOFF = RAINFALL - WITH DELLAY FUNCTION */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }
                    double rrr = 1.0;

               //    if (rr == 1.0f) { // for cedar river iowa simulation
               //        if (rr == 1.0f) { // for cedar river iowa simulation
               //         rrr = 0.55;
//                        if (time >= 20200620 && time < 20216460) {
//                            rrr = 0.55;
//                        }
//                        if (time > 20216460) {
//                            rrr = 0.70;
//                        }
               //     }

              //      }
                    double hillPrec =  basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;
                    double p = rr * basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;

                    effPrecip = Math.max(p - basinHillSlopesInfo.infiltRate(i, time), 0.0); //[mm/h]
                    qp = effPrecip; //[mm/h]
                    qp_u = Math.max(0, p - effPrecip); //[mm/h]
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]);

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])
                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));


                    //qp_l=effPrecip; //Uncomment to eliminate the effect of hillslope routing
                    qs_l = 0.0;
                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    double ks = 0 / 3.6e6;

                    double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;


                    /* the links*/ output[i] = 60 * K_Q * (((1 / 3.6) * aH[0][i] * (qp_l + qs_l)) + Q_trib - input[i] - chanLoss); //[m3/s]/min
/* hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = (1 / 60.) * (qp_u - qs_l); //output[mm/min] effPrecip and qss [mm/hour]
/* soil reservoir*/ output[i + 3 * nLi] = 0.0f; //output[mm/min] effPrecip and qss [mm/hour]
                              output[i + 4 * nLi] = (1 / 60.) *  (qp_l);
                                output[i + 5 * nLi] = (1 / 60.) *  (qs_l);
                                output[i + 6 * nLi] = (1 / 60.) *  (hillPrec);


                }
                break;

             case 2:
                /* The same as 3 but saves contribution of surface and subsurface to estimate runoff coefficient
                 * Presents a better representation of the subsurface flow
                 * Considering hillslope shape
                 * Applies SCS method to estimate infiltration
                 * This is the SCS equation, where Qacum=(Pacum-Ia)^2/(Pacum-Ia+St)
                 * Pe=Pacum-Ia
                 * So=(SI-M)
                 * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                 * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                 * M=soil moisture at time t
                 */
                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    // HILLSLOPE GEOMETRY RELATIONSHIPS

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap =0; //[km2]
                    double dsoil=0; //[mm]
                    double Vs_sat=0; //[m3]
                    double Vs_unsat=0; //[m3]
                    double da_dh = 0.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i])/1000; // [m]
                    if (hrel < 0) {
                        hrel = hb[0][i] / 1000;
                    }
                    double hrelmax =  (Ht[0][i] - hb[0][i])/1000; // [m]



                    // check limit of soil parameters
                    if(input[i + 3 * nLi]<0) input[i + 3 * nLi]=0; // [%]
                    if(input[i + 3 * nLi]>1) input[i + 3 * nLi]=1; // [%]

                    if (hrel >= 0 && hrel < hrelmax) {
                        if(hrel==0) ai = 0; // [km2]
                        else ai = aH[0][i]*(Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel/hrelmax)
                                + Area_Relief_Param[2][i] * Math.pow((hrel/hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel/hrelmax), 3));//km2
                        if(ai<0) ai=0;
                        if(ai>aH[0][i]) ai=aH[0][i];
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat=(ai*hb[0][i]*1e3); //in m3
                        Vs_unsat=(Vt[0][i]-Vs_sat); //in meters3
                        da_dh = (aH[0][i]/hrelmax)*(Area_Relief_Param[1][i]
                                 + 2 * Area_Relief_Param[2][i] * (hrel/hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel/hrelmax), 2))*1e6; //[m2/m]
                        if(da_dh <0) da_dh=0;

                    } //[km]

                    if (hrel>=hrelmax)
                    {
                        dsoil=0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel=hrelmax;
                        input[i + 2 * nLi]=hrelmax*1000+hb[0][i];
                        Vs_sat=Vt[0][i];
                        Vs_unsat=0;
                        ap =0;
                        ai=aH[0][i];
                        da_dh = (aH[0][i]/hrelmax)*(Area_Relief_Param[1][i]
                                 + 2 * Area_Relief_Param[2][i] * (hrel/hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel/hrelmax), 2))*1e6; //[m2/m]
                        if(da_dh <0) da_dh=0;
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)



                    // Hillslope model - estimation of infiltration
                    double RC= 0;
                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p ;
                    if (input[i+nLi] > 0.0f) {
                        RC= input[i+nLi] * (input[i+nLi] + 2 * dsoil) / Math.pow((input[i+nLi] + dsoil), 2); //[%]
                        if (RC> 1) {
                            RC= 1;
                        }
                        if (RC< 0) {
                            RC= 0;
                        }
                         /// this would guarantee it is in mm/h
                        qp_u = ap * (1 - RC) * input[i+nLi]/aH[0][i]; //[mm/h]
                       //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                       //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h
                    if(vH>500) vH=500;
                    if(vH<1) vH=1;
                    if(Double.isNaN(vH)) vH=1;

                    double Ksat = (vsub);     //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])
                    
                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat=Ksat*Math.exp(input[i + 3 * nLi]-1);
                    if(Double.isNaN(vH)) kunsat=Ksat;
                    qu_s = kunsat *input[i + 3 * nLi]*1000; //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                     if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!

                    else qs_l =0;
                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);



/* the links*/                  output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l)+(aH[0][i] * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                                if(Double.isNaN(output[i])) output[i]=0.0;
/*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u); //output[mm/h/min]
                                if(Double.isNaN(output[i + nLi])) output[i + nLi]=0.0;
/* depth - saturated area*/
                                double dVs_sat_dt=(1e3)*(ap * qu_s - aH[0][i] * qs_l); //m3/h
                                if(da_dh>0 && hb[0][i]>0) output[i + 2 * nLi] =(1 / 60.) *1e6* (dVs_sat_dt/(hb[0][i]*da_dh)) ; //output[mm/min] effPrecip and qss [mm/hour]
                                else output[i + 2 * nLi] =0;
                                if(Double.isNaN(output[i + 2 * nLi])) output[i + 2 * nLi]=0.0;
                                if(Vs_unsat!=0)
/* soil moisture*/              output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3*(aH[0][i]*qp_u - (ap)*qu_s) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                                else output[i + 3 * nLi]=0;
                                if(Double.isNaN(output[i + 3 * nLi])) output[i + 3 * nLi]=0.0;
                    output[i + 4 * nLi] = (1 / 60.) * (qp);
                    output[i + 5 * nLi] = (1 / 60.) * (qp_u);
                    output[i + 6 * nLi] = (1 / 60.) * (qp_l);
                    output[i + 7 * nLi] = (1 / 60.) * (qu_s);
                    output[i + 8 * nLi] = (1 / 60.) * (qs_l);
                    output[i + 9 * nLi] = (1 / 60.) * (p);
                    output[i + 10 * nLi] = ai / aH[0][i];
                    output[i + 11 * nLi] = RC;
                    output[i + 12 * nLi] = da_dh;
                    output[i + 13 * nLi] = dVs_sat_dt;


                }
                break;



        }


        // Evaluate the output flow for the upstream links - do not change with rainfall-runoff model
        for (int k = 0; k < upFlows.length; k++) {
            int i = connectingLink;

            K_Q = RoutingType(routingType, i, input[i]);

            output[i] += 60 * K_Q * (upFlows[k].evaluate(time));
        }

        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        return output;
    }

    public double Hillvelocity(int method, int hilslope, double SurfDepth) {
        double vH = 1.0;
        int i = hilslope;
        double Slope = Math.max(0.005, slopesArray[0][i]);

        int nLi = linksConectionStruct.connectionsArray.length;
        switch (method) {
            case 0: //constant
                vH = vHunoff;
                break;

            case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = (1 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000, (2 / 3)) * 3600; //(m/h)
                break;
            case 2: //as a function of land cover
                if (landCoversArray[0][i] == 0) {
                    vH = 500.0; // water
                } else if (landCoversArray[0][i] == 1) {
                    vH = 250.0; // urban area
                } else if (landCoversArray[0][i] == 2) {
                    vH = 100.0; // baren soil
                } else if (landCoversArray[0][i] == 3) {
                    vH = 10.0; // Forest
                } else if (landCoversArray[0][i] == 4) {
                    vH = 100.0; // Shrubland
                } else if (landCoversArray[0][i] == 5) {
                    vH = 20.0; // Non-natural woody/Orchards
                } else if (landCoversArray[0][i] == 6) {
                    vH = 100.0; // Grassland
                } else if (landCoversArray[0][i] == 7) {
                    vH = 20.0; // Row Crops
                } else if (landCoversArray[0][i] == 8) {
                    vH = 100.0; // Pasture/Small Grains
                } else if (landCoversArray[0][i] == 9) {
                    vH = 50.0; // Wetlands
                }
                break;
            case 3: //NRCS method manning roughness as a function of land cover and soil hyd group
                vH = (Hill_K_NRCSArray[0][i]) * Math.pow((Slope* 100), 0.5)  * 0.3048; //(m/h)
                if (vH > 500) {
                    vH = 500;
                }
                if (vH < 10) {
                    vH = 10;                      //m/h
                }
                break;
        }
        return vH;
    }

    public double RoutingType(int method, int hilslope, double Qchannel) {
        int i = hilslope;
        switch (method) {
            case 2:
                K_Q = CkArray[0][i] / lengthArray[0][i];
                break;
            case 5:
                K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                break;
        }

        if (Qchannel == 0) {
            K_Q = 0.1 / (lengthArray[0][i]);
        }

        return K_Q;
    }
}