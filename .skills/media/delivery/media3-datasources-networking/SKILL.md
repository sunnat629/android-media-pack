---
name: media3-datasources-networking
description: "Compact skill for Media3 HTTP DataSource, OkHttp/Cronet/HttpEngine, cache, headers, auth, and custom DataSource decisions."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - datasource
    - networking
    - okhttp
    - cronet
    - cache
    - headers
    - offline
---

## Trigger

Use for networking stack, cache, auth headers, retries, timeouts, custom URI handling, or offline-first media loading.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- **PREFERRED** stack order per official guidance: `HttpEngineDataSource` (API 34+) first, then `CronetDataSource` via Play Services Cronet, then `DefaultHttpDataSource` as fallback only. Use OkHttp when the app already standardizes on it.
- `DefaultHttpDataSource` is the fallback, not the recommended default.
- Custom DataSource only for measured auth, transform, retry, cache-key, or non-HTTP needs.
- Centralize User-Agent, headers, timeout, and cache policy.
- Do not let UI build media URLs directly.

## Example

```kotlin
// Fallback path only, when HttpEngine (API 34+) and Cronet are unavailable.
val fallbackFactory = DefaultHttpDataSource.Factory()
    .setUserAgent(appUserAgent)
    .setAllowCrossProtocolRedirects(true)
```

## Related

- `media3-bandwidth-abr`
- `media3-hls-dash-adaptive-streaming`
- `streaming-media-architecture`
