package edu.uptc.swii.customerservice.controller;

import edu.uptc.swii.customerservice.model.Customer;
import edu.uptc.swii.customerservice.service.CustomerEventProducer;
import edu.uptc.swii.customerservice.service.CustomerService;
import edu.uptc.swii.customerservice.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerEventProducer customerEventProducer;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/addcustomer")
    public String sendMessageAddCustomer(@RequestBody String customer) {
        customerEventProducer.sendAddCustomerEvent(JsonUtils.fromJson(customer, Customer.class));
        return "Evento enviado";
    }

    @PostMapping("/editcustomer")
    public String sendMessageEditCustomer(@RequestBody String customer) {
        customerEventProducer.sendEditCustomerEvent(JsonUtils.fromJson(customer, Customer.class));
        return "Evento enviado";
    }

    // 404 cuando no existe: order-service lo usa para validar el cliente
    @GetMapping("/findcustomerbyid/{document}")
    public ResponseEntity<Customer> findById(@PathVariable String document) {
        Customer customer = customerService.findById(document);
        return customer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(customer);
    }

    @GetMapping("/findallcustomers")
    public List<Customer> findAll() {
        return customerService.findAll();
    }
}
