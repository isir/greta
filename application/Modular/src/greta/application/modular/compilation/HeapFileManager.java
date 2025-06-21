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
package greta.application.modular.compilation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class HeapFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private ArrayList<ClassFile> classes = new ArrayList<ClassFile>();

    public HeapFileManager(StandardJavaFileManager sjfm) {
        super(sjfm);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        ClassFile clazz;
        if (sibling instanceof SourceFile) {
            SourceFile source = (SourceFile) sibling;
            clazz = new ClassFile(source.getQualifiedName());
        } else {
            clazz = new ClassFile(sibling.getName());
            System.err.println(sibling.getName()+" : ko");
        }
        classes.add(clazz);
        return clazz;
    }

    public List<ClassFile> getClasses(){
        return classes;
    }
}
