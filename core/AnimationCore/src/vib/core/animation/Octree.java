package vib.core.animation;

import java.util.ArrayList;
import vib.core.util.math.Vec3d;

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
