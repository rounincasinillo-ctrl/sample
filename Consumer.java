public abstract class Consumer implements Billable {
    private String consumerId;
    private String consumerName;
    private String consumerAddress;
    private static int consumerCount = 0;

    public Consumer() {
        this("Unknown Consumer","Unknown Address");
    }

    public Consumer(String consumerName){
        this(consumerName, "Unknown Address");
    }

    public Consumer(String consumerName, String consumerAddress){
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
        if (consumerName == null || consumerName.isBlank()) {
            throw new IllegalArgumentException("Consumer name cannot be blank.");
        }
        String name = consumerName.strip();
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Consumer name must be 2 to 50 characters.");
        }
        if (!Character.isLetter(name.charAt(0))) {
            throw new IllegalArgumentException("Consumer name must start with a letter.");
        }
        if (name.contains("  ")) {
            throw new IllegalArgumentException("Consumer name cannot have repeated spaces.");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '.' && c != '-' && c != '\'') {
                throw new IllegalArgumentException("Consumer name can only contain letters, spaces, periods, hyphens, and apostrophes.");
            }
        }
        this.consumerName = name;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public void setConsumerAddress(String consumerAddress) {
        if (consumerAddress == null || consumerAddress.isBlank()) {
            throw new IllegalArgumentException("Consumer address cannot be blank.");
        }
        String address = consumerAddress.strip();
        if (address.length() < 5 || address.length() > 100) {
            throw new IllegalArgumentException("Consumer address must be 5 to 100 characters.");
        }
        boolean hasLetter = false;
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (!Character.isDigit(c) && c != ' ' && c != '.' && c != ',' && c != '#' && c != '-' && c != '/' && c != '\'') {
                throw new IllegalArgumentException("Consumer address contains invalid characters.");
            }
        }
        if (!hasLetter) {
            throw new IllegalArgumentException("Consumer address must contain at least one letter.");
        }
        this.consumerAddress = address;
    }

    public static int getConsumerCount() {
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
