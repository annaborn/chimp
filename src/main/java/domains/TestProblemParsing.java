package domains;

import hybridDomainParsing.DomainParsingException;
import hybridDomainParsing.HybridDomain;
import hybridDomainParsing.ProblemParser;

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.TaskSelectionMetaConstraint;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import externalPahtPlanning.LookUpTableDurationEstimator;
import externalPahtPlanning.MoveBaseDurationEstimator;
import externalPahtPlanning.MoveBaseMetaConstraint;

public class TestProblemParsing {

	public static void main(String[] args) {
		
//		ProblemParser pp = new ProblemParser("problems/test_op_tuck_arms.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base.pdl");
//		ProblemParser pp = new ProblemParser("problems/test_op_move_base_blind.pdl");
		ProblemParser pp = new ProblemParser("problems/test_op_move_torso.pdl");
		
		String[][] symbols = RACEProblemsSingle.createSymbols();
		int[] ingredients = RACEProblemsSingle.createIngredients();
		PFD0Planner planner = new PFD0Planner(0,  600,  0, symbols, ingredients);
		FluentNetworkSolver fluentSolver = (FluentNetworkSolver)planner.getConstraintSolvers()[0];
		
		initPlanner(planner, "domains/race_domain.ddl");
		
		pp.createState(fluentSolver);
		
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		MetaCSPLogging.setLevel(Level.FINEST);
		
		plan(planner, fluentSolver);
		
		extractPlan(fluentSolver);
	}
	
	private static boolean plan(PFD0Planner planner, FluentNetworkSolver fluentSolver) {
		((CompoundSymbolicVariableConstraintSolver) fluentSolver.getConstraintSolvers()[0]).propagateAllSub();
		
		long startTime = System.nanoTime();
		boolean result = planner.backtrack();
		System.out.println("Found a plan? " + result);
		long endTime = System.nanoTime();

		planner.draw();
		ConstraintNetwork.draw(fluentSolver.getConstraintNetwork());
		ConstraintNetwork.draw(fluentSolver.getConstraintSolvers()[1].getConstraintNetwork());

		System.out.println(planner.getDescription());
		System.out.println("Took "+((endTime - startTime) / 1000000) + " ms"); 
		System.out.println("Finished");
		return result;
	}
	
	private static void extractPlan(FluentNetworkSolver fluentSolver) {
		PlanExtractor planEx = new PlanExtractor(fluentSolver);
		planEx.printPlan();
	}
	
	private static void initPlanner(PFD0Planner planner, String domainPath) {
		// load domain
		HybridDomain dom;
		try {
			dom = new HybridDomain(planner, domainPath);
		} catch (DomainParsingException e) {
			System.out.println("Error while parsing domain: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// init meta constraints based on domain
		TaskSelectionMetaConstraint selectionConstraint = new TaskSelectionMetaConstraint();
		selectionConstraint.addOperators(dom.getOperators());
		selectionConstraint.addMethods(dom.getMethods());
		Vector<ResourceUsageTemplate> fluentResourceUsages = dom.getFluentResourceUsages();
		selectionConstraint.setResourceUsages(fluentResourceUsages);
		planner.addMetaConstraint(selectionConstraint);
		
		for (FluentScheduler fs : dom.getFluentSchedulers()) {
			planner.addMetaConstraint(fs);
		}
		
		for (FluentResourceUsageScheduler rs : dom.getResourceSchedulers()) {
			planner.addMetaConstraint(rs);
		}
		
		MoveBaseDurationEstimator mbEstimator = new LookUpTableDurationEstimator();
		MoveBaseMetaConstraint mbConstraint = new MoveBaseMetaConstraint(mbEstimator);
		planner.addMetaConstraint(mbConstraint);
	}
	



}