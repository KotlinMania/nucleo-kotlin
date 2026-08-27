# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/7 (71.4%)
- **Function parity:** 103/105 matched (target 163) — 98.1%
- **Class/type parity:** 22/23 matched (target 38) — 95.7%
- **Combined symbol parity:** 125/128 matched (target 201) — 97.7%
- **Average inline-code cosine:** 0.65 (function body across 5 matched files)
- **Average documentation cosine:** 0.49 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. worker

- **Target:** `nucleo.Worker`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001402.5
- **Functions:** 12/12 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 2. boxcar

- **Target:** `nucleo.Boxcar`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 14903.7
- **Functions:** 38/38 matched (target 49)
- **Missing functions:** _none_
- **Types:** 10/11 matched (target 13)
- **Missing types:** `Item`
- **Tests:** 6/6 matched

### 3. lib

- **Target:** `nucleo.Nucleo`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 3704.4
- **Functions:** 30/30 matched (target 53)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 9)
- **Missing types:** _none_

### 4. par_sort

- **Target:** `nucleo.ParSort`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1503.4
- **Functions:** 14/14 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_

### 5. pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 1103.7
- **Functions:** 9/9 matched (target 25)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

