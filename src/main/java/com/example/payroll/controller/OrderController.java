package com.example.payroll.controller;

import com.example.payroll.controller.exception.EntityNotFoundException;
import com.example.payroll.controller.hateoas.OrderModelAssembler;
import com.example.payroll.model.Order;
import com.example.payroll.model.Status;
import com.example.payroll.repository.OrderRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(("/orders"))
public class OrderController {
    private final OrderRepository orderRepository;
    private final OrderModelAssembler assembler;

    public OrderController(OrderRepository orderRepository, OrderModelAssembler assembler) {
        this.orderRepository = orderRepository;
        this.assembler = assembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Order>> getOrders() {
        return assembler.toCollectionModel(orderRepository.findAll()).add(linkTo(methodOn(OrderController.class).getOrders()).withSelfRel());
    }

    @GetMapping("{id}")
    public EntityModel<Order> getOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Order with id: %d not found", id)));
        return assembler.toModel(order);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Order>> createOrder(@RequestBody Order order) {
        order.setStatus(Status.IN_PROGRESS);
        Order createdOrder = orderRepository.save(order);
        EntityModel<Order> model = assembler.toModel(createdOrder);
        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    @PutMapping("{id}")
    public ResponseEntity<EntityModel<Order>> updateOrder(@PathVariable Long id, @RequestBody Order newOrder) {
        return orderRepository.findById(id)
                .map(
                        order -> {
                            order.setDescription(getProperty(Order::getDescription, order, newOrder));
                            order.setStatus(getProperty(Order::getStatus, order, newOrder));
                            var updatedOrder = orderRepository.save(order);
                            var model = assembler.toModel(updatedOrder);
                            return ResponseEntity.ok(model);
                        }
                ).orElseGet(() -> {
                    var createdOrder = orderRepository.save(newOrder);
                    var model = assembler.toModel(createdOrder);
                    return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);

                });
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", id)));

        if (order.getStatus().equals(Status.IN_PROGRESS)) {
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(assembler.toModel(orderRepository.save(order)));
        } else {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Method not allowed")
                            .withDetail(String.format("You can't cancel an order that is %s", order.getStatus())));
        }
    }

    @PutMapping("{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", id)));

        if (order.getStatus().equals(Status.IN_PROGRESS)) {
            order.setStatus(Status.COMPLETED);
            return ResponseEntity.ok(assembler.toModel(orderRepository.save(order)));
        } else {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Method not allowed")
                            .withDetail(String.format("You can't cancel an order that is %s", order.getStatus())));
        }
    }


    private <E, P> P getProperty(Function<E, P> propertyMapper, E entityToUpdate, E updatedEntity) {
        P updatedProperty = propertyMapper.apply(updatedEntity);
        return updatedProperty != null ? updatedProperty : propertyMapper.apply(entityToUpdate);

    }
}
