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
package greta.core.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class LocaleProperties extends IniManager{

    LocaleProperties(){
        super(Locale.UK.getLanguage()+"-"+Locale.UK.getCountry());
    }

    @Override
    protected String getFileName(String definition) {
        return "./Locale/"+definition+".ini";
    }

    @Override
    protected BufferedReader getBufferedReader(String fileName) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
    }


}
