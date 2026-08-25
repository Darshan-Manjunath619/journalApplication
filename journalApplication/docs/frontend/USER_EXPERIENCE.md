# Frontend User Experience

## Consistent feedback states

`FeedbackPanel` is the shared presentation component for page-level loading,
empty, and error states. Pages still decide what happened and what retry action
to run; the component only provides consistent, accessible presentation.

```text
TanStack Query state
  -> page chooses title, message, and retry callback
  -> FeedbackPanel
  -> status or alert announced to assistive technology
```

Session bootstrap, dashboard journals, journal detail, and journal editing now
use this pattern. A tag request failure is shown as a non-blocking warning because
journals and the remaining filters can still work; retrying refreshes only tags.
