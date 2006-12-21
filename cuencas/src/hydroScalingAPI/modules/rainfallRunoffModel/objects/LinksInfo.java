/*
 * linksInfo.java
 *
 * Created on November 11, 2001, 10:39 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author  ricardo 
 */
public class LinksInfo extends java.lang.Object {

    /*El proposito de este progrma es ser la base de datos de gemetria hidraulica, rugosidad y en general todos los
           parametros asociados al sistema de links de la cuenca*/
    
    private float[][] upStreamAreaArray, lengthArray, cheziArray, manningArray, widthArray, dropArray,orderArray, slopeArray, totalChannelLengthArray;
    private float basinArea;
    
    private float[][] Ck;
    private float lambda1,lambda2;
    
    /** Creates new linksInfo */
    public LinksInfo(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksCon) throws java.io.IOException{
        lengthArray=linksCon.getVarValues(1);
        
        upStreamAreaArray=linksCon.getVarValues(2);
        dropArray=linksCon.getVarValues(3);
        orderArray=linksCon.getVarValues(4);
        basinArea=upStreamAreaArray[0][linksCon.getOutletID()];
        totalChannelLengthArray=linksCon.getVarValues(5);
        
        slopeArray=new float[1][lengthArray[0].length];
        for (int i=0;i<slopeArray[0].length;i++) {
            if (dropArray[0][i] == 0){
                slopeArray[0][i]=(float) (0.05*Math.pow(upStreamAreaArray[0][i]/2.0,-0.2)); //false Geometry
            } else {
                slopeArray[0][i]=(float) (dropArray[0][i]/lengthArray[0][i]/1000.0);
            }
        }
    }
    
    public void setSlopesHG(float coefficient, float exponent, float sdResiduals){
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        for (int i=0;i<widthArray[0].length;i++) {
            slopeArray[0][i]=(float) (coefficient*Math.pow(upStreamAreaArray[0][i],exponent)*Math.exp(ranG.sample()));
        }
    }
    
    public void setWidthsHG(float coefficient, float exponent,float sdResiduals){
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        widthArray=new float[1][lengthArray[0].length];
        for (int i=0;i<widthArray[0].length;i++) {
            //widthArray[0][i]=(float) Math.pow(upStreamAreaArray[0][i]/2.0,0.5);  // Menabde and Sivapalan
            //widthArray[0][i]=(float) Math.pow(upStreamAreaArray[0][i]/2.0,0.5); //false Geometry
            //widthArray[0][i]=(float) (0.78*Math.pow(upStreamAreaArray[0][i],.44));  // Data supported 
            //widthArray[0][i]=(float) (1.0*Math.pow(upStreamAreaArray[0][i],.48));  // As measured in Kansas
            //widthArray[0][i]=(float) (5.6*Math.pow(upStreamAreaArray[0][i],.46));  // For GC, from Molnar and Ramirez 1998 using pd=0.5  
            //widthArray[0][i]=(float) (3.6*Math.pow(upStreamAreaArray[0][i],.30));  // For Kansas measured by myself
            //widthArray[0][i]=(float) (2.36*Math.pow(upStreamAreaArray[0][i],0.31));  // Miller, 1995 -  with data from Walnut Gulch
            //widthArray[0][i]=(float) (0.29*Math.pow(upStreamAreaArray[0][i],.50));  // 
            
            widthArray[0][i]=(float) (coefficient*Math.pow(upStreamAreaArray[0][i],exponent)*Math.exp(ranG.sample()));
        }
    }
    
    public void setCheziHG(float coefficient, float exponent){
        cheziArray=new float[1][lengthArray[0].length];
        for (int i=0;i<cheziArray[0].length;i++) {
            //cheziArray[0][i]=100.0f;
            //cheziArray[0][i]=(float) (14.2*Math.pow(slopeArray[0][i],-1/3.)); //From Strikler and Shields
            cheziArray[0][i]=(float) (coefficient*Math.pow(slopeArray[0][i],exponent));
        }
    }
    
    public void setManningHG(float coefficient, float exponent){
        manningArray=new float[1][lengthArray[0].length];
        for (int i=0;i<manningArray[0].length;i++) {
            //cheziArray[0][i]=100.0f;
            //cheziArray[0][i]=(float) (14.2*Math.pow(slopeArray[0][i],-1/3.)); //From Strikler and Shields
            manningArray[0][i]=(float) (coefficient*Math.pow(slopeArray[0][i],exponent));
        }
    }
    
    public void setVqParams(float coeff,float sdResiduals,float exponentQ, float exponentA){
        Ck=new float[1][lengthArray[0].length];
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        for (int i=0;i<Ck[0].length;i++) Ck[0][i]=(float) (coeff*Math.exp(ranG.sample()));
        lambda1=exponentQ;
        lambda2=exponentA;
    }
    
    public float getLamda1(){
        return lambda1;
    }
    
    public float getLamda2(){
        return lambda2;
    }
    
    public double upStreamArea(int i){
        return (double) (upStreamAreaArray[0][i]);
    }
    
    public double linkOrder(int i){
        return (double) (orderArray[0][i]);
    }
    
    public double Chezi(int i){
        return (double) cheziArray[0][i];
    }
    
    public double Width(int i){
        return (double) widthArray[0][i];
    }
    
    public double Length(int i){
        return (double) (lengthArray[0][i]*1000.0);
        //return (double) (2000.0); //false Geometry
    }
    
    public double Slope(int i){
        return (double) slopeArray[0][i];
    }
    
    public double upStreamTotalLength(int i){
        return (double) (totalChannelLengthArray[0][i]);
    }
    
    public float[][] getCkArray(){
        return Ck;
    }
    
    public float[][] getUpStreamAreaArray(){
        return upStreamAreaArray;
    }
    
    public float[][] getLinkOrderArray(){
        return orderArray;
    }
    
    public float[][] getCheziArray(){
        return cheziArray;
    }
    
    public float[][] getManningArray(){
        return manningArray;
    }
    
    public float[][] getWidthArray(){
        return widthArray;
    }
    
    public float[][] getLengthInKmArray(){
        return lengthArray;
        //return (double) (2000.0); //false Geometry
    }
    
    public float[][] getSlopeArray(){
        return slopeArray;
    }
    
    public float basinArea(){
        
        return basinArea;
        
    }
    

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
    }

}
