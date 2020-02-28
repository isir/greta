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
package greta.core.behaviorplanner.mseconstraints;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class MultimodalEmotionConstraintSet {

    ArrayList<ConsNode> cons_trees;

    String name;

    /**
     * contructor
     *
     */
    public MultimodalEmotionConstraintSet() {

        cons_trees = new ArrayList<ConsNode>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name1) {
        this.name = name1;
    }

    public List<ConsNode> getConstraintsOfOneSignal(String name) {

        //int id=getIdOfSignal(name);
        //this. getNewConstraintsOfOneSignal(id);

        throw new UnsupportedOperationException("method not implemented  yet.");
    }

    public List<ConsNode> getConstraintsOfOneSignal(int id) {

        //int id=getIdOfSignal(name);

        //find relevant cons
        ArrayList<ConsNode> good_cons = new ArrayList<ConsNode>();

        for (ConsNode cons_iter : cons_trees) {

            if (!(cons_iter.concerns.isEmpty())) {
                // all the id's of the cons
                for (Integer concerns_iter : cons_iter.concerns) {

                    //if given id occurs on the list
                    //take this nodecons
                    if (concerns_iter.intValue() == id) {
                        good_cons.add(cons_iter);
                    }
                }//end for concerns
            }//end of if
        }//end for cons_trees

        return good_cons;
    }//end of getNewConstraintsOfOneSignal

    private void find_adequate(ConsNode consnode, ConsNode temp) {

        // is an argument
        //
        if (temp.getArgId() != -1) {
            consnode.concerns.add(temp.getArgId());
        }

        //exists always concerns the other not my element..even if it is on rightside

        if ((temp.getLeft() != null) && (temp.getConsNodeOperator() != oper.exist)) {
            find_adequate(consnode, temp.getLeft());
        }

        // with include there is deadlock
        // surely it serves for exclude
        // preceded, rightinc, ??

        //TODO: if include contains or it does not work correcly
        //if ( (temp->getRight()!=null) && (temp->getConsNodeOperator()!= include ) )

        if (temp.getRight() != null) {
            find_adequate(consnode, temp.getRight());
        }
    }

    public void addNewConstraint(ConsNode consnode) {
        //RUN THE TREE
        //ConsNode *temp=consnode;

        find_adequate(consnode, consnode);

        //do {
        //
        //	//FILL THE (left) "CONCERN LIST"
        //	//check if only the left son or both should be on the list

        //	if (temp->getArgId()!=-1) consnode->concerns.push_back(temp->getArgId());
        //
        //	temp=temp->getLeft();
        //
        //}
        //while (temp!=null);
        //
        //temp=consnode;

        //do {
        //
        //	//FILL THE (right) "CONCERN LIST"
        //	//check if only the left son or both should be on the list

        //	if (temp->getArgId()!=-1) consnode->concerns.push_back(temp->getArgId());
        //
        //	temp=temp->getRight();
        //}
        //while (temp!=null);

        cons_trees.add(consnode);
    }

    public List<ConsNode> getNewConstraints() {
        if (cons_trees.isEmpty() == true) {
            return null;
        }
        return cons_trees;
    }

    private void fill_right(int id, float start_time, float stop_time) {

        //TODO: check
        for (ConsNode cons_iter : cons_trees) {
            cons_iter.fill_right(id, start_time, stop_time);
        }
    }

    private void fill(int id, float start_time, float stop_time) {

        //TODO: check
        for (ConsNode cons_iter : cons_trees) {
            cons_iter.fill(id, start_time, stop_time);
        }
    }

    public void clean() {
        //TODO: check
        for (ConsNode cons_iter : cons_trees) {
            cons_iter.clean();
        }
    }
}//end of class
