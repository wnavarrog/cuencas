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
 * NetworkTools.java
 *
 * Created on August 11, 2003, 2:34 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 * A GUI to access
 * @author Ricardo Mantilla
 */
public class NetworkTools extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private int xOut,yOut;
    
    /**
     * Creates new form NetworkTools
     * @param parent The main GIS GUI
     * @param x The column number of the basin outlet location
     * @param y The row number of the basin outlet location
     * @param direcc The direction matrix associated to the DEM where the basin is embeded
     * @param md The MetaRaster associated to the DEM where the basin is embeded
     */
    public NetworkTools(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, hydroScalingAPI.io.MetaRaster md) {
        super(parent, true);
        mainFrame=parent;
        metaDatos=md;
        matDir=direcc;
        xOut=x;
        yOut=y;
        initComponents();
        
        setBounds(0,0, 250, 4*40);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        GeomrphAnalysis = new javax.swing.JButton();
        RunoffModel = new javax.swing.JButton();
        BasinMask = new javax.swing.JButton();
        BasinDivideToPoly = new javax.swing.JButton();
        TribsInput = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(5, 0));

        setTitle("Available Network Tools");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        GeomrphAnalysis.setFont(new java.awt.Font("Dialog", 0, 10));
        GeomrphAnalysis.setText("Geomorphology Analysis");
        GeomrphAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GeomrphAnalysisActionPerformed(evt);
            }
        });

        getContentPane().add(GeomrphAnalysis);

        RunoffModel.setFont(new java.awt.Font("Dialog", 0, 10));
        RunoffModel.setText("Rainfall - Runoff Model");
        RunoffModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunoffModelActionPerformed(evt);
            }
        });

        getContentPane().add(RunoffModel);

        BasinMask.setFont(new java.awt.Font("Dialog", 0, 10));
        BasinMask.setText("Create Basin Mask File");
        BasinMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BasinMaskActionPerformed(evt);
            }
        });

        getContentPane().add(BasinMask);

        BasinDivideToPoly.setFont(new java.awt.Font("Dialog", 0, 10));
        BasinDivideToPoly.setText("Save Divide as Polygon File");
        BasinDivideToPoly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BasinDivideToPolyActionPerformed(evt);
            }
        });

        getContentPane().add(BasinDivideToPoly);

        TribsInput.setFont(new java.awt.Font("Dialog", 0, 10));
        TribsInput.setText("tRIBS I/O");
        TribsInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TribsInputActionPerformed(evt);
            }
        });

        getContentPane().add(TribsInput);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BasinMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BasinMaskActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
            fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
            fc.setDialogTitle("Directory Selection");
            fc.showOpenDialog(this);

            if (fc.getSelectedFile() == null) return;
        
            hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOut,yOut,matDir,metaDatos);
            hydroScalingAPI.io.MetaRaster maskMR=new hydroScalingAPI.io.MetaRaster(metaDatos);
            java.io.File saveFile1,saveFile2;
            saveFile1=new java.io.File(fc.getSelectedFile().getPath()+"/"+metaDatos.getLocationMeta().getName().substring(0,metaDatos.getLocationMeta().getName().lastIndexOf("."))+"_BasinMask.metaVHC");
            saveFile2=new java.io.File(fc.getSelectedFile().getPath()+"/"+metaDatos.getLocationMeta().getName().substring(0,metaDatos.getLocationMeta().getName().lastIndexOf("."))+"_BasinMask.vhc");
            
            maskMR.setLocationMeta(saveFile1);
            maskMR.setLocationBinaryFile(saveFile2);
            maskMR.setFormat("Byte");
            maskMR.writeMetaRaster(maskMR.getLocationMeta());

            byte[][] BasMask=myCuenca.getBasinMask();
            
            java.io.DataOutputStream writer;
            writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile2)));

            for(int i=0;i<BasMask.length;i++) for(int j=0;j<BasMask[0].length;j++){
                writer.writeByte(BasMask[i][j]);
            }

            writer.close();
            
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed creating mask file for this basin. "+xOut+" "+yOut);
            System.err.println(IOE);
        }
    }//GEN-LAST:event_BasinMaskActionPerformed

    private void TribsInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TribsInputActionPerformed
        try{
            java.io.File theFile=metaDatos.getLocationBinaryFile();
            metaDatos.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaDatos.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaDatos).getInt();
            
            new hydroScalingAPI.modules.tRIBS_io.widgets.TRIBS_io(mainFrame,xOut,yOut,matDir,magnitudes,metaDatos).setVisible(true);
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed loading Rainfall_Runoff_Model. "+xOut+" "+yOut);
            System.err.println(IOE);
        } catch (visad.VisADException v){
            System.err.println("Failed loading Rainfall_Runoff_Model. "+xOut+" "+yOut);
            System.err.println(v);
        }
        
        
    }//GEN-LAST:event_TribsInputActionPerformed

    private void BasinDivideToPolyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BasinDivideToPolyActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBasePolygonsPath);
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Directory Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("poly","Polygon File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);

            if (fc.getSelectedFile() == null) return;
        
            hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOut,yOut,matDir,metaDatos);
            hydroScalingAPI.io.MetaPolygon metaPolyToWrite=new hydroScalingAPI.io.MetaPolygon ();
            metaPolyToWrite.setName(myCuenca.toString());
            metaPolyToWrite.setCoordinates(myCuenca.getLonLatBasinDivide());
            metaPolyToWrite.setInformation("Basin Divide as captured by Cuencas");
            metaPolyToWrite.writePolygon(fc.getSelectedFile());
            
            mainFrame.setUpGUI(true);
            
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed creating polygon file for this basin. "+xOut+" "+yOut);
            System.err.println(IOE);
        }
    }//GEN-LAST:event_BasinDivideToPolyActionPerformed

    private void RunoffModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunoffModelActionPerformed
        try{
            java.io.File theFile=metaDatos.getLocationBinaryFile();
            metaDatos.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaDatos.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaDatos).getInt();
            
            new hydroScalingAPI.modules.rainfallRunoffModel.widgets.Rainfall_Runoff_Model(mainFrame,xOut,yOut,matDir,magnitudes,metaDatos).setVisible(true);
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed loading Rainfall_Runoff_Model. "+xOut+" "+yOut);
            System.err.println(IOE);
        } catch (visad.VisADException v){
            System.err.println("Failed loading Rainfall_Runoff_Model. "+xOut+" "+yOut);
            System.err.println(v);
        }
    }//GEN-LAST:event_RunoffModelActionPerformed

    private void GeomrphAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GeomrphAnalysisActionPerformed
        try{
            new hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer (mainFrame,xOut,yOut,matDir,metaDatos).setVisible(true);
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed loading BasinAnalyzer. "+xOut+" "+yOut);
            System.err.println(IOE);
        } catch (visad.VisADException v){
            System.err.println("Failed loading BasinAnalyzer. "+xOut+" "+yOut);
            System.err.println(v);
        }
    }//GEN-LAST:event_GeomrphAnalysisActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3,96,matDirs,metaModif);

            new NetworkTools(tempFrame,3,96,matDirs,metaModif).setVisible(true);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }   
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BasinDivideToPoly;
    private javax.swing.JButton BasinMask;
    private javax.swing.JButton GeomrphAnalysis;
    private javax.swing.JButton RunoffModel;
    private javax.swing.JButton TribsInput;
    // End of variables declaration//GEN-END:variables
    
}
