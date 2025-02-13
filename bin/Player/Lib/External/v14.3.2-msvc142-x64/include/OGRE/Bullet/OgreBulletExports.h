
#ifndef _OgreBulletExport_H
#define _OgreBulletExport_H

#ifdef OGREBULLET_STATIC_DEFINE
#  define _OgreBulletExport
#  define OGREBULLET_NO_EXPORT
#else
#  ifndef _OgreBulletExport
#    ifdef OgreBullet_EXPORTS
        /* We are building this library */
#      define _OgreBulletExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreBulletExport __declspec(dllimport)
#    endif
#  endif

#  ifndef OGREBULLET_NO_EXPORT
#    define OGREBULLET_NO_EXPORT 
#  endif
#endif

#ifndef OGREBULLET_DEPRECATED
#  define OGREBULLET_DEPRECATED __declspec(deprecated)
#endif

#ifndef OGREBULLET_DEPRECATED_EXPORT
#  define OGREBULLET_DEPRECATED_EXPORT _OgreBulletExport OGREBULLET_DEPRECATED
#endif

#ifndef OGREBULLET_DEPRECATED_NO_EXPORT
#  define OGREBULLET_DEPRECATED_NO_EXPORT OGREBULLET_NO_EXPORT OGREBULLET_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OGREBULLET_NO_DEPRECATED
#    define OGREBULLET_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreBulletExport_H */
