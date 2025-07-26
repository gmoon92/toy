package com.gmoon.springpoi.excel.processor;

import com.gmoon.springpoi.excel.vo.ExcelSheet;

public class CompositeEventListener implements EventListener {
	private final EventListener[] listeners;

	public CompositeEventListener(EventListener... listener) {
		this.listeners = listener;
	}

	@Override
	public void onEvent(ExcelSheet<?> sheet) {
		for (EventListener listener : listeners) {
			listener.onEvent(sheet);
		}
	}
}
