/*
 * Regressions.java
 *
 * Created on March 13, 2007, 10:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.util.statistics;

/**
 * An abstract class to perform regression analysis between two varibles
 * @author Ricardo Mantilla
 */
public abstract class Regressions {
    
    public static java.util.Hashtable LinearRegression(float[] Xs, float[] Ys) {
        double[] XsD=new double[Xs.length];
        double[] YsD=new double[Ys.length];
        for (int i = 0; i < Xs.length; i++) {
            XsD[i]=(double)Xs[i];
            YsD[i]=(double)Ys[i];
        }
        return LinearRegression(XsD, YsD);
    }
    
    public static java.util.Hashtable LinearRegression(double[] Xs, double[] Ys) {
        
        double[][] depend= {Ys};
        
        Jama.Matrix O = new Jama.Matrix(depend);
        
        double[][] indep = new double[2][];
        indep[0]=new double[Ys.length];
        java.util.Arrays.fill(indep[0],1);
        indep[1]=Xs;
        
        Jama.Matrix AT = new Jama.Matrix(indep);
        Jama.Matrix A = AT.transpose();
        Jama.Matrix B = A.transpose();
        Jama.Matrix C = B.times(A);
        double[][] array=C.getArrayCopy();
        
        Jama.Matrix D = C.inverse();
        Jama.Matrix E = D.times(B).times(O.transpose());
        
        double[][] coeffArray=E.getArrayCopy();
        
        java.util.Hashtable paramsTable=new java.util.Hashtable();
        
        paramsTable.put("intercept",coeffArray[0][0]);
        paramsTable.put("slope",coeffArray[1][0]);
        
        return paramsTable;
    }
    
    public static java.util.Hashtable LinearRegressionCrossAtZero(float[] Xs, float[] Ys) {
        double[] XsD=new double[Xs.length];
        double[] YsD=new double[Ys.length];
        for (int i = 0; i < Xs.length; i++) {
            XsD[i]=(double)Xs[i];
            YsD[i]=(double)Ys[i];
        }
        return LinearRegressionCrossAtZero(XsD, YsD);
    }
    
    public static java.util.Hashtable LinearRegressionCrossAtZero(double[] Xs, double[] Ys) {
        
        double[][] depend= {Ys};
        
        Jama.Matrix O = new Jama.Matrix(depend);
        
        double[][] indep= {Xs};
        
        Jama.Matrix AT = new Jama.Matrix(indep);
        Jama.Matrix A = AT.transpose();
        Jama.Matrix B = A.transpose();
        Jama.Matrix C = B.times(A);
        double[][] array=C.getArrayCopy();
        
        Jama.Matrix D = C.inverse();
        Jama.Matrix E = D.times(B).times(O.transpose());
        
        double[][] coeffArray=E.getArrayCopy();
        
        java.util.Hashtable paramsTable=new java.util.Hashtable();
        
        paramsTable.put("slope",coeffArray[0][0]);
        
        return paramsTable;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
