package dev.refundworkflow.application.inbound;

import dev.refundworkflow.application.model.RefundEvaluation;
import dev.refundworkflow.domain.model.RefundRequest;

/** Application boundary for evaluating a refund request without approving or executing it. */
public interface EvaluateRefundRequestUseCase {

    RefundEvaluation evaluate(RefundRequest request);
}
