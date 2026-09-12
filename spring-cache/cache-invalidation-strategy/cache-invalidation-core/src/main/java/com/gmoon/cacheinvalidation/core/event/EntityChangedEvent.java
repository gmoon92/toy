package com.gmoon.cacheinvalidation.core.event;

import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;

public record EntityChangedEvent(EntityChange change) {
}
