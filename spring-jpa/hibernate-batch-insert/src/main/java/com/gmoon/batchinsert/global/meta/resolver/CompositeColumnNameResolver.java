package com.gmoon.batchinsert.global.meta.resolver;

import java.util.List;
import java.util.function.Supplier;

public class CompositeColumnNameResolver implements ColumnNameResolver {

	private final List<Supplier<ColumnNameResolver>> resolverSuppliers;

	@SafeVarargs
	public CompositeColumnNameResolver(Supplier<ColumnNameResolver>... resolverSuppliers) {
		this.resolverSuppliers = List.of(resolverSuppliers);
	}

	@Override
	public String resolve() {
		for (Supplier<ColumnNameResolver> supplier : resolverSuppliers) {
			ColumnNameResolver resolver = supplier.get();
			if (resolver.supports()) {
				return resolver.resolve();
			}
		}
		throw new IllegalStateException("No column names resolver found.");
	}

	@Override
	public boolean supports() {
		return true;
	}
}
