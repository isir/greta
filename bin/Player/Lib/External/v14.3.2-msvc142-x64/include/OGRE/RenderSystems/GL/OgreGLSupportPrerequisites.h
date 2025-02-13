
#ifndef _OgreGLExport_H
#define _OgreGLExport_H

#ifdef OGREGLSUPPORT_STATIC_DEFINE
#  define _OgreGLExport
#  define OGREGLSUPPORT_NO_EXPORT
#else
#  ifndef _OgreGLExport
#    ifdef OgreGLSupport_EXPORTS
        /* We are building this library */
#      define _OgreGLExport 
#    else
        /* We are using this library */
#      define _OgreGLExport 
#    endif
#  endif

#  ifndef OGREGLSUPPORT_NO_EXPORT
#    define OGREGLSUPPORT_NO_EXPORT 
#  endif
#endif

#ifndef OGREGLSUPPORT_DEPRECATED
#  define OGREGLSUPPORT_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREGLSUPPORT_DEPRECATED_EXPORT
#  define OGREGLSUPPORT_DEPRECATED_EXPORT _OgreGLExport OGREGLSUPPORT_DEPRECATED
#endif

#ifndef OGREGLSUPPORT_DEPRECATED_NO_EXPORT
#  define OGREGLSUPPORT_DEPRECATED_NO_EXPORT OGREGLSUPPORT_NO_EXPORT OGREGLSUPPORT_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREGLSUPPORT_NO_DEPRECATED
#    define OGREGLSUPPORT_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreGLExport_H */
