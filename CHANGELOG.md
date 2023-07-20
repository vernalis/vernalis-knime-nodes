# Changelog

## Latest Versions by KNIME Analytics Platform (Desktop) Version
The table shows the latest version of the nodes available in each update site for the KNIME Analytics Platform


| KNIME AP Desktop Version | Vernalis Plugin Version |
| :------------------------: | :-----------------------: |
| Nightly | [v1.36.4](#v1364---20-july-2023) |
| 5.1* | [v1.36.4](#v1364---20-july-2023) |
| 4.7* | [v1.36.4](#v1364---20-july-2023) |
| 4.6 | [v1.36.3](#v1363---15-may-2023) |
| 4.5 | [v1.36.1](#v1361---11-nov-2022) |
| 4.4 | [v1.34.1](#v1341---07-jun-2022) |
| 4.3 | [v1.31.0](#v1310---05-apr-2022) |
| 4.2 | [v1.31.0](#v1310---05-apr-2022) |
| 4.1 | [v1.30.1](#v1301---30-mar-2021) |
| 4.0 | [v1.27.0](#v1270---18-aug-2020) |
| 3.7 | [v1.27.0](#v1270---18-aug-2020) |
| 3.6 | [v1.18.1](#v1181---20-dec-2018) |
| 3.5 | [v1.15.0](#v1150---31-may-2018) |
| 3.4 | [v1.12.1](#v1121---20-oct-2017) |
| 3.3 | [v1.12.0](#v1120---21-jul-2017) |
| 3.2 | [v1.12.0](#v1120---21-jul-2017) |
| 3.1 | [v1.8.2](#v182---16-nov-2016) |
| 3.0 | [v1.6.5](#v165---30-jun-2016) |
| 2.12 | [v1.5.0](#v150---11-nov-2015) |
| 2.11 | [v1.5.0](#v150---11-nov-2015) |
| 2.10 | [v1.4.0](#v140---12-may-2015) |
| 2.9 | [v1.1.5](#v115---04-nov-2014) |
| 2.8 | [v1.1.4](#v114---05-aug-2014) |
| 2.7 | [v1.0.4](#v104---30-jan-2014) |

\* Currently supported versions

Back to [ReadMe](README.md)

## v1.36.4 - 20-July-2023

* KNIME 5.1 Compatibility added

Back to [top](#changelog)


## v1.36.3 - 15-May-2023

_Bug Fixes_
* Fixes error in complexity estimation when a single bond cut is not being made (See #20)

Back to [top](#changelog)

## v1.36.2 - 13-Jan-2023

_Enhancements_
* Add option to fail if no collection columns selected in Collection multi-selection-based Collection nodes with default behaviour to mimic legacy (#18)
* Abstracted common code from all multi-selection-based Collection nodes to abstract Node Dialog and Node Model classes
* Node Dialog layout improvements to Collection nodes

_Bug Fixes_
* Added feature dependency to RDKit Binaries feature to enforce installation (#17)
* Fixes addition of default value in Sparse List representation in List to Set node when that value is not actually present in the collection (#19)

_Other Changes_
* Updated to build as JDK17
* Updated to require RDKit v 4.6.1 or higher

Back to [top](#changelog)

## v1.36.1 - 11-Nov-2022

_Bug Fixes_
* Fixed 2020 upper bounds on PDB Connector Date query fields (See #16)

Back to [top](#changelog)

## v1.36.0 - 07-Sep-2022

_New Nodes_
* See also under _Bug Fixes_
* DB Port Comparator
* DB DISTINCT
* DB Extract Table Dimension
* DB List Catalogues
* DB Insert Column Header
* DB List Foreign Keys
* DB List Primary Keys
* DB List Schemas
* DB List Table Types
* DB List Tables
* DB Numeric Range
* DB Remove SQL from connection
* DB Select Table (Interactive)
* DB Table Exists IF Switch
* DB To Empty Table
* DB To Variable
* Reference DB Table Exists IF Switch

_Enhancements_
* Node Migration Rules added for all database 'Legacy' / 'DB' node pairs to allow workflow migration
* DB Table Exists IF Switch is configurable - either or both output ports can be swapped between flow variable and DB Session port types
* Reference DB Table Exists IF Switch is configurable - the table input/outputs can be swapped for any other port type

_Other Changes_
* All database nodes have been renamed with the suffix '(Legacy)' following the KNIME core database style
* New `com.vernalis.database.core` plugin
* New `com.vernalis.knime.testing.core` plugin for maven-execution of test cases
* New annotations in `com.vernalis.knime.core` plugin (`@NoTest`, `@NodeTestFlow`, `@NodeTestFlows`)
* New `.tests` fragment for JUnit tests and Workflow Tests for the corresponding plugin for the plugins `com.vernalis.knime.database.core`, `com.vernalis.knime.testing` and `com.vernalis.knime.db`
* New `com.vernalis.knime.testing` plugin with nodes for test flows
* Moved `Empty Column Checker` node from flow control plugin to testing plugin (and NodeFactoryClassMapper supplied)
* Updated builds to use Tycho v 2.7.5

_Bug Fixes_
* Fixed missing node icons for various 'database' nodes
* Added missing 'Database replace table header' node to plugin.xml
* Added missing 'Database Remove SQL' node to plugin.xml

Back to [top](#changelog)


## v1.35.0 - 02-Sep-2022
_New Nodes_
* Speedy Sequence to SMILES node
* Speedy Sequence Analysis node
* Speedy Sequence Alignment Visualisation node

_Bug Fixes_
* PDB Connector dynamic 'suggester' dropdowns fixed following remote API change
* Removed much unused code from original PDB Connector implementation

Back to [top](#changelog)


## v1.34.2 - 17-Jun-2022
_Minor Updates_
* Renamed AbstractDrawableSeriesPlotNodeDescription#getShortDescription() 
    to #getShortDescriptionImpl() due to name clash with new method in updated parent
* Updated Jackson dependency versions to avoid security CVEs in PDB Connector plugin

Back to [top](#changelog)


## v1.34.1 - 07-Jun-2022
_Bug Fixes_
* Minor bug fix to Regex String Match flow variable condition reference value box
* Minor bug fix to Wildcard String Match flow variable condition reference value box

Back to [top](#changelog)


## v1.34.0 - 23-May-2022
_New Nodes_
* Added `Ertl Scaffold Keys` node

_Enhancements_
* Added new 'streams' package in core plugin
	* Added `DataCellCollectors` class to streams package
	* Added `ArrayOfListCellCollector` class to streams package
	* Added `ToListCellCollector` class to streams package
	* Added `BitSetCollectors` class to streams package
	* Added `NonOverlappingBitSetCollector` class to streams package
	* Added `OverlappingBitSetGroupingCollector` class to streams package
* Added `getRDKitObjectFromCell(DataCell cell, boolean sanitizeMol, boolean removeHs` static method to RdkitCompatibleColumnFormats enum in chem.core plugin
* Added new `com.vernalis.knime.chem.rdkit` plugin

## v1.33.0 - 19-May-2022
_New Nodes_
  * Added Column to Singleton Collection node
  * Added Mask Lists Node
  * Added GZip Compress Binary Object node
  * Added GZip Decompress (Un-gzip) Binary Object node
  * Added Zip Binary Object node
  * Added UnZip Binary Object node
  
_Enhancements_
  * Added RemoveColumnsDataRow class to core plugin

Back to [top](#changelog)


## v1.32.0 - 18-May-2022
_New Nodes_ 
* Configurable Empty Table Switch
* Configurable IF/Case Switch (Flow Variable Value)
* Configurable IF/Case Switch
* Configurable Crossover (Flow Variable Value)
* Configurable End IF/Case

_New Features (See also API section)_
* New flow variable conditions for String, String Array, Integer, Integer Array, Long, Long Array, Double, Double Array, Boolean, Boolean Array and Path variable types
* New Port Combiners for Buffered Data Table, Flow Variable and RCSB Advanced Query port types
  
_Deprecated Nodes_
* Deprecated all IF Switch / Case Switch, End IF and End Case nodes
  
_API_
* Added 2 new extension points:
  * `com.vernalis.knime.flowcontrol.variablecondition` (Variable Condition) - Used to 
  		provide conditions to the Flow Variable value based nodes
  * `com.vernalis.knime.flowcontrol.porttypecombiner` (Port Type Combiner) - Used to allow merging of multiple active
  		ports in Configurable End IF/Case nodes 
  
_Enhancements_
* Fixed links in main CHANGELOG.md summary table

Back to [top](#changelog)


## v1.31.0 - 05-Apr-2022
_New Nodes / Features_
* Added UI Selection modifier tools

_Enhancements_
* Updated to switch to v2 of RCSB Query API automatically on 13-Apr-2022
* Made verbose JSON output optional in Query Executor node
* Modified SettingsModelRegistry implementation of QueryExecutorNodeModel to allow saved model versioning
* SettingsModelRegistry has new methods to allow saving of model versions, and adding backwards compatible new versions (Changes made backwards compatible)
* AbstractSimpleStreamableFunctionNodeModel updated to match new SettingsModelRegistry
* Added SettingsModelRegistryImpl class and SettingsModelWrapper class

Back to [top](#changelog)


## v1.30.4 - 04-Feb-2022
* Updates for changes in RDKit v4.5.0 ([#14](https://github.com/vernalis/vernalis-knime-nodes/issues/14))
* Fixes bug when show loop body node timings option is checked for benchmark nodes not at root level of workflow ([#15](https://github.com/vernalis/vernalis-knime-nodes/issues/15))

Back to [top](#changelog)


## v1.30.3 - 02-Dec-2021
* Bug fixes and feature enhancements to PDB Connector
  * Updated for changes to remote query API
  * Centralised handling of QueryPanel borders
  * Added RemovableQueryField and associated RemoveMe button
  * Only apply border highlights when mouse-over component is active
  * Added code to allow for removal of QueryFields from API and display of invalid fields
  * Added new chemical descriptor query types
  * Only display valid scoring types in Query Executor node when a query is available at the incoming port

Back to [top](#changelog)


## v1.30.2 - 11-May-2021
* Made all `NodeFactoryClassMapper`s subclass `MapNodeFactoryClassMapper`
* Fixed bug whereby `MolSanitizeException` thrown in fragment canonicalisation throws MMP Fragment nodes over
* Added optional hit count to PDB Connector Query Executor node
* Moved some of the query execution control from the node model to the query executor
* Made all RCSB.org service calls point to `https://` rather than `http://`

Back to [top](#changelog)


## v1.30.1 - 30-Mar-2021
* Fixed various bugs in RDKit Templated conformer generation node

Back to [top](#changelog)


## v1.30.0 - 04-Mar-2021
_New Nodes_
* KNIME URI Resolver
* KNIME URI Resolver (Variable)

Back to [top](#changelog)


## v1.29.0 - 19-Feb-2021
_New Nodes_
* Multiport Loop End

_Enhancements_
* Multiport Loop End, 3-Port Loop End, Upto 4-port loop end and Upto 6 port Loop end nodes have table preview view

_Deprecated Nodes_
* Loop End (3 Ports)
* Loop End (Upto 4 Ports)
* Loop End (Upto 6 Ports)

_Other_
* Added `FixedLengthQueue` to collections package in core plugin

Back to [top](#changelog)


## v1.28.3 - 15-Feb-2021
* Added retries to failed download in Query Execution (Fixes [#9](https://github.com/vernalis/vernalis-knime-nodes/issues/9))
* Added optional limit to returned hits in Query Execution Node (Closes [#10](https://github.com/vernalis/vernalis-knime-nodes/issues/10))
* Added Substructure chemical query types (Closes [#11](https://github.com/vernalis/vernalis-knime-nodes/issues/11))
* PDB Saver node now correctly handles `knime://` URL protocol in both full paths in columns and in parent folder settings ([#12](https://github.com/vernalis/vernalis-knime-nodes/issues/12))
* Save File Locally node now correctly handles `knime://` URL protocol in both full paths in columns and in parent folder settings (Fixes [#12](https://github.com/vernalis/vernalis-knime-nodes/issues/12))

Back to [top](#changelog)


## v1.28.2 - 28-Jan-2021
* Fixes [#8](https://github.com/vernalis/vernalis-knime-nodes/issues/8) (Over-zealous sequence query validation)
* Removes unused dependency in SpeedySMILES

Back to [top](#changelog)


## v1.28.1 - 22-Jan-2021
Minor enhancements and bug fixes to MMP plugin:
* Added rdkit jar to extra jars in classpath
* Corrected bug in key HAC calculation
* Added Filter for minimum key HAC

Back to [top](#changelog)


## v1.28.0 - 18-Dec-2020
* Retired original PDB Connector family
* New PDB Connector query nodes

Back to [top](#changelog)


## v1.27.4 - 01-Dec-2020
* Add `RDKitRuntimeExceptionHandler` class to allow RDKit version before 4.1 or after 4.1 to work seemlessly

Back to [top](#changelog)


## v1.27.3 - 30-Oct-2020
* Requires RDKit Types plugin v4.1.0 or higher
* RDKit exception handling updated to use `#what()` method in place of removed `#message()` method

Back to [top](#changelog)


## v1.27.2 - 19-Oct-2020
* Complete fix of PDB Connector issue (v1.27.1 did not fully fix)

Back to [top](#changelog)


## v1.27.1 - 16-Oct-2020
* Added optional 'new' attribute to query option and ignored settings exceptions for query options so tagged (Fixes [#2](https://github.com/vernalis/vernalis-knime-nodes/issues/2))

Back to [top](#changelog)


## v1.27.0 - 18-Aug-2020
_New Nodes_
* Append to Collection
* Insert into List
* Credential Variable Extractor (see https://forum.knime.com/t/credentials-input-node-extract-username/25079)

_Bux Fixes / Enhancements_
* PMI Properties node now works with two-character element symbols e.g. Cl, Br etc (see https://forum.knime.com/t/vernalis-pmi-node-failure/24979/28)
* List Folders node no longer throws `NullPointerException` (see https://forum.knime.com/t/id-like-to-download-files-from-s3-to-knime-server/23289/5?u=s.roughley)
* Memory monitoring performance loop nodes re-written to remove possible IOExceptions due to table ownership of memory usage table
* Performance loop end nodes now allow use of new or legacy date-time systems in timing and memory usage output tables
* PMI calculation errors no longer causes node execution failure
* `SettingsModelIntegerRange#createClone()` now returns correct type
* Added all missing version constraints in `MANIFEST.MF`

Back to [top](#changelog)


## v1.26.0 - 16-Mar-2020
* Bug-fixes and minor enhancements to PDB Connector family of nodes
  * Fixed broken date query fields (Deposition Date, Release Date, Revision Date)
  * Added searches for Entity IDs, Chain IDs, Uniprot Gene Names
  * Fixed mouse scroll speed

Back to [top](#changelog)


## v1.25.0 - 09-Mar-2020
_New Nodes_
* Templated Conformer Generation (RDKit)
* Force Field Conformer Energies
* RMSD Conformer List Filter

_Enhancements_
* Added `DialogComponentMultilineStringFlowvar` to core plugin
* Added `SettingsModelMultilineString` to core plugin

_Bug Fixes_
* Force requirement of RDKit v4.0.0
* Fixed bug in inject column properties node causing `IndexOutOfBoundsException` to be thrown if < 3 input columns present in the 2nd table
* Fixed bug in which PDB Connector Custom Report node overwrites xmlQuery flow variable. This bug causes crashes in KNIME 4.1 due to changes in flow variable handling

Back to [top](#changelog)


## v1.24.4 - 27-Nov-2019
* Fixed bug in Random Numbers Generator node in range of doubles returned
* Added optional random seed in Random Numbers Generator node
* Added summary flow variables to Random Numbers Generator node output

Back to [top](#changelog)


## v1.24.3 - 26-Nov-2019
* Updates RDKit Dependencies to include RDKit version 4.x.x

Back to [top](#changelog)


## v1.24.2 - 13-Nov-2019
* Corrected bug in Plate Nodes resulting in Well ID columns returning Row IDs
* Fixed bug in file reader nodes resulting in loss of final newline combination if no characters follow it
* Fixed bug in file reader nodes resulting in conversion of all newlines to unix-style '\n'

Back to [top](#changelog)


## v1.24.1 - 04-Nov-2019
* Read / Write variables nodes now support `knime:` URL protocol
* Fixed bug in Read Variables node resulting in file chooser showing 'Save' chooser and errors rather than 'Open'

Back to [top](#changelog)


## v1.24.0 - 21-Oct-2019
_New Nodes_
* Empty Collection to Missing Cell
* Missing Cell to Empty Collection
* Collection Size

_Bug Fixes_
* Thread safety issue parsing dates in Bruker Amix Peaklist reader and SD-file reader nodes
* ePMC Advanced Query node updated to v6.2 of webservice API

Back to [top](#changelog)


## v1.23.0 - 20-Sep-2019
_New Nodes_
* 1D Kernel Density Plot
* 2D Kernel Density Plot
* Kernel Loop Start
* Notched Boxplot
* PMI Kernel Density Plot

Back to [top](#changelog)


## v1.22.0 - 06-Sep-2019
_New Nodes_
* New PMI-Derived Properties Calculation node (accepts more column input formats, e.g. CTab and PDB), and many more properties
* PMI Scatter Plot
* PMI Convex Hull Plot

Back to [top](#changelog)


## v1.21.0 - 03-Sep-2019
_New Nodes_
* Trim Table node

_Enhancements_
* MMP Node Icons restored

Back to [top](#changelog)


## v1.20.5 - 12-Aug-2019
* Minor enhancement allow File Encoding Guess to work with non-markable InputStreams (see https://forum.knime.com/t/get-fails-if-the-response-does-not-have-content-type-set/17295/10 for original issue)

Back to [top](#changelog)


## v1.20.4 - 09-Jul-2019
* Changed RDKit dependency to v3.8.0 to force non-RDKit node users with Vernalis nodes to update past the RDKit 3.7.0 version which is broken on some platforms.

Back to [top](#changelog)


## v1.20.3 - 19-Jun-2019
* Update version constraints to `[3.0.0,5.0.0)` to accommodate update to KNIME v4.0.0
* Standardised licence text in feature.xml (defunct URL removed and linebreaks/indents standardised)

Back to [top](#changelog)


## v1.20.2 - 17-Jun-2019
* Fixes MMP bug https://forum.knime.com/t/mmp-molecule-fragment-rdkit-node-indexoutofboundsexception/15469/3

Back to [top](#changelog)


## v1.20.1 - 09-May-2019
* Minor bug fix to `Load text-based files` and `PDB Loader` nodes, which means that the removed input column names is not de-dupicated in the output table (e.g. 'File' as input column would be rendered 'File(#1)' in the out table, even when the 'Remove input column' option was checked)

Back to [top](#changelog)


## v1.20.0 - 02-May-2019
_New Nodes_
* URL Decode Node
* Set To List Node
* Collection To String Node
* Flow Variable Benchmarking start / end nodes (with/without memory monitoring)

_New Features_
* 'PDB Saver' / 'Save File Locally' nodes now allow option to specify parent folder / directory path to save all files to
* 'PDB Loader' / 'Load text-based files' nodes now allow removal of input column
(In both cases, legacy behaviour is retained in existing nodes, and new nodes default to existing behaviour)

_Bug fixes_
* 'PDB Saver' / 'Save File Locally' nodes now handle directory creation correctly when using multiple threads, preventing some files from being saved

Back to [top](#changelog)


## v1.19.0 - 28-Mar-2019
_New Nodes_
* Added Run Garbage Collector node
* Added Run Heavy Garbage Collector node

_Bug fixes / Enhancements_
* Major overhaul of performance monitoring plugin:
  * Refactored most nodes and rationalised class structure
  * Improved error handling for missing / mismatched loop starts
  * Optional monitoring of individual loop body node timings (preserves legacy behaviour by default)
  * Node descriptions now all created dynamically, allowing simplification of addition of further nodes with alternative port types
* Missing icons restored to MMP Nodes
* Fixed URL change for SMARTSViewer node
* Update core plugin about.ini file with current date
* Enhanced error handling in `MemoryUtils` class and corresponding nodes (Benchmarking loops and Memory use node)
* Allowed unix-style dates in 'Load Bruker AMIX Peaklist Files' node
* Added createTab method to `NodeDescriptionUtils`

Back to [top](#changelog)


## v1.18.3 - 08-Feb-2019
* Performance improvement in MMP Calculate Maximum Cuts (RDKit) node
* Removed unused imports in other MMP classes and fixed typos in javadoc

Back to [top](#changelog)


## v1.18.2 - 08-Jan-2019
* Fixes bug in international support for memory usage nodes
* Fixes bug in Mac OS support for memory usage nodes

Back to [top](#changelog)


## v1.18.1 - 20-Dec-2018
* Fixes compatibility issue in `NodeDescriptionUtils` class introduced in v 3.7.0 of KNIME Core

Back to [top](#changelog)


## v1.18.0 - 30-Nov-2018
_New Nodes_
* New Load FASTA node (accepts .fasta and .fasta.gz file extensions)
* Added URL Encode / URL Decode nodes
* Flow Control Crossover node
* Database DISTINCT node
* Extract Database Table Dimensions node
* Database to Empty table node
* Database Range node
* List Catalogues node
* List Foreign Keys node
* List Primary Keys node
* List Schemas node
* List Tables node
* List Table Types node
* Database to variable node
* Speedy SMILES Organic subset Filter / Splitter nodes
* Speedy SMILES s-Block metals Filter / Splitter nodes
* Speedy SMILES p-Block metals Filter / Splitter nodes
* Speedy SMILES d-Block metals Filter / Splitter nodes
* Speedy SMILES f-block metals Filter / Splitter nodes
* Speedy SMILES Cyclise end atoms node

_Enhancements_
* Load SDFiles node now accepts sd.gz and sdf.gz extensions
* Load XXX Nodes now report progress (Maybe misleading for .gz)
* Improved node dialog refresh performance of interactive database table selector node

_Bug Fixes_
* Random Numbers Generator bug fixed (2 instances of node shared output table spec causing errors at execution if they were producing different types)
* Fixed node dialog bug leading to settings not being displayed correctly in node dialog on load

Back to [top](#changelog)


## v1.17.3 - 01-Aug-2018
* Fixes further bugs in FASTA sequence extractor when '>' occurs other than at start of header line
* Updates RDKit changes in v3.4.0

Back to [top](#changelog)


## v1.17.2 - 23-Jul-2018
* Fixes bug in FASTA sequence extractor when '>' occurs other than at start of header line

Back to [top](#changelog)


## v1.17.1 - 23-Jul-2018
* Adds optional Flow variable input ports to all Load XXX files
* Adds Mol Block to output from Load SD-File (SDF) node
* Adds Output properties selection for the Multiline Text-Based Object nodes (SDF and Amix Peak List)

Back to [top](#changelog)


## v1.17.0 - 16-Jul-2018
* Added SDF file reader with charset detection/choice

Back to [top](#changelog)


## v1.16.0 - 08-Jun-2018
_New Nodes_
* Speedy SMILES Explicit Double Bond Geometry Filter/Splitter
* Extract / Inject column properties nodes
* 1D, 2D, 3D, nD Vector distance nodes
* Plate Well ID Nodes (Source and Manipulator)

Back to [top](#changelog)


## v1.15.0 - 31-May-2018
_New Nodes_
* Added Geometric Distance nodes (1D, 2D, 3D and nD)

Back to [top](#changelog)


## v1.14.1 - 31-May-2018
* Fixes minor bug in Database Table Selector node resulting loss of table name during node dialog reload

Back to [top](#changelog)


## v1.14.0 - 25-May-2018
_New Nodes_
* Adds Database plugin with database table chooser and table exists switches
* Adds Bruker Amix Peaklist reader
* Adds 'Include filename in output table' option in all 'Load ... files' nodes

Back to [top](#changelog)

## v1.13.0 - 18-May-2018
_New Nodes_
* New Speedy SMILES nodes

_Enhancements_
* Improved behaviour of new column name prefix/suffix
* Column name retained unchanged when input column is removed
* New `AbstractStreamableParallelisedFilterSplitterNodeModel` base class added to core plugin

Back to [top](#changelog)


## v1.12.10 - 17-May-2018
* Fixes bug in 'Load Local...' files node dialogue resulting in `NullPointerException` during initial configuration
* Fixes bug in 'Load Local...' files node dialogue meaning file selector buttons remained disabled once a flow variable had been used as file source, even after removing flow variable setting

Back to [top](#changelog)


## v1.12.9 - 20-Apr-2018
* Updates to RDKit 3.3.1 compatibility
  * Changed `RWMol#AddAtom()` and `RWMol#AddBond()` method signatures in deprecated nodes

Back to [top](#changelog)


## v1.12.8 - 16-Mar-2018
* Fixes MMP XML generation bug (https://forum.knime.com/t/mmp-molecule-fragment-node-output-svg-cells-and-system-locale/10315)
* Code clean-up removing many compiler warnings
* Corrected deprecation of FVVal IF Switch node missed previously to plugin.xml deprecation method

Back to [top](#changelog)


## v1.12.7 - 02-Mar-2018
* Efficiency improvements in RCSB Heterogen Details and SMILES Query nodes
* ePMC Advanced Query node now uses required `https:` protocol
* PDB Connector
  * New fields added to EM Report (EM Diffraction Resolution (A), EM Point Symmetry, EM Embedding, EM Staining, EM Vitrification, EM Additional Map)
  * Field emdbId replaced with new name (emdbMap), retaining old 'EMDB ID' column name for backwards compatibility
  * Field 'Specimen Type' removed in line with its removal at RCSB
  * Optional parameter 'new' added in report field XML definition DTD, which is parsed in class `ReportField2`.  If this flag is set, `InvalidSettingsExceptions` during node settings load/validate are ignored

Back to [top](#changelog)


## v1.12.6 - 28-Feb-2018
* Fixed version constraint and resolved code issues for RDKit drawing API change

Back to [top](#changelog)


## v1.12.5 - 28-Feb-2018
* Fixed bug in PDB Heterogen details webservice causing failure when molecular formula XML tag is empty
* Improved efficiency by making webservice result lazy-parse most fields

Back to [top](#changelog)


## v1.12.4 - 01-Dec-2017
* Fixed error in rendering of 1-cut 'reverse' fragmentations which flipped the key/value colours

Back to [top](#changelog)


## v1.12.3 - 24-Nov-2017
* Fixes bug in MMP Pair generation resulting in non-canonical transform generation (https://www.knime.com/forum/vernalis/mmpa-reverse-direction-transform-issue)
* Fixes bug in MMP fragmentation which counts `[H]` at end of a fragment SMILES as a heavy atom
* Fixes bug in SpeedySMILES counting `[H]` at end of fragment SMILES as heavy atom in HAC Count node
* Fixes bug in SpeedySMILES HAC Count in which attachment point (SMILES `*`) atoms where counted as heavy atoms
* Enhances SpeedySMILES HAC count to correctly handle isotopically labelled and charged explicit H atoms

Back to [top](#changelog)

## v1.12.2 - 21-Nov-2017
* Updated SMARTSviewer node to point to new URL
* Fixed bugs in PMI calculations leading to symmetrical Discs being categorised as Spheres, and then in incorrect position
  * Incorporated a tolerance in Cubic for comparison of D with 0
  * Corrected error in D=0 roots

Back to [top](#changelog)


## v1.12.1 - 20-Oct-2017
* Fixed bug in string comparison of Flow Variable Value If Switch nodes

Back to [top](#changelog)


## v1.12.0 - 21-Jul-2017
* Added 29 new fingerprint manipulation nodes
* Added new Aggregation operators for fingerprint manipulations in GroupBy and related nodes
* Deprecated original Vernalis Fingerprint Properties node

Back to [top](#changelog)


## v1.11.2 - 12-Jul-2017
_Minor bug fixes_
* Output Smiles Adapter Cells for all molecule outputs
* Output Types set to SmilesAdapterCell.RAW_TYPE via new constants in MMPConstants for all molecule outputs (makes compatible with Molecule Type Cast output)
* All nodes now recognise either SmilesValue or SmilesAdapterCells for SMILES input columns

Back to [top](#changelog)


## v1.11.1 - 11-Jul-2017
_Minor bug fixes_
* Require JavaSE1.8 RTE for all plugins
* MMP ProgressTableModel made threadsafe
* Apply Transform node has correct regex for selecting the 'Left' AP fingerprint column
* Apply Transform node handles, via warning, the situation when no fingerprint column properties are present (uses default settings and shows node warning)

Back to [top](#changelog)


## v1.11.0 - 07-Jul-2017
This is a major overhaul of the MMP Nodes which has been described at recent Cheminformatics SIGs and the Berlin Spring 2017 UGM.  Many of the original nodes have been deprecated, a number of new nodes and features added, and many of the new nodes are now streamable.

_All Nodes_
* Performance improvements
* New predefined fragmentation patterns ('MATSY', 'Peptide Sidechains', 'Nucleic Acid Sidechains')
* Improved rSMARTS validation
  * Now requires the bond between the two atoms to be both single and acyclic
  * Allows more complex SMARTS atoms matches (including recursive SMARTS)
  * No longer requires the SMARTS to be provided as an rSMARTS - although this is still supported for backwards compatibility
  * No longer requires atoms to be numbers in the SMARTS match

_Filtering (Updated)_
* Updated faster implementation
* Streamable
* Previous versions deprecated

_Fragmentation nodes (UPDATED)_
* Performance improvements
* Memory leaks fixed
* AP Fingerprint columns have properties describing creation settings to avoid mismatch
* Graph Distance AP fingerprint vectors added
* ID can now be the RowID
* HiLite mapping optionally implemented
* New Progress View
* Pass-through of data columns
* Rendering of fragmentation
* Complexity filter
* Behavioural change to remove explicit H's for 1 cut option
* Additional option regarding incoming explicit H's
* Streamable
* '3rd Gen' version deprecated

_Pair Generation (UPDATED & NEW)_
* Performance improvements including parallelisation
* HiLite mapping optionally implemented
* Transforms can be filtered by
* Heavy-Atom-Count change
* Graph distance similarity
* Remove explicit H's from pair output no longer available as an option
* Added option to require attachment points to be attached by single, acyclic bonds in SMARTS pattern
* Pass-through, difference (L-R or R-L) and ratio (L/R or R/L) of data columns from left or right of transform pair
* New 'Reference Table' version of pair generation node implemented, only returning transforms between the rows of the two input tables
* Deprecated older version

_Apply Transforms (NEW)_
* New node to apply table of rSMARTS transforms
* Allows AP-fingerprint environment filtering
* Node Views showing progress, including currently processed transform(s)
* Limited stereochemistry generation handling at present time

_Rendering Nodes (NEW)_
* Show matching bonds (also calculates number of matching bonds)
* Show cuttable bonds (also calculates number of cuttable bonds)
* Streamable

Back to [top](#changelog)


## v1.10.0 - 23-Jun-2017
* Added new Generic Run-for/to-time Loop Start nodes
* Fixed Timed Loop End nodes to show correct last iteration count in flow variables
* Added possibility for user cancellation during all Timed Loop Start nodes

Back to [top](#changelog)


## v1.9.0 - 22-Jun-2017
* Existing PDB Downloader nodes deprecated
* New PDB Downloader nodes introduced
* More file type options (NMR shifts & constraints)
* New versions do not fail on missing data
* URLs for PDB downloads fixed following changes on RCSB website for both old and new versions
* FileHelper readURLToString method attempts to follow 301 return type redirects (i.e. any of the nodes loading text files from remote urls will now act in this manner)
* https: protocol now accepted for URLs in any of the downloader nodes (in addition to file:, knime:, http:, ftp:)

Back to [top](#changelog)


## v1.8.5 - 06-Jun-2017
* Fixed undesired node execution failure when SpeedySMILES de-salt node encounters empty or missing input cell

Back to [top](#changelog)


## v1.8.4 - 01-Jun-2017
* Fixed bugs introduced into Matched Molecular Pairs plugin double bond stereochemistry handling by latest updates to RDKit

Back to [top](#changelog)


## v1.8.3 - 25-Jan-2017
* Fixed bug in Speedy SMILES row filter/splitters which cause missing cells to throw NullPointerException

Back to [top](#changelog)


## v1.8.2 - 16-Nov-2016
* Addresses loop end feature request from forum  for the multiple port loop ends (3 port, upto 4 port, upto 6 ports) and the timed loop ends (1 port, 2 port, 3 port, upto 4 ports)
* All settings are now applied on a per-port basis
* Added options to allow changing column types and changing table specs, in line with the KNIME core loop end nodes
* Implemented the 'Row Key Policy' option (this was a bug in which the change from the 'Generate unique row IDs' check box meant that this was unimplemented)
* Settings for non-connected optional input ports are disabled in the node configuration dialog

Back to [top](#changelog)


## v1.8.1 - 02-Nov-2016
* Fixed change in behaviour of European PMC Advanced query node due to remote webservice changes
* Added options for page size and email address to register to remote service
* Made further existing nodes streamable
* Fixed dependency issues preventing installation to KNIME 3.1

Back to [top](#changelog)


## v1.8.0 - 31-Oct-2016
* 'Speedy SMILES' nodes (16 nodes) added for fast high-throughput pre-processing of SMILES strings (see slidesfrom 5th Cheminformatics SIG for details)
* Benchmarking nodes with memory monitoring (6 nodes) added to Testing -> Benchmarking
* Memory Use node added to Testing
* Empty Column Checker node added to Testing - node ensures table columns have at least one non-missing cell in test cases
* Multi-file reader source nodes added to IO folder (Load Local ... Files - currently 7 nodes - CDXML, Mol, Mol2, PDB, RXN XML and text)  NB These nodes will accept a single flow variable for multiple files. From the node documentation, "Alternatively, a flow variable can be specified, containing one or more filenames separated by ';'"
* Many existing nodes are now streamable (more will become streamable in due course)

Back to [top](#changelog)


## v1.7.0 - 09-Aug-2016
* New versions of PDB Connector / PDB Connector (XML Query) nodes
* Made up the complete set of optional query builder/query execution/report generation nodes:
  * PDB Connector (Query Only)
  * PDB Connector (XML Query) (Query Only)
  * Query Builder (returns the XML query as a flow var, does nothing else - although you can still run the test query button!)
  * Custom Reporter (Takes an input table and a column selection and runs custom report)
  * Query Combiner node (takes two XML advanced queries in flow variables and combines with AND or OR logic)
* All Query generator or executor nodes now have 2 views:
  * 'Logical' query view
  * XML query view
* POST and GET methods now allow chunking of report table, resolving strange behaviour for large result sets
* Execution can now be cancelled at any point during the service request
* Dialogs have option to change variable name of XML Query and to copy query to clipboard
* Data parsing problems are now added to a column at the end of the report table in addition to the console
* When both a query and a report are run, the exectution balance has been adjusted from 30/70 to 10/90
* Query results are parsed directly to a BufferedDataContainer, reducing memory overhead for large result  sets
* New manipulator node to access the PDB 'Describe Heterogens' webservice
* New source node to access the PDB SMILES Query webservice

Back to [top](#changelog)


## v1.6.5 - 30-Jun-2016
* Fixed bug introduced to PDB Connector nodes due to changes in RCSB webservices

Back to [top](#changelog)


## v1.6.4 - 13-Apr-2016
* Added PMI Nodes plugin - nodes for calculating Principal Moments of Inertia, and aligning molecules to Inertial Reference Framge
* Added support for `knime://` protocol to List Folders node

Back to [top](#changelog)


## v1.6.3 - 17-Feb-2016
* Moved filtering of fragmentations forwards in process, resulting in significant performance improvements for filtered fragmentations (Unchanging HAC and Changing/Unchanging HAC ratio)
* Improvements to H-removal algorithm
  * Moved all H-removal handling to fragmentation factory, and removed subsequent duplicate calls to H-removal (further performance improvement)
* Added check for presence of unassigned double bonds before trying to do any assignment (upto 25% performance improvement)
* Filter/Splitter nodes no longer pass multicomponent structures
* Fragmentor nodes no longer fail with multicomponent structures
* Fixed bug in which certain double bond geometries to near-symmetrical aromatic rings became corrupted causing crashes
* Fixed bug causing multi-fragment node to only perform two cuts to a bond (`[2*]-[1*]` value) when the maximum number of cuts was 2
* Improved garbage collector class to remove possible references to deleted native objects

Back to [top](#changelog)


## v1.6.2 - 12-Feb-2016
* Fixed loss of stereointegrity issue in MMP fragmentation
* Fixed canonicalisation issue resulting from above fix (NB 'Values' will not be differently canonicalised to previously)
* Fixed graceless failures for some salted forms and apparent successful failures of other salted forms

Back to [top](#changelog)


## v1.6.1 - 02-Feb-2016
* RCSB PDB Loader nodes make 5 attempts to load each file before failing if unable to load (previously only a single attempt was made, resulting in a missing value in the output)
* Local PDB Loader now makes 5 attempts to load each, but continues with a WARN to the console and a missing value cell if it fails to load a file
* Bug fixes to MMP nodes which resulted in loss of double bond geometry (Change in RDKit toolkit behaviour), scrambling of attachment point labels and node failures in certain circumstances
* Nodes transferred to the KNIME core product (Wait for time, Wait to time, Flow variable Case switch) deprecated

Back to [top](#changelog)


## v1.6.0 - 23-Nov-2015
This is a significant update.  No new functionality is provided, but the contribution is fully KNIME 3.0-ready
_General_
* All nodes are now parallelised wherever possible
* All plugins manifests were updated to bind KNIME component versions into range `[3.0.0, 4.0.0)`
* All plugin.xml's had deprecated nodes re-added, and the deprecation flag set
* All plugin.xml's had disused expert-mode property of node extensions removed

_Main Vernalis Plugin_
#### Fingerprint properties Node
* Parallelised execution
* Fingerprint type names no longer end 'Cell', so are `SparseBitVector`, `DenseBitVector`, `SparseByteVector`, `DenseByteVector`
* Test case updated to reflect this change
* Fixed bug - Although the node could process ByteVectors, the column selector does not let it select them

#### FASTA Sequence Node
* Updated all counters for rows and subrows to `long`s

#### EuroPMC Node
* Updated hit, page and row counters to `long`s (flow variables for hit counter may therefore behave strangely)

#### List Dirs node
* Updated file and added row counters to `long`s

#### Load local PDB file node
* Parallelised
* Converted to use `PDBCellFactory`

#### Save local PDB file Node
* Parallelised
* Converted to use `BooleanCellFactory` for result cell

#### PDB Downloader (Manipulator)
* Parallelised
* Converted to use PDBCellFactory

#### PDB Downloader (Source)
* Updated row and PDB ID counters to `long`s (NB this is probably irrelevant as they are processed via arrays!)

#### Load Text files
* Parallelised

#### Save Text files
* Parallelised
* Add logging explanation for save failures where easily possible
* Return value now uses `BooleanCellFactory`

#### Random Number Generator
* Original Version deprecated due to change in settings model type
* Maximum number of rows changed to a `Long` (and Settings Model changed to `SettingsModelLongBounded`), and artificial 10,000 row cap removed
* Row counters changed to `long`s
* Generator methods changed to not work via intermediate collections but add directly, and thus to allow cancelling during number generation as well as table writing, and old methods deprecated
* New Test Case created for new version

#### PDB To Sequence
* Updated to handle long tables

#### PDB Properties
* Parallelised

#### PDB Connector / PDB Connector (XML Query)
* Updated to allow long tables at both outputs


_MMPs Plugin_
* Modified canonicalisation during fragmentation to resolve duplicate leaves canonically (results in possible changes to canonicalisation)
* Converted to `SmilesCellFactory`
* MMP canonicalisation is modified to deal with duplicate components in the 'Key'

_Flow Control Plugin_
* All PortType constructors replaced with `PortTypeRegistry.getInstance().getPortType()` constructors
* Now all use `#size()` in place of `#getRowCount()`
* Iteration counters now all `long` (flow variables for iteration counters may therefore behave strangely)

_Performance Monitoring Plugin_
* Iteration counters now `long` (flow variables for iteration counters may therefore behave strangely)
* Iteration column in timing table now `LongCell` (previously `IntCell`)

Back to [top](#changelog)


## v1.5.0 - 11-Nov-2015
* Major re-release of the MMP nodes.  The first generation nodes have been retired, and a new faster, more efficient implementation released

Back to [top](#changelog)


## v1.4.1 - 07-Sep-2015
* Fixed intermittent problem with nodes ending in uncategorised folder

Back to [top](#changelog)


## v1.4.0 - 12-May-2015
* Added benchmarking nodes

Back to [top](#changelog)


## v1.3.5 - 30-Mar-2015
* Fixed issues with RDKit conversion errors causing MMP node execution to fail
* Added optional Attachment point context fingerprints to the MMP Node and MMP Molecule Fragment Node

Back to [top](#changelog)


## v1.3.4 - 12-Mar-2015
* Added Jameed Hussain's MMP Fragmentation rSMARTS from original JCIM paper
* Added User Defined rSMARTS option
* Added Include Reverse Transform option
* Added Include rSMARTS for transform in output
* Improved memory management (Explicit deleting or RDKit objects)
* Enhanced node-cancelling capabIlity during fragmentation process

Back to [top](#changelog)


## v1.3.3 - 10-Mar-2015
* Added `knime:` protocol support to Load Text-based files and Load local PDB files nodes

Back to [top](#changelog)


## v1.3.2 - 22-Jan-2015
* Corrected bug in Load Text-Based files node for non-'Guess' options

Back to [top](#changelog)


## v1.3.1 - 05-Jan-2015
Removed erroneous reference to SMILES in MMP node dialogues

Back to [top](#changelog)


## v1.3.0 - 22-Dec-2014
Added Matched Molecular Pairs nodes

Back to [top](#changelog)


## v1.2.0 - 10-Dec-2014
* Upgraded existing If/Case flow variable switch nodes
* Added Database Port If/Case switch nodes
* Added 3, upto 4 and upto 6 port loop ends
* Added Timed loop nodes
* Added delay nodes
* Added Read/Write flow variable nodes

Back to [top](#changelog)


## v1.1.5 - 04-Nov-2014
* Added dialog option to set file encoding in load local text files node
* Provided enhanced file encoding detection in the load local text/pdb files nodes (UTF-8, UTF-16 BE/LE, UTF-32 BE/LE)

Back to [top](#changelog)


## v1.1.4 - 05-Aug-2014
* Fixed bug which had prevented SMARTSViewer node from working

Back to [top](#changelog)


## v1.1.3 - 26-Mar-2014
* Added optional new columns to List Folders node
* Re-worked Fingerprint Properties node to use `ColumnRearranger` (should improve performance for large input tables and reduce datatable filesize)
* Removed redundant `NodeView` Classes

Back to [top](#changelog)


## v1.1.2 - 17-Mar-2014
* PDB Connector node updated to provide options to use new POST or old GET service.  Existing workflows will default to the new POST service
* PDB Connector (XML Query) node added

Back to [top](#changelog)


## v1.1.1 - 28-Feb-2014
* Added Fingerprint Properties node

Back to [top](#changelog)


## v1.1.0 - 26-Feb-2014
* 5 New flow control nodes for flow variables added, emulating IF/CASE nodes in core KNIME product
* European PubMed Central Advanced Search node added
* PDB Sequence Extractor node added

Back to [top](#changelog)


## v1.0.4 - 30-Jan-2014
Updates between 25-Jun-2013 and v1.0.4 on 30-Jan-2014 are a little unclear, and where never assigned individual version IDs
* Maximum URL length has been reduced to 2000 chars to fix issues with proxy handling.
* Changes to fix the PDBConnector node applied to the stable builds.
* Retry after increasing delay periods added to PDB connector node to fix intermitent failure to retrieve results from webservice.
* SMARTSViewer node updated in nightly build to require SMARTS-typed datacell.  The original version also accepted SMILES and String types, which allowed nonsense to be sent to the remote server too easily.
* Local text file load and save nodes added to the nightly build

Back to [top](#changelog)


## v1.0.0 - 25-Jun-2013
* Vernalis Community Contribution released

Back to [top](#changelog)
