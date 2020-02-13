GTFSVTOR
========

An open-source GTFS validator, released under GPLv3.
See the [LICENSE file](LICENSE) for more information.

Project goals
-------------

- Fast
- Extensible
- Extensive coverage

How to use GTFSVOR
------------------

GTFSVTOR is still beta, but should be usable as-is.
Not all validation rules of the legacy feedvalidator.py are implemented, however.
See the [TODO](TODO) file to check what rules are missing.

For now, as there are no released bundle, you must compile the validator yourself:

- Download the source code
- Install gradle
- Run `gradle assemble` in the root directory of the project
- Unzip the generated zip file (`gtfsvtor.zip` in `build/distributions`)
- Run gtfsvtor:

    ./bin/gtfsvtor <GTFSFILE>

A sample config.properties file is included in the root of the project
if you want to configure the validation.

Performances
------------

Performance tests are done using:

- in-memory option (-m) enabled for feedvalidator.py
- duplicate trips detection option (-d) disabled for feedvalidator.py
- small memory-footprint GtfsStopTime implementation enabled for GTFSVTOR

  | GTFS        | Routes | Stops | Trips  | Times  | Shp pts | FeedValidator | GTFSVTOR      |
  |-------------|--------|-------|--------|--------|---------|---------------|---------------|
  | MBTA Boston |    236 |  9861 |  70446 |  1829k |    323k | 2m20s         | 10s           |
  | IDFM Paris  |   1870 | 63471 | 467457 | 10564k |       - | 57m50s        | 50s           |

**Note**: Performances comparisons are not 100% accurate as GTFSVTOR do not have all validators implemented,
some of them can be a bit slow (block ID overlap, trip duplication detection).
However most of the CPU-intensive work is implemented in GTFSVOR as the time of writing
(loading and conversion of stops, trips, times, shapes, calendars; shape linear indexing; calendar indexing;
stop spatial indexing; too fast travel checks...)

Developer guide
---------------

**TODO**

- Code documentation
- Adding new validation rules
- Using GTFSVTOR as a library
