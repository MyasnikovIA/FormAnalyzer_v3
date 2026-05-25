#java -Xmx32G -Xms32G -XX:+UseG1GC -XX:ParallelGCThreads=40 -jar tmis-analyzer.jar
#java -Xmx100G -Xms50G -XX:+UseG1GC -XX:ParallelGCThreads=40 -jar tmis-analyzer.jar
#java -Xmx100G -Xms50G -XX:+UseG1GC -XX:ParallelGCThreads=40 -XX:G1HeapRegionSize=32M -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -jar tmis-analyzer.jar
#java -Xmx240G -Xms200G -XX:+UseParallelGC -XX:ParallelGCThreads=40 -XX:MaxGCPauseMillis=2000 -XX:GCTimeRatio=99 -XX:+UseAdaptiveSizePolicy -XX:+AlwaysPreTouch -XX:+UseStringDeduplication -XX:ObjectAlignmentInBytes=16 -XX:+DisableExplicitGC -jar tmis-analyzer.jar
java -Xmx10G -Xms10G -jar tmis-analyzer.jar
