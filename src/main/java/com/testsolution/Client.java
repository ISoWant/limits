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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Client client = (Client) o;

      return account != null ? account.equals(client.account) : client.account == null;

   }

   @Override
   public int hashCode() {
      return account != null ? account.hashCode() : 0;
   }
}
