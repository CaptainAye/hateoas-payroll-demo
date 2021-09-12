package com.example.payroll.controller;

import com.example.payroll.controller.exception.EmployeeNotFoundException;
import com.example.payroll.controller.hateoas.EmployeeModelAssembler;
import com.example.payroll.model.Employee;
import com.example.payroll.repository.EmployeeRepository;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeModelAssembler assembler;

    public EmployeeController(EmployeeRepository employeeRepository, EmployeeModelAssembler assembler) {
        this.employeeRepository = employeeRepository;
        this.assembler = assembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Employee>> getEmployees() {
        return assembler.toCollectionModel(employeeRepository.findAll())
                .add(linkTo(methodOn(EmployeeController.class).getEmployees()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<Employee> getEmployee(@PathVariable Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with id %d does not exist", id)));
        return assembler.toModel(employee);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<Employee> createEmployee(@RequestBody Employee employee) {
        return assembler.toModel(employeeRepository.save(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Employee>> updateEmployee(@PathVariable Long id, @RequestBody Employee newEmployee) {
        return employeeRepository.findById(id).map(
                employee -> {
                    employee.setName(getProperty(Employee::getName, employee, newEmployee));
                    employee.setRole(getProperty(Employee::getRole, employee, newEmployee));
                    return ResponseEntity.ok(assembler.toModel(employeeRepository.save(employee)));
                }
        ).orElseGet(() -> {
            newEmployee.setId(id);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(assembler.toModel(employeeRepository.save(newEmployee)));
        });
    }

    private <E, P> P getProperty(Function<E, P> propertyMapper, E entityToUpdate, E updatedEntity) {
        P updatedProperty = propertyMapper.apply(updatedEntity);
        return updatedProperty != null ? updatedProperty : propertyMapper.apply(entityToUpdate);

    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }
}
