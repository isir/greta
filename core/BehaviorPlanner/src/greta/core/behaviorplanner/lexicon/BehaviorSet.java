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
package greta.core.behaviorplanner.lexicon;

import greta.core.behaviorplanner.mseconstraints.ConsNode;
import greta.core.util.parameter.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a set of {@code SignalItem} and a set of combinations of
 * these {@code SignalItem}.
 *
 * @author Andre-Marie Pez
 * @see greta.core.behaviorplanner.lexicon.SignalItem SignalItem
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.behaviorplanner.lexicon.SignalItem
 */
public class BehaviorSet implements Parameter<BehaviorSet> {

    private String name;
    private ArrayList<SignalItem> baseSignals;
    private ArrayList<ArrayList<SignalItem>> combinations;
    private String type;
    //only for MSE - the list of additional constraints
    private List<ConsNode> mseconstree;

    /**
     * Constructor
     *
     * @param name the name of the behavior set
     */
    public BehaviorSet(String name) {
        this.type = "simple";
        this.name = name;
        baseSignals = new ArrayList<SignalItem>();
        combinations = new ArrayList<ArrayList<SignalItem>>();
    }

    /**
     * Constructor
     *
     * @param name the name of the behavior set
     * @param type the type of this set
     */
    public BehaviorSet(String name, String type) {
        this.type = type;
        this.name = name;
        baseSignals = new ArrayList<SignalItem>();
        combinations = new ArrayList<ArrayList<SignalItem>>();
    }

    /**
     * Copy Constructor
     *
     * @param source the {@code BehaviorSet} to copy
     */
    public BehaviorSet(BehaviorSet source) {
            this.type = source.getType();
            this.name = source.getParamName();
            baseSignals = new ArrayList<SignalItem>();
            combinations = new ArrayList<ArrayList<SignalItem>>();
            for (SignalItem si : source.getBaseSignals()) {
                this.add(new SignalItem(si));
            }

            this.setMSEConstraints(source.getMSEConstraints());

    }

    /**
     * full reference do intention e.g. emotion-anger
     *
     * @return full reference to intention
     */
    @Override
    public String getParamName() {
        return type + ":" + name;
    }

    /**
     * at the moment 2 types are possible mse or simple (mme)
     *
     * @return the type {mse : simple}
     */
    public String getType() {
        return type;
    }

    @Override
    public void setParamName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of all valid combinations of {@code SignalItem}.
     *
     * @return the list of combinations of {@code SignalItem}
     */
    public List<? extends List<SignalItem>> getCombinations() {
        return combinations;
    }

    /**
     * Returns the list of all {@code SignalItem} used in this
     * {@code BehaviorSet}.
     *
     * @return the list of all {@code SignalItem} used
     */
    public List<SignalItem> getBaseSignals() {
        return baseSignals;
    }

    private boolean ifThen(String idItem1, String idItem2, boolean present1, boolean present2) {
        boolean removed = false;
        boolean item1present = false;
        boolean item2present = false;

        for (int i = 0; i < combinations.size(); ++i) {
            ArrayList<SignalItem> conbination = combinations.get(i);
            if (conbination == null) {
                continue;
            }
            item1present = false;
            item2present = false;
            for (SignalItem item : conbination) {
                if (item.getId().equalsIgnoreCase(idItem1)) {
                    item1present = true;
                } else if (item.getId().equalsIgnoreCase(idItem2)) {
                    item2present = true;
                }
                if (item1present && item2present) {
                    break;
                }
            }
            if (item1present == present1 && item2present != present2) {
                combinations.set(i, null);
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Adds a constraint.<br/>
     * All combinations of {@code SignalItem} in the set must verify this
     * condition :<br/>
     * if it contains {@code idItem1}, it must contain also
     * {@code idItem2}.<br/>
     * if it is not the case, the combination is set to {@code null}.
     *
     * @param idItem1 an id of a {@code SignalItem}
     * @param idItem2 the id of a {@code SignalItem} that must be present if the
     * first one is present
     * @return {@code true} if at least one combination is set to {@code null}.
     * {@code false} otherwise
     */
    public boolean ifPresentThenPresent(String idItem1, String idItem2) {
        return ifThen(idItem1, idItem2, true, true);
    }

    /**
     * Adds a constraint.<br/>
     * All combinations of {@code SignalItem} in the set must verify this
     * condition :<br/>
     * if it contains {@code idItem1}, it must not contain {@code idItem2}.<br/>
     * if it is not the case, the combination is set to {@code null}.
     *
     * @param idItem1 an id of a {@code SignalItem}
     * @param idItem2 the id of a {@code SignalItem} that must not be present if
     * the first one is present
     * @return {@code true} if at least one combination is set to {@code null}.
     * {@code false} otherwise
     */
    public boolean ifPresentThenNotPresent(String idItem1, String idItem2) {
        return ifThen(idItem1, idItem2, true, false);
    }

    /**
     * Adds a constraint.<br/>
     * All combinations of {@code SignalItem} must contain {@code idItem}.<br/>
     * A combination that not contains {@code idItem} is set to {@code null}.
     *
     * @param idItem the id of the {@code SignalItem} that must be present
     * @return {@code true} if at least one combination is set to {@code null}.
     * {@code false} otherwise
     */
    public boolean mustPresent(String idItem) {
        return ifThen(idItem, null, false, true); //combination without idItem will be deleted
    }

    /**
     * Clean all null combinations.<br/>
     * The functions
     * {@code ifPresentThenPresent}, {@code ifPresentThenNotPresent} and
     * {@code mustPresent} put {@code null} to some combination. So, the list of
     * all combinations must be clean after calling those three functions.
     *
     * @see #ifPresentThenPresent(java.lang.String, java.lang.String)
     * @see #ifPresentThenNotPresent(java.lang.String, java.lang.String)
     * @see #mustPresent(java.lang.String)
     */
    protected void cleanNullCombination() {
        ArrayList<ArrayList<SignalItem>> newCombinations = new ArrayList<ArrayList<SignalItem>>();

        for (ArrayList<SignalItem> combo : combinations) {
            if (combo != null) {
                newCombinations.add(combo);
            }
        }
        combinations = newCombinations;
    }

    /**
     * Adds a {@code SignalItem} in the behavior set.<br/>
     * All combinations are constructed by the same time.
     *
     * @param item the {@code SignalItem} to add in the set
     */
    public void add(SignalItem item) {
        if (item == null) {
            return; //never add a null object
        }
        // dont do it for MSE B.S. to much combinations possible - it goes out of heap
        if (!(type.equalsIgnoreCase("mse"))) {

            int oldsize = combinations.size();
            ArrayList<SignalItem> justOne = new ArrayList<SignalItem>();
            justOne.add(item);
            combinations.add(justOne);
            for (int i = 0; i < oldsize; ++i) {
                ArrayList<SignalItem> l = new ArrayList<SignalItem>(combinations.get(i));
                l.add(item);
                combinations.add(l);
            }
        } else {
            //System.out.println("MSE "+ name);
            combinations.clear();
        }

        baseSignals.add(item);
    }

    public void setMSEConstraints(List<ConsNode> constree) {
        this.mseconstree = constree;
    }

    public List<ConsNode> getMSEConstraints() {
        return mseconstree;
    }

    //assume no alternatives have id
    public int getIdofSignal(String name) {
        int id = -1;
        //find id of the signal
        for (SignalItem signalitem : getBaseSignals()) {
            String temp_name = signalitem.getMainShape().getName();
            if (temp_name.equalsIgnoreCase(name)) //assume there is only one!
            {
                id = Integer.parseInt(signalitem.getId());
            }
        }//end of for
        return id;

    }//end of getIdofSignal

    @Override
    public boolean equals(BehaviorSet other) {
        return this == other;
        //TODO make a better comparison
    }
}
