# Time-Based Unique Identifiers

## RDBMS 성능을 위한 순차적인 Primary Key의 중요성

RDBMS의 조회 성능을 향상시키기 위해 인덱스 튜닝은 필수적이다. 

InnoDB에서는 PK(Primary Key)를 클러스터링 인덱스로 관리하며, 이는 테이블이 디스크에 물리적으로 정렬된 상태로 저장됨을 의미한다. 큰 로그성 테이블에서는 순차적인 PK가 데이터 삽입 시 불필요한 정렬 작업을 최소화하여 성능을 최적화할 수 있다. 이를 통해 별도의 인덱스 수정 없이 빠른 조회 성능을 기대할 수 있다.

하지만 Multi-Master Replication 환경에서는 PK를 시퀀스 테이블 또는 내장형 auto increment로 생성하는 방식이 마이그레이션을 어렵게 만들 수 있다. 단일 서버 환경에서는 동시성 문제 없이 동작할 수 있지만, 분산 시스템에서는 동기화나 경쟁 조건으로 인해 성능 저하가 발생할 수 있다.

## Time-Based Unique Identifiers

- [TSID](#tsid)
- [ULID](#ulid)
- [Snowflake ID](#snowflake-id)

### TSID

TSID(`Time-Sorted Unique Identifier`)는 

시간 순서로 정렬 가능한 고유 식별자를 생성하는 방식으로, 데이터베이스에서 레코드를 정렬하거나 고유 식별자로 사용된다. 일반적으로 타임스탬프와 추가적인 랜덤 값 또는 고유 식별자 조합으로 구성된다.

#### TSID 구조

TSID는 일반적으로 다음과 같은 두 가지 주요 요소로 구성된다.

```text
{timestamp}{randomness}
  48bits      80bits
```

1. **48비트 타임스탬프**: 현재 시간을 밀리초 단위로 표현하여 시간 순서에 따라 정렬이 가능하다.
2. **80비트 랜덤 값**: 동일한 타임스탬프 내에서 고유성을 보장한다.

#### TSID 특징

- 시간 기반으로 정렬 가능하여 범위 검색과 데이터 정렬이 용이하다.
- 랜덤 값을 사용해 고유성을 보장하고 충돌을 방지한다.
- URL-safe 인코딩을 사용하여 웹 환경에서도 안전하게 사용할 수 있다.

### ULID

ULID(`Universally Unique Lexicographically Sortable Identifier`)는 

TSID의 개념을 바탕으로 설계되었으며, URL-safe한 Base32 인코딩을 사용하여 문자열로 표현된다.

#### ULID 구조

ULID는 다음과 같은 두 가지 주요 요소로 구성된다.

```text
{timestamp}{randomness}
  48bits      80bits
```

- **48비트 타임스탬프**: 밀리초 단위의 시간 정보를 제공한다.
- **80비트 랜덤 값**: 고유성을 보장하기 위한 랜덤 값이다.

#### ULID 특징

- 고유성과 시간 순서를 보장하며 사전순으로 정렬 가능하다.
- URL-safe Base32 인코딩을 사용하여 웹에서 안전하게 사용할 수 있다.
- UUID와는 다른 형식을 가지며, 128비트가 아닌 26자의 문자열로 표현된다.

#### ULID 한계

- 중복된 타임스탬프에 대한 충돌 가능성이 있다.
- 동일 밀리초 내에서 고유한 순서를 보장하지 않는다.
- 랜덤 값의 비트 수로 인해 대규모 분산 시스템에서는 충돌 가능성이 있을 수 있다.
- 가변 길이 인코딩으로 복잡성이 증가할 수 있다.

### Snowflake ID

Snowflake ID는 트위터가 대규모 분산 시스템을 위해 설계한 순차적이고 고유한 식별자 생성 방식이다. 이 방식은 높은 정밀도의 타임스탬프와 동시성 제어를 제공하며, 클러스터링 인덱스와 같은 성능 최적화 작업을 지원한다.

#### Snowflake ID 구조

Snowflake ID는 총 64비트로 구성된다.

```text
{sign bit}{timestamp}{instance}{sequence}
   1bit     41bits    10bits    12bits
```

- **1비트 - 부호 비트**: 항상 0으로 설정되며, Snowflake ID가 양의 정수로 해석되도록 한다.
- **41비트 - 타임스탬프**: 기준 시점(Epoch)으로부터의 경과 밀리초를 기록하며, 약 69년 동안 유효하다.
- **10비트 - 기기 ID**: 데이터 센터와 기계 ID를 포함하여 총 1024개의 노드를 지원한다.
- **12비트 - 일련번호**: 동일 밀리초 내에서 최대 4096개의 고유 ID를 생성할 수 있다.

### TSID VS Snowflake ID

|             | TSID                        | Snowflake ID                  |
|-------------|-----------------------------|-------------------------------|
| 타임스탬프   | 48비트 (Epoch 기준)         | 41비트 (Epoch 기준)            |
| 기기 ID      | 데이터 센터 ID 없음         | 10비트 (5비트 데이터 센터, 5비트 기기 ID) |
| 일련번호     | 80비트 랜덤 값              | 12비트 일련번호                |
| ID 길이      | 총 96비트 또는 128비트      | 총 64비트                      |
| 정렬 가능성  | 시간에 따라 완전히 정렬 가능| 시간에 따라 정렬 가능           |
| 충돌 가능성  | 데이터 센터 ID가 없어 충돌 가능성 높음 | 일련번호와 기기 ID로 충돌 방지 |
| 사용 사례    | 정렬이 중요한 시스템       | 시스템 및 대규모 서비스        |
| 언어 및 환경| 주로 Java 환경에서 사용     | 다양한 언어와 플랫폼에서 사용 |
| 생성 복잡도  | 간단한 알고리즘, 외부 서비스 필요 없음 | 트위터의 시스템에 맞춤화된 설계 |

## 마무리
### 순차적인 Primary Key의 필요성

- **PK 채번 방식의 한계**
  - auto increment 기능은 분산 시스템에서 동기화 문제가 발생할 수 있다.
    - bulk insert 작업시에도 병목 현상 발생 우려
  - 시퀀스 테이블은 보통 중앙화된 관리가 필요하기 때문에, 분산 환경에서는 오히려 병목 현상이 발생할 수 있다.
  - 시퀀스 테이블이나 내장형 auto increment 기능을 사용할 경우, Multi-Master Replication 환경에서 데이터 마이그레이션이 복잡할 수 있다.
- **UUID와의 비교**
  - 많은 시스템에서 UUID를 사용하여 고유성을 보장하지만, 시간 기반 정렬이 필요한 데이터에서는 성능이 저하될 수 있다. 
  - UUID는 비순차적이므로 데이터가 랜덤하게 삽입되며, 이로 인해 클러스터링이 비효율적일 수 있다. 
  - 시간 범위 조회를 최적화하려면 별도의 순차적 컬럼을 인덱스로 추가해야 할 수 있다.
- **B+Tree 인덱스의 이점**
    - B+Tree 구조에서는 인덱스 키가 리프 노드에 연결 리스트 구조로 순차적으로 정렬되어 저장되며, PK는 클러스터링 키로 사용된다.
    - 순차적인 PK를 사용할 경우, 클러스터링 인덱스 덕분에 데이터가 물리적으로 정렬되어 저장되므로 시간 순서에 따른 추가적인 정렬 작업이 필요 없고, 범위 검색과 시간 기반 쿼리의 성능이 향상된다.
- 쓰기 작업 디스크 I/O
  - 순차적인 PK를 사용하면 디스크에 쓰기 작업을 할 때 효율성이 증가한다. 이는 디스크의 I/O 성능을 최적화하는 데 도움이 된다.
  - 순차적인 PK는 새로운 레코드가 항상 현재 데이터의 끝에 추가되도록 보장하므로, 디스크의 페이지가 순차적으로 쓰여져 I/O 작업이 연속적으로 이루어진다. 이로 인해 디스크 헤드의 이동을 최소화하고 캐시의 효율성을 높일 수 있다.
  - 반면, 랜덤한 PK는 디스크 페이지의 분산된 위치에 데이터를 기록하므로, 디스크 헤드의 빈번한 이동을 초래하고, 디스크 캐시의 효율성을 낮출 수 있다.
  - 단. 메모리 부족으로 인한 랜덤 I/O는 순차적 PK 사용 여부와 관계없이 발생할 수 있고, 디스크 I/O 성능 저하를 유발할 수 있다.

### 데이터베이스 최적화 및 단편화 정리

단편화(Fragmentation)란 데이터가 물리적으로 비효율적으로 저장되어 디스크 공간의 활용이 저하되는 현상을 의미한다. 데이터베이스에서 데이터를 삭제하면 디스크 블록에 빈 공간이 생기고, 이 빈 공간은 디스크의 물리적 배치에 영향을 미쳐 I/O 성능 저하를 초래할 수 있다. 빈 공간이 누적되면 데이터 접근 시 더 많은 I/O 작업이 필요하게 되며, 이는 성능 저하로 이어질 수 있다. 데이터베이스는 이러한 삭제된 블록을 재사용하거나 정리하는 메커니즘을 가지고 있지만, 이 과정에서 시스템에 부하가 발생할 수 있다.

클러스터링 인덱스는 디스크에 데이터를 순차적으로 저장하여 검색 성능을 향상시키지만, 빈번한 데이터 추가와 삭제로 인해 단편화가 발생할 수 있다. 예를 들어, 데이터를 삭제한 후에 새로운 데이터를 추가하면, 새 데이터가 삭제된 데이터의 빈 공간에 저장되지 않고 다른 위치에 저장될 수 있으며, 이로 인해 디스크의 물리적 배치가 비효율적으로 될 수 있다.

이를 해결하기 위해 MySQL InnoDB에서는 `OPTIMIZE TABLE` 명령어를 사용하여 테이블을 재정렬하고, 단편화된 공간을 회수한다. `OPTIMIZE TABLE` 명령어는 테이블을 다시 작성하여 데이터의 물리적 배치를 정리하고, 디스크 공간의 낭비를 줄인다. PostgreSQL에서는 `VACUUM` 명령어를 사용하여 삭제된 데이터가 남긴 빈 공간을 정리하고 단편화를 줄인다. `VACUUM` 명령어는 데이터베이스 파일 내의 빈 공간을 회수하고, 테이블의 통계를 업데이트하여 쿼리 성능을 유지하는 데 도움을 준다.

이러한 정리 작업들은 데이터베이스의 성능을 유지하고, 효율적인 디스크 공간 활용을 보장하는 데 중요하다. 빈번한 데이터 변경이 발생하는 시스템에서는 정기적인 단편화 정리 작업이 필요할 수 있다.

## Reference

- [wiki - Snowflake_ID](https://en.wikipedia.org/wiki/Snowflake_ID)
- [twitter archive - snowflake](https://github.com/twitter-archive/snowflake)
- [sony - Sonyflake for java](https://github.com/sony/sonyflake)
- [baidu - UIDGenerator](https://github.com/baidu/uid-generator)
- [tsid creator](https://github.com/f4b6a3/tsid-creator)