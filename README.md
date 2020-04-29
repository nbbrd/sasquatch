# Sasquatch - SAS dataset library for Java

[![Download](https://img.shields.io/github/release/nbbrd/sasquatch.svg)](https://github.com/nbbrd/sasquatch/releases/latest)

This [Java library](#java-library) provides a reader for SAS datasets.  
It also provides a [command-line tool](#command-line-tool) and a [desktop application](#desktop-application).

Key points:

- lightweight library designed as a [facade](https://en.wikipedia.org/wiki/Facade_pattern)
- Java 8 minimum requirement
- has a module-info that makes it compatible with [JPMS](https://www.baeldung.com/java-9-modularity) 

Features:

- reads meta and data from SAS datasets (*.sas7bdat)
- browses data with 3 types of cursor: forward-only, scrollable and splittable
- is compatible with Java Stream API
- provides a simple facade that allows to plug in any implementation at deployment time
- implies the addition of a single mandatory dependency

## Java library

### API overview

Sasquatch is instantiated by a factory:

```java
Sasquatch sasquatch = Sasquatch.ofServiceLoader();
```

It provides 3 ways of browsing the data:
- forward-only: row by row from the first to the last
- scrollable: any row by its position
- splittable: rows as a (parallel) stream

```java
Path file = ...;

// forward-only cursor
try (SasForwardCursor cursor = sasquatch.readForward(file)) {
    while (cursor.next()) {
    }
}

// scrollable cursor
try (SasScrollableCursor cursor = sasquatch.readScrollable(file)) {
    for (int i = 0; i < cursor.getRowCount(); i++) {
        cursor.moveTo(i);
    }
}

// splittable cursor
try (SasSplittableCursor cursor = sasquatch.readSplittable(file)) {
    Stream<SasRow> stream = StreamSupport.stream(cursor.getSpliterator(), false);
}
```
Some shortcuts are also available:

```java
// sample factory that extracts the first field as a string
SasRow.Factory<String> factory = cursor -> row -> row.getString(0);

// stream shortcut
try (Stream<String> stream = sasquatch.rows(file, factory)) {
}

// list shortcut
List<String> rows = sasquatch.getAllRows(file, factory);
```
Metadata can be retrieved directly or through a cursor:
```java
// direct
SasMetaData meta = sasquatch.readMetaData(file);

// through a cursor
try (SasCursor cursor = sasquatch.read...(file)) {
    cursor.getMetaData();
}
```

### Implementations

At least one implementation must be available at runtime (on classpath or modulepath) in order to read datasets. No implementation triggers an `IOException` on read operations.

Sasquatch supports the following implementations:

| artifactId | description | support |
| --- | --- | :-: |
| `sasquatch-ri` | native reference implementation | advanced |
| `sasquatch-parso` | wrapper around parso library | advanced |
| `sasquatch-sassy` | wrapper around sassy library | basic |
| `sasquatch-biostatmatt` | java version of biostatmatt r code | basic |

Feature matrix:

| | `ri` | `parso` | `sassy` | `biostatmatt` |
| --: | :-: | :-: | :-: | :-: |
| `BIG_ENDIAN_32` | x | x | - | - |
| `LITTLE_ENDIAN_32` | x | x | x | x |
| `BIG_ENDIAN_64` | x | x | - | - |
| `LITTLE_ENDIAN_64` | x | x | - | x |
| `ATTRIBUTES` | x | x | - | x |
| `LABEL_META` | x | x | - | - |
| `FIELD_ENCODING` | x | x | - | - |
| `COLUMN_ENCODING` | x | x | - | - |
| `CHAR_COMP` | x | x | - | - |
| `BIN_COMP` | x | x | - | - |
| `DATE_TYPE` | x | x | - | - |
| `DATE_TIME_TYPE` | x | x | - | - |
| `TIME_TYPE` | x | x | - | - |
| `CUSTOM_NUMERIC` | x | x | x | - |
| `COLUMN_FORMAT` | x | x | - | - |

### Dependencies setup

```xml
<dependencies>
  <dependency>
    <groupId>be.nbb.rd</groupId>
    <artifactId>sasquatch-api</artifactId>
    <version>LATEST_VERSION</version>
  </dependency>
  <dependency>
    <groupId>be.nbb.rd</groupId>
    <artifactId>sasquatch-ri</artifactId>
    <version>LATEST_VERSION</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>

<repositories>
  <repository>
    <id>oss-jfrog-artifactory-releases</id>
    <url>https://oss.jfrog.org/artifactory/oss-release-local</url>
    <snapshots><enabled>false</enabled></snapshots>
  </repository>
</repositories>
```

## Command-line tool

The command-line tool (`sasquatch` in `sasquatch-cli` project) allows to export a SAS dataset to a CSV or SQL file.

```bash
$ sasquath csv somedata.sas7bdat -o somedata.csv
$ sasquath sql somedata.sas7bdat -o somedata.sql
```

## Desktop application

The desktop application (`sasquatchw` in `sasquatch-desktop` project) is a basic dataset viewer.
