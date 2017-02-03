# Maven Repository Migrator
This tool splits the content of a [Maven](https://maven.apache.org/) repository with the version policy mixed into two folders, one containing the release artifacts, and the other the snapshot artifacts.   

## Usage description
Usage: `java -jar -Dpath=[PATH] [other properties] maven-repo-migrator-<VERSION>-jar-with-dependencies.jar`

Properties:

     path
        Path to the repository that should be split.
     newReleaseRepo
        If set to true the release artifacts will be copied to a new folder, otherwise the operations will be executed directly on the source repository. 
        Default: true
     removeSnapshotsWithRelease
        If set to true all snapshots with a matching release version won't be copied or they get removed from the source repository. 
        Default: false

