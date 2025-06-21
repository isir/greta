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
package greta.auxiliary.MeaningMiner;

//import greta.core.intentions.Intention;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Brian, Takeshi
 */
public interface MeaningMinerModule {
    public void processText(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException;
    //public List<Intention> processText_2(String input) throws TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException;
}
