package com.gmoon.resourceserver.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
  Bookmark findBookmarkByName(String name);
}
