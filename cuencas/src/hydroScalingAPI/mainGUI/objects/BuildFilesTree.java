/*
 * buildFilesTree.java
 *
 * Created on March 4, 2003, 5:11 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class BuildFilesTree extends java.lang.Thread{
    
    private javax.swing.tree.DefaultMutableTreeNode mainNode;
    private java.io.FileFilter treeFilter;
    private java.io.File rootFile;
    
    /** Creates a new instance of buildFilesTree */
    public BuildFilesTree(javax.swing.tree.DefaultMutableTreeNode topNode, java.io.File rf, java.io.FileFilter filter) {
        mainNode=topNode;
        treeFilter=filter;
        rootFile=rf;
    }
    
    public void treeDigger(java.io.File fileToDig, javax.swing.tree.DefaultMutableTreeNode upperNode){
        
        java.io.File[] dirsToDig=fileToDig.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
        java.util.Arrays.sort(dirsToDig);
        for (int i=0;i<dirsToDig.length;i++){
            javax.swing.tree.DefaultMutableTreeNode newNode=new javax.swing.tree.DefaultMutableTreeNode(dirsToDig[i].getName());
            upperNode.add(newNode);
            treeDigger(dirsToDig[i],newNode);
        }
        java.io.File[] filesToAdd=fileToDig.listFiles(treeFilter);
        for (int i=0;i<filesToAdd.length;i++){
            upperNode.add(new javax.swing.tree.DefaultMutableTreeNode(filesToAdd[i].getName()));
        }
    }
    
    public void run(){
        treeDigger(rootFile,mainNode);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager myInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager();

        javax.swing.tree.DefaultMutableTreeNode topoTreeModel=new javax.swing.tree.DefaultMutableTreeNode("Elevation Maps");
        new hydroScalingAPI.mainGUI.objects.BuildFilesTree(topoTreeModel,myInfoManager.dataBaseRastersDemPath,new hydroScalingAPI.util.fileUtilities.DotFilter("metaDEM")).start();
        
        javax.swing.tree.DefaultMutableTreeNode hydroTreeModel=new javax.swing.tree.DefaultMutableTreeNode("Hydrological Fields");
        new hydroScalingAPI.mainGUI.objects.BuildFilesTree(hydroTreeModel,myInfoManager.dataBaseRastersHydPath,new hydroScalingAPI.util.fileUtilities.DotFilter("metaHVC")).start();

    }    
}
