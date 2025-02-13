
#ifndef _OgrePropertyExport_H
#define _OgrePropertyExport_H

#ifdef OGREPROPERTY_STATIC_DEFINE
#  define _OgrePropertyExport
#  define OGREPROPERTY_NO_EXPORT
#else
#  ifndef _OgrePropertyExport
#    ifdef OgreProperty_EXPORTS
        /* We are building this library */
#      define _OgrePropertyExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgrePropertyExport __declspec(dllimport)
#    endif
#  endif

#  ifndef OGREPROPERTY_NO_EXPORT
#    define OGREPROPERTY_NO_EXPORT 
#  endif
#endif

#ifndef OGREPROPERTY_DEPRECATED
#  define OGREPROPERTY_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREPROPERTY_DEPRECATED_EXPORT
#  define OGREPROPERTY_DEPRECATED_EXPORT _OgrePropertyExport OGREPROPERTY_DEPRECATED
#endif

#ifndef OGREPROPERTY_DEPRECATED_NO_EXPORT
#  define OGREPROPERTY_DEPRECATED_NO_EXPORT OGREPROPERTY_NO_EXPORT OGREPROPERTY_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREPROPERTY_NO_DEPRECATED
#    define OGREPROPERTY_NO_DEPRECATED
#  endif
#endif

#endif /* _OgrePropertyExport_H */
