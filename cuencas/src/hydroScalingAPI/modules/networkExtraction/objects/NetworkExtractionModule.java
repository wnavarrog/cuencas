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
 *
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class NetworkExtractionModule implements Runnable {
    
    public hydroScalingAPI.io.MetaRaster metaDEM;
    public double[][] DEM;
    public double[][] DEMrep;
    public int[][] DIR;
    public double dy,dxm;
    public double[] dx;
    public double[] dxy;
    public hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle MT;
    public java.util.Vector Sink_t;
    public int sinkOrder=0;
    public int maxOrder=0;
    public boolean firstCorrection = true;
    public hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions OpProc;
    public hydroScalingAPI.mainGUI.ParentGUI parent;
    public float[][] Areas;
    public double[][] MaxPend;
    public byte RedRas[][];
    public byte LA[][];
    public GeomorphCell_1 GEO[][];
    public GeomorphCell_2 GEO2[][];
    public boolean debug;
    public boolean convergenceAlarmType1;
    public int convergenceAlarmCounter=0;
    
    //Para las Opciones
    public float    cCorte = 10f, 
                    cAltura = 20f,
                    pixManual=Float.POSITIVE_INFINITY,
                    pixCManual=Float.POSITIVE_INFINITY,
                    latManual=-100,
                    lonManual=-100,
                    pixPodado=0.01f,
                    alfa = 0, 
                    C = 0, 
                    pixAP = 0;
    
    public int  unPixM=0,
                unPixCM=0,
                unFilaM=1,
                unColM=0,
                filaManual=-100,
                colManual=-100,
                ordenMax=Integer.MAX_VALUE,
                unPixPod=0, 
                unPixAP=0, 
                npuntosAP=6, 
                nCeldasConv=3;
    
    public boolean  archPro=false,
                    lAzules=false, 
                    areaPend_nuevo=false, 
                    areaPend_cargar=false, 
                    areaPend_LA = false,
                    laplace = true, 
                    todoRed = true, 
                    umbralAP = false, 
                    printDebug = true;
    
    public boolean  taskDIR=true, 
                    taskRED=true, 
                    taskGEO=true, 
                    taskVECT=true;
    
    public java.io.File fileLAzules;
    
    public java.io.OutputStreamWriter newfile;
    
    public hydroScalingAPI.tools.Stats DEMstats;
    
    public NetworkExtractionModule(hydroScalingAPI.mainGUI.ParentGUI parent1, hydroScalingAPI.io.MetaRaster metaDEM1, hydroScalingAPI.io.DataRaster dataDEM1) {
        metaDEM=metaDEM1;
        parent=parent1;
        inicio(dataDEM1);
        OpProc = new hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions(this);
        OpProc.setVisible(true);
    }
    
    public NetworkExtractionModule(hydroScalingAPI.io.MetaRaster metaDEM1, hydroScalingAPI.io.DataRaster dataDEM1) {
        //printDebug=false;
        printDebug=true;
        metaDEM=metaDEM1;
        inicio(dataDEM1);
        OpProc = new hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions(this);
        //taskDIR=true; taskRED=true; taskGEO=false; taskVECT=false;
        taskDIR=false; taskRED=false; taskGEO=true; taskVECT=true;
        corrigeDEM();
        OpProc.dispose();
    }
    
    public void inicio(hydroScalingAPI.io.DataRaster dataDEM){
        int ncol = metaDEM.getNumCols();
        int nfila = metaDEM.getNumRows();
        
        /*Se dimensiona las matrices maxpend[][], MD[][] y DEMC[][] con el numero de filas
         y columnas del DEM original sumandole una fila y una columna para la mascara.*/
        
        double[][] tempDEM = dataDEM.getDouble();
        
        DEM = new double[nfila+2][ncol+2];
        DIR = new int[nfila+2][ncol+2];

        //Se colocan -1 en la primera fila y columna (i=0, j=0) y en las ultimas (i=nfila+1, j=ncol+1).
        for (int i=0; i<nfila+2; i++){
            DEM[i][0]=-1 ; DEM[i][ncol+1]=-1;
            DIR[i][0]=-1 ; DIR[i][ncol+1]=-1;
        }
        for (int i=0; i<ncol+2; i++){
            DEM[0][i]=-1 ; DEM[nfila+1][i]=-1;
            DIR[0][i]=-1 ; DIR[nfila+1][i]=-1;
        }
        
        for (int i=1; i<=nfila; i++){
            for (int j=1; j<=ncol ;j++){
                if (tempDEM[i-1][j-1] == new Double(metaDEM.getMissing()).doubleValue() || tempDEM[i-1][j-1]<=0)
                    DEM[i][j] = -1;
                else
                    DEM[i][j] = tempDEM[i-1][j-1];
            }
        }
        
        tempDEM = null;
        
        DEMstats=new hydroScalingAPI.tools.Stats(DEM,-1.0);

        dy = 6378.0*metaDEM.getResLat()*Math.PI/(3600.0*180.0);
        dx = new double[nfila+1];
        dxy = new double[nfila+1];
        
        /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel
          y la diagonal, dependiendo de la latitud.*/
        for (int i=1 ; i<=nfila ; i++){
            dx[i] = 6378.0*Math.cos((i*metaDEM.getResLat()/3600.0 + metaDEM.getMinLat())*Math.PI/180.0)*metaDEM.getResLat()*Math.PI/(3600.0*180.0);
            dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
        }
        
        dxm=dx[nfila/2];
        MT = new WorkRectangle(2,nfila-1,2,ncol-1,this);
    }
    
    public void corrigeDEM(){

        java.util.Calendar iniTime=java.util.Calendar.getInstance();
        if (printDebug) System.out.println(">>> Running fixDEM()"+" - Initial Time: "+iniTime.getTime());
        if(!firstCorrection){ leeDEM(); if (printDebug) System.out.println(">>> Running readDEM()");}
        if(taskRED && lAzules){
            LA = RasterNetworkBlueLines.Lee_Incisa(this);
            OpProc.buscandoPits();
        }
        if(taskDIR || (taskRED && lAzules)){
            do{
                corrigeUniPitFullDEM();
                OpProc.buscandoPits();
                Sink_t = new java.util.Vector();
                convergenceAlarmType1=false;
                MT.corrigeWorkRectangle(OpProc.myFSc);
                corrigeSink_t();

                MT = new WorkRectangle(2,metaDEM.getNumRows()-1,2,metaDEM.getNumCols()-1,this);

                convergenceAlarmCounter++;
                
                if(convergenceAlarmCounter > 20){
                    writeCorrDem(metaDEM.getLocationBinaryFile());
                    System.exit(0);
                    OpProc.set_ready();
                    OpProc.announceFailure();
                    return;
                }
            } while(convergenceAlarmType1);
            
            MT.direcciones(-10.0);
            if (printDebug) System.out.println(">>> Final Pit Verification PASS");
            Sink_t = new java.util.Vector();
            MT.corrigeWorkRectangle(OpProc.myFSc);
            corrigeSink_t();
            //Scan the matrix for crossing (This should not occur and it needs to be address in the algorithm itself)
            int countSituation=0;
            
            countSituation=0;
            for (int i=1; i<DIR.length-1; i++){
                for (int j=1; j<DIR[0].length-1; j++){
                    int rnum=(int)Math.floor(2*Math.random());
                    if(DIR[i][j] == 9 && DIR[i][j+1] == 7) {
                        DIR[i][j+rnum]=8;
                        countSituation++;
                    }
                    //System.out.println(">>>> CHANGE MADE AT x:"+(j-1)+" y:"+(i-1));
                    if(DIR[i][j] == 3 && DIR[i][j+1] == 1) {
                        DIR[i][j+rnum]=2;
                        countSituation++;
                    }

                    if(DIR[i][j] == 7 && DIR[i+1][j] == 1) {
                        DIR[i+rnum][j]=4;
                        countSituation++;
                    }
                    if(DIR[i][j] == 9 && DIR[i+1][j] == 3) {
                        DIR[i+rnum][j]=6;
                        countSituation++;
                    }
                }
            }
            System.out.println(">>> Crossings Fixed: "+countSituation);
            

            if (printDebug) System.out.println(">>> Done With Sink Correction");
            
            writeCorrDem(metaDEM.getLocationBinaryFile());
            
            GetGeomorphologyRAM.getAreas(this);
            GetGeomorphologyRAM.bordes(this);
            if(taskDIR && !lAzules){
                GetGeomorphologyRAM.getMaxPend(this);
                writePREL(metaDEM.getLocationBinaryFile());
            }
        }
        
        if(!taskDIR) leeDEM_DIR_AREA_PEND();
        
        RedRas = new byte[DIR.length][DIR[0].length];
        
        if(taskRED){
            if(todoRed) GetRasterNetwork.todo_uno(this);
            boolean red_umbrales = (ordenMax < Integer.MAX_VALUE || pixPodado > 0);
            if(lAzules){
                RasterNetworkBlueLines.cabecitas(this,LA,true);
                GetGeomorphologyRAM.getMaxPend(this);
                writePREL(metaDEM.getLocationBinaryFile());
            }
            if(red_umbrales){
                GetRasterNetwork.umbral_Area_Ord(this);
            }
            if(umbralAP){
                GetGeomorphologyRAM.getORD(this);
                GetRasterNetwork.umbralASalfa(this);
            }
            if(laplace && !areaPend_nuevo){
                GetRasterNetwork.laplace2(this,nCeldasConv);
                GetRasterNetwork.persiga(this);
            }
            
            if(areaPend_nuevo){
                GetRasterNetwork.umbral_Area(pixAP,this);
                GetGeomorphologyRAM.getORD(this);
                GetRasterNetwork.getArea_Pend(this);
                OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
            }
            if(areaPend_cargar) OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
            if(areaPend_LA){
                boolean soloPuntos = false;
                if(!soloPuntos){
                    GetRasterNetwork.todo_uno(this);
                    GetGeomorphologyRAM.getORD(this);
                }
                LA = RasterNetworkBlueLines.Lee_LA(this);
                RasterNetworkBlueLines.cabecitas(this,LA,false);
                if(!soloPuntos){
                    GetRasterNetwork.getArea_Pend_Dif(this);
                    OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
                }
            }
            if(!areaPend_cargar && !areaPend_nuevo && !areaPend_LA )
                writeRED(metaDEM.getLocationBinaryFile());
        }
        
        if(!taskRED) leeRED();
        
        if(taskGEO){
            if(!archPro){
                GetGeomorphologyRAM.getDistanceToBorder(this);
                System.gc();
                GetGeomorphologyRAM.getGEO(this);
                writeGEO(metaDEM.getLocationBinaryFile());
                GetGeomorphologyRAM.getRedVect(this);
            }else{
                if (printDebug) System.out.println(">>> Calculating Geormorphology - ROM based Algorithm");
                GetGeomorphologyRAM.getDistanceToBorder(this);
                System.gc();
                new hydroScalingAPI.modules.networkExtraction.objects.GetGeomorphologyROM(metaDEM);
                
                
                if (printDebug) System.out.println(">>> Geormorphology Completed");
                GetGeomorphologyRAM.getRedVect(this);
            }
        }
        
        java.util.Calendar finalTime=java.util.Calendar.getInstance();
        if (printDebug) {
            System.out.println(">>> DONE <<<"+" - Final Time: "+finalTime.getTime());
            System.out.println(">>> DEM Characteristics : ");
            System.out.println(">>> DEM Size : Ncols : "+metaDEM.getNumCols()+", Nrows : "+metaDEM.getNumRows());
            System.out.println(">>> Min Height : "+DEMstats.minValue+", Max Height : "+DEMstats.maxValue);
            System.out.println(">>> Heights Average : "+DEMstats.meanValue+", Height Std. Deviation : "+DEMstats.standardDeviation);
            System.out.println(">>> Running Time is "+(finalTime.getTimeInMillis()-iniTime.getTimeInMillis())/1000.);
        }
        firstCorrection = false;
        OpProc.set_ready();
    }
    
    
    public void corrigeSink_t(){
        int next_pit = get_nextPit();
        
        if (Sink_t.size() != 0) OpProc.setMaxMinExtractionBar((int)((Pit) Sink_t.lastElement()).cota, (int)((Pit) Sink_t.firstElement()).cota);
        
        Pit Sn;
        
        while(next_pit!=-1){
            Sn=(Pit) Sink_t.get(next_pit);
            Sn.corrigePit(this);
            if(convergenceAlarmType1) {
                return;
            }
            next_pit = get_nextPit();
        }
        
        if (Sink_t.size() != 0) OpProc.setValueExtractionBar((int)((Pit) Sink_t.lastElement()).cota);
        
    }
    
    public int get_nextPit(){
        
        if (Sink_t.size() == 0) return -1;
            
        java.util.Collections.sort(Sink_t);
        
        Pit Sn = (Pit) Sink_t.get(0);
        boolean sale = true;
        int n;
        for(n=0 ; n<Sink_t.size(); n++){
            Sn = (Pit) Sink_t.get(n);
            if(!Sn.corregido){
                return n;
            }
        }
        return -1;
    }
    
    
    public void corrigeUniPitFullDEM(){
        
        if (printDebug) System.out.println("    >>> General UniPit Method" );
        
        int ncol = metaDEM.getNumCols();
        int nfila = metaDEM.getNumRows();

        double min_ady = Double.MAX_VALUE;
        int contAltas = 0;
        int contPlanas=0;
        int contFalt=0;

        for (int i=1; i<=nfila ; i++){
            for (int j=1; j<=ncol; j++){
                min_ady = Double.MAX_VALUE;
                contAltas = 0;
                contPlanas=0;
                contFalt=0;
                
                /*Se busca en las ocho celdas adyacentes y se cuenta con cont el numero de ellas
                 que tienen cota corregida mayor que la de la celda (i,j). Se actualiza el minimo
                 adyacente de (i,j).*/
                
                for (int k=0; k<=8; k++){
                    if (DEM[i+(k/3)-1][j+(k%3)-1]>=0 && k!=4)
                        min_ady = Math.min(min_ady,DEM[i+(k/3)-1][j+(k%3)-1]);
                    if(DEM[i+(k/3)-1][j+(k%3)-1]>DEM[i][j])
                        contAltas++;
                    if(DEM[i+(k/3)-1][j+(k%3)-1]==DEM[i][j])
                        contPlanas++;
                    if(DEM[i+(k/3)-1][j+(k%3)-1]<0)
                        contFalt++;
                }
                /*Si las ocho celdas adyacentes a (i,j) son m�s altas que ella (cont=8), se le asigna
                  la cota de la m�nima adyacente, y queda corregido el pit deuna sola celda.*/
                if (contAltas == 8)
                    DEM[i][j] = min_ady;
                //Esto es para que no hayan zonas planas ni sumideros que toquen los bordes
                //Las celdas que estan en los bordes se ponen mas bajas que la minima adyacente 
                if(DEM[i][j]>0 && contFalt>0){
                    if(contPlanas+contFalt == 9)
                        DEM[i][j]=min_ady*0.9;
                    else{
                        double min_ady_interior = Double.MAX_VALUE;
                        for (int k=0; k<=8; k++){                    
                            if (DEM[i+(k/3)-1][j+(k%3)-1]>0){ 
                                int i2 = i+(k/3)-1; int j2 = j+(k%3)-1;
                                boolean tocaFalt = false;
                                int k2=0;
                                do{
                                    tocaFalt = (DEM[i2+(k2/3)-1][j2+(k2%3)-1]<=0); 
                                    k2++;
                                }while(!tocaFalt && k2<=8);
                                if (!tocaFalt ){
                                    min_ady_interior = Math.min(min_ady_interior,DEM[i2][j2]);
                                }                            
                            }
                        }
                        if(min_ady_interior < Double.MAX_VALUE)
                            DEM[i][j]=min_ady_interior*0.9;
                    }
                }
            }
        }
        if (printDebug) System.out.println("    >>> Done with UniPit Method" );

    }//metodo corrUnipit
    
    
    //Calcula el incremento diferencial para la correccion de zonas planas.
    public double calculaIncr(java.util.Vector F){

        int max_M=0;
        double min_dif_front = 1000.0;
        for(int n=0; n<F.size(); n++){
            Pit Fn = (Pit)F.get(n);
            max_M = Math.max(max_M, Math.max(Fn.Mp.end_j-Fn.Mp.ini_j,Fn.Mp.end_i-Fn.Mp.ini_i));
            if (max_M==0) max_M=2;
            for(int i=Fn.Mp.ini_i; i<=Fn.Mp.end_i; i++){
                for(int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                    if(DIR[i][j]==11 + Fn.grupo*100){
                        for(int k=0; k<= 8; k++){
                            if (DEM[i+(k/3)-1][j+(k%3)-1] > DEM[i][j] )
                                min_dif_front = Math.min(min_dif_front,DEM[i+(k/3)-1][j+(k%3)-1]-DEM[i][j]) ;
                        }
                    }
                }
            }
        }

        return Math.max(Math.pow(10.0,Math.floor(Math.log(min_dif_front/max_M)/2.30258)- 1 ),1E-10);

    }//Metodo calc_incr
    
    public void writePREL(java.io.File arch){
        String path = arch.getPath();
        if (printDebug) System.out.println(">>> Writing To :"+arch.getParent());
        String[] destinations ={path.substring(0, path.lastIndexOf(".")) + ".areas",
                                path.substring(0, path.lastIndexOf(".")) + ".corrDEM",
                                path.substring(0, path.lastIndexOf(".")) + ".slope",
                                path.substring(0, path.lastIndexOf(".")) + ".dir"};
                                
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            for (int i=1; i <= metaDEM.getNumRows(); i++) for (int j=1; j <= metaDEM.getNumCols() ;j++){
                if(DEM[i][j]==-1.0){
                    outs[0].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                    outs[1].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                    outs[2].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                    outs[3].writeByte(-10);
                }
                else{
                    outs[0].writeFloat(Areas[i][j]);
                    outs[1].writeDouble(DEM[i][j]);
                    outs[2].writeDouble(MaxPend[i][j]);
                    outs[3].writeByte((byte)DIR[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){
            System.err.println(e1.toString());
        }
    }//metodo writePREL
    
    public void writeCorrDem(java.io.File arch){
        String path = arch.getPath();
        if (printDebug) System.out.println(">>> Writing To :"+arch.getParent());
        String[] destinations ={ path.substring(0, path.lastIndexOf(".")) + ".corrDEM"};
                                
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            for (int i=1; i <= metaDEM.getNumRows(); i++) for (int j=1; j <= metaDEM.getNumCols() ;j++){
                if(DEM[i][j]==-1.0){
                    outs[0].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                }
                else{
                    outs[0].writeDouble(DEM[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){
            System.err.println(e1.toString());
        }
    }//metodo writeCorrDem
    
    public void writeRED(java.io.File arch){
        if (printDebug) System.out.println(">>> Writing Raster Network");
        String path = arch.getPath();
        String[] destinations ={path.substring(0, path.lastIndexOf(".")) + ".redRas"};
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            for (int i=1; i <= metaDEM.getNumRows(); i++) for (int j=1; j <= metaDEM.getNumCols() ;j++){
                if (DIR[i][j]==0){
                    outs[0].writeByte(-10);
                }
                else{
                    outs[0].writeByte(RedRas[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){System.err.println(e1.toString());}
    }//metodo writeRED
    
    
    
    public void writeGEO(java.io.File arch){
        String path = arch.getPath();
        String[] destinations ={
            path.substring(0, path.lastIndexOf(".")) + ".redRas",
            path.substring(0, path.lastIndexOf(".")) + ".ltc",
            path.substring(0, path.lastIndexOf(".")) + ".lcp",
            path.substring(0, path.lastIndexOf(".")) + ".horton",
            path.substring(0, path.lastIndexOf(".")) + ".magn",
            path.substring(0, path.lastIndexOf(".")) + ".dtopo",
            path.substring(0, path.lastIndexOf(".")) + ".tcd",
            path.substring(0, path.lastIndexOf(".")) + ".mcd",};
            
            java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
            java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
            
            try{
                for(int k=0 ; k<destinations.length ; k++){
                    buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                    outs[k] = new java.io.DataOutputStream(buffOuts[k]);
                }
                for (int i=1; i <= metaDEM.getNumRows(); i++) for (int j=1; j <= metaDEM.getNumCols() ;j++){
                    if (DIR[i][j]==0){
                        outs[0].writeByte(-10);
                        outs[1].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[2].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[3].writeByte(-10);
                        outs[4].writeInt(new Integer(metaDEM.getMissing()).intValue());
                        outs[5].writeInt(new Integer(metaDEM.getMissing()).intValue());
                        outs[6].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[7].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                    }
                    else{
                        outs[0].writeByte(RedRas[i][j]);
                        if(RedRas[i][j] >0){
                            outs[1].writeFloat((float)GEO2[i][j].ltc);
                            outs[2].writeFloat((float)GEO2[i][j].lcp);
                            outs[3].writeByte(GEO[i][j].orden);
                            outs[4].writeInt(GEO2[i][j].magn);
                            outs[5].writeInt(GEO2[i][j].d_topo);
                            outs[6].writeFloat((float)GEO2[i][j].tcd);
                            outs[7].writeFloat((float)GEO2[i][j].mcd);
                        } else {
                            outs[1].writeFloat(-1.0f);
                            outs[2].writeFloat(-1.0f);
                            outs[3].writeByte(-1);
                            outs[4].writeInt(-1);
                            outs[5].writeInt(-1);
                            outs[6].writeFloat(-1.0f);
                            outs[7].writeFloat(-1.0f);
                        }
                    }
                }
                for(int k=0 ; k<destinations.length ; k++)
                    buffOuts[k].close() ;
            }catch(java.io.IOException e1){System.err.println(e1.toString());}
            
    }//metodo writeGeomorphCell_1
    
    public void leeDEM_DIR_AREA_PEND(){
        if (printDebug) System.out.println(">>> Reading DEM & DIR & AREA & SLOPES");
        Areas = new float[DIR.length][DIR[0].length];
        MaxPend = new double[DIR.length][DIR[0].length];
        try{
            java.io.FileInputStream files[] = new java.io.FileInputStream[4];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[4];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[4];
            String exts[] = {".corrDEM",".dir",".areas",".slope"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            for(int k=0; k<4; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            for (int i=1; i <= metaDEM.getNumRows(); i++){
                for (int j=1; j <= metaDEM.getNumCols() ;j++){
                    DEM[i][j] = datas[0].readDouble();
                    DIR[i][j] = (int) datas[1].readByte();
                    Areas[i][j] = datas[2].readFloat();
                    MaxPend[i][j] = datas[3].readDouble();
                }
            }
            for(int k=0; k<4; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){e.printStackTrace();}
    }
    
    public void leeRED(){
        
        if (printDebug) System.out.println(">>> Reading Raster Network");
        
        try{
            java.io.FileInputStream files[] = new java.io.FileInputStream[1];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[1];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[1];
            String exts[] = {".redRas"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            for(int k=0; k<1; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            for (int i=1; i <= metaDEM.getNumRows(); i++){
                for (int j=1; j <= metaDEM.getNumCols() ;j++){
                    RedRas[i][j] = datas[0].readByte();
                    if(RedRas[i][j] != 1) RedRas[i][j]=0;
                }
            }
            for(int k=0; k<1; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){e.printStackTrace();}
    }
    
    
    
    public void leeDEM(){
        try{
            java.io.FileInputStream files[] = new java.io.FileInputStream[1];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[1];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[1];
            String exts[] = {".dem"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            for(int k=0; k<1; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            if(metaDEM.getFormat().equalsIgnoreCase("Integer")){
                for (int i=1; i <= metaDEM.getNumRows(); i++){
                    for (int j=1; j <= metaDEM.getNumCols() ;j++){
                        DEM[i][j] = (double) datas[0].readInt();
                    }
                }
            }
            if(metaDEM.getFormat().equalsIgnoreCase("Double")){
                for (int i=1; i <= metaDEM.getNumRows(); i++){
                    for (int j=1; j <= metaDEM.getNumCols() ;j++){
                        DEM[i][j] = (double) datas[0].readDouble();
                    }
                }
            }
            
            if(metaDEM.getFormat().equalsIgnoreCase("Float")){
                for (int i=1; i <= metaDEM.getNumRows(); i++){
                    for (int j=1; j <= metaDEM.getNumCols() ;j++){
                        DEM[i][j] = (double) datas[0].readFloat();
                    }
                }
            }
            
            for(int k=0; k<1; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){e.printStackTrace();}
    }
    
    
    public void run(){
        corrigeDEM();
    }
    
    
    public static void main(String[] Arguments){
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_01/84928846.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_01/84928846.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_02/31908796.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_02/31908796.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_03/12620307.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_03/12620307.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM",
                                 "/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.dem"};*/
                                 
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/caseFlat001.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/caseFlat001.dem"};*/
                                 
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit001.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit001.dem"};*/
        
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit002.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit002.dem"};*/
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File(Arguments[0]));
            metaRaster1.setLocationBinaryFile(new java.io.File(Arguments[1]));
            
            hydroScalingAPI.io.DataRaster datosRaster = new hydroScalingAPI.io.DataRaster(metaRaster1);
            new NetworkExtractionModule(metaRaster1, datosRaster);
            System.exit(0);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
            System.exit(0);
        }

    }
    
    
    
}
