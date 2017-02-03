package de.gamue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class Main {
	private static final String	RELEASES_SUFFIX	= "-RELEASES";
	private static final String	SNAPSHOT_SUFFIX	= "-SNAPSHOT";

	private static final String	PARAMETER_REPO_SOURCE_PATH			= "path";
	private static final String	PARAMETER_REMOVE_SNAPSHOTS			= "removeSnapshotsWithRelease";
	private static final String	PARAMETER_CREATE_NEW_RELEASE_REPO	= "newReleaseRepo";

	public static void main(final String[] args) throws IOException {

		final String repoSourcePath = System.getProperty(PARAMETER_REPO_SOURCE_PATH);
		Validate.notBlank(repoSourcePath, "No path given");

		final File sourceRepo = new File(repoSourcePath);
		Validate.isTrue(sourceRepo.exists(), "path does not exist");

		final boolean removeSnapshotsWithReleases = Boolean.parseBoolean(System.getProperties().getProperty(PARAMETER_REMOVE_SNAPSHOTS, "false"));
		final boolean createNewReleaseRepo = Boolean.parseBoolean(System.getProperties().getProperty(PARAMETER_CREATE_NEW_RELEASE_REPO, "true"));

		System.out.println("### START ###");
		System.out.println("path=" + sourceRepo + "; createNewReleaseRepo=" + createNewReleaseRepo + "; removeSnapshotsWithReleases="
				+ removeSnapshotsWithReleases);

		final Collection<File> allArtifactDirs = getArtifactDirs(sourceRepo);
		final Collection<File> snapshotDirs = getSnapshotDirectories(allArtifactDirs);
		final Collection<File> snapshotsWithRelease = getSnapshotsWithReleases(snapshotDirs);

		if (createNewReleaseRepo) {
			copyReleaseArtifactsToNewReleaseRepo(sourceRepo, allArtifactDirs, snapshotDirs);
		}

		if (removeSnapshotsWithReleases) {
			if (createNewReleaseRepo) {
				snapshotDirs.removeAll(snapshotsWithRelease);
			} else {
				for (final File file : snapshotsWithRelease) {
					FileUtils.deleteDirectory(file);
				}
			}
			System.out.println("Removed " + snapshotsWithRelease.size() + " SNAPSHOT-artifacts with matching release artifacts.");
		}

		copySnapshotsToNewSnapshotRepo(sourceRepo, snapshotDirs);

		if (!createNewReleaseRepo) {
			removeSnapshotsInSource(snapshotDirs);
		}

		System.out.println("### END ###");
	}

	private static void copyReleaseArtifactsToNewReleaseRepo(final File sourceRepo, final Collection<File> allArtefactDirs, final Collection<File> snapshotDirs)
			throws IOException {
		final String workingReleaseRepoPath = sourceRepo + RELEASES_SUFFIX;
		int count = 0;
		for (final File file : allArtefactDirs) {
			if (!snapshotDirs.contains(file)) {
				String targetPath = file.getAbsolutePath();
				targetPath = targetPath.replace(sourceRepo.getAbsolutePath(), workingReleaseRepoPath);
				FileUtils.copyDirectory(file, new File(targetPath));
				count++;
			}
		}
		System.out.println("Copied " + count + " release artifacts to new repository " + workingReleaseRepoPath);
	}

	private static Collection<File> getArtifactDirs(final File sourceRepo) {
		final Collection<File> allSubdirs = FileUtils.listFilesAndDirs(sourceRepo, DirectoryFileFilter.DIRECTORY, TrueFileFilter.TRUE);

		final Collection<File> artefactDirs = new HashSet<File>();
		for (final File dir : allSubdirs) {
			boolean hasSubDir = false;
			for (final File file : dir.listFiles()) {
				if (file.isDirectory()) {
					hasSubDir = true;
					break;
				}
			}
			if (!hasSubDir) {
				artefactDirs.add(dir);
			}
		}

		return artefactDirs;
	}

	private static void removeSnapshotsInSource(final Collection<File> snapshotDirs) throws IOException {
		int count = 0;
		for (final File file : snapshotDirs) {
			if (file.exists()) {
				FileUtils.deleteDirectory(file);
				count++;
			}
		}
		System.out.println("Removed " + count + " SNAPSHOT artifacts in source repository");
	}

	private static void copySnapshotsToNewSnapshotRepo(final File sourceRepo, final Collection<File> snapshotDirs) throws IOException {
		final String snapshotRepoPath = sourceRepo + SNAPSHOT_SUFFIX;
		int count = 0;
		for (final File file : snapshotDirs) {
			if (file.exists()) {
				String targetPath = file.getAbsolutePath();
				targetPath = targetPath.replace(sourceRepo.getAbsolutePath(), snapshotRepoPath);
				FileUtils.copyDirectory(file, new File(targetPath));
				count++;
			}
		}
		System.out.println("Copied " + count + " SNAPSHOT artifacts to new repository " + snapshotRepoPath);
	}

	private static Collection<File> getSnapshotsWithReleases(final Collection<File> allSnapshotDirs) {
		final Collection<File> snapshotWithReleases = new HashSet<File>();
		for (final File file : allSnapshotDirs) {
			final String dirWithoutSnapshot = StringUtils.removeEnd(file.getAbsolutePath(), SNAPSHOT_SUFFIX);
			if (new File(dirWithoutSnapshot).exists()) {
				snapshotWithReleases.add(file);
			}
		}
		return snapshotWithReleases;
	}

	private static Collection<File> getSnapshotDirectories(final Collection<File> allSubdirs) {
		final Collection<File> allSnapshotDirs = new HashSet<File>();
		for (final File file : allSubdirs) {
			if (file.getName().endsWith(SNAPSHOT_SUFFIX)) {
				allSnapshotDirs.add(file);
			}
		}
		return allSnapshotDirs;
	}
}
