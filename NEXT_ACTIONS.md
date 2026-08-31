# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 18/26 (69.2%)
- **Function parity:** 174/227 matched (target 276) — 76.7%
- **Class/type parity:** 37/45 matched (target 53) — 82.2%
- **Combined symbol parity:** 211/272 matched (target 329) — 77.6%
- **Average inline-code cosine:** 0.61 (function body across 16 matched files)
- **Average documentation cosine:** 0.30 (doc text across 16 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 8 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. matcher.config

- **Target:** `nucleo.Config`
- **Similarity:** 0.72
- **Dependents:** 4
- **Priority Score:** 4000302.8
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 2. matcher.utf32_str

- **Target:** `nucleo.Utf32Str`
- **Similarity:** 0.25
- **Dependents:** 2
- **Priority Score:** 2062207.5
- **Functions:** 14/18 matched (target 48)
- **Missing functions:** `fmt`, `next`, `next_back`, `default`
- **Types:** 2/4 matched
- **Missing types:** `Chars`, `Item`

### 3. matcher.chars

- **Target:** `chars.Chars`
- **Similarity:** 0.58
- **Dependents:** 1
- **Priority Score:** 1031304.2
- **Functions:** 8/10 matched (target 14)
- **Missing functions:** `fmt`, `eq`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Char`

### 4. chars.normalize

- **Target:** `chars.Normalize`
- **Similarity:** 0.84
- **Dependents:** 1
- **Priority Score:** 1010601.6
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `check_conversions`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/5 matched

### 5. nucleo.worker

- **Target:** `nucleo.Worker`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001402.5
- **Functions:** 12/12 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 6. matcher.matrix

- **Target:** `nucleo.Matrix`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 71207.7
- **Functions:** 2/6 matched (target 4)
- **Missing functions:** `new`, `fieds_from_ptr`, `alloc`, `drop`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `MatrixLayout`, `MatcherDataView`, `MatcherData`

### 7. matcher.lib

- **Target:** `nucleo.Matcher [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42110.0
- **Functions:** 16/20 matched (target 16)
- **Missing functions:** `clone`, `fmt`, `default`, `new`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 8. matcher.fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 30705.6
- **Functions:** 4/7 matched (target 9)
- **Missing functions:** `setup`, `score_row`, `populate_matrix`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 9. matcher.pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 21603.1
- **Functions:** 9/11 matched (target 16)
- **Missing functions:** `clone`, `clone_from`
- **Types:** 5/5 matched
- **Missing types:** _none_

### 10. nucleo.pattern

- **Target:** `pattern.MultiPattern`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 11103.9
- **Functions:** 8/9 matched
- **Missing functions:** `new`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 11. nucleo.boxcar

- **Target:** `nucleo.Boxcar`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 4903.7
- **Functions:** 38/38 matched (target 49)
- **Missing functions:** _none_
- **Types:** 11/11 matched (target 15)
- **Missing types:** _none_
- **Tests:** 6/6 matched

### 12. nucleo.lib

- **Target:** `nucleo.Nucleo [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 3710.0
- **Functions:** 30/30 matched (target 37)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 8)
- **Missing types:** _none_

### 13. nucleo.par_sort

- **Target:** `nucleo.ParSort`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1503.4
- **Functions:** 14/14 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_

### 14. matcher.exact

- **Target:** `nucleo.Exact`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 501.5
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 15. matcher.prefilter

- **Target:** `nucleo.Prefilter`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 402.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 16. matcher.score

- **Target:** `nucleo.Score`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 17. matcher.fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 104.7
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 18. chars.case_fold

- **Target:** `chars.CaseFold`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

