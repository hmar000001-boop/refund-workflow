# Phase 1 — Refund domain rules

## 1. Monetary invariants

- `Money` is a non-negative amount paired with a Java `Currency`.
- The amount is normalized to the currency's standard fraction digits.
- No rounding is performed. An amount that would require rounding is rejected.
- Addition, subtraction and comparison require identical currencies.
- Subtraction cannot produce a negative amount.

## 2. Payment eligibility

- `CAPTURED` and `PARTIALLY_REFUNDED` payments may be considered for approval.
- `DISPUTED` payments require manual review.
- `REFUNDED` payments are ineligible and expose a zero remaining balance.
- `AUTHORIZED`, `VOIDED` and `FAILED` payments are ineligible.
- The payment and customer identifiers must match the refund request.

## 3. Refund-history accounting

- `PENDING` refunds reserve refundable balance because an execution may already be in flight.
- `SUCCEEDED` refunds consume refundable balance permanently.
- `FAILED` and `CANCELLED` refunds do not consume refundable balance.
- If the history contains a different payment reference or a different currency, the decision requires manual review and the service omits the remaining-balance estimate.
- If consumed/reserved refunds exceed the transaction amount, the request is ineligible.

## 4. Request-level rules

- Requested amount must be positive.
- Requested currency must match the payment transaction currency.
- An already-seen request identifier is routed to manual review. A later idempotency-aware application boundary may return the existing workflow/refund instead of starting a new one.
- `SUSPECTED_FRAUD` requires manual review.
- An amount greater than the remaining balance is ineligible.

## 5. Decision precedence

If any hard ineligibility rule is present, the result is `INELIGIBLE`, even if another condition also requires manual review. Otherwise, any manual-review condition produces `MANUAL_REVIEW_REQUIRED`. Only a request with no adverse condition produces `ELIGIBLE_FOR_APPROVAL`.

## 6. Explicit non-guarantees

`ELIGIBLE_FOR_APPROVAL` does not mean approved, authorized, reserved or executed. Phase 1 is read-only. The check and any later reservation are not atomic in this phase; later execution work must re-evaluate and reserve the refundable balance transactionally, then use an idempotency key for the provider-side effect.
