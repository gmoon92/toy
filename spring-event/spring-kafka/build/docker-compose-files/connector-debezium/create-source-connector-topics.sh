echo '[+] Waiting for Kafka broker to be ready...' && \
sleep 2 && \
echo '[+] Creating required Kafka topics...' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic SOURCE_CONNECT_OFFSET --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "SOURCE_CONNECT_OFFSET" created or already exists.' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic SOURCE_CONNECT_CONFIG --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "SOURCE_CONNECT_CONFIG" created or already exists.' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic SOURCE_CONNECT_STATUS --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "SOURCE_CONNECT_STATUS" created or already exists.' && \

echo '[+] Listing current Kafka topics...' && \
/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --list && \

echo '[✓] Kafka topic setup complete.'
