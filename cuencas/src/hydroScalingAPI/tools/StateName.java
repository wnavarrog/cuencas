/*
 * StateName.java
 *
 * Created on June 13, 2003, 11:17 PM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author Ricardo Mantilla
 */
public abstract class StateName {
    private static String[] code={"AK","AL","AR","AS","AZ","CA","CO","CT","DC","DE","FL","GA","GU","HI","IA","ID","IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","PR","RI","SC","SD","TN","TX","UT","VA","VI","VT","WA","WI","WV","WY"};
    private static String[] name={"Alaska","Alabama","Arkansas","American Samoa","Arizona","California","Colorado","Connecticut","District Of Columbia","Delaware","Florida","Georgia","Guam","Hawaii","Iowa","Idaho","Illinois","Indiana","Kansas","Kentucky","Louisiana","Massachusetts","Maryland","Maine","Michigan","Minnesota","Missouri","Mississippi","Montana","North Carolina","North Dakota","Nebraska","New Hampshire","New Jersey","New Mexico","Nevada","New York","Ohio","Oklahoma","Oregon","Pennsylvania","Puerto Rico","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Virginia","Virgin Islands","Vermont","Washington","Wisconsin","West Virginia","Wyoming"};

    /** Creates a new instance of StateName */
    public static String StateName(String state) {
        for (int i=0;i<code.length;i++){
            if(code[i].equalsIgnoreCase(state)) return name[i];
            if(name[i].equalsIgnoreCase(state)) return name[i];
        }
        return state;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(StateName.StateName("co"));
    }
    
}
