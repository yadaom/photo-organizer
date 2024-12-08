package com.omprakashyadav.arrange.photos;

import jakarta.enterprise.context.Dependent;

@Dependent
public class GreetingService {
  public void sayHello(String name) {
    System.out.println("Hello " + name + "!");
  }
}