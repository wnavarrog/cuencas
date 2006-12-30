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

/**
 *
 * @author Ricardo Mantilla
 */
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

