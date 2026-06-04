package utils;

public class Constants {
    public enum SpotType {
        COMPACT("Compact", 2.0),
        REGULAR("Regular", 5.0),
        HANDICAPPED("Handicapped", 2.0),
        RESERVED("Reserved", 10.0);
        
        private final String name;
        private final double hourlyRate;
        
        SpotType(String name, double hourlyRate) {
            this.name = name;
            this.hourlyRate = hourlyRate;
        }
        
        public String getName() { return name; }
        public double getHourlyRate() { return hourlyRate; }
    }
    
    public enum VehicleType {
        MOTORCYCLE("Motorcycle"),
        CAR("Car"),
        SUV_TRUCK("SUV/Truck"),
        HANDICAPPED_VEHICLE("Handicapped Vehicle");
        //BUS("Bus");
        
        private final String name;
        
        VehicleType(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public boolean canFit(SpotType spotType) {
            switch (this) {
                case CAR:
                    return spotType == SpotType.REGULAR || spotType == SpotType.HANDICAPPED;
                case MOTORCYCLE:
                    return true; // Motorcycles can fit anywhere
                case HANDICAPPED_VEHICLE:
                    return spotType == SpotType.HANDICAPPED;
                default:
                    return false;
            }
        }
    }
    
    public enum FineScheme {
        FIXED("Fixed Fine", 50.0),
        PROGRESSIVE("Progressive Fine", 0.0),
        HOURLY("Hourly Fine", 20.0);
        
        private final String name;
        private final double baseAmount;
        
        FineScheme(String name, double baseAmount) {
            this.name = name;
            this.baseAmount = baseAmount;
        }
        
        public String getName() { return name; }
        public double getBaseAmount() { return baseAmount; }
        
        @Override
        public String toString() {
            return name;
        }
    }
}