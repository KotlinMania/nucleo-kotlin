# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 1/7 (14.3%)
- **Function parity:** 14/80 matched (target 20) — 17.5%
- **Class/type parity:** 1/16 matched (target 6) — 6.2%
- **Combined symbol parity:** 15/96 matched (target 26) — 15.6%
- **Average inline-code cosine:** 0.67 (function body across 1 matched files)
- **Average documentation cosine:** 0.93 (doc text across 1 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. par_sort

- **Target:** `nucleo.ParSort`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 1503.3
- **Functions:** 14/14 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
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
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
