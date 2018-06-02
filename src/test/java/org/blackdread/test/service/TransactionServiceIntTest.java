package org.blackdread.test.service;

import org.blackdread.test.TestApp;
import org.blackdread.test.web.rest.vm.StatisticsVM;
import org.blackdread.test.web.rest.vm.TransactionVM;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class TransactionServiceIntTest {

    private static final Double DEFAULT_AMOUNT = 0D;
    private static final Double UPDATED_AMOUNT = 1D;

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionStatisticService transactionStatisticService;

    @Autowired
    private TransactionTestService transactionTestService;

    @Autowired
    private DateTimeService dateTimeService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        transactionTestService.clearAllValues();
    }

    public static TransactionVM createVM() {
        TransactionVM vm = new TransactionVM();
        vm.setAmount(DEFAULT_AMOUNT);
        vm.setTimestamp(Timestamp.from(DEFAULT_TIMESTAMP));
        return vm;
    }

    public static TransactionVM createVM2() {
        TransactionVM vm = new TransactionVM();
        vm.setAmount(UPDATED_AMOUNT);
        vm.setTimestamp(Timestamp.from(UPDATED_TIMESTAMP));
        return vm;
    }

    @Test
    @Repeat(20)
    public void addTransactionIsThreadSafe() {
        final int TIMES_RUN = 10000;
        final TransactionVM vm1 = createVM();
        final TransactionVM vm2 = createVM2();
        final ArrayList<CompletableFuture<Void>> futures = new ArrayList<>(TIMES_RUN);
        for (int i = 0; i < TIMES_RUN; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                    transactionService.addTransaction(vm1);
                    transactionService.addTransaction(vm2);
                })
            );
        }
        futures.forEach(future -> {
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                throw new RuntimeException(ex);
            }
        });
        final double expectedResult = TIMES_RUN * vm1.getAmount() + TIMES_RUN * vm2.getAmount();
        Assert.assertEquals(expectedResult, transactionTestService.getSumAmount(), 0.001);
    }

    @Test
    @Repeat(20)
    public void getStatisticsLast60SecIsCoherent() {
        final TransactionVM vm1 = createVM();
        vm1.setAmount(15.55133);
        vm1.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(5)));
        final TransactionVM vm2 = createVM();
        vm2.setAmount(20.0);
        vm2.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(10)));
        final TransactionVM vm3 = createVM();
        vm3.setAmount(55.6666);
        vm3.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(15)));
        final TransactionVM vm4 = createVM();
        vm4.setAmount(1.6);
        vm4.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(20)));
        transactionService.addTransaction(vm1);
        transactionService.addTransaction(vm2);
        transactionService.addTransaction(vm3);
        transactionService.addTransaction(vm4);
        final StatisticsVM last60Sec = transactionStatisticService.getStatisticsLast60Sec();
        final double sumExpected = 15.55133 + 20.0 + 55.6666 + 1.6;
        final double delta = 0.000001;
        Assert.assertEquals(1.6, last60Sec.getMin(), delta);
        Assert.assertEquals(55.6666, last60Sec.getMax(), delta);
        Assert.assertEquals(sumExpected / 4, last60Sec.getAvg(), delta);
        Assert.assertEquals(4.0, last60Sec.getCount(), delta);
        Assert.assertEquals(sumExpected, last60Sec.getSum(), delta);
        Assert.assertEquals(sumExpected, transactionTestService.getSumAmount(), delta);
    }

    @Test
    @Repeat(20)
    public void getStatisticsLast60SecIsCoherentWithManySameTimeTransaction() {
        final TransactionVM vm1 = createVM();
        vm1.setAmount(15.55133);
        vm1.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(5)));
        final TransactionVM vm2 = createVM();
        vm2.setAmount(20.0);
        vm2.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(10)));
        final TransactionVM vm3 = createVM();
        vm3.setAmount(55.6666);
        vm3.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(15)));
        final TransactionVM vm4 = createVM();
        vm4.setAmount(1.6);
        vm4.setTimestamp(Timestamp.from(dateTimeService.getCurrentInstant().minusMillis(20)));
        transactionService.addTransaction(vm1);
        transactionService.addTransaction(vm1);
        transactionService.addTransaction(vm2);
        transactionService.addTransaction(vm3);
        transactionService.addTransaction(vm3);
        transactionService.addTransaction(vm4);
        transactionService.addTransaction(vm4);
        transactionService.addTransaction(vm4);
        final StatisticsVM last60Sec = transactionStatisticService.getStatisticsLast60Sec();
        final double sumExpected = 15.55133 * 2 + 20.0 + 55.6666 * 2 + 1.6 * 3;
        final double delta = 0.000001;
        Assert.assertEquals(1.6, last60Sec.getMin(), delta);
        Assert.assertEquals(55.6666, last60Sec.getMax(), delta);
        Assert.assertEquals(sumExpected / 8, last60Sec.getAvg(), delta);
        Assert.assertEquals(8.0, last60Sec.getCount(), delta);
        Assert.assertEquals(sumExpected, last60Sec.getSum(), delta);
        Assert.assertEquals(sumExpected, transactionTestService.getSumAmount(), delta);
    }
}
