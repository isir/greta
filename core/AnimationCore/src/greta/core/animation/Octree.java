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
public class Octree {
    OctreeCell p_rootcell;
    ArrayList<OctreeCell> m_tree_cell;
    int m_max_level;
    public Octree(){
        init(new Vec3d(0,0,0), new Vec3d(1,1,1));
    }

    public Octree(Vec3d origin, Vec3d halfDim){
        init(origin, halfDim);
    }

    public void init(Vec3d origin, Vec3d halfDim){
        m_max_level = 8;
	p_rootcell = new OctreeCell(origin, halfDim, this, -1, -1);
	m_tree_cell.clear();
	m_tree_cell.add(p_rootcell);
    }

    public void reset(Vec3d origin, Vec3d halfDim){
	init(origin, halfDim);
//	for(int i = 0; i < m_tree_points.size(); ++i)
//	{
//		OctreePoint& p = m_tree_points[i];
//		if(p_rootcell->isInside(p.m_position))
//		{
//			p_rootcell->insert(&p);
//		}
//	}
//	updateParameters();
}
}
