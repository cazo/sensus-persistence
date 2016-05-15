/*
This file is part of sensus-persistence (SessionFactory on Hibernate).

Sensus-persistence is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Sensus-persistence is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with sensus-persistence.  If not, see <http://www.gnu.org/licenses/>.
*/
package br.com.ziben.persistence;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;

/**
 * Class to handle DAOs by a generic hibernate session factoring
 * @author ccardozo
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public abstract class GenericSessionDAO<T> {
	
	private Logger log = Logger.getLogger("GenericSessionDAO");
	
    private Session session;
    private Transaction tx;
	private Class<T> classe;

	public GenericSessionDAO() {
		this.classe = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        HibernateFactory.buildIfNeeded();
    }

    protected void persist(T obj) {
        log.debug(">>GenericSessionDAO:persist(): " + this.classe.toString());
        try {
            startOperation();
            session.saveOrUpdate(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<<GenericSessionDAO:persist(): " + this.classe.toString());
        }
    }

    protected void delete(T obj) {
        log.debug(">>GenericSessionDAO:delete(): " + this.classe.toString());
        try {
            startOperation();
            session.delete(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<<GenericSessionDAO:delete(): " + this.classe.toString());
        }
    }

    protected Object find(Serializable id) {
        log.debug(">>GenericSessionDAO:find(): " + this.classe.toString());
        Object obj = null;
        try {
            startOperation();
            obj = session.load(this.classe, id);
            session.flush();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        	log.debug("<<GenericSessionDAO:find()");
        }
        return obj;
    }

    protected List<T> findAll(T clazz) {
        log.debug(">>GenericSessionDAO:findAll(): " + this.classe.toString());
        List<T> objects = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + this.classe.getName());
            objects = query.list();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        	log.debug("<<GenericSessionDAO:findAll()");
        }
        return objects;
    }

    protected List<T> findByCriteria(Criterion... criterion) {
		log.info(">>GenericSessionDAO:findByCriteria()");
		Criteria crit = null;
		List<T> list = null;
		try {
            startOperation();
		    crit = session.createCriteria(this.classe);
		    for (final Criterion c : criterion) {
		    	crit.add(c);
		    }
		    list = crit.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO:findByCriteria()");
            HibernateFactory.close(session);
        }
		return list;
    }

    protected Long countForPagination() {
		log.info(">>GenericSessionDAO:countForPagination()");
		
		Long count = 0L;
		
		try {
            startOperation();
    		Criteria criteriaCount = session.createCriteria(this.classe);
    		criteriaCount.setProjection(Projections.rowCount());
			count = (Long) criteriaCount.uniqueResult();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO:countForPagination()");
            HibernateFactory.close(session);
        }
		return count;
    }

    protected List<?> runQuery(String strQuery) {
		log.info(">>GenericSessionDAO:runQuery()");
		List<?> list = null;
		try {
            startOperation();
		    Query query = session.createSQLQuery(strQuery);
		    list = query.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO:runQuery()");
            HibernateFactory.close(session);
        }
		return list;
    }
    
    protected void handleException(HibernateException e) throws DataAccessLayerException {
        HibernateFactory.rollback(tx);
        throw new DataAccessLayerException(e);
    }

    protected void startOperation() throws HibernateException {
        log.debug(">>GenericSessionDAO:startOperation()");
        session = HibernateFactory.openSession();
        session.setFlushMode(FlushMode.MANUAL);
        ManagedSessionContext.bind(session);
        tx = session.beginTransaction();
        log.debug("<<GenericSessionDAO:startOperation()");
    }
}
