package com.upgrad.Grofers.api.controllers;


import com.upgrad.Grofers.api.*;
import com.upgrad.Grofers.service.business.*;
import com.upgrad.Grofers.service.entity.CategoryEntity;
import com.upgrad.Grofers.service.entity.ItemEntity;
import com.upgrad.Grofers.service.entity.StoreEntity;
import com.upgrad.Grofers.service.exception.CategoryNotFoundException;
import com.upgrad.Grofers.service.exception.StoreNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CustomerService customerService;


    /**
     * A controller method to get a Store details from the database.
     *
     * @param StoreId - The uuid of the Store whose details has to be fetched from the database.
     * @return - ResponseEntity<StoreDetailsResponse> type object along with Http status OK.
     * @throws StoreNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{Store_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StoreDetailsResponse> getStoreDetails(@PathVariable("Store_id") String StoreId)
            throws StoreNotFoundException {

        StoreEntity storeEntity = storeService.storeByUUID(StoreId);
        if(storeEntity==null){
            throw new StoreNotFoundException("RNF-001", "No Store by this id");

        }
        List<CategoryEntity> StoreCategoryEntityList = categoryService.getCategoriesByStores(StoreId);
        StoreDetailsResponseAddressState StoreDetailsResponseAddressState = new StoreDetailsResponseAddressState()
                .id(UUID.fromString(storeEntity.getAddress().getState().getUuid()))
                .stateName(storeEntity.getAddress().getState().getStateName());
        StoreDetailsResponseAddress StoreDetailsResponseAddress =
                new StoreDetailsResponseAddress().id(UUID.fromString(storeEntity.getAddress().getUuid()))
                        .flatBuildingName(storeEntity.getAddress().getFlatBuilNo())
                        .city(storeEntity.getAddress().getCity()).
                        locality(storeEntity.getAddress().getLocality()).pincode(storeEntity.getAddress().getPinCode())
                        .state(StoreDetailsResponseAddressState);
        StoreDetailsResponse StoreDetailsResponse =
                new StoreDetailsResponse().id(UUID.fromString(storeEntity.getUuid()))
                        .storeName(storeEntity.getStoreName()).
                        customerRating(BigDecimal.valueOf(storeEntity.getCustomerRating()))
                        .numberCustomersRated(storeEntity.getNumberCustomersRated()).
                        address(StoreDetailsResponseAddress);
        for (CategoryEntity categoryEntity : StoreCategoryEntityList) {
            CategoryList categoryList = new CategoryList().id(UUID.fromString(categoryEntity.getUuid()))
                    .categoryName(categoryEntity.getCategoryName());
            List<ItemEntity> categoryItemEntities =
                    itemService.getItemsByCategoryAndStore(StoreId, categoryEntity.getUuid());
            for (ItemEntity itemEntity : categoryItemEntities) {
                ItemList itemList =
                        new ItemList().id(UUID.fromString(itemEntity.getUuid())).itemName(itemEntity.getItemName()).
                                price(itemEntity.getPrice());
                categoryList.addItemListItem(itemList);
            }
            StoreDetailsResponse.addCategoriesItem(categoryList);
        }
        return new ResponseEntity<StoreDetailsResponse>(StoreDetailsResponse, HttpStatus.OK);
    }

    /**
     * A controller method to get Store details by its name from the database.
     *
     * @param StoreName - The name of the Store whose details has to be fetched from the database.
     * @return - ResponseEntity<StoreListResponse> type object along with Http status OK.
     * @throws StoreNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "name/{Store_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StoreListResponse> getStoreByName(@PathVariable("Store_name") String StoreName) throws StoreNotFoundException {

        final List<StoreEntity> stores = storeService.storesByName("%" + StoreName + "%");
        if (stores == null) {
            return new ResponseEntity<StoreListResponse>(new StoreListResponse(), HttpStatus.NOT_FOUND);
        }
        Comparator<StoreEntity> compareByStoreName = new Comparator<StoreEntity>() {
            @Override
            public int compare(StoreEntity r1, StoreEntity r2) {
                return r1.getStoreName().compareTo(r2.getStoreName());
            }
        };
        StoreListResponse storesResponse =  getStoreListResponse(stores);
        return new ResponseEntity<StoreListResponse>(storesResponse, HttpStatus.OK);
    }


    /**
     * A controller method to get all Store by a category from the database.
     *
     * @param categoryId - The uuid of the category under which the Store list has to be fetched from the database.
     * @return - ResponseEntity<StoreListResponse> type object along with Http status OK.
     * @throws CategoryNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/category/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StoreListResponse> getStoreByCategory(@PathVariable("category_id") String categoryId) throws CategoryNotFoundException {
        if (categoryId.isEmpty()) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity = categoryService.getCategoryById(categoryId);
        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        List<StoreEntity> storeEntityList = categoryEntity.getStores();
        return (ResponseEntity<StoreListResponse>) storeEntityList;
    }

    /**
     * A controller method to get all Store from the database.
     *
     * @return - ResponseEntity<StoreListResponse> type object along with Http status OK.
     */
    @RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StoreListResponse> getAllStore() {

        final List<StoreEntity> stores = storeService.storesByRating();
        Comparator<StoreEntity> compareByCustomerRating = new Comparator<StoreEntity>() {
            @Override
            public int compare(StoreEntity r1, StoreEntity r2) {
                return r2.getCustomerRating().compareTo(r1.getCustomerRating());
            }
        };
        Collections.sort(stores, compareByCustomerRating);
        StoreListResponse storesResponse = getStoreListResponse(stores);
        return new ResponseEntity<StoreListResponse>(storesResponse, HttpStatus.OK);

    }


    private StoreListResponse getStoreListResponse(List<StoreEntity> StoreEntityList) {
        StoreListResponse StoreListResponse = new StoreListResponse();
        for (StoreEntity StoreEntity : StoreEntityList) {
            List<CategoryEntity> StoreCategoryEntityList = categoryService.getCategoriesByStores(StoreEntity.getUuid());
            StoreDetailsResponseAddressState StoreDetailsResponseAddressState = new StoreDetailsResponseAddressState()
                    .id(UUID.fromString(StoreEntity.getAddress().getState().getUuid()))
                    .stateName(StoreEntity.getAddress().getState().getStateName());
            StoreDetailsResponseAddress StoreDetailsResponseAddress =
                    new StoreDetailsResponseAddress().id(UUID.fromString(StoreEntity.getAddress().getUuid()))
                            .flatBuildingName(StoreEntity.getAddress().getFlatBuilNo())
                            .city(StoreEntity.getAddress().getCity()).
                            locality(StoreEntity.getAddress().getLocality())
                            .pincode(StoreEntity.getAddress().getPinCode()).state(StoreDetailsResponseAddressState);
            String categoryString = "";
            for (CategoryEntity categoryEntity : StoreCategoryEntityList) {
                categoryString = categoryString + categoryEntity.getCategoryName() + ", ";
            }
            categoryString = categoryString.substring(0, categoryString.length() - 2);
            StoreList StoreList =
                    new StoreList().id(UUID.fromString(StoreEntity.getUuid())).storeName(StoreEntity.getStoreName()).
                            customerRating(BigDecimal.valueOf(StoreEntity.getCustomerRating()))
                            .numberCustomersRated(StoreEntity.getNumberCustomersRated()).
                            address(StoreDetailsResponseAddress).categories(categoryString);
            StoreListResponse.addStoresItem(StoreList);
        }
        return StoreListResponse;
    }

}
