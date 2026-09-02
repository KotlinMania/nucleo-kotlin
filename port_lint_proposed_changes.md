# port-lint Proposed Changes

**Generated:** 2026-09-02
**Source:** tmp/nucleo
**Target:** src/commonMain/kotlin/io/github/kotlinmania/nucleo

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/Worker.kt` | `// port-lint: source worker.rs` | `// port-lint: source worker.rs` | `worker.rs` | `port-lint provenance header matched only after fallback normalization: 'worker.rs' vs expected 'worker.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/Boxcar.kt` | `// port-lint: source boxcar.rs` | `// port-lint: source boxcar.rs` | `boxcar.rs` | `port-lint provenance header matched only after fallback normalization: 'boxcar.rs' vs expected 'boxcar.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/nucleo/BoxcarTest.kt` | `// port-lint: tests boxcar.rs` | `// port-lint: tests boxcar.rs` | `boxcar.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:boxcar.rs' vs expected 'boxcar.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/Nucleo.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/ParSort.kt` | `// port-lint: source par_sort.rs` | `// port-lint: source par_sort.rs` | `par_sort.rs` | `port-lint provenance header matched only after fallback normalization: 'par_sort.rs' vs expected 'par_sort.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/nucleo/ParSortTest.kt` | `// port-lint: tests par_sort.rs` | `// port-lint: tests par_sort.rs` | `par_sort.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:par_sort.rs' vs expected 'par_sort.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/nucleo/pattern/MultiPattern.kt` | `// port-lint: source pattern.rs` | `// port-lint: source pattern.rs` | `pattern.rs` | `port-lint provenance header matched only after fallback normalization: 'pattern.rs' vs expected 'pattern.rs'` |
