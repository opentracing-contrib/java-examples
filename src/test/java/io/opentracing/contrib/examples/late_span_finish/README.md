# Late Span finish.

Shows how a Span becomes the parent of a set of Span instances created under different threads, without any tie to its children lifetime.

This examples creates a Span in the main thread, passing it to a set of callbacks, each one creating and finishing a new Span in turn, and finishing the initial Span later on in the main thread.
