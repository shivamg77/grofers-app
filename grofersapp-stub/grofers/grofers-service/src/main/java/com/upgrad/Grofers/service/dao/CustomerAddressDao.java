package com.upgrad.Grofers.service.dao;

import com.upgrad.Grofers.service.entity.AddressEntity;
import com.upgrad.Grofers.service.entity.CustomerAddressEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerAddressEntity getCustomerAddressByAddress(AddressEntity address) {

        try {
            return this.entityManager.createNamedQuery("customerAddressByAddressId", CustomerAddressEntity.class).setParameter("address", address).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    public CustomerAddressEntity getCustomerByAddress(String addressId) {
        try {
            return entityManager.createNamedQuery("userByAddress", CustomerAddressEntity.class).setParameter("uuid", addressId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

//    public  List<CustomerAddressEntity> getCustomerAddressesListByCustomerId(CustomerEntity customerEntity) {
//
//        try {
//            return this.entityManager.createNamedQuery("userByAddress", CustomerAddressEntity.class).setParameter("customerEntity", customerEntity).getResultList();
//        } catch (NoResultException nre) {
//            return null;
//        }
//    }


    public CustomerAddressEntity createCustomerAddress(CustomerAddressEntity customerAddressEntity) {
        this.entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }
}