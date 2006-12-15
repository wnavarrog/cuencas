package hydroScalingAPI;

/*
 * Cuencas.java
 *
 * Created on March 3, 2003, 4:52 PM
 */

/**
 * This class is the typical entrance point to the CUENCAS GUI.  It launches a splash screen and initializes some libraries. It is designed to test for the presence of Java3D.
 * 
 * It can be ommited and initilize the GUI using {@link hydroScalingAPI.mainGUI.ParentGUI}
 * @author Ricardo Mantilla
 */
public class Cuencas {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager myInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager();
        hydroScalingAPI.mainGUI.ParentGUI mainGUI=new hydroScalingAPI.mainGUI.ParentGUI(myInfoManager);
        hydroScalingAPI.mainGUI.widgets.Splash theSP = new hydroScalingAPI.mainGUI.widgets.Splash(myInfoManager.dataBaseLogoPath);
        theSP.setVisible(true);
        theSP.animate();
        theSP.dispose();
        mainGUI.setVisible(true);
    }
    
}
