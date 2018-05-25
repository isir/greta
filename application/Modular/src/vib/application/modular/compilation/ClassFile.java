/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.compilation;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 *
 * @author Andre-Marie Pez
 */
public class ClassFile extends SimpleJavaFileObject{

    private String qualifiedName;
    private ByteArrayOutputStream baos;
    private String targetFileName;

    public ClassFile(String name) {
        super(URI.create("byte:///" + name.replaceAll("\\.", "/") + JavaFileObject.Kind.CLASS.extension), JavaFileObject.Kind.CLASS);
        baos = new ByteArrayOutputStream();
        this.qualifiedName = name;
        targetFileName = name.replaceAll("\\.", "/") + JavaFileObject.Kind.CLASS.extension;
    }
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        throw new IllegalStateException();
    }

    @Override
    public OutputStream openOutputStream() {
        return baos;
    }

    @Override
    public InputStream openInputStream() {
        throw new IllegalStateException();
    }

    public byte[] getBytes() {
        return baos.toByteArray();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getFileName(){
        return targetFileName;
    }

    public ByteArrayOutputStream getOutputStream(){
        return baos;
    }
}
