package uk.gov.pay.products.matchers;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.model.Link;
import uk.gov.pay.products.client.publicapi.model.Links;
import uk.gov.pay.products.client.publicapi.model.PaymentState;
import uk.gov.pay.products.client.publicapi.model.RefundSummary;
import uk.gov.pay.products.client.publicapi.model.SettlementSummary;

import jakarta.json.JsonObject;

public class PaymentResponseMatcher {
    public static Matcher<PaymentResponse> hasAllPaymentProperties(final JsonObject expectedPaymentResponse) {
        return new BaseMatcher<>() {
            @Override
            public boolean matches(final Object obj) {
                final PaymentResponse actualPaymentResponse = (PaymentResponse) obj;

                boolean matched = true;
                matched = matched && StringUtils.equals(actualPaymentResponse.getPaymentId(), expectedPaymentResponse.getString("payment_id"));
                matched = matched && (actualPaymentResponse.getAmount() == (long) expectedPaymentResponse.getInt("amount"));

                JsonObject expectedState = expectedPaymentResponse.getJsonObject("state");
                PaymentState actualState = actualPaymentResponse.getState();
                if (actualState == null) return false;

                matched = matched && StringUtils.equals(actualState.getStatus(), expectedState.getString("status"));
                matched = matched && (actualState.isFinished() == expectedState.getBoolean("finished"));
                matched = matched && StringUtils.equals(actualState.getMessage(), expectedState.getString("message"));
                matched = matched && StringUtils.equals(actualState.getCode(), expectedState.getString("code"));

                matched = matched && StringUtils.equals(actualPaymentResponse.getDescription(), expectedPaymentResponse.getString("description"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getReference(), expectedPaymentResponse.getString("reference"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getEmail(), expectedPaymentResponse.getString("email"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getEmail(), expectedPaymentResponse.getString("email"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getPaymentProvider(), expectedPaymentResponse.getString("payment_provider"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getReturnUrl(), expectedPaymentResponse.getString("return_url"));
                matched = matched && StringUtils.equals(actualPaymentResponse.getCreatedDate(), expectedPaymentResponse.getString("created_date"));

                JsonObject expectedRefundSummary = expectedPaymentResponse.getJsonObject("refund_summary");
                RefundSummary actualRefundSummary = actualPaymentResponse.getRefundSummary();
                if (actualRefundSummary == null) return false;

                matched = matched && StringUtils.equals(actualRefundSummary.getStatus(), expectedRefundSummary.getString("status"));
                matched = matched && (actualRefundSummary.getAmountAvailable() == (long) expectedRefundSummary.getInt("amount_available"));
                matched = matched && (actualRefundSummary.getAmountSubmitted() == (long) expectedRefundSummary.getInt("amount_submitted"));

                JsonObject expectedSettlementSummary = expectedPaymentResponse.getJsonObject("settlement_summary");
                SettlementSummary actualSettlementSummary = actualPaymentResponse.getSettlementSummary();
                if (actualSettlementSummary == null) return false;

                matched = matched && StringUtils.equals(actualSettlementSummary.getCaptureSubmitTime(), expectedSettlementSummary.getString("capture_submit_time"));
                matched = matched && StringUtils.equals(actualSettlementSummary.getCapturedDate(), expectedSettlementSummary.getString("captured_date"));

                JsonObject expectedLinks = expectedPaymentResponse.getJsonObject("_links");
                Links actualLinks = actualPaymentResponse.getLinks();
                if (actualLinks == null) return false;

                JsonObject expectedSelfLink = expectedLinks.getJsonObject("self");
                Link actualSelfLink = actualLinks.getSelf();
                if (actualSelfLink == null) return false;

                matched = matched && StringUtils.equals(actualSelfLink.getHref(), expectedSelfLink.getString("href"));
                matched = matched && StringUtils.equals(actualSelfLink.getMethod(), expectedSelfLink.getString("method"));

                JsonObject expectedNextUrlLink = expectedLinks.getJsonObject("next_url");
                Link actualNextUrlLink = actualLinks.getNextUrl();
                if (actualNextUrlLink == null) return false;

                matched = matched && StringUtils.equals(actualNextUrlLink.getHref(), expectedNextUrlLink.getString("href"));
                matched = matched && StringUtils.equals(actualNextUrlLink.getMethod(), expectedNextUrlLink.getString("method"));

                JsonObject expectedNextUrlPostLink = expectedLinks.getJsonObject("next_url_post");
                Link actualNextUrlPostLink = actualLinks.getNextUrlPost();
                if (actualNextUrlPostLink == null) return false;

                matched = matched && StringUtils.equals(actualNextUrlPostLink.getHref(), expectedNextUrlPostLink.getString("href"));
                matched = matched && StringUtils.equals(actualNextUrlPostLink.getMethod(), expectedNextUrlPostLink.getString("method"));
                matched = matched && StringUtils.equals(actualNextUrlPostLink.getType(), expectedNextUrlPostLink.getString("type"));

                JsonObject expectedEventsLink = expectedLinks.getJsonObject("events");
                Link actualEventsLink = actualLinks.getEvents();
                if (actualEventsLink == null) return false;

                matched = matched && StringUtils.equals(actualEventsLink.getHref(), expectedEventsLink.getString("href"));
                matched = matched && StringUtils.equals(actualEventsLink.getMethod(), expectedEventsLink.getString("method"));

                JsonObject expectedRefundsLink = expectedLinks.getJsonObject("refunds");
                Link actualRefundsLink = actualLinks.getRefunds();
                if (actualRefundsLink == null) return false;

                matched = matched && StringUtils.equals(actualRefundsLink.getHref(), expectedRefundsLink.getString("href"));
                matched = matched && StringUtils.equals(actualRefundsLink.getMethod(), expectedRefundsLink.getString("method"));

                JsonObject expectedCancelsLink = expectedLinks.getJsonObject("cancel");
                Link actualCancelLink = actualLinks.getCancel();
                if (actualCancelLink == null) return false;

                matched = matched && StringUtils.equals(actualCancelLink.getHref(), expectedCancelsLink.getString("href"));
                matched = matched && StringUtils.equals(actualCancelLink.getMethod(), expectedCancelsLink.getString("method"));

                return matched;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("PaymentResponse ").appendValue(expectedPaymentResponse);
            }
        };
    }
}

