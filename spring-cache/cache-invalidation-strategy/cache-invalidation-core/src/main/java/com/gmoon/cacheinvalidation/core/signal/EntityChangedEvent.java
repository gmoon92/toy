package com.gmoon.cacheinvalidation.core.signal;

import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;

public record EntityChangedEvent(EntityChange change) {
}
