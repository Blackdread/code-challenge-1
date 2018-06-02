package org.blackdread.test.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.blackdread.test.service.DateTimeService;
import org.blackdread.test.service.TransactionService;
import org.blackdread.test.service.TransactionStatisticService;
import org.blackdread.test.web.rest.vm.StatisticsVM;
import org.blackdread.test.web.rest.vm.TransactionVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;

/**
 * REST controller for managing Transaction.
 */
@RestController
public class TransactionResource {

    private final Logger log = LoggerFactory.getLogger(TransactionResource.class);

    /**
     * Duration which we accept to add statistics received for transaction. Not older than X seconds from current time
     */
    private static final Duration MAX_ALLOWED_AGE_TRANSACTION_DURATION = Duration.ofSeconds(60L);

    private final TransactionService transactionService;

    private final TransactionStatisticService transactionStatisticService;

    private final DateTimeService dateTimeService;

    public TransactionResource(TransactionService transactionService, final TransactionStatisticService transactionStatisticService, final DateTimeService dateTimeService) {
        this.transactionService = transactionService;
        this.transactionStatisticService = transactionStatisticService;
        this.dateTimeService = dateTimeService;
    }

    /**
     * POST  /transactions : Add a new transaction.
     *
     * @param transactionVM the transactionVM to create
     * @return the ResponseEntity with status 201 (Created) and with empty body, or with status 204 (Bad Request) if the transaction is older than 60 seconds
     */
    @PostMapping("/transactions")
    @Timed
    public ResponseEntity<Void> addTransaction(@Valid @RequestBody TransactionVM transactionVM) {
        log.debug("REST request to add Transaction : {}", transactionVM);
        if (dateTimeService.getCurrentInstant().minus(MAX_ALLOWED_AGE_TRANSACTION_DURATION).isAfter(transactionVM.getTimestampInstant()))
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        // Documentation of test does not specify about timestamp that are in the future (UTC) so we consider it as good input
        transactionService.addTransaction(transactionVM);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * GET  /statistics : statistic based on the transactions which happened in the last 60 seconds.
     *
     * @return the statistic based on the transactions which happened in the last 60 seconds
     */
    @GetMapping("/statistics")
    @Timed
    public ResponseEntity<StatisticsVM> getStatisticsLast60Sec() {
        log.debug("REST request to get statistic of Transactions in the last 60 seconds");
        StatisticsVM statisticsVM = transactionStatisticService.getStatisticsLast60Sec();
        return new ResponseEntity<>(statisticsVM, HttpStatus.OK);
    }

}
