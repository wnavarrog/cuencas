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
 * RK4step.java
 *
 * Created on June 3, 2001, 4:50 PM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 *
 * @author  Ricardo Mantilla 
 */
public class RK4 {
    
    hydroScalingAPI.util.ordDiffEqSolver.BasicFunction theFunction;
    float[] initialCond;
    float stepSize;
    float time;
    
    /** Creates new RK4step */
    public RK4(hydroScalingAPI.util.ordDiffEqSolver.BasicFunction fu, float[] IC, float h, float t) {
        
        theFunction=fu;
        initialCond=IC;
        stepSize=h;
        time=t;
        
    }
    
    public float[] step(){
        float[] evalPoint=new float[initialCond.length];
        float evalTime=0.0f;
        
        evalPoint=(float[]) initialCond.clone();
        evalTime=time;
        float[] k1=theFunction.eval(evalPoint,evalTime);
                
        for (int i=0;i<initialCond.length;i++) evalPoint[i]=initialCond[i]+stepSize*k1[i]/2.0f;
        evalTime=time+stepSize/2.0f;
        float[] k2=theFunction.eval(evalPoint,evalTime);
                 
        for (int i=0;i<initialCond.length;i++) evalPoint[i]=initialCond[i]+stepSize*k2[i]/2.0f;
        evalTime=time+stepSize/2.0f;
        float[] k3=theFunction.eval(evalPoint,evalTime);
                
        for (int i=0;i<initialCond.length;i++) evalPoint[i]=initialCond[i]+stepSize*k3[i];
        evalTime=time+stepSize;
        float[] k4=theFunction.eval(evalPoint,evalTime);
                
        for (int i=0;i<initialCond.length;i++) evalPoint[i]=initialCond[i]+stepSize/6.0f*(k1[i]+2*k2[i]+2*k3[i]+k4[i]);
        
        return evalPoint;
    }
    
    public float[][] run(int nSteps){
        float[][] result=new float[initialCond.length][nSteps];
        float[] real_IC=(float[]) initialCond.clone();
        for (int i=0;i<nSteps;i++){
            float[] salida=step();
            for (int j=0;j<initialCond.length;j++){
                result[j][i]=salida[j];
            }
            initialCond=salida;
        }
        initialCond=real_IC;
        return result;
    }
    
    public static void main (String args[]) {
        hydroScalingAPI.util.ordDiffEqSolver.Lorenz funcionLorenz;
        hydroScalingAPI.util.ordDiffEqSolver.Rossler funcionRossler;
        
        String[] argsv={"lorenz","1000"};
        args=argsv;
        
        int n=new Integer(args[1]).intValue();
        float[][] answer;
        
        if (args[0].equalsIgnoreCase("lorenz")){
            funcionLorenz=new hydroScalingAPI.util.ordDiffEqSolver.Lorenz(16.0f,45.0f,4.0f);
            answer=new RK4(funcionLorenz, new float[] {-13,-12, 52}, 0.005f, 0.0f).run(n);
        } else{
            funcionRossler=new hydroScalingAPI.util.ordDiffEqSolver.Rossler(0.398f,2.0f,4.0f);
            answer=new RK4(funcionRossler, new float[] {-13,-12, 52}, 0.005f, 0.0f).run(n);
        }
        
        
        
        //new TransformBDHSJ("/Applications/HidrosigJava/BaseDeDatosHSJ");
        //0.00500000     -12.9034     -11.4966      51.7236
    }

}
