package com.kamesan.erpapi.sales.repository;

import com.kamesan.erpapi.sales.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退貨單明細 Repository
 */
@Repository
public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {

    List<RefundItem> findByRefundId(Long refundId);

    List<RefundItem> findByProductId(Long productId);
}
