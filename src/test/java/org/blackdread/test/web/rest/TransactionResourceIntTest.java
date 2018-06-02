package org.blackdread.test.web.rest;

import org.blackdread.test.TestApp;
import org.blackdread.test.service.DateTimeService;
import org.blackdread.test.service.TransactionService;
import org.blackdread.test.service.TransactionStatisticService;
import org.blackdread.test.web.rest.errors.ExceptionTranslator;
import org.blackdread.test.web.rest.vm.TransactionVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.blackdread.test.web.rest.TestUtil.createFormattingConversionService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the TransactionResource REST controller.
 *
 * @see TransactionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class TransactionResourceIntTest {

    private static final Double DEFAULT_AMOUNT = 0D;
    private static final Double UPDATED_AMOUNT = 1D;

    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionStatisticService transactionStatisticService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;


    private MockMvc restTransactionMockMvc;

    private TransactionVM transactionVM;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TransactionResource transactionResource = new TransactionResource(transactionService, transactionStatisticService, dateTimeService);
        this.restTransactionMockMvc = MockMvcBuilders.standaloneSetup(transactionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }


    public static TransactionVM createVM() {
        TransactionVM vm = new TransactionVM();
        vm.setAmount(DEFAULT_AMOUNT);
        vm.setTimestamp(Timestamp.from(DEFAULT_TIMESTAMP));
        return vm;
    }

    @Before
    public void initTest() {
        transactionVM = createVM();
    }

    @Test
    public void addTransactionTooOld() throws Exception {
        // Create the Transaction
        TransactionVM vm = createVM();
        restTransactionMockMvc.perform(post("/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vm)))
            .andExpect(status().isNoContent());
    }

    @Test
    public void addTransactionNowIsOk() throws Exception {
        // Create the Transaction
        TransactionVM vm = createVM();
        vm.setTimestamp(Timestamp.from(Instant.now().truncatedTo(ChronoUnit.MILLIS)));
        restTransactionMockMvc.perform(post("/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vm)))
            .andExpect(status().isCreated());
    }

    @Test
    public void checkAmountIsRequired() throws Exception {
        TransactionVM vm = createVM();
        // set the field null
        vm.setAmount(null);

        restTransactionMockMvc.perform(post("/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vm)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkTimestampIsRequired() throws Exception {
        TransactionVM vm = createVM();
        // set the field null
        vm.setTimestamp(null);

        restTransactionMockMvc.perform(post("/transactions")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(vm)))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void getStatistics() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get("/statistics"))
            .andExpect(status().isOk());
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TransactionVM.class);
        TransactionVM transaction1 = new TransactionVM();
        transaction1.setAmount(1.0001);
        transaction1.setTimestamp(Timestamp.from(DEFAULT_TIMESTAMP));
        TransactionVM transaction2 = new TransactionVM();
        transaction2.setAmount(1.0001);
        transaction2.setTimestamp(Timestamp.from(DEFAULT_TIMESTAMP));
        assertThat(transaction1).isEqualTo(transaction2);

        transaction1.setTimestamp(Timestamp.from(UPDATED_TIMESTAMP));
        transaction2.setTimestamp(Timestamp.from(UPDATED_TIMESTAMP));
        assertThat(transaction1).isEqualTo(transaction2);

        transaction2.setAmount(1.0002);
        assertThat(transaction1).isNotEqualTo(transaction2);

        transaction1.setAmount(null);
        assertThat(transaction1).isNotEqualTo(transaction2);
    }

}
