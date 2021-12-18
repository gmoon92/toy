package com.gmoon.resourceserver.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ResponseBody
@RequestMapping("/bookmark")
@RequiredArgsConstructor
public class BookmarkController {
  private final BookmarkRepository repository;

  @GetMapping
  public ResponseEntity<List<Bookmark>> getAll() {
    return ResponseEntity.ok(repository.findAll());
  }

  @GetMapping("/{name}")
  public ResponseEntity<Bookmark> get(@PathVariable String name) {
    return ResponseEntity.ok(repository.findBookmarkByName(name));
  }

  @PostMapping("/{name}")
  public ResponseEntity<Bookmark> save(@PathVariable String name) {
    Bookmark bookmark = repository.save(Bookmark.create(name));
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(bookmark);
  }

  @DeleteMapping("/{name}")
  public ResponseEntity<Void> remove(@PathVariable String name) {
    repository.delete(repository.findBookmarkByName(name));
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .build();
  }
}
