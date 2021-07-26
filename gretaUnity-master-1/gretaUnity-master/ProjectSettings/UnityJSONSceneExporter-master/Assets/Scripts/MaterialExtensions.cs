using UnityEngine;

namespace Demonixis.UnityJSONSceneExporter
{
    public static class MaterialExtensions
    {
        public static Texture TryGetTexture(this Material material, params string[] name)
        {
            foreach (var n in name)
            {
                if (material.HasProperty(n))
                    return material.GetTexture(n);
            }

            return null;
        }

        public static Color TryGetColor(this Material material, params string[] name)
        {
            foreach (var n in name)
            {
                if (material.HasProperty(n))
                    return material.GetColor(n);
            }

            return Color.black;
        }

        public static float TryGetFloat(this Material material, params string[] name)
        {
            foreach (var n in name)
            {
                if (material.HasProperty(n))
                    return material.GetFloat(n);
            }

            return 0.0f;
        }
    }
}
