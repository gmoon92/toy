package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.invalidation.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.event.EntityChange;

public interface CacheInvalidator {

	void invalidate(EntityChange change, ChangeSource source);
}
