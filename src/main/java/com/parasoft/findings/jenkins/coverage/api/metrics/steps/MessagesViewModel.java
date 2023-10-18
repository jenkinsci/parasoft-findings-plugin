/**
 * MIT License
 *
 * Copyright (c) 2019 Dr. Ullrich Hafner
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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.parasoft.findings.jenkins.coverage.api.metrics.steps;

import hudson.model.ModelObject;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MessagesViewModel implements ModelObject {
    private final Run<?, ?> owner;
    private final List<String> errorMessages;
    private final List<String> infoMessages;
    private final String displayName;

    public MessagesViewModel(final Run<?, ?> owner, final String displayName, final List<String> infoMessages) {
        this(owner, displayName, infoMessages, Collections.emptyList());
    }

    public MessagesViewModel(final Run<?, ?> owner, final String displayName, final List<String> infoMessages, final List<String> errorMessages) {
        this.owner = owner;
        this.errorMessages = this.asImmutableList(errorMessages);
        this.infoMessages = this.asImmutableList(infoMessages);
        this.displayName = displayName;
    }

    private List<String> asImmutableList(final List<String> elements) {
        return Collections.unmodifiableList(new ArrayList(elements));
    }

    public final Run<?, ?> getOwner() {
        return this.owner;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Collection<String> getInfoMessages() {
        return this.infoMessages;
    }

    public Collection<String> getErrorMessages() {
        return this.errorMessages;
    }

    public boolean hasErrors() {
        return !this.errorMessages.isEmpty();
    }
}