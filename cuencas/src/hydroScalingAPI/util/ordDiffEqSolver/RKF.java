/*
 * RKF.java
 *
 * Created on November 11, 2001, 10:23 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  Ricardo Mantilla 
 */
public class RKF extends java.lang.Object {

    hydroScalingAPI.util.ordDiffEqSolver.BasicFunction theFunction;
    public double[] initialCond, finalCond;
    double epsilon;
    double basicTimeStep;
    //Scheme parameters
    double[]     a={ 0.,            1/5.,        3/10.,       3/5.,             1.,      7/8. };
    double[][]   b={
                   {0.},
                   {1/5.},
                   {3/40.,          9/40.},
                   {3/10.,          -9/10.,       6/5.},
                   {-11/54.,        5/2.,        -70/27.,     35/27.},
                   {1631/55296.,    175/512.,    575/13824.,  44275/110592.,	253/4096.    }};

     double[]     c={37/378.,      0.,          250/621.,        125/594.,        0.,          512/1771.};
     double[] cStar={2825/27648.,  0.,		18575/48384.,    13525/55296.,    277/14336.,  1/4.     };
     
     double[] Derivs;
     double[] carrier,k0,k1,k2,k3,k4,k5,newY,newYstar;
     
     double Delta,newTimeStep,factor;

    /** Creates new RKF */
    public RKF(hydroScalingAPI.util.ordDiffEqSolver.BasicFunction fu, double eps, double basTs) {
        theFunction=fu;
        epsilon=eps;
        basicTimeStep=basTs;
    }
    
    public double[][] step(double currentTime, double[] IC, double timeStep, boolean finalize){
        
        carrier=new double[IC.length];
        
        k0=theFunction.eval(IC,        currentTime);
        for (int i=0;i<IC.length;i++) carrier[i]=Math.max(0,IC[i]+timeStep*b[1][0]*k0[i]);
        
        k1=theFunction.eval(carrier,   currentTime+a[1]*timeStep);
	for (int i=0;i<IC.length;i++) carrier[i]=Math.max(0,IC[i]+timeStep*(b[2][0]*k0[i]+b[2][1]*k1[i]));
	
        k2=theFunction.eval(carrier,   currentTime+a[2]*timeStep);
	for (int i=0;i<IC.length;i++) carrier[i]=Math.max(0,IC[i]+timeStep*(b[3][0]*k0[i]+b[3][1]*k1[i]+b[3][2]*k2[i]));
	
        k3=theFunction.eval(carrier,   currentTime+a[3]*timeStep);
	for (int i=0;i<IC.length;i++) carrier[i]=Math.max(0,IC[i]+timeStep*(b[4][0]*k0[i]+b[4][1]*k1[i]+b[4][2]*k2[i]+b[4][3]*k3[i]));
	
        k4=theFunction.eval(carrier,   currentTime+a[4]*timeStep);
	for (int i=0;i<IC.length;i++) carrier[i]=Math.max(0,IC[i]+timeStep*(b[5][0]*k0[i]+b[5][1]*k1[i]+b[5][2]*k2[i]+b[5][3]*k3[i]+b[5][4]*k4[i]));
	
        k5=theFunction.eval(carrier,   currentTime+a[5]*timeStep);
        
	newY=new double[IC.length];
        for (int i=0;i<IC.length;i++) {
            newY[i]=     IC[i]+timeStep*(c[0]*k0[i]      +c[1]*k1[i]     +c[2]*k2[i]     +c[3]*k3[i]     +c[4]*k4[i]     +c[5]*k5[i]);
            newY[i]=Math.max(0,newY[i]);
        }
        
        newYstar=new double[IC.length];
        for (int i=0;i<IC.length;i++) {
            newYstar[i]= IC[i]+timeStep*(cStar[0]*k0[i]  +cStar[1]*k1[i] +cStar[2]*k2[i] +cStar[3]*k3[i] +cStar[4]*k4[i] +cStar[5]*k5[i]);
            newYstar[i]=Math.max(0,newYstar[i]);
        }
                
        Delta=0;
        for (int i=0;i<IC.length;i++) {
            if ((newY[i]+newYstar[i]) > 0)
                Delta=Math.max(Delta,Math.abs(2*(newY[i]-newYstar[i])/(newY[i]+newYstar[i])));
        }
        
        newTimeStep=timeStep;

        if (finalize){
            return new double[][] {{newTimeStep},newY};
        }
        else {
            if (Delta != 0.0){
                factor=epsilon/Delta;

                if (factor >= 1)
                    newTimeStep=timeStep*Math.pow(factor,0.15);
                else
                    newTimeStep=timeStep*Math.pow(factor,0.25);
            } else{
                factor=1e8;
                newTimeStep=timeStep*Math.pow(factor,0.15);
                finalize=true;
            }
            
            //System.out.println("    --> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");
        
            return step(currentTime, IC , newTimeStep,true);
        }

    }
    
    public double[][][] simpleRun(double iniTime, double finalTime, double[] IC){
        
        double currentTime=iniTime;
        
        java.util.Vector corrida=new java.util.Vector();
        corrida.addElement(new double[][] {{iniTime},IC});
        double[][] givenStep;
        while(currentTime < finalTime){
            givenStep=step(currentTime, IC , basicTimeStep,false);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];
            
            corrida.addElement(givenStep);
            
            if (givenStep[0][0]+basicTimeStep > finalTime) break;
        }
        
        givenStep=step(currentTime, IC , (finalTime-currentTime),true);
        basicTimeStep=givenStep[0][0];
        currentTime+=basicTimeStep;
        givenStep[0][0]=currentTime;
        IC=givenStep[1];
        corrida.addElement(givenStep);
        
        double[][][] runOutput=new double[corrida.size()][][];
        for (int i=0;i<runOutput.length;i++){
            runOutput[i]=(double[][]) corrida.elementAt(i);
        }
        
        
        return runOutput;
    }
    
    public double[][][] jumpsRun(double iniTime, double finalTime, double incrementalTime, double[] IC){
        
        double currentTime=iniTime,targetTime;
        
        java.util.Vector corrida=new java.util.Vector();
        corrida.addElement(new double[][] {{iniTime},IC});
        double[][] givenStep;
        
        while(currentTime < finalTime){
            targetTime=currentTime+incrementalTime;
            while(currentTime < targetTime){
                givenStep=step(currentTime, IC , basicTimeStep,false);
                
                if (currentTime+givenStep[0][0] > targetTime) break;

                basicTimeStep=givenStep[0][0];
                currentTime+=basicTimeStep;
                givenStep[0][0]=currentTime;
                IC=givenStep[1];
            }
            
            givenStep=step(currentTime, IC , targetTime-currentTime,true);
            
            if (currentTime+givenStep[0][0] > finalTime) break;

            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            corrida.addElement(givenStep);

        }
        
        if (currentTime != finalTime){
            givenStep=step(currentTime, IC , finalTime-currentTime,true);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            corrida.addElement(givenStep);
        }

        double[][][] runOutput=new double[corrida.size()][][];
        for (int i=0;i<runOutput.length;i++){
            runOutput[i]=(double[][]) corrida.elementAt(i);
        }
        
        
        return runOutput;
    }
    
    public void simpleRunToFile(double iniTime, double finalTime, double[] IC, java.io.DataOutputStream outputStream) throws java.io.IOException {
        
        double currentTime=iniTime;
        
        outputStream.writeDouble(currentTime);
        for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);
       
        double[][] givenStep;
        
        while(currentTime < finalTime){
            givenStep=step(currentTime, IC , basicTimeStep,false);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];
            
            java.util.Calendar thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");

            outputStream.writeDouble(currentTime);
            for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);
            
            if (givenStep[0][0]+basicTimeStep > finalTime) break;
        }
        
        if (currentTime != finalTime){
            givenStep=step(currentTime, IC , finalTime-currentTime,true);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            outputStream.writeDouble(currentTime);
            for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);
        }
        
        finalCond=IC;

    }
    
    public void jumpsRunToFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.DataOutputStream outputStream) throws java.io.IOException {
        
        outputStream.writeInt((int) Math.round((finalTime-iniTime)/incrementalTime)+1);
        System.out.println(((int) Math.round((finalTime-iniTime)/incrementalTime)+1));
        double currentTime=iniTime,targetTime;
        
        outputStream.writeDouble(currentTime);
        for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
        System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();
       
        double[][] givenStep;
        
        while(currentTime < finalTime){
            targetTime=currentTime+incrementalTime;
            while(currentTime < targetTime){
                
                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep=step(currentTime, IC , basicTimeStep,false);
                
                if (currentTime+givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep=givenStep[0][0];
                currentTime+=basicTimeStep;
                givenStep[0][0]=currentTime;
                IC=givenStep[1];
                
                
            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if(targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep=step(currentTime, IC , targetTime-currentTime,true);
            
            if (currentTime+givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];
            
            outputStream.writeDouble(currentTime);
            for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);
            
            thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
            
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        if (currentTime != finalTime){
            givenStep=step(currentTime, IC , finalTime-currentTime-1/60.,true);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            outputStream.writeDouble(currentTime);
            for(int j=0;j<IC.length;j++) outputStream.writeDouble(IC[j]);

            thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        finalCond=IC;

    }
    
    public void jumpsRunToAsciiFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {
        
        double currentTime=iniTime,targetTime;
        
        int ouletID=linksStructure.getOutletID();
        
        outputStream.write("\n");
        outputStream.write(currentTime+",");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
        System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();
       
        double[][] givenStep;
        
        while(currentTime < finalTime){
            targetTime=currentTime+incrementalTime;
            while(currentTime < targetTime){
                
                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep=step(currentTime, IC , basicTimeStep,false);
                
                if (currentTime+givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep=givenStep[0][0];
                currentTime+=basicTimeStep;
                givenStep[0][0]=currentTime;
                IC=givenStep[1];
                
                
            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if(targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep=step(currentTime, IC , targetTime-currentTime,true);
            
            if (currentTime+givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }
            
            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];
            
            outputStream.write("\n");
            outputStream.write(currentTime+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
            }
            
            thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
            
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        if (currentTime != finalTime && IC[ouletID] > 1e-3){
            givenStep=step(currentTime, IC , finalTime-currentTime-1/60.,true);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
            }

            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");*/
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        finalCond=IC;

    }
    
    public void jumpsRunToIncompleteAsciiFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {
        
        double currentTime=iniTime,targetTime;
        
        int basinOrder=linksStructure.getBasinOrder();
        int ouletID=linksStructure.getOutletID();
        
        outputStream.write("\n");
        outputStream.write(currentTime+",");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
        System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();
       
        double[][] givenStep;
        
        while(currentTime < finalTime){
            targetTime=currentTime+incrementalTime;
            while(currentTime < targetTime){
                
                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep=step(currentTime, IC , basicTimeStep,false);
                
                if (currentTime+givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep=givenStep[0][0];
                currentTime+=basicTimeStep;
                givenStep[0][0]=currentTime;
                IC=givenStep[1];
            }
            
            double typicalStepSize=basicTimeStep;
            
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if(targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep=step(currentTime, IC , targetTime-currentTime,true);
            
            if (currentTime+givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }
            
            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];
            
            outputStream.write("\n");
            outputStream.write(currentTime+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
            }
            
            thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+") - Tipical Time Step: "+typicalStepSize);
            
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        if (currentTime != finalTime && IC[ouletID] > 1e-3){
            givenStep=step(currentTime, IC , finalTime-currentTime-1/60.,true);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            IC=givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]]+",");
            }

            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");*/
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        
        finalCond=IC;

    }
    
    public void setBasicTimeStep(double newBTS){
        basicTimeStep=newBTS;
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        hydroScalingAPI.util.ordDiffEqSolver.Lorenz funcionLorenz;
        double[][][] answer;
        
        funcionLorenz=new hydroScalingAPI.util.ordDiffEqSolver.Lorenz(16.0f,45.0f,4.0f);
        //double[][] answer1=new RKF(funcionLorenz, 1e-6, .001).step(0.0,new double[] {-13,-12, 52},.001,false);
        //System.out.print("Time: "+answer1[0][0]+" Evaluation: ");
        //for(int j=0;j<answer1[1].length;j++) System.out.print(answer1[1][j]+" ");
        //System.exit(0);
        
        System.out.println("starts running");
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time: "+startTime.toString());
        answer=new RKF(funcionLorenz, 1e-4, .001).jumpsRun(0,1000,0.2,new double[] {-13,-12, 52});
        //answer=new RKF(funcionLorenz, 1e-4, .001).simpleRun(0,10000,new double[] {-13,-12, 52});
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        /*System.out.println(answer.length);
       System.exit(0);
        for (int i=0;i<answer.length;i++){
            System.out.print("Time: "+answer[i][0][0]+" Evaluation: ");
            for(int j=0;j<answer[i][1].length;j++) System.out.print(answer[i][1][j]+" ");
            System.out.println("");
        }*/
        
        
    }

}

