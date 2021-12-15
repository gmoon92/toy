package com.gmoon.resourceserver.bookmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {
  @Autowired WebApplicationContext context;
  @MockBean BookmarkRepository repository;

  MockMvc mockMvc;
  String bookmarkName = "gmoon92.github.io";

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .alwaysDo(print())
            .apply(springSecurity())
            .build();
  }

  @Test
  @DisplayName("지정된 이름이로 북마크를 찾는다")
  void testGet() throws Exception {
    // given
    given(repository.findBookmarkByName(bookmarkName))
            .willReturn(Bookmark.create(bookmarkName));

    // when
    ResultActions result = mockMvc.perform(get("/bookmark/" + bookmarkName)
            .accept(MediaType.APPLICATION_JSON));

    // then
    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.name").value(bookmarkName));
  }

  @Test
  @DisplayName("북마크를 저장한다")
  void testSave() throws Exception {
    // given
    given(repository.save(any()))
            .willReturn(Bookmark.create(bookmarkName));

    // when
    ResultActions result = mockMvc.perform(post("/bookmark/" + bookmarkName)
            .accept(MediaType.APPLICATION_JSON));

    // then
    result.andExpect(status().isCreated());
    result.andExpect(jsonPath("$.name").value(bookmarkName));
  }

  @Test
  @DisplayName("북마크를 삭제한다")
  void testRemove() throws Exception {
    // when
    ResultActions result = mockMvc.perform(delete("/bookmark/" + bookmarkName)
            .accept(MediaType.APPLICATION_JSON));

    // then
    result.andExpect(status().isNoContent());
  }
}
