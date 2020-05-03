package com.upgrad.Grofers.api.controllers;


import com.upgrad.Grofers.api.*;
import com.upgrad.Grofers.service.business.CustomerService;
import com.upgrad.Grofers.service.entity.CustomerAuthEntity;
import com.upgrad.Grofers.service.entity.CustomerEntity;
import com.upgrad.Grofers.service.exception.AuthenticationFailedException;
import com.upgrad.Grofers.service.exception.AuthorizationFailedException;
import com.upgrad.Grofers.service.exception.SignUpRestrictedException;
import com.upgrad.Grofers.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer") public class CustomerController {
	@Autowired
	private CustomerService customerService;

	/**
	 * A controller method for customer signup.
	 *
	 * @param signupCustomerRequest - This argument contains all the attributes required to store customer details in the database.
	 * @return - ResponseEntity<SignupCustomerResponse> type object along with Http status CREATED.
	 * @throws SignUpRestrictedException
	 */




	@RequestMapping(method = RequestMethod.POST, path = "signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<SignupCustomerResponse> signup(@RequestBody(required = false) final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {


		//If any field other than last name is empty,
		if (signupCustomerRequest.getFirstName().isEmpty() || signupCustomerRequest.getContactNumber().isEmpty() || signupCustomerRequest.getEmailAddress().isEmpty() || signupCustomerRequest.getPassword().isEmpty()) {
			throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
		}
		final CustomerEntity customerEntity = new CustomerEntity();

		customerEntity.setUuid(UUID.randomUUID().toString());
		customerEntity.setFirstName(signupCustomerRequest.getFirstName());
		customerEntity.setLastName(signupCustomerRequest.getLastName());
		customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
		customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
		customerEntity.setSalt("1234abc");
		customerEntity.setPassword(signupCustomerRequest.getPassword());

		final CustomerEntity createdCustomerEntity = customerService.saveCustomer(customerEntity);

		SignupCustomerResponse customerResponse = new SignupCustomerResponse()
				.id(createdCustomerEntity.getUuid())
				.status("CUSTOMER SUCCESSFULLY REGISTERED");
		return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
	}

	/**
	 * A controller method for customer authentication.
	 *
	 * @param authorization - A field in the request header which contains the customer credentials as Basic authentication.
	 * @return - ResponseEntity<LoginResponse> type object along with Http status OK.
	 * @throws AuthenticationFailedException
	 */

	//login method is used to perform a Basic authorization when the customer tries to login for the first time.
	@RequestMapping(method = RequestMethod.POST, path = "/customer/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<LoginResponse> login(
			@RequestHeader("authorization") final String authorization)
			throws AuthenticationFailedException
	{
		byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[0]);
		String decodedText = new String(decode);
		int index = decodedText.indexOf(":");
		if (decodedText.substring(index, decodedText.length() - 1).equals("")
				|| index == 0) {
			throw new AuthenticationFailedException(
					"ATH-003", "Incorrect format of decoded customer name and password");
		}
		String[] credentials = decodedText.split(":");
		String userName = credentials[0];
		String password = credentials[1];
		CustomerAuthEntity customerAuth = customerService.authenticate(userName, password);
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setId(customerAuth.getCustomer().getUuid());
		loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
		loginResponse.setFirstName(customerAuth.getCustomer().getFirstName());
		loginResponse.setLastName(customerAuth.getCustomer().getLastName());
		loginResponse.setEmailAddress(customerAuth.getCustomer().getEmail());
		loginResponse.setContactNumber(customerAuth.getCustomer().getContactNumber());
		HttpHeaders headers = new HttpHeaders();
		headers.set("access-token", customerAuth.getAccessToken());
		List<String> header = new ArrayList<>();
		header.add("access-token");
		headers.setAccessControlExposeHeaders(header);
		return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
	}


	/**
	 * A controller method for customer logout.
	 *
	 * @param authorization - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 */


	//logout method is used to logout a loggedin customer from the application.
	@RequestMapping(method = RequestMethod.POST, path = "/customer/logout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<LogoutResponse> logout(
			@RequestHeader("authorization") final String authorization)
			throws AuthorizationFailedException
	{
		String accessToken = authorization.split("Bearer ")[0];
		CustomerAuthEntity customerAuthEntity = customerService.logout(accessToken);
		LogoutResponse logoutResponse = new LogoutResponse()
				.id(customerAuthEntity.getCustomer().getUuid()).message("LOGGED OUT SUCCESSFULLY");
		return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
	}


	/**
	 * A controller method for updating customer password.
	 *
	 password * @param updatePasswordRequest - This argument contains all the attributes required to update customer password in the database.
	 * database@param authorization         - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 * @throws UpdateCustomerException
	 */

	//changePassword method is used to change the customer password details from the application.
	@RequestMapping(method = RequestMethod.PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<UpdatePasswordResponse> changePassword(@RequestBody(required = false) final UpdatePasswordRequest updatePasswordRequest,
																 @RequestHeader("authorization") final String authorization) throws UpdateCustomerException, AuthorizationFailedException {

		if (updatePasswordRequest.getOldPassword().equals("") || updatePasswordRequest.getNewPassword().equals("")) {
			throw new UpdateCustomerException("UCR-003", "No field should be empty");
		}

		String accessToken = authorization.split("Bearer ")[0];
		CustomerEntity customerEntity = customerService.getCustomer(accessToken);

		CustomerEntity updatedCustomerEntity = customerService.updateCustomerPassword(
				updatePasswordRequest.getOldPassword(),
				updatePasswordRequest.getNewPassword(),
				customerEntity
		);

		UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse()
				.id(updatedCustomerEntity.getUuid())
				.status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
		return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
	}

}
