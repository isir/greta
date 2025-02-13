
#ifndef _OgreGLSLangProgramManagerExport_H
#define _OgreGLSLangProgramManagerExport_H

#ifdef PLUGIN_GLSLANGPROGRAMMANAGER_STATIC_DEFINE
#  define _OgreGLSLangProgramManagerExport
#  define PLUGIN_GLSLANGPROGRAMMANAGER_NO_EXPORT
#else
#  ifndef _OgreGLSLangProgramManagerExport
#    ifdef Plugin_GLSLangProgramManager_EXPORTS
        /* We are building this library */
#      define _OgreGLSLangProgramManagerExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreGLSLangProgramManagerExport __declspec(dllimport)
#    endif
#  endif

#  ifndef PLUGIN_GLSLANGPROGRAMMANAGER_NO_EXPORT
#    define PLUGIN_GLSLANGPROGRAMMANAGER_NO_EXPORT 
#  endif
#endif

#ifndef PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED
#  define PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED __declspec(deprecated)
#endif

#ifndef PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED_EXPORT
#  define PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED_EXPORT _OgreGLSLangProgramManagerExport PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED
#endif

#ifndef PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED_NO_EXPORT
#  define PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED_NO_EXPORT PLUGIN_GLSLANGPROGRAMMANAGER_NO_EXPORT PLUGIN_GLSLANGPROGRAMMANAGER_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef PLUGIN_GLSLANGPROGRAMMANAGER_NO_DEPRECATED
#    define PLUGIN_GLSLANGPROGRAMMANAGER_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreGLSLangProgramManagerExport_H */
