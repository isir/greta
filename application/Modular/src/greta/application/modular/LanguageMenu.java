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
package greta.application.modular;

import greta.core.utilx.gui.ToolBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class LanguageMenu extends ToolBox.LocalizedJMenu{
    private String localePath = "./Locale";
    private String localExtention = ".ini";
    private String iconExtention = ".png";

    private ModularWindow parent;

    private JMenuItem selected;
    LanguageMenu(final ModularWindow parent){
        super("GUI.language");
        this.parent = parent;
        File localeDir = new File(localePath);
        for(File f : localeDir.listFiles()){
            if(f.getName().toLowerCase().endsWith(localExtention)){
                String languageCode = f.getName().substring(0, f.getName().length()-localExtention.length());
                final Locale l = Modular.getLocaleForLanguage(languageCode);

                if(l!=null){
                    final JMenuItem languageItem = new JCheckBoxMenuItem(l.getDisplayLanguage(l));
                    String iconFileName = localePath+"/"+languageCode+iconExtention;
                    if((new File(iconFileName)).exists()){
                        javax.swing.ImageIcon icon = new javax.swing.ImageIcon(iconFileName);
                        languageItem.setIcon(icon);
                    }
                    if(l.getLanguage().equals(Locale.getDefault().getLanguage()) &&
                       l.getCountry().equals(Locale.getDefault().getCountry())){
                        select(languageItem);
                    }
                    languageItem.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            select(languageItem);
                            Locale.setDefault(l);
                            UIManager.getDefaults().setDefaultLocale(l);
                            JComponent.setDefaultLocale(l);
                            updateLocale(LanguageMenu.this.parent,l);
                            for(greta.application.modular.modules.Module module : parent.getGraph().getModules()){
                                if(module.getFrame()!=null){
                                    updateLocale(module.getFrame(),l);
                                }
                            }
                            ModularSateIO.setSelectedLanguage(l);
                        }
                    });
                    this.add(languageItem);
                }
            }
        }
    }


    private void select(JMenuItem selected){
        if(this.selected!=null){
            this.selected.setSelected(false);
        }
        this.selected = selected;
        if(this.selected!=null){
            this.selected.setSelected(true);
        }
    }

    private static void updateLocale(java.awt.Component c, Locale l){
        c.setLocale(l);
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            javax.swing.JPopupMenu jpm =jc.getComponentPopupMenu();
            if(jpm != null) {
                updateLocale(jpm, l);
            }
        }
        java.awt.Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu)c).getMenuComponents();
        }
        else if (c instanceof java.awt.Container) {
            children = ((java.awt.Container)c).getComponents();
        }
        if (children != null) {
            for (java.awt.Component child : children) {
                updateLocale(child,l);
            }
        }
    }
}
