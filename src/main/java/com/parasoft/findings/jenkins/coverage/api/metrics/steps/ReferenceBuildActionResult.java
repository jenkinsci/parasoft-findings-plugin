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

public class ReferenceBuildActionResult {

    private final CoverageBuildAction coverageBuildAction;

    private final ReferenceResult referenceResult;

    public ReferenceBuildActionResult(CoverageBuildAction coverageBuildAction, ReferenceResult referenceResult) {
        this.coverageBuildAction = coverageBuildAction;
        this.referenceResult = referenceResult;
    }

    public ReferenceBuildActionResult(ReferenceResult referenceResult) {
        this.coverageBuildAction = null;
        this.referenceResult = referenceResult;
    }

    public CoverageBuildAction getCoverageBuildAction() {
        return coverageBuildAction;
    }

    public ReferenceResult getReferenceResult() {
        return referenceResult;
    }

}
