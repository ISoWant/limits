package com.testsolution;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class Filter {
   private ArrayList<Limit> limits = new ArrayList<Limit>();          // Массив заданных ограничений
   private ArrayList<Payment> tbc = new ArrayList<Payment>();           // Массив платежей в статусе "требует подтверждения"
   private ArrayList<Payment> readyToHost = new ArrayList<Payment>();   // Массив платежей в статусе "готов к проведению"

   public ArrayList<Limit> getLimits() {
      return limits;
   }

   public ArrayList<Payment> getTbc() {
      return tbc;
   }

   public ArrayList<Payment> getReadyToHost() {
      return readyToHost;
   }

   public void paymentReview(Payment... payments) {
      for (Payment payment : payments) {
         paymentReview(payment);
      }
   }

   public void paymentReview(Payment payment) {       // Проверка поступивших в систему платежей
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

   private void orderedInsert(Payment payment) {      // Все платежи в readyToHost отсортированы по дате.
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

   private boolean isCorrectPayment(Limit limit, Payment payment) {
      int total = 0;
      int count = 1;
      boolean checkIt = true;

      boolean isInstalledPeriod = !limit.getTimeOfDayStart().equals(limit.getTimeOfDayEnd());

      if (isInstalledPeriod) {
         Boolean flag = amountCheck(limit, payment); // Возможно изначально сумма платежа больше допустимой в этот период.
         if (flag != null) return flag;
      }

      for (Payment hostPayment : readyToHost) {
         checkIt = clientCheck(limit, payment, hostPayment);      // Если ограничение проверяет на клиента, то выясним один ли клиент

         if (checkIt) {
            checkIt = serviceCheck(limit, payment, hostPayment);  // Если ограничение проверяет на услугу, выясним одна ли услуга
            if (checkIt) {
               checkIt = timeLapseCheck(limit, payment, hostPayment); //Если в лимите есть ограничение по отрезку времени, то в этом ли диапозоне платёж из списка?
               if (checkIt && isInstalledPeriod) {
                  if (limit.getTimeOfDayEnd() == LocalTime.MAX && limit.getTimeOfDayStart() == LocalTime.MIN) { // В лимите установлена проверка суток?
                     if (!isForOneDay(payment, hostPayment)) // В течение одних суток были проведены платежи или нет?
                        checkIt = false;
                  } else
                     checkIt = timeOfDayCheck(limit, hostPayment); // Попадает ли платёж из списка во временные рамки установленные лимитом
               }
            }
         }

         if (checkIt) { // Если все проверки пройдены, т.е. платёж из списка может повлиять на решение о текущем платеже
            total += hostPayment.getSum(); //то добавляем его к итоговой сумме
            count++;                       //и в количество платежей
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
      LocalTime paymentTime = LocalTime.of(payment.getCalendar().get(Calendar.HOUR_OF_DAY),
              payment.getCalendar().get(Calendar.MINUTE), payment.getCalendar().get(Calendar.SECOND));

      if (limit.getTimeOfDayEnd().isAfter(limit.getTimeOfDayStart())) {
         if (paymentTime.isAfter(limit.getTimeOfDayEnd()) ||
                 paymentTime.isBefore(limit.getTimeOfDayStart()))
            return true;
         else if (limit.getTotal() > 0 && payment.getSum() > limit.getTotal())
            return false;
      } else {
         if (paymentTime.isAfter(limit.getTimeOfDayStart()) ||
                 paymentTime.isBefore(limit.getTimeOfDayEnd())) {
            if (limit.getTotal() > 0 && payment.getSum() > limit.getTotal())
               return false;
         } else return true;
      }

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
      LocalTime paymentTime = LocalTime.of(hostPayment.getCalendar().get(Calendar.HOUR_OF_DAY),
              hostPayment.getCalendar().get(Calendar.MINUTE), hostPayment.getCalendar().get(Calendar.SECOND));

      boolean checkIt = true;

      if (limit.getTimeOfDayEnd().isAfter(limit.getTimeOfDayStart())) {
         if (paymentTime.isAfter(limit.getTimeOfDayEnd()) ||
                 paymentTime.isBefore(limit.getTimeOfDayStart()))
            checkIt = false;
      } else {
         if (!(paymentTime.isAfter(limit.getTimeOfDayStart()) ||
                 paymentTime.isBefore(limit.getTimeOfDayEnd())))
            checkIt = false;
      }

      return checkIt;
   }

   private boolean isForOneDay(Payment payment, Payment hostPayment) {
      Calendar p = payment.getCalendar();
      Calendar h = hostPayment.getCalendar();

      if (p.get(Calendar.YEAR) == h.get(Calendar.YEAR) &&
              p.get(Calendar.MONTH) == h.get(Calendar.MONTH) &&
              p.get(Calendar.DATE) == h.get(Calendar.DATE)) {
         return true;
      }

      return false;
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
      if (this.limits.size() > index)
         this.limits.remove(index);
      else throw new IllegalArgumentException("An invalid index (" + index + ") of limit. 0 <= index <= " + limits.size());
   }
}