package org.blackdread.test.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Provide time
 * Created by Yoann CAPLAIN on 2017/2/16.
 */
public interface DateTimeService {
    /**
     * @return ZonedDateTime
     */
    ZonedDateTime getCurrentZonedDateTime();

    /**
     * @return LocalDateTime
     */
    LocalDateTime getCurrentDateTime();

    /**
     * @return LocalDate
     */
    LocalDate getCurrentDate();

    /**
     * @return Timestamp
     */
    Timestamp getCurrentTimestamp();

    /**
     * @return Instant
     */
    Instant getCurrentInstant();
}
