package com.example.payroll.controller.hateoas;

import com.example.payroll.controller.EmployeeController;
import com.example.payroll.model.Employee;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EmployeeModelAssembler implements RepresentationModelAssembler<Employee, EntityModel<Employee>> {
    @Override
    public EntityModel<Employee> toModel(Employee entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(EmployeeController.class).getEmployee(entity.getId())).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).getEmployees()).withRel("employees"));
    }
}
