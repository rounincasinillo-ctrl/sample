import java.time.LocalDate;
public class MeterReading {

    private double previousReading;
    private double currentReading;
    private String readingDate;

    public MeterReading(double previousReading, double currentReading) {
        this(previousReading, currentReading, LocalDate.now().toString());
    }

    public MeterReading(double previousReading, double currentReading, String readingDate) {
        setPreviousReading(previousReading);
        setCurrentReading(currentReading);
        setReadingDate(readingDate);
    }

    public double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(double previousReading) {
        if (previousReading < 0) {
            throw new IllegalArgumentException("Previous reading cannot be negative.");
        }
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        if (currentReading < 0) {
            throw new IllegalArgumentException("Current reading cannot be negative.");
        }
        if (currentReading < previousReading) {
            throw new IllegalArgumentException("Current reading cannot be lower than the previous reading");
        }
        this.currentReading = currentReading;
    }

    public String getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(String readingDate) {
        if (readingDate == null || readingDate.isBlank()) {
            throw new IllegalArgumentException("Reading date cannot be null or blank.");
        }
        this.readingDate = readingDate.strip();
    }

    public double getConsumption() {
        return getCurrentReading() - getPreviousReading();
    }

    @Override
    public String toString() {
        return String.format("Previous Reading: %.2f%nCurrent Reading: %.2f%nActual Consumption: %.2fkWh%nReading Date: %s",
                getPreviousReading(), getCurrentReading(), getConsumption(), getReadingDate());
    }
}