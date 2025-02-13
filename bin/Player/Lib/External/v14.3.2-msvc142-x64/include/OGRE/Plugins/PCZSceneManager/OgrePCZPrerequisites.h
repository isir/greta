
#ifndef _OgrePCZPluginExport_H
#define _OgrePCZPluginExport_H

#ifdef PLUGIN_PCZSCENEMANAGER_STATIC_DEFINE
#  define _OgrePCZPluginExport
#  define PLUGIN_PCZSCENEMANAGER_NO_EXPORT
#else
#  ifndef _OgrePCZPluginExport
#    ifdef Plugin_PCZSceneManager_EXPORTS
        /* We are building this library */
#      define _OgrePCZPluginExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgrePCZPluginExport __declspec(dllimport)
#    endif
#  endif

#  ifndef PLUGIN_PCZSCENEMANAGER_NO_EXPORT
#    define PLUGIN_PCZSCENEMANAGER_NO_EXPORT 
#  endif
#endif

#ifndef PLUGIN_PCZSCENEMANAGER_DEPRECATED
#  define PLUGIN_PCZSCENEMANAGER_DEPRECATED __declspec(deprecated)
#endif

#ifndef PLUGIN_PCZSCENEMANAGER_DEPRECATED_EXPORT
#  define PLUGIN_PCZSCENEMANAGER_DEPRECATED_EXPORT _OgrePCZPluginExport PLUGIN_PCZSCENEMANAGER_DEPRECATED
#endif

#ifndef PLUGIN_PCZSCENEMANAGER_DEPRECATED_NO_EXPORT
#  define PLUGIN_PCZSCENEMANAGER_DEPRECATED_NO_EXPORT PLUGIN_PCZSCENEMANAGER_NO_EXPORT PLUGIN_PCZSCENEMANAGER_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef PLUGIN_PCZSCENEMANAGER_NO_DEPRECATED
#    define PLUGIN_PCZSCENEMANAGER_NO_DEPRECATED
#  endif
#endif

#endif /* _OgrePCZPluginExport_H */
