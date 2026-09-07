package josue.CloudParking.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void shouldAcquireLockWhenKeyDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq("cloudparking:idempotency:user1:checkin:ABC1D23:A1"),
                eq("1"),
                eq(Duration.ofSeconds(15))
        )).thenReturn(true);

        assertTrue(idempotencyService.acquireLock("user1", "checkin", "ABC1D23", "A1"));
    }

    @Test
    void shouldNotAcquireLockWhenKeyAlreadyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                eq("cloudparking:idempotency:user1:checkout:session1"),
                eq("1"),
                eq(Duration.ofSeconds(15))
        )).thenReturn(false);

        assertFalse(idempotencyService.acquireLock("user1", "checkout", "session1"));
    }

    @Test
    void shouldReleaseLock() {
        idempotencyService.releaseLock("user1", "checkin", "ABC1D23", "A1");

        verify(redisTemplate).delete("cloudparking:idempotency:user1:checkin:ABC1D23:A1");
    }
}
