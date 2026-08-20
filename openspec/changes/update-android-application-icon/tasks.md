## 1. Android icon assets

- [x] 1.1 Recolour the main Android launcher icon with exact signal green `#14B37D` while preserving its existing design, silhouette, composition, and safe area.
- [x] 1.2 Apply the same colour refresh to the development-specific Android launcher icon while preserving its existing distinguishing treatment.
- [x] 1.3 Synchronize all adaptive, legacy, round, and density-specific Android launcher resources so none retain the previous colour treatment.

## 2. Verification

- [x] 2.1 Add an automated resource check that covers the required main and development launcher variants, exact green value, expected resource forms, and unchanged source geometry where practical.
- [ ] 2.2 Build the main and development Android variants and verify each resolves to its intended launcher resources.
- [x] 2.3 Visually inspect both variants under representative adaptive masks and a legacy launcher, confirming the design is unchanged, green is consistent, and the development variant remains identifiable.
- [ ] 2.4 Run relevant Android static checks and strict OpenSpec validation.
