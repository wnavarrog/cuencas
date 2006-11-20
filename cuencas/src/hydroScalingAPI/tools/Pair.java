/*
 * Pair.java
 *
 * Created on March 21, 2005, 12:16 PM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author  furey
 */
public class Pair implements Comparable{

    public float property1;
    public float property2;
    
    
    /** Creates a new instance of Pair */
    public Pair(float a, float b) {
        property1=a;
        property2=b;
    }
    
    public int compareTo(Object o) {
        
        Pair newPair=(Pair)o;
        
        if (this.property1 > newPair.property1) return 1;
        if (this.property1 < newPair.property1) return -1;
        if (this.property1 == newPair.property1) return 0;
        
        return 0;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args ) {
        
        Pair[] mypairs=new Pair[5];
        
        //mypairs[0]=new Pair(2,40);
        //mypairs[1]=new Pair(5,50);
        //mypairs[2]=new Pair(3,40);
        //mypairs[3]=new Pair(1,30);
        //mypairs[4]=new Pair(9,40);
        
        //for(int i=0;i<5;i++) System.out.println(mypairs[i].property1+" "+mypairs[i].property2);
        
        java.util.Arrays.sort(mypairs);
        
        //for(int i=0;i<5;i++) System.out.println(mypairs[i].property1+" "+mypairs[i].property2);
        
    }
    
    
    
}
