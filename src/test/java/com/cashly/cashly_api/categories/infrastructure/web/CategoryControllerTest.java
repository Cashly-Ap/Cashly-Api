package com.cashly.cashly_api.categories.infrastructure.web;

import com.cashly.cashly_api.categories.application.create.request.CreateCategoryRequest;
import com.cashly.cashly_api.categories.application.update.request.UpdateCategoryRequest;
import com.cashly.cashly_api.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.categories.application.create.use_case.CreateCategoryUseCase;
import com.cashly.cashly_api.categories.application.delete.use_case.DeleteCategoryUseCase;
import com.cashly.cashly_api.categories.application.queries.use_case.GetCategoryUseCase;
import com.cashly.cashly_api.categories.application.queries.use_case.ListCategoriesUseCase;
import com.cashly.cashly_api.categories.application.update.use_case.UpdateCategoryUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Mock
    private CreateCategoryUseCase createCategoryUseCase;

    @Mock
    private GetCategoryUseCase getCategoryUseCase;

    @Mock
    private ListCategoriesUseCase listCategoriesUseCase;

    @Mock
    private UpdateCategoryUseCase updateCategoryUseCase;

    @Mock
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    @DisplayName("should create category and return 201")
    void should_create_category_and_return_201() {
        String categoryId = UUID.randomUUID().toString();
        CategoryDetailResponse response = new CategoryDetailResponse(
                categoryId, "Test Category", "Description", true, 0L,
                LocalDateTime.now(), LocalDateTime.now());

        when(createCategoryUseCase.execute(any(CreateCategoryRequest.class))).thenReturn(response);

        CreateCategoryRequest request = new CreateCategoryRequest("Test Category", "Description");

        ResponseEntity<CategoryDetailResponse> result = categoryController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(categoryId, result.getBody().id());
        verify(createCategoryUseCase).execute(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("should get all categories and return 200")
    void should_get_all_categories_and_return_200() {
        CategoryListResponse cat = new CategoryListResponse(
                UUID.randomUUID().toString(), "Category 1", "Desc", true, false);
        PageResponse<CategoryListResponse> pageResponse = new PageResponse<>(
                List.of(cat), 1L, 1, 10, 0, true, true, false);

        when(listCategoriesUseCase.execute(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<CategoryListResponse>> result = categoryController.getAll(
                null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        verify(listCategoriesUseCase).execute(null, null, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("should get empty categories list")
    void should_get_empty_categories_list() {
        PageResponse<CategoryListResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(listCategoriesUseCase.execute(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        ResponseEntity<PageResponse<CategoryListResponse>> result = categoryController.getAll(
                null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
        verify(listCategoriesUseCase).execute(null, null, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("should get category by id and return 200")
    void should_get_category_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        CategoryDetailResponse response = new CategoryDetailResponse(
                idStr, "Category", "Desc", true, 5L,
                LocalDateTime.now(), LocalDateTime.now());

        when(getCategoryUseCase.execute(id)).thenReturn(response);

        ResponseEntity<CategoryDetailResponse> result = categoryController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(idStr, result.getBody().id());
        verify(getCategoryUseCase).execute(id);
    }

    @Test
    @DisplayName("should update category and return 200")
    void should_update_category_and_return_200() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        CategoryDetailResponse response = new CategoryDetailResponse(
                idStr, "Updated", "Updated desc", true, 0L,
                LocalDateTime.now(), LocalDateTime.now());

        when(updateCategoryUseCase.execute(eq(id), any(UpdateCategoryRequest.class))).thenReturn(response);

        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated", "Updated desc", true);

        ResponseEntity<CategoryDetailResponse> result = categoryController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(idStr, result.getBody().id());
        verify(updateCategoryUseCase).execute(eq(id), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("should delete category and return 204")
    void should_delete_category_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteCategoryUseCase).execute(id);

        ResponseEntity<Void> result = categoryController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteCategoryUseCase).execute(id);
    }
}
