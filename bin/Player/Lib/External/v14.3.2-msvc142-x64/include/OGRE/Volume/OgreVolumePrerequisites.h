
#ifndef _OgreVolumeExport_H
#define _OgreVolumeExport_H

#ifdef OGREVOLUME_STATIC_DEFINE
#  define _OgreVolumeExport
#  define OGREVOLUME_NO_EXPORT
#else
#  ifndef _OgreVolumeExport
#    ifdef OgreVolume_EXPORTS
        /* We are building this library */
#      define _OgreVolumeExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreVolumeExport __declspec(dllimport)
#    endif
#  endif

#  ifndef OGREVOLUME_NO_EXPORT
#    define OGREVOLUME_NO_EXPORT 
#  endif
#endif

#ifndef OGREVOLUME_DEPRECATED
#  define OGREVOLUME_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREVOLUME_DEPRECATED_EXPORT
#  define OGREVOLUME_DEPRECATED_EXPORT _OgreVolumeExport OGREVOLUME_DEPRECATED
#endif

#ifndef OGREVOLUME_DEPRECATED_NO_EXPORT
#  define OGREVOLUME_DEPRECATED_NO_EXPORT OGREVOLUME_NO_EXPORT OGREVOLUME_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREVOLUME_NO_DEPRECATED
#    define OGREVOLUME_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreVolumeExport_H */
