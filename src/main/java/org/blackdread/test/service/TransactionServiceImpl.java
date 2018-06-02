package org.blackdread.test.service;

import org.blackdread.test.web.rest.vm.StatisticsVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Service Implementation for managing Transaction.
 */
@Service
public class TransactionServiceImpl implements TransactionService, TransactionStatisticService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);


    public TransactionServiceImpl() {
    }

    @Override
    public StatisticsVM getStatisticsLast60Sec() {
        final StatisticsVM vm = new StatisticsVM();
        vm.setSum(10.00001);
        vm.setAvg(1.0005);
        vm.setMax(2.00000999);
        vm.setMin(0.000000001);
        vm.setCount(20L);
        return vm;
    }
}
