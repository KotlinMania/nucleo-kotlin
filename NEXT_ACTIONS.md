# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/7 (71.4%)
- **Function parity:** 103/105 matched (target 171) — 98.1%
- **Class/type parity:** 23/23 matched (target 40) — 100.0%
- **Combined symbol parity:** 126/128 matched (target 211) — 98.4%
- **Average inline-code cosine:** 0.68 (function body across 4 matched files)
- **Average documentation cosine:** 0.39 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. worker

- **Target:** `nucleo.Worker [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001402.5
- **Functions:** 12/12 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/worker.rs` vs expected `worker.rs`
- **Proposed provenance header:** `// port-lint: source worker.rs` (current: `// port-lint: source src/worker.rs`)
- **Lint issues:** 1

### 2. boxcar

- **Target:** `nucleo.Boxcar [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 4903.7
- **Functions:** 38/38 matched (target 49)
- **Missing functions:** _none_
- **Types:** 11/11 matched (target 15)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/boxcar.rs` vs expected `boxcar.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/boxcar.rs` vs expected `boxcar.rs`
- **Proposed provenance header:** `// port-lint: source boxcar.rs` (current: `// port-lint: source src/boxcar.rs`)
- **Proposed provenance header:** `// port-lint: tests boxcar.rs` (current: `// port-lint: tests src/boxcar.rs`)
- **Lint issues:** 2

### 3. lib

- **Target:** `nucleo.Nucleo [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 3710.0
- **Functions:** 30/30 matched (target 58)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 9)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source matcher/src/lib.rs`)
- **Lint issues:** 2

### 4. par_sort

- **Target:** `nucleo.ParSort [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1503.4
- **Functions:** 14/14 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/par_sort.rs` vs expected `par_sort.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/par_sort.rs` vs expected `par_sort.rs`
- **Proposed provenance header:** `// port-lint: source par_sort.rs` (current: `// port-lint: source src/par_sort.rs`)
- **Proposed provenance header:** `// port-lint: tests par_sort.rs` (current: `// port-lint: tests src/par_sort.rs`)
- **Lint issues:** 2

### 5. pattern

- **Target:** `pattern.Pattern [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 1103.1
- **Functions:** 9/9 matched (target 28)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/pattern.rs` vs expected `pattern.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pattern.rs` vs expected `pattern.rs`
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source matcher/src/pattern.rs`)
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source src/pattern.rs`)
- **Lint issues:** 2

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

