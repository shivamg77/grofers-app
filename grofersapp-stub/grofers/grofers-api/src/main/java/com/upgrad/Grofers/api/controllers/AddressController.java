package com.upgrad.Grofers.api.controllers;

import com.upgrad.Grofers.api.*;
import com.upgrad.Grofers.service.business.AddressService;
import com.upgrad.Grofers.service.business.CustomerAddressService;
import com.upgrad.Grofers.service.business.CustomerService;
import com.upgrad.Grofers.service.entity.AddressEntity;
import com.upgrad.Grofers.service.entity.CustomerAddressEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.entity.StateEntity;
import com.upgrad.Grofers.service.exception.AddressNotFoundException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SaveAddressException;
import com.upgrad.Grofers.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/")
public class AddressController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerAddressService customerAddressService;

    /**
     * A controller method to save an address in the database.
     *
     * @body SaveAddressRequest - This argument contains all the attributes required to store address details in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<SaveAddressResponse> type object along with Http status CREATED.
     * @throws AuthorizationFailedException
     * @throws SaveAddressException
     * @throws AddressNotFoundException
     */




    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestBody(required = false) final SaveAddressRequest saveAddressRequest, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException
    {
        String accessToken = authorization.split("Bearer ")[0];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        final CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();

        final AddressEntity address = new AddressEntity();
        address.setUuid(UUID.randomUUID().toString());
        address.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        address.setLocality(saveAddressRequest.getLocality());
        address.setCity(saveAddressRequest.getCity());
        address.setPinCode(saveAddressRequest.getPincode());
        address.setState(addressService.getStateByUUID(saveAddressRequest.getStateUuid()));
        customerAddressEntity.setAddress(address);


        final AddressEntity savedAddressEntity = addressService.saveAddress(address, customerAddressEntity);
        SaveAddressResponse addressResponse = new SaveAddressResponse().id(customerEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(addressResponse, HttpStatus.CREATED);
    }


    /**
     * A controller method to delete an address from the database.
     *
     * @param addressId    - The uuid of the address to be deleted from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<DeleteAddressResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws AddressNotFoundException
     */




    @RequestMapping(method = RequestMethod.DELETE, path = "address/{addressId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@PathVariable("addressId") final String addressID,
                                                               @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AddressNotFoundException {

        String accessToken = authorization.split("Bearer ")[0];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        if (addressID.equals("")) {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        AddressEntity addressEntity = addressService.getAddressByUUID(addressID, customerEntity);
        String Uuid  = addressService.deleteAddress(addressEntity, authorization);
        DeleteAddressResponse addDeleteResponse = new DeleteAddressResponse().id(UUID.fromString(Uuid)).status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(addDeleteResponse, HttpStatus.OK);
    }


    /*
    getallsavedaddresses endpoint retrieves all the addresses of a valid customer present in the database
    */
    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAddressByUUID(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");
        final CustomerEntity customerEntity = customerService.getCustomer(bearerToken[0]);
        final List<CustomerAddressEntity> customerAddressEntities = addressService.getAllAddress(customerEntity);

        List<AddressEntity> addressEntities = new ArrayList<>();
        for (CustomerAddressEntity customerAddressEntity : customerAddressEntities) {
            AddressEntity addressEntitiy = customerAddressEntity.getAddress();
            addressEntities.add(addressEntitiy);
        }

        Comparator<AddressEntity> compareBySavedTime = new Comparator<AddressEntity>() {
            @Override
            public int compare(AddressEntity a1, AddressEntity a2) {
                return a1.getId().compareTo(a2.getId());
            }
        };
        Collections.sort(addressEntities, compareBySavedTime);


        AddressListResponse addressListResponse = new AddressListResponse();

        for (AddressEntity address : addressEntities) {
            AddressList addressList = new AddressList();
            addressList.id(UUID.fromString(address.getUuid()));
            addressList.flatBuildingName(address.getFlatBuilNo());
            addressList.locality(address.getLocality());
            addressList.pincode(address.getPinCode());
            addressList.city(address.getCity());

            AddressListState addressListState = new AddressListState();
            addressListState.id(UUID.fromString(address.getState().getUuid()));
            addressListState.stateName(address.getState().getStateName());

            addressList.state(addressListState);

            addressListResponse.addAddressesItem(addressList);
        }

        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }
}
