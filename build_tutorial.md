## ANT Installation and Setup

You can download ANT from [https://ant.apache.org/](https://ant.apache.org/). Download the binary distribution, unzip it, and move it to a directory of your choice. After that, set these environment variables:
1. ANT_HOME: <the-unzipped-ANT-directory>
2. Path: add the <the-unzipped-ANT-directory\bin>
3. JAVA_HOME: <your-default-JDK-directory> (you might already have this set)

You are ready to use ANT! (don't forget to close the existing command line window, though)

## Basic

We use ANT to perform the build. It does the necessary clean up, compilation, and packaging. If the project depends on another projects which have not been packaged, the script will automatically calls the build scripts of those projects (and thus taking care of the dependency problem).

This is the example of AnimationCore's ``build_manual.xml`` as of commit 39b3e6336ba2c14cb2ad76a99e8ef4dedf27176b:
```xml
<?xml version="1.0"?>
<project name="AnimationCore" default="build" basedir="." xmlns:if="ant:if" xmlns:unless="ant:unless">

    <property name="src.dir" location="src" />
    <property name="classes.dir" location="compilation" />
    
    <property name="jar.dir" location="../../bin/Common/Lib/Internal" />
    <property name="jar.output" location="${jar.dir}/${ant.project.name}.jar" />
    
    <target name="delete.jar.output">
        <delete file="${jar.output}"/>
    </target>
    
    <target name="delete.classes">
        <delete dir="${classes.dir}"/>
    </target>    

    <target name="clean" depends="delete.jar.output, delete.classes"/>
    
    <target name="makedir">
        <mkdir dir="${classes.dir}" />
        <mkdir dir="${jar.dir}" />
    </target>
    
    <!-- DEPENDENCY TASKS HERE -->
    <target name="build.dependency.util" if="${need.util}">
        <ant antfile="../Util/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
    </target>
    <!-- -->
    
    <path id="path.jars">
        <fileset dir="${jar.dir}">
            <include name="Util.jar"/>
        </fileset>    
    </path>
    
    <target name="build.dependencies">
        <!-- DEPENDENCY TASKS HERE -->
        <condition property="need.util">
            <not>
                <and>
                    <available file="${jar.dir}" type="dir"/>
                    <available file="${jar.dir}/Util.jar" type="file"/>
                </and>
            </not>
        </condition>
        <antcall target="build.dependency.util"/>
        <!-- -->
    </target>
    
    <target name="compile" depends="delete.classes, makedir, build.dependencies">
        <javac srcdir="${src.dir}" destdir="${classes.dir}" includeAntRuntime="false">
            <classpath refid="path.jars"/>
        </javac>
        <copy todir="${classes.dir}">
          <fileset dir="${src.dir}">
            <exclude name="**/*.java"/>
          </fileset>
        </copy>
    </target>
    
    <target name="build" depends="clean, compile">
        <pathconvert targetos="unix" property="path.jars.manifest.converted" refid="path.jars" pathsep=" ">
            <map from="${jar.dir}" to="."/>
        </pathconvert>   
        <jar destfile="${jar.output}" basedir="${classes.dir}">
            <manifest>
                <attribute name="Class-Path" value="${path.jars.manifest.converted}"/>
            </manifest>
        </jar>
        <antcall target="delete.classes"/>
    </target>
    
</project>
```

The name of the project is obviously the name of the project. However, the ``<property name="jar.output" location="${jar.dir}/${ant.project.name}.jar" />`` instruction also sets the project name as the name of the output JAR file. This is just an existing convention which this build script makes explicit.

In a ANT script, we can specify tasks. Each task has a name. We create a task in this way:
```xml
<target name="task.name">
    <!-- perform the task -->
</target>
```

A task may have dependencies. For example, if we want to run ``task.a``, but it depends on ``task.b`` and ``task.c``, then we can do this:
```xml
<target name="task.c">
    <!-- perform the task -->
</target>
<target name="task.b">
    <!-- perform the task -->
</target>
<target name="task.a" depends="task.b, task.c">
    <!-- perform the task -->
</target>
```

It is also possible to run a task only if a certain boolean variable is set to ``true``. To do that, we can create a task in this way:
```xml
<target name="task.name" if="${boolean.variable.name}">
    <!-- perform the task -->
</target>
```

We use this structure to build dependent JARs if the JAR is not found. If the JAR file is found, we don't rebuild the project. The reason is that some projects are depended on by many other projects. For example, many projects depend on the Util project. It does not make sense to rebuild Util many times. As an example, in the AnimationCore example above, we have the following:
```xml
<target name="build.dependency.util" if="${need.util}">
    <ant antfile="../Util/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
</target>

<condition property="need.util">
    <not>
        <and>
            <available file="${jar.dir}" type="dir"/>
            <available file="${jar.dir}/Util.jar" type="file"/>
        </and>
    </not>
</condition>
<antcall target="build.dependency.util"/>
```

We set ``need.util`` according to whether the ``Util.jar`` file not exists (false if exists, true if not exists). Then, we call ``build.dependency.util``. If ``need.util`` is true, then the task is performed (i.e. the Util project will be built), otherwise ANT will just move on to the next things it has to do. **We have to create such block for every dependent project**. To build a dependent project, we have to specify the path of the dependent project's build script. In the case of this example, we do the following:
```xml
<ant antfile="../Util/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
```
The path in the ``antfile`` is relative to the path of the current project.

With the dependent JARs are taen care of, we are ready to compile our project. This task is to do the compilation:
```xml
<property name="src.dir" location="src" />
<property name="classes.dir" location="compilation" />

<property name="jar.dir" location="../../bin/Common/Lib/Internal" />

<path id="path.jars">
    <fileset dir="${jar.dir}">
        <include name="Util.jar"/>
    </fileset>    
</path>

<target name="compile" depends="delete.classes, makedir, build.dependencies">
    <javac srcdir="${src.dir}" destdir="${classes.dir}" includeAntRuntime="false">
        <classpath refid="path.jars"/>
    </javac>
    <copy todir="${classes.dir}">
      <fileset dir="${src.dir}">
        <exclude name="**/*.java"/>
      </fileset>
    </copy>
</target>
```
The ``javac`` command is to compile the java files inside ``${src.dir}`` and write the resulting class files to ``${classes.dir}``. Sometimes, we also put non-java files in the sorce folder. A case example when it might happen is when we create a GUI application and we have a few images we want to use in the application, we might put those images in the source directory. To deal with such cases, we also perform the ``copy`` command which copies all non-java files from ``${src.dir}`` to ``${classes.dir}``.

To perform the ``javac`` command, we must specify where to look for the dependent JARs. We do so by specifying ``<classpath refid="path.jars"/>``. The ``${jar.dir}`` is the path of the directory of the internal jar files relative to the project we are building.

After we have successfully compiled the project, we can then proceed with the packaging.
```xml
<project name="AnimationCore" default="build" basedir="." xmlns:if="ant:if" xmlns:unless="ant:unless">
    <property name="classes.dir" location="compilation" />
    
    <property name="jar.dir" location="../../bin/Common/Lib/Internal" />
    <property name="jar.output" location="${jar.dir}/${ant.project.name}.jar" />
    
    <target name="delete.classes">
        <delete dir="${classes.dir}"/>
    </target>  
    
    <path id="path.jars">
        <fileset dir="${jar.dir}">
            <include name="Util.jar"/>
        </fileset>    
    </path>
    
    <target name="build" depends="clean, compile">
        <pathconvert targetos="unix" property="path.jars.manifest.converted" refid="path.jars" pathsep=" ">
            <map from="${jar.dir}" to="."/>
        </pathconvert>   
        <jar destfile="${jar.output}" basedir="${classes.dir}">
            <manifest>
                <attribute name="Class-Path" value="${path.jars.manifest.converted}"/>
            </manifest>
        </jar>
        <antcall target="delete.classes"/>
    </target>
</project>
```
The ``default="build"`` in the ``project`` tag means that the default task to be executed is ``build`` task. This is our **convention** in all projects. If we want to execute another task, we have to specify it when running the ANT command. A JAR file is basically a kind of ZIP file containing the class files. However, it also has a special file named ``MANIFEST.MF`` inside. In this file, we have to specify the class path, which we do at ``<attribute name="Class-Path" value="${path.jars.manifest.converted}"/>``. In most cases (but not always), the class path is the same as the one we use for compilation. However, the class path for compilation is relative to the project, while the class path for the manifest file is relative to the resulting JAR file. For example, the manifest file of the AnimationCore.jar is as following:
```
Manifest-Version: 1.0
Ant-Version: Apache Ant 1.10.3
Created-By: 10.0.1+10 ("Oracle Corporation")
Class-Path: ./Util.jar
```
The class path in the manifest file is like that because the AnimationCore.jar and the Util.jar are in the same directory. The conversion of the class path is done by this instruction:
```xml
<pathconvert targetos="unix" property="path.jars.manifest.converted" refid="path.jars" pathsep=" ">
    <map from="${jar.dir}" to="."/>
</pathconvert>   
```
We convert ``${jar.dir}`` (which is ``../../bin/Common/Lib/Internal``) to ``.``.

Lastly, we delete the ``${classes.dir}`` by running ``<antcall target="delete.classes"/>``. This is just a clean up work.


## Conventions
### JAR Output Name
The JAR output file is the same as the project name
### JAR Output Directory
The JAR output directory is ``bin/Common/Lib/Internal/``
### Java Version
The resulting JAR must be runnable by JRE 1.8
### Source Directory Name
The name is ``src``. It is a long-standing convention which we make explicit here. It is okay to have the second (or third, or fourth, or fifth, etc) source directory if you want, just be careful to adjust the compilation instruction accordingly.
### Build Script File Name
The name is ``build_manual.xml``. **Do not** name it ``build.xml`` because that is the file name of the ANT script used by Netbeans for its own build. Netbeans (which we are using by default) uses ANT for its build (of course, it autogenerates the content according to the IDE settings).
### Default Build Task
The default task name is ``build``. This task will call the build scripts of the dependent projects (if necessary), compile the code, package it into a JAR file, and put the JAR file in the JAR output directory.
### Directory of Class Files
The default directory name is ``compilation``. **Do not** name it ``build`` because that is the directory used by Netbeans for its own compilation.
### Hygiene
Delete the class files from the compilation process once the JAR file is created.

## Not So Basic

There are some cases where the project depends on JARs from multiple directories. An example of this case is EnvironmentManager. Here is a snippet of the build script:
```xml
<target name="build.dependency.util" if="${need.util}">
    <ant antfile="../../core/Util/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
</target>   

<target name="build.dependency.mpeg4" if="${need.mpeg4}">
    <ant antfile="../../core/MPEG4/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
</target>

<target name="build.dependency.activemq" if="${need.activemq}">
    <ant antfile="../ActiveMQ/build_manual.xml" target="build" useNativeBasedir="true" inheritAll="false"/>
</target>

<path id="path.jars">
    <fileset dir="${jar.dir}">
        <include name="Util.jar"/>
        <include name="MPEG4.jar"/>
        <include name="ActiveMQ.jar"/>
    </fileset>
    <fileset dir="${external.jar.dir}">
        <include name="activemq-all-5.10.0.jar"/>
    </fileset>
</path>

<target name="compile" depends="delete.classes, makedir, build.dependencies">
    <javac srcdir="${src.dir}" destdir="${classes.dir}" includeAntRuntime="false">
        <classpath refid="path.jars"/>
    </javac>
    <copy todir="${classes.dir}">
      <fileset dir="${src.dir}">
        <exclude name="**/*.java"/>
      </fileset>
    </copy>
</target>

<target name="build" depends="clean, compile">
    <pathconvert targetos="unix" property="path.jars.manifest.converted" refid="path.jars" pathsep=" ">
        <map from="${jar.dir}" to="."/>
        <map from="${external.jar.dir}" to="../External"/>
    </pathconvert>
    <jar destfile="${jar.output}" basedir="${classes.dir}">
        <manifest>
            <attribute name="Class-Path" value="${path.jars.manifest.converted}"/>
        </manifest>
    </jar>
    <antcall target="delete.classes"/>
</target>
```

The first thing we have to make sure is that the path of the antfile in the ``build.dependency.<something>`` tasks are correct. Then, we have to ensure that all directories of the dependent JAR files are listed in the ``path.jars``. The compilation task remains the same. Lastly, in the ``pathconvert`` in the ``build`` task, we have to convert each of the directory paths in the ``path.jars`` to the path relative to the location of resulting JAR file (in this case, we convert ``${jar.dir}`` and ``${external.jar.dir}``). The resulting manifest is as following:
```
Manifest-Version: 1.0
Ant-Version: Apache Ant 1.10.3
Created-By: 10.0.1+10 ("Oracle Corporation")
Class-Path: ./ActiveMQ.jar ./MPEG4.jar ./Util.jar ../External/activemq
 -all-5.10.0.jar
```

As of commit 39b3e6336ba2c14cb2ad76a99e8ef4dedf27176b, Modular is the only case where the class paths for compilation and the class paths of the manifest file are different. This is not the usual case, but it is possible (e.g. if you load the class by using reflection). The class path used for compilation is needed to make the compilation succeeds while the class path used for the manifest file is needed to make the application runs correctly. I **assume** that Modular needs all other projects to run correctly. Here is a snipped of the Modular's build script:
```xml
<path id="path.jars">
    <fileset dir="${jar.dir}">
        <include name="Util.jar"/>
        <include name="Utilx.jar"/>
    </fileset>
    <fileset dir="${external.jar.dir}">
        <include name="jgraphx.jar"/>
    </fileset>
</path>

<target name="build.dependencies">
    <!-- BUILD ALL OTHER PROJECTS (if necessary) -->
</target>       

<target name="compile" depends="delete.classes, makedir, build.dependencies">
    <javac srcdir="${src.dir}" destdir="${classes.dir}" includeAntRuntime="false">
        <classpath refid="path.jars"/>
    </javac>
    <copy todir="${classes.dir}">
      <fileset dir="${src.dir}">
        <exclude name="**/*.java"/>
      </fileset>
    </copy>
</target>

<target name="build" depends="clean, compile">
    <path id="path.jars.manifest">
        <fileset dir="${jar.dir}">
            <include name="*.jar"/>
        </fileset>
        <fileset dir="${external.jar.dir}">
            <include name="jgraphx.jar"/>
        </fileset>
        <fileset dir="${internal.player.dir}">
            <include name="*.jar"/>
        </fileset>
    </path>

    <pathconvert targetos="unix" property="path.jars.manifest.converted" refid="path.jars.manifest" pathsep=" ">
        <map from="${jar.dir}" to="./Common/Lib/Internal"/>
        <map from="${external.jar.dir}" to="./Common/Lib/External"/>
        <map from="${internal.player.dir}" to="./Player/Lib/Internal"/>
    </pathconvert> 
    <jar destfile="${modular.jar.output}" basedir="${classes.dir}">
        <manifest>
            <attribute name="Main-Class" value="vib.application.modular.Modular"/>
            <attribute name="Class-Path" value="${path.jars.manifest.converted}"/>
        </manifest>
    </jar>
    <antcall target="delete.classes"/>
</target>
```
Notice that for compilation, Modular needs only Util.jar, Utilx.jar, and jgraphx.jar. However, for the manifest file, it needs all internal jars and jgraphx.jar (see the ``path.jars.manifest``). This is why the ``build.dependencies`` task builds all other projects. Unlike in other projects, the ``pathconvert`` instruction converts the path from ``path.jars.manifest``, not from ``path.jars``.
