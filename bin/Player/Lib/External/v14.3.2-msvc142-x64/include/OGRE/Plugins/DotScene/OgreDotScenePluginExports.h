
#ifndef _OgreDotScenePluginExport_H
#define _OgreDotScenePluginExport_H

#ifdef PLUGIN_DOTSCENE_STATIC_DEFINE
#  define _OgreDotScenePluginExport
#  define PLUGIN_DOTSCENE_NO_EXPORT
#else
#  ifndef _OgreDotScenePluginExport
#    ifdef Plugin_DotScene_EXPORTS
        /* We are building this library */
#      define _OgreDotScenePluginExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreDotScenePluginExport __declspec(dllimport)
#    endif
#  endif

#  ifndef PLUGIN_DOTSCENE_NO_EXPORT
#    define PLUGIN_DOTSCENE_NO_EXPORT 
#  endif
#endif

#ifndef PLUGIN_DOTSCENE_DEPRECATED
#  define PLUGIN_DOTSCENE_DEPRECATED __declspec(deprecated)
#endif

#ifndef PLUGIN_DOTSCENE_DEPRECATED_EXPORT
#  define PLUGIN_DOTSCENE_DEPRECATED_EXPORT _OgreDotScenePluginExport PLUGIN_DOTSCENE_DEPRECATED
#endif

#ifndef PLUGIN_DOTSCENE_DEPRECATED_NO_EXPORT
#  define PLUGIN_DOTSCENE_DEPRECATED_NO_EXPORT PLUGIN_DOTSCENE_NO_EXPORT PLUGIN_DOTSCENE_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef PLUGIN_DOTSCENE_NO_DEPRECATED
#    define PLUGIN_DOTSCENE_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreDotScenePluginExport_H */
