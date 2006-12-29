/*
 * attractorPlot.java
 *
 * Created on August 9, 2001, 4:16 PM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

/**
 *
 * @author Ricardo Mantilla
 * @version 
 */
public class attractorPlot extends java.lang.Object {

    /** Creates new attractorPlot */
    public attractorPlot() throws RemoteException, VisADException {
        //float[][] answer=new RK4(new uposearch.Lorenz(16.0f,45.0f,4.0f), new float[] {-13,-12, 52}, 0.005f, 0.0f).run(5000);
        float[][] answer=new RK4(new hydroScalingAPI.util.ordDiffEqSolver.Rossler(0.398f,2.0f,4.0f), new float[] {0,1, 1}, 0.05f, 0.0f).run(50000);
        
        RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude,RealType.Altitude);
        
        DisplayImplJ3D display = new DisplayImplJ3D("display3D");
        GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);

        ScalarMap lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        ScalarMap latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        ScalarMap altMap = new ScalarMap( RealType.Altitude, Display.ZAxis );

        display.addMap( latMap );
        display.addMap( lonMap );
        display.addMap( altMap );

        Gridded3DSet elemVectorial=new Gridded3DSet(campo,answer,answer[0].length);
        
        DataReferenceImpl data_ref = new DataReferenceImpl("data_ref3D");
        data_ref.setData(elemVectorial);

        //Agrego una referencia a los datos
        display.addReference( data_ref );
        
        javax.swing.JFrame jframe = new javax.swing.JFrame("Lorenz Attractor");
        jframe.getContentPane().add(display.getComponent());


        // Set window size and make it visible

        jframe.setSize(500, 500);
        jframe.setVisible(true);
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws RemoteException, VisADException {
        
        new attractorPlot();
        
    }

}
