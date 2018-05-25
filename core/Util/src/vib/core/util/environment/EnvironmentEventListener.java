/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.environment;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */

public interface EnvironmentEventListener {

    // 3 kinds of events:
    // events on tree changes
    // events on node changes
    // *events on animations*

    /**
     *
     * @param e
     */
    public void onTreeChange(TreeEvent e);

    /**
     *
     * @param e
     */
    public void onNodeChange(NodeEvent e);

    public void onLeafChange(LeafEvent event);

}
