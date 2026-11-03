package edu.uptc.swii.customerservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uptc.swii.customerservice.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}