package org.bddaid.rules.impl;

import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import org.bddaid.model.Feature;
import org.bddaid.model.RunResult;
import org.bddaid.rules.IRuleBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateScenarioName implements IRuleBatch {

    private Map<String, Map<String, Integer>> featuresWithDuplicates = new HashMap<>();

    @Override
    public RunResult applyRule(List<Feature> features) {

        boolean result = true;
        List<String> errors = new ArrayList<>();

        GherkinDocument gherkinDocument = null;
        for (Feature feature : features) {

            gherkinDocument = feature.getGherkinDocument();
            List<Pickle> pickles = new Compiler().compile(gherkinDocument);

            Map<String, Integer> frequency = new HashMap<>();
            if (pickles.size() > 1) {
                for (Pickle pickle : pickles) {
                    if (frequency.containsKey(pickle.getName())) {
                        frequency.put(pickle.getName(), frequency.get(pickle.getName()) + 1);
                    } else {
                        frequency.put(pickle.getName(), 1);
                    }


                }

                for (Map.Entry<String, Integer> fr : frequency.entrySet()) {
                    if (fr.getValue() > 1)
                        this.featuresWithDuplicates.put(feature.getFileName(), frequency);


                }
            } else {
                //TODO: log warning
            }

        }
        if (featuresWithDuplicates.size() > 0) {
            errors.add(getErrorMessage());
            result = false;
        }
        return new RunResult(result,errors);

    }

    @Override
    public String getName() {
        return "duplicate_scenario_name";
    }

    @Override
    public String getDescription() {
        return "This rule prevents feature files having duplicate scenario names";
    }

    @Override
    public String getErrorMessage() {

        String msg = "\nDuplicate scenario names detected in feature files:";

        for (Map.Entry<String, Map<String, Integer>> feature : featuresWithDuplicates.entrySet())
            msg = msg + String.format("\n Feature file: %s \n\tScenarios: %s", feature.getKey(), feature.getValue().entrySet());

        return msg;
    }
}