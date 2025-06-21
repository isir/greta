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
