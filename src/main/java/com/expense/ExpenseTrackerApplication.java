package com.expense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication tells Spring that this is the main class to run our application
@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        // Starts the Spring Boot application and internal web server (Tomcat)
        SpringApplication.run(ExpenseTrackerApplication.class, args);
        System.out.println("==================================================");
        System.out.println("  Personal Expense Tracker started successfully!  ");
        System.out.println("  Open your browser at: http://localhost:8080    ");
        System.out.println("==================================================");
    }
}
