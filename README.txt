This project is about implementation and semantic description of computation which can be represented in a machine understandable way and sent to another execution environment where, using the semantic, it can be reconstructed and computed. 

Currently : 26/02/2012 
optimization is not the task but functionalities (data, computation, identity and performance analysis) and architectural choices are priority at moment

Currently two streams are considered:
- Nokia Cloud, AWS based (OSS based) with Qt based client integration on device side
- MSFT Cloud, WCF based with C# based client integration on device side (RIA, RX frameworks included)
streams are mirroring each other, bounded with Elata (Latency analyzer tool) and Backend to Backend protocol should shape the tools for Interoperability in Services.


An interface to serialize computations exist(AtomicClosure) and some class implement them addintClosure, add3intClosure, addsubintClosure, subintClosure

Chain of these functionalities can be created and sent o the distributed information management system (RDF store)

Execution is made out of order and bean as soon as possible and in parallel

Planned prioritized extensions:

-Vector parameters implemented with parallelization objectives
-HOF and in particular Map with implementation oriented to parallelization
-Different data types
-Analysis methods to use chain as non-named closures

Known issues:

-Partial and final and obsolete triples are not deleted if they don't affect functionality
-some subscription doesn't work at moment and it is substituted by polling
-Some method is not used some is not implemented and code need to be polished
 