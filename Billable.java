public interface Billable {
    double calculateBill(MeterReading reading);
    double getRatePerKwh();
    String getBillableType();
}
