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

## Keyboard and responsive behavior

The delete confirmation is an accessible modal dialog. Opening it moves focus
to the confirmation action, Tab and Shift+Tab stay within its actions, Escape
closes it, and focus returns to the original Delete button. The visible title
and description are connected with `aria-labelledby` and `aria-describedby`.

Global `:focus-visible` styling makes keyboard position clear. Navigation,
journal actions, delete actions, and pagination change from compact desktop rows
to wrapping or stacked mobile layouts.

Manual responsive check:

1. Run `npm.cmd run dev` from `frontend` and open `http://localhost:5173`.
2. Use browser responsive mode at 320 px and 768 px widths.
3. Confirm navigation, dashboard filters, pagination, and journal actions do not
   overlap or create horizontal scrolling.
4. Use Tab, Shift+Tab, Enter, and Escape through the delete confirmation.
