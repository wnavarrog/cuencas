/*
 * GaugesGUI_monitor.java
 *
 * Created on June 18, 2003, 5:05 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author  ricardo
 */
public class GaugesGUI_monitor extends Thread{
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.mainGUI.objects.GaugesManager dialogManager;
    
    
    /** Creates new form GaugesOpenDialog */
    public GaugesGUI_monitor(hydroScalingAPI.mainGUI.ParentGUI parent) {
        
        mainFrame=parent;
        dialogManager=mainFrame.getGaugesManager();
        
    }
    
    public void run() {
        
        while(!dialogManager.isLoaded()){
            new visad.util.Delay(500);
        }
        
        mainFrame.GaugesPanelEnabled(true);
        return;
    }
    
}
