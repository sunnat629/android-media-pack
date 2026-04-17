# Manifest and stream error matrix

`PlaybackException.errorCode` is the routing key. Branch by code, never by message text.

| Code | Typical cause | Recovery |
| --- | --- | --- |
| `ERROR_CODE_PARSING_MANIFEST_MALFORMED` | Broken HLS or DASH manifest | Report to origin. Do not retry silently. |
| `ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED` | New manifest feature | Log and surface a clear error. |
| `ERROR_CODE_IO_NETWORK_CONNECTION_FAILED` | Transient network | Exponential backoff, capped retries. |
| `ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT` | Slow network | Same as above. |
| `ERROR_CODE_IO_BAD_HTTP_STATUS` | 4xx / 5xx from origin | Inspect `HttpDataSourceException`. Do not retry 4xx. |
| `ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE` | Wrong MIME type at origin | Surface an origin-side issue. |
| `ERROR_CODE_BEHIND_LIVE_WINDOW` | Player fell behind the live edge | `player.seekToDefaultPosition()` to snap back. |
| `ERROR_CODE_IO_NO_PERMISSION` | Auth expired or geo-blocked | Refresh credentials or show geo-block UI. |

## Handler

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED ->
                reportBadManifest(error)

            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                retryWithBackoff()

            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW ->
                player.seekToDefaultPosition()

            PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
                refreshCredentialsOrShowGeoBlock()

            else ->
                analytics.logPlayerError(error)
        }
    }
})
```

## Rules

- **MUST** branch on `errorCode`. Message text is not stable.
- **MUST NOT** auto-retry 4xx. It burns origin quota and masks auth issues.
- **MUST** cap retries. Infinite retry is never correct.
- **PREFERRED** is a single top-level error handler, not one per screen.
