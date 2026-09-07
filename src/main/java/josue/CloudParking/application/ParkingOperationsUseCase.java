package josue.CloudParking.application;

import josue.CloudParking.domain.ParkingSession;
import josue.CloudParking.domain.ParkingSpot;
import josue.CloudParking.domain.SpotStatus;
import josue.CloudParking.domain.VehicleInfo;
import josue.CloudParking.domain.service.ParkingCheckOut;
import josue.CloudParking.infrastructure.config.CloudParkingProperties;
import josue.CloudParking.infrastructure.config.ParkingGridCalculator;
import josue.CloudParking.infrastructure.exception.DuplicateOperationException;
import josue.CloudParking.infrastructure.exception.ParkingNotFoundException;
import josue.CloudParking.infrastructure.exception.SessionAlreadyClosedException;
import josue.CloudParking.infrastructure.exception.SpotNotAvailableException;
import josue.CloudParking.infrastructure.persistence.ParkingSessionRepository;
import josue.CloudParking.infrastructure.persistence.ParkingSpotRepository;
import josue.CloudParking.infrastructure.service.IdempotencyService;
import josue.CloudParking.infrastructure.util.PlateNormalizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ParkingOperationsUseCase {

    private final ParkingSpotRepository spotRepository;
    private final ParkingSessionRepository sessionRepository;
    private final VehicleLookupUseCase vehicleLookupUseCase;
    private final IdempotencyService idempotencyService;
    private final ParkingGridCalculator gridCalculator;
    private final CloudParkingProperties properties;

    public ParkingOperationsUseCase(ParkingSpotRepository spotRepository,
                                    ParkingSessionRepository sessionRepository,
                                    VehicleLookupUseCase vehicleLookupUseCase,
                                    IdempotencyService idempotencyService,
                                    ParkingGridCalculator gridCalculator,
                                    CloudParkingProperties properties) {
        this.spotRepository = spotRepository;
        this.sessionRepository = sessionRepository;
        this.vehicleLookupUseCase = vehicleLookupUseCase;
        this.idempotencyService = idempotencyService;
        this.gridCalculator = gridCalculator;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> listAllSpots() {
        return spotRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> listAvailableSpots() {
        return spotRepository.findByStatus(SpotStatus.FREE);
    }

    @Transactional(readOnly = true)
    public List<ParkingSession> listOpenSessions() {
        return sessionRepository.findByExitDateIsNull();
    }

    @Transactional(readOnly = true)
    public ParkingSession findSessionById(String id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ParkingNotFoundException(id));
    }

    @Transactional
    public ParkingSession checkIn(String plate, String spotCode) {
        String userId = resolveUserId();
        String normalizedPlate = PlateNormalizer.normalize(plate);
        String normalizedSpot = spotCode.trim().toUpperCase();

        if (!idempotencyService.acquireLock(userId, "checkin", normalizedPlate, normalizedSpot)) {
            throw new DuplicateOperationException("Entrada já processada recentemente para esta placa e vaga");
        }

        try {
            sessionRepository.findByLicenseAndExitDateIsNull(normalizedPlate)
                    .ifPresent(s -> {
                        throw new DuplicateOperationException("Placa " + normalizedPlate + " já possui sessão aberta");
                    });

            ParkingSpot spot = spotRepository.findByCodeForUpdate(normalizedSpot)
                    .orElseThrow(() -> new IllegalArgumentException("Vaga inválida: " + normalizedSpot));

            if (!spot.isFree()) {
                throw new SpotNotAvailableException(normalizedSpot);
            }

            VehicleInfo vehicleInfo = vehicleLookupUseCase.lookupByPlate(normalizedPlate)
                    .orElse(new VehicleInfo(normalizedPlate, "Desconhecida", "Desconhecido", "Indefinida", null, "SP"));

            String sessionId = UUID.randomUUID().toString().replace("-", "");
            ParkingSession session = new ParkingSession(
                    sessionId,
                    normalizedSpot,
                    vehicleInfo.plate(),
                    vehicleInfo.state(),
                    vehicleInfo.model(),
                    vehicleInfo.color()
            );

            spot.occupy();
            spotRepository.save(spot);
            return sessionRepository.save(session);
        } catch (RuntimeException ex) {
            idempotencyService.releaseLock(userId, "checkin", normalizedPlate, normalizedSpot);
            throw ex;
        }
    }

    @Transactional
    public ParkingSession checkOut(String sessionId) {
        String userId = resolveUserId();

        if (!idempotencyService.acquireLock(userId, "checkout", sessionId)) {
            throw new DuplicateOperationException("Saída já processada recentemente para esta sessão");
        }

        try {
            ParkingSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ParkingNotFoundException(sessionId));

            if (!session.isOpen()) {
                throw new SessionAlreadyClosedException(sessionId);
            }

            return finalizeCheckOut(session);
        } catch (RuntimeException ex) {
            idempotencyService.releaseLock(userId, "checkout", sessionId);
            throw ex;
        }
    }

    @Transactional
    public ParkingSession checkOutByPlateOrSpot(String plate, String spotCode) {
        ParkingSession session;

        if (plate != null && !plate.isBlank()) {
            String normalizedPlate = PlateNormalizer.normalize(plate);
            session = sessionRepository.findByLicenseAndExitDateIsNull(normalizedPlate)
                    .orElseThrow(() -> new ParkingNotFoundException(normalizedPlate));
        } else if (spotCode != null && !spotCode.isBlank()) {
            String normalizedSpot = spotCode.trim().toUpperCase();
            session = sessionRepository.findBySpotCodeAndExitDateIsNull(normalizedSpot)
                    .orElseThrow(() -> new ParkingNotFoundException(normalizedSpot));
        } else {
            throw new IllegalArgumentException("Informe placa ou código da vaga");
        }

        return checkOut(session.getId());
    }

    public ParkingGridCalculator.GridDimensions getGridDimensions() {
        return gridCalculator.resolve(properties.grid());
    }

    private ParkingSession finalizeCheckOut(ParkingSession session) {
        LocalDateTime exitDate = LocalDateTime.now();
        session.close(exitDate, ParkingCheckOut.calculateBill(session.getEntryDate(), exitDate));

        ParkingSpot spot = spotRepository.findByCodeForUpdate(session.getSpotCode())
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada: " + session.getSpotCode()));
        spot.release();
        spotRepository.save(spot);

        return sessionRepository.save(session);
    }

    private String resolveUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }
}
