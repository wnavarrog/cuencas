/*
 * Operations.java
 *
 * Created on March 13, 2007, 8:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.matrix;

import visad.matrix.JamaCholeskyDecomposition;

/**
 *
 * @author Ricardo Mantilla
 */
public class Operations {
    
    /** Creates a new instance of Operations */
    public Operations() {
        
        double[] Xs=new double[200];
        double[] Ys=new double[200];
        for (int i = 0; i < Xs.length; i++) {
            Xs[i]=Math.random();
            Ys[i]=Xs[i]*10+6;
        }
        
        
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
        
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j]+" ");
            }
            System.out.println();
        }
        Jama.Matrix D = C.inverse();
        Jama.Matrix E = D.times(B).times(O.transpose());
        
        array=E.getArrayCopy();
        
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(i+" "+j+" "+array[i][j]+" ");
            }
            System.out.println();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Operations();
    }
    
}
