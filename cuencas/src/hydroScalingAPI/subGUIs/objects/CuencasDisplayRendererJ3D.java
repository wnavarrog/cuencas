/*
 * MrDisplayRenderer.java
 *
 * Created on June 12, 2001, 5:04 PM
 */

package hydroScalingAPI.subGUIs.objects;

import visad.*;
import visad.java3d.*;

import java.lang.reflect.*;
import java.awt.event.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.util.*;

public class CuencasDisplayRendererJ3D extends visad.java3d.DefaultDisplayRendererJ3D{    

  private boolean boxMode;
  private float[] startPoint=new float[2];

  public CuencasDisplayRendererJ3D() {
    super();
  }

  public void setBoxMode(boolean state){
      boxMode=state;
  }
  
  public void setIniPoint(float[] sp){
      startPoint=sp;
  }    
  
}

