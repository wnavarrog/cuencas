/*
 * plot1.java
 *
 * Created on 23 de julio de 2001, 03:48 PM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author  olver
 * @version
 */

class XYPoint
{

    XYPoint(double d, double d1, String s)
    {
        xValue = d;
        yValue = d1;
        labelString = s;
    }

    public double x()
    {
        return xValue;
    }

    public double y()
    {
        return yValue;
    }

    public String labelString()
    {
        return labelString;
    }

    protected double xValue;
    protected double yValue;
    protected String labelString;
}
