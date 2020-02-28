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
package greta.application.modular.tools.classloader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *
 * @author Andre-Marie Pez
 */
@SuppressWarnings("all")
public class CustomURLClassPath {

    final static String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
    final static String JAVA_VERSION;
    static {
        JAVA_VERSION = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<String>(){
                    public String run() {
                        return System.getProperty("java.version");
                    }
                });
    }

    private final ArrayList<URL> path = new ArrayList<URL>();
    private final Stack<URL> urls = new Stack<URL>();
    private final ArrayList<Loader> loaders = new ArrayList<Loader>();
    private final HashMap<String, Loader> lmap = new HashMap<String, Loader>();
    private URLStreamHandler jarHandler;
    private boolean closed = false;

    public CustomURLClassPath(URL[] urls, URLStreamHandlerFactory factory) {
        path.addAll(Arrays.asList(urls));
        push(urls);
        if (factory != null) {
            jarHandler = factory.createURLStreamHandler("jar");
        }
    }

    public CustomURLClassPath(URL[] urls) {
        this(urls, null);
    }

    public synchronized List<IOException> closeLoaders() {
        if (closed) {
            return Collections.emptyList();
        }
        List<IOException> result = new LinkedList<IOException>();
        for (Loader loader : loaders) {
            try {
                loader.close();
            } catch (IOException e) {
                result.add(e);
            }
        }
        closed = true;
        return result;
    }

    public synchronized void addURL(URL url) {
        if (closed) {
            return;
        }
        synchronized (urls) {
            if (url == null || path.contains(url)) {
                return;
            }

            urls.add(0, url);
            path.add(url);
        }
    }

    public URL[] getURLs() {
        synchronized (urls) {
            return path.toArray(new URL[path.size()]);
        }
    }

    public URL findResource(String name, boolean check) {
        Loader loader;
        for (int i = 0; (loader = getLoader(i)) != null; i++) {
            URL url = loader.findResource(name, check);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public Resource getResource(String name, boolean check) {
        Loader loader;
        for (int i = 0; (loader = getLoader(i)) != null; i++) {
            Resource res = loader.getResource(name, check);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public Enumeration<URL> findResources(final String name,
            final boolean check) {
        return new Enumeration<URL>() {
            private int index = 0;
            private URL url = null;

            private boolean next() {
                if (url != null) {
                    return true;
                } else {
                    Loader loader;
                    while ((loader = getLoader(index++)) != null) {
                        url = loader.findResource(name, check);
                        if (url != null) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            public boolean hasMoreElements() {
                return next();
            }

            @Override
            public URL nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                URL u = url;
                url = null;
                return u;
            }
        };
    }

    public Resource getResource(String name) {
        return getResource(name, true);
    }

    public Enumeration<Resource> getResources(final String name,
            final boolean check) {
        return new Enumeration<Resource>() {
            private int index = 0;
            private Resource res = null;

            private boolean next() {
                if (res != null) {
                    return true;
                } else {
                    Loader loader;
                    while ((loader = getLoader(index++)) != null) {
                        res = loader.getResource(name, check);
                        if (res != null) {
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            public boolean hasMoreElements() {
                return next();
            }

            @Override
            public Resource nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                Resource r = res;
                res = null;
                return r;
            }
        };
    }

    public Enumeration<Resource> getResources(final String name) {
        return getResources(name, true);
    }

    private synchronized Loader getLoader(int index) {
        if (closed) {
            return null;
        }
        while (loaders.size() < index + 1) {
            URL url;
            synchronized (urls) {
                if (urls.empty()) {
                    return null;
                } else {
                    url = urls.pop();
                }
            }
            String urlNoFragString = urlNoFragString(url);
            if (lmap.containsKey(urlNoFragString)) {
                continue;
            }
            // Otherwise, create a new Loader for the URL.
            Loader loader;
            try {
                loader = getLoader(url);
                URL[] urls = loader.getClassPath();
                if (urls != null) {
                    push(urls);
                }
            } catch (IOException e) {
                continue;
            }
            loaders.add(loader);
            lmap.put(urlNoFragString, loader);
        }
        return loaders.get(index);
    }

    private Loader getLoader(final URL url) throws IOException {
        try {
            return java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<Loader>() {
                        @Override
                        public Loader run() throws IOException {
                            String file = url.getFile();
                            if (file != null && file.endsWith("/")) {
                                if ("file".equals(url.getProtocol())) {
                                    return new FileLoader(url);
                                } else {
                                    return new Loader(url);
                                }
                            } else {
                                return new JarLoader(url, jarHandler, lmap);
                            }
                        }
                    });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    private void push(URL[] us) {
        synchronized (urls) {
            for (int i = us.length - 1; i >= 0; --i) {
                urls.push(us[i]);
            }
        }
    }

    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            File f = new File(st.nextToken());
            try {
                f = new File(f.getCanonicalPath());

            } catch (IOException x) {
                // use the non-canonicalized fileName
            }
            try {
                urls[count++] = (URL) Factory.ParseUtil_fileToEncodedURL.invoke(null, f);
            } catch (Exception x) {
            }
        }

        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    public URL checkURL(URL url) {
        try {
            check(url);
        } catch (Exception e) {
            return null;
        }

        return url;
    }

    static void check(URL url) throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            URLConnection urlConnection = url.openConnection();
            Permission perm = urlConnection.getPermission();
            if (perm != null) {
                try {
                    security.checkPermission(perm);
                } catch (SecurityException se) {
                    // fallback to checkRead/checkConnect for pre 1.2
                    // security managers
                    if ((perm instanceof java.io.FilePermission)
                            && perm.getActions().contains("read")) {
                        security.checkRead(perm.getName());
                    } else if ((perm instanceof java.net.SocketPermission)
                            && perm.getActions().contains("connect")) {
                        URL locUrl = url;
                        if (urlConnection instanceof JarURLConnection) {
                            locUrl = ((JarURLConnection) urlConnection).getJarFileURL();
                        }
                        security.checkConnect(locUrl.getHost(),
                                locUrl.getPort());
                    } else {
                        throw se;
                    }
                }
            }
        }
    }

    private static class Loader implements Closeable {

        private final URL base;
        private JarFile jarfile; // if this points to a jar file

        Loader(URL url) {
            base = url;
        }

        URL getBaseURL() {
            return base;
        }

        URL findResource(final String name, boolean check) {
            URL url;
            try {
                String spec = (String) Factory.ParseUtil_encodePath.invoke(null, name, false);
                url = new URL(base, spec);
            } catch (Exception e) {
                throw new IllegalArgumentException("name");
            }

            try {
                if (check) {
                    CustomURLClassPath.check(url);
                }

                URLConnection uc = url.openConnection();
                if (uc instanceof HttpURLConnection) {
                    HttpURLConnection hconn = (HttpURLConnection) uc;
                    hconn.setRequestMethod("HEAD");
                    if (hconn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                        return null;
                    }
                } else {
                    // our best guess for the other cases
                    InputStream is = url.openStream();
                    is.close();
                }
                return url;
            } catch (Exception e) {
                return null;
            }
        }

        Resource getResource(final String name, boolean check) {
            final URL url;
            try {
                String spec = (String) Factory.ParseUtil_encodePath.invoke(null, name, false);
                url = new URL(base, spec);
            } catch (Exception e) {
                throw new IllegalArgumentException("name");
            }
            final URLConnection uc;
            try {
                if (check) {
                    CustomURLClassPath.check(url);
                }
                uc = url.openConnection();
                InputStream in = uc.getInputStream();
                if (uc instanceof JarURLConnection) {
                    JarURLConnection juc = (JarURLConnection) uc;
                    jarfile = juc.getJarFile();
                    in.close();
                }
            } catch (Exception e) {
                return null;
            }
            return new Resource() {
                @Override
                public String getName() {
                    return name;
                }
                @Override
                public URL getURL() {
                    return url;
                }
                @Override
                public URL getCodeSourceURL() {
                    return base;
                }
                @Override
                public InputStream getInputStream() throws IOException {
                    return uc.getInputStream();
                }
                @Override
                public int getContentLength() throws IOException {
                    return uc.getContentLength();
                }
            };
        }

        Resource getResource(final String name) {
            return getResource(name, true);
        }

        @Override
        public void close() throws IOException {
            if (jarfile != null) {
                jarfile.close();
            }
        }

        URL[] getClassPath() throws IOException {
            return null;
        }
    }

    static class JarLoader extends Loader {

        private JarFile jar;
        private URL csu;
        private Object index;
        private Object metaIndex;
        private URLStreamHandler handler;
        private HashMap<String, Loader> lmap;
        private boolean closed = false;

        JarLoader(URL url, URLStreamHandler jarHandler,
                HashMap<String, Loader> loaderMap)
                throws IOException {
            super(new URL("jar", "", -1, url + "!/", jarHandler));
            csu = url;
            handler = jarHandler;
            lmap = loaderMap;

            if (!isOptimizable(url)) {
                ensureOpen();
            } else {
                String fileName = url.getFile();
                if (fileName != null) {
                    try{fileName = (String) Factory.ParseUtil_decode.invoke(null, fileName);} catch(Throwable t){}
                    File f = new File(fileName);
                    metaIndex = null;
                    try {metaIndex = Factory.MetaIndex_forJar_File.invoke(null, f);} catch (Exception ex) {}
                    if (metaIndex != null && !f.exists()) {
                        metaIndex = null;
                    }
                }
                if (metaIndex == null) {
                    ensureOpen();
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                ensureOpen();
                jar.close();
            }
        }

        JarFile getJarFile() {
            return jar;
        }

        private boolean isOptimizable(URL url) {
            return "file".equals(url.getProtocol());
        }

        private void ensureOpen() throws IOException {
            if (jar == null) {
                try {
                    java.security.AccessController.doPrivileged(
                            new java.security.PrivilegedExceptionAction<Void>() {
                                @Override
                                public Void run() throws IOException {
                                    jar = getJarFile(csu);
                                    try { index = Factory.JarIndex_getJarIndex_JarFile_MetaIndex.invoke(null, jar, metaIndex);} catch (Exception ex) {}
                                    if (index != null) {
                                        String[] jarfiles = new String [0];
                                        try { jarfiles = (String[]) Factory.JarIndex_getJarFiles.invoke(index);} catch (Exception ex) {}

                                        for (int i = 0; i < jarfiles.length; i++) {
                                            try {
                                                URL jarURL = new URL(csu, jarfiles[i]);
                                                String urlNoFragString = urlNoFragString(jarURL);
                                                if (!lmap.containsKey(urlNoFragString)) {
                                                    lmap.put(urlNoFragString, null);
                                                }
                                            } catch (MalformedURLException e) {
                                                continue;
                                            }
                                        }
                                    }
                                    return null;
                                }
                            }
                    );
                } catch (java.security.PrivilegedActionException pae) {
                    throw (IOException) pae.getException();
                }
            }
        }

        private JarFile getJarFile(URL url) throws IOException {
            try{
            if (isOptimizable(url)) {
                Object p = Factory.FileURLMapper_constructor_URL.newInstance(url);

                if (!((Boolean)Factory.FileURLMapper_exists.invoke(p))) {
                    throw new FileNotFoundException((String)Factory.FileURLMapper_getPath.invoke(p));
                }
                return new JarFile((String)Factory.FileURLMapper_getPath.invoke(p));
            }
            }catch(Exception e){
                throw new IOException(e);
            }
            URLConnection uc = getBaseURL().openConnection();
            uc.setRequestProperty(USER_AGENT_JAVA_VERSION, JAVA_VERSION);
            return ((JarURLConnection) uc).getJarFile();
        }

        Object getIndex() {
            try {
                ensureOpen();
            } catch (IOException e) {
                throw (InternalError) new InternalError().initCause(e);
            }
            return index;
        }

        Resource checkResource(final String name, boolean check, final JarEntry entry) {
            final URL url;
            try {
                String spec = (String) Factory.ParseUtil_encodePath.invoke(null, name, false);
                url = new URL(getBaseURL(), spec);
                if (check) {
                    CustomURLClassPath.check(url);
                }
            } catch (Exception e) {
                return null;
            }

            return new Resource() {
                @Override
                public String getName() {
                    return name;
                }
                @Override
                public URL getURL() {
                    return url;
                }
                @Override
                public URL getCodeSourceURL() {
                    return csu;
                }
                @Override
                public InputStream getInputStream() throws IOException {
                    return jar.getInputStream(entry);
                }
                @Override
                public int getContentLength() {
                    return (int) entry.getSize();
                }
                @Override
                public Manifest getManifest() throws IOException {
                    return jar.getManifest();
                }
                @Override
                public Certificate[] getCertificates() {
                    return entry.getCertificates();
                }
                @Override
                public CodeSigner[] getCodeSigners() {
                    return entry.getCodeSigners();
                }
            };
        }

        boolean validIndex(final String name) {
            String packageName = name;
            int pos;
            if ((pos = name.lastIndexOf("/")) != -1) {
                packageName = name.substring(0, pos);
            }

            String entryName;
            ZipEntry entry;
            Enumeration<JarEntry> enum_ = jar.entries();
            while (enum_.hasMoreElements()) {
                entry = enum_.nextElement();
                entryName = entry.getName();
                if ((pos = entryName.lastIndexOf("/")) != -1) {
                    entryName = entryName.substring(0, pos);
                }
                if (entryName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        URL findResource(final String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        @Override
        Resource getResource(final String name, boolean check) {
            try {
                if (metaIndex != null) {
                    if (!((Boolean)Factory.MetaIndex_mayContain_String.invoke(metaIndex, name))) {
                        return null;
                    }
                }
                ensureOpen();
            } catch (Exception e) {
                throw (InternalError) new InternalError().initCause(e);
            }
            final JarEntry entry = jar.getJarEntry(name);
            if (entry != null) {
                return checkResource(name, check, entry);
            }

            if (index == null) {
                return null;
            }

            HashSet<String> visited = new HashSet<String>();
            return getResource(name, check, visited);
        }

        Resource getResource(final String name, boolean check,
                Set<String> visited) {

            Resource res;
            Object[] jarFiles;
            boolean done = false;
            int count = 0;
            LinkedList jarFilesList = null;

            try {jarFilesList = (LinkedList) Factory.JarIndex_get_String.invoke(index, name);} catch (Exception ex) {}
            if (jarFilesList == null) {
                return null;
            }

            do {
                jarFiles = jarFilesList.toArray();
                int size = jarFilesList.size();
                while (count < size) {
                    String jarName = (String) jarFiles[count++];
                    JarLoader newLoader;
                    final URL url;

                    try {
                        url = new URL(csu, jarName);
                        String urlNoFragString = urlNoFragString(url);
                        if ((newLoader = (JarLoader) lmap.get(urlNoFragString)) == null) {
                            newLoader = AccessController.doPrivileged(
                                    new PrivilegedExceptionAction<JarLoader>() {
                                        @Override
                                        public JarLoader run() throws IOException {
                                            return new JarLoader(url, handler,
                                                    lmap);
                                        }
                                    });

                            Object newIndex = newLoader.getIndex();
                            if (newIndex != null) {
                                int pos = jarName.lastIndexOf("/");
                                try {
                                    Factory.JarIndex_merge_JarIndex_String.invoke(newIndex, this.index, (pos == -1
                                            ? null : jarName.substring(0, pos + 1)));
                                } catch (Exception ex) {}
                            }

                            lmap.put(urlNoFragString, newLoader);
                        }
                    } catch (java.security.PrivilegedActionException pae) {
                        continue;
                    } catch (MalformedURLException e) {
                        continue;
                    }

                    boolean visitedURL = !visited.add(urlNoFragString(url));
                    if (!visitedURL) {
                        try {
                            newLoader.ensureOpen();
                        } catch (IOException e) {
                            throw (InternalError) new InternalError().initCause(e);
                        }
                        final JarEntry entry = newLoader.jar.getJarEntry(name);
                        if (entry != null) {
                            return newLoader.checkResource(name, check, entry);
                        }
                        if (!newLoader.validIndex(name)) {
                            /* the mapping is wrong */
                            throw new RuntimeException("Invalid index");
                        }
                    }
                    if (visitedURL || newLoader == this
                            || newLoader.getIndex() == null) {
                        continue;
                    }
                    if ((res = newLoader.getResource(name, check, visited))
                            != null) {
                        return res;
                    }
                }
                try {jarFilesList = (LinkedList) Factory.JarIndex_get_String.invoke(index, name);} catch (Exception ex) {}

            } while (count < jarFilesList.size());
            return null;
        }

        @Override
        URL[] getClassPath() throws IOException {
            if (index != null) {
                return null;
            }

            if (metaIndex != null) {
                return null;
            }

            ensureOpen();
//            parseExtensionsDependencies();
//                if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(jar)) { // Only get manifest when necessary
//                    Manifest man = jar.getManifest();
//                    if (man != null) {
//                        Attributes attr = man.getMainAttributes();
//                        if (attr != null) {
//                            String value = attr.getValue(Name.CLASS_PATH);
//                            if (value != null) {
//                                return parseClassPath(csu, value);
//                            }
//                        }
//                    }
//                }
            return null;
        }

//        private void parseExtensionsDependencies() throws Exception {
//            Factory.ExtensionDependency.getMethod("checkExtensionsDependencies", JarFile.class).invoke(null, jar);
//        }

//        private URL[] parseClassPath(URL base, String value)
//                throws MalformedURLException {
//            StringTokenizer st = new StringTokenizer(value);
//            URL[] urls = new URL[st.countTokens()];
//            int i = 0;
//            while (st.hasMoreTokens()) {
//                String path = st.nextToken();
//                urls[i] = new URL(base, path);
//                i++;
//            }
//            return urls;
//        }
    }

    private static class FileLoader extends Loader {
        private File dir;

        FileLoader(URL url) throws IOException {
            super(url);
            if (!"file".equals(url.getProtocol())) {
                throw new IllegalArgumentException("url");
            }
            String path = url.getFile().replace('/', File.separatorChar);
            try{path = (String) Factory.ParseUtil_decode.invoke(null, path);} catch(Throwable t){}
            dir = (new File(path)).getCanonicalFile();
        }

        @Override
        URL findResource(final String name, boolean check) {
            Resource rsc = getResource(name, check);
            if (rsc != null) {
                return rsc.getURL();
            }
            return null;
        }

        @Override
        Resource getResource(final String name, boolean check) {
            final URL url;
            try {
                URL normalizedBase = new URL(getBaseURL(), ".");
                String spec = (String) Factory.ParseUtil_encodePath.invoke(null, name, false);
                url = new URL(getBaseURL(), spec);

                if (url.getFile().startsWith(normalizedBase.getFile()) == false) {
                    // requested resource had ../..'s in path
                    return null;
                }

                if (check) {
                    CustomURLClassPath.check(url);
                }

                final File file;
                if (name.contains("..")) {
                    file = (new File(dir, name.replace('/', File.separatorChar)))
                            .getCanonicalFile();
                    if (!((file.getPath()).startsWith(dir.getPath()))) {
                        /* outside of base dir */
                        return null;
                    }
                } else {
                    file = new File(dir, name.replace('/', File.separatorChar));
                }

                if (file.exists()) {
                    return new Resource() {
                        @Override
                        public String getName() {
                            return name;
                        }
                        @Override
                        public URL getURL() {
                            return url;
                        }
                        @Override
                        public URL getCodeSourceURL() {
                            return getBaseURL();
                        }
                        @Override
                        public InputStream getInputStream() throws IOException {
                            return new FileInputStream(file);
                        }
                        @Override
                        public int getContentLength() throws IOException {
                            return (int) file.length();
                        }
                    };
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }

    private static String urlNoFragString(URL url) {
        StringBuilder strForm = new StringBuilder();

        String protocol = url.getProtocol();
        if (protocol != null) {
            /* protocol is compared case-insensitive, so convert to lowercase */
            protocol = protocol.toLowerCase();
            strForm.append(protocol);
            strForm.append("://");
        }

        String host = url.getHost();
        if (host != null) {
            /* host is compared case-insensitive, so convert to lowercase */
            host = host.toLowerCase();
            strForm.append(host);

            int port = url.getPort();
            if (port == -1) {
                /* if no port is specificed then use the protocols
                 * default, if there is one */
                port = url.getDefaultPort();
            }
            if (port != -1) {
                strForm.append(":").append(port);
            }
        }

        String file = url.getFile();
        if (file != null) {
            strForm.append(file);
        }

        return strForm.toString();
    }
}
