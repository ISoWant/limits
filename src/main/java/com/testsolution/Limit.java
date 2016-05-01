package com.testsolution;

import java.time.LocalTime;

public class Limit {
   private int count;
   private boolean onAccountOfOneClient;
   private boolean forOneService;
   private float total;
   private long timeLapse;
   private LocalTime timeOfDayStart;
   private LocalTime timeOfDayEnd;


   public Limit() {
      this.count = -1;
      this.onAccountOfOneClient = false;
      this.forOneService = false;
      this.total = -1.0f;
      this.timeLapse = -1;
      this.timeOfDayStart = LocalTime.MIN;
      this.timeOfDayEnd = LocalTime.MIN;
   }

   public void setCount(int count) {
      if (count >= 0)
         this.count = count;
      else throw new IllegalArgumentException("Incorrect count of payment. Count must be >= 0");
   }

   public void setOneClient(boolean onAccountOfOneClient) {
      this.onAccountOfOneClient = onAccountOfOneClient;
   }

   public void setForOneService(boolean forOneService) {
      this.forOneService = forOneService;
   }

   public void setTotal(float total) {
      if (total > 0)
         this.total = total;
      else throw new IllegalArgumentException("Incorrect total amount of payment. Must be > 0");
   }

   public void setTimeLapse(int minute) {
      if (minute > 0)
         this.timeLapse = minute * 1000 * 60;
      else throw new IllegalArgumentException("Incorrect time lapse of limit. Must be > 0");
   }

   public void setTimeOfDayStart(LocalTime timeOfDayStart) {
      this.timeOfDayStart = timeOfDayStart;
   }

   public void setTimeOfDayEnd(LocalTime timeOfDayEnd) {
      this.timeOfDayEnd = timeOfDayEnd;
   }

   public int getCount() {
      return count;
   }

   public boolean isOneClient() {
      return onAccountOfOneClient;
   }

   public boolean isForOneService() {
      return forOneService;
   }

   public float getTotal() {
      return total;
   }

   public long getTimeLapse() {
      return timeLapse;
   }

   public LocalTime getTimeOfDayEnd() {
      return timeOfDayEnd;
   }

   public LocalTime getTimeOfDayStart() {
      return timeOfDayStart;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Limit limit = (Limit) o;

      if (count != limit.count) return false;
      if (onAccountOfOneClient != limit.onAccountOfOneClient) return false;
      if (forOneService != limit.forOneService) return false;
      if (Float.compare(limit.total, total) != 0) return false;
      if (timeLapse != limit.timeLapse) return false;
      if (timeOfDayStart != null ? !timeOfDayStart.equals(limit.timeOfDayStart) : limit.timeOfDayStart != null)
         return false;
      return timeOfDayEnd != null ? timeOfDayEnd.equals(limit.timeOfDayEnd) : limit.timeOfDayEnd == null;

   }

   @Override
   public int hashCode() {
      int result = count;
      result = 31 * result + (onAccountOfOneClient ? 1 : 0);
      result = 31 * result + (forOneService ? 1 : 0);
      result = 31 * result + (total != +0.0f ? Float.floatToIntBits(total) : 0);
      result = 31 * result + (int) (timeLapse ^ (timeLapse >>> 32));
      result = 31 * result + (timeOfDayStart != null ? timeOfDayStart.hashCode() : 0);
      result = 31 * result + (timeOfDayEnd != null ? timeOfDayEnd.hashCode() : 0);
      return result;
   }
}