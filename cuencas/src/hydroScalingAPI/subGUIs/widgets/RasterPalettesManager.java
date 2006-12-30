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


package hydroScalingAPI.subGUIs.widgets;

import visad.*;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;

/**
 *
 * @author Ricardo Mantilla
 */
public class RasterPalettesManager extends javax.swing.JPanel {
    
    private visad.util.LabeledColorWidget myLabeledColorWidget;
    private ScalarMap varMap;
    
    private java.util.Hashtable colorTables=new java.util.Hashtable();
    private boolean first=true;
    
    /** Creates new form colorManager */
    public RasterPalettesManager(ScalarMap theMap) throws RemoteException, VisADException{
        varMap=theMap;
        initComponents();
        
        myLabeledColorWidget=new visad.util.LabeledColorWidget(varMap);
        add("Center",myLabeledColorWidget);
        
        try{
            java.io.BufferedReader fr = new java.io.BufferedReader(new java.io.InputStreamReader(getClass().getResourceAsStream("/hydroScalingAPI/subGUIs/configuration/colorTables.txt")));
            
            String fullLine=fr.readLine();
            
            while (fullLine != null){
                java.util.StringTokenizer tokens=new java.util.StringTokenizer(fullLine,":");
                String tableName=tokens.nextToken();
                int magnTabla=Integer.parseInt(tokens.nextToken().trim());
                float[][] estaTabla=new float[3][magnTabla];
                java.util.StringTokenizer tokensR=new java.util.StringTokenizer(fr.readLine());
                java.util.StringTokenizer tokensG=new java.util.StringTokenizer(fr.readLine());
                java.util.StringTokenizer tokensB=new java.util.StringTokenizer(fr.readLine());
                
                for (int i=0;i<magnTabla;i++){
                    estaTabla[0][i]=Float.parseFloat(tokensR.nextToken());
                    estaTabla[1][i]=Float.parseFloat(tokensG.nextToken());
                    estaTabla[2][i]=Float.parseFloat(tokensB.nextToken());
                }
                fr.readLine();

                colorTables.put(tableName,estaTabla);
                colorTablesOptions.addItem(tableName);
                fullLine=fr.readLine();
            }
            fr.close();
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        }        
    }
    
    public void setSelectedTable(String name){
        try{
            ColorControl control = (ColorControl) varMap.getControl();
            control.setTable((float[][])colorTables.get(name));
        } catch (RemoteException RME){
            System.err.println(RME);
        } catch (VisADException VisEX){
            System.err.println(VisEX);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        colorTablesOptions = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        colorTablesOptions.setFont(new java.awt.Font("Dialog", 0, 10));
        colorTablesOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorTablesOptionsActionPerformed(evt);
            }
        });

        add(colorTablesOptions, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void colorTablesOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorTablesOptionsActionPerformed
        if(first) {
            first=false;
            return;
        }
        setSelectedTable((String)colorTablesOptions.getSelectedItem());
    }//GEN-LAST:event_colorTablesOptionsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox colorTablesOptions;
    // End of variables declaration//GEN-END:variables
    
}
