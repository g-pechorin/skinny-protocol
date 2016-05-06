using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Net;


public class HostRoot : MonoBehaviour
{
	public const int defaultPort = 5967;

	private TcpListener tcpListener = null;
	private TcpClient tcpClient = null;
	private Example.ProtoTry.UpDoot.DeStream deStream = null;
	public GameObject prefab;
	public int port = defaultPort;

	void Start ()
	{
		Debug.Assert (0 == transform.childCount);
		Debug.Assert (null == tcpClient);
		Debug.Assert (null == tcpListener);
		Debug.Assert (0 == transform.childCount);

		tcpListener = new TcpListener (IPAddress.Parse ("127.0.0.1"), port);
		tcpListener.Start ();

		LogOut ("Powerup ...");
	}


	class Foo : Example.ProtoTry.UpDoot.Listener
	{
		HostRoot hostRoot;

		public Foo (HostRoot hostRoot)
		{
			this.hostRoot = hostRoot;
		}

		private Dictionary<int,GameObject> registry = new Dictionary<int,GameObject> ();

		void Example.ProtoTry.UpDoot.Listener.spawn (int id, int parent)
		{
			registry [id] = Instantiate (hostRoot.prefab);
			registry [id].transform.parent = ((0 == parent) ? hostRoot.gameObject : (registry [parent])).transform;
			registry [id].transform.localPosition = Vector3.zero;
		}

		void Example.ProtoTry.UpDoot.Listener.move (int id, float x, float y, float z)
		{
			registry [id].transform.localPosition = new Vector3 (x, y, z);
		}

		void Example.ProtoTry.UpDoot.Listener.close (int id)
		{
			Destroy (registry [id]);
			registry [id] = null;
		}
	};

	void Update ()
	{
		if (null != tcpClient && !tcpClient.Connected) {
			tcpClient.Close ();
			tcpClient = null;
			LogOut ("Tossed");
		}

		if (null == tcpClient && tcpListener.Pending ()) {
			tcpClient = tcpListener.AcceptTcpClient ();

			deStream = new Example.ProtoTry.UpDoot.DeStream (tcpClient.GetStream (), new Foo (this));

			LogOut ("Gotcha");
		}

		if (null == tcpClient)
			return;

		while (deStream.Pump ()) {
		}
	}

	void OnDestroy ()
	{
		Debug.Assert (null != tcpListener);

		if (null != tcpClient)
			tcpClient.Close ();
		tcpClient = null;
		tcpListener.Stop ();
		tcpListener = null;
	}

	private void LogOut (string message)
	{
		Debug.Log ("[SERVER:" + name + "] " + message);
	}
}
