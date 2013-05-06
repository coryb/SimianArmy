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
package com.netflix.simianarmy.conformity;

import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.Collections;

/**
 * The class implementing the auto scaling groups.
 */
public class AutoScalingGroup {
    private final String name;
    private final Collection<String> instances = Lists.newArrayList();

    /**
     * Constructor.
     * @param name
     *          the name of the auto scaling group
     * @param instances
     *          the instance ids in the auto scaling group
     */
    public AutoScalingGroup(String name, String... instances) {
        Validate.notNull(instances);
        this.name = name;
        for (String instance : instances) {
            this.instances.add(instance);
        }
    }

    /**
     * Gets the name of the auto scaling group.
     * @return
     *      the name of the auto scaling group
     */
    public String getName() {
        return name;
    }

    /**
     * * Gets the instances of the auto scaling group.
     * @return
     *    the instances of the auto scaling group
     */
    public Collection<String> getInstances() {
        return Collections.unmodifiableCollection(instances);
    }
}
