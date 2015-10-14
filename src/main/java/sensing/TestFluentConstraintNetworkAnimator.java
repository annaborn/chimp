package sensing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import dispatching.FluentDispatchingFunction;
import examples.TestRACEDomain;
import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;

public class TestFluentConstraintNetworkAnimator {
		
	public static void main(String[] args) {
		final long origin = Calendar.getInstance().getTimeInMillis();
		Logger logger = MetaCSPLogging.getLogger(TestFluentConstraintNetworkAnimator.class);
		String[][] symbols = TestRACEDomain.createSymbols();
		int[] ingredients = TestRACEDomain.createIngredients();

		FluentNetworkSolver fns = new FluentNetworkSolver(origin, origin+100000, symbols, ingredients);

//		Fluent[] fluents = (Fluent[]) fns.createVariables(3);
////		ConstraintNetwork.draw(solver.getConstraintNetwork());
//		fluents[0].setName("On(mug1 placingAreaWestRightTable1)");
//		fluents[1].setName("On(mug1 placingAreaWestRightTable1)");
//		fluents[2].setName("RobotAt(preManipulationAreaEastCounter1)");
		
		FluentConstraintNetworkAnimator animator = new FluentConstraintNetworkAnimator(fns, 1000);
		
		final Vector<Fluent> executingActs = new Vector<Fluent>();
		
		FluentDispatchingFunction df = new FluentDispatchingFunction("Activity") {
			
			@Override
			public boolean skip(Fluent act) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void dispatch(Fluent act) {
				executingActs.addElement(act);
				System.out.println("This is a call to ROS to do " + act);
			}
		};
		
		animator.addDispatchingFunctions(fns, df);
		
		ConstraintNetwork.draw(fns.getConstraintNetwork());
		
		FluentSensor sensorRobotAt = new FluentSensor("RobotAt", animator);
		sensorRobotAt.postSensorValue("RobotAt(manipulationAreaSouthTable1)", origin );
		
		Fluent fl0 = (Fluent) fns.createVariable("Activity");
		fl0.setName("!move_base(preManipulationAreaSouthTable1)");
		AllenIntervalConstraint dur0 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(4000,APSPSolver.INF));
		dur0.setFrom(fl0);
		dur0.setTo(fl0);
		AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(origin+5000,origin+5000));
		release.setFrom(fl0);
		release.setTo(fl0);
		fns.addConstraints(dur0, release);
		
		
//		sensorRobotAt.postSensorValue("RobotAt(preManipulationAreaEastCounter1)", origin + 4000);
//		sensorRobotAt.postSensorValue("RobotAt(manipulationAreaSouthTable1)", origin + 10000);
//		Sensor sensorB = new Sensor("SensorB", animator);
		
//		sensorA.registerSensorTrace("sensorTraces/sensorA.st");
//		sensorB.registerSensorTrace("sensorTraces/sensorB.st");

//		TimelinePublisher tp = new TimelinePublisher(fns, new Bounds(0,60000), true, "Time", "SensorA", "SensorB");
//		tp.setTemporalResolution(1);
//		TimelineVisualizer tv = new TimelineVisualizer(tp);
//		tv.startAutomaticUpdate(1000);
		
		while (true) {
			System.out.println("Executing activities (press <enter> to refresh list):");
			for (int i = 0; i < executingActs.size(); i++) System.out.println(i + ". " + executingActs.elementAt(i));
			System.out.println("--");
			System.out.print("Please enter activity to finish: ");  
			String input = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			try { input = br.readLine(); }
			catch (IOException e) { e.printStackTrace(); }
			if (!input.trim().equals("")) {
				try {
					df.finish(executingActs.elementAt(Integer.parseInt(input)));
					executingActs.remove(Integer.parseInt(input));
				}
				catch (ArrayIndexOutOfBoundsException e1) { /* Ignore unknown activity */ }
			}
		}
		
	}

}
