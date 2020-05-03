package com.upgrad.Grofers.api.controllers;


@RequestMapping("/store") public class StoreController {

	@Autowired private StoreService StoreService;

	@Autowired private ItemService itemService;

	@Autowired private CategoryService categoryService;

	@Autowired private CustomerService customerService;

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

		StoreEntity StoreEntity = StoreService.StoreByUUID(StoreId);
		List<CategoryEntity> StoreCategoryEntityList = categoryService.getCategoriesByStores(StoreId);
		StoreDetailsResponseAddressState StoreDetailsResponseAddressState = new StoreDetailsResponseAddressState()
				.id(UUID.fromString(StoreEntity.getAddress().getState().getUuid()))
				.stateName(StoreEntity.getAddress().getState().getStateName());
		StoreDetailsResponseAddress StoreDetailsResponseAddress =
				new StoreDetailsResponseAddress().id(UUID.fromString(StoreEntity.getAddress().getUuid()))
						.flatBuildingName(StoreEntity.getAddress().getFlatBuilNo())
						.city(StoreEntity.getAddress().getCity()).
						locality(StoreEntity.getAddress().getLocality()).pincode(StoreEntity.getAddress().getPincode())
						.state(StoreDetailsResponseAddressState);
		StoreDetailsResponse StoreDetailsResponse =
				new StoreDetailsResponse().id(UUID.fromString(StoreEntity.getUuid()))
						.storeName(StoreEntity.getStoreName()).
						customerRating(BigDecimal.valueOf(StoreEntity.getCustomerRating()))
						.numberCustomersRated(StoreEntity.getNumberCustomersRated()).
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
	}

	/**
	 * A controller method to get all Store from the database.
	 *
	 * @return - ResponseEntity<StoreListResponse> type object along with Http status OK.
	 */
	@RequestMapping(method = RequestMethod.GET, path = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<StoreListResponse> getAllStore() {
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
							.pincode(StoreEntity.getAddress().getPincode()).state(StoreDetailsResponseAddressState);
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
