package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author  Jorge Mario Ramirez
 * @version
 */
public abstract class RasterNetworkBlueLines extends Object {
    
    
    static byte[][] Lee_LA(hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc){
        byte[][] LA = new byte[Proc.metaDEM.getNumRows()][Proc.metaDEM.getNumCols()];
        try{
            boolean isMdt = Proc.fileLAzules.getName().lastIndexOf(".dem") != -1;
            boolean isRed = Proc.fileLAzules.getName().lastIndexOf(".redRas") != -1;
            java.io.FileInputStream fileLA = new java.io.FileInputStream(Proc.fileLAzules);
            java.io.BufferedInputStream buffLA = new java.io.BufferedInputStream(fileLA);
            java.io.DataInputStream dataLA = new java.io.DataInputStream(buffLA);
            if(isMdt){
                for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
                    for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                        LA[i][j]= (byte) dataLA.readInt();
                    }
                }
            }
            else if(isRed){
                for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
                    for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                        LA[i][j]= dataLA.readByte();
                    }
                }
            }
        }catch(java.io.IOException e){e.printStackTrace();}
        return LA;
    }
    
    
    static byte[][] Lee_Incisa(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println("INCISANDO");
        byte[][] LA = new byte[Proc.metaDEM.getNumRows()][Proc.metaDEM.getNumCols()];
        Proc.DEMrep = new double[Proc.metaDEM.getNumRows()+2][Proc.metaDEM.getNumCols()+2];
        try{
            boolean isMdt = Proc.fileLAzules.getName().lastIndexOf(".dem") != -1;
            boolean isRed = Proc.fileLAzules.getName().lastIndexOf(".redRas") != -1;
            java.io.FileInputStream fileLA = new java.io.FileInputStream(Proc.fileLAzules);
            java.io.BufferedInputStream buffLA = new java.io.BufferedInputStream(fileLA);
            java.io.DataInputStream dataLA = new java.io.DataInputStream(buffLA);
            if(isMdt){
                for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
                    for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                        LA[i][j]= (byte) dataLA.readInt();
                        Proc.DEMrep[i+1][j+1]=Proc.DEM[i+1][j+1];
                        if(LA[i][j]==1) Proc.DEM[i+1][j+1]=0.001;
                    }
                }
            }
            else if(isRed){
                for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
                    for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                        LA[i][j]= dataLA.readByte();
                        Proc.DEMrep[i+1][j+1]=Proc.DEM[i+1][j+1];
                        if(LA[i][j]==1) Proc.DEM[i+1][j+1]=0.001;
                    }
                }
            }
        }catch(java.io.IOException e){e.printStackTrace();}
        /*int respuesta;
        respuesta = javax.swing.JOptionPane.showConfirmDialog(null,"Desea ver el mapa incisado?");
        if(respuesta ==0){
        javaSIH.widgets.mdtGeomorCheck mdtGc = new javaSIH.widgets.mdtGeomorCheck(Proc.parent,true,Proc.fileLAzules,Proc.metaDEM.formato,Proc.metaDEM.infoAdicional);
        mdtGc.itemSeleccionado=new int[]{1};
        Proc.parent.abrirDEM(mdtGc);
        }*/
        return LA;
    }
    
    static void cabecitas(NetworkExtractionModule Proc, byte[][] Red, boolean incisa){
        if (Proc.printDebug) System.out.println("CABECITAS ");
        Proc.RedRas = new byte[Proc.metaDEM.getNumRows()+2][Proc.metaDEM.getNumCols()+2];
        for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
            for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                int iP = i+1; int jP = j+1;
                if (Red[i][j]>0 && Proc.DIR[iP][jP]>0){
                    boolean llegan=false;
                    int llenos =0;
                    java.util.Vector vac = new java.util.Vector(0,1);
                    loopK:
                        for (int k=0; k <= 8; k++){
                            if (Red[i+(k/3)-1][j+(k%3)-1]>0 && Proc.DIR[iP+(k/3)-1][jP+(k%3)-1]==9-k){
                                llegan=true;
                            }
                            if (!llegan &&  Red[i+(k/3)-1][j+(k%3)-1]==0)
                                vac.addElement(new Integer(k));
                            if(llegan) break loopK;
                        }
                        if(!llegan && vac.size()>5 && vac.size()<=7 && (!incisa || Red[i-1+(Proc.DIR[iP][jP]-1)/3][j-1+(Proc.DIR[iP][jP]-1)%3]>0)){
                            int herrad[][] ={{0,1,3,6,7},{0,1,2,3,5},{1,2,5,7,8},{3,5,6,7,8}};
                            boolean esta=true;
                            LoopG:
                                for(int g1=0; g1<=3; g1++){
                                    esta=true;
                                    for(int g2=0; g2<=4; g2++){
                                        esta = esta && vac.contains(new Integer(herrad[g1][g2]));
                                    }
                                    if (esta) break LoopG;
                                }
                                if(esta && incisa){
                                    persiga(i,j,Proc);
                                }
                                if(esta && !incisa){
                                    solo_persiga(i,j,Proc);
                                }
                        }
                }
            }
        }
        if(incisa){
            for(int i=1; i<Proc.metaDEM.getNumRows()-1; i++){
                for(int j=1; j<Proc.metaDEM.getNumCols()-1; j++){
                    if(Proc.RedRas[i][j]<1) Proc.DEM[i][j]=Proc.DEMrep[i][j];
                }
            }
            Proc.DEMrep = null;
        }
    }
    
    static void persiga(int i, int j, NetworkExtractionModule Proc){
        int iPv = i+1; int jPv = j+1;
        int iPn=iPv,jPn=jPv;
        Proc.DEM[iPv][jPv]=Proc.DEMrep[iPv][jPv];
        do{
            Proc.RedRas[iPv][jPv]=1;
            double min = Double.MAX_VALUE;
            for (int k=0; k <= 8; k++){
                if(Proc.DIR[iPv+(k/3)-1][jPv+(k%3)-1]==9-k){
                    min = Math.min(Proc.DEMrep[iPv+(k/3)-1][jPv+(k%3)-1] , min);
                }
            }
            Proc.DEM[iPv][jPv] = min-0.000001;
            Proc.DEMrep[iPv][jPv] = min-0.000001;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(Proc.DIR[iPn][jPn]>0);
        //System.out.println("sale en "+iPn+" "+jPn);
    }
    
    static void solo_persiga(int i, int j, NetworkExtractionModule Proc){
        int iPv = i+1; int jPv = j+1;
        int iPn=iPv,jPn=jPv;
        do{
            Proc.RedRas[iPv][jPv]=1;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(Proc.DIR[iPn][jPn]>0);
        //System.out.println("sale en "+iPn+" "+jPn);
    }
    
    
}