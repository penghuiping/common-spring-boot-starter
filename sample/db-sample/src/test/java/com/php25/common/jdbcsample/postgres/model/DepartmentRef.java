package com.php25.common.jdbcsample.postgres.model;

import com.php25.common.db.cnd.annotation.Column;
import com.php25.common.db.cnd.annotation.Table;

/**
 * @author penghuiping
 * @date 2020/1/15 09:58
 */
@Table("t_customer_department")
public class DepartmentRef {

    @Column("department_id")
    private Long departmentId;


    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
