package org.blackdread.test.service;

import org.blackdread.test.web.rest.vm.StatisticsVM;

public interface TransactionStatisticService {

    StatisticsVM getStatisticsLast60Sec();

}
