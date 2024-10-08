/*
 * Copyright 2023 Parasoft Corporation
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

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.Serializable;

public class ReferenceResult implements Serializable { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."

    private static final long serialVersionUID = -5028911049640671582L;

    static final String DEFAULT_REFERENCE_BUILD_IDENTIFIER = "default";

    private final ReferenceStatus status;

    @Nullable
    private String referenceJob;

    private final String referenceBuild;

    public ReferenceResult(ReferenceStatus status, String referenceBuild) {
        this.status = status;
        this.referenceBuild = referenceBuild;
    }

    public ReferenceResult(ReferenceStatus status, String referenceJob, String referenceBuild) {
        this.status = status;
        this.referenceJob = referenceJob;
        this.referenceBuild = referenceBuild;
    }

    public ReferenceStatus getStatus() {
        return status;
    }

    public String getReferenceJob() {
        return referenceJob;
    }

    public String getReferenceBuild() {
        return referenceBuild;
    }

    public enum ReferenceStatus {
        OK,
        NO_REF_JOB,
        NO_REF_BUILD,
        NO_PREVIOUS_BUILD_WAS_FOUND,
        REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE,
        REF_BUILD_IS_CURRENT_BUILD,
        NO_CVG_DATA_IN_REF_BUILD;
    }

}
