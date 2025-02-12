package com.dahye.wms.product.repository;

import com.dahye.wms.product.domain.Product;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dahye.wms.product.domain.QProduct.product;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Autowired
    public ProductRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory; // 생성자를 통해 queryFactory 주입받기
    }

    @Override
    public List<Product> findByIdInWithPessimisticWrite(List<Long> productIdList) {
        return queryFactory.selectFrom(product)
                .where(product.id.in(productIdList))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }
}
