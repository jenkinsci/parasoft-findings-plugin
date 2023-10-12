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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Node;

import com.parasoft.findings.jenkins.coverage.api.metrics.AbstractCoverageTest;
import io.jenkins.plugins.forensics.delta.Change;
import io.jenkins.plugins.forensics.delta.ChangeEditType;
import io.jenkins.plugins.forensics.delta.FileChanges;
import io.jenkins.plugins.forensics.delta.FileEditType;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for {@link FileChangesProcessor}.
 *
 * @author Florian Orendi
 */
class FileChangesProcessorTest extends AbstractCoverageTest {
    private static final String TEST_FILE_1 = "Test1.java";
    private static final String TEST_FILE_2 = "Main.java";
    private static final String TEST_FILE_1_PATH = "test/example/" + TEST_FILE_1;
    private static final String TEST_FILE_1_PATH_OLD = "test/example/old/" + TEST_FILE_1;

    /**
     * A JaCoCo report which contains the code coverage of a test project <b>after</b> the {@link #CODE_CHANGES} has
     * been inserted.
     */
    private static final String TEST_REPORT_AFTER = "file-changes-test-after.xml";

    /**
     * The code changes that took place between the generation of {@link
     * #TEST_REPORT_AFTER}.
     */
    private static final Map<String, FileChanges> CODE_CHANGES = new HashMap<>();

    /**
     * Initializes a map with the inserted {@link #CODE_CHANGES}.
     */
    @BeforeAll
    static void initFileChanges() {
        Change insert1 = new Change(ChangeEditType.INSERT, 4, 4, 5, 9);
        Change insert2 = new Change(ChangeEditType.INSERT, 8, 8, 14, 18);
        Change insert3 = new Change(ChangeEditType.INSERT, 25, 25, 33, 36);
        Change replace = new Change(ChangeEditType.REPLACE, 10, 11, 20, 22);
        Change delete = new Change(ChangeEditType.DELETE, 16, 19, 26, 26);
        FileChanges fileChanges = new FileChanges(TEST_FILE_1_PATH, TEST_FILE_1_PATH_OLD,
                "test", FileEditType.RENAME, new HashMap<>());
        fileChanges.addChange(insert1);
        fileChanges.addChange(insert2);
        fileChanges.addChange(insert3);
        fileChanges.addChange(replace);
        fileChanges.addChange(delete);
        CODE_CHANGES.put(TEST_FILE_1_PATH, fileChanges);
        CODE_CHANGES.put(TEST_FILE_2,
                new FileChanges("empty", "empty", "", FileEditType.MODIFY, new HashMap<>()));
    }

    @Test
    void shouldAttachChangesCodeLines() {
        FileChangesProcessor fileChangesProcessor = createFileChangesProcessor();
        Node tree = readJacocoResult(TEST_REPORT_AFTER);
        fileChangesProcessor.attachChangedCodeLines(tree, CODE_CHANGES);

        assertThat(tree.findByHashCode(Metric.FILE, TEST_FILE_1_PATH.hashCode()))
                .isNotEmpty()
                .satisfies(node -> assertThat(node.get())
                        .isInstanceOfSatisfying(FileNode.class, f -> assertThat(f.getModifiedLines())
                                        .containsExactly(
                            5, 6, 7, 8, 9, 14, 15, 16, 17, 18, 20, 21, 22, 33, 34, 35, 36)));
        assertThat(tree.findByHashCode(Metric.FILE, TEST_FILE_2.hashCode()))
                .isNotEmpty()
                .satisfies(node -> assertThat(node.get())
                            .isInstanceOfSatisfying(FileNode.class, f -> assertThat(f.getModifiedLines())
                            .isEmpty()));
    }

    /**
     * Creates an instance of {@link FileChangesProcessor}.
     *
     * @return the created instance
     */
    private FileChangesProcessor createFileChangesProcessor() {
        return new FileChangesProcessor();
    }
}
