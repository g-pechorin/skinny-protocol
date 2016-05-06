using UnityEngine;
using System.Collections;
using System.Net.Sockets;
using System.Net;

public class UpClient : MonoBehaviour
{
	private TcpClient tcpClient = null;
	private Example.ProtoTry.UpDoot.EnStream upstream = null;
	public int port = HostRoot.defaultPort;

	void OnGUI()
	{
		if (null == tcpClient && GUILayout.Button("Connect"))
		{
			tcpClient = new TcpClient();

			tcpClient.Connect("127.0.0.1", port);

			Debug.Assert(null == upstream);
			upstream = new Example.ProtoTry.UpDoot.EnStream(tcpClient.GetStream());

			SendMessage("OnUpCon", SendMessageOptions.DontRequireReceiver);
		}

		if (null != tcpClient && GUILayout.Button(tcpClient.Connected ? "DisConnect" : "Lost ; Dispose"))
		{
			tcpClient.Close();
			tcpClient = null;
			upstream = null;
		}

		if (null == tcpClient)
			return;
	}

	void OnUpCon()
	{
		LogOut("Here we go ...");
	}

	[DisallowMultipleComponent]
	class UpDooted : MonoBehaviour
	{
		public int doot;
		private Vector3 lastPosition = Vector3.zero;
		public UpClient root;

		void Update()
		{
			var localPosition = transform.localPosition;
			if (localPosition == lastPosition)
			{
				return;
			}

			lastPosition = localPosition;

			root.upstream.move(
				doot,
				localPosition.x,
				localPosition.y,
				localPosition.z
			);
		}

		void OnDestroy()
		{
			root.upstream.close(doot);
		}
	}

	void Update()
	{
		foreach (Transform childTransform in gameObject.transform)
		{
			UpdateDoots(this, childTransform.gameObject);
		}
	}

	private static void UpdateDoots(UpClient root, GameObject gameObject)
	{
		UpDooted self;
		if (null == (self = gameObject.GetComponent<UpDooted>()))
		{
			root.upstream.spawn(
				(self = gameObject.AddComponent<UpDooted>()).doot = root.NextDoot(),
				((int)((gameObject.transform.parent.gameObject != root.gameObject) ? gameObject.transform.parent.GetComponent<UpDooted>().doot : 0))
			);
			self.root = root;
		}

		foreach (Transform childTransform in gameObject.transform)
		{
			UpdateDoots(root, childTransform.gameObject);
		}
	}

	int NextDoot()
	{
		int doot = Random.Range(1, 983);

		var allDoots = GetComponentsInChildren<UpDooted>();
		recur:
		foreach (var up in allDoots)
		{
			if (up.doot == doot)
			{
				++doot;
				goto recur;
			}
		}

		return doot;
	}

	private void LogOut(string message)
	{
		Debug.Log("[CLIENT:" + name + "] " + message);
	}
}
