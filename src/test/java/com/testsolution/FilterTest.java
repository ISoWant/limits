package com.testsolution;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.time.LocalTime;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

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

   @Rule
   public final Timeout timeout = new Timeout(500);

   @Test
   public void test_add_all_limits() {    //Все 7 лимитов добовляю в @Before
      assertEquals(filter.getLimits().size(), 7);  //                public void setUp()
   }


   @Test
   public void test_edit_limit() {
      filter.getLimits().get(3).setTotal(300.0f); //4. Не более 3000 (-> 300) руб. в течение одного часа за одну услугу(*)
      Payment invalidPayment = new Payment(lenta, service2, 350);
      filter.paymentReview(invalidPayment);
      assertEquals(invalidPayment, filter.getTbc().get(0));
   }


   @Test
   public void test_add_correct_payment() {
      Payment payment = new Payment(lenta, service2, 350); //Данный платёж не противоречит ни одному из 7 заданных лимитов
      filter.paymentReview(payment);

      assertEquals(payment, filter.getReadyToHost().get(0));
   }

   @Test
   public void test_add_correct_payments() {
      Payment payment = new Payment(lenta, service2, 256); //Данные платёж не противоречит ни одному из 7 заданных лимитов
      Payment payment1 = new Payment(karauta, service3, 738);
      Payment payment2 = new Payment(agat, service4, 575);
      Payment payment3 = new Payment(bank, service1, 340);
      Payment payment4 = new Payment(bank, service5, 1000);

      filter.paymentReview(payment, payment1, payment2, payment3, payment4);

      assertEquals(5, filter.getReadyToHost().size());
   }

   @Test
   public void test_add_correct_payment_at_different_days() {
      Payment payment = new Payment(bank, service1, 1999);
      Payment payment1 = new Payment(bank, service1, 1999);

      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 12); //Чтобы не попасть в лими 2
      payment1.getCalendar().set(Calendar.HOUR_OF_DAY, 11);
      filter.paymentReview(payment, payment1);// payment1 не уд. лимиту 3. Не более 2000 руб. в сутки по одинаковым платежам(**)

      payment1.getCalendar().set(Calendar.DATE, (payment.getCalendar().get(Calendar.DATE) % 10) + 1);


      filter.paymentReview(payment1); //теперь платёжи происходят в различные дни, а значит ничто не мешает его провести

      assertEquals(payment1, filter.getTbc().get(0));
      assertEquals(payment1, filter.getReadyToHost().get(1));
   }

   @Test
   public void test_add_invalid_and_correct_payment() {
      Payment payment = new Payment(karauta, service3, 1501);
      Payment invalidPayment = new Payment(lenta, service3, 1500); // не уд. лимиту 4. Не более 3000 руб. в течение одного часа за одну услугу(*)

      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 12); //Чтобы не попасть в лимит 2
      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 12); //Чтобы не попасть в лимит 2 и навярняка в один час

      filter.paymentReview(payment, invalidPayment);
      assertEquals(payment, filter.getReadyToHost().get(0));
      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_invalid_and_2_correct_payment() {
      Payment payment = new Payment(karauta, service3, 1501);
      Payment invalidPayment = new Payment(lenta, service3, 1500); // не уд. лимиту 4. Не более 3000 руб. в течение одного часа за одну услугу(*)
      Payment payment2 = new Payment(lenta, service3, 1450);

      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 10); //Чтобы не попасть в лимит 2
      invalidPayment.getCalendar().set(Calendar.HOUR_OF_DAY, 10);//И точно в течение одного часа
      payment2.getCalendar().set(Calendar.HOUR_OF_DAY, 10);


      filter.paymentReview(payment, invalidPayment, payment2);
      assertEquals(payment2, filter.getReadyToHost().get(1));
      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_11_payments_in_favor_of_one_client() {
      Payment payment = new Payment(karauta, service3, 101);
      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 12); //Чтобы обойти лимит 2


      for (int i = 0; i < 10; i++) {
         filter.paymentReview(payment);
      }

      Payment invalidPayment = new Payment(karauta, service3, 200);
      invalidPayment.getCalendar().set(Calendar.HOUR_OF_DAY, 12);


      filter.paymentReview(invalidPayment);

      assertEquals(10, filter.getReadyToHost().size());
      assertEquals(invalidPayment, filter.getTbc().get(0));  //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
   }

   @Test
   public void test_add_payments_in_favor_of_one_client() {
      filter.deleteLimit(2);  //3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      filter.deleteLimit(3);  //4. Не более 3000 руб. в течение одного часа за одну услугу(*)

      Payment payment = new Payment(karauta, service3, 900);
      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 11); // чтобы не попасть в лимит 2

      filter.paymentReview(payment, payment, payment, payment);

      assertEquals(3, filter.getReadyToHost().size());
      assertEquals(payment, filter.getTbc().get(0)); //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
   }

   @Test
   public void test_add_only_first_limit() {    //1. Не более 5000 руб. днем с 9:00 утра до 23:00 за одну услугу(*)
      Limit limit1 = filter.getLimits().get(0);
      filter.getLimits().clear();
      filter.addLimit(limit1);

      Payment payment = new Payment(lenta, service2, 3758);
      Payment invalidPayment = new Payment(karauta, service2, 4458);
      Payment payment1 = new Payment(karauta, service3, 2458);
      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 11);
      invalidPayment.getCalendar().set(Calendar.HOUR_OF_DAY, 19);
      payment1.getCalendar().set(Calendar.HOUR_OF_DAY, 19);

      filter.paymentReview(payment, invalidPayment, payment1);

      assertEquals(2, filter.getReadyToHost().size());
      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_only_second_limit() {//2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*)
      Limit limit2 = filter.getLimits().get(1);
      filter.getLimits().clear();
      filter.addLimit(limit2);

      Payment invalidPayment = new Payment(lenta, service2, 1007);
      invalidPayment.getCalendar().set(Calendar.HOUR_OF_DAY, 23);
      filter.paymentReview(invalidPayment);

      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_only_third_limit() { //3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      Limit limit3 = filter.getLimits().get(2);
      filter.getLimits().clear();
      filter.addLimit(limit3);

      Payment invalidPayment= new Payment(lenta, service2, 2001);
      filter.paymentReview(invalidPayment);

      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_only_forth_limit() { //4. Не более 3000 руб. в течение одного часа за одну услугу(*)
      Limit limit4 = filter.getLimits().get(3);
      filter.getLimits().clear();
      filter.addLimit(limit4);

      Payment payment = new Payment(bank, service5, 1000);
      Payment invalidPayment = new Payment(bank, service5, 2500);
      Payment payment1 = new Payment(lenta, service5, 500);

      filter.paymentReview(payment, invalidPayment, payment1);

      assertEquals(2, filter.getReadyToHost().size());
      assertEquals(invalidPayment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_only_fifth_limit() {//5. Не более 20 одинаковых платежей(**) в сутки
      Limit limit5 = filter.getLimits().get(4);
      filter.getLimits().clear();
      filter.addLimit(limit5);

      Payment payment = new Payment(agat, service4, 300);

      for (int i = 0; i < 40; i++) {
         filter.paymentReview(payment);
      }

      assertEquals(20, filter.getReadyToHost().size());
      assertEquals(20, filter.getTbc().size());
   }

   @Test
   public void test_add_only_sixth_limit() {
      Limit limit6 = filter.getLimits().get(5); //6. Не более 40 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*)
      filter.getLimits().clear();
      filter.addLimit(limit6);

      Payment payment = new Payment(karauta, service3, 10);
      payment.getCalendar().set(Calendar.HOUR_OF_DAY, 13);

      for (int i = 0; i < 45; i++) {
         filter.paymentReview(payment);
      }

      assertEquals(40, filter.getReadyToHost().size());
      assertEquals(5, filter.getTbc().size());
   }

   @Test
   public void test_add_only_seventh_limit() {//7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
      Limit limit7 = filter.getLimits().get(6);
      filter.getLimits().clear();
      filter.addLimit(limit7);

      Payment payment = new Payment(karauta, service2, 1001);
      Payment payment1 = new Payment(karauta, service3, 50);

      filter.paymentReview(payment, payment, payment);

      for (int i = 0; i < 10; i++) {
         filter.paymentReview(payment1);
      }

      assertEquals(10, filter.getReadyToHost().size());
      assertEquals(payment, filter.getReadyToHost().get(0));
      assertEquals(payment1, filter.getReadyToHost().get(3));
      assertEquals(3, filter.getTbc().size());
      assertEquals(payment, filter.getTbc().get(0));
      assertEquals(payment1, filter.getTbc().get(1));
   }

   @Before
   public void setUp() {
      //1. Не более 5000 руб. днем с 9:00 утра до 23:00 за одну услугу(*)
      Limit limit1 = new Limit();
      limit1.setTotal(5000.0f);
      limit1.setTimeOfDayStart(LocalTime.of(9, 0, 0));
      limit1.setTimeOfDayEnd(LocalTime.of(23, 0, 0));
      limit1.setForOneService(true);

      //2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*)
      Limit limit2 = new Limit();
      limit2.setTotal(1000.0f);
      limit2.setTimeOfDayStart(LocalTime.of(23, 0, 0));
      limit2.setTimeOfDayEnd(LocalTime.of(9, 0, 0));
      limit2.setForOneService(true);

      //3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      Limit limit3 = new Limit();
      limit3.setTotal(2000.0f);
      limit3.setTimeOfDayStart(LocalTime.MIN);
      limit3.setTimeOfDayEnd(LocalTime.MAX);
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
      limit5.setTimeOfDayStart(LocalTime.MIN);
      limit5.setTimeOfDayEnd(LocalTime.MAX);

      //6. Не более 40 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*)
      Limit limit6 = new Limit();
      limit6.setCount(40);
      limit6.setTotal(4000.0f);
      limit6.setTimeOfDayStart(LocalTime.of(10, 0, 0));
      limit6.setTimeOfDayEnd(LocalTime.of(17, 0, 0));

      //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
      Limit limit7 = new Limit();
      limit7.setCount(10);
      limit7.setTotal(3000.0f);
      limit7.setTimeLapse(120);
      limit7.setOneClient(true);

      filter.addLimit(limit1, limit2, limit3, limit4, limit5, limit6, limit7);
   }

   @After
   public void clear_lists() {
      filter.getLimits().clear();
      filter.getTbc().clear();
      filter.getReadyToHost().clear();
   }
}
