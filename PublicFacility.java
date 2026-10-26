public class PublicFacility implements Billable {
    private String facilityId;
    private String facilityName;
    private String agency;
    private static final double RATE_PER_KWH = 10.00;
    private static final double GOVERNMENT_DISCOUNT = 0.10;
    private static int facilityCount = 0;


    // Overloaded constructor 1: ID auto-generated (PF-0001, PF-0002, ...)
    public PublicFacility(String facilityName, String agency) {
        this(String.format("PF-%04d", facilityCount + 1 ),facilityName, agency);
    }

    // Overloaded constructor 2: ID supplied (for loading saved records)
    public PublicFacility(String facilityId, String facilityName, String agency) {
        setFacilityId(facilityId);
        setFacilityName(facilityName);
        setAgency(agency);
        facilityCount++;
    }

    public String getFacilityId() {
        return facilityId;
    }


    public void setFacilityId(String facilityId) {
        if (facilityId == null || !facilityId.strip().matches("PF-\\d{4,}")) {
            throw new IllegalArgumentException("Facility ID must follow the format PF-0001.");
        }
        this.facilityId = facilityId.strip();
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        if (facilityName == null || facilityName.strip().length() < 2 || facilityName.strip().length() > 50) {
            throw new IllegalArgumentException("Facility name must be 2 to 50 characters.");
        }
        this.facilityName = facilityName.strip();
    }


    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        if (agency == null || agency.strip().length() < 2 || agency.strip().length() > 50) {
            throw new IllegalArgumentException("Agency must be 2 to 50 characters.");
        }
        this.agency = agency.strip();
    }
    public static int getFacilityCount() {
        return facilityCount;
    }

    public static void setFacilityCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Facility count cannot be negative.");
        }
        facilityCount = count;
    }
    @Override
    public double getRatePerKwh() {
        return RATE_PER_KWH;
    }

    public double getGovernmentDiscount() {
        return GOVERNMENT_DISCOUNT;
    }

    @Override
    public double calculateBill(MeterReading reading) {
        double base = reading.getConsumption() * RATE_PER_KWH;
        return base - (base * GOVERNMENT_DISCOUNT);
    }

    @Override
    public String getBillableType() {
        return "Public Facility";
    }

    @Override
    public String toString() {
        return "Facility ID: " + getFacilityId()
                + " | Name: " + getFacilityName()
                + " | Agency: " + getAgency()
                + String.format("%nFacility Type: %s%nRate per kWh: ₱%.2f%nGovernment Discount: %.0f%%",
                getBillableType(), getRatePerKwh(), getGovernmentDiscount() * 100);
    }
}