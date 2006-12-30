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
 * dotFilter.java
 *
 * Created on 3 de diciembre de 2000, 17:20
 */

package hydroScalingAPI.util.fileUtilities;

/**
 *
 * @author  Ricardo Mantilla
 */
public class DirFilter extends Object implements java.io.FileFilter{

    /** Creates new filtroExtenciones */
    public DirFilter() {
    }

    public boolean accept(final java.io.File p1) {
        return p1.isDirectory();
    }
}
