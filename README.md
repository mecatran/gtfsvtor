GTFSVTOR
========

An open-source GTFS validator, released under GPLv3.
See the [LICENSE file](LICENSE) for more information.

Project goals
-------------

- Fast
- Extensible
- Extensive coverage
- Backward-compatible with current "standard" validator

How to use GTFSVTOR
-------------------

GTFSVTOR is still beta, but should be usable as-is.
As the time of writing, all the tables and fields from the GTFS specifications are loaded,
except the two tables `translations.txt` and `attributions.txt`.
Not all validation rules of the legacy feedvalidator.py are implemented, however.
See the [TODO](TODO) file to check the few missing rules.

You can browse this [example report](https://mecatran.github.io/gtfsvtor/validation-results.html)
to see the validation result of the "verybad" dataset.

To use it:

- Download the latest release (available in the project github)
- Unzip the file somewhere
- Run gtfsvtor:

    ./gtfsvtor/bin/gtfsvtor --help
    ./gtfsvtor/bin/gtfsvtor [options] <GTFS file>

On Windows, use the provided `gtfsvtor.bat` file instead.

Please note that a Java JRE is required to run the application.

A sample config.properties file is included in the root of the project
if you want to configure the validation.

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
  | DELFI Germany   |  20385 | 561958 | 1992882 | 38519k |   8036k | ?             | 2m58s         |
  
**Note**: Performances comparisons should be fairly accurate now,
as GTFSVTOR do have all CPU-intensive validators implemented
(loading and conversion of stops, trips, times, shapes, calendars; shape linear indexing; calendar indexing;
stop spatial indexing; too fast travel checks, trip duplication detection, block ID overlap...).
Also GTFSVTOR has trip duplication detection enabled, whereas feedvalidator.py does not.

Docker
------

Run a dockerized GTFSVTOR from sources:

```sh
docker build -t gtfsvtor:latest .
docker run -rm -v <path_data_directory, e.g. $(PWD)>:/data -e TZ=Europe/Berlin gtfsvtor:latest <gtfs-file>
```

GTFSVTOR is executed in the mounted data-dir. If you'd like to use a custom config.properties, 
you may place it besides the gtfs file and supply `-c config.properties` as additional parameters.
Note that the timezone must be specified explicitly (via `-e TZ=<your timezone>`) to have correct timestamps 
reported in the validation-results.html.

Developer guide
---------------

**TODO**

- Code documentation
- Configuration
- Adding new validation rules
- Using GTFSVTOR as a library

