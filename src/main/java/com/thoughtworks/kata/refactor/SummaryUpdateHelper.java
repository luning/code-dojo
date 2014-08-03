package com.thoughtworks.kata.refactor;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.thoughtworks.kata.refactor.TrustIndicator.TRUSTED;
import static com.thoughtworks.kata.refactor.TrustIndicator.UNTRUSTED;

public class SummaryUpdateHelper {
    private Map<String, Object> updatedFields;

    public SummaryUpdateHelper(Map<String, Object> updatedFields) {
        this.updatedFields = updatedFields;
    }

    public void updateSummary(Request request, Customer customer) {
        RequestSummary requestSummary = request.getSummary();
        Summary summaryFromDb = customer.getSummary();

        if (summaryFromDb == null && requestSummary != null) {
            summaryFromDb = new Summary();
        }

        if (null != requestSummary) {
            updateSummaryAttributes(requestSummary, summaryFromDb);
        }
        if (updatedFields.containsKey("summary")) {
            customer.setSummary(summaryFromDb);
        }
    }

    private void updateSummaryAttributes(RequestSummary requestSummary, Summary dbSummary) {
        String requestSummaryDetail = requestSummary.getSummaryDetail();
        TrustIndicator requestTrustIndicator = requestSummary.getTrustIndicator();

        String dbSummaryDetail = dbSummary.getDetail();
        TrustIndicator dbTrustIndicator = dbSummary.getTrustIndicator();

        boolean shouldUpdateDetail = shouldUpdateDetail(requestSummaryDetail,
                dbSummaryDetail, requestTrustIndicator);

        if (shouldUpdateDetail && !Objects.equals(dbSummaryDetail, requestSummary)) {
            dbSummary.setDetail(trimToNull(requestSummaryDetail));
            updatedFields.put("summary", dbSummary);
        }

        TrustIndicator updatedIndicator = getUpdatedIndicator(requestSummaryDetail,
                dbSummaryDetail, requestTrustIndicator, dbTrustIndicator);

        dbSummary.setTrustIndicator(updatedIndicator);

        if (updatedIndicator != dbTrustIndicator) {
            updatedFields.put("summary", dbSummary);
        }
    }

    private boolean shouldUpdateDetail(String requestSummaryDetail, String dbSummaryDetail,
                                       TrustIndicator requestTrustIndicator) {
        boolean isDbDetailBlank = isNullOrEmpty(dbSummaryDetail);

        if (isNullOrEmpty(requestSummaryDetail)) {
            return !isDbDetailBlank && TRUSTED == requestTrustIndicator;
        }

        return isDbDetailBlank || !isSame(requestSummaryDetail, dbSummaryDetail);
    }

    private TrustIndicator getUpdatedIndicator(String requestSummaryDetail, String dbSummaryDetail,
                                               TrustIndicator requestTrustIndicator,
                                               TrustIndicator dbTrustIndicator) {
        boolean shouldUpdateIndicator = false;
        boolean isDbDetailBlank = isNullOrEmpty(dbSummaryDetail);
        boolean isRequestDetailBlank = isNullOrEmpty(requestSummaryDetail);

        if (isRequestDetailBlank && !isDbDetailBlank && TRUSTED == requestTrustIndicator) {
            shouldUpdateIndicator = true;
        } else if (!isRequestDetailBlank && isDbDetailBlank) {
            shouldUpdateIndicator = true;
        } else if (!isRequestDetailBlank) {
            if (TRUSTED == requestTrustIndicator) {
                shouldUpdateIndicator = true;
            } else if (UNTRUSTED == requestTrustIndicator
                    && !isSame(requestSummaryDetail, dbSummaryDetail)) {
                shouldUpdateIndicator = true;
            }
        }

        TrustIndicator updatedIndicator = dbTrustIndicator;
        if (shouldUpdateIndicator) {
            updatedIndicator = requestTrustIndicator;
        }
        return updatedIndicator;
    }

    private boolean isSame(String requestSummaryDetail, String dbSummaryDetail) {
        return null != requestSummaryDetail && null != dbSummaryDetail
                && requestSummaryDetail.equalsIgnoreCase(dbSummaryDetail);
    }

    private static String trimToNull(String target) {
        if (target == null) {
            return null;
        }
        String trimmed = target.trim();
        return "".equals(trimmed) ? null : trimmed;
    }

}
