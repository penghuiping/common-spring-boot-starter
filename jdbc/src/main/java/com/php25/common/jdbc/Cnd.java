package com.php25.common.jdbc;

import com.php25.common.core.specification.SearchParam;
import com.php25.common.core.specification.SearchParamBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Auther: penghuiping
 * @Date: 2018/8/12 22:57
 * @Description:
 */
public abstract class Cnd extends AbstractQuery implements Query {

    private static final Logger log = LoggerFactory.getLogger(Cnd.class);

    protected JdbcOperations jdbcOperations = null;

    protected Class clazz;

    protected DbType dbType;

    protected static Cnd of(Class cls, DbType dbType, JdbcOperations jdbcOperations) {
        Cnd dsl = null;
        switch (dbType) {
            case MYSQL:
                dsl = new CndMysql(cls, jdbcOperations);
                break;
            case ORACLE:
                dsl = new CndOracle(cls, jdbcOperations);
                break;
        }
        return dsl;
    }

    public Cnd condition() {
        Cnd dsl = null;
        switch (dbType) {
            case MYSQL:
                dsl = new CndMysql(this.clazz, this.jdbcOperations);
                break;
            case ORACLE:
                dsl = new CndOracle(this.clazz, this.jdbcOperations);
                break;
        }
        return dsl;
    }

    @Override
    public String getCol(String name) {
        try {
            return " " + JpaModelManager.getDbColumnByClassColumn(this.clazz, name) + " ";
        } catch (Exception e) {
            //"无法通过jpa注解找到对应的column,直接调用父类的方法"
            return super.getCol(name);
        }

    }


    @Override
    public <T> List<T> select(Class resultType, String... columns) {
        StringBuilder sb = null;
        if (null != columns && columns.length > 0) {
            sb = new StringBuilder("SELECT ");
            for (String column : columns) {
                sb.append(column).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb = new StringBuilder("SELECT *");
        }
        sb.append(" FROM ").append(JpaModelManager.getTableName(clazz)).append(" ").append(getSql());
        this.setSql(sb);
        addAdditionalPartSql();
        String targetSql = this.getSql().toString();
        log.info("sql语句为:" + targetSql);
        Object[] paras = getParams().toArray();
        //先清楚
        clear();
        List<T> list = null;
        if (resultType.isAssignableFrom(Map.class)) {
            list = (List<T>) this.jdbcOperations.query(targetSql, paras, new ColumnMapRowMapper());
        } else {
            list = this.jdbcOperations.query(targetSql, paras, new BeanPropertyRowMapper<T>(resultType));
        }
        return list;
    }

    @Override
    public <T> List<T> select(String... columns) {
        return this.select(clazz, columns);
    }

    @Override
    public <T> List<T> select() {
        return this.select(clazz);
    }

    @Override
    public <T> T single() {
        List<T> list = limit(0, 1).select();
        if (list.isEmpty()) {
            return null;
        }
        // 同SQLManager.single 一致，只取第一条。
        return list.get(0);
    }

    @Override
    public Map mapSingle() {
        List<Map> list = limit(0, 1).select(Map.class);
        if (list.isEmpty()) {
            return null;
        }
        // 同SQLManager.single 一致，只取第一条
        return list.get(0);
    }

    @Override
    public List<Map> mapSelect() {
        return this.select(Map.class);
    }

    @Override
    public List<Map> mapSelect(String... columns) {
        return this.select(Map.class, columns);
    }

    @Override
    public <T> int update(T t) {
        return update(t, true);
    }

    @Override
    public <T> int updateIncludeNull(T t) {
        return update(t, false);
    }

    @Override
    public <T> int insert(T t) {
        return insert(t, true);
    }

    @Override
    public <M> int[] insertBatch(List<M> list) {
        //泛型获取类所有的属性
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + JpaModelManager.getTableName(clazz) + "( ");
        List<ImmutablePair<String, Object>> pairList = JpaModelManager.getTableColumnNameAndValue(list.get(0), false);
        //拼装sql语句
        for (int i = 0; i < pairList.size(); i++) {
            if (i == (pairList.size() - 1))
                stringBuilder.append(pairList.get(i).getLeft());
            else
                stringBuilder.append(pairList.get(i).getLeft() + ",");
        }
        stringBuilder.append(" ) VALUES ( ");
        for (int i = 0; i < pairList.size(); i++) {
            if (i == (pairList.size() - 1))
                stringBuilder.append("?");
            else
                stringBuilder.append("?,");
        }
        stringBuilder.append(" )");
        log.info("sql语句为:" + stringBuilder.toString());

        //拼装参数
        List<Object[]> batchParams = new ArrayList<>();
        for (int j = 0; j < list.size(); j++) {
            List<Object> params = new ArrayList<>();
            List<ImmutablePair<String, Object>> tmp = JpaModelManager.getTableColumnNameAndValue(list.get(j), false);
            for (int i = 0; i < tmp.size(); i++) {
                params.add(tmp.get(i).getRight());
            }
            batchParams.add(params.toArray());
        }

        try {
            return jdbcOperations.batchUpdate(stringBuilder.toString(), batchParams);
        } catch (Exception e) {
            log.error("插入操作失败", e);
            throw new RuntimeException("插入操作失败", e);
        } finally {
            clear();
        }
    }

    @Override
    public <T> int insertIncludeNull(T t) {
        return insert(t, false);
    }

    @Override
    public int delete() {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(JpaModelManager.getTableName(clazz)).append(" ").append(getSql());
        this.setSql(sb);
        log.info("sql语句为:" + sb.toString());
        String targetSql = this.getSql().toString();
        Object[] paras = getParams().toArray();
        //先清除，避免执行出错后无法清除
        clear();
        int row = this.jdbcOperations.update(targetSql, paras);
        return row;
    }

    @Override
    public long count() {
        StringBuilder sb = new StringBuilder("SELECT COUNT(1) FROM ");
        sb.append(JpaModelManager.getTableName(clazz)).append(" ").append(getSql());
        this.setSql(sb);
        log.info("sql语句为:" + sb.toString());
        String targetSql = this.getSql().toString();
        Object[] paras = getParams().toArray();
        //先清除，避免执行出错后无法清除
        clear();
        Long result = this.jdbcOperations.queryForObject(targetSql, Long.class, paras);
        return result;
    }

    @Override
    public Cnd having(QueryCondition condition) {
        // 去除叠加条件中的WHERE
        int i = condition.getSql().indexOf(WHERE);
        if (i > -1) {
            condition.getSql().delete(i, i + 5);
        }
        if (this.groupBy == null) {
            throw new RuntimeException("having 需要在groupBy后调用");
        }
        groupBy.addHaving(condition.getSql().toString());
        this.addParam(condition.getParams());
        return this;
    }

    @Override
    public Cnd groupBy(String column) {
        GroupBy groupBy = getGroupBy();
        groupBy.add(getCol(column));
        return this;
    }

    @Override
    public Cnd orderBy(String orderBy) {
        OrderBy orderByInfo = this.getOrderBy();
        orderByInfo.add(orderBy);
        return this;
    }

    @Override
    public Cnd asc(String column) {
        OrderBy orderByInfo = this.getOrderBy();
        orderBy.add(getCol(column) + " ASC");
        return this;
    }

    @Override
    public Cnd desc(String column) {
        OrderBy orderByInfo = this.getOrderBy();
        orderBy.add(column + " DESC");
        return this;
    }

    /**
     * 默认从1开始，自动翻译成数据库的起始位置。如果配置了OFFSET_START_ZERO =true，则从0开始。
     */
    @Override
    public Cnd limit(long startRow, long pageSize) {
        this.startRow = startRow;
        this.pageSize = pageSize;
        return this;
    }


    private OrderBy getOrderBy() {
        if (this.orderBy == null) {
            orderBy = new OrderBy();
        }
        return this.orderBy;
    }

    private GroupBy getGroupBy() {
        if (this.groupBy == null) {
            groupBy = new GroupBy();
        }
        return this.groupBy;
    }


    private <T> int insert(T t, boolean ignoreNull) {
        //泛型获取类所有的属性
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder("INSERT INTO " + JpaModelManager.getTableName(clazz) + "( ");
        List<ImmutablePair<String, Object>> pairList = JpaModelManager.getTableColumnNameAndValue(t, ignoreNull);
        //拼装sql语句
        for (int i = 0; i < pairList.size(); i++) {
            if (i == (pairList.size() - 1))
                stringBuilder.append(pairList.get(i).getLeft());
            else
                stringBuilder.append(pairList.get(i).getLeft() + ",");
        }
        stringBuilder.append(" ) VALUES ( ");
        for (int i = 0; i < pairList.size(); i++) {
            if (i == (pairList.size() - 1))
                stringBuilder.append("?");
            else
                stringBuilder.append("?,");
            params.add(pairList.get(i).getRight());
        }
        stringBuilder.append(" )");
        log.info("sql语句为:" + stringBuilder.toString());
        try {
            return jdbcOperations.update(stringBuilder.toString(), params.toArray());
        } catch (Exception e) {
            log.error("插入操作失败", e);
            throw new RuntimeException("插入操作失败", e);
        } finally {
            clear();
        }
    }


    private <T> int update(T t, boolean ignoreNull) {
        //泛型获取类所有的属性
        Field[] fields = t.getClass().getDeclaredFields();
        StringBuilder stringBuilder = new StringBuilder("UPDATE " + JpaModelManager.getTableName(t.getClass()) + " SET ");
        List<ImmutablePair<String, Object>> pairList = JpaModelManager.getTableColumnNameAndValue(t, ignoreNull);
        //获取主键id
        String pkName = JpaModelManager.getPrimaryKeyColName(t.getClass());

        Object pkValue = null;
        for (int i = 0; i < pairList.size(); i++) {
            //移除主键
            if (!pairList.get(i).getLeft().equals(pkName)) {
                if (i == (pairList.size() - 1))
                    stringBuilder.append(pairList.get(i).getLeft()).append("=? ");
                else
                    stringBuilder.append(pairList.get(i).getLeft()).append("=?,");
                params.add(pairList.get(i).getRight());
            } else {
                pkValue = pairList.get(i).getValue();
            }
        }
        stringBuilder.append(String.format("WHERE %s=?", pkName));
        params.add(pkValue);
        log.info("sql语句为:" + stringBuilder.toString());
        try {
            return jdbcOperations.update(stringBuilder.toString(), params.toArray());
        } catch (Exception e) {
            log.error("更新操作失败", e);
            throw new RuntimeException("更新操作失败", e);
        } finally {
            clear();
        }
    }

    /**
     * 通过searchParamBuilder来构造查询条件
     *
     * @param searchParamBuilder
     * @return
     */
    public Cnd andSearchParamBuilder(SearchParamBuilder searchParamBuilder) {
        List<SearchParam> searchParams = searchParamBuilder.build();
        for (SearchParam searchParam : searchParams) {
            searchParam.getFieldName();
            String operator = searchParam.getOperator().name();
            if (null != operator) {
                if ("eq".equals(operator.toLowerCase())) {
                    this.andEq(searchParam.getFieldName(), searchParam.getValue());
                } else if ("ne".equals(operator.toLowerCase())) {
                    this.andNotEq(searchParam.getFieldName(), searchParam.getValue());
                } else if ("like".equals(operator.toLowerCase())) {
                    this.andLike(searchParam.getFieldName(), (String) searchParam.getValue());
                } else if ("gt".equals(operator.toLowerCase())) {
                    this.andGreat(searchParam.getFieldName(), searchParam.getValue());
                } else if ("lt".equals(operator.toLowerCase())) {
                    this.andLess(searchParam.getFieldName(), searchParam.getValue());
                } else if ("gte".equals(operator.toLowerCase())) {
                    this.andGreatEq(searchParam.getFieldName(), searchParam.getValue());
                } else if ("lte".equals(operator.toLowerCase())) {
                    this.andLessEq(searchParam.getFieldName(), searchParam.getValue());
                } else if ("in".equals(operator.toLowerCase())) {
                    this.andIn(searchParam.getFieldName(), (Collection<?>) searchParam.getValue());
                } else if ("nin".equals(operator.toLowerCase())) {
                    this.andNotIn(searchParam.getFieldName(), (Collection<?>) searchParam.getValue());
                } else {
                    this.andEq(searchParam.getFieldName(), searchParam.getValue());
                }
            }
        }
        return this;
    }

    /**
     * 增加分页，排序
     */
    protected abstract void addAdditionalPartSql();
}
