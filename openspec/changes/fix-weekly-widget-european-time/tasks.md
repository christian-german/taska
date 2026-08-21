## 1. Calendar-week time formatting

- [x] 1.1 Replace locale-dependent calendar-week task time formatting with a zero-padded 24-hour `HH:mm` format.
- [x] 1.2 Preserve device-time-zone conversion, task content, and existing widget behavior outside clock presentation.

## 2. Verification

- [x] 2.1 Add focused unit coverage proving that calendar-week task text uses `HH:mm` for both American and European locales.
- [x] 2.2 Run relevant Android widget tests and strict OpenSpec validation.
