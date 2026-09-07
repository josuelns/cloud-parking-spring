package josue.CloudParking.infrastructure.util;

public final class PlateNormalizer {

    private PlateNormalizer() {
    }

    public static String normalize(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("Placa inválida");
        }
        return plate.replace("-", "").replace(" ", "").toUpperCase();
    }
}
