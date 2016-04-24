package com.testsolution;

public class Client {
   private String name;
   private String account;

   public Client(String name, String account) {
      this.name = name;
      this.account = account;
   }

   @Override
   public String toString() {
      return account + " (" + name + ")";
   }
}
