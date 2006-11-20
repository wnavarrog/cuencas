package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author  Jorge Mario Ramirez
 * @version
 */
public class GeomorphCell_0 extends Object implements Comparable{
    
    public int i ;
    public int j ;
    public double var_to_compare;
    public double height ;
    public double min_ady;
    public double distopit;
    public int i_romper;
    public int j_romper;
    
    public GeomorphCell_0(int ii, int jj, double ccota, double mmin_ady){
        i = ii;
        j = jj;
        height = ccota;
        var_to_compare = ccota;
        min_ady =  mmin_ady;
    }
    //ESTE COMPARADOR ORGANIZA LAS CELDAS DE MENOR A MAYOR SEGUN SU COTA
    public int compareTo(java.lang.Object c1) {
        int comp;
        GeomorphCell_0 thisGeomorphCell_0 =(GeomorphCell_0)c1;
        comp = (int)(( var_to_compare - thisGeomorphCell_0.var_to_compare)/Math.abs(var_to_compare - thisGeomorphCell_0.var_to_compare));
        return comp;
    }
    
    public double euclid_distance(GeomorphCell_0 cell){
        return Math.sqrt(Math.pow(this.i - cell.i,2) + Math.pow(this.j - cell.j,2));
    }
    
    public void findDisToPit(Object[] pitcells){
        
        
        for (int k=0; k< pitcells.length ; k++){
            GeomorphCell_0 thisCell=(GeomorphCell_0)pitcells[k];
            thisCell.var_to_compare = euclid_distance(thisCell);
        }
        java.util.Arrays.sort(pitcells);
        distopit =((GeomorphCell_0)pitcells[0]).var_to_compare;
        var_to_compare=distopit;
        i_romper=((GeomorphCell_0)pitcells[0]).i;
        j_romper=((GeomorphCell_0)pitcells[0]).j;
        
    }
    
}