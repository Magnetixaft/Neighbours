import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Struct;
import java.util.*;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.*;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then the method start() far below.
 * - The method updateWorld() is called periodically by a Java timer.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, NONE   // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA     // Not applicable (NA), used for NONEs
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        //if (!(currentTimeMillis() % 3000 < 500)) return;
        // % of surrounding neighbours that are like me
        final double threshold = 0.5;
        List<Integer> vacantIndices = new ArrayList<Integer>();
        //number of each type of actor to move BLUE, RED
        int[] moveActors = new int[]{0,0};

        for (int x = 0; x < world.length; x++){
            for (int y = 0; y < world[0].length; y++){

                State state = CheckState(x, y, threshold);

                if (!State.SATISFIED.equals(state)){
                    //removes the unsatisfied actor from world and
                    if (State.UNSATISFIED.equals(state)) {
                        moveActors[world[x][y].ordinal()]++;
                        world[x][y] = Actor.NONE;
                    }
                    //current coordinate as an index in a one dimensional array
                    int coordAsIndex = x * world.length + y;
                    //marks the location as vacant for all unsatisfied and NA
                    vacantIndices.add(coordAsIndex);
                }
            }
        }
        //reintroduces the previously removed actors to world in new random positions
        for (int i = 0; i < 2; i++) {
            PopulateWithActor(Actor.values()[i], vacantIndices, moveActors[i]);//CountOfGivenActor(actorsToMove, Actor.values()[i]));
        }
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of BlUE, RED and NONE
        double[] dist = {0.25, 0.25, 0.5};
        // Number of locations (places) in world (square)
        int sideLength = 30;
        int nLocations = sideLength * sideLength;
        world = new Actor[sideLength][sideLength];
        //list where all currently empty locations are stored in a 1d projection of world
        List<Integer> vacantIndices;
        //makes a list with every ordinal leading up to nLocations since all spaces are currently empty
        vacantIndices = CreateIndexList(nLocations);

        //runs PopulateWithActor for each type of actor based on its enum index
        for (int actorIndex = 0; actorIndex <= 2; actorIndex++) {
            PopulateWithActor(
                    Actor.values()[actorIndex],
                    vacantIndices,
                    (int)(nLocations * dist[actorIndex]));
        }

        fixScreenSize(nLocations);
    }

    //---------------- Methods ----------------------------

    //Check if an actor is satisfied
    State CheckState(int x, int y, double threshold){
        //the currently evaluated actor
        Actor current = world[x][y];
        //returns NA if no actor inhibits world[x][y]
        if (Actor.NONE.equals(current))
            return State.NA;
        //amount of neighbors
        int nNeighbors = 0;
        //amount of alike neighbors
        int nAlikeNeighbors = 0;

        //cycles through the eight neighboring cells
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                //Skip locations outside of world and the location currently being evaluated
                if (!isValidLocation(world.length, i, j) || (i == x && j == y)) { continue; }

                //checks if world[i][j] is a neighbor and if so increments nNeighbors
                if (!Actor.NONE.equals(world[i][j])){
                    nNeighbors++;
                    //Checks if neighbor world[i][j] is like the currently evaluated actor
                    if (current.equals(world[i][j]))
                        nAlikeNeighbors++;
                }
            }
        }
        //determines whether the share of alike neighbors is above the threshold
        boolean isSatisfied = nAlikeNeighbors >= threshold * (double)nNeighbors || nNeighbors == 0;

        if (isSatisfied)
            return State.SATISFIED;
        else
            return State.UNSATISFIED;
    }

    //Spreads a given actor across the world according to dist
    void PopulateWithActor(Actor actor, List<Integer> vacantIndices, int loops){
        final Random rnd = new Random();

        for (int i = 0; i < loops; i++){
            //index to select an unused index from the vacantIndices List
            int indexSelector = rnd.nextInt(vacantIndices.size());
            //location in world as a 1-dimensional index
            int location = vacantIndices.get(indexSelector);

            //populates the given world location with type actor currently being sprinkled throughout world
            world[location / world.length][location % world.length] = actor;
            vacantIndices.remove(indexSelector);
        }
    }

    //Creates a list where every element represents its own index
    List<Integer> CreateIndexList(int length){
        List<Integer> temp = new ArrayList<Integer>();

        for (int i = 0; i < length; i++){
            temp.add(i);
        }

        return temp;
    }

    // Check if inside world
    boolean isValidLocation(int size, int col, int row) {
        return 0 <= row && row < size &&
                0 <= col && col < size;
    }


    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {Actor.RED, Actor.RED, Actor.NONE},
                {Actor.NONE, Actor.BLUE, Actor.NONE},
                {Actor.RED, Actor.NONE, Actor.BLUE}
        };
        double th = 0.5;   // Simple threshold used for testing

        out.println(testWorld[1][1].toString());

        int size = testWorld.length;
        out.println(size);
        out.println(isValidLocation(size, 0, 0));
        out.println(!isValidLocation(size, -5, -2));
        out.println(!isValidLocation(size, 3, 0));
        out.println(isValidLocation(size, 2, 2));

        // TODO More tests

        exit(0);
    }

    // Helper method for testing (NOTE: reference equality)
    <T> int count(T[] arr, T toFind) {
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == toFind) {
                count++;
            }
        }
        return count;
    }

    // ###########  NOTHING to do below this row, it's JavaFX stuff  ###########

    double width = 400;   // Size for window
    double height = 400;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
        // Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Segregation Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}