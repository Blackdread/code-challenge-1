package org.blackdread.test.service;

import org.blackdread.test.web.rest.vm.TransactionVM;

public interface TransactionService {

    /**
     * Add information of a given transaction that happened
     *
     * @param transactionVM vm with data (not null)
     */
    void addTransaction(final TransactionVM transactionVM);
}
