package com.gmoon.test.fixturemonkey.orders;

/**
 * <pre>
 * 주문 접수: {@link OrderStatus#ORDERED}
 * 결제 완료: {@link OrderStatus#PAID}
 * 주문 확인: {@link OrderStatus#CONFIRMED}
 * 포장 완료: {@link OrderStatus#PICKED}
 * 배송 시작: {@link OrderStatus#SHIPPED}
 * 배송 완료: {@link OrderStatus#DELIVERED}
 * 주문 취소: {@link OrderStatus#CANCELLED}
 * </pre>
 */
public enum OrderStatus {
	ORDERED,
	PAID,
	CONFIRMED,
	PICKED,
	SHIPPED,
	DELIVERED,
	CANCELLED
}
