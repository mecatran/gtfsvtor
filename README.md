GTFSVTOR
========

An open-source GTFS validator, released under GPLv3.
See the [LICENSE file](LICENSE) for more information.

![Java CI](https://github.com/mecatran/gtfsvtor/workflows/Java%20CI/badge.svg)

Online public validator
-----------------------

**New!** A free online validator is hosted here: [https://gtfsvtor.mecatran.com/](https://gtfsvtor.mecatran.com/).

Features and project goals
--------------------------

- Fast
- Memory-efficient, ability to process very large GTFS
- Extensible, code easy to read and maintain
- Extensive coverage of validation
- Backward-compatible with the historical "python" validator
- Various outputs (json, html...)

How to use GTFSVTOR
-------------------

GTFSVTOR is in a workable state.

As the time of writing, all the tables and fields from the GTFS specifications are loaded.

Almost all validation rules of the legacy feedvalidator.py are implemented.
See the [list of issues in github](https://github.com/mecatran/gtfsvtor/issues?q=is%3Aopen+is%3Aissue+label%3Abackward-compat) to check the few missing rules.
Other (new or refined) rules have also been added, aiming for backward-compatibility when possible.
The most notable backward-incompability is the taking into account of exact shapes in computing too fast travels;
however this change in behavior increase the accuracy and reliability of too fast travels validation.

You can browse this [HTML example report](https://mecatran.github.io/gtfsvtor/validation-results.html)
to see the validation result of the "verybad" dataset.
Also see this [JSON example report summary](https://raw.githubusercontent.com/mecatran/gtfsvtor/master/docs/validation-results.json).

To use it:

- Download the latest release (available in the project github)
- Unzip the file somewhere
- Run gtfsvtor:

```
    ./gtfsvtor/bin/gtfsvtor --help
    ./gtfsvtor/bin/gtfsvtor [options] <GTFS file>
```

On Windows, use the provided `gtfsvtor.bat` file instead.

Please note that a Java JRE is required to run the application.

A sample config.properties file is included in the root of the project
if you want to configure the validation (see [Configuration](#configuration) section below).

For large GTFS, you can increase the default JVM heap size by setting
the appropriate JVM options in the `GTFSVTOR_OPTS` variable:

    GTFSVTOR_OPTS=-Xmx6G ./gtfsvtor/bin/gtfsvtor <GTFS file>

Also, unzipping data on disk can reduce memory usage for large GTFS.

Performances
------------

Performance tests are done using:

- in-memory option (-m) enabled for feedvalidator.py
- duplicate trips detection option (-d) disabled for feedvalidator.py
- small memory-footprint GtfsStopTime implementation enabled for GTFSVTOR
- multi-threading validation enabled (--numThreads 8) for GTFSVTOR

  | GTFS            | Routes | Stops  | Trips   | Times  | Shp pts | FeedValidator | GTFSVTOR      |
  |-----------------|--------|--------|---------|--------|---------|---------------|---------------|
  | MBTA Boston     |    236 |   9861 |   70446 |  1829k |    323k | 2m20s         | 8s            |
  | Montréal        |    229 |   9241 |  206069 |  7814k |    199k | 9m23s         | 20s           |
  | IDFM Paris      |   1870 |  63471 |  467457 | 10564k |       - | 57m50s        | 40s           |
  | OV Netherlands  |   2703 |  63995 |  787736 | 16103k |   3384k | ?             | 1m50s         |
  | DELFI Germany   |  20656 | 565016 | 2161712 | 41971k |   8322k | ?             | 2m44s         |
  
**Note**: Performances comparisons should be fairly accurate now,
as GTFSVTOR do have all CPU-intensive validators implemented
(loading and conversion of stops, trips, times, shapes, calendars; shape linear indexing; calendar indexing;
stop spatial indexing; too fast travel checks, trip duplication detection, block ID overlap...).
Also GTFSVTOR has trip duplication detection enabled, whereas feedvalidator.py does not.

Docker
------

Run a dockerized GTFSVTOR using the [`laurentgregoire/gtfsvtor` Docker image](https://hub.docker.com/r/laurentgregoire/gtfsvtor):

```sh
docker run -rm -v <path_data_directory, e.g. $(PWD)>:/data -e TZ=Europe/Berlin laurentgregoire/gtfsvtor /data/<gtfs-file>
```

GTFSVTOR is executed in the mounted `/data` dir. If you'd like to use a custom config.properties, 
you may place it besides the gtfs file and supply `-c config.properties` as additional parameters.
Note that the timezone must be specified explicitly (via `-e TZ=<your timezone>`) to have correct timestamps 
reported in the validation-results.html.

Configuration
-------------

You can configure GTFSVTOR by editing a config file in the project root folder.
You should specify which config file to load by using the `--config` command-line option.
See the provided [config.properties](config.properties) example.

You can disable a validator (or enable a validator disabled by default) by writing:

    validator.SomeValidator.enabled = true
    validator.SomeOtherValidator.enabled = false

When `SomeValidator` is the class name of the validator to enable/disable.

Use the `--listValidators` command-line option to list all validators and their options.

To configure a validator option, write for example:

    validator.CalendarValidator.expiredCutoffDate = 2020/12/31

This example will configure the feed expiry cutoff date to the specified date
(for information by default the default expiry date is "today").

Bug tracking
------------

If you experience a bug, please create a ticket in the issue page of the GTFSVTOR github project [here](https://github.com/mecatran/gtfsvtor/issues/new).

Please follow standard best-practices by providing:

- A short summary of the bug
- What is wrong
- What is expected
- The version of GTFSVTOR used
- The options used (command-line options if any, config file if any)
- A (minimal) example of (GTFS) data that experience the behavior (or a link to this data)
- If relevant to the bug, your environment (JVM & OS type and version...)

Create a distinct issue per different bug.

Developer guide
---------------

**TODO**

- Code documentation
- Adding new validation rules
- Using GTFSVTOR as a library

