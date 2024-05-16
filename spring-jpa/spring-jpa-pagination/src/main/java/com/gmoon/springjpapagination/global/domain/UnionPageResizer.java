package com.gmoon.springjpapagination.global.domain;

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
public class UnionPageResizer extends BasePaginatedVO {

	private final Set<ResizedPage> resizedPages = new HashSet<>();
	private final Integer page;
	private final int pageSize;

	@RequiredArgsConstructor
	@Getter
	@EqualsAndHashCode
	private static class ResizedPage {

		private static final long serialVersionUID = -5376487084890406173L;

		@EqualsAndHashCode.Include
		private final Class<?> clazz;
		private final BasePaginatedVO BasePaginatedVO;
		private final boolean merged;

		private static ResizedPage unmerge(BasePaginatedVO BasePaginatedVO) {
			BasePaginatedVO initialized = BasePaginatedVO.initialize();
			return new ResizedPage(BasePaginatedVO.getClass(), initialized, false);
		}

		private static ResizedPage merge(BasePaginatedVO BasePaginatedVO) {
			return new ResizedPage(BasePaginatedVO.getClass(), BasePaginatedVO, true);
		}

		public long getTotalCount() {
			return BasePaginatedVO.getTotalCount();
		}
	}

	public UnionPageResizer(int requestPage, int pageSize, List<BasePaginatedVO> BasePaginatedVOList) {
		this.page = requestPage;
		this.pageSize = pageSize;

		long prevTotalCount = 0;
		for (BasePaginatedVO BasePaginatedVO : BasePaginatedVOList) {
			ResizedPage resizedPage = obtainResizedPage(BasePaginatedVO, prevTotalCount);
			prevTotalCount += resizedPage.getTotalCount();
			resizedPages.add(resizedPage);
		}
	}

	private ResizedPage obtainResizedPage(BasePaginatedVO BasePaginatedVO, long prevTotalCount) {
		final int dataPresent = 1;
		final int resizingStartPage = getTotalPage(prevTotalCount + dataPresent, pageSize);
		final int totalPageWithPrevPages = getTotalPageWithPrev(BasePaginatedVO, prevTotalCount);

		boolean resizing = resizingStartPage <= page && page <= totalPageWithPrevPages;
		if (resizing) {
			return resizingPage(BasePaginatedVO, resizingStartPage, prevTotalCount);
		}

		return ResizedPage.unmerge(BasePaginatedVO);
	}

	private int getTotalPageWithPrev(BasePaginatedVO BasePaginatedVO, long prevTotalCount) {
		return BasePaginatedVO.getTotalPage(BasePaginatedVO.getTotalCount() + prevTotalCount, pageSize);
	}

	private ResizedPage resizingPage(BasePaginatedVO BasePaginatedVO, final long resizingStartPage, long prevTotalCount) {
		final long prevLastPageSize = getLastPageSize(prevTotalCount);

		long zeroBasedPage = Math.max(page - resizingStartPage, 0);
		long adjustedPageSize = adjustPageSize(resizingStartPage, prevLastPageSize);
		long adjustedOffset = adjustOffset(zeroBasedPage, resizingStartPage, prevLastPageSize);

		BasePaginatedVO.initialize(zeroBasedPage + 1, adjustedPageSize, adjustedOffset);
		return ResizedPage.merge(BasePaginatedVO);
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

	public boolean isMergedData(BasePaginatedVO BasePaginatedVO) {
		return get(BasePaginatedVO.getClass())
			.isMerged();
	}

	public <T extends BasePaginatedListVO<R>, R> List<R> getContent(Class<T> clazz, Function<T, List<R>> contentProvider) {
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

	public <T extends BasePaginatedVO> T getBasePaginatedVO(Class<T> clazz) {
		BasePaginatedVO BasePaginatedVO = get(clazz).getBasePaginatedVO();
		return clazz.cast(BasePaginatedVO);
	}

	public long getTotalCount() {
		return resizedPages.stream()
			.map(ResizedPage::getBasePaginatedVO)
			.mapToLong(BasePaginatedVO::getTotalCount)
			.sum();
	}

}

