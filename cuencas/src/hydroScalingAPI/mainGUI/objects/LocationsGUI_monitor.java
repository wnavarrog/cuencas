/*
 * GaugesGUI_monitor.java
 *
 * Created on June 18, 2003, 5:05 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class LocationsGUI_monitor extends Thread{
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.mainGUI.objects.LocationsManager dialogManager;
    
    
    /** Creates new form GaugesOpenDialog */
    public LocationsGUI_monitor(hydroScalingAPI.mainGUI.ParentGUI parent) {
        
        mainFrame=parent;
        dialogManager=mainFrame.getLocationsManager();
        
    }
    
    public void run() {
        
        while(!dialogManager.isLoaded()){
            new visad.util.Delay(500);
        }
        
        mainFrame.LocationsPanelEnabled(true);
        return;
    }
    
}
