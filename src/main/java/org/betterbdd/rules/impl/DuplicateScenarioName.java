package org.betterbdd.rules.impl;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import org.betterbdd.model.Feature;
import org.betterbdd.model.enums.Rule;
import org.betterbdd.model.enums.RuleCategory;
import org.betterbdd.model.result.RunResult;
import org.betterbdd.model.result.impl.FeatureRunResult;
import org.betterbdd.model.result.impl.FeaturesRunResult;
import org.betterbdd.model.result.impl.ScenarioRunResult;
import org.betterbdd.rules.IRuleBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.betterbdd.model.enums.Rule.duplicate_scenario_name;
import static org.betterbdd.model.enums.RuleCategory.DUPLICATION;

public class DuplicateScenarioName extends IRuleBatch {

    private List<Feature> featuresWithDuplicates = new ArrayList<>();

    private static final Rule RULE = duplicate_scenario_name;
    private static final String DESCRIPTION = duplicate_scenario_name.description();
    private static final String ERROR_MESSAGE = "Duplicate scenario names found";
    private static final RuleCategory CATEGORY = DUPLICATION;

    public DuplicateScenarioName() {
        super(RULE, DESCRIPTION, ERROR_MESSAGE, CATEGORY);
    }

    @Override
    public RunResult applyRule(List<Feature> features) {

        List<FeatureRunResult> featureRunResultList = new ArrayList<>();

        for (Feature feature : features) {

            GherkinDocument gherkinDocument = feature.getGherkinDocument();

            if (gherkinDocument.getFeature() != null) {

                Map<String, Integer> frequency = new HashMap<>();

                for (ScenarioDefinition scenario : gherkinDocument.getFeature().getChildren()) {
                    if (frequency.containsKey(scenario.getName())) {
                        frequency.put(scenario.getName(), frequency.get(scenario.getName()) + 1);
                    } else {
                        frequency.put(scenario.getName(), 1);
                    }
                }

                List<ScenarioRunResult> scenarioRunResultList = new ArrayList<>();

                for (Map.Entry<String, Integer> fr : frequency.entrySet()) {
                    if (fr.getValue() > 1) {
                        this.featuresWithDuplicates.add(feature);
                        scenarioRunResultList.add(new ScenarioRunResult(false, this, fr.getKey()));

                    } else {
                        scenarioRunResultList.add(new ScenarioRunResult(true, this, fr.getKey()));
                    }

                    boolean featureIsSuccess = true;

                    for (ScenarioRunResult result : scenarioRunResultList) {
                        if (!result.isSuccess())
                            featureIsSuccess = false;
                    }

                    featureRunResultList.add(new FeatureRunResult(featureIsSuccess, this, feature, scenarioRunResultList));
                }
            } else {
                //TODO: log warning
            }
        }
        boolean success = featuresWithDuplicates.size() <= 0;

        return new FeaturesRunResult(success, this, featureRunResultList);

    }

}


