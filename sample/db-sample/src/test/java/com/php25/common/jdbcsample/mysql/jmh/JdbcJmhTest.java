package com.php25.common.jdbcsample.mysql.jmh;

import com.baidu.fsg.uid.UidGenerator;
import com.google.common.collect.Lists;
import com.php25.common.core.service.IdGeneratorService;
import com.php25.common.core.util.DigestUtil;
import com.php25.common.db.Db;
import com.php25.common.db.DbType;
import com.php25.common.db.cnd.CndJdbc;
import com.php25.common.db.manager.JpaModelManager;
import com.php25.common.jdbcsample.mysql.model.Company;
import com.php25.common.jdbcsample.mysql.model.Customer;
import com.php25.common.jdbcsample.mysql.test.MysqlJdbcTest;
import org.junit.Assert;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Auther: penghuiping
 * @Date: 2018/8/14 17:01
 * @Description:
 */
@State(Scope.Benchmark)
public class JdbcJmhTest {
    private JdbcTemplate jdbcTemplate;

    private IdGeneratorService idGeneratorService;

    private UidGenerator uidGenerator;

    private Db db;

    private List<Customer> customers;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JdbcJmhTest.class.getSimpleName())
                .mode(Mode.Throughput)
                .timeout(TimeValue.valueOf("60s"))
                .warmupIterations(3)
                .warmupTime(TimeValue.valueOf("10s"))
                .measurementIterations(5)
                .measurementTime(TimeValue.valueOf("30s"))
                .threads(1)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    @Setup(Level.Trial)
    public void init() {
        SpringBootTestContextBootstrapper bootstrapper = new SpringBootTestContextBootstrapper();
        DefaultBootstrapContext defaultBootstrapContext = new DefaultBootstrapContext(MysqlJdbcTest.class, new DefaultCacheAwareContextLoaderDelegate());
        bootstrapper.setBootstrapContext(defaultBootstrapContext);
        TestContext testContext = bootstrapper.buildTestContext();

        this.jdbcTemplate = testContext.getApplicationContext().getBean(JdbcTemplate.class);
        this.idGeneratorService = testContext.getApplicationContext().getBean(IdGeneratorService.class);
        this.uidGenerator = testContext.getApplicationContext().getBean(UidGenerator.class);
        this.db = new Db(this.jdbcTemplate, DbType.MYSQL);

        jdbcTemplate.execute("drop table if exists t_customer");
        jdbcTemplate.execute("create table t_customer (id bigint auto_increment primary key,username varchar(20),password varchar(50),age int,create_time date,update_time date,version bigint,`enable` int,score bigint,company_id bigint)");

        CndJdbc cndJdbc = this.db.cndJdbc(Customer.class);

        Company company = new Company();
        company.setName("test");
        company.setId(1L);
        company.setCreateTime(new Date());
        company.setEnable(1);
        db.cndJdbc(Company.class).insert(company);

        this.customers = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {
            Customer customer = new Customer();
            customer.setId(uidGenerator.getUID());
            customer.setUsername("jack" + i);
            customer.setPassword(DigestUtil.MD5Str("123456"));
            customer.setAge(i * 10);
            customer.setStartTime(LocalDateTime.now());
            customer.setEnable(1);
            customer.setCompanyId(company.getId());
            this.customers.add(customer);
            cndJdbc.insert(customer);
        }

    }

    //@org.openjdk.jmh.annotations.Benchmark
    public void queryByUsername() throws Exception {
        CndJdbc cndJdbc = this.db.cndJdbc(Customer.class);
        List<Customer> customers1 = cndJdbc.whereEq("username", "jack0").limit(0, 1).asc("id").select();
    }

    //@org.openjdk.jmh.annotations.Benchmark
    public void getTableColumnNameAndValue() throws Exception {
        JpaModelManager.getTableColumnNameAndValue(customers.get(0), true);
    }

    //@org.openjdk.jmh.annotations.Benchmark
    public void getPrimaryKeyColName() throws Exception {
        JpaModelManager.getPrimaryKeyColName(Customer.class);
    }

    //@org.openjdk.jmh.annotations.Benchmark
    public void getDbColumnByClassColumn() throws Exception {
        JpaModelManager.getDbColumnByClassColumn(Customer.class, "id");
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void getIdValue() throws Exception {
        Customer customer = this.db.cndJdbc(Customer.class).whereEq("id", customers.get(0).getId()).single();
        Assert.assertEquals(customer.getId(), customers.get(0).getId());
    }
}
