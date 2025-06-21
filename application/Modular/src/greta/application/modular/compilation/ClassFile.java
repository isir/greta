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
