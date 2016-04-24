package com.testsolution;

import java.util.Calendar;

public class Payment {
   private Client client;        //Кому платит?
   private Service service;      //За что?
   private float sum;            //Сколько?
   private Calendar calendar;    //Когда?

   public Payment(Client client, Service service, float sum) {
      if (client != null)
         this.client = client;
      else throw new IllegalArgumentException();

      if (service != null)
         this.service = service;
      else throw new IllegalArgumentException();

      if (sum >= 0)
         this.sum = sum;
      else throw new IllegalArgumentException();

      calendar = Calendar.getInstance();
   }

   public Client getClient() {
      return client;
   }

   public Service getService() {
      return service;
   }

   public float getSum() {
      return sum;
   }

   public Calendar getCalendar() {
      return calendar;
   }

   public void setCalendar(Calendar calendar) {    //Для проверки
      if (calendar != null) {
         this.calendar = calendar;
      } else throw new IllegalArgumentException();
   }
}
