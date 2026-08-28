# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 13/17 (76.5%)
- **Function parity:** 76/119 matched (target 191) — 63.9%
- **Class/type parity:** 14/21 matched (target 29) — 66.7%
- **Combined symbol parity:** 90/140 matched (target 220) — 64.3%
- **Average inline-code cosine:** 0.61 (function body across 13 matched files)
- **Average documentation cosine:** 0.24 (doc text across 13 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 6 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. utf32_str

- **Target:** `nucleo.Utf32Str`
- **Similarity:** 0.25
- **Dependents:** 2
- **Priority Score:** 2062207.5
- **Functions:** 14/18 matched (target 48)
- **Missing functions:** `fmt`, `next`, `next_back`, `default`
- **Types:** 2/4 matched
- **Missing types:** `Chars`, `Item`

### 2. config

- **Target:** `nucleo.Config`
- **Similarity:** 0.72
- **Dependents:** 2
- **Priority Score:** 2000302.9
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 3. chars

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

### 5. matrix

- **Target:** `nucleo.Matrix`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 71207.7
- **Functions:** 2/6 matched (target 4)
- **Missing functions:** `new`, `fieds_from_ptr`, `alloc`, `drop`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `MatrixLayout`, `MatcherDataView`, `MatcherData`

### 6. fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 30705.6
- **Functions:** 4/7 matched (target 9)
- **Missing functions:** `setup`, `score_row`, `populate_matrix`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 7. lib

- **Target:** `nucleo.Nucleo`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 22103.0
- **Functions:** 18/20 matched (target 53)
- **Missing functions:** `fmt`, `default`
- **Types:** 1/1 matched (target 9)
- **Missing types:** _none_

### 8. pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 1602.6
- **Functions:** 11/11 matched (target 25)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_

### 9. exact

- **Target:** `nucleo.Exact`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 501.5
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 10. prefilter

- **Target:** `nucleo.Prefilter`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 402.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 11. score

- **Target:** `nucleo.Score`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 12. fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 104.7
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 13. chars.case_fold

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

