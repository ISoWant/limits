package com.testsolution;

import java.util.ArrayList;

public class Filter {
   private ArrayList<Limit> limits = new ArrayList<Limit>();
   private ArrayList<Payment> tbc = new ArrayList<Payment>();
   private ArrayList<Payment> readyToHost = new ArrayList<Payment>();

   public void paymentReview(Payment... payments) {
      for (Payment payment : payments) {
         paymentReview(payment);
      }
   }

   public void paymentReview(Payment payment) {
      boolean isCorrect = true;

      if (payment == null) {
         throw new IllegalArgumentException("An invalid payment. May not be null");
      }

      for (Limit limit : limits) {
         if (!isCorrectPayment(limit, payment)) {
            isCorrect = false;
            break;
         }
      }

      if (isCorrect) {
         if (readyToHost.size() > 0) {
            int last = readyToHost.size() - 1;
            if (readyToHost.get(last).getCalendar().after(payment.getCalendar()))
               orderedInsert(payment);
            else
               readyToHost.add(payment);
         } else readyToHost.add(payment);
      } else {
         tbc.add(payment);
      }
   }

   private void orderedInsert(Payment payment) {
      int last = readyToHost.size() - 1;
      int desiredPosition = last;

      for (int index = last; index >= 0; index--) {
         if (readyToHost.get(index).getCalendar().after(payment.getCalendar())) {
            desiredPosition = index;
         } else {
            break;
         }
      }

      readyToHost.add(desiredPosition, payment);
   }

   public ArrayList<Limit> getLimits() {
      return limits;
   }

   public ArrayList<Payment> getTbc() {
      return tbc;
   }

   public ArrayList<Payment> getReadyToHost() {
      return readyToHost;
   }

   private boolean isCorrectPayment(Limit limit, Payment payment) {
      int total = 0;
      int count = 1;
      boolean checkIt = true;

      boolean isInstalledPeriod = !limit.getTimeOfDayStart().equals(limit.getTimeOfDayEnd());

      if (isInstalledPeriod) {
         Boolean flag = amountCheck(limit, payment);
         if (flag != null) return flag;
      }

      for (Payment hostPayment : readyToHost) {
         checkIt = clientCheck(limit, payment, hostPayment);

         if (checkIt) {
            checkIt = serviceCheck(limit, payment, hostPayment);
            if (checkIt) {
               checkIt = timeLapseCheck(limit, payment, hostPayment);
               if (checkIt && isInstalledPeriod) {
                  checkIt = timeOfDayCheck(limit, hostPayment);
               }
            }
         }

         if (checkIt) {
            total += hostPayment.getSum();
            count++;
         }
      }

      if (checkIt) {
         if (limit.getTotal() > 0 && limit.getTotal() < (total + payment.getSum()))
            return false;

         if (limit.getCount() > 0 && limit.getCount() < count)
            return false;
      }

      return true;
   }

   private Boolean amountCheck(Limit limit, Payment payment) {
      if (limit.getTimeOfDayStart().after(payment.getCalendar()) ||
              payment.getCalendar().after(limit.getTimeOfDayEnd()))
         return true;
      else if (limit.getTotal() > 0 && payment.getSum() > limit.getTotal())
         return false;
      return null;
   }

   private boolean clientCheck(Limit limit, Payment payment, Payment hostPayment) {
      boolean checkIt = true;

      if (limit.isOneClient())
         if (hostPayment.getClient() != payment.getClient())
            checkIt = false;

      return checkIt;
   }

   private boolean serviceCheck(Limit limit, Payment payment, Payment hostPayment) {
      boolean checkIt = true;

      if (limit.isForOneService()) {
         if (hostPayment.getService() != payment.getService())
            checkIt = false;
      }

      return checkIt;
   }

   private boolean timeLapseCheck(Limit limit, Payment payment, Payment hostPayment) {
      boolean checkIt = true;

      if (limit.getTimeLapse() > 0) {
         long timeOfHostPayment = hostPayment.getCalendar().getTimeInMillis();
         long timeOfNewPayment = payment.getCalendar().getTimeInMillis();

         if (timeOfNewPayment - timeOfHostPayment > limit.getTimeLapse()) {
            checkIt = false;
         }
      }
      return checkIt;
   }

   private boolean timeOfDayCheck(Limit limit, Payment hostPayment) {
      boolean checkIt = true;
      if (limit.getTimeOfDayStart().after(hostPayment.getCalendar()) ||
              hostPayment.getCalendar().after(limit.getTimeOfDayEnd()))
         checkIt = false;
      return checkIt;
   }

   public void addLimit(Limit... limits) {
      for (Limit limit : limits) {
         addLimit(limit);
      }
   }

   public void addLimit(Limit limit) {
      if (limit != null) {
         this.limits.add(limit);
      } else
         throw new IllegalArgumentException("An invalid limit. May not be null");
   }

   public void deleteLimit(int index) {
      if (this.limits.size() < index)
         this.limits.remove(index);
      else throw new IllegalArgumentException("An invalid index of limit. 0 <= index <= " + limits.size());
   }
}