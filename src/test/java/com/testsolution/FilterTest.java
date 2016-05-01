package com.testsolution;

import org.junit.*;
import org.junit.rules.Timeout;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

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
      Payment payment = new Payment(lenta, service2, 350);
      filter.paymentReview(payment);
      assertEquals(payment, filter.getTbc().get(0));
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
   public void test_add_invalid_payment() {
      Payment payment = new Payment(lenta, service2, 2001); //не уд. лимиту 3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      filter.paymentReview(payment);

      assertEquals(payment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_invalid_in_night() {
      Payment payment = new Payment(lenta, service2, 1007); //Не уд. лимиту 2. Не более 1000 руб. ночью с 23:00 до 9:00 утра за одну услугу(*)
      Calendar calendar = new GregorianCalendar();
      calendar.set(Calendar.HOUR_OF_DAY, 23);
      payment.setCalendar(calendar);
      filter.paymentReview(payment);

      assertEquals(payment, filter.getTbc().get(0));
   }

   @Test
   public void test_add_correct_payment_at_different_days() {
      Payment payment = new Payment(bank, service1, 1999);
      Payment payment1 = new Payment(bank, service1, 1999);

      filter.paymentReview(payment, payment1);// payment1 не уд. лимиту 3. Не более 2000 руб. в сутки по одинаковым платежам(**)

      Calendar calendar = new GregorianCalendar();
      calendar.set(Calendar.DATE, (payment.getCalendar().get(Calendar.DATE) % 10) + 1);
      payment1.setCalendar(calendar);

      filter.paymentReview(payment1); //теперь платёжи происходят в различные дни, а значит ничто не мешает его провести

      assertEquals(payment1, filter.getTbc().get(0));
      assertEquals(payment1, filter.getReadyToHost().get(1));
   }


   @Test
   public void test_add_invalid_and_correct_payment() {
      Payment payment = new Payment(karauta, service3, 1501);
      Payment payment1 = new Payment(lenta, service3, 1500); // не уд. лимиту 4. Не более 3000 руб. в течение одного часа за одну услугу(*)
      filter.paymentReview(payment, payment1);
      assertEquals(payment, filter.getReadyToHost().get(0));
      assertEquals(payment1, filter.getTbc().get(0));
   }

   @Test
   public void test_add_invalid_and_2_correct_payment() {
      Payment payment = new Payment(karauta, service3, 1501);
      Payment payment1 = new Payment(lenta, service3, 1500); // не уд. лимиту 4. Не более 3000 руб. в течение одного часа за одну услугу(*)
      Payment payment2 = new Payment(lenta, service3, 1450);
      filter.paymentReview(payment, payment1, payment2);
      assertEquals(payment2, filter.getReadyToHost().get(1));
      assertEquals(payment1, filter.getTbc().get(0));
   }

   @Test
   public void test_add_11_payments_in_favor_of_one_client() {
      Payment payment = new Payment(karauta, service3, 101);

      for (int i = 0; i < 10; i++) {
         filter.paymentReview(payment);
      }

      Payment payment1 = new Payment(karauta, service3, 200);

      filter.paymentReview(payment1);

      assertEquals(10, filter.getReadyToHost().size());
      assertEquals(payment1, filter.getTbc().get(0));  //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
   }

   @Test
   public void test_add_20_correct_and_20_invalid_payments() {
      Limit limit5 = filter.getLimits().get(4); //5. Не более 20 одинаковых платежей(**) в сутки
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
   public void test_add_payments_in_favor_of_one_client() {
      filter.deleteLimit(2);  //3. Не более 2000 руб. в сутки по одинаковым платежам(**)
      filter.deleteLimit(3);  //4. Не более 3000 руб. в течение одного часа за одну услугу(*)

      Payment payment = new Payment(karauta, service3, 900);

      filter.paymentReview(payment, payment, payment, payment);

      assertEquals(3, filter.getReadyToHost().size());
      assertEquals(payment, filter.getTbc().get(0)); //7. Не более 10 платежей не более чем на 3000 руб.(***) в течение двух часов в пользу одного клиента
   }

   @Test
   public void test_add_40_correct_and_5_invalid_payments() {
      Limit limit6 = filter.getLimits().get(5); //6. Не более 40 платежей не более чем на 4000 руб.(***) с 10:00 до 17:00 за одну услугу(*)
      filter.getLimits().clear();
      filter.addLimit(limit6);

      Payment payment = new Payment(karauta, service3, 10);

      for (int i = 0; i < 45; i++) {
         filter.paymentReview(payment);
      }

      assertEquals(40, filter.getReadyToHost().size());
      assertEquals(5, filter.getTbc().size());
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
