/*
 * Generator.java
 *
 * Created on July 11, 2005, 10:42 AM
 */

package hydroScalingAPI.util.randomSelfSimilarNetworks;

/**
 *
 * @author Ricardo Mantilla
 */
public class Generator {
    
    private String genID;
    private Generator[] subTree;
    
    /** Creates a new instance of Generator */
    public Generator(   int extInt,
                        int generation,
                        hydroScalingAPI.util.probability.DiscreteDistribution myIntDis,
                        hydroScalingAPI.util.probability.DiscreteDistribution myExtDis,
                        String familyID) {
                            
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(4);
                            
        genID=familyID;
        
        String labelSubTree;
        
        if(generation>0){
            int numMembers;
            String GenClass="";
            if(extInt==0){
                numMembers=myExtDis.sample();
                GenClass=",E";
            } else{
                numMembers=myIntDis.sample();
                GenClass=",I";
            }
            
            subTree=new Generator[2*numMembers+1];
            if(numMembers>0){
                for(int i=0;i<2*numMembers;i++){
                    String LinkClass=",I";
                    if(1-i%2==0) LinkClass=",E";
                    
                    if (generation == 1) 
                        labelSubTree=""+labelFormat.format(i)+LinkClass;
                    else
                        labelSubTree=""+labelFormat.format(i);
                        
                    
                    subTree[i]=new Generator(1-i%2,generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
                }
            }
            if (generation == 1) 
                labelSubTree=""+labelFormat.format(2*numMembers)+GenClass;
            else
                labelSubTree=""+labelFormat.format(2*numMembers);
            subTree[2*numMembers]=new Generator(extInt, generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
        }
    }
    
    public String getGenID(){
        return genID;
    }
    
    public Generator[] getSubTree(){
        return subTree;
    }
    
    public String[] decodeRsnTree(){
        String baseID="";
        for(int i=0;i<subTree.length;i++){
            baseID+=decodeBranch(baseID,subTree[i]);
        }
        return baseID.split(":");
    }
    
    private String decodeBranch(String baseID,Generator currentBranch){
        Generator[] branchBranches=currentBranch.getSubTree();
        if(branchBranches == null) return currentBranch.getGenID()+":";
        String branchID="";
        for(int i=0;i<branchBranches.length;i++){
            branchID+=decodeBranch(baseID,branchBranches[i]);
        }
        return branchID;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        main0(args); //Writes IDL code with some RSNs
        
    }
    
    public static void main0(String[] args) {
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(4);
        
        for(int i=1;i<=5;i++){
        
            System.out.println("pro getData_for_embeding_known_RSN_"+args[0]+"_"+i);
            System.out.println("common topology,rsnTopology");
            System.out.println("");
            System.out.println("rsnTopology=[$");
            hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(0.44, 0);
            hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(0.46, 1);
            Generator testGen=new Generator(0,i,myUD_I,myUD_E,labelFormat.format(0));
            String[] decT=testGen.decodeRsnTree();
            for(int j=0;j<decT.length-1;j++){
                String[] trimedDecode=decT[j].split(",");
                for(int k=0;k<trimedDecode.length;k++) System.out.print("'"+trimedDecode[k]+"'"+",");
                System.out.println("$");
            }
            String[] trimedDecode=decT[decT.length-1].split(",");
            System.out.print("'"+trimedDecode[0]+"'");
            for(int k=1;k<trimedDecode.length;k++) System.out.print(","+"'"+trimedDecode[k]+"'");
            System.out.println("]");
            
            System.out.println("");
            System.out.println("end");
            System.out.println("");
        }
        
    }
    
}
