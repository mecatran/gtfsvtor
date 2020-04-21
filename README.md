GTFSVTOR
========

An open-source GTFS validator, released under GPLv3.
See the [LICENSE file](LICENSE) for more information.

Project goals
-------------

- Fast
- Extensible
- Extensive coverage

How to use GTFSVTOR
-------------------

GTFSVTOR is still beta, but should be usable as-is.
As the time of writing, all the tables and fields from the GTFS specifications are loaded,
except the two tables `translations.txt` and `attributions.txt`.
Not all validation rules of the legacy feedvalidator.py are implemented, however.
See the [TODO](TODO) file to check the few missing rules.

You can browse this [example report](https://mecatran.github.io/gtfsvtor/validation-report.html)
to see the validation result of the "verybad" dataset.

For now, as there are no released bundle, you must compile the validator yourself:

- Download the source code
- Install gradle
- Run `gradle assemble` in the root directory of the project
- Unzip the generated zip file (`gtfsvtor.zip` in `build/distributions`)
- Run gtfsvtor:

    ./bin/gtfsvtor --help
    ./bin/gtfsvtor [options] <GTFS file>

A sample config.properties file is included in the root of the project
if you want to configure the validation.

Performances
------------

Performance tests are done using:

- in-memory option (-m) enabled for feedvalidator.py
- duplicate trips detection option (-d) disabled for feedvalidator.py
- small memory-footprint GtfsStopTime implementation enabled for GTFSVTOR
- multi-threading validation enabled (--numThreads 8) for GTFSVTOR

  | GTFS            | Routes | Stops | Trips  | Times  | Shp pts | FeedValidator | GTFSVTOR      |
  |-----------------|--------|-------|--------|--------|---------|---------------|---------------|
  | MBTA Boston     |    236 |  9861 |  70446 |  1829k |    323k | 2m20s         | 7s            |
  | Montréal        |    229 |  9241 | 206069 |  7814k |    199k | 9m23s         | 19s           |
  | IDFM Paris      |   1870 | 63471 | 467457 | 10564k |       - | 57m50s        | 36s           |
  | OV Netherlands  |   2703 | 63995 | 787736 | 16103k |   3384k | ?             | 1m50s         |

**Note**: Performances comparisons should be fairly accurate now,
as GTFSVTOR do have all CPU-intensive validators implemented
(loading and conversion of stops, trips, times, shapes, calendars; shape linear indexing; calendar indexing;
stop spatial indexing; too fast travel checks, trip duplication detection, block ID overlap...).
Also GTFSVTOR has trip duplication detection enabled, whereas feedvalidator.py does not.

Developer guide
---------------

**TODO**

- Code documentation
- Configuration
- Adding new validation rules
- Using GTFSVTOR as a library
