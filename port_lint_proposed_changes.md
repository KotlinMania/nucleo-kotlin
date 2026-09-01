# port-lint Proposed Changes

**Generated:** 2026-08-31
**Source:** tmp/nucleo/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/nucleo

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/Matcher.kt` | `// port-lint: source matcher/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only by basename: 'matcher/lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/pattern/Pattern.kt` | `// port-lint: source matcher/pattern.rs` | `// port-lint: source pattern.rs` | `pattern.rs` | `port-lint provenance header matched only by basename: 'matcher/pattern.rs' vs expected 'pattern.rs'` |
