/*
 * MIT License
 *
 * Copyright (c) 2022 Dr. Ullrich Hafner
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

package com.parasoft.findings.jenkins.coverage.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import com.parasoft.findings.jenkins.coverage.model.Coverage.CoverageBuilder;
import edu.hm.hafner.util.TreeStringBuilder;

/**
 * Class which represents a mutation of the PIT Mutation Testing tool.
 *
 * @author Melissa Bauer
 */
@SuppressWarnings("PMD.DataClass")
// TODO: intern some strings after deserialization to improve the memory footprint
public final class Mutation implements Serializable { // parasoft-suppress OWASP2021.A8.OROM "Using default serialization mechanism."
    private static final long serialVersionUID = -7725185756332899065L;

    private final boolean detected;
    private final MutationStatus status;
    private final int line;
    private final String mutator;
    private final String killingTest;
    private final String mutatedClass;
    private final String method;
    private final String signature;
    private final String description;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Mutation(final boolean detected, final MutationStatus status, final int line, final String mutator,
            final String killingTest, final String mutatedClass,
            final String method, final String signature, final String description) {
        this.detected = detected;
        this.status = status;
        this.line = line;

        // Intern all these values as the content is repeated very often across all mutations
        this.mutator = mutator.intern();
        this.killingTest = killingTest.intern();
        this.mutatedClass = mutatedClass.intern();
        this.method = method.intern();
        this.signature = signature.intern();
        this.description = description.intern();
    }

    public String getMutatedClass() {
        return mutatedClass;
    }

    public String getMethod() {
        return method;
    }

    public String getSignature() {
        return signature;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDetected() {
        return detected;
    }

    public MutationStatus getStatus() {
        return status;
    }

    public boolean isValid() {
        return isCovered() || isMissed();
    }

    public boolean isCovered() {
        return status.isCovered();
    }

    public boolean isMissed() {
        return status.isMissed();
    }

    public int getLine() {
        return line;
    }

    public String getMutator() {
        return mutator;
    }

    public String getKillingTest() {
        return killingTest;
    }

    /**
     * Returns if the mutation was killed.
     *
     * @return if the mutation was killed
     */
    public boolean isKilled() {
        return status.equals(MutationStatus.KILLED);
    }

    /**
     * Returns if the mutation has survived.
     *
     * @return if the mutation has survived
     */
    public boolean hasSurvived() {
        return status.equals(MutationStatus.SURVIVED);
    }

    @Override
    public String toString() {
        return "[Mutation]:"
                + " isDetected=" + detected
                + ", status=" + status
                + ", lineNumber=" + line
                + ", mutator=" + mutator
                + ", killingTest='" + killingTest + "'";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mutation mutation = (Mutation) o;
        return detected == mutation.detected
                && line == mutation.line
                && status == mutation.status
                && Objects.equals(mutator, mutation.mutator)
                && Objects.equals(killingTest, mutation.killingTest)
                && Objects.equals(mutatedClass, mutation.mutatedClass)
                && Objects.equals(method, mutation.method)
                && Objects.equals(signature, mutation.signature)
                && Objects.equals(description, mutation.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(detected, status, line, mutator, killingTest, mutatedClass, method, signature,
                description);
    }

    /**
     * Builder to create new {@link Mutation} instances.
     */
    @SuppressWarnings({"checkstyle:MissingJavadocMethod", "UnusedReturnValue"})
    public static class MutationBuilder {
        private boolean isDetected;
        private MutationStatus status = MutationStatus.NO_COVERAGE;
        private int line;
        private String mutator = StringUtils.EMPTY;
        private String killingTest = StringUtils.EMPTY;
        private String description = StringUtils.EMPTY;
        private String sourceFile = StringUtils.EMPTY;
        private String mutatedClass = StringUtils.EMPTY;
        private String mutatedMethod = StringUtils.EMPTY;
        private String mutatedMethodSignature = StringUtils.EMPTY;

        @CanIgnoreReturnValue
        public MutationBuilder setIsDetected(final boolean isDetected) {
            this.isDetected = isDetected;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setStatus(final MutationStatus status) {
            this.status = status;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setLine(final String line) {
            this.line = CoverageParser.parseInteger(line);

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setLine(final int line) {
            this.line = line;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutator(final String mutator) {
            this.mutator = mutator;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setKillingTest(final String killingTest) {
            this.killingTest = killingTest;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setDescription(final String description) {
            this.description = description;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setSourceFile(final String sourceFile) {
            this.sourceFile = sourceFile;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedClass(final String mutatedClass) {
            this.mutatedClass = mutatedClass;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedMethod(final String mutatedMethod) {
            this.mutatedMethod = mutatedMethod;

            return this;
        }

        @CanIgnoreReturnValue
        public MutationBuilder setMutatedMethodSignature(final String mutatedMethodSignature) {
            this.mutatedMethodSignature = mutatedMethodSignature;

            return this;
        }

        /**
         * Builds a new mutation and adds it to the root of the tree.
         *
         * @param root
         *         the module root to add the mutations to
         * @param treeStringBuilder
         *         the tree string builder to create the file names
         */
        public void buildAndAddToModule(final ModuleNode root, final TreeStringBuilder treeStringBuilder) {
            String packageName = StringUtils.substringBeforeLast(mutatedClass, ".");
            String className = StringUtils.substringAfterLast(mutatedClass, ".");
            var packageNode = root.findOrCreatePackageNode(packageName);
            var relativePath = packageName.replace('.', '/') + '/' + sourceFile;
            var fileNode = packageNode.findOrCreateFileNode(sourceFile, treeStringBuilder.intern(relativePath));
            var classNode = fileNode.findOrCreateClassNode(className);
            var methodNode = classNode.findMethod(mutatedMethod, mutatedMethodSignature)
                    .orElseGet(() -> classNode.createMethodNode(mutatedMethod, mutatedMethodSignature));

            var coverage = methodNode.getValue(Metric.MUTATION)
                    .map(Coverage.class::cast)
                    .orElse(Coverage.nullObject(Metric.MUTATION));
            var builder = new CoverageBuilder(coverage);
            if (isDetected) {
                builder.incrementCovered();
            }
            else {
                builder.incrementMissed();
            }
            methodNode.replaceValue(builder.build());
            fileNode.addMutation(build());
        }

        public Mutation build() {
            return new Mutation(isDetected, status, line, mutator, killingTest,
                    mutatedClass, mutatedMethod, mutatedMethodSignature, description);
        }
    }
}
