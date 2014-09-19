/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataFactory;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jj8
 */
@Component
public class SparseVariantFilterRunner implements FilterRunner<VariantEvaluation, VariantFilter> {

    private static final Logger logger = LoggerFactory.getLogger(SparseVariantFilterRunner.class);

    @Autowired
    private VariantEvaluationDataFactory variantEvaluationFactory;

    /**
     *
     * @param filters
     * @param variantEvaluations
     * @return
     */
    @Override
    public List<VariantEvaluation> run(List<VariantFilter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using sparse filtering", variantEvaluations.size());
        List<VariantEvaluation> filteredVariantEvaluations = new ArrayList<>();

        if (ifThereAreNoFiltersToRun(filters)) {
            return variantEvaluations;
        }

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            for (Filter filter : filters) {
                fetchMissingFrequencyAndPathogenicityData(filter, variantEvaluation);
                FilterResult filterResult = filterVariantEvaluation(filter, variantEvaluation);
                //we want to know which filter the variant failed and then don't run any more
                //this can be an expensive operation when looking up frequency and pathogenicity info from the database
                if (variantFailedTheFilter(filterResult)) {
                    break;
                }
            }
            if (variantEvaluation.passedFilters()) {
                filteredVariantEvaluations.add(variantEvaluation);
            }
        }
        int removed = variantEvaluations.size() - filteredVariantEvaluations.size();
        logger.info("Filtering removed {} variants. Returning {} filtered variants from initial list of {}", removed, filteredVariantEvaluations.size(), variantEvaluations.size());
        return filteredVariantEvaluations;
    }

    private boolean ifThereAreNoFiltersToRun(List<VariantFilter> filters) {
        if (filters.isEmpty()) {
            logger.info("Unable to filter variants against empty Filter list - returning all variants");
            return true;
        }
        return false;
    }

    private void fetchMissingFrequencyAndPathogenicityData(Filter filter, VariantEvaluation variantEvaluation) {
        if (filter.getFilterType() == FilterType.FREQUENCY_FILTER) {
            variantEvaluationFactory.addFrequencyData(variantEvaluation);
        }
        if (filter.getFilterType() == FilterType.PATHOGENICITY_FILTER) {
            variantEvaluationFactory.addPathogenicityData(variantEvaluation);
        }
    }
    private FilterResult filterVariantEvaluation(Filter filter, VariantEvaluation variantEvaluation) {
        FilterResult filterResult = filter.runFilter(variantEvaluation);
        variantEvaluation.addFilterResult(filterResult);
        return filterResult;
    }

    private static boolean variantFailedTheFilter(FilterResult filterResult) {
        return filterResult.getResultStatus() == FilterResultStatus.FAIL;
    }


}