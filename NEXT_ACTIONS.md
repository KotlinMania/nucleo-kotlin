# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 14/26 (53.8%)
- **Function parity:** 107/222 matched (target 181) — 48.2%
- **Class/type parity:** 19/38 matched (target 25) — 50.0%
- **Combined symbol parity:** 126/260 matched (target 206) — 48.5%
- **Average inline-code cosine:** 0.21 (function body across 13 matched files)
- **Average documentation cosine:** 0.22 (doc text across 13 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 13 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 1 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. matcher.config

- **Target:** `nucleo.Config`
- **Similarity:** 0.20
- **Dependents:** 4
- **Priority Score:** 4000308.0
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 2. matcher.utf32_str

- **Target:** `nucleo.Utf32Str`
- **Similarity:** 0.05
- **Dependents:** 2
- **Priority Score:** 2063209.5
- **Functions:** 23/28 matched (target 60)
- **Missing functions:** `from`, `from`, `from`, `fmt`, `fmt`
- **Types:** 3/4 matched (target 5)
- **Missing types:** `Item`

### 3. chars.normalize

- **Target:** `chars.Normalize`
- **Similarity:** 0.17
- **Dependents:** 1
- **Priority Score:** 1010608.3
- **Functions:** 5/6 matched (target 8)
- **Missing functions:** `check_conversions`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/5 matched

### 4. matcher.chars

- **Target:** `chars.Chars`
- **Similarity:** 0.22
- **Dependents:** 1
- **Priority Score:** 1001607.8
- **Functions:** 13/13 matched (target 17)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_

### 5. matcher.debug

- **Target:** `nucleo.Debug`
- **Similarity:** 0.02
- **Dependents:** 1
- **Priority Score:** 1000209.8
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 6. matcher.score

- **Target:** `nucleo.Score`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 10309.6
- **Functions:** 2/3 matched (target 6)
- **Missing functions:** `bonus_for`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 7. matcher.lib

- **Target:** `nucleo.Matcher [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2110.0
- **Functions:** 20/20 matched (target 21)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 8. matcher.pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 2106.8
- **Functions:** 16/16 matched (target 18)
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 9. matcher.matrix

- **Target:** `nucleo.Matrix`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 1309.3
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
- **Types:** 6/6 matched
- **Missing types:** _none_

### 10. matcher.fuzzy_optimal

- **Target:** `nucleo.FuzzyOptimal`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 708.3
- **Functions:** 7/7 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 11. matcher.exact

- **Target:** `nucleo.Exact`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 506.9
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 12. matcher.prefilter

- **Target:** `nucleo.Prefilter`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 408.6
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 13. matcher.fuzzy_greedy

- **Target:** `nucleo.FuzzyGreedy`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 109.6
- **Functions:** 1/1 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 14. chars.case_fold

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
| `lib` | `Lib` | 0 | `src/lib.rs` | `Lib.kt` |

