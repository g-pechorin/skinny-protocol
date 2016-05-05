package peterlavalle

package object ski {

	def ?? : Nothing = {
		val notImplementedError: NotImplementedError = new NotImplementedError
		notImplementedError.setStackTrace(notImplementedError.getStackTrace.tail)
		throw notImplementedError
	}
}
