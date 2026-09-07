package josue.CloudParking.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlateNormalizerTest {

    @Test
    void shouldNormalizePlateRemovingHyphenAndSpaces() {
        assertEquals("ABC1D23", PlateNormalizer.normalize("abc-1d23"));
        assertEquals("ABC1D23", PlateNormalizer.normalize(" ABC 1D23 "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldRejectInvalidPlate(String plate) {
        assertThrows(IllegalArgumentException.class, () -> PlateNormalizer.normalize(plate));
    }
}
