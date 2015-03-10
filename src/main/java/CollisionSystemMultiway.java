

/**
 * Compilation: javac CollisionSystem.java Execution: java CollisionSystem N (N
 * random particles) java CollisionSystem < input.txt (from a file)
 *
 * Creates N random particles and simulates their motion according to the laws
 * of elastic collisions.
 */
//import edu.princeton.cs.algs4.MinPQ;
//import edu.princeton.cs.algs4.ParticleMultiway;
import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.introcs.StdIn;
import edu.princeton.cs.introcs.StdOut;
import edu.princeton.cs.introcs.StdRandom;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CollisionSystemMultiway {
  static private final Color CLEAR_COLOR =
      //Color.decode("0x2e3436"); // Tango Aluminium 6
      Color.decode("0xeeeeec"); // Tango Aluminium 1
  //Color.decode("0xd3d7cf"); // Tango Alumninum 2
  private MultiwayHeapMinPQ<Event> pq; // the priority queue
  private double t = 0.0;        // simulation clock time
  private double hz = 0.5;       // number of redraw events per clock tick
  private ParticleMultiway[] particles;   // the array of particles

  // create a new collision system with the given set of particles
  public CollisionSystemMultiway(ParticleMultiway[] particles) {
    this.particles = particles;
  }

  // updates priority queue with all new events for particle a
  private void predict(ParticleMultiway a, double limit) {
    if (a == null) {
      return;
    }

    // particle-particle collisions
    for (int i = 0; i < particles.length; i++) {
      double dt = a.timeToHit(particles[i]);
      if (t + dt <= limit) {
        pq.insert(new Event(t + dt, a, particles[i]));
      }
    }

    // particle-wall collisions
    double dtX = a.timeToHitVerticalWall();
    double dtY = a.timeToHitHorizontalWall();
    if (t + dtX <= limit) {
      pq.insert(new Event(t + dtX, a, null));
    }
    if (t + dtY <= limit) {
      pq.insert(new Event(t + dtY, null, a));
    }
  }

  // redraw all particles
  private void redraw(double limit) {
    StdDraw.clear(CLEAR_COLOR);
    for (int i = 0; i < particles.length; i++) {
      particles[i].draw();
    }
    StdDraw.show(20);
    if (t < limit) {
      pq.insert(new Event(t + 1.0 / hz, null, null));
    }
  }

  /**
   * Event based simulation for limit seconds
   */
  public void simulate(double limit) {

    // initialize PQ with collision events and redraw event
    pq = new MultiwayHeapMinPQ<>();
    for (int i = 0; i < particles.length; i++) {
      predict(particles[i], limit);
    }
    pq.insert(new Event(0, null, null));        // redraw event

    // the main event-driven simulation loop
    while (!pq.isEmpty()) {

      // get impending event, discard if invalidated
      Event e = pq.delMin();
      if (!e.isValid()) {
        continue;
      }
      ParticleMultiway a = e.a;
      ParticleMultiway b = e.b;

      // physical collision, so update positions, and then simulation clock
      for (int i = 0; i < particles.length; i++) {
        particles[i].move(e.time - t);
      }
      t = e.time;

      // process event
      if (a != null && b != null) {
        a.bounceOff(b);              // particle-particle collision
      } else if (a != null && b == null) {
        a.bounceOffVerticalWall();   // particle-wall collision
      } else if (a == null && b != null) {
        b.bounceOffHorizontalWall(); // particle-wall collision
      } else if (a == null && b == null) {
        redraw(limit);               // redraw event
      }
      // update the priority queue with new collisions involving a or b
      predict(a, limit);
      predict(b, limit);
    }
  }

  /**
   * An event during a particle collision simulation. Each event contains the
   * time at which it will occur (assuming no supervening actions) and the
   * particles a and b involved.
   *
   * - a and b both null: redraw event - a null, b not null: collision with
   * vertical wall - a not null, b null: collision with horizontal wall - a and
   * b both not null: binary collision between a and b
   */
  private static class Event implements Comparable<Event> {
    private final double time;         // time that event is scheduled to occur
    private final ParticleMultiway a, b;       // particles involved in event, possibly null
    private final int countA, countB;  // collision counts at event creation

    // create a new event to occur at time t involving a and b
    public Event(double t, ParticleMultiway a, ParticleMultiway b) {
      this.time = t;
      this.a = a;
      this.b = b;
      if (a != null) {
        countA = a.count();
      } else {
        countA = -1;
      }
      if (b != null) {
        countB = b.count();
      } else {
        countB = -1;
      }
    }

    // compare times when two events will occur
    public int compareTo(Event that) {
      if (this.time < that.time) {
        return -1;
      } else if (this.time > that.time) {
        return +1;
      } else {
        return 0;
      }
    }

    // has any collision occurred between when event was created and now?
    public boolean isValid() {
      if (a != null && a.count() != countA) {
        return false;
      }
      if (b != null && b.count() != countB) {
        return false;
      }
      return true;
    }

  }

  /**
   * ******************************************************************************
   * Sample client
   * ******************************************************************************
   */
  public static void main(String[] args) {
    final boolean DEBUG = true;
    String[][] colorNames;
    colorNames = new String[][]{
      //      {"Tango Aluminium 1", "0xeeeeec"},
      {"Red 3", "0xe06666"},
      {"Tango Aluminium 4", "0x888a85"},
      {"Tango Plum 1", "0xad7fa8"},
      {"Tango Sky Blue 1", "0x729fcf"},
      {"Forest 3", "0x79ba95"},
      {"Butter 2", "0xedd400"},
      {"Orange 1", "0xfce5cd"}
    };
    double radii[] = {
      0.010,
      0.006,
      0.005,
      0.004,
      0.003,
      0.002,
      0.001,};
    final double density = 1.0e11;

    double masses[] = new double[radii.length];
    double harmonicMassTotal = 0.0;
    for (int i = 0; i < radii.length; i++) {
      masses[i] = density * radii[i] * radii[i] * radii[i];
      harmonicMassTotal += 1 / masses[i];
    }
    double[] probabilities = new double[radii.length],
        cumulativeDistribution = new double[radii.length];

    for (int i = 0; i < radii.length; i++) {
      probabilities[i] = (1 / masses[i]) / harmonicMassTotal;
      cumulativeDistribution[i] = i == 0 ?
                                  probabilities[i] :
                                  probabilities[i] + cumulativeDistribution[i -
                                                                            1];
      if (DEBUG) {
        StdOut.printf("%f\n", cumulativeDistribution[i]);
      }
    }
    Map<String, Color> colorMap = new HashMap<>(colorNames.length);
    Color[] colors = new Color[colorNames.length];
    for (int i = 0; i < colorNames.length; i++) {
      colors[i] = Color.decode(colorNames[i][1]);
      colorMap.put(colorNames[i][0], Color.decode(colorNames[i][1]));
    }

    // remove the border
    StdDraw.setXscale(1.0 / 22.0, 21.0 / 22.0);
    StdDraw.setYscale(1.0 / 22.0, 21.0 / 22.0);

    StdDraw.setCanvasSize(1024, 1024);
    // StdDraw.setXscale(1.0/22.0 +.016, +0.013 + 27.6666667/22.0);
    // StdDraw.setYscale(1.0/22.0, 21.0/22.0);
    StdDraw.clear(CLEAR_COLOR);
    // turn on animation mode
    StdDraw.show(0);

    // the array of particles
    ParticleMultiway[] particles;

    // create N random particles
    if (args.length == 1) {
      int N = Integer.parseInt(args[0]);
      particles = new ParticleMultiway[N];
      for (int i = 0; i < N; i++) {
        double typeRand = StdRandom.uniform();
        int type;
        for (type = 0; type < radii.length; type++) {
          if (typeRand < cumulativeDistribution[type]) {
            break;
          }
        }
//        int type = StdRandom.uniform(colors.length);
        final double radius = radii[type];
        final double mass = density * radius * radius * radius;
        final double MARGIN = .4;
        final double theta = Math.random() * Math.PI * 2;
        {
//        final double P_MAX = 2.0;
//        final double P = P_MAX * Math.random();
        }
        final double E_MAX = .02;
        final double kineticEnergy = E_MAX * Math.random();
        final double v =
            Math.sqrt(2 * kineticEnergy / mass);
//            P / mass;
        // Corrected initial position so particles aren't created less than 
        // radius distance from walls.
        double rx = (1.0 - 2.0 * (radius + MARGIN)) * Math.random() +
                    radius + MARGIN;
        double ry = (1.0 - 2.0 * (radius + MARGIN)) * Math.random() +
                    radius + MARGIN;
        double vx = v * Math.cos(theta);
        double vy = v * Math.sin(theta);
        Color color = colors[type];

        particles[i] = new ParticleMultiway(rx, ry, vx, vy, radius, mass, color);
      }
    } // or read from standard input
    else {
      int N = StdIn.readInt();
      particles = new ParticleMultiway[N];
      for (int i = 0; i < N; i++) {
        double rx = StdIn.readDouble();
        double ry = StdIn.readDouble();
        double vx = StdIn.readDouble();
        double vy = StdIn.readDouble();
        double radius = StdIn.readDouble();
        double mass = StdIn.readDouble();
        int r = StdIn.readInt();
        int g = StdIn.readInt();
        int b = StdIn.readInt();
        Color color = new Color(r, g, b);
        particles[i] = new ParticleMultiway(rx, ry, vx, vy, radius, mass, color);
      }
    }

    // create collision system and simulate
    CollisionSystemMultiway system = new CollisionSystemMultiway(particles);
    system.simulate(10000);
  }

}
