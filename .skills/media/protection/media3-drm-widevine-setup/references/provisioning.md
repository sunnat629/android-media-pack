# Provisioning retry policy

Provisioning is the first-time device handshake with the Widevine backend. It can fail transiently (network, server load) or hard (device in a revocation list).

## Classification

- **Transient**: network error, 5xx from provisioning server. Retry with exponential backoff.
- **Hard**: revoked device key, factory-reset required. **DO NOT** retry. Surface a clear message and disable Widevine playback.

Media3 surfaces provisioning failures as `PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED`.

## Backoff

- Start at 1 second.
- Double on each failure, up to 30 seconds.
- Add jitter of +/- 20% to avoid synchronized retry storms.
- Cap total attempts at 5 per user-visible playback attempt.

```kotlin
class ProvisioningBackoff {
    private var attempt = 0

    fun nextDelayMs(): Long? {
        if (attempt >= 5) return null
        val base = minOf(1_000L shl attempt, 30_000L)
        attempt++
        val jitter = (base * 0.2).toLong()
        return base + ((-jitter)..jitter).random()
    }

    fun reset() { attempt = 0 }
}
```

## Rules

- **MUST NOT** retry provisioning in a tight loop. The license server can blacklist the device.
- **MUST** surface a user-visible error after the retry cap is hit.
- **MUST** reset the counter on a successful provisioning.
- **DO NOT** retry after `ERROR_CODE_DRM_SYSTEM_ERROR` without telemetry. That error usually means the device MediaDrm stack is wedged and needs a process restart.
