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
package greta.auxiliary.player.ogre;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Placeholder Ogre Player implementation.
 * This is a stub to prevent module loading errors.
 * The actual 3D rendering functionality needs to be implemented.
 */
public class OgreFrame extends JFrame {
    
    public OgreFrame() {
        super("Ogre Player - Not Implemented");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(400, 300);
        
        // Add a simple message
        add(new JLabel("Ogre 3D Player - Implementation Required", SwingConstants.CENTER));
        
        System.out.println("WARNING: Ogre Player is not fully implemented. This is a placeholder module.");
    }
}