package com.gmoon.cacheinvalidation.core.listener;

import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;

public record EntityChangeEvent(EntityChange change) {
}
