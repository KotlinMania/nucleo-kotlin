# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/7 (71.4%)
- **Function parity:** 90/105 matched (target 139) — 85.7%
- **Class/type parity:** 19/23 matched (target 33) — 82.6%
- **Combined symbol parity:** 109/128 matched (target 172) — 85.2%
- **Average inline-code cosine:** 0.60 (function body across 5 matched files)
- **Average documentation cosine:** 0.49 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 2 files with <0.60 function similarity

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
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 174906.2
- **Functions:** 25/38 matched (target 28)
- **Missing functions:** `drop`, `size_hint`, `next_back`, `drive_unindexed`, `drive`, `with_producer`, `into_iter`, `location`, `extend_unique_bucket`, `extend_over_two_buckets`, `extend_over_more_than_two_buckets`, `extend_with_incorrect_reported_len_is_caught`, `extend_over_max_capacity`
- **Types:** 7/11 matched (target 9)
- **Missing types:** `Item`, `ParIterProducer`, `IntoIter`, `IncorrectLenIter`
- **Tests:** 0/6 matched
- **Lint issues:** 3

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
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 1503.3
- **Functions:** 14/14 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
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

