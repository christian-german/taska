## Context

Android task presentations use the Material outlined calendar icon (`CalendarToday`) as the established appointment identifier. The Today and calendar-week home-screen widgets already receive each task's `type`, but their `RemoteViews` rows do not expose that identifier. The two widgets use different row rendering paths: the Today widget binds fixed rows in its root layout, while the calendar-week widget binds collection rows through a `RemoteViewsService`.

## Goals / Non-Goals

**Goals:**

- Make appointments recognizable in both Android home-screen widgets.
- Match the outlined calendar icon used by the rest of the Android application.
- Expose an accessible Appointment description on every visible widget appointment icon.
- Preserve the existing row actions and presentation for to-dos.

**Non-Goals:**

- Change task classification or infer appointment status from any field other than `type`.
- Change widget filtering, ordering, date grouping, capacity, completion, navigation, or refresh behavior.
- Add appointment indicators to non-widget surfaces, which already have their own presentation requirements.

## Decisions

### Use a widget-compatible vector representation of the established icon

Add an Android drawable that reproduces the application's outlined `CalendarToday` icon for use by `RemoteViews`. Bind it through an `ImageView` rather than using a text glyph, ensuring the widgets match the established non-color-only identifier without depending on a font.

### Bind visibility directly from task type

Show the appointment `ImageView` only when the bound task's `type` is `APPOINTMENT`; otherwise hide it without reserving visible row space. Apply this rule in both the fixed Today row binder and the calendar-week collection factory. A missing or non-appointment type retains the standard to-do presentation.

### Keep accessibility semantics with the icon

Use a localized Appointment content description on each appointment image. Hiding the image for to-dos also removes the appointment description, so assistive technology receives the same classification distinction as sighted users.

## Risks / Trade-offs

- [The widget icon could drift from the Compose icon] → Derive the vector path and proportions from the same Material outlined `CalendarToday` asset and add resource-level coverage.
- [An added icon reduces title width] → Keep the indicator compact and preserve the existing title ellipsis and line limits.
- [The widgets have separate binders] → Cover both render paths with focused tests so their appointment rules stay aligned.

## Migration Plan

1. Add the widget-compatible outlined calendar drawable and accessible string resource.
2. Add an appointment image slot to the Today and calendar-week task-row layouts.
3. Bind its visibility from `TaskDto.type` in both widget render paths.
4. Verify appointment and to-do rows in both widgets while retaining existing widget behavior.
5. Roll back by removing the image slots, resources, and type-based binding; no stored data migration is required.

## Open Questions

- None.
