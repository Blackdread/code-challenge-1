package org.blackdread.test.service;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Provide time in UTC
 * Created by Yoann CAPLAIN on 2017/2/16.
 */
@Service
public class CurrentTimeDateTimeService implements DateTimeService {

    @Override
    public ZonedDateTime getCurrentZonedDateTime() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    @Override
    public Timestamp getCurrentTimestamp() {
        return Timestamp.from(Instant.now(Clock.systemUTC()));
    }

    @Override
    public Instant getCurrentInstant() {
        return Clock.systemUTC().instant();
    }

}
