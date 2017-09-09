# Active Span replacement.

Shows how a Span temporary becomes the active Span for a code region, saving the previous active one, and restoring it upon deactivation.

This example creates an initial Span in the main thread, schedules a callback to be run in a different thread, and have this Span activated there temporary.
