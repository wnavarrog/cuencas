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
 * GaugesGUI_monitor.java
 *
 * Created on June 18, 2003, 5:05 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author Ricardo Mantilla
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
