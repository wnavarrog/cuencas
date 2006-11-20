package hydroScalingAPI.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class MetaRaster{
    
    private java.io.BufferedReader fileMeta;

    private java.io.File locationMeta;
    private java.io.File locationBinaryFile;
    private boolean completed;
    
    private String originalFormat;

    private java.util.Hashtable properties;
    private java.util.Hashtable categories;

    public String[] parameters= { 
                                    "[Name]",
                                    "[Southernmost Latitude]",
                                    "[Westernmost Longitude]",
                                    "[Longitudinal Resolution (ArcSec)]",
                                    "[Latitudinal Resolution (ArcSec)]",
                                    "[# Columns]",
                                    "[# Rows]",
                                    "[Format]",
                                    "[Missing]",
                                    "[Temporal Resolution]",
                                    "[Units]",
                                    "[Information]"
                                };

    /** Creates new metaMdt */
                                
    public MetaRaster(MetaRaster mr){
        
        properties=mr.cloneProperties();
        
    }
    
    public MetaRaster(){
        
        properties=new java.util.Hashtable();
        
    }
    
    public MetaRaster(java.io.File file) throws java.io.IOException{

        locationMeta=file;

        checkParameters(file);
        
        properties=new java.util.Hashtable();
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int hemisphereFactor;

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[0]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[1]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[2]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[3]));
        try{
            Double resLon=new Double(fileMeta.readLine());
            properties.put(fullLine,resLon);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [Longitudinal Resolution (ArcSec)]");
            return;
        }

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[4]));
        try{
            Double resLat=new Double(fileMeta.readLine());
            properties.put(fullLine,resLat);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [Latitudinal Resolution (ArcSec)]");
            return;
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[5]));
        try{
            Integer nCols=new Integer(fileMeta.readLine());
            properties.put(fullLine,nCols);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [# Columns]");
            return;
        }
        

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[6]));
        try{
            Integer nRows=new Integer(fileMeta.readLine());
            properties.put(fullLine,nRows);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [# Rows]");
            return;
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[7]));
        properties.put(fullLine,fileMeta.readLine());

        originalFormat=(String)properties.get(fullLine);
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[8]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[9]));
        properties.put(fullLine,fileMeta.readLine());
       
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[10]));
        properties.put(fullLine,fileMeta.readLine());
       
        if (((String)(properties.get("[Units]"))).equalsIgnoreCase("categories")){
            categories=new java.util.Hashtable();
            while(!(fullLine=fileMeta.readLine()).equalsIgnoreCase("")){
                java.util.StringTokenizer tokens=new java.util.StringTokenizer(fullLine,":");
                if (tokens.countTokens()>1){
                    categories.put(tokens.nextToken(),tokens.nextToken());
                }
            }
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[11]));
        properties.put(fullLine,fileMeta.readLine());
        
        completed=true;
        fileMeta.close();
    }

    public boolean checkParameters(java.io.File file) throws java.io.IOException{
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int i=0;

        do{
            fullLine=fileMeta.readLine();
            if (fullLine.trim().equalsIgnoreCase(parameters[i])) i++;
        } while (i<parameters.length && fullLine != null);

        fileMeta.close();

        if (i == parameters.length) return true;
        else return false;

    }
    
    public String toString(){
        return getName()+" - "+getLocationMeta().getName();
    }
    
    public void setLocationMeta(java.io.File file){
        locationMeta=file;
    }
    
    public void setLocationBinaryFile(java.io.File file){
        locationBinaryFile=file;
    }
    
    public void setName(String newName){
        properties.put("[Name]",newName);
    }
    
    public void setMinLat(String newMinLat){
        properties.put("[Southernmost Latitude]",newMinLat);
    }
    
    public void setMinLon(String newMinLon){
        properties.put("[Westernmost Longitude]",newMinLon);
    }
    
    public void setResLat(double newResLat){
        properties.put("[Latitudinal Resolution (ArcSec)]",new Double(newResLat));
    }
    
    public void setResLon(double newResLon){
        properties.put("[Longitudinal Resolution (ArcSec)]",new Double(newResLon));
    }
    
    public void setNumCols(int newNumCols){
        properties.put("[# Columns]",new Integer(newNumCols));
    }
    
    public void setNumRows(int newNumRows){
        properties.put("[# Rows]",new Integer(newNumRows));
    }
    
    public void setFormat(String newFormat){
        properties.put("[Format]",newFormat);
    }
    
    public void setMissing(String newMissinng){
        properties.put("[Missing]",newMissinng);
    }
    
    public void restoreOriginalFormat(){
        properties.put("[Format]",originalFormat);
    }
    
    public void setTemporalScale(String tempScale){
        properties.put("[Temporal Resolution]",tempScale);
    }
    
    public void setUnits(String newUnits){
        properties.put("[Units]",newUnits);
    }
    
    public void setInformation(String newInformation){
        properties.put("[Information]",newInformation);
    }
    
    public java.io.File getLocationMeta(){
        return locationMeta;
    }
    
    public java.io.File getLocationBinaryFile(){
        return locationBinaryFile;
    }
    
    public String getProperty(String prop){
        return (String) properties.get(prop);
    }
    
    public String getMissing(){
        return (String) properties.get("[Missing]");
    }
    
    public String getFormat(){
        return (String) properties.get("[Format]");
    }
    
    public String getUnits(){
        return (String) properties.get("[Units]");
    }
    
    public String getCategory(String catToFind){
        return (String) categories.get(catToFind);
    }
    
    public int getNumRows(){
        return ((Integer)  properties.get("[# Rows]")).intValue();
    }
    
    public int getNumCols(){
        return ((Integer)  properties.get("[# Columns]")).intValue();
    }
    
    public double getMinLat(){
        return hydroScalingAPI.tools.DMSToDegrees.getDegrees((String) properties.get("[Southernmost Latitude]"));
    }
    
    public double getMinLon(){
        return hydroScalingAPI.tools.DMSToDegrees.getDegrees((String) properties.get("[Westernmost Longitude]"));
    }
    
    public double getResLat(){
        return ((Double) properties.get("[Latitudinal Resolution (ArcSec)]")).doubleValue();
    }
    
    public double getResLon(){
        return ((Double) properties.get("[Longitudinal Resolution (ArcSec)]")).doubleValue();
    }
    
    public double getMaxLat(){
        return this.getMinLat()+this.getNumRows()*this.getResLat()/3600.0;
    }
    
    
    public double getMaxLon(){
        return this.getMinLon()+this.getNumCols()*this.getResLon()/3600.0;
    }
    
    public String getName(){
        return (String) properties.get("[Name]");
    }
    
    public int getTemporalScale(){
        int milliseconsTemporal=0;
        String temporalScale=(String) properties.get("[Temporal Resolution]");
        java.util.StringTokenizer tokensTime=new java.util.StringTokenizer(temporalScale,"-");
        if (temporalScale.indexOf("fix") != -1) milliseconsTemporal=-1;
        if (temporalScale.indexOf("day") != -1) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*24*60*60*1000;
        if (temporalScale.indexOf("hour") != -1) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*60*60*1000;
        if (temporalScale.indexOf("minute") != -1 ) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*60*1000;
        if (temporalScale.indexOf("second") != -1 ) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*1000;
        
        return milliseconsTemporal;
    }
    
    public float[][] getArray() throws java.io.IOException{
        return new hydroScalingAPI.io.DataRaster(this).getFloat();
    }
    
    public visad.FlatField getField() throws visad.VisADException, java.io.IOException{
        
        hydroScalingAPI.io.DataRaster theData = new hydroScalingAPI.io.DataRaster(this);
        
        float[][] valueAndColor=new float[2][];
        valueAndColor[0]=(theData.getFloatLine())[0];
        if(locationBinaryFile.getName().lastIndexOf(".horton") == -1){
            valueAndColor[1]=(theData.getFloatLineEqualized())[0];
        } else {
            valueAndColor[1]=(theData.getFloatLine())[0];
            hydroScalingAPI.tools.Stats statColors=new hydroScalingAPI.tools.Stats(valueAndColor[1]);
            for (int i=0;i<valueAndColor[1].length;i++) 
                valueAndColor[1][i]=240*(valueAndColor[1][i]-statColors.minValue)/(statColors.maxValue-statColors.minValue)+1;
        }
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.RealTupleType range=new visad.RealTupleType(visad.RealType.getRealType("varValue"),visad.RealType.getRealType("varColor"));
        visad.FunctionType  domainToRangeFunction = new visad.FunctionType( domain, range);
        visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domain,getMinLon()+getResLon()/3600.0/2.0,getMinLon()+getNumCols()*getResLon()/3600.0-getResLon()/3600.0/2.0,getNumCols(),
                                                                        getMinLat()+getResLat()/3600.0/2.0,getMinLat()+getNumRows()*getResLat()/3600.0-getResLat()/3600.0/2.0,getNumRows());
        visad.FlatField theField = new visad.FlatField( domainToRangeFunction, domainExtent);
        theField.setSamples( valueAndColor, false );
        
        return theField;    
    }
    
    public void writeMetaRaster(java.io.File newMetaLocation) throws java.io.IOException{
        
        java.io.BufferedWriter writerMeta = new java.io.BufferedWriter(new java.io.FileWriter(newMetaLocation));
        
        for(int i=0;i<3;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write((String)properties.get(parameters[i])+"\n");
            writerMeta.write("\n");
        }
        
        for(int i=3;i<5;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write(((Double)properties.get(parameters[i])).toString()+"\n");
            writerMeta.write("\n");
        }
        
        for(int i=5;i<7;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write(((Integer)properties.get(parameters[i])).toString()+"\n");
            writerMeta.write("\n");
        }

        for(int i=7;i<12;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write((String)properties.get(parameters[i])+"\n");
            writerMeta.write("\n");
        }
        
        
        writerMeta.close();
        
    }
    
    public java.util.Hashtable cloneProperties(){
        
        return (java.util.Hashtable) properties.clone();

    }
    
    public static void main (String args[]) {
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.metaDEM"));
            
            System.out.println(metaRaster1.getName());
            System.out.println(metaRaster1.getNumRows());
            System.out.println(metaRaster1.getNumCols());
            System.out.println(metaRaster1.getMinLat());
            System.out.println(metaRaster1.getMinLon());
            System.out.println(metaRaster1.getResLat());
            System.out.println(metaRaster1.getResLon());
            System.out.println(metaRaster1.getMaxLat());
            System.out.println(metaRaster1.getMaxLon());
            System.out.println(metaRaster1.getTemporalScale());
            
            System.out.println();
            metaRaster1.setFormat("Double");
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.corrDEM"));
            double[][] datosToPrint=new hydroScalingAPI.io.DataRaster(metaRaster1).getDouble();
            
            java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
            java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
            dpoint4.applyPattern("0000.00000000000000000");
            
            for(int i=2030;i>2020;i--){
                for(int j=430;j<440;j++){
                    System.out.print(dpoint4.format(datosToPrint[i][j])+" ");
                }
                System.out.println();
            }
            
            System.out.println();
            metaRaster1.setFormat("Byte");
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.dir"));
            datosToPrint=new hydroScalingAPI.io.DataRaster(metaRaster1).getDouble();
            
            for(int i=2030;i>2020;i--){
                for(int j=430;j<440;j++){
                    System.out.print(dpoint4.format(datosToPrint[i][j])+" ");
                }
                System.out.println();
            }
            
            //metaRaster1.writeMetaRaster(new java.io.File("/tmp/test.metaDEM"));
            
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }/* catch (visad.VisADException vie){
            System.out.print(vie);
            System.exit(0);
        }*/
        
        System.exit(0);
    }
}
