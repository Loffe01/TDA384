public class SequentialCount {
  public static
  void main(String[] args) {
      Counter counter = new Counter();
      counter.run(); // increment once
      counter.run(); // increment twice
      // print final value of counter
      System.out.println(
          counter.counter());
  }
}
