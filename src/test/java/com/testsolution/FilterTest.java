package com.testsolution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class FilterTest {
   private static Filter filter = new Filter();

   private static Service service1 = new Service("Пополнение счёта мобильного телефона", ServiceType.COMMUNICATION);
   private static Service service2 = new Service("Оплата продуктов в магазине", ServiceType.PRODUCT);
   private static Service service3 = new Service("Оплата строительных материалов в магазине", ServiceType.PRODUCT);
   private static Service service4 = new Service("Стрижка", ServiceType.SERVICE);
   private static Service service5 = new Service("Снять деньги в банкомате", ServiceType.CASH);

   private static Client lenta = new Client("Лента", "12345678900");
   private static Client karauta = new Client("K-rauta", "123456789000");
   private static Client agat = new Client("парикмахерская \"Агат\"", "12345");
   private static Client bank = new Client("Банк", "123456");

   @Test(timeout = 10)
   public void test_add_limits() {
      Assert.assertEquals(filter.getLimits().size(), 7);
   }


   @Test(timeout = 10)
   public void test_edit_limit() {
      filter.getLimits().get(3).setTotal(300.0f);
      Payment payment = new Payment(lenta, service2, 350);
      filter.paymentReview(payment);
      Assert.assertEquals(filter.getTbc().get(0), payment);
      //Assert.assertEquals(filter.getTbc().get(0), payment);
   }


   @Test(timeout = 10)
   public void test_add_correct_payment(){
      Payment payment = new Payment(lenta, service2, 350);
      filter.paymentReview(payment);

      Assert.assertEquals(filter.getReadyToHost().get(0), payment);
   }

   @Test(timeout = 100)
   public void test_add_invalid_payment(){
      Payment payment = new Payment(lenta, service2, 2001); //не удовлетворяет лимиту 3
      filter.paymentReview(payment);
      System.out.println(filter.getLimits().get(0).getTimeOfDayStart().getTime());
      System.out.println(filter.getLimits().get(0).getTimeOfDayEnd().getTime());

      Assert.assertEquals(filter.getTbc().get(0), payment);
   }

   @Test (timeout = 100)
   public void test_add_invalid_in_night(){
      Payment payment = new Payment(lenta, service2, 1009);
      Calendar calendar = new GregorianCalendar();
      calendar.set(Calendar.HOUR_OF_DAY, 23);
      payment.setCalendar(calendar);
      filter.paymentReview(payment);

      Assert.assertEquals(filter.getTbc().get(0), payment);
   }


   @Test(timeout = 100)
   public void test_add_invalid_and_correct_payment(){
      Payment payment = new Payment(karauta, service3, 1501);
      Payment payment1 = new Payment(lenta, service3, 1500); // не утовлетворяет лимиту 4 (т.к. один тип услуги)
      filter.paymentReview(payment);
      filter.paymentReview(payment1);
      Assert.assertEquals(payment, filter.getReadyToHost().get(0));
      Assert.assertEquals(payment1, filter.getTbc().get(0));
   }

   @Test (timeout = 100)
   public void test_add_11_payments_in_favor_of_one_client(){
      Payment payment = new Payment(karauta, service3, 101);

      for (int i = 0; i < 10; i++) {
         filter.paymentReview(payment);
      }
      Payment payment1 = new Payment(karauta, service3, 200);

      filter.paymentReview(payment1);

      Assert.assertEquals(10, filter.getReadyToHost().size());
      Assert.assertEquals(filter.getTbc().get(0), payment1);
   }

   @Before
   public void setUp(){
      filter.getLimits().clear();
      filter.getTbc().clear();
      filter.getReadyToHost().clear();
    //1. Не более 5000 руб. днем с 9:00 утра до 23:00 за одну услугу(*)
      Limit limit1 = new Limit();
      limit1.setTotal(5000.0f);
      limit1.setTimeOfDayStart(9, 0, 0);
      limit1.setTimeOfDayEnd(23, 0, 0);
      limit1.setForOneService(true);

      //2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*)
      Limit limit2 = new Limit();
      limit2.setTotal(1000.0f);
      limit2.setTimeOfDayStart(23, 0, 0);
      limit2.setTimeOfDayEnd(9, 0, 0);
      limit2.setForOneService(true);

      //3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      Limit limit3 = new Limit();
      limit3.setTotal(2000.0f);
      limit3.setTimeOfDayStart(0, 0, 0);
      limit3.setTimeOfDayEnd(23, 59, 59);
      limit3.setOneClient(true);
      limit3.setForOneService(true);

      //4. Не более 3000 руб. в течение одного часа за одну услугу(*)
      Limit limit4 = new Limit();
      limit4.setTotal(3000.0f);
      limit4.setTimeLapse(60);
      limit4.setForOneService(true);

      //5. Не более 20 одинаковых платежей(**) в сутки
      Limit limit5 = new Limit();
      limit5.setCount(20);
      limit5.setOneClient(true);
      limit5.setForOneService(true);
      limit5.setTimeOfDayStart(0, 0, 0);
      limit5.setTimeOfDayEnd(23, 59, 59);

      //6. Не более 40 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*)
      Limit limit6 = new Limit();
      limit6.setCount(40);
      limit6.setTotal(4000.0f);
      limit6.setTimeOfDayStart(10, 0, 0);
      limit6.setTimeOfDayEnd(17, 0, 0);

      //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
      Limit limit7 = new Limit();
      limit7.setCount(10);
      limit7.setTotal(3000.0f);
      limit7.setTimeLapse(120);
      limit7.setOneClient(true);

      filter.addLimit(limit1, limit2, limit3, limit4, limit5, limit6, limit7);
   }
}
