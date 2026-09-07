package josue.CloudParking.application;

import josue.CloudParking.domain.ParkingSession;
import josue.CloudParking.domain.ParkingSpot;
import josue.CloudParking.domain.SpotStatus;
import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.infrastructure.config.CloudParkingProperties;
import josue.CloudParking.infrastructure.config.ParkingGridCalculator;
import josue.CloudParking.infrastructure.exception.DuplicateOperationException;
import josue.CloudParking.infrastructure.exception.ParkingNotFoundException;
import josue.CloudParking.infrastructure.exception.SessionAlreadyClosedException;
import josue.CloudParking.infrastructure.exception.SpotNotAvailableException;
import josue.CloudParking.infrastructure.persistence.ParkingSessionRepository;
import josue.CloudParking.infrastructure.persistence.ParkingSpotRepository;
import josue.CloudParking.infrastructure.service.IdempotencyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingOperationsUseCaseTest {

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private ParkingSessionRepository sessionRepository;

    @Mock
    private VehicleLookupUseCase vehicleLookupUseCase;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private ParkingGridCalculator gridCalculator;

    @Mock
    private CloudParkingProperties properties;

    @InjectMocks
    private ParkingOperationsUseCase useCase;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCheckInSuccessfully() {
        when(idempotencyService.acquireLock("anonymous", "checkin", "ABC1D23", "A1")).thenReturn(true);
        when(sessionRepository.findByLicenseAndExitDateIsNull("ABC1D23")).thenReturn(Optional.empty());

        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(spot));

        VehicleInfo vehicleInfo = new VehicleInfo("ABC1D23", "Fiat", "Uno", "Branco", 2018, "SP");
        when(vehicleLookupUseCase.lookupByPlate("ABC1D23")).thenReturn(Optional.of(vehicleInfo));
        when(spotRepository.save(spot)).thenReturn(spot);
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingSession result = useCase.checkIn("abc-1d23", "a1");

        assertEquals("ABC1D23", result.getLicense());
        assertEquals("A1", result.getSpotCode());
        assertEquals("Uno", result.getModel());
        assertTrue(result.isOpen());
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
        verify(idempotencyService, never()).releaseLock("anonymous", "checkin", "ABC1D23", "A1");
    }

    @Test
    void shouldRejectDuplicateCheckInByIdempotencyLock() {
        when(idempotencyService.acquireLock("anonymous", "checkin", "ABC1D23", "A1")).thenReturn(false);

        assertThrows(DuplicateOperationException.class, () -> useCase.checkIn("ABC1D23", "A1"));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldRejectCheckInWhenPlateAlreadyHasOpenSession() {
        ParkingSession openSession = new ParkingSession("existing", "B2", "ABC1D23", "SP", "Gol", "Prata");

        when(idempotencyService.acquireLock("anonymous", "checkin", "ABC1D23", "A1")).thenReturn(true);
        when(sessionRepository.findByLicenseAndExitDateIsNull("ABC1D23")).thenReturn(Optional.of(openSession));

        assertThrows(DuplicateOperationException.class, () -> useCase.checkIn("ABC1D23", "A1"));
        verify(idempotencyService).releaseLock("anonymous", "checkin", "ABC1D23", "A1");
    }

    @Test
    void shouldRejectCheckInWhenSpotIsInvalid() {
        when(idempotencyService.acquireLock("anonymous", "checkin", "ABC1D23", "Z9")).thenReturn(true);
        when(sessionRepository.findByLicenseAndExitDateIsNull("ABC1D23")).thenReturn(Optional.empty());
        when(spotRepository.findByCodeForUpdate("Z9")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.checkIn("ABC1D23", "Z9"));
        verify(idempotencyService).releaseLock("anonymous", "checkin", "ABC1D23", "Z9");
    }

    @Test
    void shouldRejectCheckInWhenSpotIsOccupied() {
        ParkingSpot occupiedSpot = new ParkingSpot("A1", 'A', 1);
        occupiedSpot.occupy();

        when(idempotencyService.acquireLock("anonymous", "checkin", "ABC1D23", "A1")).thenReturn(true);
        when(sessionRepository.findByLicenseAndExitDateIsNull("ABC1D23")).thenReturn(Optional.empty());
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(occupiedSpot));

        assertThrows(SpotNotAvailableException.class, () -> useCase.checkIn("ABC1D23", "A1"));
        verify(idempotencyService).releaseLock("anonymous", "checkin", "ABC1D23", "A1");
    }

    @Test
    void shouldUseDefaultVehicleInfoWhenLookupReturnsEmpty() {
        when(idempotencyService.acquireLock("anonymous", "checkin", "XYZ9K88", "A1")).thenReturn(true);
        when(sessionRepository.findByLicenseAndExitDateIsNull("XYZ9K88")).thenReturn(Optional.empty());

        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(spot));
        when(vehicleLookupUseCase.lookupByPlate("XYZ9K88")).thenReturn(Optional.empty());
        when(spotRepository.save(spot)).thenReturn(spot);
        when(sessionRepository.save(any(ParkingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingSession result = useCase.checkIn("XYZ9K88", "A1");

        assertEquals("Desconhecido", result.getModel());
        assertEquals("Indefinida", result.getColor());
    }

    @Test
    void shouldCheckOutSuccessfully() {
        ParkingSession session = new ParkingSession("session1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);
        spot.occupy();

        when(idempotencyService.acquireLock("anonymous", "checkout", "session1")).thenReturn(true);
        when(sessionRepository.findById("session1")).thenReturn(Optional.of(session));
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);
        when(sessionRepository.save(session)).thenReturn(session);

        ParkingSession result = useCase.checkOut("session1");

        assertFalse(result.isOpen());
        assertNotNull(result.getExitDate());
        assertNotNull(result.getBill());
        assertEquals(SpotStatus.FREE, spot.getStatus());
    }

    @Test
    void shouldRejectDuplicateCheckOutByIdempotencyLock() {
        when(idempotencyService.acquireLock("anonymous", "checkout", "session1")).thenReturn(false);

        assertThrows(DuplicateOperationException.class, () -> useCase.checkOut("session1"));
        verify(sessionRepository, never()).findById("session1");
    }

    @Test
    void shouldRejectCheckOutWhenSessionNotFound() {
        when(idempotencyService.acquireLock("anonymous", "checkout", "missing")).thenReturn(true);
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ParkingNotFoundException.class, () -> useCase.checkOut("missing"));
        verify(idempotencyService).releaseLock("anonymous", "checkout", "missing");
    }

    @Test
    void shouldRejectCheckOutWhenSessionAlreadyClosed() {
        ParkingSession closedSession = new ParkingSession("session1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        closedSession.close(java.time.LocalDateTime.now(), 5.0);

        when(idempotencyService.acquireLock("anonymous", "checkout", "session1")).thenReturn(true);
        when(sessionRepository.findById("session1")).thenReturn(Optional.of(closedSession));

        assertThrows(SessionAlreadyClosedException.class, () -> useCase.checkOut("session1"));
        verify(idempotencyService).releaseLock("anonymous", "checkout", "session1");
    }

    @Test
    void shouldCheckOutByPlate() {
        ParkingSession session = new ParkingSession("session1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);
        spot.occupy();

        when(sessionRepository.findByLicenseAndExitDateIsNull("ABC1D23")).thenReturn(Optional.of(session));
        when(idempotencyService.acquireLock("anonymous", "checkout", "session1")).thenReturn(true);
        when(sessionRepository.findById("session1")).thenReturn(Optional.of(session));
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);
        when(sessionRepository.save(session)).thenReturn(session);

        ParkingSession result = useCase.checkOutByPlateOrSpot("abc-1d23", null);

        assertFalse(result.isOpen());
        assertEquals(SpotStatus.FREE, spot.getStatus());
    }

    @Test
    void shouldCheckOutBySpotCode() {
        ParkingSession session = new ParkingSession("session1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        ParkingSpot spot = new ParkingSpot("A1", 'A', 1);
        spot.occupy();

        when(sessionRepository.findBySpotCodeAndExitDateIsNull("A1")).thenReturn(Optional.of(session));
        when(idempotencyService.acquireLock("anonymous", "checkout", "session1")).thenReturn(true);
        when(sessionRepository.findById("session1")).thenReturn(Optional.of(session));
        when(spotRepository.findByCodeForUpdate("A1")).thenReturn(Optional.of(spot));
        when(spotRepository.save(spot)).thenReturn(spot);
        when(sessionRepository.save(session)).thenReturn(session);

        ParkingSession result = useCase.checkOutByPlateOrSpot(null, "a1");

        assertFalse(result.isOpen());
    }

    @Test
    void shouldRejectCheckOutByPlateOrSpotWithoutParameters() {
        assertThrows(IllegalArgumentException.class, () -> useCase.checkOutByPlateOrSpot(null, null));
        assertThrows(IllegalArgumentException.class, () -> useCase.checkOutByPlateOrSpot("  ", "  "));
    }

    @Test
    void shouldFindSessionById() {
        ParkingSession session = new ParkingSession("session1", "A1", "ABC1D23", "SP", "Uno", "Branco");
        when(sessionRepository.findById("session1")).thenReturn(Optional.of(session));

        ParkingSession result = useCase.findSessionById("session1");

        assertEquals("session1", result.getId());
    }

    @Test
    void shouldThrowWhenSessionNotFoundById() {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ParkingNotFoundException.class, () -> useCase.findSessionById("missing"));
    }

    @Test
    void shouldListAvailableSpots() {
        ParkingSpot freeSpot = new ParkingSpot("A1", 'A', 1);
        when(spotRepository.findByStatus(SpotStatus.FREE)).thenReturn(List.of(freeSpot));

        List<ParkingSpot> result = useCase.listAvailableSpots();

        assertEquals(1, result.size());
        assertEquals("A1", result.getFirst().getCode());
    }

    @Test
    void shouldResolveGridDimensions() {
        var grid = new CloudParkingProperties.GridProperties(20, null, null);
        var expected = new ParkingGridCalculator.GridDimensions(4, 5);

        when(properties.grid()).thenReturn(grid);
        when(gridCalculator.resolve(grid)).thenReturn(expected);

        var result = useCase.getGridDimensions();

        assertEquals(4, result.rows());
        assertEquals(5, result.columns());
    }
}
