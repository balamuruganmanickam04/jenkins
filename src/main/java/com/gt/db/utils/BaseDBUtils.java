package com.gt.db.utils;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.gt.db.BaseDAO;

/**
 * Commonly used db functions
 *
 * @author GT
 *         <p>
 *         Mar 3, 2012 com.gt.db.utils-DBUtils.java
 */
public class BaseDBUtils extends BaseDAO {

	/**
	 * Status 1 = active - not deleted
	 */

	public BaseDBUtils() {
		super();
	}

	public final List readAll(Class clazz) throws Exception {
		Session s = getSession();
		try {
			Query q = s.createQuery("from " + clazz.getName() + "  order by id desc");
			return super.runReadQuery(q);
		} finally {
			close(s);
		}
	}

	public final List readAllNoStatus(Class clazz) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("from " + clazz.getName() + "  order by id desc");
			return super.runReadQuery(q);
		} finally {
			close(s);
		}
	}

	public final Object getById(Class clazz, int id) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("from " + clazz.getName() + " where id=:id ");
			q.setInteger("id", id);
			System.out.println(q.getQueryString() + " Reading ID " + id);
			return super.runReadQuery(q).get(0);
		} finally {
			close(s);
		}
	}

	public final Object getByIdNoStatus(Class clazz, int id) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("from " + clazz.getName() + " where id=:id order by id desc");
			q.setInteger("id", id);
			return super.runReadQuery(q).get(0);
		} finally {
			close(s);
		}
	}

	public final int deleteById(Class clazz, int id) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("update " + clazz.getName() + " set  where id=:id");
			q.setInteger("id", id);
			return super.runQuery(q);
		} finally {
			close(s);
		}
	}

	public final int deleteByIdPhysical(Class clazz, int id) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("delete from " + clazz.getName() + " where id=:id");
			q.setInteger("id", id);
			return super.runQuery(q);
		} finally {
			close(s);
		}
	}

	public final void saveOrUpdate(Object object) throws Exception {
		super.saveOrUpdate(object);

	}

	public final List getPaymentDetailsByPurchaseId(Class clazz, int id, String joinTable) throws Exception {

		Session s = getSession();
		try {
			Query q = s.createQuery("from " + clazz.getName() + " where " + joinTable + ".id=:id  order by id desc");
			q.setInteger("id", id);
			return super.runReadQuery(q);
		} finally {
			close(s);
		}
	}
}
