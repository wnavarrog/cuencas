package hydroScalingAPI.util.fileUtilities;

public class ChronoFile extends Object implements Comparable{

    public java.io.File fileName;
    private String baseName;
    
    String[] months={"Enero","January","Febrero","February","Marzo","March","Abril","April","Mayo","May","Junio","June","Julio","July","Agosto","August","Septiembre","September","Octubre","October","Noviembre","November","Diciembre","December"};
    java.util.Vector holdMonths=new java.util.Vector();
    
    java.util.Calendar date=null;  //If the file is dated this variable contains the file date
        
    /** Creates new chronoFile */
    public ChronoFile(java.io.File file, String bN) {
        fileName=file;
        baseName=bN;
        for (int i=0;i<months.length;i++) holdMonths.add(months[i]);
        
        java.util.StringTokenizer thisFile=new java.util.StringTokenizer(this.fileName.getName().substring(baseName.length()+1),".");
        
        String year1,month1,fullDate1;
        int y1,m1,d1,h1,min1,sec1;
        
        switch (thisFile.countTokens()){
            case 1:
                date=java.util.Calendar.getInstance();
                break;
            case 2:
                year1=thisFile.nextToken();
                if(year1.equalsIgnoreCase("average")) break;
                y1=new Integer(year1).intValue();
                date=java.util.Calendar.getInstance(); date.set(y1, 1, 1);
                break;
            case 3:
                month1=thisFile.nextToken();
                m1=holdMonths.indexOf(month1)/2;
                year1=thisFile.nextToken();
                if( year1.equalsIgnoreCase("average")) break;
                y1=new Integer(year1).intValue();
                date=java.util.Calendar.getInstance(); date.clear(); date.set(y1, m1, 1);
                break;
            case 4:
                d1=new Integer(thisFile.nextToken()).intValue();
                month1=thisFile.nextToken();
                m1=holdMonths.indexOf(month1)/2;
                year1=thisFile.nextToken();
                if( year1.equalsIgnoreCase("average")) break;
                y1=new Integer(year1).intValue();
                date=java.util.Calendar.getInstance(); date.clear(); date.set(y1, m1, d1);
                break;
            case 5:
                fullDate1=thisFile.nextToken();
                
                h1=new Integer(fullDate1.substring(0,2)).intValue();
                min1=new Integer(fullDate1.substring(2,4)).intValue();
                sec1=new Integer(fullDate1.substring(4,6)).intValue();
                
                d1=new Integer(thisFile.nextToken()).intValue();
                month1=thisFile.nextToken();
                m1=holdMonths.indexOf(month1)/2;
                y1=new Integer(thisFile.nextToken()).intValue();
                date=java.util.Calendar.getInstance(); date.clear(); date.set(y1, m1, d1, h1, min1, sec1);
                break;
        }
        
        
    }
    
    public int compareTo(final java.lang.Object obj) {
        
        java.util.StringTokenizer thisFile=new java.util.StringTokenizer(this.fileName.getName().substring(baseName.length()+1),".");
        ChronoFile arComp=(ChronoFile) obj;
        java.util.StringTokenizer otroFile=new java.util.StringTokenizer(arComp.fileName.getName().substring(baseName.length()+1),".");
        
        //Primer criterio:  Un solo token gana
        if (thisFile.countTokens() == 1) return -1;
        if (otroFile.countTokens() == 1) return 1;
        
        //Segundo criterio:  Mayor No de Tokens Gana Jerarqia
        if (thisFile.countTokens() != otroFile.countTokens()) return thisFile.countTokens()-otroFile.countTokens();
        
        //Tercer criterio:  Si tienen el mismo numero de tokens determine cuantos
        //si uno solo tiene el ahno, gana average
        
        String year1,year2,month1,mes2,fullDate1,fullDate2;
        int y1,y2,m1,m2,d1,d2,h1,h2,min1,min2,sec1,sec2;
        java.util.Calendar date1,date2;
        
        switch (thisFile.countTokens()){
            case 2:
                year1=thisFile.nextToken(); year2=otroFile.nextToken();
                if(year1.equalsIgnoreCase("promedio") || year1.equalsIgnoreCase("average")) return -1;
                if(year2.equalsIgnoreCase("promedio") || year2.equalsIgnoreCase("average")) return 1;
                return year1.compareTo(year2);
            case 3:
                month1=thisFile.nextToken(); mes2=otroFile.nextToken();
                year1=thisFile.nextToken(); year2=otroFile.nextToken();
                if (year1.equalsIgnoreCase(year2)){
                    return holdMonths.indexOf(month1)-holdMonths.indexOf(mes2);
                } else {
                    if(year1.equalsIgnoreCase("promedio") || year1.equalsIgnoreCase("average")) return -1;
                    if(year2.equalsIgnoreCase("promedio") || year2.equalsIgnoreCase("average")) return 1;
                    return year1.compareTo(year2);
                }
            case 4:
                d1=new Integer(thisFile.nextToken()).intValue();
                d2=new Integer(otroFile.nextToken()).intValue();
                month1=thisFile.nextToken(); mes2=otroFile.nextToken();
                m1=holdMonths.indexOf(month1)/2;
                m2=holdMonths.indexOf(mes2)/2;
                y1=new Integer(thisFile.nextToken()).intValue();
                y2=new Integer(otroFile.nextToken()).intValue();
                date1=java.util.Calendar.getInstance(); date1.set(y1, m1, d1);
                date2=java.util.Calendar.getInstance(); date2.set(y2, m2, d2);
                return (date1.before(date2))?-1:1;
            case 5:
                fullDate1=thisFile.nextToken();
                fullDate2=otroFile.nextToken();
                
                h1=new Integer(fullDate1.substring(0,2)).intValue();
                h2=new Integer(fullDate2.substring(0,2)).intValue();
                min1=new Integer(fullDate1.substring(2,4)).intValue();
                min2=new Integer(fullDate2.substring(2,4)).intValue();
                sec1=new Integer(fullDate1.substring(4,6)).intValue();
                sec2=new Integer(fullDate2.substring(4,6)).intValue();
                
                d1=new Integer(thisFile.nextToken()).intValue();
                d2=new Integer(otroFile.nextToken()).intValue();
                month1=thisFile.nextToken(); mes2=otroFile.nextToken();
                m1=holdMonths.indexOf(month1)/2;
                m2=holdMonths.indexOf(mes2)/2;
                y1=new Integer(thisFile.nextToken()).intValue();
                y2=new Integer(otroFile.nextToken()).intValue();
                date1=java.util.Calendar.getInstance(); date1.set(y1, m1, d1, h1, min1, sec1);
                date2=java.util.Calendar.getInstance(); date2.set(y2, m2, d2, h2, min2, sec2);
                return (date1.before(date2))?-1:1;
        }
        
        return 0;
    }
    
    public java.util.Calendar getDate(){
        return date;
    }
    
    public static void main (String args[]) {
        ChronoFile otr=new ChronoFile (new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/precipitation_events/event_00/precipitation_interpolated_ev00.041000.10.September.1964.vhc"),"precipitation_interpolated_ev00");
        ChronoFile uno=new ChronoFile (new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/precipitation_events/event_00/precipitation_interpolated_ev00.233500.09.September.1964.vhc"),"precipitation_interpolated_ev00");
        System.out.println(uno.compareTo(otr));
                 
        
        otr=new ChronoFile (new java.io.File("/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Hidrologia/precipitation/monthlyData/precipitation.Average.vhc"),"precipitation");
        uno=new ChronoFile (new java.io.File("/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Hidrologia/precipitation/monthlyData/precipitation.July.1993.vhc"),"precipitation");
        System.out.println(uno.compareTo(otr));
    }
    
}