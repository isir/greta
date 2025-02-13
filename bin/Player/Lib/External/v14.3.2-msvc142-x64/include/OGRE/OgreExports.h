
#ifndef _OgreExport_H
#define _OgreExport_H

#ifdef OGREMAIN_STATIC_DEFINE
#  define _OgreExport
#  define _OgrePrivate
#else
#  ifndef _OgreExport
#    ifdef OgreMain_EXPORTS
        /* We are building this library */
#      define _OgreExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreExport __declspec(dllimport)
#    endif
#  endif

#  ifndef _OgrePrivate
#    define _OgrePrivate 
#  endif
#endif

#ifndef OGRE_DEPRECATED
#  define OGRE_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGRE_DEPRECATED_EXPORT
#  define OGRE_DEPRECATED_EXPORT _OgreExport OGRE_DEPRECATED
#endif

#ifndef OGRE_DEPRECATED_NO_EXPORT
#  define OGRE_DEPRECATED_NO_EXPORT _OgrePrivate OGRE_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREMAIN_NO_DEPRECATED
#    define OGREMAIN_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreExport_H */
