/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.compilation;

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
