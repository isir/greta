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
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * @author Andre-Marie Pez
 */
@SuppressWarnings("all")
public class CustomURLClassLoader extends SecureClassLoader implements Closeable {

    private final CustomURLClassPath ucp;
    private final AccessControlContext acc;

    public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new CustomURLClassPath(urls);
        this.acc = AccessController.getContext();
    }

    CustomURLClassLoader(URL[] urls, ClassLoader parent,
                   AccessControlContext acc) {
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new CustomURLClassPath(urls);
        this.acc = acc;
    }
    public CustomURLClassLoader(URL[] urls) {
        super();
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new CustomURLClassPath(urls);
        this.acc = AccessController.getContext();
    }

    CustomURLClassLoader(URL[] urls, AccessControlContext acc) {
        super();
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new CustomURLClassPath(urls);
        this.acc = acc;
    }

    public CustomURLClassLoader(URL[] urls, ClassLoader parent,
                          URLStreamHandlerFactory factory) {
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new CustomURLClassPath(urls, factory);
        acc = AccessController.getContext();
    }

    private final WeakHashMap<Closeable,Void> closeables = new WeakHashMap<Closeable,Void>();

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            if (url == null) {
                return null;
            }
            URLConnection urlc = url.openConnection();
            InputStream is = urlc.getInputStream();
            if (urlc instanceof JarURLConnection) {
                JarURLConnection juc = (JarURLConnection)urlc;
                JarFile jar = juc.getJarFile();
                synchronized (closeables) {
                    if (!closeables.containsKey(jar)) {
                        closeables.put(jar, null);
                    }
                }
            } else if (Factory.FileURLConnection.isInstance(urlc)) {
                synchronized (closeables) {
                    closeables.put(is, null);
                }
            }
            return is;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new RuntimePermission("closeClassLoader"));
        }
        List<IOException> errors = ucp.closeLoaders();

        // now close any remaining streams.

        synchronized (closeables) {
            Set<Closeable> keys = closeables.keySet();
            for (Closeable c : keys) {
                try {
                    c.close();
                } catch (IOException ioex) {
                    errors.add(ioex);
                }
            }
            closeables.clear();
        }

        if (errors.isEmpty()) {
            return;
        }

        IOException firstex = errors.remove(0);

        // Suppress any remaining exceptions

        for (IOException error: errors) {
            firstex.addSuppressed(error);
        }
        throw firstex;
    }

    protected void addURL(URL url) {
        ucp.addURL(url);
    }

    public URL[] getURLs() {
        return ucp.getURLs();
    }

    @Override
    protected Class<?> findClass(final String name)
         throws ClassNotFoundException
    {
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<Class>() {
                    @Override
                    public Class run() throws ClassNotFoundException {
                        String path = name.replace('.', '/').concat(".class");
                        Resource res = ucp.getResource(path, false);
                        if (res != null) {
                            try {
                                return defineClass(name, res);
                            } catch (Exception e) {
                                throw new ClassNotFoundException(name, e);
                            }
                        } else {
                            throw new ClassNotFoundException(name);
                        }
                    }
                }, acc);
        } catch (java.security.PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        }
    }

    private Package getAndVerifyPackage(String pkgname,
                                        Manifest man, URL url) {
        Package pkg = getPackage(pkgname);
        if (pkg != null) {
            if (pkg.isSealed()) {
                if (!pkg.isSealed(url)) {
                    throw new SecurityException(
                        "sealing violation: package " + pkgname + " is sealed");
                }
            } else {
                if ((man != null) && isSealed(pkgname, man)) {
                    throw new SecurityException(
                        "sealing violation: can't seal package " + pkgname +
                        ": already loaded");
                }
            }
        }
        return pkg;
    }

    private Class defineClass(String name, Resource res) throws Exception {
        long t0 = System.nanoTime();
        int i = name.lastIndexOf('.');
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            // Check if package already loaded.
            Manifest man = res.getManifest();
            if (getAndVerifyPackage(pkgname, man, url) == null) {
                try {
                    if (man != null) {
                        definePackage(pkgname, man, url);
                    } else {
                        definePackage(pkgname, null, null, null, null, null, null, null);
                    }
                } catch (IllegalArgumentException iae) {
                    // parallel-capable class loaders: re-verify in case of a
                    // race condition
                    if (getAndVerifyPackage(pkgname, man, url) == null) {
                        // Should never happen
                        throw new AssertionError("Cannot find package " +
                                                 pkgname);
                    }
                }
            }
        }
        java.nio.ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            try{Factory.PerfCounter_addElapsedTimeFrom.invoke(Factory.PerfCounter_getReadClassBytesTime.invoke(null), t0);}catch(Throwable t){}
            return defineClass(name, bb, cs);
        } else {
            byte[] b = res.getBytes();
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            try{Factory.PerfCounter_addElapsedTimeFrom.invoke(Factory.PerfCounter_getReadClassBytesTime.invoke(null), t0);}catch(Throwable t){}
            return defineClass(name, b, 0, b.length, cs);
        }
    }

    protected Package definePackage(String name, Manifest man, URL url)
        throws IllegalArgumentException
    {
        String path = name.replace('.', '/').concat("/");
        String specTitle = null, specVersion = null, specVendor = null;
        String implTitle = null, implVersion = null, implVendor = null;
        String sealed = null;
        URL sealBase = null;

        Attributes attr = man.getAttributes(path);
        if (attr != null) {
            specTitle   = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
            specVendor  = attr.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            implTitle   = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            implVendor  = attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            sealed      = attr.getValue(Attributes.Name.SEALED);
        }
        attr = man.getMainAttributes();
        if (attr != null) {
            if (specTitle == null) {
                specTitle = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
            }
            if (specVersion == null) {
                specVersion = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
            }
            if (specVendor == null) {
                specVendor = attr.getValue(Attributes.Name.SPECIFICATION_VENDOR);
            }
            if (implTitle == null) {
                implTitle = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            }
            if (implVersion == null) {
                implVersion = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            }
            if (implVendor == null) {
                implVendor = attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr.getValue(Attributes.Name.SEALED);
            }
        }
        if ("true".equalsIgnoreCase(sealed)) {
            sealBase = url;
        }
        return definePackage(name, specTitle, specVersion, specVendor,
                             implTitle, implVersion, implVendor, sealBase);
    }

    private boolean isSealed(String name, Manifest man) {
        String path = name.replace('.', '/').concat("/");
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Attributes.Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Attributes.Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    @Override
    public URL findResource(final String name) {
        /*
         * The same restriction to finding classes applies to resources
         */
        URL url = AccessController.doPrivileged(
            new PrivilegedAction<URL>() {
                @Override
                public URL run() {
                    return ucp.findResource(name, true);
                }
            }, acc);

        return url != null ? ucp.checkURL(url) : null;
    }

    @Override
    public Enumeration<URL> findResources(final String name)
        throws IOException
    {
        final Enumeration<URL> e = ucp.findResources(name, true);

        return new Enumeration<URL>() {
            private URL url = null;

            private boolean next() {
                if (url != null) {
                    return true;
                }
                do {
                    URL u = AccessController.doPrivileged(
                        new PrivilegedAction<URL>() {
                            @Override
                            public URL run() {
                                if (!e.hasMoreElements())
                                    return null;
                                return e.nextElement();
                            }
                        }, acc);
                    if (u == null)
                        break;
                    url = ucp.checkURL(u);
                } while (url == null);
                return url != null;
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

            @Override
            public boolean hasMoreElements() {
                return next();
            }
        };
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codesource){
        PermissionCollection perms = super.getPermissions(codesource);

        URL url = codesource.getLocation();

        Permission p;
        URLConnection urlConnection;

        try {
            urlConnection = url.openConnection();
            p = urlConnection.getPermission();
        } catch (java.io.IOException ioe) {
            p = null;
            urlConnection = null;
        }

        if (p instanceof FilePermission) {
            String path = p.getName();
            if (path.endsWith(File.separator)) {
                path += "-";
                p = new FilePermission(path, "read");
            }
        } else if ((p == null) && (url.getProtocol().equals("file"))) {
            String path = url.getFile().replace('/', File.separatorChar);
            try{path = (String) Factory.ParseUtil_decode.invoke(null, path);} catch(Throwable t){}
            if (path.endsWith(File.separator))
                path += "-";
            p =  new FilePermission(path, "read");
        } else {
            URL locUrl = url;
            if (urlConnection instanceof JarURLConnection) {
                locUrl = ((JarURLConnection)urlConnection).getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && (host.length() > 0))
                p = new SocketPermission(host, "connect,accept");
        }

        if (p != null) {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                final Permission fp = p;
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() throws SecurityException {
                        sm.checkPermission(fp);
                        return null;
                    }
                }, acc);
            }
            perms.add(p);
        }
        return perms;
    }
}
