package com.gmoon.javacore.test.domain;

import com.gmoon.javacore.persistence.Embeddable;
import com.gmoon.javacore.persistence.EmbeddedId;
import com.gmoon.javacore.persistence.Entity;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Favorites {

	@EmbeddedId
	@EqualsAndHashCode.Include
	private Id id;

	@Embeddable
	@RequiredArgsConstructor
	public static class Id {

		private final UUID userId;
		private final FavoriteType favoriteType;
	}
}
