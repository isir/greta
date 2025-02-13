
#ifndef _OgreRsImageCodecExport_H
#define _OgreRsImageCodecExport_H

#ifdef CODEC_RSIMAGE_STATIC_DEFINE
#  define _OgreRsImageCodecExport
#  define CODEC_RSIMAGE_NO_EXPORT
#else
#  ifndef _OgreRsImageCodecExport
#    ifdef Codec_RsImage_EXPORTS
        /* We are building this library */
#      define _OgreRsImageCodecExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreRsImageCodecExport __declspec(dllimport)
#    endif
#  endif

#  ifndef CODEC_RSIMAGE_NO_EXPORT
#    define CODEC_RSIMAGE_NO_EXPORT 
#  endif
#endif

#ifndef CODEC_RSIMAGE_DEPRECATED
#  define CODEC_RSIMAGE_DEPRECATED __declspec(deprecated)
#endif

#ifndef CODEC_RSIMAGE_DEPRECATED_EXPORT
#  define CODEC_RSIMAGE_DEPRECATED_EXPORT _OgreRsImageCodecExport CODEC_RSIMAGE_DEPRECATED
#endif

#ifndef CODEC_RSIMAGE_DEPRECATED_NO_EXPORT
#  define CODEC_RSIMAGE_DEPRECATED_NO_EXPORT CODEC_RSIMAGE_NO_EXPORT CODEC_RSIMAGE_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef CODEC_RSIMAGE_NO_DEPRECATED
#    define CODEC_RSIMAGE_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreRsImageCodecExport_H */
