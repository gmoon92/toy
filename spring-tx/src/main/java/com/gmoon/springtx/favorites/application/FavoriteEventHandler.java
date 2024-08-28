package com.gmoon.springtx.favorites.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.springtx.global.event.DeleteFavoriteEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class FavoriteEventHandler {

	private final FavoriteService favoriteService;

	@TransactionalEventListener(
		 phase = TransactionPhase.AFTER_COMMIT,
		 fallbackExecution = true
	)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void delete(DeleteFavoriteEvent deleteFavoriteEvent) {
		String userId = deleteFavoriteEvent.getUserId();
		log.info("userId: {}", userId);
		favoriteService.delete(userId);
	}
}
