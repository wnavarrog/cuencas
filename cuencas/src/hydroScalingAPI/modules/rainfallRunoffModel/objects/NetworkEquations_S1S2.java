/*
 * NetworkEquations.java
 *
 * Created on November 11, 2001, 10:26 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author  ricardo
 * @version
 */
public class NetworkEquations_S1S2 implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    
    private double qd, effPrecip, qs, qe, Q_trib, Qs_trib, K_Q ;
    private float flowdepth, hydrad, mannings_n ;
    //public double pcoe;
    
    /* PF ADDITION - START ... */
    private float[][] cheziArray, widthArray, lengthArray, slopeArray;
    private float[][] areasHillArray;
    
    private int hilltype, routingtype;
    private double satsurf, mst, Ku, qdh, qds, inf, re, qe1, qe2 ;
    private float [] runoffrat;
    /* PF ADDITION - ... END */
    
    private double[] output;
    /** Creates new NetworkEquations */
    public NetworkEquations_S1S2(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn){
        linksConectionStruct=links;
        basinHillSlopesInfo=hillinf;
        linksHydraulicInfo=linkIn;
        
        cheziArray=linksHydraulicInfo.getCheziArray();
        widthArray=linksHydraulicInfo.getWidthArray();
        lengthArray=linksHydraulicInfo.getLengthInKmArray();
        for (int i=0;i<lengthArray[0].length;i++) lengthArray[0][i]=lengthArray[0][i]*1000;
        slopeArray=linksHydraulicInfo.getSlopeArray();
        
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
        //System.out.println(input.length);
        
        int nLi=linksConectionStruct.headsArray.length;
        
        // Conditions for hillslope-channel processes
        hilltype = 1 ;
        routingtype = 1 ; // 1 = const vel w/var l, 4 = mannings
        output=new double[input.length];
        
        //output_vc=new double[nLi];
        
        switch (hilltype) {
            case 0 :    /* RM ORIGINAL */
                
                for (int i=0;i<nLi;i++){
                    
                    qd=Math.max(basinHillSlopesInfo.precipitation(i,time)-basinHillSlopesInfo.infiltRate(i,time),0.0);
                    effPrecip=basinHillSlopesInfo.precipitation(i,time)-qd;
                    
                    qs=((input[i+nLi] > basinHillSlopesInfo.So(i))?1:0)*(1/basinHillSlopesInfo.Ts(i)*(input[i+nLi]-basinHillSlopesInfo.So(i)));
                    qe=((input[i+nLi] > 0)?1:0)*(1/basinHillSlopesInfo.Te(i)*(input[i+nLi]));
                    
                    Q_trib=0.0;
                    
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
                    }
                    
                    
                    /*K_Q=3/2.*Math.pow(input[i],1/3.)
                     *Math.pow(22.0f,2/3.)
                     *Math.pow(linksHydraulicInfo.Width(i),-1/3.)
                     *Math.pow(linksHydraulicInfo.Length(i),-1)
                     *Math.pow(linksHydraulicInfo.Slope(i),1/3.);*/

                    /*K_Q=3/2.*Math.pow(input[i],1/3.)
                     *Math.pow(linksHydraulicInfo.Chezi(i),2/3.)
                     *Math.pow(linksHydraulicInfo.Width(i),-1/3.)
                     *Math.pow(linksHydraulicInfo.Length(i),-1)
                     *Math.pow(linksHydraulicInfo.Slope(i),1/3.);*/
                    
                    K_Q=14.2*Math.pow(input[i],1/3.)
                    *Math.pow(linksHydraulicInfo.Width(i),-1/3.)
                    *Math.pow(linksHydraulicInfo.Length(i),-1)
                    *Math.pow(linksHydraulicInfo.Slope(i),2/9.);
                    
                    /*K_Q=3.0/linksHydraulicInfo.Length(i);*/
                    
                    //the links
                    output[i]=60*K_Q*(1/3.6*basinHillSlopesInfo.Area(i)*(qd+qs)+Q_trib-input[i]);
                    
                    //the hillslopes
                    output[i+nLi]=1/60.*(effPrecip-qs-qe); //mm/hr to mm/min
                }
                
                break ;
                
            case 1 :    /* PF CHANGE */
                
                //RunoffRatios ratios = new RunoffRatios();
                //runoffrat = ratios.Read_ratios("/home/furey/HSJ/goodwin_ms/08mar04_gauss_Chezy_tests/01min_rain_steps/sims_01min/var_runoffrat_uniform/runoff_ratios_ev20.txt");
                //for (int i=0;i<10;i++) System.out.println(runoffrat[i]);
                
                for (int i=0;i<nLi;i++){
                    
                /* NOTE: Initial conditions are ...
                   input[i]  for link discharge
                   input[i+nLi] for link base flow
                   input[i+2*nLi] for hillslope S1
                   input[i+3*nLi] for hillslope S2 .

                   input[] is updated for each time step in DiffEqSolver.RKF . */
                    
                    /* CHANGE UNITS*/
                    //System.out.println(runoffrat[i]) ;
                    double prec_mmphr = basinHillSlopesInfo.precipitation(i,time);   // *0.0, *0.5f, *runoffrat[i]
                    //double prec_mmphr=Math.max(basinHillSlopesInfo.precipitation(i,time)-1.32,0.0); // 1.32 mm/hr from asae228j.pdf document, Its 50% of mean Ks for GC
                    double prec_mphr = prec_mmphr / 1000.;   // FROM mm/hr TO m/hr
                    double area_km2 = basinHillSlopesInfo.Area(i);
                    double area_m2 = area_km2*1e6;
                    
                    //if (i==1) System.out.println(basinHillSlopesInfo.precipitation(1,time));
                    //System.out.println(i);
                    //System.out.println(prec_mmphr);
                    
                    
                    /*HILLSLOPE FLUX CONDITIONS */
                    satsurf = basinHillSlopesInfo.S2Param(i) * input[i+3*nLi]; // dimless
                    mst = input[i+2*nLi]/( basinHillSlopesInfo.S2max(i) - input[i+3*nLi] ); // dimless
                    Ku = basinHillSlopesInfo.Ks(i) * ( Math.pow(mst,basinHillSlopesInfo.MstExp(i)) ); // mphr
                    
                    /*HILLSLOPE S1-SURFACE FLUX VALUES */
                    //if ( prec_mphr < basinHillSlopesInfo.Ks(i) ) {
                    //    inf = (1.0 - satsurf) * area_m2 * prec_mphr ; // m3phr
                    //    qdh = 0.0 ; // m3phr
                    //}
                    //else {
                    inf = 0.0D ; //m3phr
                    qdh = (1.0D - satsurf) * area_m2 * prec_mphr; // m3phr
                    //}
                    
                    qe1 = basinHillSlopesInfo.ETrate(i) * area_m2 * (1.0D - satsurf) * mst ; // m3phr
                    
                    /*HILLSLOPE S1-S2 FLUX VALUE */
                    re = basinHillSlopesInfo.Ks(i) * area_m2 * (1.0D - satsurf) *
                            ( Math.pow(mst,basinHillSlopesInfo.MstExp(i)) ); // m3phr
                    
                    /*HILLSLOPE S2-SURFACE FLUX VALUES */
                    qds = satsurf * area_m2 * prec_mphr ; // m3phr
                    qe2 = basinHillSlopesInfo.ETrate(i) * area_m2 * satsurf ; // m3phr, *0.0
                    qs = basinHillSlopesInfo.RecParam(i) * input[i+3*nLi] ; // m3phr
                    
                    /*HILLSLOPE DIRECT RUNOFF (TOTAL) FLUXES */
                    qd = qdh + qds ; // m3phr
                    
                    /*LINK FLUX ( Q )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Q_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Q_trib+=input[linksConectionStruct.connectionsArray[i][j]]; // units m^3/s
                    }
                    
                    /*LINK FLUX ( Q SUBSURFACE, BASE FLOW )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Qs_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Qs_trib+=input[linksConectionStruct.connectionsArray[i][j]+nLi]; // units m^3/s
                    }
                    
                    /*ROUTING RATE (K_Q) and CHANNEL VELOCITY (vc)*/
                    //System.out.println(routingtype);
                    
                    switch (routingtype) {
                        
                        case 0 :   /* Constant velocity and link length. For Goodwin, mean length = 221.8 and assume velocity 0.5 m/s */
                            K_Q = 0.5/221.8 ;  // units 1/s
                            break ;
                            
                        case 1 :    /* Constant velocity. For Goodwin, assume velocity 0.5 m/s */
                            K_Q = 0.5f*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 1.0*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 2.0*Math.pow(linksHydraulicInfo.Length(i),-1);  // units 1/s
                            break ;
                            
                        case 2 :    /* No Chezi explicitly */
                            K_Q=8.796*Math.pow(input[i],1/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],2/9.);   // units 1/s*/
                            break ;
                            
                        case 3 :    /* Chezi explicit */
                            //System.out.println("Chezy");
                            K_Q=3/2.*Math.pow(input[i],1/3.)
                            *Math.pow(cheziArray[0][i],2/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],1/3.); // units 1/s
                            break ;
                            
                        case 4 :    /* Mannings equation */
                            flowdepth = (float) ((1./3.)*Math.pow(input[i],1/3.)) ;   // depth m, input m^3/s; general observed relation for gc from molnar and ramirez 1998
                            hydrad = (flowdepth*widthArray[0][i])/(2.f*flowdepth + widthArray[0][i]) ;  // m
                            mannings_n = 0.030f ;       // mannings n suggested by Jason via his observations at Whitewater for high flows. Low flows will have higher n ... up to 2x more.
                            K_Q = ( Math.pow(hydrad,2./3.)
                            *Math.pow(slopeArray[0][i],1/2.)
                            /mannings_n )                          // m/s ; this term is v from mannings eqn
                            *Math.pow(lengthArray[0][i],-1);   // 1/s
                            break;
                            
                    }
                    
                    
           /*if (i == 62) {
               System.out.println("   WD ratio ="+ linksHydraulicInfo.Width(i)/flowdepth);
                System.out.println("   Mannings v (m/s) =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) );
                System.out.println("   K_Q =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) *Math.pow(linksHydraulicInfo.Length(i),-1) );
            }*/
                    if (input[i]==0.0D) K_Q=1e-10;
                    
                    
                    /* OUTPUT */
                    //LINK dQ/dt; big () term is m^3/s, 60*K_Q is 1/min
                    output[i]= 60.0D*K_Q*((1.0D/3600.)*(qd+qs)+Q_trib-input[i]);  //units (m^3/s)/min
                    
                    //LINK dQs/dt
                    output[i+nLi]= 60.0D*K_Q*((1.0D/3600.)*(qs)+Qs_trib-input[i+nLi]);  //units (m^3/s)/min
                    
                    //HILLSLOPE dS1/dt
                    output[i+(2*nLi)]=(1.0D/60.0)*(inf-re-qe1);  //units m3pmin
                    
                    //HILLSLOPE dS2/dt
                    output[i+(3*nLi)]=(1.0D/60.0)*(re-qs-qe2); //units m3pmin
                    
                }
                
                break ;
                
        }
        
        return output ;
    }
    
}
