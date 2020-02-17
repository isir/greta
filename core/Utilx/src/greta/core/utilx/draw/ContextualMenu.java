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
package greta.core.utilx.draw;

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
