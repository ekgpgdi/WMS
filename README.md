# WMS 프로젝트

## 개요 

WMS 프로젝트는 사용자가 API 또는 엑셀 업로드를 통해 단건/여러 건의 상품 주문을 처리할 수 있도록 구현한 시스템입니다.

## 기술 스택 

### 📌 Backend  
* Spring Boot 3.4.2: 애플리케이션 프레임워크
* Spring Security: 인증 및 인가 관리
* Spring Data JPA: ORM 및 데이터베이스 연동
* QueryDSL 5.0.0: 타입 안전한 동적 쿼리
* Spring Mail: 이메일 발송 지원

### 📌 Database
* H2 Database: 메인 데이터베이스 및 테스트용 인메모리 DB

### 📌 API 문서화
* SpringDoc OpenAPI 2.8.4: Swagger 기반 API 문서 자동 생성

### 📌 인증 및 보안
* Spring Security: 보안 관리
* JWT (io.jsonwebtoken:jjwt 0.11.5): 토큰 기반 인증

### 📌 DevOps & 배포
* Gradle: 빌드 및 의존성 관리
* Lombok: 보일러플레이트 코드 제거

### 📌 기타
* Spring Boot DevTools: 개발 편의 기능
* Redis (Lettuce 6.5.1.RELEASE): 이메일 인증 관련 캐싱 기능
* JUnit 5 / AssertJ / Mockito: 테스트 프레임워크
* Apache POI 5.4.0 : 엑셀 파일 처리

## 주요 기능

### 👥 고객 관리
- 회원가입 및 로그인 (JWT 인증 방식)
- 이메일 인증

### 💳 주문 관리
- JSON Body를 통한 단건/여러건 주문
- 엑셀 기반 단건/여러건 주문

## 주문 시 상세 FLOW

1. 주문할 상품들에 대해 PESSIMISTIC_WRITE 잠금 설정
   * 다른 트랜잭션이 해당 상품을 수정하지 못하도록 비관적 락을 획득
   * (예: 동시에 같은 상품을 주문할 경우, 하나의 트랜잭션이 완료될 때까지 다른 트랜잭션은 대기)

2. 상품 재고 확인
   * 주문 수량과 현재 재고를 비교하여 재고가 부족하면 주문 실패 처리

3. 주문 정보 생성 
   * 검증이 완료되면 주문 정보를 저장

4. 상품 재고 업데이트
* 주문이 정상적으로 완료되면 해당 상품의 재고를 차감

✅ 위 과정은 하나의 트랜잭션 내에서 수행되며 (@Transactional(propagation = Propagation.REQUIRED) 적용), 어느 한 단계에서 예외가 발생하면 전체 주문이 롤백되어 데이터 정합성을 보장함

## 기술적 특징

### ✅ 이메일 인증 코드 관리
Redis를 활용하여 이메일 인증 코드를 관리하고, 인증된 이메일을 효과적으로 처리할 수 있도록 구현하였습니다.

### ✅ 예외 처리
GlobalExceptionHandler와 CustomException을 통해 예외 처리 로직을 중앙 집중화하고, 일관된 에러 응답을 제공했습니다.

### ✅ API 문서화 자동화
Swagger를 활용하여 API 문서화를 자동화하고, 개발 중 API 변경 사항을 실시간으로 반영할 수 있도록 했습니다.

## 실행 방법

1. 의존성 빌드 `./gradlew build`
2. 어플리케이션 실행 `./gradlew bootRun`

## API 명세 간략

| 메서드 | 엔드포인트                               | 설명         |
|--------|-------------------------------------|------------|
| POST   | /api/v1/auth/login                  | 로그인        |
| POST   | /api/v1/auth/send-verification-code | 인증 코드 요청   |
| POST   | /api/v1/auth/verify-code            | 인증 코드 확인   |
| POST   | /api/v1/auth/sign                   | 회원가입       |
| POST   | /api/v1/order                       | JSON Body를 통한 주문 |
| POST   | /api/v1/order/excel                 | 엑셀을 통한 주문  |

## ERD
[https://dbdiagram.io/](https://dbdiagram.io/) 활용
<br/>
```
Table customer {
  id bigint [primary key]
  email varchar(50) [unique, not null]
  name varchar(50) [not null]
  password varchar(256) [not null]
  created_at timestamp(6) 
  updated_at timestamp(6) 
}

Table "order" {
  id bigint [primary key]
  customer_id bigint [not null]
  total_amount bigint [not null]
  postcode varchar(10)
  order_number varchar(36) [unique, not null]
  address varchar(255)
  order_status enum ('FAILED', 'SUCCESS') [not null]
  created_at timestamp(6) 
  updated_at timestamp(6) 
}

Ref: "order".customer_id > customer.id // many-to-one

Table order_item {
  id bigint [primary key]
  order_id bigint [not null]
  product_id bigint [not null]
  quantity integer [not null]
  price bigint [not null]
  created_at timestamp(6) 
  updated_at timestamp(6) 
}

Ref: order_item.order_id > "order".id // many-to-one
Ref: order_item.product_id > product.id // many-to-one

Table product {
  id bigint [primary key]
  name varchar(255) [not null]
  description varchar(1000)
  price bigint [not null]
  stock integer [not null]
  created_at timestamp(6)
  updated_at timestamp(6) 
}

Table product_log {
  id bigint [primary key]
  order_id bigint [not null]
  product_id bigint [not null]
  stock bigint [not null]
  created_at timestamp(6) 
  updated_at timestamp(6) 
}

Ref: product_log.order_id > "order".id // many-to-one
Ref: product_log.product_id > product.id // many-to-one

```






