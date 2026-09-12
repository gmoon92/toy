package com.gmoon.cacheinvalidation.core.invalidation;

public interface CommitSignalSink {

	void accept(EntityChange change, InvalidationSource source);
}
