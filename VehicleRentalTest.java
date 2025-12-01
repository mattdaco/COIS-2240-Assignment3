import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;


public class VehicleRentalTest {

    @Test
    public void testLicensePlate_vai() {

        // ===== VALID PLATES =====
        Vehicle v1_vai = new Car("Toyota", "Camry", 2020, 4);
        Vehicle v2_vai = new Car("Honda", "Civic", 2018, 4);
        Vehicle v3_vai = new Car("Ford", "Focus", 2019, 4);

        // these should NOT throw
        assertDoesNotThrow(() -> v1_vai.setLicensePlate("AAA100"));
        assertDoesNotThrow(() -> v2_vai.setLicensePlate("ABC567"));
        assertDoesNotThrow(() -> v3_vai.setLicensePlate("ZZZ999"));

        // ===== INVALID PLATES =====
        Vehicle v4_vai = new Car("Test", "Test", 2000, 4);

        // these SHOULD throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> v4_vai.setLicensePlate(""));      // empty

        assertThrows(IllegalArgumentException.class,
                () -> v4_vai.setLicensePlate(null));    // null

        assertThrows(IllegalArgumentException.class,
                () -> v4_vai.setLicensePlate("AAA1000"));// too long

        assertThrows(IllegalArgumentException.class,
                () -> v4_vai.setLicensePlate("ZZZ99")); // too short
    }

    @Test
    public void testRentAndReturnVehicle_vai() {

        RentalSystem rs_vai = RentalSystem.getInstance();

        // Create vehicle & customer
        Vehicle v_vai = new Car("Toyota", "Camry", 2020, 4);
        v_vai.setLicensePlate("AAA100");

        Customer c_vai = new Customer(1, "Manav");

        rs_vai.addVehicle(v_vai);
        rs_vai.addCustomer(c_vai);

        // Initially available
        assertEquals(Vehicle.VehicleStatus.Available, v_vai.getStatus());

        // Rent should be successful
        boolean rentSuccess = rs_vai.rentVehicle(v_vai, c_vai, java.time.LocalDate.now(), 100.0);
        assertTrue(rentSuccess);
        assertEquals(Vehicle.VehicleStatus.Rented, v_vai.getStatus());

        // Renting again should fail
        boolean secondRentAttempt = rs_vai.rentVehicle(v_vai, c_vai, java.time.LocalDate.now(), 100.0);
        assertFalse(secondRentAttempt);

        // Return should be successful
        boolean returnSuccess = rs_vai.returnVehicle(v_vai, c_vai, java.time.LocalDate.now(), 0.0);
        assertTrue(returnSuccess);
        assertEquals(Vehicle.VehicleStatus.Available, v_vai.getStatus());

        // Returning again should fail
        boolean secondReturnAttempt = rs_vai.returnVehicle(v_vai, c_vai, java.time.LocalDate.now(), 0.0);
        assertFalse(secondReturnAttempt);
    }

    @Test
    public void testSingletonRentalSystem_vai() throws Exception {

        // get the declared constructor (should be private)
        Constructor<RentalSystem> ctor_vai =
                RentalSystem.class.getDeclaredConstructor();

        int mods_vai = ctor_vai.getModifiers();
        assertTrue(Modifier.isPrivate(mods_vai),
                "RentalSystem constructor should be private");

        // getInstance should always return the SAME object
        RentalSystem rs1_vai = RentalSystem.getInstance();
        RentalSystem rs2_vai = RentalSystem.getInstance();

        assertNotNull(rs1_vai);
        assertSame(rs1_vai, rs2_vai,
                "getInstance() should return the same singleton instance");
    }
    
    
}


