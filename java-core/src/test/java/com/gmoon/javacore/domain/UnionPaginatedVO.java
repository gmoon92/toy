package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UnionPaginatedVO extends BasePaginatedVO {

	private final List<BasePaginatedVO> ALL = new ArrayList<>();

	public UnionPaginatedVO(int requestPage, int pageSize, BasePaginatedVO... pages) {
		setPage(requestPage);
		setPageSize(pageSize);
		add(pages);
	}

	public synchronized <T extends BasePaginatedVO> void add(T... pages) {
		ALL.addAll(Arrays.asList(pages));
		resizing();
	}

	public Optional<BasePaginatedVO> find(BasePaginatedVO vo) {
		return ALL.stream()
			 .filter(vo::equals)
			 .findFirst();
	}

	public Optional<BasePaginatedVO> find(Class<? extends BasePaginatedVO> clazz) {
		return ALL.stream()
			 .filter(clazz::isInstance)
			 .findFirst();
	}

	public BasePaginatedVO get(BasePaginatedVO vo) {
		return find(vo)
			 .orElseThrow(() -> new RuntimeException("not found page object."));
	}

	public BasePaginatedVO get(Class<? extends BasePaginatedVO> clazz) {
		return find(clazz)
			 .orElseThrow(() -> new RuntimeException("not found page object."));
	}

	private void resizing() {
		long prevTotalCount = 0;

		for (int i = 0; i < ALL.size(); i++) {
			BasePaginatedVO paginatedVO = ALL.get(i);
			resizingPage(paginatedVO, prevTotalCount);

			prevTotalCount += paginatedVO.getTotalCount();
		}
	}

	private void resizingPage(BasePaginatedVO paginatedVO, long prevTotalCount) {
		final long requestPage = getPage();
		final long pageSize = getPageSize();

		final int dataPresent = 1;
		final long resizingStartPage = obtainTotalPage(prevTotalCount + dataPresent);
		final long prevLastPageSize = obtainLastPageSize(prevTotalCount);

		long zeroBasedPage = positiveNumberOrZero(requestPage - resizingStartPage);
		long adjustedPageSize = 0;
		long adjustedOffset = zeroBasedPage * pageSize;

		if (requestPage == resizingStartPage) {
			adjustedPageSize = pageSize - prevLastPageSize;
		} else if (requestPage > resizingStartPage) {
			adjustedPageSize = pageSize;
			adjustedOffset -= prevLastPageSize;
		}

		paginatedVO.setPage(toInt(zeroBasedPage) + 1);
		paginatedVO.setPageSize(toInt(adjustedPageSize));
		paginatedVO.setOffset(adjustedOffset);
	}

	@Override
	public long getTotalCount() {
		return ALL.stream()
			 .mapToLong(BasePaginatedVO::getTotalCount)
			 .sum();
	}
}
