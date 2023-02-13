public class Counter {
  private int counter = 0;

  // increment counter by one
  public void run() {
     int cnt = counter; 
     counter = cnt + 1; 
  }                     

  // current value of counter
  public int counter() {
     return counter;
  }
}
