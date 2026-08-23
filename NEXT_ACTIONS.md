# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 3/7 (42.9%)
- **Function parity:** 44/110 matched (target 82) — 40.0%
- **Class/type parity:** 9/23 matched (target 20) — 39.1%
- **Combined symbol parity:** 53/133 matched (target 102) — 39.8%
- **Average inline-code cosine:** 0.56 (function body across 3 matched files)
- **Average documentation cosine:** 0.61 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `nucleo.Nucleo`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 103706.3
- **Functions:** 21/30 matched (target 39)
- **Missing functions:** `clone`, `get_unchecked`, `get_item_unchecked`, `matcher_item_refs`, `canceled`, `cleared`, `new`, `tick_inner`, `drop`
- **Types:** 6/7 matched
- **Missing types:** `State`
- **Lint issues:** 1

### 2. par_sort

- **Target:** `nucleo.ParSort`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 1503.3
- **Functions:** 14/14 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_

### 3. pattern

- **Target:** `pattern.Pattern`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 1103.7
- **Functions:** 9/9 matched (target 23)
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

