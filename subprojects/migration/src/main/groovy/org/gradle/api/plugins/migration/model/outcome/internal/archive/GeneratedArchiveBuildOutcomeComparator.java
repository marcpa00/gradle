/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.plugins.migration.model.outcome.internal.archive;

import org.gradle.api.Transformer;
import org.gradle.api.plugins.migration.model.outcome.internal.BuildOutcomeAssociation;
import org.gradle.api.plugins.migration.model.compare.internal.BuildOutcomeComparator;
import org.gradle.api.plugins.migration.model.outcome.internal.archive.entry.ArchiveEntry;
import org.gradle.api.plugins.migration.model.outcome.internal.archive.entry.ArchiveEntryComparison;
import org.gradle.api.plugins.migration.model.outcome.internal.archive.entry.FileToArchiveEntrySetTransformer;
import org.gradle.api.plugins.migration.model.outcome.internal.archive.entry.ZipEntryToArchiveEntryTransformer;
import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.*;

public class GeneratedArchiveBuildOutcomeComparator implements BuildOutcomeComparator<GeneratedArchiveBuildOutcome, GeneratedArchiveBuildOutcomeComparisonResult> {

    private final Transformer<Set<ArchiveEntry>, File> archiveToEntriesTransformer;

    public GeneratedArchiveBuildOutcomeComparator() {
        this(new FileToArchiveEntrySetTransformer(new ZipEntryToArchiveEntryTransformer()));
    }

    GeneratedArchiveBuildOutcomeComparator(Transformer<Set<ArchiveEntry>, File> archiveToEntriesTransformer) {
        this.archiveToEntriesTransformer = archiveToEntriesTransformer;
    }

    public Class<GeneratedArchiveBuildOutcome> getComparedType() {
        return GeneratedArchiveBuildOutcome.class;
    }

    public GeneratedArchiveBuildOutcomeComparisonResult compare(BuildOutcomeAssociation<GeneratedArchiveBuildOutcome> association) {
        GeneratedArchiveBuildOutcome from = association.getFrom();
        GeneratedArchiveBuildOutcome to = association.getTo();

        Set<ArchiveEntry> fromEntries;
        if (from.getArchiveFile() != null && from.getArchiveFile().exists()) {
            fromEntries = archiveToEntriesTransformer.transform(from.getArchiveFile());
        } else {
            fromEntries = Collections.emptySet();
        }

        Set<ArchiveEntry> toEntries;
        if (to.getArchiveFile() != null && to.getArchiveFile().exists()) {
            toEntries = archiveToEntriesTransformer.transform(to.getArchiveFile());
        } else {
            toEntries = Collections.emptySet();
        }

        CollectionUtils.SetDiff<ArchiveEntry> diff = CollectionUtils.diffSetsBy(fromEntries, toEntries, new Transformer<String, ArchiveEntry>() {
            public String transform(ArchiveEntry entry) {
                return entry.getPath();
            }
        });

        SortedSet<ArchiveEntryComparison> entryComparisons = new TreeSet<ArchiveEntryComparison>(new Comparator<ArchiveEntryComparison>() {
            public int compare(ArchiveEntryComparison o1, ArchiveEntryComparison o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });

        for (ArchiveEntry fromOnly : diff.leftOnly) {
            entryComparisons.add(new ArchiveEntryComparison(fromOnly.getPath(), fromOnly, null));
        }

        for (CollectionUtils.SetDiff.Pair<ArchiveEntry> pair : diff.common) {
            entryComparisons.add(new ArchiveEntryComparison(pair.left.getPath(), pair.left, pair.right));
        }

        for (ArchiveEntry toOnly : diff.rightOnly) {
            entryComparisons.add(new ArchiveEntryComparison(toOnly.getPath(), null, toOnly));
        }

        return new GeneratedArchiveBuildOutcomeComparisonResult(association, entryComparisons);
    }
}