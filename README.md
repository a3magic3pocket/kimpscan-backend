# kimpscan

## 로컬 실행 명령 환경변수 
- ```bash
    -Dspring.config.location=classpath:/application.yml
    ```

## kafka 설정
- ```bash
  ## exchange-ticker 토픽 생성
  kafka-topics.sh --create \
    --bootstrap-server kafka:9092 \
    --topic exchange-ticker \
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
  kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic ticker --from-beginning --group tester

  # 카프카 컨슈머 그룹 목록 보기 
  kafka-consumer-groups.sh --bootstrap-server kafka:9092 --list
  
  # 카프카 컨슈머 그룹 삭제
  # 삭제할 컨슈머 그룹 오프셋 초기화
  kafka-consumer-groups.sh --bootstrap-server kafka:9092 --group 삭제할컨슈머그룹ID --topic 토픽명 --reset-offsets --to-latest --execute
  kafka-consumer-groups.sh --bootstrap-server kafka:9092 --group 삭제할컨슈머그룹ID --delete
  
  # 토픽 목록
  kafka-topics.sh --bootstrap-server kafka:9092 --list
  
  # 토픽 삭제
  kafka-topics.sh --bootstrap-server kafka:9092 --topic 삭제할토픽명 --delete
  
    ```
  
## 도커 스택
- ```bash
    # docker kimpscan 이미지 삭제
    docker rmi kimpscan:latest
  
    # docker image 빌드
    docker build -t kimpscan .
  
    # 빌드 이미지 확인하기 위해 쉘 접속
    docker run --rm -it --name kimpscan kimpscan /bin/sh

    # docker stack 배포
    docker stack deploy -c docker-compose.yml kimpscan
  
    # 서비스 로그 확인
    docker service logs kimpscan_kimpscan
  
    # docker stack 제거
    docker stack rm kimpscan
  
    # docker 서비스 상태 확인
    docker stats
  
    # mac, colima 환경에서 도커 스웜 주소 알아내기
    # - 아래 명령 후 address 에서 노출되는 IP
    colima status
  
    # 마리아db 쉘 접속
    docker exec -it <마리아db 컨테이너 ID> mariadb
  
    # [마리아db 쉘] database 및 테이블 생성
    # - src/main/resources/sql/ddl.sql를 순차적으로 실행
  
    # [마리아db 쉘] 유저 생성
    CREATE USER 'scanner'@'%' IDENTIFIED BY 'hello-world-kimp';
  
    # [마리아db 쉘] 권한 부여 
    GRANT ALL PRIVILEGES ON kimpscan.* TO 'scanner'@'%' WITH GRANT OPTION;
  
    # [마리아db 쉘] 권한 즉시 반영 
    FLUSH PRIVILEGES;
  
    # application-prod.yml이 jar 파일에 포함되었는지 확인
    jar -tvf ./build/libs/*-SNAPSHOT.jar | grep application-prod.yml
  
    ```