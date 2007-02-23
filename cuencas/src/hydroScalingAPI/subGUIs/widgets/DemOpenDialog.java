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
 * mdtGeomorCheck.java
 *
 * Created on March 3, 2003, 4:31 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author  Ricardo Mantilla
 */
public class DemOpenDialog extends javax.swing.JDialog {
    
    private int[] selectedSubMap;
    private String demPath;
    private String demName;
    
    private hydroScalingAPI.mainGUI.ParentGUI myParent;
    private hydroScalingAPI.io.MetaRaster metaData;
    private hydroScalingAPI.io.MetaRaster[] listOfMetaRasters;
    private java.util.Vector elemDeriv;
    
    private String[] units={ "",
                            "",
                            "*",
                            "km^2",
                            "*",
                            "km",
                            "km",
                            "km",
                            "*",
                            "",
                            "",
                            "*",
                            "*",
                            "# of links",
                            "*"};
    
    private String[] extension={  ".dem",
                                  ".corrDEM",
                                  ".dir",
                                  ".areas",
                                  ".horton",
                                  ".lcp",
                                  ".ltc",
                                  ".gdo",
                                  ".tdo",
                                  ".mcd",
                                  ".tcd",
                                  ".magn",
                                  ".slope",
                                  ".dtopo",
                                  ".redRas"};
                                  
    private String[] derivedName={     " Digital Elevation Model",
                                      " Fixed Elevation Model",
                                      " Drainage Directions",
                                      " Accumulated Area",
                                      " Horton Numbers",
                                      " Longest Channel Length",
                                      " Total Channels Length",
                                      " Geometric Distance to Border",
                                      " Topologic Distance to Border",
                                      " Maximum Channel Drop",
                                      " Total Channels Drop",
                                      " Magnitude",
                                      " Gradient Value",
                                      " Topologic Diameter",
                                      " Raster Drainage Network"};
    private String[] extraInfo={   "",
                                  " Fixed Digital Elevation Model - ",
                                  " Drainage Directions - ",
                                  " Accumulated Area - ",
                                  " Horton Numbers - ",
                                  " Longest Channel Length - ",
                                  " Total Channels Length - ",
                                  " Geometric Distance to Border - ",
                                  " Topologic Distance to Border - ",
                                  " Maximum Channel Drop - ",
                                  " Total Channels Drop - ",
                                  " Magnitude - ",
                                  " Gradient Value - ",
                                  " Topologic Diameter - ",
                                  " Raster Drainage Network - "};
        
    public DemOpenDialog(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md) {
        this(parent,md,"Open");
    }
    /** Creates new form mdtGeomorCheck */
    public DemOpenDialog(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md,String actionToDo) {
        super (parent, true);
        initComponents ();
        pack ();
        
        myParent=parent;
        metaData=md;
        
        open.setText(actionToDo);
        if(!actionToDo.equalsIgnoreCase("Open")){
            launch.setEnabled(false);
            jPanel3.removeAll();
            listDEM_subproducts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        }
        
        extraInfo[0]=metaData.getProperty("[Information]");
        units[0]=(units[1]=metaData.getProperty("[Units]"));
        units[9]=(units[10]=units[0]);
        
        for (int i=1;i<extraInfo.length;i++) extraInfo[i]=extraInfo[i]+metaData.getProperty("[Name]");
        
        setBounds(0,0, 340, 200);
        java.awt.Rectangle marcoParent=parent.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        
        elemDeriv=new java.util.Vector();
        elemDeriv.add(derivedName[0]);
        
        demPath=metaData.getLocationMeta().getParent();
        demName=metaData.getLocationMeta().getName();
        demName=demName.substring(0,demName.lastIndexOf(".metaDEM"));
        
        
        for (int i=1;i<derivedName.length;i++){
            if (new java.io.File(demPath+"/"+demName+extension[i]).exists()) {
                elemDeriv.add(derivedName[i]);
            } else {
                elemDeriv.add("** > Map Not Available ("+derivedName[i]+")");
            }
        }

        listDEM_subproducts.setListData(elemDeriv);
        listDEM_subproducts.setSelectedIndex(0);
    }
    
    public boolean is3D(){
        return option3D.isSelected();
    }
    
    public boolean mapsSelected(){
        return listOfMetaRasters != null;
    }
    
    public hydroScalingAPI.io.MetaRaster[] getSelectedMetaRasters(){
        return listOfMetaRasters;
    }
    
    public java.util.Hashtable getRelatedMaps(){
        java.util.Hashtable nameToFile = new java.util.Hashtable();
        
        for (int i=0;i<derivedName.length;i++) nameToFile.put((String)elemDeriv.get(i),demPath+"/"+demName+extension[i]);
        
        return nameToFile;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        listDEM_subproducts = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        open = new javax.swing.JButton();
        launch = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        option2D = new javax.swing.JRadioButton();
        option3D = new javax.swing.JRadioButton();

        setTitle("DEM and Derived Quantities");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        listDEM_subproducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listDEM_subproductsMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(listDEM_subproducts);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(1, 3));

        open.setFont(new java.awt.Font("Dialog", 0, 12));
        open.setText("Open");
        open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openActionPerformed(evt);
            }
        });

        jPanel2.add(open);

        launch.setFont(new java.awt.Font("Dialog", 0, 12));
        launch.setText("Get Network");
        launch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchActionPerformed(evt);
            }
        });

        jPanel2.add(launch);

        cancel.setFont(new java.awt.Font("Dialog", 0, 12));
        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        jPanel2.add(cancel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        option2D.setSelected(true);
        option2D.setText("View 2D");
        buttonGroup1.add(option2D);
        jPanel3.add(option2D);

        option3D.setText("View 3D");
        buttonGroup1.add(option3D);
        jPanel3.add(option3D);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents

    private void launchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchActionPerformed
         try{
            closeDialog(null);
            metaData.setLocationBinaryFile(new java.io.File(demPath+"/"+demName+extension[0]));
            hydroScalingAPI.io.DataRaster dataDEM = new hydroScalingAPI.io.DataRaster(metaData);
            new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(myParent, metaData, dataDEM);
            myParent.openDEM(metaData.getLocationMeta());
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
    }//GEN-LAST:event_launchActionPerformed

  private void listDEM_subproductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listDEM_subproductsMouseClicked

    if (evt.getClickCount() == 2) {
        openActionPerformed(null);
    }
  }//GEN-LAST:event_listDEM_subproductsMouseClicked

  private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
    
    selectedSubMap=null;
    setVisible (false);
    dispose ();
    
  }//GEN-LAST:event_cancelActionPerformed

  private void openActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openActionPerformed

    selectedSubMap=listDEM_subproducts.getSelectedIndices();
    listOfMetaRasters=new hydroScalingAPI.io.MetaRaster[selectedSubMap.length];
    
    for (int i=0;i<selectedSubMap.length;i++){
        try{
            listOfMetaRasters[i]=new hydroScalingAPI.io.MetaRaster(metaData.getLocationMeta());
            listOfMetaRasters[i].setName(derivedName[selectedSubMap[i]]);
            listOfMetaRasters[i].setLocationBinaryFile(new java.io.File(demPath+"/"+demName+extension[selectedSubMap[i]]));
            if(extension[selectedSubMap[i]].equalsIgnoreCase(".dem"))
                listOfMetaRasters[i].setFormat(metaData.getFormat());
            else
                listOfMetaRasters[i].setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(extension[selectedSubMap[i]]));
            listOfMetaRasters[i].setUnits(units[selectedSubMap[i]]);
            listOfMetaRasters[i].setInformation(extraInfo[selectedSubMap[i]]);
        }catch(java.io.IOException IOE){
            System.err.println("At openActionPerformed");
            System.err.println(IOE);
        }
    }
    
    setVisible (false);
    dispose ();
    
  }//GEN-LAST:event_openActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton option2D;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton open;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton cancel;
    private javax.swing.JRadioButton option3D;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JList listDEM_subproducts;
    private javax.swing.JButton launch;
    // End of variables declaration//GEN-END:variables

}
