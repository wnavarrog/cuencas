/*
 * ImportLocationImage.java
 *
 * Created on July 12, 2003, 10:44 AM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author  ricardo
 */
public class ImportLocationImage extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private boolean fileSelected=true;
    
    private java.io.File pathToLookFor;
    
    /** Creates new form ImportLocationImage */
    public ImportLocationImage(hydroScalingAPI.mainGUI.ParentGUI parent, java.io.File path) {
        super(parent, true);
        mainFrame=parent;
        pathToLookFor=path;
        initComponents();
        
        setBounds(0,0,400,120);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
    }
    
    public String getFilePath(){
        return fileNameField.getText();
    }
    
    public String getFileName(){
        String name=fileNameField.getText();
        if (name.indexOf("http://") != -1) return name;
        return "images/"+(new java.io.File(name).getName());
    }
    
    public String getDescription(){
        return descriptionField.getText();
    }
    
    public boolean fileSelected(){
        return fileSelected;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fileNameField = new javax.swing.JTextField();
        selectFile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        importButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(3, 0));

        setTitle("Import Dialog");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel1.setText("File Name / URL : ");
        jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

        fileNameField.setFont(new java.awt.Font("Dialog", 0, 10));
        fileNameField.setText("http://");
        jPanel1.add(fileNameField, java.awt.BorderLayout.CENTER);

        selectFile.setFont(new java.awt.Font("Dialog", 0, 10));
        selectFile.setText(" ... ");
        selectFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFileActionPerformed(evt);
            }
        });

        jPanel1.add(selectFile, java.awt.BorderLayout.EAST);

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText("Image Description : ");
        jPanel2.add(jLabel2, java.awt.BorderLayout.WEST);

        descriptionField.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel2.add(descriptionField, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2);

        importButton.setFont(new java.awt.Font("Dialog", 0, 10));
        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        jPanel3.add(importButton);

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 10));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel3.add(cancelButton);

        getContentPane().add(jPanel3);

        pack();
    }//GEN-END:initComponents

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        fileSelected=true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_importButtonActionPerformed

    private void selectFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFileActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser(pathToLookFor);
        fc.setFileSelectionMode(fc.FILES_ONLY);
        fc.setDialogTitle("Image Selection");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("jpg","JPEG Image");
        fc.addChoosableFileFilter(mdtFilter);
        fc.showOpenDialog(this);

        if (fc.getSelectedFile() == null) return;
        if (!fc.getSelectedFile().isFile()) return;

        fileNameField.setText(fc.getSelectedFile().getPath());
    }//GEN-LAST:event_selectFileActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        fileSelected=false;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        new ImportLocationImage(tempFrame,new java.io.File("/")).setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton selectFile;
    // End of variables declaration//GEN-END:variables
    
}
