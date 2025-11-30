import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class RentalSystem {
	
	private static RentalSystem instance;
	
	private RentalSystem() {
		loadData();
	}
	
	public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }
	
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();

    public boolean addVehicle(Vehicle vehicle) {
        Vehicle existing = findVehicleByPlate(vehicle.getLicensePlate());
        if (existing != null) {
            System.out.println("A vehicle with license plate " + vehicle.getLicensePlate() + " already exists. Vehicle not added.");
            return false;
        }

        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        Customer existing = findCustomerById(customer.getCustomerId());
        if (existing != null) {
            System.out.println("A customer with ID " + customer.getCustomerId() + " already exists. Customer not added.");
            return false;
        }

        customers.add(customer);
        saveCustomer(customer);
        return true;
    }
    
    private void loadData() {
        loadVehicles();
        loadCustomers();
        loadRecords();
    }
    
    private void loadVehicles() {
        try (BufferedReader reader = new BufferedReader(new FileReader("vehicles.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length != 5) {
                    continue;}

                String type  = parts[0];
                String plate = parts[1];
                String make  = parts[2];
                String model = parts[3];
                int year;

                try {
                    year = Integer.parseInt(parts[4]);
                } catch (NumberFormatException e) {
                    continue;}

                Vehicle vehicle = null;

                if ("Car".equalsIgnoreCase(type)) {
                    int seats = 4;
                    vehicle = new Car(make, model, year, seats);
                } else if ("Minibus".equalsIgnoreCase(type)) {
                    boolean isAccessible = false;
                    vehicle = new Minibus(make, model, year, isAccessible);
                } else if ("PickupTruck".equalsIgnoreCase(type) || "Pickup Truck".equalsIgnoreCase(type)) {
                    double cargoSize = 1000.0;
                    boolean hasTrailer = false;
                    vehicle = new PickupTruck(make, model, year, cargoSize, hasTrailer);
                }

                if (vehicle != null) {
                    vehicle.setLicensePlate(plate);
                    vehicles.add(vehicle);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error loading vehicles: " + e.getMessage());
        }
    }
    
    private void loadCustomers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length != 2) {
                    continue;
                }

                int id;
                try {
                    id = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    continue;
                }

                String name = parts[1];

                Customer customer = new Customer(id, name);
                customers.add(customer);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }
    
    private void loadRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader("rental_records.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length != 5) {
                    continue;
                }

                String recordType = parts[0];
                String plate = parts[1];

                int customerId;
                try {
                    customerId = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    continue;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(parts[3]);
                } catch (Exception e) {
                    continue;
                }

                double amount;
                try {
                    amount = Double.parseDouble(parts[4]);
                } catch (NumberFormatException e) {
                    continue;
                }

                Vehicle vehicle = findVehicleByPlate(plate);
                Customer customer = findCustomerById(customerId);

                if (vehicle != null && customer != null) {
                    RentalRecord record = new RentalRecord(vehicle, customer, date, amount, recordType);
                    rentalHistory.addRecord(record);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            System.out.println("Error loading rental records: " + e.getMessage());
        }
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Available) {
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Rented) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    

    public void displayVehicles(Vehicle.VehicleStatus status) {
        // Display appropriate title based on status
        if (status == null) {
            System.out.println("\n=== All Vehicles ===");
        } else {
            System.out.println("\n=== " + status + " Vehicles ===");
        }
        
        // Header with proper column widths
        System.out.printf("|%-16s | %-12s | %-12s | %-12s | %-6s | %-18s |%n"," Type", "Plate", "Make", "Model", "Year", "Status");
        System.out.println("|--------------------------------------------------------------------------------------------|");
    	  
        boolean found = false;
        for (Vehicle vehicle : vehicles) {
            if (status == null || vehicle.getStatus() == status) {
                found = true;
                String vehicleType;
                if (vehicle instanceof Car) {
                    vehicleType = "Car";
                } else if (vehicle instanceof Minibus) {
                    vehicleType = "Minibus";
                } else if (vehicle instanceof PickupTruck) {
                    vehicleType = "Pickup Truck";
                } else {
                    vehicleType = "Unknown";
                }
                System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-6d | %-18s |%n", 
                    vehicleType, vehicle.getLicensePlate(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus().toString());
            }
        }
        if (!found) {
            if (status == null) {
                System.out.println("  No Vehicles found.");
            } else {
                System.out.println("  No vehicles with Status: " + status);
            }
        }
        System.out.println();
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        if (rentalHistory.getRentalHistory().isEmpty()) {
            System.out.println("  No rental history found.");
        } else {
            // Header with proper column widths
            System.out.printf("|%-10s | %-12s | %-20s | %-12s | %-12s |%n"," Type", "Plate", "Customer", "Date", "Amount");
            System.out.println("|-------------------------------------------------------------------------------|");
            
            for (RentalRecord record : rentalHistory.getRentalHistory()) {                
                System.out.printf("| %-9s | %-12s | %-20s | %-12s | $%-11.2f |%n", 
                    record.getRecordType(), 
                    record.getVehicle().getLicensePlate(),
                    record.getCustomer().getCustomerName(),
                    record.getRecordDate().toString(),
                    record.getTotalAmount()
                );
            }
            System.out.println();
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }
    
    public void saveVehicle(Vehicle vehicle) {
        String line = vehicle.getClass().getSimpleName() + "," +
                      vehicle.getLicensePlate() + "," +
                      vehicle.getMake() + "," +
                      vehicle.getModel() + "," +
                      vehicle.getYear();

        try (FileWriter fileWriter = new FileWriter("vehicles.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {

            out.println(line);
        } catch (IOException e) {
            System.out.println("Error saving vehicle: " + e.getMessage());
        }
    }
    
    public void saveCustomer(Customer customer) {
        String line = customer.getCustomerId() + "," +
                      customer.getCustomerName();

        try (FileWriter fileWriter = new FileWriter("customers.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {

            out.println(line);
        } catch (IOException e) {
            System.out.println("Error saving customer: " + e.getMessage());
        }
    }
    
    public void saveRecord(RentalRecord record) {
        String line = record.getRecordType() + "," +
                      record.getVehicle().getLicensePlate() + "," +
                      record.getCustomer().getCustomerId() + "," +
                      record.getRecordDate().toString() + "," +
                      record.getTotalAmount();

        try (FileWriter fileWriter = new FileWriter("rental_records.txt", true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {

            out.println(line);
        } catch (IOException e) {
            System.out.println("Error saving rental record: " + e.getMessage());
        }
    }
}