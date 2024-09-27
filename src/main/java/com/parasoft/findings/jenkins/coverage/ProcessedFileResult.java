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

package com.parasoft.findings.jenkins.coverage;

import java.io.Serializable;

public class ProcessedFileResult implements Serializable { // parasoft-suppress OWASP2021.A8.OROM "reviewed"
    private static final long serialVersionUID = 2393265115219226404L;

    private final String coberturaPattern;

    private final String generatedCoverageBuildDir;

    public ProcessedFileResult(String coberturaPattern, String generatedCoverageBuildDir) {
        this.coberturaPattern = coberturaPattern;
        this.generatedCoverageBuildDir = generatedCoverageBuildDir;
    }

    public String getCoberturaPattern() {
        return coberturaPattern;
    }

    public String getGeneratedCoverageBuildDir() {
        return generatedCoverageBuildDir;
    }
}
