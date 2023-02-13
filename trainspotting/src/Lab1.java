import java.util.*;
import java.util.concurrent.Semaphore;
import TSim.*;

public class Lab1 {

  private List<Rail> railsection = new ArrayList<>();
  Lab1.Rail onewayright = new Rail("onewayright");
  Lab1.Rail onewayleft = new Rail("onewayleft");
  Lab1.Rail intersection = new Rail("intersection");
  Lab1.Rail twotop = new Rail("twotop");
  Lab1.Rail twobot = new Rail("twobot");
  Lab1.Rail onetop = new Rail("twotop");
  Lab1.Rail onebot = new Rail("twobot");
  Lab1.Rail railstopbot = new Rail("railstopbot");

  public Lab1() {

  }

  private void init() {
    railsection.add(onewayright);
    railsection.add(onewayleft);
    railsection.add(intersection);
    railsection.add(twotop);
    railsection.add(twobot);
    railsection.add(onetop);
    railsection.add(onebot);
    onewayright.add(18, 7);
    onewayright.add(18, 9);
    onewayleft.add(1, 10);
    onewayleft.add(2, 11);
    onewayleft.add(3, 9);
    intersection.add(6, 6);
    intersection.add(10, 7);
    intersection.add(9, 5);
    intersection.add(10, 8);
    twotop.add(13, 9);
    twotop.add(6, 9);
    twobot.add(6, 10);
    twobot.add(13, 10);
    onetop.add(16, 8);
    onetop.add(16, 7);
    onebot.add(9, 7);
    onebot.add(9, 8);

  }

  public Rail findRail(int x, int y) {

    for (Rail r : railsection) {
      for (Rail.Sensor sensor : r.sensors) {
        if (sensor.xPos == x && y == sensor.yPos) {
          return r;

        }

      }

    }
    System.out.println(x + " " + y);
    return null;
  }

  class Rail extends Semaphore {

    public final String string;
    // public final List<Sensor> sensorlist;
    public List<Sensor> sensors = new ArrayList<Sensor>();

    class Sensor {
      int xPos;
      int yPos;

      public Sensor(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
      }

    }

    public Rail(String string) {
      super(1, true);
      this.string = string;

    }

    public void add(int x, int y) {
      this.sensors.add(new Sensor(x, y));
    }

    @Override // eq kollar på pekar inte på stringsssss
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (o == null) {
        return false;
      }
      if (!(o instanceof Rail)) {
        return false;
      }
      Rail r = (Rail) o;
      if (this.string.equals(r.string)) {
        return true;
      }
      return false;
    }

  }

  class Train extends Thread {

    class Switch {

      int xPos;
      int yPos;
      int switchDir;

      public Switch(int xPos, int yPos, int switchDir) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.switchDir = switchDir;

      }

    }

    private int trainID;
    private int Speed;
    private boolean up;
    private TSimInterface tsi = TSimInterface.getInstance();

    public Train(int trainID, int Speed, boolean up) {
      this.trainID = trainID;
      this.Speed = Speed;
      this.up = up;
    }

    @Override

    public void run() {

      try {
        tsi.setSpeed(trainID, Speed);
        while (true) {
          SensorEvent currentsensor = tsi.getSensor(trainID);
          int x = currentsensor.getXpos();
          int y = currentsensor.getYpos();

          Rail currentrail = findRail(x, y);
          // System.out.print(currentrail + "cringe");
          if (currentsensor.getStatus() == 0x01) {
            // System.out.print(currentsensor);
            if (currentrail == null) {
              System.out.println("rail is null");
            } else {
              System.out.println(currentrail.string);
            }

            if (currentrail == null) {

              // Dvs sensorer som inte är kopplade till semaforer/rails
              if (x == 14 && y > 10 && this.up == false) {
                tsi.setSpeed(trainID, 0);
                Thread.sleep(1000 + (20 * Speed));
                Speed = -Speed;
                tsi.setSpeed(trainID, Speed);
                this.up = true;
              }

              if (x == 14 && y < 10 && this.up == true) {
                tsi.setSpeed(trainID, 0);
                Thread.sleep(1000 + (20 * Speed));
                Speed = -Speed;
                tsi.setSpeed(trainID, Speed);
                this.up = false;

              }
              if ((x == 6 && y == 11 && this.up == true)) {
                tsi.setSpeed(trainID, 0);
                onewayleft.acquire();
                tsi.setSpeed(trainID, Speed);
                tsi.setSwitch(3, 11, 1);
                railstopbot.release();
              }
              if (x == 6 && y == 13 && this.up == true) {

                tsi.setSpeed(trainID, 0);
                onewayleft.acquire();
                tsi.setSpeed(trainID, Speed);
                tsi.setSwitch(3, 11, 0);

              }

              if (x == 16 && y == 9 && this.up == true) {

                twobot.release();
                twotop.release();

              }

              continue;
            }
            if ((x == 6 && y == 6 && this.up == false) ||
                (x == 9 && y == 5 && this.up == false)) {
              tsi.setSpeed(trainID, 0);
              System.out.println("fick10");
              intersection.acquire();
              tsi.setSpeed(trainID, Speed);

            }
            if (currentrail.equals(intersection)) {
              if (this.up == false) {
                if (x == 10 && y == 7) {
                  intersection.release();
                  System.out.println("fick1");
                  onetop.acquire();
                }
                if (x == 10 && y == 8) {
                  intersection.release();
                  System.out.println("fick2");
                  onetop.acquire();
                }
              } else {
                if ((x == 10 && y == 7) && (x == 10 && y == 8)) {
                  onebot.release();
                  onetop.release();
                }

                if (x == 6 && y == 6) {
                  intersection.release();
                  System.out.println("fick3");
                }

                if (x == 9 && y == 5) {
                  intersection.release();
                  System.out.println("fick4");
                }

              }

            }
            // System.out.print(currentrail + "cringe");

            if (currentrail.equals(onewayleft)) {

              if (this.up == true) {
                if (twotop.tryAcquire() && x == 1 && y == 9) {
                  tsi.setSwitch(4, 9, 0);

                }
                if (twobot.tryAcquire() && x == 1 && y == 10) {
                  tsi.setSwitch(4, 9, 1);

                }
              } else {
                if (x == 3 && y == 9) {
                  twobot.release();
                  twotop.release();
                }
                if (x == 2 && y == 11 && railstopbot.tryAcquire()) {
                  tsi.setSwitch(3, 11, 1);
                } else {
                  tsi.setSwitch(3, 11, 0);
                }

              }
            }

            if (currentrail.equals(twobot) || currentrail.equals(twotop)) {

              if (this.up == true) {

                if (x == 6 && y == 10 || x == 6 && y == 9) {
                  onewayleft.release();

                }
                if (x == 13 && y == 10) {
                  tsi.setSpeed(trainID, 0);
                  onewayright.acquire();
                  tsi.setSwitch(15, 9, 1);
                  tsi.setSpeed(trainID, Speed);

                }
                if (x == 13 && y == 9) {
                  System.out.println("fuckshitaaaaaaaaaaaaaaaaaaaaaas");
                  System.out.println(Speed);
                  tsi.setSpeed(trainID, 0);
                  onewayright.acquire();
                  tsi.setSwitch(15, 9, 0);
                  tsi.setSpeed(trainID, 0);
                }

              }

              else {
                onewayright.release();
                if (x == 13 && y == 10 || x == 13 && y == 9) {
                  onewayright.release();
                }
                if (x == 6 && y == 10 || x == 6 && y == 9) {
                  tsi.setSpeed(trainID, 0);
                  onewayright.acquire();
                  tsi.setSwitch(4, 9, 0);
                  tsi.setSpeed(trainID, Speed);
                }

              }
            }

            if (currentrail.equals(onewayright)) {
              if (this.up == true) {

                if (x == 18 && y == 7) {
                  System.out.println("jävla nördjävel2");
                  if (onebot.tryAcquire()) {
                    System.out.println("jävla nördjävel");
                    tsi.setSwitch(17, 7, 1);
                  } else {
                    System.out.println("jävla nördjävel3");
                    onetop.acquire();
                    System.out.println("jävla nördjävel4");
                    tsi.setSwitch(17, 7, 0);

                  }
                }
              } else {
                if (x == 18 && y == 7) {
                  onetop.release();
                  onebot.release();

                }
                if (twotop.tryAcquire() && x == 18 && y == 9) {
                  tsi.setSwitch(15, 9, 1);

                } else {
                  if (x == 18 && y == 9) {
                    System.out.println("oi1");
                    twobot.acquire();
                    tsi.setSwitch(15, 9, 0);
                    System.out.println("oi");
                  }
                }

              }
            }

            if ((currentrail.equals(onetop) && this.up == true) || (currentrail.equals(onebot) && this.up == true)) {

              if (x == 16 && y == 7 || x == 16 && y == 8) {
                onewayright.release();
              }

              if (x == 10 && y == 7 || x == 10 && y == 8) {
                tsi.setSpeed(trainID, 0);
                System.out.println("fick10");
                intersection.acquire();
                tsi.setSpeed(trainID, Speed);
              }

            }

            if (currentrail.equals(onetop) && this.up == false) {
              System.out.println("onetop and 16");
              if (x == 16 && y == 7) {
                tsi.setSpeed(trainID, 0);
                onewayright.acquire();
                tsi.setSpeed(trainID, Speed);
                tsi.setSwitch(17, 7, 0);
              }

            }

            if (currentrail.equals(onebot) && this.up == false) {
              if (x == 16 && y == 8) {
                tsi.setSpeed(trainID, 0);
                onewayright.acquire();
                tsi.setSpeed(trainID, Speed);
                tsi.setSwitch(15, 9, 1);
              }

            }
          }
        }
      } catch (

      Exception e) {
        e.printStackTrace(); // or only e.getMessage() for the error
        System.exit(1);
      }

    }

  }

  public Lab1(int speed1, int speed2) {
    init();
    TSimInterface tsi = TSimInterface.getInstance();
    Train train1 = new Train(1, 15, false);
    Train train2 = new Train(2, 15, true);
    /*
     * for (Rail r : railsection) {
     * for (Rail.Sensor sensor : r.sensors) {
     * System.out.println(r.string + " " + sensor.xPos + " " + sensor.yPos);
     * 
     * }
     * 
     * }
     */
    train1.start();
    train2.start();

  }

}
