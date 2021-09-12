package com.example.payroll.controller.hateoas;

import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import com.example.payroll.controller.OrderController;
import com.example.payroll.model.Status;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import com.example.payroll.model.Order;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<Order, EntityModel<Order>> {
    @Override
    public EntityModel<Order> toModel(Order entity) {

        EntityModel<Order> model = EntityModel.of(entity)
                .add(linkTo(methodOn(OrderController.class).getOrder(entity.getId())).withSelfRel(),
                        linkTo(methodOn(OrderController.class).getOrders()).withRel("orders"));

      if (entity.getStatus().equals(Status.IN_PROGRESS)) {
          model.add(linkTo(methodOn(OrderController.class).cancelOrder(entity.getId())).withRel("cancel"));
          model.add(linkTo(methodOn(OrderController.class).completeOrder(entity.getId())).withRel("complete"));

      }
      return model;
    }
}
