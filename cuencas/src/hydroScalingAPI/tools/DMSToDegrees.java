package hydroScalingAPI.tools;

public abstract class DMSToDegrees{
    public static int LATITUDE=0;
    public static int LONGITUDE=1;
    private static double Degrees=0;

    /** Creates new GMSToDegrees */
    private static void processString(String GMS){
            java.util.StringTokenizer Chain=new java.util.StringTokenizer(GMS);

            String Horas = Chain.nextToken(":");
            String Minutos=Chain.nextToken(":");
            String Segundos=Chain.nextToken(" ").trim();
            int Hora=new Integer(Horas).intValue();
            int Min=new Integer(Minutos).intValue();
            float Seg=new Float(Segundos.substring(1)).floatValue();

            String Direccion=Chain.nextToken().trim();
            Degrees = Hora;
            Degrees += Min/60d;
            Degrees += Seg/3600d;
            
            Degrees *= -1f;
            
            if(Direccion.equalsIgnoreCase("n") || Direccion.equalsIgnoreCase("e"))
                Degrees *= -1f;
 
    }
    
    public static double getDegrees(String GMS){
        processString(GMS);
        return Degrees;
    }
    
    public static Double getDoubleDegrees(String GMS){
        processString(GMS);
        return new Double(Degrees);
    }
    
    public static void main (String args[]) {
        System.out.println(hydroScalingAPI.tools.DMSToDegrees.getDegrees("00:00:20.05 N"));
        System.out.println(hydroScalingAPI.tools.DMSToDegrees.getDegrees("60:30:20.05 W"));
        System.exit(0);
    }

}