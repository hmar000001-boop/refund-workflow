package dev.refundworkflow.domain.policy;

import dev.refundworkflow.domain.model.Money;
import dev.refundworkflow.domain.model.PaymentSnapshot;
import dev.refundworkflow.domain.model.PaymentStatus;
import dev.refundworkflow.domain.model.RefundDecisionCode;
import dev.refundworkflow.domain.model.RefundEligibilityDecision;
import dev.refundworkflow.domain.model.RefundEligibilityStatus;
import dev.refundworkflow.domain.model.RefundRecord;
import dev.refundworkflow.domain.model.RefundReasonCode;
import dev.refundworkflow.domain.model.RefundRequest;
import dev.refundworkflow.domain.model.RefundStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Pure, deterministic eligibility policy. An eligible result is only a recommendation
 * to enter an approval process; it is neither approval nor a reservation of funds.
 */
public final class RefundEligibilityPolicy {

    public RefundEligibilityDecision evaluate(
            RefundRequest request,
            Optional<PaymentSnapshot> paymentResult,
            List<RefundRecord> refundHistory) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(paymentResult, "paymentResult must not be null");
        Objects.requireNonNull(refundHistory, "refundHistory must not be null");

        if (paymentResult.isEmpty()) {
            return decision(
                    RefundEligibilityStatus.INELIGIBLE,
                    List.of(RefundDecisionCode.PAYMENT_NOT_FOUND),
                    Optional.empty());
        }

        PaymentSnapshot payment = paymentResult.orElseThrow();
        List<RefundDecisionCode> codes = new ArrayList<>();
        boolean ineligible = false;
        boolean manualReview = false;

        if (!payment.paymentId().equals(request.paymentId())) {
            codes.add(RefundDecisionCode.PAYMENT_REFERENCE_MISMATCH);
            ineligible = true;
        }
        if (!payment.customerId().equals(request.customerId())) {
            codes.add(RefundDecisionCode.CUSTOMER_MISMATCH);
            ineligible = true;
        }

        if (payment.status() == PaymentStatus.DISPUTED) {
            codes.add(RefundDecisionCode.PAYMENT_DISPUTED);
            manualReview = true;
        } else if (payment.status() == PaymentStatus.REFUNDED) {
            codes.add(RefundDecisionCode.PAYMENT_ALREADY_REFUNDED);
            ineligible = true;
        } else if (payment.status() == PaymentStatus.AUTHORIZED
                || payment.status() == PaymentStatus.VOIDED
                || payment.status() == PaymentStatus.FAILED) {
            codes.add(RefundDecisionCode.PAYMENT_NOT_CAPTURED);
            ineligible = true;
        }

        if (refundHistory.stream().anyMatch(record -> record.requestId().equals(request.id()))) {
            codes.add(RefundDecisionCode.REQUEST_ALREADY_EXISTS);
            manualReview = true;
        }

        if (!request.requestedAmount().currency().equals(payment.transactionAmount().currency())) {
            codes.add(RefundDecisionCode.REQUESTED_CURRENCY_MISMATCH);
            ineligible = true;
        }

        Money reservedAmount = Money.zero(payment.transactionAmount().currency());
        boolean historyReliable = true;
        for (RefundRecord record : refundHistory) {
            if (!record.paymentId().equals(payment.paymentId())) {
                codes.add(RefundDecisionCode.REFUND_HISTORY_REFERENCE_MISMATCH);
                manualReview = true;
                historyReliable = false;
                continue;
            }
            if (!record.amount().currency().equals(payment.transactionAmount().currency())) {
                codes.add(RefundDecisionCode.REFUND_HISTORY_CURRENCY_MISMATCH);
                manualReview = true;
                historyReliable = false;
                continue;
            }
            if (record.status() == RefundStatus.PENDING || record.status() == RefundStatus.SUCCEEDED) {
                reservedAmount = reservedAmount.plus(record.amount());
            }
        }

        Optional<Money> remaining = Optional.empty();
        if (historyReliable) {
            if (reservedAmount.isGreaterThan(payment.transactionAmount())) {
                codes.add(RefundDecisionCode.REFUND_HISTORY_EXCEEDS_TRANSACTION_AMOUNT);
                ineligible = true;
            } else if (payment.status() == PaymentStatus.REFUNDED
                    || payment.status() == PaymentStatus.AUTHORIZED
                    || payment.status() == PaymentStatus.VOIDED
                    || payment.status() == PaymentStatus.FAILED) {
                remaining = Optional.of(Money.zero(payment.transactionAmount().currency()));
            } else {
                remaining = Optional.of(payment.transactionAmount().minus(reservedAmount));
                if (request.requestedAmount().currency().equals(payment.transactionAmount().currency())
                        && request.requestedAmount().isGreaterThan(remaining.orElseThrow())) {
                    codes.add(RefundDecisionCode.AMOUNT_EXCEEDS_REMAINING_BALANCE);
                    ineligible = true;
                }
            }
        }

        if (request.reasonCode() == RefundReasonCode.SUSPECTED_FRAUD) {
            codes.add(RefundDecisionCode.REASON_REQUIRES_MANUAL_REVIEW);
            manualReview = true;
        }

        if (ineligible) {
            return decision(RefundEligibilityStatus.INELIGIBLE, codes, remaining);
        }
        if (manualReview) {
            return decision(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, codes, remaining);
        }
        return decision(
                RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL,
                List.of(RefundDecisionCode.ELIGIBLE),
                remaining);
    }

    private static RefundEligibilityDecision decision(
            RefundEligibilityStatus status,
            List<RefundDecisionCode> codes,
            Optional<Money> remaining) {
        return new RefundEligibilityDecision(status, codes.stream().distinct().toList(), remaining);
    }
}
