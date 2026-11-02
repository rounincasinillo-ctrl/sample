public class PublicFacility implements Billable {
    private String facilityId;
    private String facilityName;
    private String agency;
    private static final double RATE_PER_KWH = 10.00;
    private static final double GOVERNMENT_DISCOUNT = 0.10;
    private static int facilityCount = 0;

    public PublicFacility(){
        this("Unknown Facility Name", "Unknown Agency");
    }

    public PublicFacility(String facilityName) {
        this(facilityName, "Unknown Agency");

    }

    public PublicFacility(String facilityName, String agency){
        this(String.format("PF-%04d", facilityCount + 1), facilityName, agency);
    }

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
        if (facilityName == null || facilityName.isBlank()) {
            throw new IllegalArgumentException("Facility name cannot be blank.");
        }
        String name = facilityName.strip();
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Facility name must be 2 to 50 characters.");
        }
        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (!Character.isDigit(c) && c != ' ' && c != '.' && c != ',' && c != '-' && c != '\'' && c != '&' && c != '(' && c != ')' && c != '/') {
                throw new IllegalArgumentException("Facility name contains invalid characters.");
            }
        }
        if (!hasLetter) {
            throw new IllegalArgumentException("Facility name must contain at least one letter.");
        }
        this.facilityName = name;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        if (agency == null || agency.isBlank()) {
            throw new IllegalArgumentException("Agency cannot be blank.");
        }
        String agencyName = agency.strip();
        if (agencyName.length() < 2 || agencyName.length() > 50) {
            throw new IllegalArgumentException("Agency must be 2 to 50 characters.");
        }
        boolean hasLetter = false;
        for (int i = 0; i < agencyName.length(); i++) {
            char c = agencyName.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (!Character.isDigit(c) && c != ' ' && c != '.' && c != ',' && c != '-' && c != '\'' && c != '&' && c != '(' && c != ')' && c != '/') {
                throw new IllegalArgumentException("Agency contains invalid characters.");
            }
        }
        if (!hasLetter) {
            throw new IllegalArgumentException("Agency must contain at least one letter.");
        }
        this.agency = agencyName;
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
        if (reading == null) {
            throw new IllegalArgumentException("Meter reading cannot be null.");
        }
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
