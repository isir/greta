/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.draw;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/**
 *
 * @author Ken Prepin
 */
public abstract class ContextualMenu<DRAWABLE extends IMovableDrawable> extends JPanel implements ItemListener, ActionListener {

    public DRAWABLE drawable;

    public ContextualMenu(DRAWABLE drawable) {
        super();
        this.drawable = drawable;
        initContextualMenu();
    }
    public ContextualMenu(DRAWABLE drawable, Point2D position) {
        super();
        this.drawable = drawable;
        initContextualMenu();
    }


    public abstract void initContextualMenu(); /*{
        ButtonGroup groupeFormes = new ButtonGroup();
        JCheckBoxMenuItem itemBoite = new JCheckBoxMenuItem("soleil", true);
        itemBoite.addItemListener(this);
        groupeFormes.add(itemBoite);
        add(itemBoite);
        itemBoite = new JCheckBoxMenuItem("lune", false);
        itemBoite.addItemListener(this);
        groupeFormes.add(itemBoite);
        add(itemBoite);
    }*/

    public abstract void validateChanges() throws Exception;
}
