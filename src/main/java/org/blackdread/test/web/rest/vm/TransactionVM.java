package org.blackdread.test.web.rest.vm;


import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * A VM for getting information on transactions.
 */
public class TransactionVM implements Serializable {

    @NotNull
    @DecimalMin(value = "0")
    private Double amount;

    /**
     * Time in epoch in millis in UTC time zone
     */
    @NotNull
    private Long timestamp;
//    private Timestamp timestamp;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Better to use Instant as it is immutable
     *
     * @return Timestamp
     */
    public Instant getTimestampInstant() {
//        return timestamp == null ? null : timestamp.toInstant();
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
    }

    /**
     * Better to use Instant as it is immutable
     *
     * @return Timestamp
     * @see #getTimestampInstant()
     */
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp == null ? null : timestamp.getTime();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TransactionVM that = (TransactionVM) o;
        if (amount == null || that.amount == null || timestamp == null || that.timestamp == null)
            return false;
        return Objects.equals(amount, that.amount) &&
            Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, timestamp);
    }

    @Override
    public String toString() {
        return "TransactionVM{" +
            "amount=" + getAmount() +
            ", timestamp='" + getTimestamp() + "'" +
            "}";
    }
}
