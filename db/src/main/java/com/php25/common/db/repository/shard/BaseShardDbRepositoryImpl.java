package com.php25.common.db.repository.shard;

import com.google.common.collect.Lists;
import com.php25.common.db.Db;
import com.php25.common.db.manager.JdbcModelManager;
import com.php25.common.db.repository.BaseDbRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author penghuiping
 * @date 2020/9/9 16:51
 */
public class BaseShardDbRepositoryImpl<T extends Persistable<ID>, ID extends Comparable<?>> extends JdbcShardDbRepositoryImpl<T, ID> implements BaseDbRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseShardDbRepositoryImpl.class);

    private TwoPhaseCommitTransaction twoPhaseCommitTransaction;

    public BaseShardDbRepositoryImpl(List<Db> dbList, ShardRule shardRule, TwoPhaseCommitTransaction twoPhaseCommitTransaction) {
        super(dbList, shardRule);
        this.twoPhaseCommitTransaction = twoPhaseCommitTransaction;
    }

    @NotNull
    @Override
    public <S extends T> S save(S s) {
        ID id = s.getId();
        Db db = shardRule.shardPrimaryKey(this.dbList, id);
        if (s.isNew()) {
            //新增
            db.cndJdbc(model).ignoreCollection(false).insert(s);
        } else {
            //更新
            db.cndJdbc(model).ignoreCollection(false).update(s);
        }
        return s;
    }

    @NotNull
    @Override
    public <S extends T> Iterable<S> saveAll(@NotNull Iterable<S> iterable) {
        List<TransactionCallback<List<S>>> list = this.dbList.stream().map(db -> {
            return new TransactionCallback<List<S>>() {
                @Override
                public List<S> doInTransaction() {
                    List<S> models = Lists.newArrayList(iterable).stream()
                            .filter(id -> db.equals(shardRule.shardPrimaryKey(dbList, id)))
                            .collect(Collectors.toList());
                    if (!models.isEmpty()) {
                        S s = models.iterator().next();
                        if (s.isNew()) {
                            db.cndJdbc(model).insertBatch(models);
                        } else {
                            db.cndJdbc(model).updateBatch(models);
                        }
                    }
                    return models;
                }
                @Override
                public Db getDb() {
                    return db;
                }
            };
        }).collect(Collectors.toList());
        List<List<S>> result = twoPhaseCommitTransaction.execute(list);
        return result.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Optional<T> findById(@NotNull ID id) {
        Db db = shardRule.shardPrimaryKey(this.dbList, id);
        T t = db.cndJdbc(model).ignoreCollection(false).whereEq(pkName, id).single();
        if (null != t) {
            return Optional.of(t);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(@NotNull ID id) {
        Db db = shardRule.shardPrimaryKey(this.dbList, id);
        return db.cndJdbc(model).whereEq(pkName, id).count() > 0;
    }

    @NotNull
    @Override
    public Iterable<T> findAll() {
        List<T> result = new ArrayList<>();
        for (Db db : dbList) {
            List<T> tmp = db.cndJdbc(model).select();
            if (null != tmp && !tmp.isEmpty()) {
                result.addAll(tmp);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public Iterable<T> findAllById(@NotNull Iterable<ID> iterable) {
        List<T> result = new ArrayList<>();
        for (Db db : dbList) {
            List<ID> ids = Lists.newArrayList(iterable).stream()
                    .filter(id -> db.hashCode() == shardRule.shardPrimaryKey(dbList, id).hashCode())
                    .collect(Collectors.toList());
            List<T> tmp = db.cndJdbc(model).whereIn(pkName, Lists.newArrayList(ids)).select();
            result.addAll(tmp);
        }
        return result;
    }

    @Override
    public long count() {
        long count = 0L;
        for (Db db : dbList) {
            count = count + db.cndJdbc(model).count();
        }
        return count;
    }

    @Override
    public void deleteById(@NotNull ID id) {
        Db db = shardRule.shardPrimaryKey(this.dbList, id);
        T obj = db.cndJdbc(model).whereEq(pkName, id).single();
        this.delete(obj);
    }

    @Override
    public void delete(T t) {
        Db db = shardRule.shardPrimaryKey(this.dbList, t.getId());
        db.cndJdbc(model).ignoreCollection(false).delete(t);
    }

    @Override
    public void deleteAll(@NotNull Iterable<? extends T> iterable) {
        for (Db db : dbList) {
            List<ID> ids = Lists.newArrayList(iterable).stream()
                    .filter(model -> db.hashCode() == shardRule.shardPrimaryKey(dbList, model.getId()).hashCode())
                    .map(T::getId)
                    .collect(Collectors.toList());
            String pkName = JdbcModelManager.getPrimaryKeyColName(model);
            db.cndJdbc(model).whereIn(pkName, ids).delete();
        }
    }

    @Override
    public void deleteAll() {
        for (Db db : dbList) {
            db.cndJdbc(model).delete();
        }
    }
}
