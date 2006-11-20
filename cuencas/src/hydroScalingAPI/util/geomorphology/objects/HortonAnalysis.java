/*
 * hortonAnalysis.java
 *
 * Created on July 19, 2001, 9:59 AM
 */

package hydroScalingAPI.util.geomorphology.objects;

/**
 *
 * @author  Ricardo Mantilla
 * @version 
 */
public class HortonAnalysis extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster localMetaRaster;
    private hydroScalingAPI.util.geomorphology.objects.Basin basin;
    private byte[][] fullDirMatrix;
    
    private double binSize=1.0; //kilometers or # of links
    
    private int basinOrder;
    
    public int[][] headsArray;
    public int[][] contactsArray;
    public int[][] tailsArray;
    
    private String[] extenciones={".areas",
                                  ".magn",
                                  ".lcp",
                                  ".dtopo",
                                  ".ltc",
                                  ".slopes",
                                  ".tcd",
                                  ".mcd",
                                  ".corrDEM"};
                                  
    public int[] formatosAsoc={0,1,0,1,0,2,0,0,2}; //0:Flotantes, 1:Enteros, 2:Doubles

    /** Creates new hortonAnalysis */
    public HortonAnalysis(hydroScalingAPI.util.geomorphology.objects.Basin bas,hydroScalingAPI.io.MetaRaster metar, byte[][] DM) throws java.io.IOException{
        
        localMetaRaster=metar;
        basin=bas;
        fullDirMatrix=DM;
        
        byte[][] basinMask=basin.getNetworkMask();

        //abrir los archivos .stream . link .point
        
        hydroScalingAPI.io.MetaNetwork fullNetwork=new hydroScalingAPI.io.MetaNetwork(localMetaRaster);
        
        java.util.Vector myContacts=new java.util.Vector();
        java.util.Vector myHeads=new java.util.Vector();
        java.util.Vector myTails=new java.util.Vector();
        
        for (int i=0;i<fullNetwork.getStreamRecord().length;i++) {

            int posY=fullNetwork.getStreamRecord()[i][0]/localMetaRaster.getNumCols()-basin.getMinY();
            int posX=fullNetwork.getStreamRecord()[i][0]%localMetaRaster.getNumCols()-basin.getMinX();
            
            if (posY>=0 && posY<basinMask.length && posX>=0 && posX<basinMask[0].length){
                if (basinMask[posY][posX] != 0){
            
                    myHeads.add(new int[] {fullNetwork.getStreamRecord()[i][0]});
                    myContacts.add(new int[] {fullNetwork.getStreamRecord()[i][3],fullNetwork.getStreamRecord()[i][1]});
                    myTails.add(new int[] {fullNetwork.getStreamRecord()[i][2]});
                    
                    basinOrder=Math.max(fullNetwork.getStreamRecord()[i][3],basinOrder);
                    
                }
            }

        }
        
        float[] cuentaOrdenes=new float[basinOrder];
        int[] tmpInt1;
        int linkSalida=0;
        
        for (int i=0;i<myContacts.size();i++){
            tmpInt1=(int[]) myContacts.get(i);
            cuentaOrdenes[tmpInt1[0]-1]++;
            if (tmpInt1[0] == basinOrder) linkSalida=i;
        }
        
        tmpInt1=(int[]) myHeads.get(linkSalida);
        
        myHeads.remove(linkSalida);
        myContacts.remove(linkSalida);
        myTails.remove(linkSalida);

        myHeads.add(tmpInt1);
        myContacts.add(new int[] {basinOrder, basin.getOutletID()});

        int ia=basin.getOutletID()/localMetaRaster.getNumCols();
        int ja=basin.getOutletID()%localMetaRaster.getNumCols();

        int iaN=ia+((fullDirMatrix[ia][ja]-1)/3)-1;
        int jaN=ja+((fullDirMatrix[ia][ja]-1)%3)-1;

        int idContacto=iaN*localMetaRaster.getNumCols()+jaN;

        myTails.add(new int[] {idContacto});
        
        headsArray=new int[basinOrder][];
        contactsArray=new int[basinOrder][];
        tailsArray=new int[basinOrder][];
        
        for (int i=0;i<basinOrder;i++){
            headsArray[i]=new int[(int) cuentaOrdenes[i]];
            contactsArray[i]=new int[(int) cuentaOrdenes[i]];
            tailsArray[i]=new int[(int) cuentaOrdenes[i]];
            cuentaOrdenes[i]=0;
            
        }
        
        for (int i=0;i<myContacts.size();i++){
            int[] thisHead=(int[]) myHeads.get(i);
            int[] thisContact=(int[]) myContacts.get(i);
            int[] thisTail=(int[]) myTails.get(i);
            
            headsArray[thisContact[0]-1][(int) cuentaOrdenes[thisContact[0]-1]]=thisHead[0];
            contactsArray[thisContact[0]-1][(int) cuentaOrdenes[thisContact[0]-1]]=thisContact[1];
            tailsArray[thisContact[0]-1][(int) cuentaOrdenes[thisContact[0]-1]]=thisTail[0];
            
            cuentaOrdenes[thisContact[0]-1]++;
        }
        
    }
    
    public float[] getBranchingRatio(int minOrderToInclude, int maxOrderToInclude,float[] cuentaOrdenes){
        //This method retunrns the Branching Ratio and the regresion coeficient
        return ajustesRO(cuentaOrdenes,minOrderToInclude,maxOrderToInclude,true);
    }
    
    public float[] getBranchingRatio(int minOrderToInclude, int maxOrderToInclude){
        //This method retunrns the Branching Ratio and the regresion coeficient
        
        float[] cuentaOrdenes=getBranchingPerOrder();

        return getBranchingRatio(minOrderToInclude,maxOrderToInclude,cuentaOrdenes);
    }
    
    public float[] getBranchingPerOrder(){
        //This method retunrns the numbers of branches of order w
        float[] cuentaOrdenes=new float[basinOrder];
        for (int i=0;i<tailsArray.length;i++)
            cuentaOrdenes[i]=tailsArray[i].length;
        
        return cuentaOrdenes;
        
    }
    
    public float[] getQuantityRatio(int minOrderToInclude, int maxOrderToInclude,int varToGet) throws java.io.IOException{
        //This method retunrns the Area Ratio and the regresion coeficient
        
        float[] meansQuantityArray= getQuantityPerOrder(varToGet);
        
        return ajustesRO(meansQuantityArray,minOrderToInclude,maxOrderToInclude,true);
    }
    
    public float[] getQuantityRatio(int minOrderToInclude, int maxOrderToInclude,float[][] quantityArray) throws java.io.IOException{
        //This method retunrns the Area Ratio and the regresion coeficient
        
        float[] meansQuantityArray= getQuantityPerOrder(quantityArray);
        
        return ajustesRO(meansQuantityArray,minOrderToInclude,maxOrderToInclude,true);
    }

    public float[] getQuantityPerOrder(int varToGet) throws java.io.IOException{
        //This method retunrns the average areas of order w
        
        float[][] quantityArray=getQuantityDistributionPerOrder(varToGet);
        
        float[] meansQuantityArray=new float[basinOrder];
        for (int i=0;i<basinOrder;i++){
            for (int j=0;j<quantityArray[i].length;j++){
                meansQuantityArray[i]+=quantityArray[i][j];
            }
            meansQuantityArray[i]/=(float) quantityArray[i].length;
        }
        
        return meansQuantityArray;
        
    }
    
    public float[] getQuantityPerOrder(float[][] quantityArray) throws java.io.IOException{
        //This method retunrns the average areas of order w
        
        float[] meansQuantityArray=new float[basinOrder];
        for (int i=0;i<basinOrder;i++){
            for (int j=0;j<quantityArray[i].length;j++){
                meansQuantityArray[i]+=quantityArray[i][j];
            }
            meansQuantityArray[i]/=(float) quantityArray[i].length;
        }
        
        return meansQuantityArray;
        
    }
    
    public float[][] getQuantityDistributionPerOrder(int varToGet) throws java.io.IOException {
        //This method retunrns all the areas for links of order w
        
        float[][] quantityArray;
        
        java.io.File rutaQuantity=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+extenciones[varToGet]);
        java.io.RandomAccessFile fileQuantity=new java.io.RandomAccessFile(rutaQuantity,"r");
        
        quantityArray=new float[basinOrder][];
        
        for (int i=0;i<quantityArray.length;i++){
            
            quantityArray[i]=new float[contactsArray[i].length];

            for (int j=0;j<quantityArray[i].length;j++){
                
                switch(formatosAsoc[varToGet]){
                    case 0:
                        fileQuantity.seek(4*contactsArray[i][j]);
                        quantityArray[i][j]=fileQuantity.readFloat();
                        break;
                    case 1:
                        fileQuantity.seek(4*contactsArray[i][j]);
                        quantityArray[i][j]=(float) fileQuantity.readInt();
                        break;
                    case 2:
                        if(varToGet == 8){
                            fileQuantity.seek(8*headsArray[i][j]);
                            quantityArray[i][j]=(float) fileQuantity.readDouble();
                            fileQuantity.seek(8*tailsArray[i][j]);
                            quantityArray[i][j]-=(float) fileQuantity.readDouble();
                        } else {
                            fileQuantity.seek(8*contactsArray[i][j]);
                            quantityArray[i][j]=(float) fileQuantity.readDouble();
                        }
                        break;
                }
                
            }
            
        }
        
        fileQuantity.close();
        
        return quantityArray;
        
    }
    
    public float[] getLengthRatio(int minOrderToInclude, int maxOrderToInclude,int metric) throws java.io.IOException{
        //This method retunrns the Area Ratio and the regresion coeficient
        float[] meansLengthArray= getLengthPerOrder(metric);
        
        return ajustesRO(meansLengthArray,minOrderToInclude,maxOrderToInclude,true);
    }
    
    public float[] getLengthRatio(int minOrderToInclude, int maxOrderToInclude,float[][] lengthArray) throws java.io.IOException{
        //This method retunrns the Area Ratio and the regresion coeficient
        float[] meansLengthArray= getLengthPerOrder(lengthArray);
        
        return ajustesRO(meansLengthArray,minOrderToInclude,maxOrderToInclude,true);
    }

    
    public float[] getLengthPerOrder(int metric) throws java.io.IOException{
        //This method retunrns the average areas of order w
        
        float[][] lengthArray=getLengthDistributionPerOrder(metric);
        
        float[] meansLengthArray=new float[basinOrder];
        for (int i=0;i<basinOrder;i++){
            for (int j=0;j<lengthArray[i].length;j++){
                meansLengthArray[i]+=lengthArray[i][j];
            }
            meansLengthArray[i]/=(float) lengthArray[i].length;
        }
        
        return meansLengthArray;
        
    }
    
     public float[] getLengthPerOrder(float[][] lengthArray) throws java.io.IOException{
        //This method retunrns the average areas of order w
        
        float[] meansLengthArray=new float[basinOrder];
        for (int i=0;i<basinOrder;i++){
            for (int j=0;j<lengthArray[i].length;j++){
                meansLengthArray[i]+=lengthArray[i][j];
            }
            meansLengthArray[i]/=(float) lengthArray[i].length;
        }
        
        return meansLengthArray;
        
    }
    
    public float[][] getLengthDistributionPerOrder(int metric/* 0: geometric, 1: Topologic */){
        //This method returns all the areas for links of order w
        
        byte [][] rasterNetworkMatrix=new byte[1][1];;
        try{
            localMetaRaster.setLocationBinaryFile(new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+".redRas"));
            localMetaRaster.setFormat("Byte");
            rasterNetworkMatrix=new hydroScalingAPI.io.DataRaster(localMetaRaster).getByte();
        } catch (java.io.IOException IOE){
            System.out.print("IOException at getLengthDistributionPerOrder: "+metric);
            System.out.print(IOE);
        }
        
        double dy = 6378.0*localMetaRaster.getResLat()*Math.PI/(3600.0*180.0);
        double[] dx = new double[localMetaRaster.getNumRows()+1];
        double[] dxy = new double[localMetaRaster.getNumRows()+1];
        /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel
          y la diagonal, dependiendo de la latitud.*/
        
        int numCols=localMetaRaster.getNumCols();
        int numRows=localMetaRaster.getNumRows();
        
        for (int i=1 ; i<=numRows ; i++){
            dx[i] = 6378.0*Math.cos((i*localMetaRaster.getResLat()/3600.0 + localMetaRaster.getMinLat())*Math.PI/180.0)*localMetaRaster.getResLat()*Math.PI/(3600.0*180.0);
            dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
        }
        
        float[][] lengthArray=new float[basinOrder][];
        
        for (int i=0;i<lengthArray.length;i++){
            lengthArray[i]=new float[tailsArray[i].length];
            for (int j=0;j<lengthArray[i].length;j++){
                double rayLength=0;
                
                int upID=headsArray[i][j]; 
                do{
                    int upIDX=upID%numCols;
                    int upIDY=upID/numCols;
                    
                    int downIDX=upIDX-1+(fullDirMatrix[upIDY][upIDX]-1)%3;
                    int downIDY=upIDY-1+(fullDirMatrix[upIDY][upIDX]-1)/3;
                    
                    int downID=downIDY*numCols+downIDX;
                    
                    if (metric == 0){
                        rayLength+=Math.sqrt(Math.pow(dx[upIDY],2)*Math.abs(downIDX-upIDX)+Math.pow(dy,2)*Math.abs(downIDY-upIDY));
                    } else {
                        //check if a neighbor gets to me
                        byte endLink=0;
                        for (int k=0; k <= 8; k++){
                           int existent=(int)Math.max(0,(double)rasterNetworkMatrix[downIDY+(k/3)-1][downIDX+(k%3)-1]);
                           if ((existent*fullDirMatrix[downIDY+(k/3)-1][downIDX+(k%3)-1]) == 9-k){
                               endLink++;
                           }
                       }
                       rayLength+=((endLink>1)?1:0);
                    }
                    upID=downID;
                } while(upID != tailsArray[i][j]);
                lengthArray[i][j]=(float)rayLength;
            }
            
        }
        
        return lengthArray;
    }
    
    
    
    
    public float[] ajustesRO(float[] datos,int minOrderToInclude, int maxOrderToInclude,boolean takeLogs){
        
        int maxOrden=datos.length;
        
        float Ys[] = new float[maxOrderToInclude-minOrderToInclude+1];
        float Xs[] = new float[maxOrderToInclude-minOrderToInclude+1];
        
        if (takeLogs){
            for (int i=0; i<Xs.length; i++){
                Xs[i]=minOrderToInclude+i;
                Ys[i]=(float) Math.log(datos[minOrderToInclude+i-1]);
                //System.out.println(Xs[i]+" "+datos[minOrderToInclude+i-1]);
            }
        } else {
            for (int i=0; i<Xs.length; i++){
                Xs[i]=minOrderToInclude+i;
                Ys[i]=datos[minOrderToInclude+i-1];
            }
        }
        
        hydroScalingAPI.tools.Stats estadY = new hydroScalingAPI.tools.Stats(Ys);
        hydroScalingAPI.tools.Stats estadX = new hydroScalingAPI.tools.Stats(Xs);
        
        float covar=0.0f;
        
        for (int i=0; i<Xs.length; i++) covar+=(Xs[i]-estadX.meanValue)*(Ys[i]-estadY.meanValue);
        
        float b = covar/(float) Math.pow(estadX.standardDeviation,2)/(float) Xs.length;
        float a = estadY.meanValue-b*estadX.meanValue;
        
        float R2=(float) Math.pow(covar/Math.sqrt(Math.pow(estadX.standardDeviation,2)*Math.pow(estadY.standardDeviation,2)*Math.pow(Xs.length,2)),2);
        
        float[] puntos = {b,a,R2};         
        return  puntos;
    }
    
    public int getBasinOrder(){
        return basinOrder;
    }
    
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000"); 
        
        try{
            
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dir"));
        
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3083,1688,matDirs,metaModif);
            
            HortonAnalysis myResults=new HortonAnalysis(laCuenca, metaModif, matDirs);
            
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dem"));
            metaModif.restoreOriginalFormat();
            float[][] myDEM=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            for (int i=1;i< myResults.contactsArray.length;i++){
                int k=1;
                //System.out.println("Order "+(i+1));
                for (int j=0;j< myResults.contactsArray[i].length;j++){
                    
                    int upIDX=myResults.contactsArray[i][j]%metaModif.getNumCols();
                    int upIDY=myResults.contactsArray[i][j]/metaModif.getNumCols();
                    
                    double resultX=upIDX*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    double resultY=upIDY*metaModif.getResLat()/3600.0+metaModif.getMinLat();
                    
                    //System.out.println("O"+(i+1)+(new Float((float)k/10000.+0.000001).toString().substring(2,6))+" N"+hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY,0)+" W"+hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX,1)+" "+myDEM[upIDY][upIDX]);
                    System.out.println((i+1)+dpoint2.format(new Double((double)k/10000.0)).substring(2,6)+"B\t"+resultY+"\t"+resultX+"\t"+myDEM[upIDY][upIDX]);
                    
                    upIDX=myResults.headsArray[i][j]%metaModif.getNumCols();
                    upIDY=myResults.headsArray[i][j]/metaModif.getNumCols();
                    
                    resultX=upIDX*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    resultY=upIDY*metaModif.getResLat()/3600.0+metaModif.getMinLat();
                    
                    //System.out.println("O"+(i+1)+(new Float((float)k/10000.+0.000001).toString().substring(2,6))+" N"+hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY,0)+" W"+hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX,1)+" "+myDEM[upIDY][upIDX]);
                    System.out.println((i+1)+dpoint2.format(new Double((double)k/10000.0)).substring(2,6)+"T\t"+resultY+"\t"+resultX+"\t"+myDEM[upIDY][upIDX]);
                    
                    k++;
                    
                    System.exit(0);
                }
            }
                
                
            /*float[][] ratios=myResults.getLengthDistributionPerOrder(0);
            for (int i=0;i<ratios.length;i++) {
                for (int j=0;j<ratios[i].length;j++) {
                    System.out.print(ratios[i][j]+" ");
                }
                System.out.println("");
            }
            
            ratios=myResults.getLengthDistributionPerOrder(1);
            for (int i=0;i<ratios.length;i++) {
                for (int j=0;j<ratios[i].length;j++) {
                    System.out.print(ratios[i][j]+" ");
                }
                System.out.println("");
            }*/
           
            /*float[][] ratios=myResults.getQuantityDistributionPerOrder(5);
            for (int i=0;i<ratios.length;i++) {
                for (int j=0;j<ratios[i].length;j++) {
                    System.out.print(ratios[i][j]+" ");
                }
                System.out.println("");
            }*/
            
            
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }

}
