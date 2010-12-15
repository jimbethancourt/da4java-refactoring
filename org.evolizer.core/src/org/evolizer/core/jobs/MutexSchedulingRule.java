/*
 * Copyright 2009 University of Zurich, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evolizer.core.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Applying this scheduling rule to a given family of jobs ensures that a common resource is not accessed concurrently
 * and that the number of running jobs of the given family does not exceed the number of available processors in the
 * system. Usage:
 * 
 * <pre>
 * MyResource resource = new MyResource();
 * MyJob job1 = new MyJob(); // belongsTo(&quot;myJobFamily&quot;) == true
 * job1.setResource(resource);
 * job1.setRule(new MutexSchedulingRule(resource, &quot;myJobFamily);
 * job1.schedule();
 * 
 * MyJob job2 = new MyJob(); // belongsTo(&quot;myJobFamily&quot;) == true
 * job2.setResource(resource);
 * job2.setRule(new MutexSchedulingRule(resource, &quot;myJobFamily);
 * job2.schedule(); // will only run job2 if #processors &gt; 1 or if job1 has completed
 * 
 * </pre>
 * 
 * @author wuersch
 * @see <a
 *      href="http://help.eclipse.org/help30/topic/org.eclipse.platform.doc.isv/guide/runtime_jobs_rules.htm">Platform
 *      Plug-in Developer Guide on Scheduling Rules.</a>
 */
public class MutexSchedulingRule implements ISchedulingRule {

    private static final boolean MULTICORE = Runtime.getRuntime().availableProcessors() > 1;
    private static final int MAX_JOBS = Runtime.getRuntime().availableProcessors();

    private Object fResource;
    private Object fJobFamily;

    /**
     * Constructor.
     * 
     * @param resource
     *            the common resource.
     * @param jobFamily
     *            the job family that this rule applies to.
     */
    public MutexSchedulingRule(Object resource, Object jobFamily) {
        super();
        fResource = resource;
        fJobFamily = jobFamily;
    }

    /**
     * Returns whether this scheduling rule completely contains another scheduling rule. Rules can only be nested within
     * a thread if the inner rule is completely contained within the outer rule.
     * 
     * @see ISchedulingRule#contains(ISchedulingRule)
     * @param rule
     *            the rule to check for containment
     * @return <code>true</code> if this rule contains the given rule, and <code>false</code> otherwise.
     */
    public boolean contains(ISchedulingRule rule) {
        return isConflicting(rule);
    }

    /**
     * Returns whether this scheduling rule is compatible with another scheduling rule. If <code>true</code> is
     * returned, then no job with this rule will be run at the same time as a job with the conflicting rule. If
     * <code>false</code> is returned, then the job manager is free to run jobs with these rules at the same time.
     * 
     * @see ISchedulingRule#isConflicting(ISchedulingRule)
     * @param rule
     *            the rule to check for conflicts
     * @return <code>true</code> if the rule is conflicting, and <code>false</code> otherwise.
     */
    public boolean isConflicting(ISchedulingRule rule) {

        if (rule instanceof MutexSchedulingRule) {
            if (fResource == null) {
                return true;
            }

            MutexSchedulingRule mcRule = (MutexSchedulingRule) rule;
            if (MULTICORE) {
                return mcRule.fResource.equals(fResource) || numberOfAllowedRunningJobsExceeded();
            }

            return true;
        }

        return false;
    }

    /*
     * Checks whether the number of running jobs of a given family exceeds
     * the number of available processors in the system.
     */
    private boolean numberOfAllowedRunningJobsExceeded() {
        Job[] jobs = Job.getJobManager().find(fJobFamily);
        int numberOfRunningJobs = 0;

        for (Job job : jobs) {
            if (job.getState() == Job.RUNNING) {
                numberOfRunningJobs++;
            }
        }

        return numberOfRunningJobs >= MAX_JOBS;
    }
}
