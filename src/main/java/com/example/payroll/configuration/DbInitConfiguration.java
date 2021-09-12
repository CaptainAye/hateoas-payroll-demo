package com.example.payroll.configuration;

import com.example.payroll.model.Employee;
import com.example.payroll.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DbInitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DbInitConfiguration.class);

    @Bean
    CommandLineRunner dbInit(EmployeeRepository employeeRepository) {
        return args -> {
            Employee will = new Employee("Will", "Product manager");
            Employee tom = new Employee("Tom", "Software dev");
            List<Employee> employeesToCreate = Arrays.asList(will, tom);
            List<Employee> createdEmployees = employeeRepository.saveAll(employeesToCreate);
            log.info("Created employees: {}", createdEmployees);
        };

    }
}
