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

import greta.application.modular.modules.Style;
import greta.core.util.IniManager;
import greta.core.util.speech.Speech;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 *
 * @author Andre-Marie Pez
 */
public class Modular {
    public static final String iconRessourceName = "greta/application/modular/joy3_.png";
    public static Image icon = Toolkit.getDefaultToolkit().getImage(Modular.class.getClassLoader().getResource(iconRessourceName));
    public static Image warnIcon = Toolkit.getDefaultToolkit().getImage(Modular.class.getClassLoader().getResource("greta/application/modular/warning.png"));

    private static String[] knownProblematicLAF = {};//"GTK+"};

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args){
        Locale.setDefault(ModularSateIO.getSelectedLanguage());
        Style.setMapper(ModularSateIO.getStyleMapper());
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                //try to load choosen look and feel
                boolean lafSet = false;
                try {
                    String choosenLAF = ModularSateIO.getLookAndFeel();
                    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()){
                        if(info.getClassName().equals(choosenLAF) && !isProblematicLAF(info)){
                            javax.swing.UIManager.setLookAndFeel(choosenLAF);
                            lafSet = true;
                        }
                    }
                }
                catch (Exception ex) {}

                //if the look and feel is not set we use the system look and feel
                if(!lafSet) {
                    if( ! isProblematicLAF(javax.swing.UIManager.getSystemLookAndFeelClassName())){
                        try {
                            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                        }
                        catch (Exception e) {}
                    }
                }

                //create modular window
                ModularWindow mw = new ModularWindow();
                ModuleLoader.loadModularXML(mw, mw.getGraph());
                mw.loadPositions();
                mw.checkValidity();
                mw.setVisible(true);
                if(args.length>0) {
                    mw.openGraph(args[0]);
                }
                else{
                    if(ModularSateIO.isLoadLastFile()){
                        String fileName = ModularSateIO.getLastFile();
                        if(fileName!=null) {
                            mw.openGraph(fileName);
                        }
                    }
                }
            }
        });

        //TODO need to be removed (put it in ini file)
        Speech.setOriginal(true);

    }

    public static boolean isProblematicLAF(javax.swing.UIManager.LookAndFeelInfo lafInfo){
        for(String lafName : knownProblematicLAF){
            if(lafInfo.getName().equals(lafName)){
                return true;
            }
        }
        return false;
    }

    public static boolean isProblematicLAF(String lafClassName){
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()){
            if(info.getClassName().equals(lafClassName)){
                return isProblematicLAF(info);
            }
        }
        return false;
    }

    public static Locale getLocaleForLanguage(String languageCode){
        return
            IniManager.getJavaVersion()>=1.7 ? Locale.forLanguageTag(languageCode) : //to compile under older version of java, comment this line
            new Locale(languageCode);
    }

    public static String createRegularClassName(String aName){
        String clean = cleanName(aName);
        return clean.substring(0, 1).toUpperCase() + clean.substring(1);
    }

    public static String createRegularVariableName(String aName){
        String clean = cleanName(aName);
        return clean.substring(0, 1).toLowerCase() + clean.substring(1);
    }

    public static String cleanName(String aName){
        String strTemp = Normalizer.normalize(aName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(strTemp).replaceAll("").replaceAll("\\W", "_").replaceFirst("^(\\d)", "_$1");
    }
}
