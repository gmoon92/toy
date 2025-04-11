echo '[+] Waiting for Kafka broker to be ready...' && \
sleep 2 && \
echo '[+] Creating required Kafka topics...' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic connect.offsets --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "connect.offsets" created or already exists.' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic connect.configs --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "connect.configs" created or already exists.' && \

/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --create --if-not-exists --topic connect.status --replication-factor 1 --partitions 1 --config cleanup.policy=compact && \
echo '[✓] Topic "connect.status" created or already exists.' && \

echo '[+] Listing current Kafka topics...' && \
/opt/kafka/bin/kafka-topics.sh --bootstrap-server broker:19092 --list && \

echo '[✓] Kafka topic setup complete.'
