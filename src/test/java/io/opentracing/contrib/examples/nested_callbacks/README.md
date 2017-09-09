# Nested callbacks.

Shows a Span representing a task spawning nested callbacks, one at the time, and having the Span done when the last one finishes.

This example creates a Span in the main thread, scheduling a set of nested callbacks, one at the time, and activating/deactivating the Span as they are executed.
