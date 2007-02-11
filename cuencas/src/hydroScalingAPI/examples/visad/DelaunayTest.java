package hydroScalingAPI.examples.visad;
//
// DelaunayTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 2006 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.
 
You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import visad.*;
import visad.java3d.DisplayImplJ3D;

/**
 * DelaunayTest provides a graphical demonstration of implemented
 * Delaunay triangulation algorithms, in 2-D or 3-D.
 */
public class DelaunayTest {
    
    public static final int CLARKSON = 1;
    public static final int WATSON = 2;
    public static final int FAST = 3;
    
    public static final int NONE = 1;
    public static final int BOXES = 2;
    public static final int TRIANGLES = 3;
    public static final int VERTICES = 4;
    
    /** Run 'java DelaunayTest' for usage instructions */
    public static void main(String[] argv) throws VisADException,
            RemoteException {
        
        double delX=-0.1;
        double delY=-0.1;
        System.out.println(Math.atan(delY/delX)/2.0/Math.PI*360);
        System.out.println(((delX>0&delY<0)?360:0)+(delX>0?0:180)+(delX==0?-90*delY/Math.abs(delY):Math.atan(delY/delX)/2.0/Math.PI*360));
        float    x1=686414.2f,
                 y1=4214126.0f,
                 x2=0.0f,
                 y2=3.8191632E7f,
                 x3=686407.8f,
                 y3=4214138.0f,
                 x4=0.0f,
                 y4=4550338.0f;
        float[] result=intersection(x1,y1,x2,y2,x3,y3,x4,y4);
        System.out.println(result[0]+" "+result[1]);
        //System.exit(0);

        
        argv=new String[] {"2","1000","1","3"};
        
        boolean problem = false;
        int numpass = 0;
        int dim = 0;
        int points = 0;
        int type = 0;
        int l = 1;
        boolean test = false;
        if (argv.length < 3) problem = true;
        else {
            try {
                dim = Integer.parseInt(argv[0]);
                points = Integer.parseInt(argv[1]);
                type = Integer.parseInt(argv[2]);
                if (argv.length > 3) l = Integer.parseInt(argv[3]);
                test = argv.length > 4;
                if (dim < 2 || dim > 3 || points < 1 || type < 1 || l < 1 || l > 4) {
                    problem = true;
                }
                if (dim == 3 && type > 2) {
                    System.out.println("Only Clarkson and Watson support " +
                            "3-D triangulation.\n");
                    System.exit(2);
                }
            } catch (NumberFormatException exc) {
                problem = true;
            }
        }
        if (problem) {
            System.out.println("Usage:\n" +
                    "   java DelaunayTest dim points type [label] [test]\n" +
                    "dim    = The dimension of the triangulation\n" +
                    "         2 = 2-D\n" +
                    "         3 = 3-D\n" +
                    "points = The number of points to triangulate.\n" +
                    "type   = The triangulation method to use:\n" +
                    "         1 = Clarkson\n" +
                    "         2 = Watson\n" +
                    "         3 = Fast\n" +
                    "     X + 3 = Fast with X improvement passes\n" +
                    "label  = How to label the diagram:\n" +
                    "         1 = No labels (default)\n" +
                    "         2 = Vertex boxes\n" +
                    "         3 = Triangle numbers\n" +
                    "         4 = Vertex numbers\n" +
                    "test   = Whether to test the triangulation (default: no)\n");
            System.exit(1);
        }
        if (type > 3) {
            numpass = type - 3;
            type = 3;
        }
        
        float[][] samples = null;
        if (dim == 2) samples = new float[2][points];
        else samples = new float[3][points];
        
        float[] samp0 = samples[0];
        float[] samp1 = samples[1];
        float[] samp2 = null;
        if (dim == 3) samp2 = samples[2];
        
        for (int i=0; i<points; i++) {
            samp0[i] = (float) (1000 * Math.random());
            samp1[i] = (float) (1000 * Math.random());
        }
        if (dim == 3) {
            for (int i=0; i<points; i++) {
                samp2[i] = (float) (1000 * Math.random());
            }
        }
        Delaunay delaun=makeTriang(samples, type, numpass, test);
        visTriang(delaun, samples, l);
        sortTriangles(delaun,samples);
        writeTriangulation(delaun,samples);
    }
    
    /**
     * Triangulates the given samples according to the specified algorithm.
     *
     * @param type One of CLARKSON, WATSON, FAST
     * @param numpass Number of improvement passes
     * @param test Whether to test the triangulation for errors
     */
    public static Delaunay makeTriang(float[][] samples, int type,
            int numpass, boolean test) throws VisADException, RemoteException {
        int dim = samples.length;
        int points = samples[0].length;
        System.out.print("Triangulating " + points + " points " +
                "in " + dim + "-D with ");
        
        long start = 0;
        long end = 0;
        Delaunay delaun = null;
        if (type == CLARKSON) {
            System.out.println("the Clarkson algorithm.");
            start = System.currentTimeMillis();
            delaun = (Delaunay) new DelaunayClarkson(samples);
            end = System.currentTimeMillis();
        } else if (type == WATSON) {
            System.out.println("the Watson algorithm.");
            start = System.currentTimeMillis();
            delaun = (Delaunay) new DelaunayWatson(samples);
            end = System.currentTimeMillis();
        } else if (type == FAST) {
            System.out.println("the Fast algorithm.");
            start = System.currentTimeMillis();
            delaun = (Delaunay) new DelaunayFast(samples);
            end = System.currentTimeMillis();
        }
        float time = (end - start) / 1000f;
        System.out.println("Triangulation took " + time + " seconds.");
        if (numpass > 0) {
            System.out.println("Improving samples: " + numpass + " pass" +
                    (numpass > 1 ? "es..." : "..."));
            start = System.currentTimeMillis();
            delaun.improve(samples, numpass);
            end = System.currentTimeMillis();
            time = (end - start) / 1000f;
            System.out.println("Improvement took " + time + " seconds.");
        }
        if (test) {
            System.out.print("Testing triangulation integrity...");
            if (delaun.test(samples)) System.out.println("OK");
            else System.out.println("FAILED!");
        }
        return delaun;
    }
    
    /**
     * Displays the results for the given Delaunay triangulation of the
     * specified samples in a window.
     *
     * @param delaun The triangulation to visualize
     * @param samples The samples corresponding to the triangulation
     * @param label One of NONE, BOXES, TRIANGLES, VERTICES
     */
    public static void visTriang(Delaunay delaun, float[][] samples,
            int labels) throws VisADException, RemoteException {
        int dim = samples.length;
        int points = samples[0].length;
        
        // set up final variables
        final int label = labels;
        final int[][] tri = delaun.Tri;
        final int[][] edges = delaun.Edges;
        final int numedges = delaun.NumEdges;
        final int[][] walk=delaun.Walk;
        
        // set up frame
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        float[] samp0 = samples[0];
        float[] samp1 = samples[1];
        float[] samp2 = null;
        if (dim == 3) samp2 = samples[2];
        
        if (dim == 2) {
            // set up GUI components in 2-D
            final float[] s0 = samp0;
            final float[] s1 = samp1;
            JComponent jc = new JComponent() {
                public void paint(Graphics gr) {
                    
                    // draw triangles
                    for (int i=0; i<tri.length; i++) {
                        int[] t = tri[i];
                        gr.drawLine((int) s0[t[0]], (int) s1[t[0]],
                                (int) s0[t[1]], (int) s1[t[1]]);
                        gr.drawLine((int) s0[t[1]], (int) s1[t[1]],
                                (int) s0[t[2]], (int) s1[t[2]]);
                        gr.drawLine((int) s0[t[2]], (int) s1[t[2]],
                                (int) s0[t[0]], (int) s1[t[0]]);
                    }
                    
                    // draw labels if specified
                    if (label == 2) {        // vertex boxes
                        for (int i=0; i<s0.length; i++) {
                            gr.drawRect((int) s0[i]-2, (int) s1[i]-2, 4, 4);
                        }
                    } else if (label == 3) {   // triangle numbers
                        for (int i=0; i<tri.length; i++) {
                            int t0 = tri[i][0];
                            int t1 = tri[i][1];
                            int t2 = tri[i][2];
                            int avgX = (int) ((s0[t0] + s0[t1] + s0[t2])/3);
                            int avgY = (int) ((s1[t0] + s1[t1] + s1[t2])/3);
                            gr.drawString("T-"+String.valueOf(i), avgX-4, avgY);
                            
                        }
                        for (int i=0; i<s0.length; i++) {
                            gr.drawString("V-" + i, (int) s0[i], (int) s1[i]);
                        }
                        for (int i=0; i<edges.length; i++) {
                            float v1X=s0[tri[i][1]]-s0[tri[i][0]];
                            float v1Y=s1[tri[i][1]]-s1[tri[i][0]];
                            float v2X=s0[tri[i][2]]-s0[tri[i][0]];
                            float v2Y=s1[tri[i][2]]-s1[tri[i][0]];
                            
                            float kComp=v1X*v2Y-v2X*v1Y;
                            
                            gr.drawString("E-" + edges[i][0], (int) (s0[tri[i][0]]+s0[tri[i][1]])/2, (int) (s1[tri[i][0]]+s1[tri[i][1]])/2);
                            gr.drawString("E-" + edges[i][1], (int) (s0[tri[i][1]]+s0[tri[i][2]])/2, (int) (s1[tri[i][1]]+s1[tri[i][2]])/2);
                            gr.drawString("E-" + edges[i][2], (int) (s0[tri[i][2]]+s0[tri[i][0]])/2, (int) (s1[tri[i][2]]+s1[tri[i][0]])/2);
                        }
                        for (int i=0; i<walk.length; i++) {
                            int t0 = tri[i][0];
                            int t1 = tri[i][1];
                            int t2 = tri[i][2];
                            int avgX = (int) ((s0[t0] + s0[t1] + s0[t2])/3);
                            int avgY = (int) ((s1[t0] + s1[t1] + s1[t2])/3);
                            for(int j=0;j<3;j++){
                                if(walk[i][j%3] != -1){
                                    int t00 = tri[walk[i][j%3]][0];
                                    int t10 = tri[walk[i][j%3]][1];
                                    int t20 = tri[walk[i][j%3]][2];
                                    int avgXX = (int) ((s0[t00] + s0[t10] + s0[t20])/3);
                                    int avgYY = (int) ((s1[t00] + s1[t10] + s1[t20])/3);
                                    gr.setColor(new Color(50*j));
                                    gr.drawLine(avgX,avgY,avgXX,avgYY);
                                }
                            }
                            
                        }
                    } else if (label == 4) {   // vertex numbers
                        for (int i=0; i<s0.length; i++) {
                            gr.drawString("V-" + i, (int) s0[i], (int) s1[i]);
                        }
                    }
                }
            };
            frame.getContentPane().add(jc);
        } else {
            // set up GUI components in 3-D
            final float[][] samps = samples;
            final float[] s0 = samp0;
            final float[] s1 = samp1;
            final float[] s2 = samp2;
            
            // construct a UnionSet of line segments (tetrahedra edges)
            final RealType x = RealType.getRealType("x");
            final RealType y = RealType.getRealType("y");
            final RealType z = RealType.getRealType("z");
            RealTupleType xyz = new RealTupleType(x, y, z);
            int[] e0 = {0, 0, 0, 1, 1, 2};
            int[] e1 = {1, 2, 3, 2, 3, 3};
            Gridded3DSet[] gsp = new Gridded3DSet[numedges];
            for (int i=0; i<numedges; i++) gsp[i] = null;
            for (int i=0; i<edges.length; i++) {
                int[] trii = tri[i];
                int[] edgesi = edges[i];
                for (int j=0; j<6; j++) {
                    if (gsp[edgesi[j]] == null) {
                        float[][] pts = new float[3][2];
                        float[] p0 = pts[0];
                        float[] p1 = pts[1];
                        float[] p2 = pts[2];
                        int tp0 = trii[e0[j]];
                        int tp1 = trii[e1[j]];
                        p0[0] = samp0[tp0];
                        p1[0] = samp1[tp0];
                        p2[0] = samp2[tp0];
                        p0[1] = samp0[tp1];
                        p1[1] = samp1[tp1];
                        p2[1] = samp2[tp1];
                        gsp[edgesi[j]] = new Gridded3DSet(xyz, pts, 2);
                    }
                }
            }
            UnionSet tet = new UnionSet(xyz, gsp);
            final DataReference tetref = new DataReferenceImpl("tet");
            tetref.setData(tet);
            
            // set up Java3D Display
            DisplayImpl display = new DisplayImplJ3D("image display");
            display.addMap(new ScalarMap(x, Display.XAxis));
            display.addMap(new ScalarMap(y, Display.YAxis));
            display.addMap(new ScalarMap(z, Display.ZAxis));
            display.addMap(new ConstantMap(1, Display.Red));
            display.addMap(new ConstantMap(1, Display.Green));
            display.addMap(new ConstantMap(0, Display.Blue));
            
            // draw labels if specified
            if (label == 2) {
                throw new UnimplementedException(
                        "DelaunayTest.testTriang: vertex boxes");
            } else if (label == 3) {   // triangle numbers
                int len = tri.length;
                TextType text = new TextType("text");
                RealType t = RealType.getRealType("t");
                RealTupleType rtt = new RealTupleType(new RealType[] {t});
                Linear1DSet timeSet = new Linear1DSet(rtt, 0, len - 1, len);
                TupleType textTuple = new TupleType(new MathType[] {x, y, z, text});
                FunctionType textFunction = new FunctionType(t, textTuple);
                FieldImpl textField = new FieldImpl(textFunction, timeSet);
                for (int i=0; i<len; i++) {
                    int t0 = tri[i][0];
                    int t1 = tri[i][1];
                    int t2 = tri[i][2];
                    int t3 = tri[i][3];
                    int avgX = (int) ((s0[t0] + s0[t1] + s0[t2] + s0[t3])/4);
                    int avgY = (int) ((s1[t0] + s1[t1] + s1[t2] + s1[t3])/4);
                    int avgZ = (int) ((s2[t0] + s2[t1] + s2[t2] + s2[t3])/4);
                    Data[] td = {new Real(x, avgX),
                    new Real(y, avgY),
                    new Real(z, avgZ),
                    new Text(text, "" + i)};
                    TupleIface tt = new Tuple(textTuple, td);
                    textField.setSample(i, tt);
                }
                display.addMap(new ScalarMap(text, Display.Text));
                DataReferenceImpl rtf = new DataReferenceImpl("rtf");
                rtf.setData(textField);
                display.addReference(rtf, null);
            } else if (label == 4) {   // vertex numbers
                int len = s0.length;
                TextType text = new TextType("text");
                RealType t = RealType.getRealType("t");
                RealTupleType rtt = new RealTupleType(new RealType[] {t});
                Linear1DSet timeSet = new Linear1DSet(rtt, 0, len - 1, len);
                TupleType textTuple = new TupleType(new MathType[] {x, y, z, text});
                FunctionType textFunction = new FunctionType(t, textTuple);
                FieldImpl textField = new FieldImpl(textFunction, timeSet);
                for (int i=0; i<len; i++) {
                    Data[] td = {new Real(x, s0[i]),
                    new Real(y, s1[i]),
                    new Real(z, s2[i]),
                    new Text(text, "" + i)};
                    TupleIface tt = new Tuple(textTuple, td);
                    textField.setSample(i, tt);
                }
                display.addMap(new ScalarMap(text, Display.Text));
                DataReferenceImpl rtf = new DataReferenceImpl("rtf");
                rtf.setData(textField);
                display.addReference(rtf, null);
            }
            
            // finish setting up Java3D Display
            display.getDisplayRenderer().setBoxOn(false);
            display.addReference(tetref);
            
            // set up frame's panel
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(display.getComponent());
            frame.getContentPane().add(panel);
        }
        frame.setBounds(500,0,1010,1030);
        frame.setTitle("Triangulation results");
        frame.setVisible(true);
        
        
    }
    
    public static float[] intersection(float x1, float y1, float x2, float y2,float x3, float y3, float x4, float y4){
        float b1=x1*y2-y1*x2;
        float b2=x3*y4-y3*x4;
        
        float U1=(x3-x4)*b1-(x1-x2)*b2;
        float U2=(y3-y4)*b1-(y1-y2)*b2;
        float BG=(x1-x2)*(y3-y4)-(x3-x4)*(y1-y2);
        
        float xi=U1/BG;
        float yi=U2/BG;
        
        float[] answ={xi,yi};
        
        return answ;
    }
    
    public static void sortTriangles(Delaunay delaun,float[][] samples){
        for(int j=0;j<delaun.Vertices.length;j++) {
            float[][] lines1=new float[3][delaun.Vertices[j].length];
            for(int i=0;i<delaun.Vertices[j].length;i++){
                for(int k=0;k<3;k++){
                    lines1[0][i]+=samples[0][delaun.Tri[delaun.Vertices[j][i]][k]];
                    lines1[1][i]+=samples[1][delaun.Tri[delaun.Vertices[j][i]][k]];
                }
                lines1[0][i]/=3.0;
                lines1[1][i]/=3.0;
                double delX=lines1[0][i]-samples[0][j];
                double delY=lines1[1][i]-samples[1][j];
                lines1[2][i]=(float)(((delX>0&delY<0)?360:0)+(delX>0?0:180)+(delX==0?90:Math.atan(delY/delX)/2.0/Math.PI*360));
                
            }
            float[] sortedAngles=lines1[2].clone();
            java.util.Arrays.sort(sortedAngles);
            int[] sortedTriList=new int[delaun.Vertices[j].length];
            for(int i=0;i<delaun.Vertices[j].length;i++) sortedTriList[java.util.Arrays.binarySearch(sortedAngles,lines1[2][i])]=delaun.Vertices[j][i];
            delaun.Vertices[j]=sortedTriList;
            
            
            
        }
    }
    
    public static void writeTriangulation(Delaunay delaun,float[][] samples){
        
        float[] s0 = samples[0];
        float[] s1 = samples[1];
        
        int[][] tri = delaun.Tri;
        int[][] edges = delaun.Edges;
        int numedges = delaun.NumEdges;
        int[][] walk=delaun.Walk;
        int[][] vert=delaun.Vertices;
        
        System.out.println("Edges File");
        java.util.Hashtable edges_order_map=new java.util.Hashtable();
        java.util.Vector edges_ef=new java.util.Vector();
        int runningID=0;
        for(int k=0;k<vert.length;k++){
            java.util.Vector localEdges=new java.util.Vector();
            for (int j=0; j<vert[k].length; j++) {
                int i=vert[k][j];
                float v1X=s0[tri[i][1]]-s0[tri[i][0]];
                float v1Y=s1[tri[i][1]]-s1[tri[i][0]];
                float v2X=s0[tri[i][2]]-s0[tri[i][0]];
                float v2Y=s1[tri[i][2]]-s1[tri[i][0]];

                float kComp=v1X*v2Y-v2X*v1Y;
                
                int pPos=0;
                for (int l=1; l<3; l++) if(k == tri[i][l]) pPos=l;
                //System.out.println("P"+k+" In Triang("+(kComp>0?"+":"-")+"):"+" P"+tri[i][0]+" P"+tri[i][1]+" P"+tri[i][2]+" pPos "+pPos);
                int L1pos=(pPos+1+(kComp>0?0:1))%3;
                int L2pos=(pPos+1+(kComp>0?1:0))%3;
                //System.out.println("    L1 is: E"+k+"-"+tri[i][L1pos]);
                //System.out.println("    L2 is: E"+k+"-"+tri[i][L2pos]);
                if(!localEdges.contains("E"+k+"-"+tri[i][L1pos])) localEdges.add("E"+k+"-"+tri[i][L1pos]);
                if(!localEdges.contains("E"+k+"-"+tri[i][L2pos])) localEdges.add("E"+k+"-"+tri[i][L2pos]);
                
            }
            localEdges.add(localEdges.firstElement());
            Object[] myEdges=localEdges.toArray();
            for (int j=1; j<myEdges.length; j++) {
                edges_ef.add(myEdges[j]+" "+k+" "+((String)myEdges[j]).substring(((String)myEdges[j]).indexOf("-")+1)+" "+myEdges[j-1]);
                if(edges_order_map.get((String)myEdges[j]) == null){
                    edges_order_map.put((String)myEdges[j],""+(2*runningID));
                    String inverseEdge=((String)myEdges[j]).substring(1);
                    String[] elementsIE=inverseEdge.split("-");
                    inverseEdge="E"+elementsIE[1]+"-"+elementsIE[0];
                    edges_order_map.put(inverseEdge,""+(2*runningID+1));
                    runningID++;
                } 
            }
            
        }
        int totEdges=edges_ef.size();
        //System.out.println(totEdges+" "+edges_order_map.size());
        java.util.Enumeration en=edges_order_map.keys();
//        while(en.hasMoreElements()) {
//            Object tttt=en.nextElement();
//            System.out.println(tttt+" "+edges_order_map.get(tttt));
//        }
        String[] edges_ef_ready=new String[totEdges];
        for(int i=0;i<totEdges;i++){
            String[] toPrint=((String)edges_ef.get(i)).split(" ");
            //System.out.println(i+" "+edges_ef.get(i));
            //System.out.println(edges_order_map.get(toPrint[0])+" "+toPrint[1]+" "+toPrint[2]+" "+edges_order_map.get(toPrint[3]));
            edges_ef_ready[Integer.parseInt((String)edges_order_map.get(toPrint[0]))]=edges_order_map.get(toPrint[0])+" "+toPrint[1]+" "+toPrint[2]+" "+edges_order_map.get(toPrint[3]);
        }
        for(int i=0;i<totEdges;i++){
            System.out.println(edges_ef_ready[i]);
        }
        
        System.out.println("Nodes File");
        java.util.Vector nodes_nf=new java.util.Vector();
        java.util.Vector edges_nf=new java.util.Vector();
        java.util.Vector codes_nf=new java.util.Vector();
        
        
        for (int i=0; i<edges.length; i++) {
            for (int j=0; j<3; j++) {
                if(!nodes_nf.contains("P"+tri[i][j])){
                    nodes_nf.add("P"+tri[i][j]);
                    edges_nf.add("E"+tri[i][j]+"-"+tri[i][(j+1)%3]);
                }
            }
        }
        for(int i=0;i<vert.length;i++){
            int pointID=Integer.parseInt(((String)nodes_nf.get(i)).substring(1));
            System.out.println(samples[0][pointID]+" "+samples[1][pointID]+" "+edges_order_map.get(edges_nf.get(i))+" "+"BC-XX");
        }
        
        System.out.println("Triangles File");
        for (int i=0; i<edges.length; i++) {
            float v1X=s0[tri[i][1]]-s0[tri[i][0]];
            float v1Y=s1[tri[i][1]]-s1[tri[i][0]];
            float v2X=s0[tri[i][2]]-s0[tri[i][0]];
            float v2Y=s1[tri[i][2]]-s1[tri[i][0]];

            float kComp=v1X*v2Y-v2X*v1Y;
            //System.out.print("T("+i+(kComp>0?"+":"-")+")");
            if(kComp>0){
                System.out.print(tri[i][2]+" "+tri[i][1]+" "+tri[i][0]);
                System.out.print(" "+walk[i][0]+" "+walk[i][2]+" "+walk[i][1]);
                System.out.print(" "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][1]));
            } else {
                System.out.print(tri[i][0]+" "+tri[i][1]+" "+tri[i][2]);
                System.out.print(" "+walk[i][1]+" "+walk[i][2]+" "+walk[i][0]);
                System.out.print(" "+(String)edges_order_map.get("E"+tri[i][0]+"-"+tri[i][2])+
                                 " "+(String)edges_order_map.get("E"+tri[i][1]+"-"+tri[i][0])+
                                 " "+(String)edges_order_map.get("E"+tri[i][2]+"-"+tri[i][1]));
            }
            System.out.println();
        }
        
    }
    
}

