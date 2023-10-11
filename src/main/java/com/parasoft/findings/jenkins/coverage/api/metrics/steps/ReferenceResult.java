/*
 * MIT License
 *
 * Copyright (c) 2018 Shenyu Zheng and other Jenkins contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

public class ReferenceResult {

    static final String DEFAULT_REFERENCE_BUILD_IDENTIFIER = "default";

    private final ReferenceStatus status;

    private final String referenceBuild;

    public ReferenceResult(ReferenceStatus status, String referenceBuild) {
        this.status = status;
        this.referenceBuild = referenceBuild;
    }

    public ReferenceStatus getStatus() {
        return status;
    }

    public String getReferenceBuild() {
        return referenceBuild;
    }

    public enum ReferenceStatus {
        OK,
        NO_REF_BUILD,
        NO_PREVIOUS_BUILD_WAS_FOUND,
        REF_BUILD_NOT_SUCCESSFUL_OR_UNSTABLE,
        NO_CVG_DATA_IN_REF_BUILD;
    }

}
