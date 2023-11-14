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

package com.parasoft.findings.jenkins.coverage.api.metrics.color;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains color IDs which represent the keys of a JSON object that is dynamically filled with the currently set
 * Jenkins colors.
 *
 * @author Florian Orendi
 */
public enum CoverageColorJenkinsId {

    GREEN("--green"),
    LIGHT_GREEN("--light-green"),
    YELLOW("--yellow"),
    LIGHT_YELLOW("--light-yellow"),
    ORANGE("--orange"),
    LIGHT_ORANGE("--light-orange"),
    RED("--red"),
    LIGHT_RED("--light-red");

    private final String jenkinsColorId;

    CoverageColorJenkinsId(final String colorId) {
        this.jenkinsColorId = colorId;
    }

    public String getJenkinsColorId() {
        return jenkinsColorId;
    }

    public static Set<String> getAll() {
        return Arrays.stream(values())
                .map(id -> id.jenkinsColorId)
                .collect(Collectors.toSet());
    }
}
