
using System;
using System.Collections;
using System.Net.Sockets;
using System.Net;
using System.IO;




namespace Example.ProtoTry
{
	namespace UpDoot
	{
		public interface Listener
		{
			void spawn (int id, int parent);
			void move (int id, float x, float y, float z);
			void close (int id);
		}

		public class EnStream : Listener
		{
			private Stream _outer;

			public EnStream(Stream outer) {
				_outer = outer;
			}

			public void close (int id)
			{
				var _stream = new MemoryStream ();
				using (BinaryWriter _writer = new BinaryWriter (_stream)) {
					_writer.Write ((short)1);

					_writer.Write ((int)id);
				}

				byte[] _bytes = _stream.ToArray ();
				_outer.Write (_bytes, 0, _bytes.Length);
			}
			public void move (int id, float x, float y, float z)
			{
				var _stream = new MemoryStream ();
				using (BinaryWriter _writer = new BinaryWriter (_stream)) {
					_writer.Write ((short)2);

					_writer.Write ((int)id);
					_writer.Write ((float)x);
					_writer.Write ((float)y);
					_writer.Write ((float)z);
				}

				byte[] _bytes = _stream.ToArray ();
				_outer.Write (_bytes, 0, _bytes.Length);
			}
			public void spawn (int id, int parent)
			{
				var _stream = new MemoryStream ();
				using (BinaryWriter _writer = new BinaryWriter (_stream)) {
					_writer.Write ((short)3);

					_writer.Write ((int)id);
					_writer.Write ((int)parent);
				}

				byte[] _bytes = _stream.ToArray ();
				_outer.Write (_bytes, 0, _bytes.Length);
			}
		}

		public class DeStream
		{
			private BinaryReader _binaryReader;
			private Listener _listener;

			public DeStream (Stream outer, Listener listener)
			{
				_binaryReader = new BinaryReader (outer);
				_listener = listener;
			}

			public bool Pump ()
			{
				if (!((NetworkStream)(_binaryReader.BaseStream)).DataAvailable) {
					return false;
				}

				switch ((int)(_binaryReader.ReadInt16 ())) {
				case 1:
					{
						int id = _binaryReader.ReadInt32 ();

						_listener.close (id);

						return true;
					}
				case 2:
					{
						int id = _binaryReader.ReadInt32 ();
						float x = _binaryReader.ReadSingle ();
						float y = _binaryReader.ReadSingle ();
						float z = _binaryReader.ReadSingle ();

						_listener.move (id, x, y, z);

						return true;
					}
				case 3:
					{
						int id = _binaryReader.ReadInt32 ();
						int parent = _binaryReader.ReadInt32 ();

						_listener.spawn (id, parent);

						return true;
					}
				default:
					throw new Exception ("This connection is now corrupted");
				}
			}
		}
	}
}
