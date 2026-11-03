package edu.uptc.swii.orderservice.repository;

import edu.uptc.swii.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,String > {}
