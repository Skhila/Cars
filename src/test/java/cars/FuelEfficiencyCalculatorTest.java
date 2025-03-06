package cars;

import com.example.cars.components.FuelEfficiencyCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FuelEfficiencyCalculatorTest {

    private final FuelEfficiencyCalculator fuelEfficiencyCalculator = new FuelEfficiencyCalculator();

    @Test
    void shouldCalculateFuelEfficiencyCorrectly() {
        double horsePower = 400;
        double capacity = 4.4;
        double weightKg = 1800;

        double expectedResult = 0.8;
        double actualResult = fuelEfficiencyCalculator.calculateFuelEfficiency(horsePower, capacity, weightKg);

        Assertions.assertEquals(expectedResult, actualResult);
    }
}
