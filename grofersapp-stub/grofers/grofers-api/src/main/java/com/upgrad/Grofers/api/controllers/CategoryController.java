package com.upgrad.Grofers.api.controllers;

import com.upgrad.Grofers.api.CategoriesListResponse;
import com.upgrad.Grofers.api.CategoryDetailsResponse;
import com.upgrad.Grofers.api.CategoryListResponse;
import com.upgrad.Grofers.api.ItemList;
import com.upgrad.Grofers.service.business.CategoryService;
import com.upgrad.Grofers.service.entity.CategoryEntity;
import com.upgrad.Grofers.service.entity.ItemEntity;
import com.upgrad.Grofers.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * A controller method to get CategoryById
     *
     * @param categoryId - The uuid of the category whose detail is asked from the database..
     * @return - ResponseEntity<CategoryDetailsResponse> type object along with Http status OK.
     * @throws CategoryNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryById(@PathVariable("category_id") String categoryId) throws CategoryNotFoundException {
         // Get all items for category
        CategoryEntity categoryEntity = categoryService.getCategoryById(categoryId);

        // Create response
        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse().id(UUID.fromString(categoryEntity.getUuid())).categoryName(categoryEntity.getCategoryName());

        for (ItemEntity itemEntity : categoryEntity.getItems()) {
            ItemList itemList = new ItemList()
                    .id(UUID.fromString(itemEntity.getUuid()))
                    .itemName(itemEntity.getItemName())
                    .price(itemEntity.getPrice());
            categoryDetailsResponse.addItemListItem(itemList);
        }

        return new ResponseEntity<CategoryDetailsResponse>(categoryDetailsResponse, HttpStatus.OK);

    }

    /**
     * A controller method to get all categories from the database.
     *
     * @return - ResponseEntity<CategoriesListResponse> type object along with Http status OK.
     */
    @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getAllCategories() {
        // get all categories ordered by name
        List<CategoryEntity> categoryEntityList = categoryService.getAllCategoriesOrderedByName();

        // create response
        CategoriesListResponse categoriesListResponse = new CategoriesListResponse();

        for (CategoryEntity categoryEntity : categoryEntityList) {
            CategoryListResponse categoryListResponse = new CategoryListResponse()
                    .id(UUID.fromString(categoryEntity.getUuid()))
                    .categoryName(categoryEntity.getCategoryName());
            categoriesListResponse.addCategoriesItem(categoryListResponse);
        }
        return new ResponseEntity<CategoriesListResponse>(categoriesListResponse, HttpStatus.OK);

    }


}