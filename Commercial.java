public class Commercial extends Consumer {
    private static final double RATE_PER_KWH = 18.00;

    public Commercial(String consumerName, String consumerAddress) {
        super(consumerName, consumerAddress);
    }

    public Commercial(String consumerId,String consumerName, String consumerAddress) {
        super(consumerId,consumerName, consumerAddress);
    }
    @Override
    public double calculateBill(MeterReading reading) {
        return reading.getConsumption() * RATE_PER_KWH;
    }
    @Override
    public  double getRatePerKwh() {
        return RATE_PER_KWH;
    }

    @Override
    public String getBillableType(){
        return "Commercial";
    }
}

