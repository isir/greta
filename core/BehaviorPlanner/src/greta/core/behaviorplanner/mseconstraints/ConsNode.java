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

/*
 Info - the node can be :

 - two-argment operator :

 left!=null, right!=null, operator!=unknown
 value=-1, start=0, end=0, type=unknown, arg_id=-1

 - one-argument operator :

 left!=null, right==null, operator={exist,lessthan,morethan,not,equal}
 value=-1, start=0, end=0, type=unknown, arg_id=-1


 - id argument :

 left==null, right==null, operator==unknown
 value=-1, start>0, end>0, , arg_id!=-1
 type = unknown if operator one level up is two arg
 type= {start,stop} of operator one level up is one arg

 - number argument :

 left==null, right==null, operator==unknown
 value!=-1, start=0, end=0,
 type=unknown, arg_id=-1

 */
enum oper {

    unknown, rigthinc, include, exclude, precede, exist, morethan, lessthan, equal, and, or, not
};

enum type {

    none, start, end
};

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class ConsNode {

    /**
     *
     */
    public List<Integer> concerns;
    /**
     *
     */
    public String id;
//left argument
    private ConsNode left;
//right argument
    private ConsNode right;
//id the argument
    private int arg_id;
//value (if left and right == null)
    private double start_time;
//value (if left and right == null)
    private double stop_time;
//value (if left and right == null)
    private double value;
    //unknown
    //start
    //stop
    private type my_type;
//operator
    private oper my_oper;

    /**
     * contructor
     *
     */
    public ConsNode() {

        concerns = new ArrayList<Integer>();

        this.start_time = -1.0f;
        this.stop_time = -1.0f;

        this.left = null;
        this.right = null;

        //if id =-1 means no argument only value
        this.arg_id = -1;

        this.my_type = type.none;
        this.my_oper = oper.unknown;

        this.value = -1;
    }

    /**
     *
     * sets the left child node
     *
     * @param temp - signal id
     */
    public void setLeft(ConsNode temp) {
        //check maybe clone?
        left = temp;
    }

    /**
     *
     * sets the right child node
     *
     * @param temp - signal id
     */
    public void setRight(ConsNode temp) {
        //check maybe clone?
        right = temp;
    }

    /**
     *
     * gets the left son
     *
     * @return the left son
     */
    public ConsNode getLeft() {
        return left;
    }

    /**
     *
     * gets the right son
     *
     * @return the right son
     */
    public ConsNode getRight() {
        return right;
    }

    @Override
    public ConsNode clone() {

        ConsNode temp = new ConsNode();

        //rekursja

        //ConsNode *left;
        if (this.left != null) {
            temp.setLeft((this.left).clone());
        } else {
            temp.setLeft(null);
        }

        //ConsNode *right;
        if (this.right != null) {
            temp.setRight((this.right).clone());
        } else {
            temp.setRight(null);
        }

        //arg_id;
        temp.setArg(this.arg_id);

        //double start_time;
        temp.setStartTime(this.start_time);

        //double stop_time;
        temp.setStopTime(this.stop_time);

        //double stop_time;
        //this.id.copy(temp->id);
        temp.id = this.id;

        //double value;
        temp.setValue(this.value);

        //type my_type;
        temp.setArgType(this.my_type);

        //oper my_oper;
        temp.setConsNodeOperator(this.getConsNodeOperatorToString());

        //copy concerns list
        if (!(this.concerns.isEmpty())) {
            // all the id's of the cons
            for (Integer concerns_iter : concerns) {
                temp.concerns.add(concerns_iter);
            }

        }

        return temp;

    }

    /**
     *
     * verifies a condition
     *
     * @param other - the other exists in animation
     */
//if means that consnode needs also the current animation state
    private boolean exists(ConsNode other) {

        //ignore if contraditory
        if (this.arg_id == other.arg_id) {
            return true;
        }

        if (other == null) {
            return false;
        }

        //if this element has defined its start and end time it probably(?) means that it exists
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0)) {
            return true;
        }

        return false;
    }

    /**
     *
     * verifies a condition: the other starts before this
     *
     * @param other - left argument
     */
//it should be called: isPreceded
    private boolean isPreceded(ConsNode other) {

        //other does not exist!
        if ((other.getStartTime() < 0) || (other.getStopTime() <= 0)) {
            return true;
        }

        //I do not exist!
        if ((start_time < 0) || (stop_time < 0)) {
            return true;
        }

        //if both conditions  (stop_time>0) and  && (stop_time=0) together - no difference

        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0)) {

            // if other starts after I start - no satisfied
            if (other.getStartTime() > start_time) {
                return false;
            }

            //if other stops after I start - no satisfied
            if ((other.getStartTime() + other.getStopTime()) > start_time) {
                return false;
            }
        }

        //satisfied by default
        return true;
    }

    /**
     *
     * verifies a condition: this is bigger than other
     *
     * @param other - left argument
     */
//it should be called: isIncluded
    private boolean isIncluded(ConsNode other) {

        //ignore if contraditory
        if (this.arg_id == other.arg_id) {
            return true;
        }

        //other does not exist!
        if ((other.getStartTime() < 0) || (other.getStopTime() <= 0)) {
            return true;
        }

        //I do not exist!
        if ((start_time < 0) || (stop_time < 0)) {
            return true;
        }

        //without the end I do not know if Im included or not

        //if both exist
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time > 0)) {

            // if other starts after I start - no satisfied
            if (other.getStartTime() > start_time) {
                return false;
            }

            //if other stops before I stop - no satisfied
            if ((other.getStartTime() + other.getStopTime()) < (start_time + stop_time)) {
                return false;
            }

        }

        //if I only start
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time == 0)) {

            // if other starts after I start - no satisfied
            if (other.getStartTime() > start_time) {
                return false;
            }

            // if other stops before I start - no satisfied
            if ((other.getStartTime() + other.getStopTime()) < start_time) {
                return false;
            }
        }

        //satisfied by default
        return true;
    }

    /**
     *
     * verifies a condition: this and no other at the same time
     *
     * @param other - left argument
     */
    private boolean excludes(ConsNode other) {

        //other does not exist!
        if ((other.getStartTime() < 0) || (other.getStopTime() <= 0)) {
            return true;
        }

        //I do not exist!
        if ((start_time < 0) || (stop_time < 0)) {
            return true;
        }

        //if both exist
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time > 0)) {

            //start time is in other
            if ((other.getStartTime() < start_time) && (start_time < (other.getStartTime() + other.getStopTime()))) {
                return false;
            }

            //stop time is in other
            if ((other.getStartTime() < (start_time + stop_time)) && ((start_time + stop_time) < (other.getStartTime() + other.getStopTime()))) {
                return false;
            }

        }

        //if one exists
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time == 0)) {

            //start time is in other
            if ((other.getStartTime() < start_time) && (start_time < (other.getStartTime() + other.getStopTime()))) {
                return false;
            }


        }

        return true;
    }

    /**
     *
     * verifies a condition: the other starts before this
     *
     * @param other - left argument
     */
//it should be called: isLeftIncluded
    private boolean isLeftIncluded(ConsNode other) {

        //ignore if contraditory
        if (this.arg_id == other.arg_id) {
            return true;
        }


        //other does not exist!
        if ((other.getStartTime() < 0) || (other.getStopTime() <= 0)) {
            return true;
        }

        //I do not exist!
        if ((start_time < 0) || (stop_time < 0)) {
            return true;
        }

        //if both exist
        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time > 0)) {

            // if other starts after I start - no satisfied
            if (other.getStartTime() > start_time) {
                return false;
            }

            //if other stops before I start - no satisfied
            if ((other.getStartTime() + other.getStopTime()) < start_time) {
                return false;
            }


            //if I stop before the other stops - no satisfied  - maybe not used ???
            if ((other.getStartTime() + other.getStopTime()) > (start_time + stop_time)) {
                return false;
            }

        }


        if ((other.getStartTime() >= 0) && (other.getStopTime() > 0) && (start_time >= 0) && (stop_time == 0)) {

            // if other starts after I start - no satisfied
            if (other.getStartTime() > start_time) {
                return false;
            }

            //if other stops before I start - no satisfied
            if ((other.getStartTime() + other.getStopTime()) < start_time) {
                return false;
            }
        }

        //par default
        return true;
    }

//enum oper {unknown,rigthinc,include,exclude,precede,exist, morethan,lessthan,equal, and, or, not};
    /**
     *
     * sets the operator type for the node
     *
     * @param type1 - operator name
     */
    public void setConsNodeOperator(String type1) {
        //par default unkown
        this.my_oper = oper.unknown;

        //change it
        if (type1.equalsIgnoreCase("rightinc")) {
            this.my_oper = oper.rigthinc;
        }
        if (type1.equalsIgnoreCase("included")) {
            this.my_oper = oper.include;
        }
        if (type1.equalsIgnoreCase("excludes")) {
            this.my_oper = oper.exclude;
        }
        if (type1.equalsIgnoreCase("preceded")) {
            this.my_oper = oper.precede;
        }
        if (type1.equalsIgnoreCase("exists")) {
            this.my_oper = oper.exist;
        }
        if (type1.equalsIgnoreCase("morethan")) {
            this.my_oper = oper.morethan;
        }
        if (type1.equalsIgnoreCase("lessthan")) {
            this.my_oper = oper.lessthan;
        }
        if (type1.equalsIgnoreCase("equal")) {
            this.my_oper = oper.equal;
        }
        if (type1.equalsIgnoreCase("and")) {
            this.my_oper = oper.and;
        }
        if (type1.equalsIgnoreCase("or")) {
            this.my_oper = oper.or;
        }
        if (type1.equalsIgnoreCase("not")) {
            this.my_oper = oper.not;
        }
    }

    /**
     *
     * gets the operator type of the node
     *
     * @return operator type
     */
    public String getConsNodeOperatorToString() {

        if (this.my_oper == oper.rigthinc) {
            return "rightinc";
        }
        if (this.my_oper == oper.include) {
            return "included";
        }
        if (this.my_oper == oper.exclude) {
            return "excludes";
        }
        if (this.my_oper == oper.precede) {
            return "preceded";
        }
        if (this.my_oper == oper.exist) {
            return "exists";
        }
        if (this.my_oper == oper.morethan) {
            return "morethan";
        }
        if (this.my_oper == oper.lessthan) {
            return "lessthan";
        }
        if (this.my_oper == oper.equal) {
            return "equal";
        }
        if (this.my_oper == oper.and) {
            return "and";
        }
        if (this.my_oper == oper.or) {
            return "or";
        }
        if (this.my_oper == oper.not) {
            return "not";
        }

        return "unknown";
    }

    /**
     *
     * gets the operator type of the node
     *
     * @return operator type
     */
    public oper getConsNodeOperator() {
        return this.my_oper;
    }

    /**
     *
     * sets the node
     *
     * @param id1 - signal id
     * @param type1 - cons type (start/stop/unknown)
     */
    public void setArg(int id1, String type1) {
        this.arg_id = id1;

        //To Do: check it
        if (type1.equalsIgnoreCase("start")) {
            this.my_type = type.start;
        }
        if (type1.equalsIgnoreCase("stop")) {
            this.my_type = type.end;
        }
        if (type1.equalsIgnoreCase("none")) {
            this.my_type = type.none;
        }
    }

    /**
     *
     * sets the node
     *
     * @param id1 - signal id
     */
    public void setArg(int id1) {
        this.arg_id = id1;
        this.my_type = type.none;

    }

    /**
     *
     * gets cons id
     *
     * @return id
     */
    public int getArgId() {
        return this.arg_id;
    }

    public String getArgType() {

        if (this.my_type == type.start) {
            return "start";
        }
        if (this.my_type == type.end) {
            return "stop";
        }

        return "";
    }

    public void setValue(double value1) {
        this.value = value1;
    }

    public double getValue() {
        return this.value;
    }

    public boolean evaluate(int id, List<ShortSignal> temp_animation) {

        if ((this.left != null) && (this.right != null)) {

            //composed rule by an logical oporator

            //in this case sons are not final nodes

            if (this.my_oper == oper.and) {
                return this.left.evaluate(id, temp_animation) && this.right.evaluate(id, temp_animation);
            }
            if (this.my_oper == oper.or) {
                return this.left.evaluate(id, temp_animation) || this.right.evaluate(id, temp_animation);
            }

            // composed by any of other two

            //in this case sons are final nodes

            if (this.my_oper == oper.rigthinc) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }


                //MAY2011
                //it means at least one of them is not defined yet - and it is not exit operator - return 1 by convention;
                if ((this.left.getStartTime() < 0) || (this.right.getStartTime() < 0)) {
                    return true;
                }

                //check do I have to disintguish left from the right

                if (this.left.getArgId() == id) {
                    return this.left.isLeftIncluded(this.right);
                }

                // should be something about left included, maybe
                //if (this.right->getArgId()==id) return this.right->isLeftIncluded(this.left);

                if (this.right.getArgId() == id) {

                    return this.right.isLeftIncluded(this.left);

                    //if left has to preceded the right  and the left does exist then the rule is false

                    //MAY 2011 - all rules non inistantiated should be 1, no?
                    //if ( (this.left->getStartTime()>=0) &&( this.left->getStopTime()>0) ) return 0;

                    //left does not exist thus element may be added
                    //it does not exist becouse the element is empty, but it should be filled with the last apperance of the epression of this id

                    //MAY2011
                    //another condition should be condiedered here

                    //MAY 2011 else commented
                    //else
                    //return true;
                }

                // if any argument do not concern this rule probably the rule is true
                if ((this.left.getArgId() != id) && (this.right.getArgId() != id)) {
                    return true;
                }

                return false;
            }

            // CHECKED MAY 2011
            if (this.my_oper == oper.include) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }


                //MAY2011
                //it means at least one of them is not defined yet - and it is not exit operator - return 1 by convention;
                if ((this.left.getStartTime() < 0) || (this.right.getStartTime() < 0)) {
                    return true;
                }

                if (this.left.getArgId() == id) {
                    return this.left.isIncluded(this.right);
                }

                //deadlock
                //if (this.right->getArgId()==id) return this.right->isIncluded(this.left);

                if (this.right.getArgId() == id) {

                    //if left has to be included in the right
                    // and the left does exist then the rule is false becouse the right cannot embrace the left (is too late)
                    //otherwise no problem for right to occur

                    //TO DO: but if the left is finished then maybe we can add a new right element?
                    if ((this.left.getStartTime() >= 0)
                            && (this.left.getStopTime() > 0)
                            && ((this.left.getStartTime() + this.left.getStopTime()) < this.right.getStartTime())) {
                        return true;
                    }

                    if ((this.left.getStartTime() >= 0) && (this.left.getStopTime() > 0)) {
                        return false;
                    } //left does not exist thus element may be added
                    //it does not exist becouse the element is empty, but it should be filled with the last apperance of the epression of this id
                    else {
                        return true;
                    }
                }

                return false;
            }

            //the only one that is symmetric
            // CHECKED MAY 2011
            if (this.my_oper == oper.exclude) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }

                //MAY2011
                //it means at least one of them is not defined yet - and it is not exit operator - return 1 by convention;
                if ((this.left.getStartTime() < 0) || (this.right.getStartTime() < 0)) {
                    return true;
                }


                //ok
                if (this.left.getArgId() == id) {
                    return this.left.excludes(this.right);
                }
                if (this.right.getArgId() == id) {
                    return this.right.excludes(this.left);
                }

                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }
                return false;
            }

            // CHECKED MAY 2011
            if (this.my_oper == oper.precede) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }

                //MAY2011
                //it means at least one of them is not defined yet - and it is not exit operator - return 1 by convention;
                if ((this.left.getStartTime() < 0) || (this.right.getStartTime() < 0)) {
                    return true;
                }


                if (this.left.getArgId() == id) {
                    return this.left.isPreceded(this.right);
                }

                // should be something about left included, maybe
                //if (this.right->getArgId()==id) return this.right->isPreceded(this.left);

                if (this.right.getArgId() == id) {

                    return this.right.isPreceded(this.left);

                    //if left has to preceded the right and the left does exist then the rule is false

                    //MAY 2011 - commented any rule non ini should be 1,no?
                    //if ( (this.left->getStartTime()>=0) &&( this.left->getStopTime()>0)) return 0;

                    //left does not exist thus element may be added
                    //it does not exist becouse the element is empty, but it should be filled with the last apperance of the epression of this id

                    //MAY 2011 else commented

                    //another case should be considered here!!


                    //JUNE COMMENTED retun no sense
                    //else
                    //return true;
                }

                //probably true if do not concern any of two arguments
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }

                return false;
            }


            // WHAT IF y STOP is 0

            if (this.my_oper == oper.morethan) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }


                //check is it right to change the operator
                //if (this.left->getArgId()==id) return  this.left->morethan(this.right);
                //if (this.right->getArgId()==id) return this.right->lessthan(this.left);

                double arg_a = 0;
                double arg_b = 0;

                if (this.left.my_type == type.start) {
                    arg_a = this.left.start_time;
                }

                if ((this.left.my_type == type.end) && (this.left.stop_time > 0)) {
                    arg_a = (this.left.start_time + this.left.stop_time);
                }
                if ((this.left.my_type == type.end) && (this.left.stop_time == 0)) {
                    return true; //cannot be analized
                }
                if (this.left.my_type == type.none) {
                    arg_a = this.left.value;
                }

                if (this.right.my_type == type.start) {
                    arg_b = this.right.start_time;
                }
                if ((this.right.my_type == type.end) && (this.right.stop_time > 0)) {
                    arg_b = (this.right.start_time + this.right.stop_time);
                }
                if ((this.right.my_type == type.end) && (this.right.stop_time == 0)) {
                    return true; //cannot be analized
                }
                // thus the value is absolute
                // start = 0
                // stop = 10 - i.e. it has to stop in 10 sec and it does not mean that its duration is 10 sec
                if (this.right.my_type == type.none) {
                    arg_b = this.right.value;
                }


                //value unknown (return 2);
                if (arg_a == -1) {
                    return true;
                }
                if (arg_b == -1) {
                    return true;
                }


                //check the condition;
                if (arg_a > arg_b) {
                    return true;
                }

                return false;
            }

            if (this.my_oper == oper.lessthan) {

                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }


                //check is it right to change the operator
                //if (this.left->getArgId()==id) return  this.left->morethan(this.right);
                //if (this.right->getArgId()==id) return this.right->lessthan(this.left);

                double arg_a = 0;
                double arg_b = 0;

                if (this.left.my_type == type.start) {
                    arg_a = this.left.start_time;
                }

                if ((this.left.my_type == type.end) && (this.left.stop_time > 0)) {
                    arg_a = (this.left.start_time + this.left.stop_time);
                }
                if ((this.left.my_type == type.end) && (this.left.stop_time == 0)) {
                    return true; //cannot be analized
                }
                if (this.left.my_type == type.none) {
                    arg_a = this.left.value;
                }

                if (this.right.my_type == type.start) {
                    arg_b = this.right.start_time;
                }
                if ((this.right.my_type == type.end) && (this.right.stop_time > 0)) {
                    arg_b = (this.right.start_time + this.right.stop_time);
                }
                if ((this.right.my_type == type.end) && (this.right.stop_time == 0)) {
                    return true; //cannot be analized
                }
                // thus the value is absolute
                // start = 0
                // stop = 10 - i.e. it has to stop in 10 sec and it does not mean that its duration is 10 sec
                if (this.right.my_type == type.none) {
                    arg_b = this.right.value;
                }

                //value unknown (return 2);
                if (arg_a == -1) {
                    return true;
                }
                if (arg_b == -1) {
                    return true;
                }

                //check the condition;
                if (arg_a < arg_b) {
                    return true;
                }


                return false;
            }

            // CHECKED MAY 2011
            if (this.my_oper == oper.equal) {
                //MAY2011
                //if this rules does not concern then 1..
                if ((this.right.getArgId() != id) && (this.left.getArgId() != id)) {
                    return true;
                }


                //if (this.left.getArgId()==id) return  this.left.equal(this.right);
                //if (this.right.getArgId()==id) return this.right.equal(this.left);

                double arg_a = 0;
                double arg_b = 0;

                //take avalue
                if (this.left.my_type == type.start) {
                    arg_a = this.left.start_time;
                }

                //not used
                if ((this.left.my_type == type.end) && (this.left.stop_time > 0)) {
                    arg_a = (this.left.start_time + this.left.stop_time);
                }
                if ((this.left.my_type == type.end) && (this.left.stop_time == 0)) {
                    return true; //cannot be analized
                }
                //not used
                if (this.left.my_type == type.none) {
                    arg_a = this.left.value;
                }

                //take a value
                if (this.right.my_type == type.start) {
                    arg_b = this.right.start_time;
                }

                //not used
                if ((this.right.my_type == type.end) && (this.right.stop_time > 0)) {
                    arg_b = (this.right.start_time + this.right.stop_time);
                }
                if ((this.right.my_type == type.end) && (this.right.stop_time == 0)) {
                    return true; //cannot be analized
                }

                // thus the value is absolute
                // start = 0
                // stop = 10 - i.e. it has to stop in 10 sec and it does not mean that its duration is 10 sec

                //not used
                if (this.right.my_type == type.none) {
                    arg_b = this.right.value;
                }

                //starts are unknown - rule cannot be inistatiated - return by default 1

                //value unknown (return 2);
                if (arg_a == -1) {
                    return true;
                }
                if (arg_b == -1) {
                    return true;
                }

                //check the condition;

                //TODO : should plus minus = - otherwise it will dificult that happens

                if (arg_a == arg_b) {
                    return true;
                }
                if ((arg_a > arg_b) && ((arg_a - arg_b) < 0.3)) {
                    return true;
                }
                if ((arg_b > arg_a) && ((arg_b - arg_a) < 0.3)) {
                    return true;
                }

                //otherwise end
                return false;
            }

            //the empty node is not possible if there are two arguments
            //the empty node of the type arg is possible if there is only one son called cons

            //is it possible??? no it is not possible
            //if (this.oper1 == not) return  !(this.left->evaluate());


        }// and of if


        if ((this.left != null) && (this.right == null)) {

            //NoT
            if (this.my_oper == oper.not) {
                return !(this.left.evaluate(id, temp_animation));
            }

            //EXIST

            if (this.my_oper == oper.exist) {

                //ignore nosense - if exists already X when I want to include it
                if (this.left.arg_id == id) {
                    return true;
                }

                // loop all the list of mme

                for (ShortSignal temp_iterator : temp_animation) {

                    //is there there this.left.arg_id which has this.left.start> 0 and this.left.end > 0

                    //this consnode is operator exists
                    //his son is an argument of exist that why this. left. arg_id

                    //bulls
                    if (temp_iterator.id == this.left.arg_id) {
                        return true;
                    }//end of if

                }//end of for

                //otherwise false

                return false;
            }

            // empty argument to be evaluated at lower level

            if (this.my_oper == oper.unknown) {
                return this.left.evaluate(id, temp_animation);
            }

        }//end of left null rihgt null

        //NOT POSSIBLE!!!!
        //if (left == null) && (right != null) return ;

        if ((this.left == null) && (this.right == null)) {

            //nothing ???
            //one by default if the rule cannot be instatiated
            //MAY2011
            return true;

        }//end of two nulls


        //otherwise fails

        return false;

    }//end of method

    /**
     * fills left part of the rule
     *
     *
     * @return void
     */
    void fill_left(int id, double start_time, double stop_time) {

        if ((this.left == null) && (this.right == null)) {

            //check if the node corresponds to
            if (this.arg_id == id) {
                this.start_time = start_time;
                this.stop_time = stop_time;
            }

        }
        if (this.left != null) {
            left.fill_left(id, start_time, stop_time);
        }

        //is it possible?
        if (this.right != null) {
            right.fill_left(id, start_time, stop_time);
        }

    }

    /**
     * fills right part of the rule, used?
     *
     *
     * @param id
     * @param start_time
     * @param stop_time
     */
    public void fill_right(int id, double start_time, double stop_time) {

        if ((this.left == null) && (this.right == null)) {

            //check if the node corresponds to
            if (this.arg_id == id) {
                this.start_time = start_time;
                this.stop_time = stop_time;
            }

        }

        //Not possible
        //if (this.left!=null) left->fill_right(id,start_time,stop_time);

        if (this.right != null) {
            right.fill_right(id, start_time, stop_time);
        }

    }

    /**
     * fill a tree
     *
     *
     * @param id
     * @param stop_time
     * @param start_time
     */
    public void fill(int id, double start_time, double stop_time) {

        if ((this.left == null) && (this.right == null)) {

            //check if the node corresponds to
            if (this.arg_id == id) {
                this.start_time = start_time;
                this.stop_time = stop_time;

            }

            return;
        }

        if (this.left != null) {
            left.fill(id, start_time, stop_time);
        }
        if (this.right != null) {
            right.fill(id, start_time, stop_time);
        }

    }

    /**
     *
     */
    public void clean() {

        this.start_time = -1.0f;
        this.stop_time = -1.0f;

        if (this.left != null) {
            left.clean();
        }
        if (this.right != null) {
            right.clean();
        }

    }

    /**
     * evaluates an aperator
     *
     *
     * @return int 1 if satisfied, 1 if dont know, 0 if not satisfied, 0 if
     * exception
     */
    //not used????
 /*
     private   bool booleanop(bool l, oper oper1, bool r) {

     //if (oper1 == "and") return l && r;
     //if (oper1 == "or") return l || r;
     //if (oper1 == "not") return l || r;

     return 0;

     }
     *
     */
    /**
     *
     *
     *
     * @return start time
     */
    public double getStartTime() {
        return start_time;
    }

    /**
     *
     *
     *
     * @return stop time
     */
    public double getStopTime() {
        return stop_time;
    }

    /**
     *
     *
     *
     * @param time
     */
    public void setStartTime(double time) {
        this.start_time = time;
    }

    /**
     *
     *
     *
     * @param time
     */
    public void setStopTime(double time) {
        this.stop_time = time;
    }

    /**
     * self-explained
     *
     *
     * @param mtemp a type
     */
    public void setArgType(type mtemp) {

        this.my_type = mtemp;
    }

    public ConsNode copy() {

        ConsNode temp = new ConsNode();

        //rekursja

        //ConsNode *left;
        if (this.left != null) {
            temp.setLeft((this.left).copy());
        } else {
            temp.setLeft(null);
        }

        //ConsNode *right;
        if (this.right != null) {
            temp.setRight((this.right).clone());
        } else {
            temp.setRight(null);
        }

        //arg_id;
        temp.setArg(this.arg_id);

        //double start_time;
        temp.setStartTime(this.start_time);

        //double stop_time;
        temp.setStopTime(this.stop_time);

        temp.id = this.id;

        //double value;
        temp.setValue(this.value);

        //type my_type;
        temp.setArgType(this.my_type);

        //oper my_oper;
        temp.setConsNodeOperator(this.getConsNodeOperatorToString());

        //copy concerns list
        if (!(this.concerns.isEmpty())) {
            // all the id's of the cons
            for (Integer in : concerns) {

                temp.concerns.add(new Integer(in));

            }//end of for
        }//end of if

        return temp;
    }
}
