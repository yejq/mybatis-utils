package com.yejq.interceptor;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.yejq.common.BaseQuery;

/**
 * 采用分页拦截器执行物理分页查询时，原生的Executor创建cacheKey时未能包含分页参数page，为了解决这个问题，创建了本拦截器，
 * 本拦截器会拦截CachingExecutor的query方法，在创建cacheKey时将分页参数page包含其中。 老规矩，签名里要拦截的类型只能是接口。
 * 
 * @author jim.ye
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class,
		Object.class, RowBounds.class, ResultHandler.class }) })
public class CacheInterceptor implements Interceptor {
	private static final Log logger = LogFactory.getLog(CacheInterceptor.class);
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static String defaultPageSqlId = ".*Page$"; // 需要拦截的ID(正则匹配)
	private static String pageSqlId = ""; // 需要拦截的ID(正则匹配)

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Executor executorProxy = (Executor) invocation.getTarget();
		MetaObject metaExecutor = MetaObject.forObject(executorProxy, DEFAULT_OBJECT_FACTORY,
				DEFAULT_OBJECT_WRAPPER_FACTORY);
		// 分离代理对象链
		while (metaExecutor.hasGetter("h")) {
			Object object = metaExecutor.getValue("h");
			metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY,
					DEFAULT_OBJECT_WRAPPER_FACTORY);
		}
		// 分离最后一个代理对象的目标类
		while (metaExecutor.hasGetter("target")) {
			Object object = metaExecutor.getValue("target");
			metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY,
					DEFAULT_OBJECT_WRAPPER_FACTORY);
		}
		Object[] args = invocation.getArgs();
		return this.query(metaExecutor, args);
	}

	public <E> List<E> query(MetaObject metaExecutor, Object[] args) throws SQLException {
		MappedStatement ms = (MappedStatement) args[0];
		Object parameterObject = args[1];
		RowBounds rowBounds = (RowBounds) args[2];
		ResultHandler resultHandler = (ResultHandler) args[3];
		BoundSql boundSql = ms.getBoundSql(parameterObject);
		CacheKey cacheKey = createCacheKey(ms, parameterObject, rowBounds, boundSql);
		Executor executor = (Executor) metaExecutor.getOriginalObject();
		return executor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
	}

	private CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds,
			BoundSql boundSql) {
		Configuration configuration = ms.getConfiguration();
		pageSqlId = configuration.getVariables().getProperty("pageSqlId");
		if (null == pageSqlId || "".equals(pageSqlId)) {
			logger.warn("Property pageSqlId is not setted,use default '.*Page$' ");
			pageSqlId = defaultPageSqlId;
		}
		CacheKey cacheKey = new CacheKey();
		cacheKey.update(ms.getId());
		cacheKey.update(rowBounds.getOffset());
		cacheKey.update(rowBounds.getLimit());

		// 当需要分页查询时，将page参数里的当前页和每页数加到cachekey里
		if (ms.getId().matches(pageSqlId) && parameterObject instanceof BaseQuery) {
			BaseQuery page = (BaseQuery) parameterObject;
			if (null != page) {
				cacheKey.update(page.getOriginalPageIndex());
				cacheKey.update(page.getPageSize());
			}
		}
		return cacheKey;
	}

	/**
	 * 只拦截CachingExecutor，其他的直接返回目标本身
	 */
	@Override
	public Object plugin(Object target) {
		if (target instanceof CachingExecutor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {

	}

}
