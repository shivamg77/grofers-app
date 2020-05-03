package com.upgrad.Grofers.api.controllers;



@RestController  @RequestMapping("/customer") public class CustomerController {
	@Autowired private CustomerService customerService;

	/**
	 * A controller method for customer signup.
	 *
	 * @param signupCustomerRequest - This argument contains all the attributes required to store customer details in the database.
	 * @return - ResponseEntity<SignupCustomerResponse> type object along with Http status CREATED.
	 * @throws SignUpRestrictedException
	 */

	/**
	 * A controller method for customer authentication.
	 *
	 * @param authorization - A field in the request header which contains the customer credentials as Basic authentication.
	 * @return - ResponseEntity<LoginResponse> type object along with Http status OK.
	 * @throws AuthenticationFailedException
	 */

	/**
	 * A controller method for customer logout.
	 *
	 * @param authorization - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 */

	/**
	 * A controller method for updating customer password.
	 *
	 * @param updatePasswordRequest - This argument contains all the attributes required to update customer password in the database.
	 * @param authorization         - A field in the request header which contains the JWT token.
	 * @return - ResponseEntity<LogoutResponse> type object along with Http status OK.
	 * @throws AuthorizationFailedException
	 * @throws UpdateCustomerException
	 */

}
