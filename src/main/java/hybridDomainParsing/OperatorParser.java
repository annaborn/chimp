package hybridDomainParsing;

import htn.EffectTemplate;
import htn.HTNOperator;
import htn.HTNPlanner;
import htn.HTNPrecondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import resourceFluent.ResourceUsageTemplate;

public class OperatorParser extends PlanReportroiryItemParser {
	
	
	private final List<ResourceUsageTemplate> rtList = new ArrayList<ResourceUsageTemplate>();
	

	public OperatorParser(String textualSpecification, HTNPlanner planner, int maxArgs){
		super(textualSpecification, planner, maxArgs);

		// Parse Resources
		String[] resourceElements = HybridDomain.parseKeyword(HybridDomain.ACTION_RESOURCE_KEYWORD, 
				textualSpecification);
		for (String resElement : resourceElements) {
			ResourceUsageTemplate rt = HybridDomain.parseResourceUsage(resElement, false);
			rtList.add(rt);
		}
	}
	
	@Override
	public HTNOperator create() throws DomainParsingException {
		HTNPrecondition[] preconditions = createPreconditions(true);
		
		String headname = HybridDomain.extractName(head);
		EffectTemplate[] effects = createEffectTemplates("PlannedState", HybridDomain.EFFECT_KEYWORD);
		HTNOperator op =  new HTNOperator(headname, argStrings, preconditions, effects);
		op.setVariableOccurrencesMap(variableOccurrencesMap);
		
		// add additional constraints from head to head or between preconditions or effects
		op.setAdditionalConstraints(filterAdditionalConstraints());
		
		Map<String,String[]> variablesPossibleValuesMap = parseValueRestrictions(HybridDomain.VALUE_RESTRICTION_KEYWORD, HybridDomain.TYPE_KEYWORD);
		op.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		
		Map<String,String[]> variablesImpossibleValuesMap = parseValueRestrictions(HybridDomain.NEGATED_VALUE_RESTRICTION_KEYWORD, HybridDomain.NOT_TYPE_KEYWORD);
		op.setVariablesImpossibleValuesMap(variablesImpossibleValuesMap);
		
		SubDifferentDefinition[] subDiffs = parseSubDifferentDefinitions();
		op.setSubDifferentDefinitions(subDiffs);
		
		op.addResourceUsageTemplates(rtList);
		return op;
	}

}
