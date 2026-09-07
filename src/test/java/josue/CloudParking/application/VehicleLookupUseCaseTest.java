package josue.CloudParking.application;

import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.domain.port.VehicleLookupPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleLookupUseCaseTest {

    @Mock
    private VehicleLookupPort vehicleLookupPort;

    @InjectMocks
    private VehicleLookupUseCase useCase;

    @Test
    void shouldNormalizePlateBeforeLookup() {
        VehicleInfo vehicleInfo = new VehicleInfo("ABC1D23", "Fiat", "Uno", "Branco", 2018, "SP");
        when(vehicleLookupPort.lookupByPlate("ABC1D23")).thenReturn(Optional.of(vehicleInfo));

        Optional<VehicleInfo> result = useCase.lookupByPlate("abc-1d23");

        assertTrue(result.isPresent());
        assertEquals("ABC1D23", result.get().plate());
        verify(vehicleLookupPort).lookupByPlate("ABC1D23");
    }

    @Test
    void shouldReturnEmptyWhenPortReturnsEmpty() {
        when(vehicleLookupPort.lookupByPlate("XYZ9K88")).thenReturn(Optional.empty());

        Optional<VehicleInfo> result = useCase.lookupByPlate("XYZ9K88");

        assertTrue(result.isEmpty());
    }
}
