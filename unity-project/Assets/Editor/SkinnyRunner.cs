

using UnityEditor;
using UnityEngine;
using System.IO;

public class SkinnyRunner : EditorWindow
{
	[MenuItem("Window/SkinnyRunner")]
	static void ShowWindow()
	{
		GetWindow<SkinnyRunner>().Show();
	}

	void OnGUI()
	{
		EditorSettings.serializationMode = SerializationMode.ForceText;

		if (GUILayout.Button("Re-Generate"))
		{
			File.Copy(SourceFolder + "skinny.properties", WorkingDirectory + "skinny.properties", true);

			var process = new System.Diagnostics.Process();
			process.StartInfo.FileName = "java";
			process.StartInfo.WorkingDirectory = WorkingDirectory;
			process.StartInfo.Arguments = "-jar \"" + SourceFolder + "sbt-launch.jar\" @skinny.properties -i " + Application.dataPath + " -o:cs " + Application.dataPath;

			/*
			process.StartInfo.RedirectStandardOutput= true;
			process.StartInfo.RedirectStandardError = true;

			process.StartInfo.UseShellExecute = false;
			*/
			process.ErrorDataReceived += (sender, e) =>
			{
				Debug.LogError(e.Data.ToString());
			};

			process.OutputDataReceived += (sender, e) =>
			{
				Debug.Log(e.Data.ToString());
			};

			process.Start();
			process.WaitForExit();
		}
	}

	public string SourceFolder
	{
		get
		{
			var assetPath = AssetDatabase.GetAssetPath(MonoScript.FromScriptableObject(this));
			return ProjectFolder + assetPath.Substring(0, assetPath.LastIndexOf('/') + 1);
		}
	}

	public string WorkingDirectory
	{
		get
		{
			return ProjectFolder + "Temp/";
		}
	}

	public string ProjectFolder
	{
		get
		{
			return Application.dataPath.Substring(0, Application.dataPath.LastIndexOf("/") + 1);
		}
	}
}
