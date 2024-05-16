package com.gmoon.springjpapagination.global.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class UnionPageResizer extends Pageable {

	private static final long serialVersionUID = 7721966097602701397L;

	private final Set<ResizedPage> resizedPages = new HashSet<>();
	private final Integer page;
	private final int pageSize;

	@RequiredArgsConstructor
	@Getter
	@EqualsAndHashCode
	private static class ResizedPage implements Serializable {

		private static final long serialVersionUID = -2996504992546648328L;

		@EqualsAndHashCode.Include
		private final Class<?> clazz;
		private final Pageable pageable;
		private final boolean merged;

		private static ResizedPage unmerge(Pageable pageable) {
			Pageable initialized = pageable.initialize();
			return new ResizedPage(pageable.getClass(), initialized, false);
		}

		private static ResizedPage merge(Pageable pageable) {
			return new ResizedPage(pageable.getClass(), pageable, true);
		}

		private long getTotalCount() {
			return pageable.getTotalCount();
		}
	}

	public UnionPageResizer(int requestPage, int pageSize, List<Pageable> pageableList) {
		this.page = requestPage;
		this.pageSize = pageSize;

		long prevTotalCount = 0;
		for (Pageable pageable : pageableList) {
			ResizedPage resizedPage = obtainResizedPage(pageable, prevTotalCount);
			prevTotalCount += resizedPage.getTotalCount();
			resizedPages.add(resizedPage);
		}
	}

	private ResizedPage obtainResizedPage(Pageable pageable, long prevTotalCount) {
		final int dataPresent = 1;
		final int resizingStartPage = getTotalPage(prevTotalCount + dataPresent, pageSize);
		final int totalPageWithPrevPages = getTotalPageWithPrev(pageable, prevTotalCount);

		boolean resizing = resizingStartPage <= page && page <= totalPageWithPrevPages;
		if (resizing) {
			return resizingPage(pageable, resizingStartPage, prevTotalCount);
		}

		return ResizedPage.unmerge(pageable);
	}

	private int getTotalPageWithPrev(Pageable pageable, long prevTotalCount) {
		return pageable.getTotalPage(pageable.getTotalCount() + prevTotalCount, pageSize);
	}

	private ResizedPage resizingPage(Pageable pageable, final long resizingStartPage, long prevTotalCount) {
		final long prevLastPageSize = getLastPageSize(prevTotalCount);

		long zeroBasedPage = Math.max(page - resizingStartPage, 0);
		long adjustedPageSize = adjustPageSize(resizingStartPage, prevLastPageSize);
		long adjustedOffset = adjustOffset(zeroBasedPage, resizingStartPage, prevLastPageSize);

		pageable.initialize(zeroBasedPage + 1, adjustedPageSize, adjustedOffset);
		return ResizedPage.merge(pageable);
	}

	private long adjustOffset(long zeroBasedPage, long resizingStartPage, long prevLastPageSize) {
		if (page > resizingStartPage) {
			return zeroBasedPage * pageSize - prevLastPageSize;
		}
		return zeroBasedPage * pageSize;
	}

	private long adjustPageSize(long resizingStartPage, long prevLastPageSize) {
		if (page == resizingStartPage) {
			return pageSize - prevLastPageSize;
		} else if (page > resizingStartPage) {
			return pageSize;
		}

		return 0;
	}

	private long getLastPageSize(long totalCount) {
		long totalPage = getTotalPage(totalCount, pageSize);
		long totalPageSize = totalPage * pageSize;
		long lastPageSize = pageSize - (totalPageSize - totalCount);
		if (pageSize == lastPageSize) {
			return 0;
		}
		return lastPageSize;
	}

	public boolean isMergedData(Pageable pageable) {
		return get(pageable.getClass())
			.isMerged();
	}

	public <T extends PaginatedContent<R>, R> List<R> getContent(Class<T> clazz, Function<T, List<R>> contentProvider) {
		T BasePaginatedVO = getBasePaginatedVO(clazz);
		if (isMergedData(BasePaginatedVO)) {
			return contentProvider.apply(BasePaginatedVO);
		}

		return new ArrayList<>();
	}

	private ResizedPage get(Class<?> clazz) {
		return resizedPages.stream()
			.filter(reference -> reference.clazz.equals(clazz))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("not found page object."));
	}

	public <T extends Pageable> T getBasePaginatedVO(Class<T> clazz) {
		Pageable pageable = get(clazz).getPageable();
		return clazz.cast(pageable);
	}

	public long getTotalCount() {
		return resizedPages.stream()
			.map(ResizedPage::getPageable)
			.mapToLong(Pageable::getTotalCount)
			.sum();
	}

}

