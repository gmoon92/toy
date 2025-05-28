package com.gmoon.batchinsert.global.meta.resolver;

/**
 * TODO: 컬럼명(column name)에 직접적 영향 주는 JPA/Hibernate 어노테이션/클래스
 * <ul>
 *   <li>{@link jakarta.persistence.Column}</li>
 *   <li>{@link jakarta.persistence.JoinColumn}</li>
 *   <li>{@link jakarta.persistence.JoinTable}</li>
 *   <li>{@link jakarta.persistence.AttributeOverride}</li>
 *   <li>{@link jakarta.persistence.AssociationOverride}</li>
 *   <li>{@link jakarta.persistence.MapKeyColumn}</li>
 *   <li>{@link jakarta.persistence.OrderColumn}</li>
 *   <li>{@link jakarta.persistence.DiscriminatorColumn}</li>
 *   <li>{@link jakarta.persistence.PrimaryKeyJoinColumn}</li>
 *   <li>{@link jakarta.persistence.SecondaryTable}</li>
 *   <li>{@link jakarta.persistence.Embedded}</li>
 *   <li>{@link jakarta.persistence.Embeddable}</li>
 *   <!-- (옵션) Hibernate 확장 -->
 *   <li>{@link org.hibernate.annotations.ColumnTransformer}</li>
 * </ul>
 */
public interface ColumnNameResolver {
	boolean supports();

	String resolve();
}
