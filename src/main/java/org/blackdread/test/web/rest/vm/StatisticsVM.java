package org.blackdread.test.web.rest.vm;

/**
 * View Model object for returning statistics on Transactions.
 */
public class StatisticsVM {

    private Double sum = 0.0;

    private Double avg = 0.0;

    private Double max = 0.0;

    private Double min = 0.0;

    private Long count = 0L;

    public StatisticsVM() {
    }

    public StatisticsVM(final Double sum, final Double avg, final Double max, final Double min, final Long count) {
        this.sum = sum;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.count = count;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(final Double sum) {
        this.sum = sum;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(final Double avg) {
        this.avg = avg;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(final Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(final Double min) {
        this.min = min;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(final Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "StatisticsVM{" +
            "sum=" + sum +
            ", avg=" + avg +
            ", max=" + max +
            ", min=" + min +
            ", count=" + count +
            '}';
    }
}
