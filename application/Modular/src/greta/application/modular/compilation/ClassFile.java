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
