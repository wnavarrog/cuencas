package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author Jorge Mario Ramirez and Ricardo Mantilla
  */
public class GetGeomorphologyROM extends Object {
    
    private int GEO[][];
    private int MD[][];
    private byte RedRas[][];
    private int llegan[][];
    private double dy;
    private double dx[];
    private double dxy[];
    private hydroScalingAPI.io.MetaRaster MR;
    private java.io.RandomAccessFile Dir;
    private java.io.RandomAccessFile Orden;
    private java.io.RandomAccessFile Ltc;
    private java.io.RandomAccessFile Lcp;
    private java.io.RandomAccessFile Magn;
    private java.io.RandomAccessFile Dtopo;
    private java.io.RandomAccessFile Red;
    private int NPoints; //para rayos4
    private int NLinks; //para rayos4
    private java.io.DataOutputStream Dstream; //para rayos4
    private java.io.DataOutputStream Dlink; //para rayos4
    private java.io.DataOutputStream Dpoint; //para rayos4
    private WorkRectangle M;
    
    
    public static void main(String[] arguments){
        try{
            
            hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.metaDEM"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.dem"));
            
            System.out.println(metaRaster1.getLocationBinaryFile());
            
            new GetGeomorphologyROM(metaRaster1);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
        
        System.out.println(">>> DONE <<<");
    }
    
    private void inicio(){
        
        try{
            String rutaMR = MR.getLocationBinaryFile().getPath();
            String rutaDatos = rutaMR.substring(0,rutaMR.lastIndexOf("."));
            Dir=new  java.io.RandomAccessFile(rutaDatos + ".dir","r");
            Red = new java.io.RandomAccessFile(rutaDatos + ".redRas","r");
            Orden = new java.io.RandomAccessFile(rutaDatos + ".horton","rw");
            Magn = new java.io.RandomAccessFile(rutaDatos + ".magn","rw");
            Ltc = new java.io.RandomAccessFile(rutaDatos + ".ltc","rw");
            Lcp = new java.io.RandomAccessFile(rutaDatos + ".lcp","rw");
            Dtopo = new java.io.RandomAccessFile(rutaDatos + ".dtopo","rw");
            MD = new int[MR.getNumRows()][MR.getNumCols()];
            GEO= new int[MR.getNumRows()][MR.getNumCols()];
            RedRas = new byte[MR.getNumRows()][MR.getNumCols()];
            llegan= new int[MR.getNumRows()][MR.getNumCols()];
            dy = 6378.0*MR.getResLon()*Math.PI/(3600.0*180.0);
            dx = new double[MR.getNumRows()+1];
            dxy = new double[MR.getNumRows()+1];
            M = new WorkRectangle(0,MR.getNumRows()-1,0,MR.getNumCols()-1);
            for(int i=0; i<MR.getNumRows(); i++){
                for(int j=0; j<MR.getNumCols(); j++){
                    MD[i][j]=Dir.readByte();
                    RedRas[i][j]=Red.readByte();
                    if(MD[i][j]==0){
                        esc("Ltc",i,j,false,new Float(MR.getMissing()).floatValue());
                        esc("Lcp",i,j,false,new Float(MR.getMissing()).floatValue());
                        esc("Orden",i,j,false,(byte)-10);
                        esc("Magn",i,j,false,new Integer(MR.getMissing()).intValue());
                        esc("Dtopo",i,j,false,new Integer(MR.getMissing()).intValue());
                    }
                    else{
                        esc("Magn",i,j,false,(int)0);
                        esc("Orden",i,j,false,(byte)0);
                        esc("Dtopo",i,j,false,(int)0);
                        esc("Ltc",i,j,false,(float)0.0);
                        esc("Lcp",i,j,false,(float)0.0);
                    }
                }
            }
            
            for(int i=0; i<MR.getNumRows(); i++){
                dx[i] = 6378*Math.cos((i*MR.getResLat()/3600.0 + MR.getMinLat())*Math.PI/180.0)*MR.getResLat()*Math.PI/(3600.0*180.0);
                dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
                for(int j=0; j<MR.getNumCols(); j++){
                    int nllegan = 0;
                    for (int k=0; k <= 8; k++){
                        if(M.is_on(i+(k/3)-1,j+(k%3)-1)){
                            if (MD[i+(k/3)-1][j+(k%3)-1]==9-k && RedRas[i+(k/3)-1][j+(k%3)-1]>0)
                                nllegan++;
                        }
                    }
                    if(RedRas[i][j]<=0)
                        GEO[i][j]= -2;
                    else if (MD[i][j]>0){
                        GEO[i][j] = nllegan;
                    }
                    else{
                        GEO[i][j]= -3;
                    }
                    llegan[i][j]=nllegan;
                }
            }
            
        }catch (java.io.IOException e1){
            System.err.println("ERROR4** "+e1.getMessage());
        }
        System.out.println(">>> Initializtion Process Completed <<<");
    }
    
    //-------------------------------------------------------------------------------------------------------------------------
    public GetGeomorphologyROM(hydroScalingAPI.io.MetaRaster MR1) {
        MR=MR1;
        inicio();
        int contn;
        do{
            contn = 0;
            for(int i=0; i<MR.getNumRows() ; i++){
                double[] dist = {dxy[i],dy,dxy[i],dx[i],1,dx[i],dxy[i],dy,dxy[i]};
                for (int j=0; j<MR.getNumCols(); j++){
                    if (GEO[i][j] < 0)
                        contn++;
                    if (GEO[i][j] == 0){
                        GEOcero(i,j,contn);
                    }//en cada cero
                }//for j
            }//for i
        }while(contn < (MD.length)*(MD[0].length) );
        
    }
    
    //---------------------------------------------------------------------------------------------------------------------------
    
    void GEOcero(int i, int j,int contn){
        try{
            double[] dist = {dxy[i],dy,dxy[i],dx[i],1,dx[i],dxy[i],dy,dxy[i]};
            GEO[i][j] = -1;
            esc("Ltc",i,j,true,(float)dist[MD[i][j]-1]);
            esc("Lcp",i,j,true,(float)dist[MD[i][j]-1]);
            if (llegan[i][j] == 0){
                esc("Magn",i,j,false,(int)1);
                esc("Orden",i,j,false,(byte)1);
                esc("Dtopo",i,j,false,(int)1);
            }
            int x1=i-1+(MD[i][j]-1)/3 ; int y1=j-1+(MD[i][j]-1)%3;
            if (GEO[x1][y1] >= 0 ){
                GEO[x1][y1]--  ;
                Magn.seek((long)4*(i*MR.getNumCols()+j)); int m2=Magn.readInt();
                Ltc.seek((long)4*(i*MR.getNumCols()+j)); float lt2=Ltc.readFloat();
                Lcp.seek((long)4*(i*MR.getNumCols()+j)); float lc2=Lcp.readFloat();
                Lcp.seek((long)4*(x1*MR.getNumCols()+y1)); float lc3=Lcp.readFloat();
                if (llegan[i][j]== 0)
                    esc("Magn", x1,y1,true,1);
                else esc("Magn",x1,y1,true,m2);
                esc("Ltc",x1,y1,true,lt2);
                esc("Lcp",x1,y1,false,Math.max(lc2,lc3));
            }
            if(llegan[i][j]> 0){
                java.util.Vector diamt = new java.util.Vector(0,1);
                java.util.Vector ord = new java.util.Vector(0,1);
                for (int k=0; k <= 8; k++){
                    if (MD[i+(k/3)-1][j+(k%3)-1]==9-k && RedRas[i+(k/3)-1][j+(k%3)-1]!=0){
                        Orden.seek((i+(k/3)-1)*MR.getNumCols()+(j+(k%3)-1));
                        Dtopo.seek(4*((i+(k/3)-1)*MR.getNumCols()+(j+(k%3)-1)));
                        ord.addElement(new Byte(Orden.readByte()));
                        diamt.addElement(new Integer(Dtopo.readInt()));
                    }
                }
                java.util.Collections.sort(ord);
                java.util.Collections.sort(diamt);
                if(llegan[i][j]> 1)
                    esc("Dtopo",i,j,false,1 + new Integer(diamt.get(diamt.size()-1).toString()).intValue());
                if(llegan[i][j]==1 )
                    esc("Dtopo",i,j,false,new Integer(diamt.get(diamt.size()-1).toString()).intValue());
                if (ord.size()>1){
                    if (ord.get(ord.size()-1).equals(ord.get(ord.size()-2))){
                        esc("Orden",i,j,false,(byte)(1 + new Byte(ord.get(ord.size()-1).toString()).byteValue()));
                        RedRas[i][j]=-1;
                    }
                    else{ esc("Orden",i,j,false,new Byte(ord.get(ord.size()-1).toString()).byteValue());
                    }
                }
                if (ord.size()==1){
                    esc("Orden",i,j,false,new Byte(ord.get(0).toString()).byteValue());
                }
            }
            if (llegan[x1][y1]==1 && MD[x1][y1]>0){
                GEOcero(x1,y1,contn);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
    }
    
    //------------------------------------------------------------------------------------------------------------------------
    
    
    public static void getRedVect(NetworkExtractionModule Proc){
        int NPoints; //para rayos4
        int NLinks; //para rayos4
        java.io.DataOutputStream Dstream; //para rayos4
        java.io.DataOutputStream Dlink; //para rayos4
        java.io.DataOutputStream Dpoint; //para rayos4
        int ncol = Proc.metaDEM.getNumCols();
        
        if (Proc.printDebug) System.out.println(">>> Writing Vectorial Network Representation");
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        
        for(int i=0; i<Proc.metaDEM.getNumRows()+2; i++) for (int j=0; j<Proc.metaDEM.getNumCols()+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        }
        
        try{
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.BufferedOutputStream Bstream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".stream"));
            Dstream = new java.io.DataOutputStream(Bstream);
            java.io.BufferedOutputStream Blink = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".link"));
            Dlink = new java.io.DataOutputStream(Blink);
            java.io.BufferedOutputStream Bpoint = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".point"));
            Dpoint = new java.io.DataOutputStream(Bpoint);
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            
            int PointerPoints=0;
            int PointerLinks=0;
            boolean changeLink,changeDir;
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            for(int i=2; i<Proc.metaDEM.getNumRows(); i++){
                for (int j=2; j<Proc.metaDEM.getNumCols(); j++){
                    if (Proc.RedRas[i][j]==1){
                        //informacion sobre el punto especifico
                        int myOrder=Proc.GEO[i][j].orden;
                        boolean source=true;
                        //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                source=false;
                            }
                        }
                        if (source || Proc.GEO[i][j].pcambio){
                            
                            NPoints=1;
                            NLinks=0;
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            Dpoint.writeInt((ia-1)*ncol+(ja-1));
                            int myMagn=Proc.GEO2[ia][ja].magn;
                            Dlink.writeInt(myMagn);
                            Dlink.writeInt((ia-1)*ncol+(ja-1));
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            do{
                                if (cambieLink){
                                    Dpoint.writeInt((ia-1)*ncol+(ja-1));
                                    myMagn=Proc.GEO2[ia][ja].magn;
                                    Dlink.writeInt(myMagn);
                                    Dlink.writeInt((ia-1)*ncol+(ja-1));
                                    NPoints++;
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                                changeDir=false;
                                try{
                                    changeDir=Proc.DIR[ia][ja] != Proc.DIR[iaN][jaN];
                                }catch(ArrayIndexOutOfBoundsException e){System.err.println("error "+"error "+i+" "+j+" "+ia+" "+ja+" "+iaN+" "+jaN); System.exit(0);}
                                int lleganAca=0;
                                for (int k=0; k <= 8; k++){
                                    arroundI=iaN+(k/3)-1;
                                    arroundJ=jaN+(k%3)-1;
                                    if (arroundI>0 && arroundI < Proc.metaDEM.getNumRows() && arroundJ>0 && arroundJ < Proc.metaDEM.getNumCols()){
                                        if (Proc.DIR[iaN+(k/3)-1][jaN+(k%3)-1]==9-k){
                                            lleganAca++;
                                        }
                                    }
                                }
                                changeLink=lleganAca>1;
                                outOrder=Proc.GEO[iaN][jaN].orden;
                                if (changeLink || outOrder <= 0){
                                    Dpoint.writeInt((ia-1)*ncol+(ja-1));
                                    Dpoint.writeInt((iaN-1)*ncol+(jaN-1));
                                    Dlink.writeInt((ia-1)*ncol+(ja-1));
                                    Dlink.writeInt((iaN-1)*ncol+(jaN-1));
                                    
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    DataAP.writeDouble((double) areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    
                                    if (myOrder != outOrder){
                                        Dstream.writeInt((i-1)*ncol+(j-1));
                                        Dstream.writeInt((ia-1)*ncol+(ja-1));
                                        Dstream.writeInt((iaN-1)*ncol+(jaN-1));
                                    }
                                    NPoints+=2;
                                    Dlink.writeInt(PointerPoints*4);  Dlink.writeInt(NPoints);
                                    NLinks++;
                                    PointerPoints+=NPoints;
                                    NPoints=0;
                                    cambieLink=true;
                                } else {
                                    if (changeDir){
                                        Dpoint.writeInt((iaN-1)*ncol+(jaN-1));
                                        NPoints++;
                                    }
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                            Dstream.writeInt(myOrder);
                            Dstream.writeInt(PointerLinks*8);
                            Dstream.writeInt(NLinks);
                            PointerLinks+=NLinks;
                        }
                    }
                }
            }
            Bstream.close(); Blink.close(); Bpoint.close();
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){
            System.err.println(e.toString());
        }
    }
    
    private void esc(String s, int i,int j, boolean m, byte v){
        try{
            if (s.equals("Orden")){
                Orden.seek((long)(i*MR.getNumCols()+j));
                if (m){
                    byte d=Orden.readByte();
                    Orden.seek((long)(i*MR.getNumCols()+j));
                    Orden.writeByte(v+d);
                }
                else  Orden.writeByte(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());}
    }
    
    private void esc(String s, int i,int j, boolean m, float v){
        try{
            if (s.equals("Ltc")){
                Ltc.seek((long)4*(i*MR.getNumCols()+j));
                if (m){
                    float d=Ltc.readFloat();
                    Ltc.seek((long)4*(i*MR.getNumCols()+j));
                    Ltc.writeFloat(v+d);
                }
                else  Ltc.writeFloat(v);
            }
            if (s.equals("Lcp")){
                Lcp.seek((long)4*(i*MR.getNumCols()+j));
                if (m){
                    float d=Lcp.readFloat();
                    Lcp.seek((long)4*(i*MR.getNumCols()+j));
                    Lcp.writeFloat(v+d);
                }
                else  Lcp.writeFloat(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR2** "+e1.getMessage());}
    }
    
    private void esc(String s, int i,int j, boolean m, int v){
        try{
            if (s.equals("Dtopo")){
                Dtopo.seek((long)4*(i*MR.getNumCols()+j));
                if (m){
                    int d=Dtopo.readInt();
                    Dtopo.seek((long)4*(i*MR.getNumCols()+j));
                    Dtopo.writeInt(v+d);
                }
                else  Dtopo.writeInt(v);
            }
            if (s.equals("Magn")){
                Magn.seek((long)4*(i*MR.getNumCols()+j));
                if (m){
                    int d=Magn.readInt();
                    Magn.seek((long)4*(i*MR.getNumCols()+j));
                    Magn.writeInt(v+d);
                }
                else  Magn.writeInt(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR3** "+e1.getMessage());}
    }
    
}