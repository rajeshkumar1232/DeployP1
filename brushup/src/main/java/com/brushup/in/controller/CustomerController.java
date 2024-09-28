package com.brushup.in.controller;

import com.brushup.in.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cust")
public class CustomerController {


    @Autowired
    CustomerService customerService;

    @PostMapping("/{customerId}/order/{orderId}")
    public ResponseEntity<?> orderOfCustomer(@PathVariable Long CustomerId, @PathVariable Long orderId){
        customerService.getCustomerOrder(CustomerId, orderId );

        return ResponseEntity.ok("order for customer has been confimred");
    }
}
