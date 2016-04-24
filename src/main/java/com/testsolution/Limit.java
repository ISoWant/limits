package com.testsolution;


import java.util.Calendar;

public class Limit {
   private float sum;
   private boolean onAccountOfOneClient;
   private float total;
   private int timeLapse;
   private Calendar timeOfDayStart;
   private Calendar timeOfDayEnd;


   public void setSum(float sum){
      if (sum > 0)
         this.sum = sum;
      else throw new IllegalArgumentException();
   }

   public void setOnAccountOfOneClient(boolean onAccountOfOneClient) {
      this.onAccountOfOneClient = onAccountOfOneClient;
   }

   public void setTotal(float total) {
      if (total > 0)
         this.total = total;
      else throw new IllegalArgumentException();
   }

   public void setTimeLapse(int timeLapse){
      if (total > 0 && total <= Integer.MAX_VALUE)
         this.timeLapse = timeLapse;
      else throw new IllegalArgumentException();
   }

   public void setTimeOfDayStart(int hourOfDay, int minute){
      if (hourOfDay >= 0 && hourOfDay < 24 || minute >= 0 && minute <= 60) {
            this.timeOfDayStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            this.timeOfDayStart.set(Calendar.MINUTE, minute);
         } else throw new IllegalArgumentException();
   }

   public void setTimeOfDayEnd(int hourOfDay, int minute) {
      if (hourOfDay >= 0 && hourOfDay < 24 || minute >= 0 && minute <= 60) {
            this.timeOfDayEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
            this.timeOfDayEnd.set(Calendar.MINUTE, minute);
      } else throw new IllegalArgumentException();
   }

   public float getSum() {
      return sum;
   }

   public boolean isOnAccountOfOneClient() {
      return onAccountOfOneClient;
   }

   public float getTotal() {
      return total;
   }

   public int getTimeLapse() {
      return timeLapse;
   }

   public Calendar getTimeOfDayStart() {
      return timeOfDayStart;
   }

   public Calendar getTimeOfDayEnd() {
      return timeOfDayEnd;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this)
         return true;

      if (obj == null)
         return false;

      if (obj.getClass() != this.getClass())
         return false;

      Limit limit = (Limit) obj;

      if (limit.getTimeOfDayStart() == null || limit.getTimeOfDayEnd() == null)
         return false;

      boolean result = limit.getSum() == this.getSum();

      result = result && (limit.isOnAccountOfOneClient() == this.onAccountOfOneClient);
      result = result && (limit.total == this.total);
      result = result && (limit.getTimeLapse() == this.timeLapse);
      result = result && (limit.getTimeOfDayStart() == this.timeOfDayStart);
      result = result && (limit.getTimeOfDayEnd() == this.timeOfDayEnd);

      return result;
   }

   @Override
   public int hashCode() {
      if (this == null)
         return 0;

      if (this.timeOfDayStart == null || this.timeOfDayEnd == null)
         return 0;

      return (int) this.sum + (int) this.total + this.timeLapse +
              this.timeOfDayStart.get(Calendar.HOUR_OF_DAY) + this.timeOfDayStart.get(Calendar.MINUTE) +
              this.timeOfDayEnd.get(Calendar.HOUR_OF_DAY) + this.timeOfDayEnd.get(Calendar.MINUTE);
   }
}