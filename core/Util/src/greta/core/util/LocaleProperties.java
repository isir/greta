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
