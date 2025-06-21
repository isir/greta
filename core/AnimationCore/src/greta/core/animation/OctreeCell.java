/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
