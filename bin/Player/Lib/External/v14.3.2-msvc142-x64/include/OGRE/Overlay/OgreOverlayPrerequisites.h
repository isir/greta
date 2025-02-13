
#ifndef _OgreOverlayExport_H
#define _OgreOverlayExport_H

#ifdef OGREOVERLAY_STATIC_DEFINE
#  define _OgreOverlayExport
#  define OGREOVERLAY_NO_EXPORT
#else
#  ifndef _OgreOverlayExport
#    ifdef OgreOverlay_EXPORTS
        /* We are building this library */
#      define _OgreOverlayExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreOverlayExport __declspec(dllimport)
#    endif
#  endif

#  ifndef OGREOVERLAY_NO_EXPORT
#    define OGREOVERLAY_NO_EXPORT 
#  endif
#endif

#ifndef OGREOVERLAY_DEPRECATED
#  define OGREOVERLAY_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREOVERLAY_DEPRECATED_EXPORT
#  define OGREOVERLAY_DEPRECATED_EXPORT _OgreOverlayExport OGREOVERLAY_DEPRECATED
#endif

#ifndef OGREOVERLAY_DEPRECATED_NO_EXPORT
#  define OGREOVERLAY_DEPRECATED_NO_EXPORT OGREOVERLAY_NO_EXPORT OGREOVERLAY_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREOVERLAY_NO_DEPRECATED
#    define OGREOVERLAY_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreOverlayExport_H */
