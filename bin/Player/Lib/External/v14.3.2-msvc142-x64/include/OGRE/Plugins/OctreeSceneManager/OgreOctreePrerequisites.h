
#ifndef _OgreOctreePluginExport_H
#define _OgreOctreePluginExport_H

#ifdef PLUGIN_OCTREESCENEMANAGER_STATIC_DEFINE
#  define _OgreOctreePluginExport
#  define PLUGIN_OCTREESCENEMANAGER_NO_EXPORT
#else
#  ifndef _OgreOctreePluginExport
#    ifdef Plugin_OctreeSceneManager_EXPORTS
        /* We are building this library */
#      define _OgreOctreePluginExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreOctreePluginExport __declspec(dllimport)
#    endif
#  endif

#  ifndef PLUGIN_OCTREESCENEMANAGER_NO_EXPORT
#    define PLUGIN_OCTREESCENEMANAGER_NO_EXPORT 
#  endif
#endif

#ifndef PLUGIN_OCTREESCENEMANAGER_DEPRECATED
#  define PLUGIN_OCTREESCENEMANAGER_DEPRECATED __declspec(deprecated)
#endif

#ifndef PLUGIN_OCTREESCENEMANAGER_DEPRECATED_EXPORT
#  define PLUGIN_OCTREESCENEMANAGER_DEPRECATED_EXPORT _OgreOctreePluginExport PLUGIN_OCTREESCENEMANAGER_DEPRECATED
#endif

#ifndef PLUGIN_OCTREESCENEMANAGER_DEPRECATED_NO_EXPORT
#  define PLUGIN_OCTREESCENEMANAGER_DEPRECATED_NO_EXPORT PLUGIN_OCTREESCENEMANAGER_NO_EXPORT PLUGIN_OCTREESCENEMANAGER_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef PLUGIN_OCTREESCENEMANAGER_NO_DEPRECATED
#    define PLUGIN_OCTREESCENEMANAGER_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreOctreePluginExport_H */
