package org.blackdread.test.service;

import org.blackdread.test.web.rest.vm.StatisticsVM;
import org.blackdread.test.web.rest.vm.TransactionVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BinaryOperator;


/**
 * Service Implementation for managing Transaction.
 */
@Service
public class TransactionServiceImpl implements TransactionService, TransactionStatisticService, TransactionTestService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    /**
     * Duration which we accept to add statistics received for transaction. Not older than X seconds from current time
     */
    private final Duration DURATION_60_SECONDS = Duration.ofSeconds(60L);

    private final DateTimeService dateTimeService;

    // Not the most efficient way to make thread-safe but for first implementation we do with that
    final SortedMap<Instant, ValueWrapper> map = Collections.synchronizedSortedMap(new TreeMap<>());

    public TransactionServiceImpl(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    // Can use BigDecimal if we want to make sure to not have rounding errors

    @Scheduled(initialDelay = 60000, fixedDelay = 30000)
    public void cleanOldData() {
        final SortedMap<Instant, ValueWrapper> oldData = map.headMap(dateTimeService.getCurrentInstant().minus(DURATION_60_SECONDS.toMillis() * 2, ChronoUnit.MILLIS));
        oldData.clear();
    }

    @Override
    public void addTransaction(final TransactionVM transactionVM) {
        map.compute(transactionVM.getTimestampInstant(), (instant, valueWrapper) -> {
            if (valueWrapper == null) {
                return new ValueWrapper(transactionVM);
            }
            valueWrapper.addNewTransaction(transactionVM);
            return valueWrapper;
        });
    }

    @Override
    public Double getSumAmount() {
        final Collection<ValueWrapper> values = map.values();
        synchronized (map) {
            return values.stream().mapToDouble(wrapper -> wrapper.sumAmount).sum();
        }
    }

    @Override
    public void clearAllValues() {
        map.clear();
    }

    @Override
    public StatisticsVM getStatisticsLast60Sec() {
        final Instant now = dateTimeService.getCurrentInstant();
        final SortedMap<Instant, ValueWrapper> subMap = map.subMap(now.minus(DURATION_60_SECONDS), now);
        synchronized (map) {
            //*
            // way 1 but not best
            return subMap.values().parallelStream()
                .reduce(accumulateWrapper())
                .map(wrapper -> new StatisticsVM(wrapper.sumAmount, wrapper.sumAmount / wrapper.count, wrapper.maxAmount, wrapper.minAmount, wrapper.count))
                .orElseGet(StatisticsVM::new);
            //*/
        }
    }

    private static BinaryOperator<ValueWrapper> accumulateWrapper() {
        return (valueWrapper, valueWrapper2) -> {
            final ValueWrapper wrapper = new ValueWrapper(valueWrapper);
            wrapper.count = valueWrapper.count + valueWrapper2.count;
            wrapper.minAmount = valueWrapper.minAmount < valueWrapper2.minAmount ? valueWrapper.minAmount : valueWrapper2.minAmount;
            wrapper.maxAmount = valueWrapper.maxAmount > valueWrapper2.maxAmount ? valueWrapper.maxAmount : valueWrapper2.maxAmount;
            wrapper.sumAmount += valueWrapper2.sumAmount;
            return wrapper;
        };
    }

    /*
    private static StatisticsVM combine(final StatisticsVM a, final StatisticsVM b) {
        final StatisticsVM vm = new StatisticsVM();
        vm.setSum(a.getSum() + b.getSum());
        vm.setCount(a.getCount() + b.getCount());
        vm.setMax(b.getMax() > a.getMax() ? b.getMax() : a.getMax());
        vm.setMin(b.getMin() < a.getMin() ? b.getMin() : a.getMin());
        vm.setAvg(vm.getSum() / vm.getCount());
        return a;
    }
    //*/

    private static class ValueWrapper {
        private Double sumAmount;
        private Double maxAmount;
        private Double minAmount;
        private long count = 0;

        public ValueWrapper(final ValueWrapper wrapper) {
            sumAmount = wrapper.sumAmount;
            maxAmount = wrapper.maxAmount;
            minAmount = wrapper.minAmount;
            count = wrapper.count;
        }

        public ValueWrapper(final TransactionVM transactionVM) {
            addNewTransaction(transactionVM);
        }

        public void addNewTransaction(final TransactionVM transactionVM) {
            sumAmount = sumAmount == null ? transactionVM.getAmount() : sumAmount + transactionVM.getAmount();
            maxAmount = maxAmount == null ? transactionVM.getAmount() : transactionVM.getAmount() > maxAmount ? transactionVM.getAmount() : maxAmount;
            minAmount = minAmount == null ? transactionVM.getAmount() : transactionVM.getAmount() < minAmount ? transactionVM.getAmount() : minAmount;
            count++;
        }

        @Override
        public String toString() {
            return "ValueWrapper{" +
                "sumAmount=" + sumAmount +
                ", maxAmount=" + maxAmount +
                ", minAmount=" + minAmount +
                ", count=" + count +
                '}';
        }
    }
}
