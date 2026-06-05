---
name: feature-settlement
description: Settlement domain rules for JustWoo — payer/payee ledger with multi-currency balance, ISO 4217 currency strings, immutable settlement records, TDD across all layers, Swagger contract is mandatory. Trigger when touching SettlementRoutes, SettlementService, SettlementRepository, or shared settlement UseCases.
paths: **/SettlementRoutes.kt, **/SettlementService.kt, **/SettlementRepository.kt, **/schema/Settlements.kt, **/domain/usecase/settlement/**, **/ui/nav/settlement/**
---

# Feature: Settlement

A Settlement records a payment from one house member to another. The aggregate over Settlements + priced Tasks gives each member's outstanding balance. Files involved:

- `backend/.../routes/SettlementRoutes.kt`
- `backend/.../service/SettlementService.kt`
- `backend/.../repositories/SettlementRepository.kt`
- `backend/.../schema/Settlements.kt`
- `:core` Settlement DTOs
- `shared/.../domain/usecase/` (settlement UseCases — add if missing)

Also read [`backend-best-practice`](../backend-best-practice/SKILL.md) and [`kmp-best-practice`](../kmp-best-practice/SKILL.md).

## Invariants — non-negotiable

1. **A Settlement is immutable once created.** No `UPDATE` on amount, payer, payee, or currency. The only allowed mutation is `DELETE` (treat as a void / correction) and only by ADMIN or the original payer, within a short window. If you need to "fix" a wrong settlement, create a counter-entry — don't mutate.
2. **`payerId != payeeId`.** Enforced at the service layer and as a DB-level check if possible. A user cannot pay themselves.
3. **Both users must be members of the house.** Validate at the service layer using the house membership check from [`feature-house`](../feature-house/SKILL.md). Cross-house settlements are rejected.
4. **`currencyCode` is ISO 4217 `String`, mandatory on every Settlement.** No optional currency, no default currency — every row carries its own currency explicitly.
5. **Balance computation is currency-aware.** The `/houses/{id}/settlements/balance` endpoint returns a per-member outstanding amount **converted to the house's display currency** (or whatever the API spec defines). Conversion happens in one helper, with the rate source and timestamp recorded if rates are not pinned. Never sum amounts across currencies without conversion.
6. **Amount is `Double` for now** (mirrors backend schema), but **avoid floating-point arithmetic when computing balances**. Use `BigDecimal` (JVM) / `kotlinx.math` equivalents in the balance helper; serialize back to `Double` only at the API boundary. A penny lost to FP drift is a trust bug.
7. **Settlements are house-scoped.** Every read filters by `houseId` + caller-is-member.

## TDD flow for a Settlement change

**TDD is mandatory here — no exceptions.**

1. **Service test first** (`SettlementServiceTest.kt`):
   - Happy path: record settlement, balance updates.
   - Self-payment rejected.
   - Non-member as payer or payee rejected.
   - Multi-currency balance: settlements in USD + EUR + TWD aggregated correctly to the house display currency.
   - Settlement of a missing house → 404, not 500.
   - Permission to delete: only ADMIN / original payer, within window.
2. **Repository test** against TestContainers Postgres:
   - FK constraints (deleting a house cascades / restricts settlements consistently with policy).
   - Currency string round-trips.
   - Concurrent insert + balance read are consistent (use a transaction with a serializable isolation if needed — verify it).
3. **Route test**:
   - End-to-end record → list → balance → delete flow.
   - Cross-house leakage check (user in house A can't read settlements in house B).

## Swagger contract

Every change to these requires a YAML update in [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml):

- `GET/POST /houses/{id}/settlements`
- `DELETE /houses/{id}/settlements/{settlementId}`
- `GET /houses/{id}/settlements/balance` — pay extra attention to the response schema; this is the endpoint clients render headline numbers from. Document the conversion contract (which currency the balance is in, whether the response includes per-currency breakdown).

## Shared / client side

- Settlement DTO in `:core` has `payerId: Long`, `payeeId: Long`, `amount: Double`, `currencyCode: String`, `note: String?`, `createTime: Instant`.
- Display: format with `NumberFormatter`-style helpers per row's `currencyCode` (don't use `Locale.current.currencyCode`).
- Caching: settlement list is SQLDelight-backed for offline read. Balance is **always re-fetched** when a settlement is created or deleted — never trust a stale cached balance.

## Anti-patterns — reject on sight

- An UPDATE on a settlement row.
- `currencyCode` typed as an enum.
- `settlements.map { it.amount }.sum()` over mixed currencies.
- Using raw `Double` arithmetic to compute a member's outstanding balance.
- A balance endpoint that returns a single number with no currency.
- `payerId == payeeId` allowed by the service.
- A settlement created without verifying both users are house members.
- Auto-creating a settlement when a Task is marked complete (Task and Settlement are intentionally decoupled — see [`feature-task`](../feature-task/SKILL.md)).
- New endpoint without Swagger.
