package dev.refundworkflow.domain.policy;

import dev.refundworkflow.domain.model.CustomerId;
import dev.refundworkflow.domain.model.Money;
import dev.refundworkflow.domain.model.PaymentId;
import dev.refundworkflow.domain.model.PaymentSnapshot;
import dev.refundworkflow.domain.model.PaymentStatus;
import dev.refundworkflow.domain.model.RefundDecisionCode;
import dev.refundworkflow.domain.model.RefundEligibilityStatus;
import dev.refundworkflow.domain.model.RefundId;
import dev.refundworkflow.domain.model.RefundRecord;
import dev.refundworkflow.domain.model.RefundReasonCode;
import dev.refundworkflow.domain.model.RefundRequest;
import dev.refundworkflow.domain.model.RefundRequestId;
import dev.refundworkflow.domain.model.RefundStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefundEligibilityPolicyTest {

    private static final PaymentId PAYMENT_ID = new PaymentId("pay-100");
    private static final CustomerId CUSTOMER_ID = new CustomerId("customer-10");
    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");
    private final RefundEligibilityPolicy policy = new RefundEligibilityPolicy();

    @Test
    void allowsRequestWithinRemainingBalanceAndCountsPendingAndSuccessfulRefunds() {
        RefundRequest request = request("50.00", RefundReasonCode.ORDER_CANCELLED);
        List<RefundRecord> history = List.of(
                refund("20.00", RefundStatus.SUCCEEDED),
                refund("10.00", RefundStatus.PENDING),
                refund("15.00", RefundStatus.FAILED),
                refund("5.00", RefundStatus.CANCELLED));

        var decision = policy.evaluate(request, payment(PaymentStatus.PARTIALLY_REFUNDED), history);

        assertEquals(RefundEligibilityStatus.ELIGIBLE_FOR_APPROVAL, decision.status());
        assertEquals(List.of(RefundDecisionCode.ELIGIBLE), decision.decisionCodes());
        assertEquals(Money.of("70.00", "CAD"), decision.remainingRefundableAmount().orElseThrow());
    }

    @Test
    void rejectsAmountAboveRemainingBalance() {
        var decision = policy.evaluate(
                request("81.00", RefundReasonCode.ORDER_CANCELLED),
                payment(PaymentStatus.CAPTURED),
                List.of(refund("20.00", RefundStatus.SUCCEEDED)));

        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.AMOUNT_EXCEEDS_REMAINING_BALANCE));
        assertEquals(Money.of("80.00", "CAD"), decision.remainingRefundableAmount().orElseThrow());
    }

    @Test
    void rejectsUnknownPayment() {
        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), Optional.empty(), List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertEquals(List.of(RefundDecisionCode.PAYMENT_NOT_FOUND), decision.decisionCodes());
        assertTrue(decision.remainingRefundableAmount().isEmpty());
    }

    @Test
    void rejectsPaymentSnapshotForDifferentPaymentReference() {
        var wrongPayment = new PaymentSnapshot(
                new PaymentId("different-payment"), CUSTOMER_ID, Money.of("100.00", "CAD"),
                PaymentStatus.CAPTURED, Optional.of(NOW.minusSeconds(3600)));
        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), Optional.of(wrongPayment), List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.PAYMENT_REFERENCE_MISMATCH));
    }

    @Test
    void rejectsCustomerMismatch() {
        var decision = policy.evaluate(
                new RefundRequest(RefundRequestId.newId(), PAYMENT_ID, new CustomerId("another-customer"),
                        Money.of("10.00", "CAD"), RefundReasonCode.OTHER, NOW),
                payment(PaymentStatus.CAPTURED),
                List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.CUSTOMER_MISMATCH));
    }

    @Test
    void routesDisputedPaymentToManualReview() {
        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), payment(PaymentStatus.DISPUTED), List.of());
        assertEquals(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.PAYMENT_DISPUTED));
    }

    @Test
    void routesSuspectedFraudToManualReview() {
        var decision = policy.evaluate(
                request("10.00", RefundReasonCode.SUSPECTED_FRAUD), payment(PaymentStatus.CAPTURED), List.of());
        assertEquals(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REASON_REQUIRES_MANUAL_REVIEW));
    }

    @Test
    void treatsExistingRequestAsManualReviewInsteadOfCreatingAnotherRefund() {
        RefundRequest request = request("10.00", RefundReasonCode.OTHER);
        var existing = new RefundRecord(
                RefundId.newId(), request.id(), PAYMENT_ID, Money.of("10.00", "CAD"), RefundStatus.PENDING, NOW);

        var decision = policy.evaluate(request, payment(PaymentStatus.CAPTURED), List.of(existing));

        assertEquals(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REQUEST_ALREADY_EXISTS));
    }

    @Test
    void rejectsPaymentAlreadyMarkedAsFullyRefundedEvenWhenHistoryIsEmpty() {
        var decision = policy.evaluate(
                request("10.00", RefundReasonCode.OTHER), payment(PaymentStatus.REFUNDED), List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.PAYMENT_ALREADY_REFUNDED));
        assertEquals(Money.of("0.00", "CAD"), decision.remainingRefundableAmount().orElseThrow());
    }

    @Test
    void rejectsUncapturedPayment() {
        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), payment(PaymentStatus.AUTHORIZED), List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.PAYMENT_NOT_CAPTURED));
    }

    @Test
    void flagsRefundHistoryForAnotherPaymentAndDoesNotExposeBalance() {
        RefundRecord wrongPayment = new RefundRecord(
                RefundId.newId(), RefundRequestId.newId(), new PaymentId("other-payment"),
                Money.of("10.00", "CAD"), RefundStatus.SUCCEEDED, NOW);
        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), payment(PaymentStatus.CAPTURED), List.of(wrongPayment));
        assertEquals(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REFUND_HISTORY_REFERENCE_MISMATCH));
        assertTrue(decision.remainingRefundableAmount().isEmpty());
    }

    @Test
    void flagsInconsistentRefundHistoryAndDoesNotExposeUntrustedBalance() {
        RefundRecord wrongCurrency = new RefundRecord(
                RefundId.newId(), RefundRequestId.newId(), PAYMENT_ID,
                Money.of("10.00", "USD"), RefundStatus.SUCCEEDED, NOW);

        var decision = policy.evaluate(request("10.00", RefundReasonCode.OTHER), payment(PaymentStatus.CAPTURED), List.of(wrongCurrency));

        assertEquals(RefundEligibilityStatus.MANUAL_REVIEW_REQUIRED, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REFUND_HISTORY_CURRENCY_MISMATCH));
        assertTrue(decision.remainingRefundableAmount().isEmpty());
    }

    @Test
    void rejectsHistoryThatExceedsCapturedAmount() {
        var decision = policy.evaluate(
                request("10.00", RefundReasonCode.OTHER),
                payment(PaymentStatus.CAPTURED),
                List.of(refund("70.00", RefundStatus.SUCCEEDED), refund("40.00", RefundStatus.PENDING)));
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REFUND_HISTORY_EXCEEDS_TRANSACTION_AMOUNT));
        assertFalse(decision.remainingRefundableAmount().isPresent());
    }

    @Test
    void rejectsRequestedCurrencyMismatch() {
        var decision = policy.evaluate(
                new RefundRequest(RefundRequestId.newId(), PAYMENT_ID, CUSTOMER_ID, Money.of("10.00", "USD"),
                        RefundReasonCode.OTHER, NOW),
                payment(PaymentStatus.CAPTURED),
                List.of());
        assertEquals(RefundEligibilityStatus.INELIGIBLE, decision.status());
        assertTrue(decision.decisionCodes().contains(RefundDecisionCode.REQUESTED_CURRENCY_MISMATCH));
    }

    private RefundRequest request(String amount, RefundReasonCode reason) {
        return new RefundRequest(RefundRequestId.newId(), PAYMENT_ID, CUSTOMER_ID, Money.of(amount, "CAD"), reason, NOW);
    }

    private Optional<PaymentSnapshot> payment(PaymentStatus status) {
        Optional<Instant> capturedAt = switch (status) {
            case CAPTURED, PARTIALLY_REFUNDED, REFUNDED, DISPUTED -> Optional.of(NOW.minusSeconds(3600));
            case AUTHORIZED, VOIDED, FAILED -> Optional.empty();
        };
        return Optional.of(new PaymentSnapshot(PAYMENT_ID, CUSTOMER_ID, Money.of("100.00", "CAD"), status, capturedAt));
    }

    private RefundRecord refund(String amount, RefundStatus status) {
        return new RefundRecord(RefundId.newId(), RefundRequestId.newId(), PAYMENT_ID, Money.of(amount, "CAD"), status, NOW);
    }
}
