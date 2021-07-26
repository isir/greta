# UnityJSONSceneExporter
Export an Unity scene into the JSON file format. Not all components are supported, this project focuses on 3D export for now.

## How does it works?
1. Copy scripts in your project
2. Place the `SceneExport` script on a GameObject
3. Select a path, a filename
4. Click Export

All children of the GameObject that contains this script will be serialized into JSON and saved.
Now you can parse the JSON file with another game engine and reconstruct you scene.

## Exported Components

All exported component have a field called Enabled.

| Component | Fields |
|-----------|--------|
| MeshRenderer | Mesh, Materials |
| MeshFilter | Vertices, Indices, SubMeshes |
| Material | MainTexture Name, Offset, Scale |
| Collider | Min, Max, Radius |
| Light | Radius, Intensity, Type, Angle, Color, Shadows |
| Reflection Probe | Backed, Intensity, BoxSize, BoxMin, BoxMax, Resolution, Clip Planes |
| Terrain | Heightmap, Weightmap, Layers, Size |

## Texture export
Textures can be exported too and doesn't requires that the `Read/Write` flag is checked.

## External Exporter
For now there is a MonoGame exporter that generates an MGCB content file that contains textures and map file.

### GameObject
A GameObject contains by default all components. You've to check if those components are valid or not.

#### Fields
- ID
- Name
- Parent
- IsStatic
- IsActive
- LocalPosition/Rotation/Scale
- Renderer / Collider / Light / ReflectionProbe / Terrain

## License
This project is released under the MIT license.
