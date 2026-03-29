package cloud.larn.bump.domain.exception

import cloud.larn.bump.domain.model.IdempotencyKey

class DuplicateIdempotencyKeyException(key: IdempotencyKey) :
    RuntimeException("Usage event with idempotency key '${key.value}' has already been recorded")
