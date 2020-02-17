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
