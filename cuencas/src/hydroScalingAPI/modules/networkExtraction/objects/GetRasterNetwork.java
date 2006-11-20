package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author  Jorge Mario Ramirez
 * @version
 */
public abstract class GetRasterNetwork extends Object {
    
    
    public static void todo_uno(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> All cells are Network");
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.DEM[i][j]>0) Proc.RedRas[i][j]=1;
            }
        }
    }
    
    
    public static void umbral_Area_Ord(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Area-Order Threshold");
        boolean critOrd = (Proc.ordenMax < Integer.MAX_VALUE);
        boolean critArea = (Proc.pixPodado > 0);
        double areaPodado=Float.MAX_VALUE;
        int ordPodado=Integer.MIN_VALUE;
        if (critOrd){
            GetRasterNetwork.todo_uno(Proc);
            GetGeomorphologyRAM.getORD(Proc);
            ordPodado = Proc.maxOrder - Proc.ordenMax;
        }
        if(critArea) areaPodado = Proc.pixPodado*Proc.dy*Proc.dxm;
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if ((Proc.Areas[i][j] > areaPodado) || (critOrd && Proc.GEO[i][j].orden > ordPodado))
                    Proc.RedRas[i][j]=1;
                else
                    Proc.RedRas[i][j]=0;
            }
        }
    }
    
    
    public static void umbral_Area(float pixUmbral, NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Area Threshold");
        double areaPodado = pixUmbral*Proc.dy*Proc.dxm;
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if ((Proc.Areas[i][j] > areaPodado))
                    Proc.RedRas[i][j]=1;
            }
        }
    }
    
    
    public static void laplace2(NetworkExtractionModule Proc, int m){
        if (Proc.printDebug) System.out.println(">>> Calulating Laplacian - 2 "+m);
        for (int i=2; i<Proc.DIR.length-2; i++){
            for (int j=2; j<Proc.DIR[0].length-2; j++){
                int llegan=0;
                if(Proc.RedRas[i][j]>0){
                    for (int k=0; k <= 8; k++){
                        if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k)
                            llegan++;
                    }
                    if(llegan>m) Proc.RedRas[i][j]=1;
                    else Proc.RedRas[i][j] = 0;
                }
                else Proc.RedRas[i][j] = 0;
            }
        }
    }
    
    public static void persiga(NetworkExtractionModule Proc){
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.RedRas[i][j]==1){
                    int iPv=i; int jPv=j;
                    int iPn=i; int jPn=j;
                    do{
                        Proc.RedRas[iPv][jPv]=1;
                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                        iPv=iPn; jPv=jPn;
                    }while(Proc.DIR[iPn][jPn]>0 && Proc.RedRas[iPn][jPn]!=1);
                }
            }
        }
    }
    
    public static void persiga(NetworkExtractionModule Proc, int inii, int inij){
        int iPv=inii; int jPv=inij;
        int iPn=inii; int jPn=inij;
        do{
            Proc.RedRas[iPv][jPv]=1;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(Proc.DIR[iPn][jPn]>0 && Proc.RedRas[iPn][jPn]!=1);
    }
    
    
    public static void persiga(NetworkExtractionModule Proc, int inii, int inij, int endi, int endj){
        int iPv=inii; int jPv=inij;
        int iPn=inii; int jPn=inij;
        do{
            Proc.RedRas[iPv][jPv]=1;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(iPn != endi || jPn != endj);
    }
    
    
    public static void getArea_Pend(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Area-Slope");
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        /*for(int i=0; i<Proc.metaDEM.getNumRows()+2; i++) for (int j=0; j<Proc.metaDEM.getNumCols()+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        } */
        try{
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            boolean changeLink,changeDir;
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            for(int i=2; i<Proc.metaDEM.getNumRows()-2 ; i++){
                for (int j=2; j<Proc.metaDEM.getNumCols()-2; j++){
                    if (Proc.RedRas[i][j]==1){
                        //informacion sobre el punto especifico
                        int myOrder=Proc.GEO[i][j].orden;
                        boolean source=true;
                        //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1){
                                source=false;
                            }
                        }
                        if (source || Proc.GEO[i][j].pcambio){
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            //System.out.println("inicia "+i+" "+j+" "+Proc.DEM[i][j]);
                            do{
                                if (cambieLink){
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                                int lleganAca=0;
                                for (int k=0; k <= 8; k++){
                                    arroundI=iaN+(k/3)-1;
                                    arroundJ=jaN+(k%3)-1;
                                    if (arroundI>0 && arroundI < Proc.metaDEM.getNumRows() && arroundJ>0 && arroundJ < Proc.metaDEM.getNumCols()){
                                        if (Proc.DIR[iaN+(k/3)-1][jaN+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1){
                                            lleganAca++;
                                        }
                                    }
                                }
                                changeLink=lleganAca>1;
                                outOrder=Proc.GEO[iaN][jaN].orden;
                                if (changeLink || outOrder <= 0){
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    
                                    int contllegan=0;
                                    for (int k=0; k <= 8; k++){
                                        if (Proc.DIR[ia+(k/3)-1][ja+(k%3)-1]==9-k){
                                            contllegan++;
                                        }
                                    }
                                    if(contllegan >3)DataAP.writeDouble(-1.0*(double) areaLink);
                                    else DataAP.writeDouble((double) areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    cambieLink=true;
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                        }
                    }
                }
            }
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){System.err.println(e.toString());}
    }
    
    
    
    public static double[][] calculaPromAP(NetworkExtractionModule Proc){
        Valor[] areaPend = new Valor[0];
        double minPend = Double.MAX_VALUE;
        double[][] arregloAP;
        boolean todos=false;
        try{
            if (Proc.printDebug) System.out.println(">>> Reading *.ap");
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.File FileAP = new java.io.File(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap");
            java.io.BufferedInputStream BufAP = new java.io.BufferedInputStream(new java.io.FileInputStream(FileAP));
            java.io.DataInputStream DataAP = new java.io.DataInputStream(BufAP);
            if(Proc.npuntosAP > FileAP.length()/32){
                Object [] tmp = new Object[]{"Accept"};
                javax.swing.JOptionPane.showOptionDialog((java.awt.Component)Proc.parent, "Minimum number of points to get the average is " + (FileAP.length()/32)+", this value will be used for calculations" , "Error",javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null,tmp,tmp[0]);
                Proc.npuntosAP = (int) FileAP.length()/32;
                todos = true;
            }
            if(Proc.npuntosAP <=0) todos = true;
            areaPend = new Valor[(int) FileAP.length()/16];
            for(int k=0; k<areaPend.length; k++){
                areaPend[k]=new Valor(DataAP.readDouble(),DataAP.readDouble());
                if(areaPend[k].pendiente >0)
                    minPend = Math.min(areaPend[k].pendiente,minPend);
            }
        }catch(java.io.IOException e){e.printStackTrace(); System.exit(0);}
        
        if(!todos){
            java.util.Arrays.sort(areaPend);
            double areaProm=0; double pendProm=0;
            int nValores = areaPend.length/Proc.npuntosAP;
            if (Proc.printDebug) {
                System.out.println("areaPend.length "+areaPend.length);
                System.out.println("(Proc.npuntosAP-1) "+(Proc.npuntosAP-1));
                System.out.println("nValores "+nValores);
            }
            arregloAP = new double[2][nValores];
            int contK =0; boolean cambio_signo = false;
            int cont_neg=0;
            for(int k=0; k < areaPend.length-(areaPend.length%nValores); k++){
                if(areaPend[k].area > 0){
                    areaProm += areaPend[k].area;
                }
                else{
                    areaProm += areaPend[k].area;
                    cont_neg++;
                    if(!cambio_signo && areaPend[k+1].area > 0)
                        cambio_signo=true;
                }
                pendProm += areaPend[k].pendiente;
                if((k+1) % Proc.npuntosAP == 0 || cambio_signo){
                    try{
                        if(cambio_signo){
                            if((k+1) % Proc.npuntosAP != 0){ //Promedio en el lado negativo
                                arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/(cont_neg%Proc.npuntosAP));
                                arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/(cont_neg%Proc.npuntosAP));
                                k += (Proc.npuntosAP - cont_neg%Proc.npuntosAP);
                            }
                            else{
                                arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/Proc.npuntosAP);
                                arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/Proc.npuntosAP);
                            }
                            cambio_signo = false;
                        }
                        else{
                            arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/Proc.npuntosAP);
                            arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/Proc.npuntosAP);
                        }
                    }catch(ArrayIndexOutOfBoundsException e){System.err.println("ArrayIndex "+ contK);}
                    contK++;
                    areaProm=0; pendProm=0;
                }
            }
            //System.exit(0);
        }
        else{
            java.util.Arrays.sort(areaPend);
            arregloAP = new double[2][areaPend.length];
            for(int k=0; k<areaPend.length; k++){
                if(areaPend[k].area < 0) arregloAP[0][k] = -1.0*Math.log(-1.0*areaPend[k].area);
                else arregloAP[0][k] = Math.log(areaPend[k].area);
                arregloAP[1][k] = Math.log(Math.max(areaPend[k].pendiente,minPend));
            }
        }
        return arregloAP;
    }
    
    
    public static void getArea_Pend_Dif(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Area-Slope Relation over Blue Lines");
        try{
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            boolean changeLink,changeDir;
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            for(int i=2; i<Proc.metaDEM.getNumRows()-2 ; i++){
                for (int j=2; j<Proc.metaDEM.getNumCols()-2; j++){
                    if (Proc.DIR[i][j]>0){
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
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            //System.out.println("inicia "+i+" "+j+" "+Proc.DEM[i][j]);
                            do{
                                if (cambieLink){
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
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
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    /*A los que son de la red se les pone el �rea negativa para colocarles otro color en GraficaAP */
                                    //if(Proc.DEM[iaN][jaN]>2400){
                                    if(Proc.RedRas[iaN][jaN]==1 && Proc.RedRas[ia][ja]==1) DataAP.writeDouble((double)-1.0 * areaLink);
                                    else DataAP.writeDouble((double)areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    //}
                                    cambieLink=true;
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                        }
                    }
                }
            }
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){System.err.println(e.toString());}
    }
    
    
    public static double[][] getAP_Puntos(NetworkExtractionModule Proc, int c){
        java.util.Vector vectorAP = new java.util.Vector();
        for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
            for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                if (Proc.RedRas[i][j]>0 && Proc.DIR[i][j]>0){
                    boolean llegan=false;
                    int llenos =0;
                    java.util.Vector vac = new java.util.Vector(0,1);
                    loopK:
                        for (int k=0; k <= 8; k++){
                            if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                llegan=true;
                            }
                            if (!llegan &&  Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==0)
                                vac.addElement(new Integer(k));
                            if(llegan) break loopK;
                        }
                        if(!llegan && vac.size()>5 && vac.size()<=7){
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
                                if(esta){
                                    int cont=0;
                                    int iPv = i; int jPv = j;
                                    int iPn=iPv,jPn=jPv;
                                    do{
                                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                                        cont++;
                                        iPv=iPn; jPv=jPn;
                                        if(!(iPv>1 && iPv<Proc.metaDEM.getNumRows() && jPv>1 && jPv<Proc.metaDEM.getNumCols()))
                                            cont = c;
                                    }while(cont < c);
                                    //System.exit(0);
                                    if(iPv>1 && iPv<Proc.metaDEM.getNumRows() && jPv>1 && jPv<Proc.metaDEM.getNumCols()){
                                        //if(Proc.Areas[iPv][jPv]>0 && Proc.MaxPend[iPv][jPv]>0){
                                        vectorAP.add(new Valor((double) 1000000*Proc.Areas[iPv][jPv],Proc.MaxPend[iPv][jPv]));
                                    }
                                    //System.out.println(iPv+" "+jPv+" "+Proc.Areas[iPv][jPv]+" "+Proc.MaxPend[iPv][jPv]);
                                }
                        }
                }
            }
        }
        double dataAP[][] = new double[2][vectorAP.size()];
        for(int k=0; k<vectorAP.size(); k++){
            dataAP[0][k]= Math.log(((Valor) vectorAP.get(k)).area);
            dataAP[1][k]= Math.log(((Valor) vectorAP.get(k)).pendiente);
        }
        return dataAP;
    }
    
    
    public static double[][] getAP_Fuentes(NetworkExtractionModule Proc){
        java.util.Vector vectorAP = new java.util.Vector();
        for(int i=0; i<Proc.metaDEM.getNumRows(); i++){
            for(int j=0; j<Proc.metaDEM.getNumCols(); j++){
                if (Proc.RedRas[i][j]>0 && Proc.DIR[i][j]>0){
                    boolean llegan=false;
                    int llenos =0;
                    java.util.Vector vac = new java.util.Vector(0,1);
                    loopK:
                        for (int k=0; k <= 8; k++){
                            if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                llegan=true;
                            }
                            if (!llegan &&  Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==0)
                                vac.addElement(new Integer(k));
                            if(llegan) break loopK;
                        }
                        if(!llegan && vac.size()>5 && vac.size()<=7){
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
                                if(esta){
                                    boolean salio = false;
                                    int iPv = i , jPv = j, iPn = iPv , jPn = jPv;
                                    int iConv = -1, jConv=-1, contlleganRed=0;
                                    double distFuente =0.0; double distFuenteConv =0.0;
                                    do{
                                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                                        int contllegan =0;
                                        for (int k=0; k <= 8; k++){
                                            if (Proc.DIR[iPn+(k/3)-1][jPn+(k%3)-1]==9-k){
                                                contllegan++;
                                            }
                                        }
                                        if(iConv < 0 && contllegan>=3){
                                            iConv=iPv;
                                            jConv = jPv;
                                        }
                                        double[] dist = {Proc.dxy[iPv],Proc.dy,Proc.dxy[iPv],Proc.dx[iPv],1,Proc.dx[iPv],Proc.dxy[iPv],Proc.dy,Proc.dxy[iPv]};
                                        distFuente += (dist[Proc.DIR[iPv][jPv]-1])*1000;
                                        if(iConv>=0) distFuenteConv += (dist[Proc.DIR[iPv][jPv]-1])*1000;
                                        
                                        contlleganRed=0;
                                        for (int k=0; k <= 8; k++){
                                            if (Proc.DIR[iPv+(k/3)-1][jPv+(k%3)-1]==9-k && Proc.RedRas[iPv+(k/3)-1][jPv+(k%3)-1]==1){
                                                contlleganRed++;
                                            }
                                        }
                                        if(!(iPn>1 && iPn<Proc.metaDEM.getNumRows() && jPn>1 && jPn<Proc.metaDEM.getNumCols()))
                                            salio=true;
                                        iPv=iPn; jPv=jPn;
                                    }while(contlleganRed<=1 && !salio);
                                    if(iConv < 0){ iConv=i; jConv=j; distFuenteConv = distFuente; }
                                    if(iPv>1 && iPv<Proc.metaDEM.getNumRows() && jPv>1 && jPv<Proc.metaDEM.getNumCols()){
                                        vectorAP.add(new Valor((double) 1000000*Proc.Areas[iConv][jConv],(Proc.DEM[iConv][jConv]-Proc.DEM[iPn][jPn])/distFuenteConv));
                                    }
                                }
                        }
                }
            }
        }
        double dataAP[][] = new double[2][vectorAP.size()];
        for(int k=0; k<vectorAP.size(); k++){
            dataAP[0][k]= Math.log(((Valor) vectorAP.get(k)).area);
            dataAP[1][k]= Math.log(((Valor) vectorAP.get(k)).pendiente);
        }
        return dataAP;
    }
    
    
    public static void umbralASalfa(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println("UMBRAL ASalfa>C");
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        for(int i=0; i<Proc.metaDEM.getNumRows()+2; i++) for (int j=0; j<Proc.metaDEM.getNumCols()+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        }
        boolean changeLink,changeDir;
        int ia, ja, iaN, jaN, arroundI, arroundJ;
        double areaPodado =0;
        java.util.Vector fuentes = new java.util.Vector();
        for(int i=2; i<Proc.metaDEM.getNumRows()-2 ; i++){
            for (int j=2; j<Proc.metaDEM.getNumCols()-2; j++){
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
                        ia=i;
                        ja=j;
                        int outOrder=0;
                        boolean cambieLink=false;
                        double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                        double areaLink=0;
                        do{
                            if (cambieLink){
                                cambieLink=false;
                                distLink=0;
                                cotaini=Proc.DEM[ia][ja];
                            }
                            iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                            jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                            double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                            distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
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
                                cotafin=Proc.DEM[iaN][jaN];
                                areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                areaPodado = Proc.pixPodado*Proc.dy*Proc.dx[iaN]*1000000;
                                //CRITERIO ASalfa >= C :
                                if(areaLink >= areaPodado && areaLink*Math.pow((-cotafin+cotaini)/distLink,(double)Proc.alfa) >= (double)Proc.C){
                                    fuentes.add(new Integer(ia*Proc.metaDEM.getNumCols() + ja));
                                    //persiga(Proc,ia,ja,iaN,jaN);
                                }
                                cambieLink=true;
                            }
                            ia=iaN; ja=jaN;
                        } while (myOrder == outOrder);
                    }
                }
            }
        }
        Proc.RedRas = new byte[Proc.DIR.length][Proc.DIR[0].length];
        if(fuentes.size()>0){
            for(int k=0; k<fuentes.size(); k++){
                int valor = new Integer(fuentes.get(k).toString()).intValue();
                persiga(Proc,valor/Proc.metaDEM.getNumCols(),valor%Proc.metaDEM.getNumCols());
            }
        }
        
    }
    
    
    public static void probabCh_Ini(NetworkExtractionModule Proc){
        //Constantes
        double densAgua =1, densSuelo = 2.6;
        double gravedad = 9.8;
        double m1 = 0.6, n1 = 0.7;
        double tao_est = 0.045, k = 0.0474 , p = 1/6;
        double r_min = 40, r_max = 55;
        double na_min = 0.015, na_max = 0.1;
        double d50_media = 2, d50_de = 0.48;
        double tao_c, nb, d50, r, na;
        //double C[][] = new double[Proc.metaDEM.getNumRows()][Proc.metaDEM.getNumCols()];
        java.util.Random random_r = new java.util.Random(10001313);
        java.util.Random random_na = new java.util.Random(10401313);
        java.util.Random random_d50 = new java.util.Random(10601313);
        //for(int i=1; i<=Proc.metaDEM.getNumRows() ; i++){
        //for (int j=1; j<=Proc.metaDEM.getNumCols(); j++){
        for(int i=50; i<=55; i++){
            for (int j=50; j<=55; j++){
                r = (1d/(1000*24*3600d))*((random_r.nextDouble()*(r_max-r_min))+r_min);
                if (Proc.printDebug) System.out.println("r "+r);
                na = (random_na.nextDouble()*(na_max-na_min))+na_min;
                if (Proc.printDebug) System.out.println("na "+na);
                d50 = Math.exp(Math.log(d50_media)+((random_d50.nextGaussian())*Math.log(d50_de)));
                if (Proc.printDebug) System.out.println("d50 "+d50);
                tao_c = tao_est*gravedad*(densSuelo-densAgua)*d50;
                if (Proc.printDebug) System.out.println("tao_c "+tao_c);
                nb = k*Math.pow(d50/1000,p);
                if (Proc.printDebug) System.out.println("nb "+nb);
                Proc.DEM[i][j] = Math.pow(tao_c/(densAgua*gravedad*Math.pow(nb,1.5)*Math.pow(nb+na,-0.9)*Math.pow(r,m1)),1/m1);
                if (Proc.printDebug) System.out.println("C "+Proc.DEM[i][j]);
                if (Proc.printDebug) System.out.println("-----------------------------------");
            }
        }
        System.exit(0);
    }
    
}

class Valor extends Object implements Comparable{
    public double area,pendiente;
    
    Valor(double a,double p){
        area = a;
        pendiente = p;
    }
    
    public int compareTo(java.lang.Object val) {
        return (new Double(area)).compareTo(new Double(((Valor)val).area));
    }
}