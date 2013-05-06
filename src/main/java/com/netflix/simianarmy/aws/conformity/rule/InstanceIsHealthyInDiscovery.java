/*
 *
 *  Copyright 2012 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.simianarmy.aws.conformity.rule;

import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.simianarmy.conformity.AutoScalingGroup;
import com.netflix.simianarmy.conformity.Cluster;
import com.netflix.simianarmy.conformity.Conformity;
import com.netflix.simianarmy.conformity.ConformityRule;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * The class implements a conformity rule to check if all instances in the cluster are healthy in Discovery.
 */
public class InstanceIsHealthyInDiscovery implements ConformityRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIsHealthyInDiscovery.class);

    private static final String RULE_NAME = "InstanceIsHealthyInDiscovery";
    private static final String REASON = "Instances are not 'UP' in Discovery.";

    private final DiscoveryClient discoveryClient;

    /**
     * Constructor.
     * @param discoveryClient
     *          the client to access the Discovery/Eureka service for checking the status of instances.
     */
    public InstanceIsHealthyInDiscovery(DiscoveryClient discoveryClient) {
        Validate.notNull(discoveryClient);
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Conformity check(Cluster cluster) {
        Collection<String> failedComponents = Lists.newArrayList();
        for (AutoScalingGroup asg : cluster.getAutoScalingGroups()) {
            for (String instance : asg.getInstances()) {
                List<InstanceInfo> instanceInfos = discoveryClient.getInstancesById(instance);
                for (InstanceInfo info : instanceInfos) {
                    InstanceInfo.InstanceStatus status = info.getStatus();
                    if (!status.equals(InstanceInfo.InstanceStatus.UP)
                            && !status.equals(InstanceInfo.InstanceStatus.STARTING)) {
                        LOGGER.debug(String.format("Instance %s is not healthy in Discovery with status %s.",
                                instance, status.name()));
                        failedComponents.add(instance);
                    }
                }
            }
        }
        return new Conformity(getName(), failedComponents);
    }

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public String getNonconformingReason() {
        return REASON;
    }
}
