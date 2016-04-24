package com.testsolution;

public class Service {
   private String name;
   private ServiceType type;

   public Service(String name, ServiceType type) {
      this.name = name;
      this.type = type;
   }

   @Override
   public String toString() {
      return name;
   }
}
