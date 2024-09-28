package com.brushup.in.service;

import com.brushup.in.entity.Customer;
import com.brushup.in.entity.Order;
import com.brushup.in.repository.CustomerRepository;
import com.brushup.in.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    public void getCustomerOrder(Long customerId, Long orderId){
        Customer customer = customerRepository.getReferenceById(customerId);
        Order order = orderRepository.getReferenceById(orderId);

        customer.getOrders().add(order);
        customerRepository.save(customer);
    }
}
