# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 18/26 (69.2%)
- **Function parity:** 120/227 matched (target 260) — 52.9%
- **Class/type parity:** 27/45 matched (target 47) — 60.0%
- **Combined symbol parity:** 147/272 matched (target 307) — 54.0%
- **Average inline-code cosine:** 0.50 (function body across 18 matched files)
- **Average documentation cosine:** 0.29 (doc text across 18 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 10 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. matcher.config

- **Target:** `nucleo.Config [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 4
- **Priority Score:** 4000302.8
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `config.rs` vs expected `config.rs`
- **Proposed provenance header:** `// port-lint: source config.rs` (current: `// port-lint: source config.rs`)
- **Lint issues:** 1

### 2. matcher.utf32_str

- **Target:** `nucleo.Utf32Str [PROVENANCE-FALLBACK]`
- **Similarity:** 0.25
- **Dependents:** 2
- **Priority Score:** 2062207.5
- **Functions:** 14/18 matched (target 48)
- **Missing functions:** `fmt`, `next`, `next_back`, `default`
- **Types:** 2/4 matched
- **Missing types:** `Chars`, `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `utf32_str.rs` vs expected `utf32_str.rs`
- **Proposed provenance header:** `// port-lint: source utf32_str.rs` (current: `// port-lint: source utf32_str.rs`)
- **Lint issues:** 1

### 3. chars.normalize

- **Target:** `chars.Normalize [PROVENANCE-FALLBACK]`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1050609.4
- **Functions:** 1/6 matched (target 2)
- **Missing functions:** `check_conversions`, `general`, `invisible_chars`, `boundary_cases`, `unchanged_outside_blocks`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `chars/normalize.rs` vs expected `chars/normalize.rs`
- **Proposed provenance header:** `// port-lint: source chars/normalize.rs` (current: `// port-lint: source chars/normalize.rs`)
- **Lint issues:** 1

### 4. matcher.chars

- **Target:** `chars.Chars [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 1
- **Priority Score:** 1031304.2
- **Functions:** 8/10 matched (target 14)
- **Missing functions:** `fmt`, `eq`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Char`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `chars.rs` vs expected `chars.rs`
- **Proposed provenance header:** `// port-lint: source chars.rs` (current: `// port-lint: source chars.rs`)
- **Lint issues:** 1

### 5. worker

- **Target:** `nucleo.Worker [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001402.5
- **Functions:** 12/12 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `worker.rs` vs expected `worker.rs`
- **Proposed provenance header:** `// port-lint: source worker.rs` (current: `// port-lint: source worker.rs`)
- **Lint issues:** 1

### 6. lib

- **Target:** `nucleo.Matcher [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 373710.0
- **Functions:** 0/30 matched (target 16)
- **Missing functions:** `clone`, `push`, `extend`, `injected_items`, `get_unchecked`, `get`, `clear`, `update`, `item_count`, `pattern`, `matched_item_count`, `matched_items`, `get_item_unchecked`, `get_item`, `matches`, `get_matched_item`, `matcher_item_refs`, `canceled`, `cleared`, `new`, `active_injectors`, `snapshot`, `injector`, `restart`, `update_config`, `sort_results`, `reverse_items`, `tick`, `tick_inner`, `drop`
- **Types:** 0/7 matched (target 1)
- **Missing types:** `Item`, `Injector`, `Match`, `Status`, `Snapshot`, `State`, `Nucleo`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

### 7. matcher.lib

- **Target:** `nucleo.Nucleo [PROVENANCE-FALLBACK]`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 192109.6
- **Functions:** 2/20 matched (target 37)
- **Missing functions:** `fmt`, `default`, `fuzzy_match`, `fuzzy_indices`, `fuzzy_matcher_impl`, `fuzzy_match_greedy`, `fuzzy_indices_greedy`, `fuzzy_match_greedy_impl`, `substring_match`, `substring_indices`, `substring_match_impl`, `exact_match`, `exact_indices`, `prefix_match`, `prefix_indices`, `postfix_match`, `postfix_indices`, `exact_match_impl`
- **Types:** 0/1 matched (target 8)
- **Missing types:** `Matcher`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

### 8. boxcar

- **Target:** `nucleo.Boxcar [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 84904.9
- **Functions:** 32/38 matched (target 41)
- **Missing functions:** `location`, `extend_unique_bucket`, `extend_over_two_buckets`, `extend_over_more_than_two_buckets`, `extend_with_incorrect_reported_len_is_caught`, `extend_over_max_capacity`
- **Types:** 9/11 matched
- **Missing types:** `Item`, `IncorrectLenIter`
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `boxcar.rs` vs expected `boxcar.rs`
- **Proposed provenance header:** `// port-lint: source boxcar.rs` (current: `// port-lint: source boxcar.rs`)
- **Lint issues:** 1

### 9. matcher.matrix

- **Target:** `nucleo.Matrix [PROVENANCE-FALLBACK]`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 71207.7
- **Functions:** 2/6 matched (target 4)
- **Missing functions:** `new`, `fieds_from_ptr`, `alloc`, `drop`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `MatrixLayout`, `MatcherDataView`, `MatcherData`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `matrix.rs` vs expected `matrix.rs`
- **Proposed provenance header:** `// port-lint: source matrix.rs` (current: `// port-lint: source matrix.rs`)
- **Lint issues:** 1

### 10. matcher.fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 30705.6
- **Functions:** 4/7 matched (target 9)
- **Missing functions:** `setup`, `score_row`, `populate_matrix`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fuzzy_optimal.rs` vs expected `fuzzy_optimal.rs`
- **Proposed provenance header:** `// port-lint: source fuzzy_optimal.rs` (current: `// port-lint: source fuzzy_optimal.rs`)
- **Lint issues:** 1

### 11. matcher.pattern

- **Target:** `pattern.Pattern [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 21603.1
- **Functions:** 9/11 matched (target 16)
- **Missing functions:** `clone`, `clone_from`
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `pattern.rs` vs expected `pattern.rs`
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source pattern.rs`)
- **Lint issues:** 1

### 12. pattern

- **Target:** `pattern.MultiPattern [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 11103.9
- **Functions:** 8/9 matched
- **Missing functions:** `new`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `pattern.rs` vs expected `pattern.rs`
- **Proposed provenance header:** `// port-lint: source pattern.rs` (current: `// port-lint: source pattern.rs`)
- **Lint issues:** 1

### 13. par_sort

- **Target:** `nucleo.ParSort [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1503.4
- **Functions:** 14/14 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `par_sort.rs` vs expected `par_sort.rs`
- **Proposed provenance header:** `// port-lint: source par_sort.rs` (current: `// port-lint: source par_sort.rs`)
- **Lint issues:** 1

### 14. matcher.exact

- **Target:** `nucleo.Exact [PROVENANCE-FALLBACK]`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 501.5
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `exact.rs` vs expected `exact.rs`
- **Proposed provenance header:** `// port-lint: source exact.rs` (current: `// port-lint: source exact.rs`)
- **Lint issues:** 1

### 15. matcher.prefilter

- **Target:** `nucleo.Prefilter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 402.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `prefilter.rs` vs expected `prefilter.rs`
- **Proposed provenance header:** `// port-lint: source prefilter.rs` (current: `// port-lint: source prefilter.rs`)
- **Lint issues:** 1

### 16. matcher.score

- **Target:** `nucleo.Score [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `score.rs` vs expected `score.rs`
- **Proposed provenance header:** `// port-lint: source score.rs` (current: `// port-lint: source score.rs`)
- **Lint issues:** 1

### 17. matcher.fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy [PROVENANCE-FALLBACK]`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 104.7
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fuzzy_greedy.rs` vs expected `fuzzy_greedy.rs`
- **Proposed provenance header:** `// port-lint: source fuzzy_greedy.rs` (current: `// port-lint: source fuzzy_greedy.rs`)
- **Lint issues:** 1

### 18. chars.case_fold

- **Target:** `chars.CaseFold [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `chars/case_fold.rs` vs expected `chars/case_fold.rs`
- **Proposed provenance header:** `// port-lint: source chars/case_fold.rs` (current: `// port-lint: source chars/case_fold.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

