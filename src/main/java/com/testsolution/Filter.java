package com.testsolution;

import java.util.ArrayList;
import java.util.Map;

public class Filter {
   private Map<Limit, ArrayList<Payment>> limits;
   private ArrayList<Payment> tbc;
   private ArrayList<Payment> readyToHost;


   public void paymentReview(Payment payment) {
      boolean isCorrect = true;
      for (Limit limit : limits.keySet()) {
         if (!isCorrectPayment(limit, payment)) {
            isCorrect = false;
            break;
         }
      }

      if (isCorrect) {
         readyToHost.add(payment);
      } else {
         tbc.add(payment);
      }
   }

   private boolean isCorrectPayment(Limit limit, Payment payment) {
      /*Solution*/
      return true;
   }

   public void addLimit(Limit limit, String description) {
      if (limit != null && description != null) {
         this.limits.put(limit, null);
      } else
         throw new IllegalArgumentException();
   }

   public void deleteLimit() {
   }
}