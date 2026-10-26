
public abstract class Consumer implements Billable{
    private String consumerId;
    private String consumerName;
    private String consumerAddress;
    private static int consumerCount = 0;

    public Consumer(String consumerName, String consumerAddress) {
        this(String.format("C-%04d", consumerCount + 1), consumerName, consumerAddress);
    }

    public Consumer(String consumerId, String consumerName, String consumerAddress) {
        setConsumerId(consumerId);
        setConsumerName(consumerName);
        setConsumerAddress(consumerAddress);
        consumerCount++;
    }

    public String getConsumerId() {
        return consumerId;
    }
    public void setConsumerId(String consumerId) {
        if (consumerId == null || !consumerId.strip().matches("C-\\d{4,}")) {
            throw new IllegalArgumentException("Consumer ID must follow the format C-0001.");
        }
        this.consumerId = consumerId.strip();
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        if (consumerName == null || consumerName.strip().length() < 2 || consumerName.strip().length() > 50) {
            throw new IllegalArgumentException("Consumer name must be 2 to 50 characters.");
        }
        this.consumerName = consumerName.strip();
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public void setConsumerAddress(String consumerAddress) {
        if (consumerAddress == null || consumerAddress.strip().length() < 5 || consumerAddress.strip().length() > 100) {
            throw new IllegalArgumentException("Consumer address must be 5 to 100 characters.");
        }
        this.consumerAddress = consumerAddress.strip();
    }

    public static int getConsumerCount(){
        return consumerCount;
    }
    public static void setConsumerCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Consumer count cannot be negative.");
        }
        consumerCount = count;
    }
    @Override
    public abstract double calculateBill(MeterReading reading);
    @Override
    public abstract double getRatePerKwh();
    @Override
    public abstract String getBillableType();

    @Override
    public String toString() {
        return "Consumer ID: " + getConsumerId()
                + " | Name: " + getConsumerName()
                + " | Address: " + getConsumerAddress()
                + " | Consumer Type: " + getBillableType()
                + String.format("%nRate per kWh: ₱%.2f", getRatePerKwh());
    }
}