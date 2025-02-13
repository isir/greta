
#ifndef _OgreParticleFXExport_H
#define _OgreParticleFXExport_H

#ifdef PLUGIN_PARTICLEFX_STATIC_DEFINE
#  define _OgreParticleFXExport
#  define PLUGIN_PARTICLEFX_NO_EXPORT
#else
#  ifndef _OgreParticleFXExport
#    ifdef Plugin_ParticleFX_EXPORTS
        /* We are building this library */
#      define _OgreParticleFXExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreParticleFXExport __declspec(dllimport)
#    endif
#  endif

#  ifndef PLUGIN_PARTICLEFX_NO_EXPORT
#    define PLUGIN_PARTICLEFX_NO_EXPORT 
#  endif
#endif

#ifndef PLUGIN_PARTICLEFX_DEPRECATED
#  define PLUGIN_PARTICLEFX_DEPRECATED __declspec(deprecated)
#endif

#ifndef PLUGIN_PARTICLEFX_DEPRECATED_EXPORT
#  define PLUGIN_PARTICLEFX_DEPRECATED_EXPORT _OgreParticleFXExport PLUGIN_PARTICLEFX_DEPRECATED
#endif

#ifndef PLUGIN_PARTICLEFX_DEPRECATED_NO_EXPORT
#  define PLUGIN_PARTICLEFX_DEPRECATED_NO_EXPORT PLUGIN_PARTICLEFX_NO_EXPORT PLUGIN_PARTICLEFX_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef PLUGIN_PARTICLEFX_NO_DEPRECATED
#    define PLUGIN_PARTICLEFX_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreParticleFXExport_H */
