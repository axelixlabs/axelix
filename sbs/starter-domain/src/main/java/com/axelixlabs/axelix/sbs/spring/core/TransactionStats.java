package com.axelixlabs.axelix.sbs.spring.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LazyLoadingTarget;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;
import org.jspecify.annotations.NonNull;

/**
 * These are transactional stats that we have collected for the given transactional method
 * within the lifetime of the application.
 * <p>
 * There are a couple of very important nuances that we need to take seriously here. First of all, the
 * {@link TransactionStats#put(TransactionExecutionProfile)} is supposedly going to be relatively hot,
 * since the given transactional method will be executed quite often.
 *
 * @author Mikhail Polivakha
 */
public class TransactionStats {

    private Map<LazyLoadingTarget, Integer> nPlusOneOccasions;
    private Map<String, Integer> inMemoryPaginatedEntities;

    public void put(TransactionExecutionProfile transaction) {
        var incomingNPlusOneMap = new HashMap<LazyLoadingTarget, Integer>();

        for (TransactionExecutionProfile.AnalyzedSqlQueryRecord recordedQuery : transaction.getRecordedQueries()) {

            if (recordedQuery.getLazyLoadingTarget() != null) {

                incomingNPlusOneMap.compute(recordedQuery.getLazyLoadingTarget(), (target, records) -> {
                    if (records == null) {
                        records = 0;
                    }

                    return records + 1;
                });
            }

            if (recordedQuery.isInMemoryPaginated()) {
                int index = recordedQuery.getSql().indexOf(" from ");

                if (index == -1) {
                    index = recordedQuery.getSql().indexOf(" FROM ");
                }

                if (index == -1) {
                    // o_0, how can it be? warning and skip
                }
                String trimmedSql = recordedQuery.getSql().substring(index + 6).trim();
                int tailingWhitespace = trimmedSql.indexOf("\\s+"); // whitespace
                String selectionTarget = trimmedSql.substring(0, tailingWhitespace);

                inMemoryPaginatedEntities.compute(selectionTarget, (s, integer) -> {
                    if (integer == null) {
                        return 1;
                    }

                    return integer + 1;
                });
            }
        }

        incomingNPlusOneMap.forEach((lazyLoadingTarget, incomingCounter) -> {
            nPlusOneOccasions.compute(lazyLoadingTarget, (key, oldValue) -> {
                if (oldValue == null) {
                    return incomingCounter;
                }
                return Math.max(oldValue, incomingCounter);
            });
        });
    }

    private static @NonNull HashMap<LazyLoadingTarget, Integer> assembleNPlusOneMap(TransactionExecutionProfile transaction) {
        var result = new HashMap<LazyLoadingTarget, Integer>();

        for (TransactionExecutionProfile.AnalyzedSqlQueryRecord recordedQuery : transaction.getRecordedQueries()) {

            if (recordedQuery.getLazyLoadingTarget() != null) {
                result.compute(recordedQuery.getLazyLoadingTarget(), (target, records) -> {
                    if (records == null) {
                        records = 0;
                    }

                    return records + 1;
                });
            }
        }
        return result;
    }

}
