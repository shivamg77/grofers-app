package com.upgrad.Grofers.service.business;

import com.upgrad.Grofers.service.dao.AddressDao;
import com.upgrad.Grofers.service.dao.CustomerAddressDao;
import com.upgrad.Grofers.service.dao.CustomerDao;
import com.upgrad.Grofers.service.dao.StateDao;
import com.upgrad.Grofers.service.entity.*;
import com.upgrad.Grofers.service.exception.AddressNotFoundException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SaveAddressException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressDao addressDao;
    private final CustomerAddressDao customerAddressDao;
    private final CustomerDao customerDao;
    private final StateDao stateDao;

    public AddressServiceImpl(AddressDao addressDao, CustomerAddressDao customerAddressDao, CustomerDao customerDao, StateDao stateDao) {
        this.addressDao = addressDao;
        this.customerAddressDao = customerAddressDao;
        this.customerDao = customerDao;
        this.stateDao = stateDao;
    }


    /**
     * The method implements the business logic for save address endpoint.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerAddressEntity customerAddressEntity) throws SaveAddressException {
        String pinCodeRegex = "^[0-9]{6}$";

        if (addressEntity.getFlatBuilNo().isEmpty() || addressEntity.getLocality().isEmpty() || addressEntity.getCity().isEmpty() || addressEntity.getPinCode().isEmpty() || addressEntity.getUuid().isEmpty()) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        } else if (!addressEntity.getPinCode().matches(pinCodeRegex)) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        } else {
            return addressDao.saveAddress(addressEntity);
        }
    }

    /**
     * The method implements the business logic for get address by uuid endpoint.
     */
    @Override
    public AddressEntity getAddressByUUID(String addressId, CustomerEntity customerEntity) throws AuthorizationFailedException, AddressNotFoundException {


        AddressEntity addressEntity = addressDao.getAddressByUUID(addressId);
        if (addressId.isEmpty()) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }
        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        if (!addressEntity.getUuid().equals(customerEntity.getUuid())) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        return addressEntity;
    }

    /**
     * The method implements the business logic for delete address endpoint.
     */

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAddress(AddressEntity addressEntity, String authorization) throws AuthorizationFailedException {

        // Validate User
        CustomerAuthEntity customerAuth = customerDao.getCustomerAuthByAccesstoken(authorization);
        CustomerEntity customerEntity = customerDao.getCustomerById(addressEntity.getUuid());

        if (customerAuth.equals(null)) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        final ZonedDateTime customerSignOutTime = customerAuth.getLogoutAt();

        if (customerSignOutTime != null && customerAuth != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime customerSessionExpireTime = customerAuth.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());
        if (customerSessionExpireTime.compareTo(currentTime) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }


        if (customerAuth.getCustomer().getId().equals(addressEntity.getId())) {
            return addressDao.deleteAddress(addressEntity);
        } else {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }
    }


    /**
     * The method implements the business logic for saving customer address.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity) {
        return customerAddressEntity;
    }


    /**
     * The method implements the business logic for getting all saved address endpoint.
     *
     * @return
     */
    @Override
    public List<CustomerAddressEntity> getAllAddress(CustomerEntity customerEntity) {
//        List<CustomerAddressEntity> CustomerAddressEntities = customerAddressDao.getCustomerAddressesListByCustomerId(customerEntity);
//
//        return CustomerAddressEntities;
        return customerEntity.getCustomerAddress();

    }

    /**
     * The method implements the business logic for getting state by id.
     */
    @Override
    public StateEntity getStateByUUID(String StateUuid) throws AddressNotFoundException, SaveAddressException {
        StateEntity stateEntity = stateDao.getStateByUuid(StateUuid);
        if (StateUuid.isEmpty()) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        if (stateEntity == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        } else {
            return stateEntity;
        }
    }


}
