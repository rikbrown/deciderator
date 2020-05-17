#!/usr/bin/env bash

HEAP_SIZE="$[((${RANDOM} % 16) * 16) + 2048]m"

java \
  -XX:+PrintCommandLineFlags \
  -Xms${HEAP_SIZE} -Xmx${HEAP_SIZE} \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump/deciderator-server.hprof -XX:+ExitOnOutOfMemoryError \
  -server -XX:+AlwaysPreTouch \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20  \
  -XX:+ParallelRefProcEnabled \
  -XX:+ExplicitGCInvokesConcurrent \
  -XX:+UnlockDiagnosticVMOptions -XX:G1SummarizeRSetStatsPeriod=1 \
  -XX:MaxMetaspaceExpansion=64M \
  -XX:+PerfDisableSharedMem \
  -Dcom.sun.management.jmxremote.port=3000 -Dcom.sun.management.jmxremote.rmi.port=3000 -Djava.rmi.server.hostname=localhost -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false \
  -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
  ${JAVA_OPTS} \
  -jar "deciderator-server.jar"
