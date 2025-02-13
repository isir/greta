
#ifndef _OgreVulkanExport_H
#define _OgreVulkanExport_H

#ifdef RENDERSYSTEM_VULKAN_STATIC_DEFINE
#  define _OgreVulkanExport
#  define RENDERSYSTEM_VULKAN_NO_EXPORT
#else
#  ifndef _OgreVulkanExport
#    ifdef RenderSystem_Vulkan_EXPORTS
        /* We are building this library */
#      define _OgreVulkanExport __declspec(dllexport)
#    else
        /* We are using this library */
#      define _OgreVulkanExport __declspec(dllimport)
#    endif
#  endif

#  ifndef RENDERSYSTEM_VULKAN_NO_EXPORT
#    define RENDERSYSTEM_VULKAN_NO_EXPORT 
#  endif
#endif

#ifndef RENDERSYSTEM_VULKAN_DEPRECATED
#  define RENDERSYSTEM_VULKAN_DEPRECATED __declspec(deprecated)
#endif

#ifndef RENDERSYSTEM_VULKAN_DEPRECATED_EXPORT
#  define RENDERSYSTEM_VULKAN_DEPRECATED_EXPORT _OgreVulkanExport RENDERSYSTEM_VULKAN_DEPRECATED
#endif

#ifndef RENDERSYSTEM_VULKAN_DEPRECATED_NO_EXPORT
#  define RENDERSYSTEM_VULKAN_DEPRECATED_NO_EXPORT RENDERSYSTEM_VULKAN_NO_EXPORT RENDERSYSTEM_VULKAN_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef RENDERSYSTEM_VULKAN_NO_DEPRECATED
#    define RENDERSYSTEM_VULKAN_NO_DEPRECATED
#  endif
#endif

#endif /* _OgreVulkanExport_H */
