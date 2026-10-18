# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 17/26 (65.4%)
- **Function parity:** 145/176 matched (target 255) — 82.4%
- **Class/type parity:** 33/37 matched (target 49) — 89.2%
- **Combined symbol parity:** 178/213 matched (target 304) — 83.6%
- **Average inline-code cosine:** 0.63 (function body across 17 matched files)
- **Average documentation cosine:** 0.29 (doc text across 17 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 5 files with <0.60 function similarity

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
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 2. matcher.utf32_str

- **Target:** `nucleo.Utf32Str`
- **Similarity:** 0.30
- **Dependents:** 2
- **Priority Score:** 2012207.0
- **Functions:** 18/18 matched (target 60)
- **Missing functions:** _none_
- **Types:** 3/4 matched (target 5)
- **Missing types:** `Item`

### 3. matcher.chars

- **Target:** `chars.Chars`
- **Similarity:** 0.68
- **Dependents:** 1
- **Priority Score:** 1011303.2
- **Functions:** 10/10 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/3 matched
- **Missing types:** `Char`

### 4. chars.normalize

- **Target:** `chars.Normalize`
- **Similarity:** 0.84
- **Dependents:** 1
- **Priority Score:** 1010601.6
- **Functions:** 5/6 matched (target 8)
- **Missing functions:** `check_conversions`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/5 matched

### 5. worker

- **Target:** `nucleo.Worker`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001402.5
- **Functions:** 12/12 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 6. matcher.debug

- **Target:** `nucleo.Debug`
- **Similarity:** 0.19
- **Dependents:** 1
- **Priority Score:** 1000108.1
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 7. boxcar

- **Target:** `nucleo.Boxcar`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 4903.7
- **Functions:** 38/38 matched (target 49)
- **Missing functions:** _none_
- **Types:** 11/11 matched (target 15)
- **Missing types:** _none_
- **Tests:** 6/6 matched

### 8. matcher.pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 1602.4
- **Functions:** 11/11 matched (target 18)
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 9. par_sort

- **Target:** `nucleo.ParSort`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1503.4
- **Functions:** 14/14 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_

### 10. matcher.matrix

- **Target:** `nucleo.Matrix`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 1205.5
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 6/6 matched
- **Missing types:** _none_

### 11. pattern

- **Target:** `pattern.MultiPattern`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 1103.1
- **Functions:** 9/9 matched (target 10)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 12. matcher.fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 703.6
- **Functions:** 7/7 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 13. matcher.exact

- **Target:** `nucleo.Exact`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 501.5
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 14. matcher.prefilter

- **Target:** `nucleo.Prefilter`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 402.7
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 15. matcher.score

- **Target:** `nucleo.Score`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 16. matcher.fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 104.7
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 17. chars.case_fold

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

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `matcher.lib` | `matcher.src.Lib` | 0 | `matcher/src/lib.rs` | `matcher/src/Lib.kt` |
| `lib` | `Lib` | 0 | `src/lib.rs` | `Lib.kt` |

