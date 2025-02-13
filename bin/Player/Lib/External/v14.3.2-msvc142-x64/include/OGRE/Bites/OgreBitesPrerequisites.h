
#ifndef _OgreBitesExport_H
#define _OgreBitesExport_H

#ifdef OGREBITES_STATIC_DEFINE
#  define _OgreBitesExport
#  define OGREBITES_NO_EXPORT
#else
#  ifndef _OgreBitesExport
#    ifdef OgreBites_EXPORTS
        /* We are building this library */
#      define _OgreBitesExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreBitesExport __declspec(dllimport)
#    endif
#  endif

#  ifndef OGREBITES_NO_EXPORT
#    define OGREBITES_NO_EXPORT 
#  endif
#endif

#ifndef OGREBITES_DEPRECATED
#  define OGREBITES_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREBITES_DEPRECATED_EXPORT
#  define OGREBITES_DEPRECATED_EXPORT _OgreBitesExport OGREBITES_DEPRECATED
#endif

#ifndef OGREBITES_DEPRECATED_NO_EXPORT
#  define OGREBITES_DEPRECATED_NO_EXPORT OGREBITES_NO_EXPORT OGREBITES_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREBITES_NO_DEPRECATED
#    define OGREBITES_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreBitesExport_H */
