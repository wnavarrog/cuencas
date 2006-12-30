/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/*
 * linksAnalysis.java
 *
 * Created on October 14, 2001, 4:23 PM
 */

package hydroScalingAPI.util.geomorphology.objects;

/**
 *
 * @author  Ricardo Mantilla
 */
public class LinksAnalysis extends java.lang.Object {
    
    public hydroScalingAPI.io.MetaRaster localMetaRaster;
    private hydroScalingAPI.util.geomorphology.objects.Basin basin;
    private byte[][] dirMatrix;
    
    public int basinOrder=0,OuletLinkNum,ressimID;
    public int basinMagnitude=0;
    
    public int[] magnitudeArray;
    public int[] headsArray;
    public int[] contactsArray;
    public int[] tailsArray;
    
    public int[][] connectionsArray; //In this array are listed all the links that drain to the i-th link
    public int[] nextLinkArray;
    public int[] completeStreamLinksArray;
    
    /* This constructor is only used by its extension hydroScalingAPI.modules.rsnFlowSymulations.objects.rsnLinksAnalysis */
    public LinksAnalysis(){
    }
                                    
    /** Creates new linksAnalysis */
    public LinksAnalysis(hydroScalingAPI.util.geomorphology.objects.Basin bas,hydroScalingAPI.io.MetaRaster metaR, byte[][] DM) throws java.io.IOException{
        
        localMetaRaster=metaR;
        basin=bas;
        
        byte[][] basinMask=basin.getNetworkMask();
        
        //abrir los archivos .stream . link .point
        
        hydroScalingAPI.io.MetaNetwork fullNetwork=new hydroScalingAPI.io.MetaNetwork(localMetaRaster);
        
        java.util.Vector myMagnitude=new java.util.Vector();
        java.util.Vector myHeads=new java.util.Vector();
        java.util.Vector myContacts=new java.util.Vector();
        java.util.Vector myTails=new java.util.Vector();
        
        for (int i=0;i<fullNetwork.getLinkRecord().length;i++){
            
            int posX=fullNetwork.getLinkRecord()[i][1]%localMetaRaster.getNumCols()-basin.getMinX();
            int posY=fullNetwork.getLinkRecord()[i][1]/localMetaRaster.getNumCols()-basin.getMinY();
            
            if (posY>=0 && posY<basinMask.length && posX>=0 && posX<basinMask[0].length){
                if (basinMask[posY][posX] != 0){
                    
                    myMagnitude.add(new int[] {fullNetwork.getLinkRecord()[i][0]});
                    
                    myHeads.add(new int[] {fullNetwork.getLinkRecord()[i][1]});
                    
                    myContacts.add(new int[] {fullNetwork.getLinkRecord()[i][2]});
                    
                    myTails.add(new int[] {fullNetwork.getLinkRecord()[i][3]});
                    
                }
            }
            
        }
        
        magnitudeArray=new int[myTails.size()];
        headsArray=new int[myTails.size()];
        contactsArray=new int[myTails.size()];
        tailsArray=new int[myTails.size()];
        
        for (int i=0;i<magnitudeArray.length;i++){
            int[] thisMagn=(int[]) myMagnitude.get(i);
            magnitudeArray[i]=thisMagn[0];
            basinMagnitude=Math.max(basinMagnitude,thisMagn[0]);
        }
        
        for (int i=0;i<magnitudeArray.length;i++){
            
            int[] thisHead=(int[]) myHeads.get(i);
            int[] thisContact=(int[]) myContacts.get(i);
            int[] thisTail=(int[]) myTails.get(i);
            
            headsArray[i]=thisHead[0];
            
            if (magnitudeArray[i] != basinMagnitude){
                contactsArray[i]=thisContact[0];
                tailsArray[i]=thisTail[0];
            } else{
                tailsArray[i]=basin.getOutletID();
                contactsArray[i]=basin.getOutletID();
                OuletLinkNum=i;
            }
            
        }
        
        //Here the conection's array is created
        
        dirMatrix=new byte[basin.getMaxY()-basin.getMinY()+3][basin.getMaxX()-basin.getMinX()+3];
        for(int i=0;i<dirMatrix.length;i++){
            for(int j=0;j<dirMatrix[0].length;j++){
                dirMatrix[i][j]=DM[i+basin.getMinY()-1][j+basin.getMinX()-1];
            }
        }
        
        int xOulet,yOulet;
        int[][][] conectMask=new int[2][basin.getMaxY()-basin.getMinY()+3][basin.getMaxX()-basin.getMinX()+3];
        
        for(int i=0;i<contactsArray.length;i++){
            xOulet=headsArray[i]%localMetaRaster.getNumCols();
            yOulet=headsArray[i]/localMetaRaster.getNumCols();
            conectMask[0][yOulet-basin.getMinY()+1][xOulet-basin.getMinX()+1]=i+1;
            
            xOulet=contactsArray[i]%localMetaRaster.getNumCols();
            yOulet=contactsArray[i]/localMetaRaster.getNumCols();
            conectMask[1][yOulet-basin.getMinY()+1][xOulet-basin.getMinX()+1]=-i-1;
        }
        
        connectionsArray=new int[contactsArray.length][];
        java.util.Vector llegadores;
        for(int i=0;i<conectMask[0].length;i++){
            for(int j=0;j<conectMask[0][0].length;j++){
                if(conectMask[0][i][j]>0){
                    llegadores=new java.util.Vector();
                    for (int k=0; k <= 8; k++){
                        if (dirMatrix[i+(k/3)-1][j+(k%3)-1]==9-k && conectMask[1][i+(k/3)-1][j+(k%3)-1]<0){
                            llegadores.add(new int[] {-conectMask[1][i+(k/3)-1][j+(k%3)-1]-1});
                        }
                    }
                    connectionsArray[conectMask[0][i][j]-1]=new int[llegadores.size()];
                    for (int l=0; l <connectionsArray[conectMask[0][i][j]-1].length; l++) {
                        connectionsArray[conectMask[0][i][j]-1][l]=((int[]) llegadores.elementAt(l))[0];
                    }
                }
            }
        }
        
        nextLinkArray=new int[contactsArray.length];
        for(int i=0;i<conectMask[0].length;i++){
            for(int j=0;j<conectMask[0][0].length;j++){
                if(conectMask[1][i][j]<0){
                    nextLinkArray[-conectMask[1][i][j]-1]=conectMask[0][i+((dirMatrix[i][j]-1)/3)-1][j+((dirMatrix[i][j]-1)%3)-1]-1;
                }
            }
        }
        
        java.io.File rutaHorton=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
        java.io.RandomAccessFile fileHorton=new java.io.RandomAccessFile(rutaHorton,"r");
        
        java.util.Vector linksCompletos=new java.util.Vector();
        
        int myOrder,frontOrder;
        
        for(int i=0;i<nextLinkArray.length;i++){
            if (nextLinkArray[i] > 0){
                fileHorton.seek(contactsArray[i]);
                myOrder=fileHorton.readByte();
                basinOrder=Math.max(basinOrder,myOrder);
                fileHorton.seek(contactsArray[nextLinkArray[i]]);
                frontOrder=fileHorton.readByte();
                if (frontOrder>myOrder){
                    linksCompletos.addElement(new int[] {i});
                }
            } else {
                linksCompletos.addElement(new int[] {i});
                
            }
        }
        
        
        fileHorton.close();
        
        completeStreamLinksArray=new int[linksCompletos.size()];
        for(int i=0;i<linksCompletos.size();i++){
            completeStreamLinksArray[i]=((int[]) linksCompletos.get(i))[0];
        }
        
    }
    
    /**
     * 
     * @return Returns network Horton-Strahler order.
     */
    public int getBasinOrder(){
        return basinOrder;
    }
    
    public float[][] getDistancesToOutlet(){
        try{
            float[][] dToOutlet=new float[2][];
            dToOutlet[0]=getVarValues(7)[0];
            dToOutlet[1]=getVarValues(8)[0];
            return dToOutlet;
        } catch (java.io.IOException IOE){
            System.err.println("Failed reading lengths for width Function");
            System.err.println(IOE);
        }
        
        return null;
        
    }
    
    public float[][] getDistancesToOutlet(int outlet){
        try{
            float[][] dToOutlet=new float[2][1];
            //THIS METHOD TO BE COMPLETED FOR REAL BASINS
            dToOutlet[0]=getVarValues(7)[0];
            dToOutlet[1]=getVarValues(8)[0];
            return dToOutlet;
        } catch (java.io.IOException IOE){
            System.err.println("Failed reading lengths for width Function");
            System.err.println(IOE);
        }
        
        return null;
        
    }
    
    
    public double[][] getTopologicWidthFunctions(int[] outlets) throws visad.VisADException, java.rmi.RemoteException{
        
        double[][] widthFunctions=new double[outlets.length][];
        
        int OriginalBasinOutlet=OuletLinkNum;
        
        int metric=1;
        float binsize=1;
        
        for(int k=0;k<outlets.length;k++){
            if(magnitudeArray[outlets[k]] > 1){
                OuletLinkNum=outlets[k];

                float[][] wFunc=getDistancesToOutlet(outlets[k]);
                java.util.Arrays.sort(wFunc[metric]);

                float[][] gWFunc=new float[1][wFunc[metric].length];

                for (int i=0;i<wFunc[0].length;i++)
                    gWFunc[0][i]=wFunc[metric][i];

                visad.RealType numLinks= visad.RealType.getRealType("numLinks");
                visad.RealType distanceToOut = visad.RealType.getRealType("distanceToOut");
                visad.FunctionType func_distanceToOut_numLinks= new visad.FunctionType(distanceToOut, numLinks);
                visad.FlatField vals_ff_W = new visad.FlatField( func_distanceToOut_numLinks, new visad.Linear1DSet(distanceToOut,1,gWFunc[0].length,gWFunc[0].length));
                vals_ff_W.setSamples( gWFunc );

                int numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;

                visad.Linear1DSet binsSet = new visad.Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);

                visad.FlatField hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);

                float[][] laLinea=binsSet.getSamples();
                double[][] laWFunc=hist.getValues();

                widthFunctions[k]=laWFunc[0];
                widthFunctions[k][0]=1;
            } else {
                widthFunctions[k]=new double[] {1};
            }
        }
        
        OuletLinkNum=OriginalBasinOutlet;
        
        return widthFunctions;
        
    }
    
    public float[][] getVarValues(int varIndex) throws java.io.IOException {
        
        String[] extenciones={  ".areas",       /*0*/
                                ".ltc",         /*1*/
                                ".areas",       /*2*/
                                ".corrDEM",     /*3*/
                                ".horton",      /*4*/
                                ".ltc",         /*5*/
                                ".magn",        /*6*/
                                ".gdo",         /*7*/
                                ".tdo",         /*8*/
                                ".corrDEM",     /*9*/
                                ".corrDEM",     /*10*/
                                ".lcp",         /*11*/
                                ".magn",        /*12*/
                                ".tcd"          /*13*/
                                };
    
        float[][] quantityArray=new float[1][tailsArray.length];
        
        java.io.File rutaQuantity=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+extenciones[varIndex]);
        java.io.RandomAccessFile fileQuantity=new java.io.RandomAccessFile(rutaQuantity,"r");
       
        double[][] dToO;
        
        switch(varIndex){
            case 0:
                //Link's Hillslope Area.  This is done by subtraction of area at head and area at incoming links head
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }
                
                break;
            case 1:
                //Link's Length.  This is done by subtraction of tcl at head and tcl at incoming links head
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }
                
                break;
            case 2:
                //Link's Upstream area.
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }
                
                break;
            case 3:
                //Link's drop
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*tailsArray[i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());
                }
                
                break;
            case 4:
                //Link's order
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(headsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readByte();
                }
                
                break;
            case 5:
                //Total Channel Length
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readFloat();
                }
                
                break;
            case 6:
                //Link's Magnitude
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt();
                }
                
                break;
            case 7:
                //Link's Distance to Outlet
                fileQuantity.seek(4*basin.getOutletID());
                float GoToB=fileQuantity.readFloat();
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*headsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat()-GoToB;
                }
                
                break;
            case 8:
                //Link's Topologic Distance to Outlet
                fileQuantity.seek(4*basin.getOutletID());
                int ToToB=fileQuantity.readInt();
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*headsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt()-ToToB;
                }
                
                break;
            case 9:
                //Link's Slope
                
                float[][] linkLengths=getVarValues(1);
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*tailsArray[i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());
                    
                    quantityArray[0][i]/=linkLengths[0][i];
                }
                
                break;
            case 10:
                //Link's Elevation
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    quantityArray[0][i]=(float) (fileQuantity.readDouble());
                }
                
                break;
            case 11:
                //Longest Channel Length
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }
                
                break;
            case 12:
                //Binary Link Address
                
                quantityArray[0]=getBLA();
                
                break;
            case 13:
                //Total Channel Drop
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    if(quantityArray[0][i] == 0) quantityArray[0][i]=1E-5f;
                }
                
                break;
                
                
        }
        
        fileQuantity.close();
        
        return quantityArray;
    }
    
    private float[] getBLA(){
        float[][] linkOrders=new float[0][0];
        try{
            linkOrders=getVarValues(4);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            return null;
        }
        float[] BLAs= new float[connectionsArray.length];
        
        for(int i=0;i<BLAs.length;i++){
            if(magnitudeArray[i]==1){
                float currentOrder=1;
                //System.out.print(i+" : "+currentOrder+",");
                BLAs[i]+=Math.pow(2, currentOrder);
                int j=i;
                while(nextLinkArray[j] != -1){
                    if(currentOrder != linkOrders[0][nextLinkArray[j]]){
                        //System.out.print(linkOrders[0][nextLinkArray[j]]+",");
                        BLAs[i]+=Math.pow(2, linkOrders[0][nextLinkArray[j]]);
                    }
                    currentOrder=linkOrders[0][nextLinkArray[j]];
                    j=nextLinkArray[j];
                }
                //System.out.println(BLAs[i]);
            }
            
        }
        
        return BLAs;
    }
    
    public int getOutletID(){
        
        return OuletLinkNum;
        
    }
    
    public int getResSimID(int x, int y){       
        int contactsID = x+(y*localMetaRaster.getNumCols());     
        ressimID=-1;
        for (int i=0;i<contactsArray.length;i++){
            if (contactsArray[i]==contactsID) ressimID=i+1;
            //Add 1 to ressimID because link ids in resSimul array in idl code start at 1.        
        }
        return ressimID;
    }
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
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
            
            //hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3083,1688,matDirs,metaModif);
            //hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(2256,3499,matDirs,metaModif);
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(2220,4238,matDirs,metaModif);
            
            LinksAnalysis myResults=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(myResults);
            
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dem"));
            metaModif.restoreOriginalFormat();
            float[][] myDEM=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            int[] countersOrderLink=new int[myResults.getBasinOrder()];
            
            for (int i=0;i<myResults.completeStreamLinksArray.length;i++){
                int LinkOrder=(int)thisNetworkGeom.linkOrder(myResults.completeStreamLinksArray[i]);
                countersOrderLink[LinkOrder-1]++;
                if(LinkOrder > 1){
                    int xContact=myResults.contactsArray[myResults.completeStreamLinksArray[i]]%metaModif.getNumCols();
                    int yContact=myResults.contactsArray[myResults.completeStreamLinksArray[i]]/metaModif.getNumCols();

                    double resultX=(xContact+0.5)*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    double resultY=(yContact+0.5)*metaModif.getResLat()/3600.0+metaModif.getMinLat();

                    System.out.println(LinkOrder+dpoint2.format(new Double((double)countersOrderLink[LinkOrder-1]/10000.0)).substring(2,6)+"B\t"+resultY+"\t"+resultX+"\t"+myDEM[yContact][xContact]);

                    int xHead=myResults.headsArray[myResults.completeStreamLinksArray[i]]%metaModif.getNumCols();
                    int yHead=myResults.headsArray[myResults.completeStreamLinksArray[i]]/metaModif.getNumCols();

                    resultX=(xHead+0.5)*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    resultY=(yHead+0.5)*metaModif.getResLat()/3600.0+metaModif.getMinLat();

                    System.out.println(LinkOrder+dpoint2.format(new Double((double)countersOrderLink[LinkOrder-1]/10000.0)).substring(2,6)+"T\t"+resultY+"\t"+resultX+"\t"+myDEM[yHead][xHead]);
                }
            }
            /*System.out.println("Connection Structure");
            for (int i=0;i<myResults.headsArray.length;i++){
                System.out.print("Links connected to Link # "+i+" are: ");
                for (int j=0;j<myResults.connectionsArray[i].length;j++){
                    System.out.print(myResults.connectionsArray[i][j]+",");
                }
                System.out.println();
            }*/
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
}
