
#ifndef _OgreAssimpExport_H
#define _OgreAssimpExport_H

#ifdef CODEC_ASSIMP_STATIC_DEFINE
#  define _OgreAssimpExport
#  define CODEC_ASSIMP_NO_EXPORT
#else
#  ifndef _OgreAssimpExport
#    ifdef Codec_Assimp_EXPORTS
        /* We are building this library */
#      define _OgreAssimpExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreAssimpExport __declspec(dllimport)
#    endif
#  endif

#  ifndef CODEC_ASSIMP_NO_EXPORT
#    define CODEC_ASSIMP_NO_EXPORT 
#  endif
#endif

#ifndef CODEC_ASSIMP_DEPRECATED
#  define CODEC_ASSIMP_DEPRECATED __declspec(deprecated)
#endif

#ifndef CODEC_ASSIMP_DEPRECATED_EXPORT
#  define CODEC_ASSIMP_DEPRECATED_EXPORT _OgreAssimpExport CODEC_ASSIMP_DEPRECATED
#endif

#ifndef CODEC_ASSIMP_DEPRECATED_NO_EXPORT
#  define CODEC_ASSIMP_DEPRECATED_NO_EXPORT CODEC_ASSIMP_NO_EXPORT CODEC_ASSIMP_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef CODEC_ASSIMP_NO_DEPRECATED
#    define CODEC_ASSIMP_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreAssimpExport_H */
