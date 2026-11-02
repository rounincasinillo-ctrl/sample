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
        if (Double.isNaN(previousReading) || Double.isInfinite(previousReading)) {
            throw new IllegalArgumentException("Previous reading must be a valid number.");
        }
        if (previousReading < 0) {
            throw new IllegalArgumentException("Previous reading cannot be negative.");
        }
        if (previousReading > currentReading && currentReading!=0){
            throw new IllegalArgumentException("Previous Reading cannot be greater than the current reading.");
        }
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        if (Double.isNaN(currentReading) || Double.isInfinite(currentReading)) {
            throw new IllegalArgumentException("Current reading must be a valid number.");
        }
        if (currentReading < 0) {
            throw new IllegalArgumentException("Current reading cannot be negative.");
        }
        if (currentReading < previousReading) {
            throw new IllegalArgumentException("Current reading cannot be lower than the previous reading.");
        }
        this.currentReading = currentReading;
    }

    public String getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(String readingDate) {
        if (readingDate == null || readingDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Reading date cannot be blank.");
        }
        String date = readingDate.trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Reading date must be in yyyy-MM-dd format.");
        }
        LocalDate parsed = LocalDate.parse(date);
        if (parsed.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Reading date cannot be in the future.");
        }
        this.readingDate = date;
    }

    public double getConsumption() {
        return getCurrentReading() - getPreviousReading();
    }

    @Override
    public String toString() {
        return String.format("Previous Reading: %.2f%nCurrent Reading: %.2f%nActual Consumption: %.2f kWh%nReading Date: %s",
                getPreviousReading(), getCurrentReading(), getConsumption(), getReadingDate());
    }
}
