# kimpscan

## 로컬 실행 명령 환경변수 
- ```bash
    -Dspring.profiles.active=dev -Dspring.config.location=classpath:/application.yml
    ```

## kafka 설정
- ```bash
  ## ticker 토픽 생성
  # retention.ms=600000 (10분 보관)
  # delete.retention.ms=120000 (삭제 요청 후 2분 유지)
  # segment.ms=600000 (10분마다 세그먼트 롤링)
  kafka-topics.sh --create \
    --bootstrap-server kafka:9092 \
    --topic ticker \
    --partitions 1 \
    --replication-factor 1 \
    --config retention.ms=600000 \
    --config segment.ms=600000 \
    --config delete.retention.ms=120000 \
    --config compression.type=zstd \
    --config min.insync.replicas=1 \
    --config max.message.bytes=262144 \
    --config flush.messages=1 \
    --config flush.ms=1000
  
  
  ## tester 컨슈머 그룹 ticker 토픽 구독
  kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ticker --from-beginning --group tester 
    ```