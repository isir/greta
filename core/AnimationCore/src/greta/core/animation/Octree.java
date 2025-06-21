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
