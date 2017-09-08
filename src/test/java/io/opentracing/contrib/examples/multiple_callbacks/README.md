# Multiple callbacks.

Shows a Span representing a task expected to be done when the last of its related callbacks is finished.

This example creates a Span in the main thread, schedules a set of callbacks to be run in different threads, creating a `Continuation` for each one, and later using it to reactivate the Span and creating a child Span in turn.

`Client` is used to abstract the callback creation/scheduling.
