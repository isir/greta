using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
#if UNITY_EDITOR
using UnityEditor;
#endif
using UnityEngine;
using UnityEngine.Rendering;

namespace Demonixis.UnityJSONSceneExporter
{
#if UNITY_EDITOR
    [CustomEditor(typeof(SceneExporter))]
    public class SceneExporterEditor : Editor
    {
        public override void OnInspectorGUI()
        {
            DrawDefaultInspector();

            var script = (SceneExporter)target;

            if (GUILayout.Button("Export"))
                script.Export();
        }
    }
#endif

    public class SceneExporter : MonoBehaviour
    {
        // Name, RelativePath
        private Dictionary<string, string> m_ExportedTextures = new Dictionary<string, string>();

        [SerializeField]
        private bool m_LogEnabled = true;
        [SerializeField]
        public bool m_ExportMeshData = true;
        [SerializeField]
        private Formatting m_JSONFormat = Formatting.Indented;
        [SerializeField]
        private bool m_ExportTextures = true;
        [SerializeField]
        private bool m_ExportAllScene = false;
        [SerializeField]
        private string m_ExportPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "UnitySceneExporter");
        [SerializeField]
        private string m_ExportFilename = "GameMap";
        [SerializeField]
        private bool m_MonoGameExport = false;

        [ContextMenu("Export")]
        public void Export()
        {
            var transforms = GetComponentsInChildren<Transform>(true);

#if UNITY_EDITOR
            if (m_ExportAllScene)
            {
                var tr = Resources.FindObjectsOfTypeAll(typeof(Transform));
                Array.Resize(ref transforms, tr.Length);

                for (var i = 0; i < tr.Length; i++)
                    transforms[i] = (Transform)tr[i];
            }
#endif
            // Name, Path

            var list = new List<UGameObject>();

            m_ExportedTextures.Clear();

            if (m_MonoGameExport)
                MonoGameExporter.BeginContentFile("Windows");

            foreach (var tr in transforms)
            {
                list.Add(ExportObject(tr));

                if (m_LogEnabled)
                    Debug.Log($"Exporter: {tr.name}");
            }

            if (!Directory.Exists(m_ExportPath))
                Directory.CreateDirectory(m_ExportPath);

            var json = JsonConvert.SerializeObject(list.ToArray(), m_JSONFormat);
            var path = Path.Combine(m_ExportPath, $"{m_ExportFilename}.json");

            File.WriteAllText(path, json);

            if (m_MonoGameExport)
            {
                MonoGameExporter.AddMap(path);
                File.WriteAllText(Path.Combine(m_ExportPath, "Content.mgcb"), MonoGameExporter.GetContentData());
            }

            if (m_LogEnabled)
                Debug.Log($"Exported: {list.Count} objects");
        }

        public UGameObject ExportObject(Transform tr)
        {
            var uGameObject = new UGameObject
            {
                Id = tr.GetInstanceID().ToString(),
                Name = tr.name,
                IsStatic = tr.gameObject.isStatic,
                IsActive = tr.gameObject.activeSelf,
                Transform = new UTransform
                {
                    Parent = tr.transform.parent?.GetInstanceID().ToString() ?? null,
                    LocalPosition = ToFloat3(tr.transform.localPosition),
                    LocalRotation = ToFloat3(tr.transform.localRotation.eulerAngles),
                    LocalScale = ToFloat3(tr.transform.localScale)
                }
            };

            var collider = tr.GetComponent<Collider>();
            if (collider != null)
            {
                var type = ColliderType.Box;
                if (collider is SphereCollider)
                    type = ColliderType.Sphere;
                else if (collider is CapsuleCollider)
                    type = ColliderType.Capsule;
                else if (collider is MeshCollider)
                    type = ColliderType.Mesh;

                var radius = 0.0f;
                if (collider is SphereCollider)
                    radius = ((SphereCollider)collider).radius;

                uGameObject.Collider = new UCollider
                {
                    Min = ToFloat3(collider.bounds.min),
                    Max = ToFloat3(collider.bounds.max),
                    Enabled = collider.enabled,
                    Radius = radius,
                    Type = (int)type
                };
            }

            var light = tr.GetComponent<Light>();
            if (light != null)
            {
                var lightType = 0;
                if (light.type == LightType.Point)
                    lightType = 1;
                else if (light.type == LightType.Spot)
                    lightType = 2;
                else
                    lightType = -1;

                uGameObject.Light = new ULight
                {
                    Intensity = light.intensity,
                    Radius = light.range,
                    Color = ToFloat4(light.color),
                    Angle = light.spotAngle,
                    ShadowsEnabled = light.shadows != LightShadows.None,
                    Enabled = light.enabled,
                    Type = lightType
                };
            }

            var reflectionProbe = tr.GetComponent<ReflectionProbe>();
            if (reflectionProbe != null)
            {
                uGameObject.ReflectionProbe = new UReflectionProbe
                {
                    BoxSize = ToFloat3(reflectionProbe.size),
                    BoxMin = ToFloat3(reflectionProbe.bounds.min),
                    BoxMax = ToFloat3(reflectionProbe.bounds.max),
                    Intensity = reflectionProbe.intensity,
                    ClipPlanes = new[]
                    {
                        reflectionProbe.nearClipPlane,
                        reflectionProbe.farClipPlane
                    },
                    Enabled = reflectionProbe.enabled,
                    IsBacked = reflectionProbe.refreshMode != ReflectionProbeRefreshMode.EveryFrame,
                    Resolution = reflectionProbe.resolution
                };
            }

            var renderer = tr.GetComponent<MeshRenderer>();
            if (renderer != null)
            {
                var uRenderer = new UMeshRenderer
                {
                    Name = renderer.name,
                    Materials = new UMaterial[renderer.sharedMaterials.Length],
                    Enabled = renderer.enabled
                };

                for (var i = 0; i < renderer.sharedMaterials.Length; i++)
                {
                    var sharedMaterial = renderer.sharedMaterials[i];
                    var normalMap = sharedMaterial.TryGetTexture("_NormalMap", "_BumpMap");
                    var emissiveMap = sharedMaterial.TryGetTexture("_EmissionMap");
                    var metallicMap = sharedMaterial.TryGetTexture("_MetallicGlossMap");
                    var occlusionMap = sharedMaterial.TryGetTexture("_OcclusionMap");

                    uRenderer.Materials[i] = new UMaterial
                    {
                        Scale = ToFloat2(sharedMaterial.mainTextureScale),
                        Offset = ToFloat2(sharedMaterial.mainTextureOffset),
                        ShaderName = sharedMaterial.shader?.name,
                        MainTexture = sharedMaterial.mainTexture?.name,
                        NormalMap = normalMap?.name,
                        AOMap = occlusionMap?.name,
                        EmissionMap = emissiveMap?.name,
                        EmissionColor = ToFloat3(sharedMaterial.TryGetColor("_EmissionColor")),
                        MetalicMap = metallicMap?.name,
                        Cutout = sharedMaterial.TryGetFloat("_Cutoff")
                    };

                    ExportTexture(sharedMaterial.mainTexture, renderer.name);
                    ExportTexture(normalMap, renderer.name);
                    ExportTexture(emissiveMap, renderer.name);
                    ExportTexture(metallicMap, renderer.name);
                    ExportTexture(occlusionMap, renderer.name);
                }

                if (m_ExportMeshData)
                {
                    var meshFilter = renderer.GetComponent<MeshFilter>();
                    var mesh = meshFilter.sharedMesh;
                    var subMeshCount = mesh.subMeshCount;
                    var filters = new UMeshFilter[subMeshCount];

                    for (var i = 0; i < subMeshCount; i++)
                    {
                        var subMesh = mesh.GetSubmesh(i);

                        filters[i] = new UMeshFilter
                        {
                            Positions = ToFloat3(subMesh.vertices),
                            Normals = ToFloat3(subMesh.normals),
                            UVs = ToFloat2(subMesh.uv),
                            Indices = subMesh.GetIndices(0),
                            MeshFormat = (int)subMesh.indexFormat
                        };
                    }

                    uRenderer.MeshFilters = filters;
                }

                uGameObject.Renderer = uRenderer;
            }

            var terrain = tr.GetComponent<Terrain>();
            if (terrain != null)
            {
                var terrainData = terrain.terrainData;
                var terrainLayer = terrainData.terrainLayers;

                var layers = new UTerrainLayer[terrainLayer.Length];
                for (var i = 0; i < layers.Length; i++)
                {
                    layers[i] = new UTerrainLayer
                    {
                        Name = terrainLayer[i].name,
                        Albedo = terrainLayer[i].diffuseTexture.name,
                        Normal = terrainLayer[i]?.normalMapTexture?.name,
                        Metallic = terrainLayer[i].metallic,
                        Smoothness = terrainLayer[i].smoothness,
                        SpecularColor = ToFloat3(terrainLayer[i].specular),
                        Offset = ToFloat2(terrainLayer[i].tileOffset),
                        Scale = ToFloat2(terrainLayer[i].tileSize)
                    };

                    ExportTexture(terrainLayer[i].diffuseTexture, terrain.name);
                    ExportTexture(terrainLayer[i].normalMapTexture, terrain.name);
                }

                var alphamaps = new string[terrainData.alphamapTextures.Length];
                for (var i = 0; i < alphamaps.Length; i++)
                {
                    alphamaps[i] = terrainData.alphamapTextures[i].name;
                    ExportTexture(terrainData.alphamapTextures[i], terrain.name);
                }

                ExportTexture(terrainData.heightmapTexture, terrain.name);

                var uTerrain = new UTerrain
                {
                    Enabled = terrain.enabled,
                    Name = terrain.name,
                    Size = ToFloat3(terrainData.size),
                    Layers = layers,
                    Alphamaps = alphamaps,
                    Heightmap = terrainData.heightmapTexture.name
                };

                uGameObject.Terrain = uTerrain;
            }

            return uGameObject;
        }

        private string ExportTexture(Texture texture, string folder)
        {
            if (texture == null)
                return null;

            if (!m_ExportTextures)
                return texture.name;

            if (!texture.isReadable)
            {
                Debug.LogWarning($"The texture {texture.name} is not readable so we can't export it.");
                return texture.name;
            }

            if (m_ExportedTextures.ContainsKey(texture.name))
                return m_ExportedTextures[texture.name];

            Texture2D tex2D = null;

            if (texture is RenderTexture)
            {
                RenderTexture.active = (RenderTexture)texture;
                tex2D = new Texture2D(texture.width, texture.height);
                tex2D.ReadPixels(new Rect(0, 0, texture.width, texture.height), 0, 0);
                tex2D.Apply();
            }
            else
                tex2D = ToTexture2D(texture);

            var bytes = tex2D.EncodeToPNG();
            var absoluteTexturePath = Path.Combine(m_ExportPath, "Textures", folder);

            if (!Directory.Exists(absoluteTexturePath))
                Directory.CreateDirectory(absoluteTexturePath);

            var textureName = texture.name;
            if (string.IsNullOrEmpty(textureName))
                textureName = Guid.NewGuid().ToString();

            try
            {
                File.WriteAllBytes(Path.Combine(absoluteTexturePath, $"{texture.name}.png"), bytes);

                var relativeTexturePath = Path.Combine("Textures", folder, $"{texture.name}.png");

                m_ExportedTextures.Add(texture.name, relativeTexturePath);

                if (m_MonoGameExport)
                    MonoGameExporter.AddTexture(relativeTexturePath);

                Debug.Log($"Texture {texture.name} was exported in {absoluteTexturePath}.");

                return relativeTexturePath;
            }
            catch (Exception ex)
            {
                Debug.Log(ex.Message);

                return texture.name;
            }
        }

        #region Utility Functions

        public Texture2D ToTexture2D(Texture nonReadWriteTexture)
        {
            var rt = RenderTexture.GetTemporary(nonReadWriteTexture.width, nonReadWriteTexture.height);
            RenderTexture.active = rt;

            Graphics.Blit(nonReadWriteTexture, rt);

            var tex2D = new Texture2D(rt.width, rt.height, TextureFormat.RGBA32, true);
            tex2D.ReadPixels(new Rect(0, 0, rt.width, rt.height), 0, 0, false);

            RenderTexture.active = null;

            return tex2D;
        }

        public static float[] ToFloat2(Vector2 vector) => new[] { vector.x, vector.y };
        public static float[] ToFloat3(Vector3 vector) => new[] { vector.x, vector.y, vector.z };
        public static float[] ToFloat3(Color color) => new[] { color.r, color.g, color.b };
        public static float[] ToFloat4(Color color) => new[] { color.r, color.g, color.b, color.a };

        public static float[] ToFloat2(Vector2[] vecs)
        {
            var list = new List<float>();

            foreach (var vec in vecs)
            {
                list.Add(vec.x);
                list.Add(vec.y);
            }

            return list.ToArray();
        }

        public static float[] ToFloat3(Vector3[] vecs)
        {
            var list = new List<float>();

            foreach (var vec in vecs)
            {
                list.Add(vec.x);
                list.Add(vec.y);
                list.Add(vec.z);
            }

            return list.ToArray();
        }

        #endregion
    }
}