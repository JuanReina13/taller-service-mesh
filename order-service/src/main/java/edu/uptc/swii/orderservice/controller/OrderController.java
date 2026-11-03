package edu.uptc.swii.orderservice.controller;

import edu.uptc.swii.orderservice.client.CustomerClient;
import edu.uptc.swii.orderservice.model.Order;
import edu.uptc.swii.orderservice.service.OrderEventProducer;
import edu.uptc.swii.orderservice.service.OrderService;
import edu.uptc.swii.orderservice.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderEventProducer producer;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerClient customerClient;

    // Antes de generar el pedido se consulta el cliente en customer-service
    @PostMapping("/addorder")
    public ResponseEntity<String> addOrder(@RequestBody String body) {
        Order order = JsonUtils.fromJson(body, Order.class);
        String document = order.getCustomerDocument();
        if (document == null || document.isBlank()) {
            return ResponseEntity.badRequest().body("El pedido debe tener customerDocument");
        }
        try {
            if (!customerClient.exists(document)) {
                return ResponseEntity.badRequest().body("El cliente " + document + " no existe");
            }
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("customer-service no disponible: " + e.getMessage());
        }
        producer.sendAddOrderEvent(order);
        return ResponseEntity.ok("Evento enviado");
    }

    @PostMapping("/editorder")
    public String editOrder(@RequestBody String body) {
        producer.sendEditOrderEvent(JsonUtils.fromJson(body, Order.class));
        return "Evento enviado";
    }

    @GetMapping("/findorderbyid/{id}")
    public Order findById(@PathVariable String id) {
        return orderService.findById(id);
    }

    @GetMapping("/findallorders")
    public List<Order> findAllOrders() {
        return orderService.findAll();
    }
}
