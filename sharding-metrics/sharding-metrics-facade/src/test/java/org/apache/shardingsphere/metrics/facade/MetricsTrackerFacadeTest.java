/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.metrics.facade;

import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneHistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneSummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsTrackerFacadeTest {
    
    private MetricsTrackerFacade metricsTrackerFacade = MetricsTrackerFacade.getInstance();
    
    @Before
    public void setUp() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, null, null);
        metricsTrackerFacade.init(metricsConfiguration);
        assertTrue(metricsTrackerFacade.getEnabled());
    }
    
    @Test
    public void assertFindMetricsTrackerManager() {
        assertNull(metricsTrackerFacade.findMetricsTrackerManager("fixture1"));
        assertNotNull(metricsTrackerFacade.findMetricsTrackerManager("fixture"));
    }
    
    @Test
    public void counterInc() {
        metricsTrackerFacade.counterInc("request_total");
    }
    
    @Test
    public void gaugeInc() {
        metricsTrackerFacade.gaugeInc("request_total");
    }
    
    @Test
    public void gaugeDec() {
        metricsTrackerFacade.gaugeDec("request_total");
    }
    
    @Test
    public void histogram() {
        HistogramMetricsTrackerDelegate delegate = metricsTrackerFacade.histogramStartTimer("request");
        assertEquals(delegate.getClass(), NoneHistogramMetricsTrackerDelegate.class);
        metricsTrackerFacade.histogramObserveDuration(delegate);
    }
    
    @Test
    public void summary() {
        SummaryMetricsTrackerDelegate delegate = metricsTrackerFacade.summaryStartTimer("request");
        metricsTrackerFacade.summaryObserveDuration(delegate);
        assertEquals(delegate.getClass(), NoneSummaryMetricsTrackerDelegate.class);
    }
}

