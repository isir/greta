using System.Text;

namespace Demonixis.UnityJSONSceneExporter
{
    public static class MonoGameExporter
    {
        private static StringBuilder stringBuilder = new StringBuilder();

        public static void BeginContentFile(string target)
        {
            stringBuilder.Length = 0;

            AddLine("#----------------------------- Global Properties ----------------------------#\r\n");
            AddLine("");
            AddLine("/outputDir:bin/$(Platform)");
            AddLine($"/platform:{target}");
            AddLine("/config:");
            AddLine("/profile:HiDef");
            AddLine("/compress:False");
            AddLine("");
            AddLine("#-------------------------------- References --------------------------------#");
            AddLine("");
            AddLine("");
            AddLine("#---------------------------------- Content ---------------------------------#");
            AddLine("");
        }

        public static void AddTexture(string path)
        {
            const char search = '\\';
            const char replace = '/';

            var index = path.IndexOf(search);
            while (index != -1)
            {
                path = path.Replace(search, replace);
                index = path.IndexOf(search);
            }

            AddLine($"#begin {path}");
            AddLine("/importer:TextureImporter");
            AddLine("/processor:TextureProcessor");
            AddLine("/processorParam:ColorKeyColor=255,0,255,255");
            AddLine("/processorParam:ColorKeyEnabled=True");
            AddLine("/processorParam:GenerateMipmaps=False");
            AddLine("/processorParam:PremultiplyAlpha=True");
            AddLine("/processorParam:ResizeToPowerOfTwo=False");
            AddLine("/processorParam:MakeSquare=False");
            AddLine("/processorParam:TextureFormat=Color");
            AddLine($"/build:{path}");
            AddLine("");
        }

        public static void AddMap(string path)
        {
            AddLine($"#begin {path}");
            AddLine($"/copy:{path}");
            AddLine("");
        }

        public static string GetContentData() => stringBuilder.ToString();

        private static void AddLine(string line)
        {
            stringBuilder.Append($"{line}\r\n");
        }
    }
}
