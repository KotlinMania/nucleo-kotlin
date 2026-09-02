# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 14/17 (82.4%)
- **Function parity:** 92/118 matched (target 228) — 78.0%
- **Class/type parity:** 18/21 matched (target 34) — 85.7%
- **Combined symbol parity:** 110/139 matched (target 262) — 79.1%
- **Average inline-code cosine:** 0.61 (function body across 13 matched files)
- **Average documentation cosine:** 0.22 (doc text across 13 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 6 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. utf32_str

- **Target:** `nucleo.Utf32Str [PROVENANCE-FALLBACK]`
- **Similarity:** 0.30
- **Dependents:** 2
- **Priority Score:** 2012207.0
- **Functions:** 18/18 matched (target 60)
- **Missing functions:** _none_
- **Types:** 3/4 matched (target 5)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/utf32_str.rs` vs expected `utf32_str.rs`
- **Proposed provenance header:** `// port-lint: source utf32_str.rs` (current: `// port-lint: source matcher/src/utf32_str.rs`)
- **Lint issues:** 1

### 2. config

- **Target:** `nucleo.Config [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 2
- **Priority Score:** 2000302.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/config.rs` vs expected `config.rs`
- **Proposed provenance header:** `// port-lint: source config.rs` (current: `// port-lint: source matcher/src/config.rs`)
- **Lint issues:** 1

### 3. chars

- **Target:** `chars.Chars [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 1
- **Priority Score:** 1011303.4
- **Functions:** 10/10 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/3 matched
- **Missing types:** `Char`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/chars.rs` vs expected `chars.rs`
- **Proposed provenance header:** `// port-lint: source chars.rs` (current: `// port-lint: source matcher/src/chars.rs`)
- **Lint issues:** 1

### 4. chars.normalize

- **Target:** `chars.Normalize [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 1
- **Priority Score:** 1010601.6
- **Functions:** 5/6 matched (target 8)
- **Missing functions:** `check_conversions`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/chars/normalize.rs` vs expected `chars/normalize.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:matcher/src/chars/normalize.rs` vs expected `chars/normalize.rs`
- **Proposed provenance header:** `// port-lint: source chars/normalize.rs` (current: `// port-lint: source matcher/src/chars/normalize.rs`)
- **Proposed provenance header:** `// port-lint: tests chars/normalize.rs` (current: `// port-lint: tests matcher/src/chars/normalize.rs`)
- **Lint issues:** 2

### 5. lib

- **Target:** `nucleo.Nucleo [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2110.0
- **Functions:** 20/20 matched (target 58)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 9)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source matcher/src/lib.rs`)
- **Lint issues:** 2

### 6. pattern

- **Target:** `pattern.Pattern [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 1602.4
- **Functions:** 11/11 matched (target 28)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/pattern.rs` vs expected `pattern.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pattern.rs` vs expected `pattern.rs`
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source matcher/src/pattern.rs`)
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source src/pattern.rs`)
- **Lint issues:** 2

### 7. matrix

- **Target:** `nucleo.Matrix [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 1205.5
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 6/6 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/matrix.rs` vs expected `matrix.rs`
- **Proposed provenance header:** `// port-lint: source matrix.rs` (current: `// port-lint: source matcher/src/matrix.rs`)
- **Lint issues:** 1

### 8. fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 703.6
- **Functions:** 7/7 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/fuzzy_optimal.rs` vs expected `fuzzy_optimal.rs`
- **Proposed provenance header:** `// port-lint: source fuzzy_optimal.rs` (current: `// port-lint: source matcher/src/fuzzy_optimal.rs`)
- **Lint issues:** 1

### 9. exact

- **Target:** `nucleo.Exact [PROVENANCE-FALLBACK]`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 501.5
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/exact.rs` vs expected `exact.rs`
- **Proposed provenance header:** `// port-lint: source exact.rs` (current: `// port-lint: source matcher/src/exact.rs`)
- **Lint issues:** 1

### 10. prefilter

- **Target:** `nucleo.Prefilter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 402.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/prefilter.rs` vs expected `prefilter.rs`
- **Proposed provenance header:** `// port-lint: source prefilter.rs` (current: `// port-lint: source matcher/src/prefilter.rs`)
- **Lint issues:** 1

### 11. score

- **Target:** `nucleo.Score [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/score.rs` vs expected `score.rs`
- **Proposed provenance header:** `// port-lint: source score.rs` (current: `// port-lint: source matcher/src/score.rs`)
- **Lint issues:** 1

### 12. debug

- **Target:** `nucleo.Debug [PROVENANCE-FALLBACK]`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 108.1
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/debug.rs` vs expected `debug.rs`
- **Proposed provenance header:** `// port-lint: source debug.rs` (current: `// port-lint: source matcher/src/debug.rs`)
- **Lint issues:** 1

### 13. fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy [PROVENANCE-FALLBACK]`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 104.7
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/fuzzy_greedy.rs` vs expected `fuzzy_greedy.rs`
- **Proposed provenance header:** `// port-lint: source fuzzy_greedy.rs` (current: `// port-lint: source matcher/src/fuzzy_greedy.rs`)
- **Lint issues:** 1

### 14. chars.case_fold

- **Target:** `chars.CaseFold [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matcher/src/chars/case_fold.rs` vs expected `chars/case_fold.rs`
- **Proposed provenance header:** `// port-lint: source chars/case_fold.rs` (current: `// port-lint: source matcher/src/chars/case_fold.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

