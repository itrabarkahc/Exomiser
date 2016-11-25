/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation.Builder;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class KnownVariantFilterTest {

    private final VariantFilter instance = new KnownVariantFilter();
    
    private final FilterResult PASS_RESULT = new PassFilterResult(FilterType.KNOWN_VARIANT_FILTER);
    private final FilterResult FAIL_RESULT = new FailFilterResult(FilterType.KNOWN_VARIANT_FILTER);
    
    private VariantEvaluation buildVariantWithFrequencyData(FrequencyData frequencyData) {
        return new Builder(1, 1, "A", "T").frequencyData(frequencyData).build();
    }

    @Test
    public void testVariantType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
    }

    @Test
    public void testRunFilter_ReturnsFailResultWhenFilteringVariantWithRsId() {
        FrequencyData frequencyData = new FrequencyData(RsId.valueOf(12345));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }
    
    @Test
    public void testRunFilter_ReturnsFailResultWhenFilteringVariantWithKnownFrequency() {
        FrequencyData frequencyData = new FrequencyData(null, Frequency.valueOf(1f, FrequencySource.THOUSAND_GENOMES));
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(frequencyData);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(FAIL_RESULT));
    }

    @Test
    public void testRunFilter_ReturnsPassResultWhenFilteringVariantWithNoRepresentationInDatabase() {
        VariantEvaluation variantEvaluation = buildVariantWithFrequencyData(FrequencyData.EMPTY_DATA);
        FilterResult filterResult = instance.runFilter(variantEvaluation);
        assertThat(filterResult, equalTo(PASS_RESULT));
    }
    
}
