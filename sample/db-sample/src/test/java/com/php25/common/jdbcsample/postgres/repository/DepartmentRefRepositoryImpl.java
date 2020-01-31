package com.php25.common.jdbcsample.postgres.repository;

import com.php25.common.db.Db;
import com.php25.common.jdbcsample.postgres.model.DepartmentRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author penghuiping
 * @date 2020/1/31 12:42
 */
@Repository
public class DepartmentRefRepositoryImpl implements DepartmentRefRepository {

    @Autowired
    private Db db;

    @Override
    public List<DepartmentRef> findByCustomerId(Long customerId) {
        return db.cndJdbc(DepartmentRef.class).whereEq("customer_id", customerId).select();
    }

    @Override
    public void save(List<DepartmentRef> departmentRefs) {
        db.cndJdbc(DepartmentRef.class).insertBatch(departmentRefs);
    }


    @Override
    public void deleteByCustomerIds(List<Long> customerIds) {
        db.cndJdbc(DepartmentRef.class).whereIn("customer_id", customerIds).delete();
    }

}
