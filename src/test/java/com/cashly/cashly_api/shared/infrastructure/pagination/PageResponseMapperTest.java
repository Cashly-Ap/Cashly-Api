package com.cashly.cashly_api.shared.infrastructure.pagination;

import com.cashly.cashly_api.shared.dto.PageResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageResponse DTO Unit Tests")
class PageResponseMapperTest {

    @Test
    void should_CreatePageResponse_When_PageHasContent() {
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 10);

        PageResponse<String> response = PageResponseMapper.from(page);

        assertEquals(3, response.content().size());
        assertEquals(10, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(10, response.size());
        assertEquals(0, response.number());
        assertTrue(response.first());
        assertTrue(response.last());
        assertFalse(response.empty());
    }

    @Test
    void should_CreateEmptyPageResponse_When_PageIsEmpty() {
        Page<String> emptyPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 20), 0);

        PageResponse<String> response = PageResponseMapper.from(emptyPage);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertEquals(20, response.size());
        assertTrue(response.empty());
        assertTrue(response.first());
        assertTrue(response.last());
    }

    @Test
    void should_CalculateCorrectTotalPages_When_PartialPage() {
        List<String> content = List.of("item1");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

        PageResponse<String> response = PageResponseMapper.from(page);

        assertEquals(3, response.totalPages());
        assertEquals(25, response.totalElements());
        assertTrue(response.first());
        assertFalse(response.last());
    }

    @Test
    void should_HandleMiddlePage_When_NotFirstOrLast() {
        List<String> content = List.of("item1");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 10), 35);

        PageResponse<String> response = PageResponseMapper.from(page);

        assertEquals(4, response.totalPages());
        assertEquals(1, response.number());
        assertFalse(response.first());
        assertFalse(response.last());
    }

    @Test
    void should_HandleLastPage_When_NumberEqualsTotalPagesMinusOne() {
        List<String> content = List.of("item1", "item2");
        Page<String> page = new PageImpl<>(content, PageRequest.of(2, 10), 25);

        PageResponse<String> response = PageResponseMapper.from(page);

        assertEquals(3, response.totalPages());
        assertEquals(2, response.number());
        assertFalse(response.first());
        assertTrue(response.last());
    }
}
