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


package hydroScalingAPI.modules.networkExtraction.objects;

/**
 * This class implements a ROM version of the the downstream-travel algorithm over the network to
 * calculate important netwotk features (geomorphical and topological) for all locations
 * in the DEM.  All the methods in this class are static which makes this class a
 * toolbox that can be extended to include new analysis
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class GetGeomorphologyROM extends Object {
    
    private int GEO[][];
    //private int MD[][];
    //private byte RedRas[][];
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
    private java.io.RandomAccessFile Tcd;
    private java.io.RandomAccessFile Mcd;
    private WorkRectangle M;
    
    int nr,nc;
    
    private hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc;
    
    
    /**
     * Tests for the class
     * @param arguments the command line arguments
     */
    private void inicio(){
        
        try{
            System.out.println(">>> Initializtion Process Starts <<<");
            
            String rutaMR = MR.getLocationBinaryFile().getPath();
            String rutaDatos = rutaMR.substring(0,rutaMR.lastIndexOf("."));
            
            String[] destinations ={".horton",".magn",".ltc",".lcp",".dtopo",".tcd",".mcd"};
                                
            java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
            java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;

            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(rutaDatos+destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }

            float missF=new Float(MR.getMissing()).floatValue();
            int   missI=new Integer(MR.getMissing()).intValue();

            GEO= new int[nr][nc];
            llegan= new int[nr][nc];
            dy = 6378.0*MR.getResLon()*Math.PI/(3600.0*180.0);
            dx = new double[nr+1];
            dxy = new double[nr+1];
            M = new WorkRectangle(0,nr-1,0,nc-1);
            
            for(int i=0; i<nr; i++){
                for(int j=0; j<nc; j++){
                    if(Proc.DIR[i+1][j+1]==0){
                        outs[0].writeByte((byte)-10);
                        outs[1].writeInt(missI);
                        outs[2].writeFloat(missF);
                        outs[3].writeFloat(missF);
                        outs[4].writeInt(missI);
                        outs[5].writeFloat(missF);
                        outs[6].writeFloat(missF);
                    }
                    else{
                        outs[0].writeByte((byte)-1);
                        if(Proc.RedRas[i+1][j+1] == 0) outs[1].writeInt(-1); else outs[1].writeInt(0);
                        outs[2].writeFloat(0.0f);
                        outs[3].writeFloat(0.0f);
                        outs[4].writeInt(0);
                        outs[5].writeFloat(0.0f);
                        outs[6].writeFloat(0.0f);
                    }
                }
            }
            
            for(int k=0 ; k<destinations.length ; k++) buffOuts[k].close() ;

            Orden = new java.io.RandomAccessFile(rutaDatos + ".horton","rw");
            Magn = new java.io.RandomAccessFile(rutaDatos + ".magn","rw");
            Ltc = new java.io.RandomAccessFile(rutaDatos + ".ltc","rw");
            Lcp = new java.io.RandomAccessFile(rutaDatos + ".lcp","rw");
            Dtopo = new java.io.RandomAccessFile(rutaDatos + ".dtopo","rw");
            Tcd = new java.io.RandomAccessFile(rutaDatos + ".tcd","rw");
            Mcd = new java.io.RandomAccessFile(rutaDatos + ".mcd","rw");
            
            for(int i=0; i<nr; i++){
                dx[i] = 6378*Math.cos((i*MR.getResLat()/3600.0 + MR.getMinLat())*Math.PI/180.0)*MR.getResLat()*Math.PI/(3600.0*180.0);
                dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
                for(int j=0; j<nc; j++){
                    int nllegan = 0;
                    for (int k=0; k <= 8; k++){
                        if(M.is_on(i+(k/3)-1,j+(k%3)-1)){
                            if (Proc.DIR[1+i+(k/3)-1][1+j+(k%3)-1]==9-k && Proc.RedRas[1+i+(k/3)-1][1+j+(k%3)-1]>0)
                                nllegan++;
                        }
                    }
                    if(Proc.RedRas[i+1][j+1]<=0)
                        GEO[i][j]= -2;
                    else if (Proc.DIR[i+1][j+1]>0){
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
    /**
     * Creates and instance of the ROM based geomorphology class
     * @param MR1 The MetaRaster of the DEM to be processed
     */
    public GetGeomorphologyROM(hydroScalingAPI.io.MetaRaster MR1, hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule pr) {
        
        MR=MR1;

        Proc=pr;
        
        nr=MR.getNumRows();
        nc=MR.getNumCols();

        inicio();
        int contn;
            
        do{
            contn = 0;
            for(int i=0; i<nr ; i++){
                for (int j=0; j<nc; j++){
                    if (GEO[i][j] < 0)
                        contn++;
                    if (GEO[i][j] == 0){
                        GEOcero(i,j,contn);
                    }//en cada cero
                }//for j
            }//for i
        }while(contn < (GEO.length)*(GEO[0].length) );
        
    }
    
    //---------------------------------------------------------------------------------------------------------------------------
    
    void GEOcero(int i, int j,int contn){
        try{
            double[] dist = {dxy[i],dy,dxy[i],dx[i],1,dx[i],dxy[i],dy,dxy[i]};
            GEO[i][j] = -1;
            esc("Ltc",i,j,true,(float)dist[Proc.DIR[i+1][j+1]-1]);
            esc("Lcp",i,j,true,(float)dist[Proc.DIR[i+1][j+1]-1]);
            if (llegan[i][j] == 0){
                esc("Magn",i,j,false,(int)1);
                esc("Orden",i,j,false,(byte)1);
                esc("Dtopo",i,j,false,(int)1);
            }
            int x1=i-1+(Proc.DIR[i+1][j+1]-1)/3 ; int y1=j-1+(Proc.DIR[i+1][j+1]-1)%3;
            if (GEO[x1][y1] >= 0 ){
                GEO[x1][y1]--  ;
                Magn.seek((long)4*(i*nc+j)); int m2=Magn.readInt();
                Ltc.seek((long)4*(i*nc+j)); float lt2=Ltc.readFloat();
                Lcp.seek((long)4*(i*nc+j)); float lc2=Lcp.readFloat();
                Lcp.seek((long)4*(x1*nc+y1)); float lc3=Lcp.readFloat();
                if (GEO[x1][y1] == 0)
                    esc("Magn", x1,y1,true,m2);
                else 
                    esc("Magn",x1,y1,true,m2);
                esc("Ltc",x1,y1,true,lt2);
                esc("Lcp",x1,y1,false,Math.max(lc2,lc3));
            }
            if(llegan[i][j]> 0){
                java.util.Vector diamt = new java.util.Vector(0,1);
                java.util.Vector ord = new java.util.Vector(0,1);
                for (int k=0; k <= 8; k++){
                    if (Proc.DIR[1+i+(k/3)-1][1+j+(k%3)-1]==9-k && Proc.RedRas[1+i+(k/3)-1][1+j+(k%3)-1]!=0){
                        Orden.seek((i+(k/3)-1)*nc+(j+(k%3)-1));
                        Dtopo.seek(4*((i+(k/3)-1)*nc+(j+(k%3)-1)));
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
                        Proc.RedRas[i+1][j+1]=-1;
                    }
                    else{ esc("Orden",i,j,false,new Byte(ord.get(ord.size()-1).toString()).byteValue());
                    }
                }
                if (ord.size()==1){
                    esc("Orden",i,j,false,new Byte(ord.get(0).toString()).byteValue());
                }
            }
            if (llegan[x1][y1]==1 && Proc.DIR[x1+1][y1+1]>0){
                GEOcero(x1,y1,contn);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
    }
    
    private void esc(String s, int i,int j, boolean m, byte v){
        try{
            if (s.equals("Orden")){
                Orden.seek((long)(i*nc+j));
                if (m){
                    byte d=Orden.readByte();
                    Orden.seek((long)(i*nc+j));
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
                Ltc.seek((long)4*(i*nc+j));
                if (m){
                    float d=Ltc.readFloat();
                    Ltc.seek((long)4*(i*nc+j));
                    Ltc.writeFloat(v+d);
                }
                else  Ltc.writeFloat(v);
            }
            if (s.equals("Lcp")){
                Lcp.seek((long)4*(i*nc+j));
                if (m){
                    float d=Lcp.readFloat();
                    Lcp.seek((long)4*(i*nc+j));
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
                Dtopo.seek((long)4*(i*nc+j));
                if (m){
                    int d=Dtopo.readInt();
                    Dtopo.seek((long)4*(i*nc+j));
                    Dtopo.writeInt(v+d);
                }
                else  Dtopo.writeInt(v);
            }
            if (s.equals("Magn")){
                Magn.seek((long)4*(i*nc+j));
                if (m){
                    int d=Magn.readInt();
                    Magn.seek((long)4*(i*nc+j));
                    Magn.writeInt(v+d);
                }
                else
                    Magn.writeInt(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR3** "+e1.getMessage());}
    }
    
}
