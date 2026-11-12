package com.gmoon.writeinvalidate.config;

import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

/**
 * 하이버네이트의 저장·수정·삭제 이벤트 중 타입 인자 {@code T} 인 엔티티만 {@link #onEntityChanged(Object)} 로 넘긴다.
 * <p>
 * 세 이벤트 모두 flush 시점, 즉 커밋 전에 온다. 벌크 연산({@code @Modifying} JPQL, 네이티브 UPDATE)은
 * 영속성 컨텍스트를 거치지 않으므로 이벤트를 만들지 않는다.
 *
 * @param <T> 넘겨받을 엔티티 타입. 하위 클래스가 구체 타입으로 선언해야 한다
 */
public abstract class JpaEntityEventListener<T>
	 implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

	private final Class<T> supportedType;

	/**
	 * 하위 클래스가 선언한 타입 인자를 읽는다. 하위 클래스도 제네릭이면 읽을 타입이 없어 생성을 막는다.
	 */
	@SuppressWarnings("unchecked")
	protected JpaEntityEventListener() {
		Class<?> declared = GenericTypeResolver.resolveTypeArgument(getClass(), JpaEntityEventListener.class);
		Assert.state(declared != null, () -> getClass().getName() + " must declare the supported entity type");
		this.supportedType = (Class<T>)declared;
	}

	@Override
	public final void onPostInsert(PostInsertEvent event) {
		onEntityChanged(event.getEntity(), supportedType);
	}

	@Override
	public final void onPostUpdate(PostUpdateEvent event) {
		onEntityChanged(event.getEntity(), supportedType);
	}

	@Override
	public final void onPostDelete(PostDeleteEvent event) {
		onEntityChanged(event.getEntity(), supportedType);
	}

	@Override
	public boolean requiresPostCommitHandling(EntityPersister persister) {
		return false;
	}

	private void onEntityChanged(Object entity, Class<T> supportedType) {
		if (supportedType.isInstance(entity)) {
			onEntityChanged(supportedType.cast(entity));
		}
	}

	protected abstract void onEntityChanged(T entity);
}
