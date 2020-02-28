/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class OctreeCell {
    public Vec3d m_origin;
    public Vec3d m_halfDimension;
    Octree p_octree;
    OctreeCell[] m_children = new OctreeCell[8];
    int m_index;
    int m_level;
    int m_parent;
    ArrayList<Integer> m_id_depthorder;

    public OctreeCell(){}

    public OctreeCell(Vec3d origin, Vec3d halfDim, Octree octree, int parent, int localindx){
        if(parent >= 0)
	{
		m_level = p_octree.m_tree_cell.get(m_parent).m_level + 1;
		m_index = p_octree.m_tree_cell.size();

		OctreeCell parentcell = p_octree.m_tree_cell.get(m_parent);
		m_id_depthorder = (ArrayList<Integer>) parentcell.m_id_depthorder.clone();
		m_id_depthorder.add(localindx);
	}else
	{
		m_level = 0;
		m_index = 0;
		m_id_depthorder.add(0);
	}
    }


    public boolean isInside(Vec3d point){
	return !(m_origin.get(0) + m_halfDimension.get(0) < point.get(0)  ||  m_origin.get(0) - m_halfDimension.get(0) > point.get(0)  ||
                m_origin.get(1) + m_halfDimension.get(1) < point.get(1)  ||  m_origin.get(1) - m_halfDimension.get(1) > point.get(1)  ||
                m_origin.get(2) + m_halfDimension.get(2) < point.get(2)  ||  m_origin.get(2) - m_halfDimension.get(2) > point.get(2));
    }

    int getOctreeCellContainingPoint(Vec3d point) {
        int oct = 0;
	if(point.get(0) >= m_origin.get(0)) oct |= 4;
	if(point.get(1) >= m_origin.get(1)) oct |= 2;
	if(point.get(2) >= m_origin.get(2)) oct |= 1;
	return oct;
    }
}
