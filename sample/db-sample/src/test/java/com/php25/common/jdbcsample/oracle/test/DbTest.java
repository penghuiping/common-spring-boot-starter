package com.php25.common.jdbcsample.oracle.test;

import com.baidu.fsg.uid.UidGenerator;
import com.google.common.collect.Lists;
import com.php25.common.core.service.IdGeneratorService;
import com.php25.common.core.util.DigestUtil;
import com.php25.common.db.Db;
import com.php25.common.db.cnd.CndJdbc;
import com.php25.common.jdbcsample.oracle.dto.CustomerDto;
import com.php25.common.jdbcsample.oracle.model.Company;
import com.php25.common.jdbcsample.oracle.model.Customer;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: penghuiping
 * @date: 2018/8/31 10:27
 * @description:
 */
public class DbTest {

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    UidGenerator uidGenerator;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Db db;

    boolean isSequence = true;

    List<Customer> customers = Lists.newArrayList();

    List<CustomerDto> customerDtos = Lists.newArrayList();

    protected void initDb() {

    }

    private void initMeta(boolean isSequence) throws Exception {
        Class cls = Class.forName("org.h2.Driver");
        Driver driver = (Driver) cls.newInstance();
        Connection connection = driver.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=ORACLE", null);
        Statement statement = connection.createStatement();
        statement.execute("drop table if exists t_customer");
        statement.execute("drop table if exists t_company");
        statement.execute("create table t_customer (id number(64,0),username varchar2(20),password varchar2(50),age integer ,create_time date,update_time date,version number(64,0),company_id number(64,0),score number(32,0),`enable` number(1,0))");
        statement.execute("create table t_company (id number(64,0) primary key,name varchar2(20),create_time date,update_time date,`enable` number(1,0))");
        if (isSequence) {
            statement.execute("drop SEQUENCE if exists SEQ_ID");
            statement.execute("CREATE SEQUENCE SEQ_ID");
        }
        statement.closeOnCompletion();
        connection.close();
    }

    @Before
    public void before() throws Exception {
        initMeta(isSequence);
        this.initDb();
        CndJdbc cndJdbc = db.cndJdbc(Customer.class);
        CndJdbc cndJdbcCompany = db.cndJdbc(Company.class);

        Company company = new Company();
        company.setName("Google");
        company.setId(uidGenerator.getUID());
        company.setCreateTime(new Date());
        company.setEnable(1);
        cndJdbcCompany.insert(company);


        for (int i = 0; i < 3; i++) {
            Customer customer = new Customer();
            if (!isSequence) {
                customer.setId(uidGenerator.getUID());
            }
            customer.setUsername("jack" + i);
            customer.setPassword(DigestUtil.MD5Str("123456"));
            customer.setAge(i * 10);
            customer.setStartTime(LocalDateTime.now());
            customer.setEnable(1);
            customer.setCompanyId(company.getId());
            customer.setUpdateTime(LocalDateTime.now());
            customer.setScore(BigDecimal.valueOf(1000L));
            customers.add(customer);
            cndJdbc.insert(customer);
            Assert.assertNotNull(customer.getId());
        }

        for (int i = 0; i < 3; i++) {
            Customer customer = new Customer();
            if (!isSequence) {
                customer.setId(uidGenerator.getUID());
            }
            customer.setUsername("mary" + i);
            customer.setPassword(DigestUtil.MD5Str("123456"));
            customer.setStartTime(LocalDateTime.now());
            customer.setAge(i * 20);
            customer.setEnable(0);
            customer.setCompanyId(company.getId());
            customer.setScore(BigDecimal.valueOf(1000L));
            customers.add(customer);
            cndJdbc.insert(customer);
            Assert.assertNotNull(customer.getId());
        }

        customerDtos = customers.stream().map(customer -> {
            CustomerDto customerDto = new CustomerDto();
            BeanUtils.copyProperties(customer, customerDto);
            return customerDto;
        }).collect(Collectors.toList());
    }
}