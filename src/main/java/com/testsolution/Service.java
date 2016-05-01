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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Service service = (Service) o;

      return type == service.type;

   }

   @Override
   public int hashCode() {
      return type != null ? type.hashCode() : 0;
   }
}
