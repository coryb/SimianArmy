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

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.simianarmy.client.aws.AWSClient;
import com.netflix.simianarmy.conformity.AutoScalingGroup;
import com.netflix.simianarmy.conformity.Cluster;
import com.netflix.simianarmy.conformity.Conformity;
import com.netflix.simianarmy.conformity.ConformityRule;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The class implementing a conformity rule that checks if the zones in ELB and ASG are the same.
 */
public class SameZonesInElbAndAsg implements ConformityRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceHasStatusUrl.class);

    private static final String RULE_NAME = "SameZonesInElbAndAsg";
    private static final String REASON = "Zones of ELB are different from zones of ASG";

    @Override
    public Conformity check(Cluster cluster) {
        List<String> asgNames = Lists.newArrayList();
        for (AutoScalingGroup asg : cluster.getAutoScalingGroups()) {
            asgNames.add(asg.getName());
        }
        AWSClient awsClient = new AWSClient(cluster.getRegion());
        Collection<String> failedComponents = Lists.newArrayList();
        if (cluster.getAutoScalingGroups().size() != 0) {
            for (com.amazonaws.services.autoscaling.model.AutoScalingGroup asg :
                    awsClient.describeAutoScalingGroups(asgNames.toArray(new String[asgNames.size()]))) {
                if (asg.getLoadBalancerNames().size() == 0) {
                    continue;
                }
                for (LoadBalancerDescription lbd :
                    awsClient.describeElasticLoadBalancers(asg.getLoadBalancerNames().toArray(new String[]{}))) {
                    if (!haveSameZones(asg, lbd)) {
                        LOGGER.info(String.format("ASG %s and ELB %s do not have the same availability zones",
                                asg.getAutoScalingGroupName(), lbd.getLoadBalancerName()));
                        failedComponents.add(lbd.getLoadBalancerName());
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

    /**
     * Checks whetehr the ASG and ELB have the same set of availability zones.
     * @param asg
     *      the auto scaling group
     * @param lbd
     *      the load balancer description
     * @return
     *      true if the ASG and ELB have the same set of availability zones, false otherwise
     */
    private boolean haveSameZones(com.amazonaws.services.autoscaling.model.AutoScalingGroup asg,
                                  LoadBalancerDescription lbd) {
        if (asg.getAvailabilityZones().size() != lbd.getAvailabilityZones().size()) {
            return false;
        }
        for (String zone : asg.getAvailabilityZones()) {
            if (!lbd.getAvailabilityZones().contains(zone)) {
                return false;
            }
        }
        for (String zone : lbd.getAvailabilityZones()) {
            if (!asg.getAvailabilityZones().contains(zone)) {
                return false;
            }
        }
        return true;
    }
}
