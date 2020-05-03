package com.upgrad.Grofers.service.business;

@Service public class AddressServiceImpl implements AddressService {

	@Autowired private AddressDao addressDao;

	/**
	 * The method implements the business logic for save address endpoint.
	 */
	@Override @Transactional(propagation = Propagation.REQUIRED) public AddressEntity saveAddress(AddressEntity addressEntity, CustomerAddressEntity customerAddressEntity) throws SaveAddressException {
	}

	/**
	 * The method implements the business logic for get address by uuid endpoint.
	 */
	@Override public AddressEntity getAddressByUUID(String addressId, CustomerEntity customerEntity) throws AuthorizationFailedException, AddressNotFoundException {
	}


	/**
	 * The method implements the business logic for saving customer address.
	 */
	@Transactional(propagation = Propagation.REQUIRED) public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity) {
	}

	/**
	 * The method implements the business logic for delete address endpoint.
	 */
	@Override @Transactional(propagation = Propagation.REQUIRED) public AddressEntity deleteAddress(AddressEntity addressEntity) {
	}

	/**
	 * The method implements the business logic for getting all saved address endpoint.
	 */
	@Override public List<AddressEntity> getAllAddress(CustomerEntity customer) {
	}

	/**
	 * The method implements the business logic for getting state by id.
	 */
	@Override public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
	}


}
