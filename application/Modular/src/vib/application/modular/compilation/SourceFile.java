/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.compilation;

import java.io.IOException;
import java.net.URI;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 *
 * @author Andre-Marie Pez
 */
public class SourceFile extends SimpleJavaFileObject{

    private String qualifiedName;
    private String sourceCode;
    private String targetFileName;

    public SourceFile(String name, String code) {
        super(URI.create("string:///" + name.replaceAll("\\.", "/") + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
        this.qualifiedName = name;
        this.sourceCode = code;
        targetFileName = name.replaceAll("\\.", "/") + JavaFileObject.Kind.SOURCE.extension;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return sourceCode;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getFileName(){
        return targetFileName;
    }
}
