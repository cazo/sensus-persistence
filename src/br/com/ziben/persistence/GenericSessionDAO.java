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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

/**
 * Class to handle DAOs by a generic hibernate session factoring
 * @author ccardozo
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public abstract class GenericSessionDAO<T> {
	
	private Logger log = Logger.getLogger(GenericSessionDAO.class);
	
    private Session session;
    private Transaction tx;
	private Class<T> inClass;

	/**
	 * Get the class that extends me, well...
	 */
	public GenericSessionDAO() {
		this.inClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        HibernateFactory.buildIfNeeded();
    }

	/**
	 * Save a record represented by T class
	 * @param obj
	 */
    protected void persist(T obj) {
        log.debug(">> GenericSessionDAO.persist(): " + this.inClass.toString());
        try {
            startOperation();
            session.saveOrUpdate(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.persist(): " + this.inClass.toString());
        }
    }

    /**
     * Remove a record represented by T class
     * @param obj
     */
    protected void delete(T obj) {
        log.debug(">> GenericSessionDAO.delete(): " + this.inClass.toString());
        try {
            startOperation();
            session.delete(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.delete(): " + this.inClass.toString());
        }
    }

    /**
     * Find an object by key
     * @param id
     * @return the object, if exists
     */
    protected Object find(Serializable id) {
        log.debug(">> GenericSessionDAO.find(): " + this.inClass.toString());
        Object obj = null;
        try {
            startOperation();
            obj = session.load(this.inClass, id);
            session.flush();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        	log.debug("<< GenericSessionDAO.find()");
        }
        return obj;
    }

    /**
     * Find all records from a entity
     * @param clazz
     * @return List<T>
     */
    protected List<T> findAll(T clazz) {
        log.debug(">> GenericSessionDAO.findAll(): " + this.inClass.toString());
        List<T> objects = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + this.inClass.getName());
            objects = query.list();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        	log.debug("<< GenericSessionDAO.findAll()");
        }
        return objects;
    }

    /**
     * Find records by a Criteiron set of arguments
     * @param criterion
     * @return List<T>
     */
    protected List<T> findByCriteria(Criterion... criterion) {
		log.info(">> GenericSessionDAO.findByCriteria()");
		Criteria crit = null;
		List<T> list = null;
		try {
            startOperation();
		    crit = session.createCriteria(this.inClass);
		    for (final Criterion c : criterion) {
		    	crit.add(c);
		    }
		    list = crit.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<< GenericSessionDAO.findByCriteria()");
            HibernateFactory.close(session);
        }
		return list;
    }
    
    protected List<T> findByCriteria(ArrayList<Criterion> criterions) {
		log.info(">> GenericSessionDAO.findByCriteria(ArrayList<Criterion>)");
		Criteria crit = null;
		List<T> list = null;
		try {
            startOperation();
            
		    crit = session.createCriteria(this.inClass);
		    for (final Criterion c : criterions) {
		    	crit.add(c);
		    }
		    list = crit.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<< GenericSessionDAO.findByCriteria(ArrayList<Criterion>)");
            HibernateFactory.close(session);
        }
		return list;
    }

    /**
     * Return how many records has a entity by T
     * @return
     */
    protected Long rowsCount() {
		log.info(">> GenericSessionDAO.countForPagination()");
		
		Long count = 0L;
		
		try {
            startOperation();
    		Criteria criteriaCount = session.createCriteria(this.inClass);
    		criteriaCount.setProjection(Projections.rowCount());
			count = (Long) criteriaCount.uniqueResult();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<< GenericSessionDAO.countForPagination()");
            HibernateFactory.close(session);
        }
		return count;
    }
    
    protected Long rowsCount(ArrayList<Criterion> criterions) {
		log.info(">>GenericSessionDAO:countForPagination(criterions)");

		Long count = 0L;
		
		try {
            startOperation();
    		Criteria criteriaCount = session.createCriteria(this.inClass);
    		criteriaCount.setProjection(Projections.rowCount());
		    for (final Criterion c : criterions) {
		    	criteriaCount.add(c);
		    }

			count = (Long) criteriaCount.uniqueResult();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO:countForPagination(criterions)");
            HibernateFactory.close(session);
        }
		return count;
    }
    
    /**
     * List for pagination by a T class, using a start and finish records
     * @param start
     * @param finish
     * @return List<T>
     */
    protected List<T> listForPagination(int start, int finish) {
		log.info(">> GenericSessionDAO.listForPagination()");
		List<T> pages = null;
		try {
            startOperation();
            Criteria criteria = session.createCriteria(this.inClass);
            criteria.setFirstResult(start);
            criteria.setMaxResults(finish);
            pages = criteria.list();

		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO.listForPagination()");
            HibernateFactory.close(session);
        }
		return pages;
    }
    
    /**
     * List for pagination with a start, a finish and criterias you want
     * @param start
     * @param finish
     * @param criterion
     * @return List<T>
     */
    protected List<T> listForPagination(int start, int finish, Criterion... criterion) {
		log.info(">>GenericSessionDAO:listForPagination(Criterion)");
		List<T> pages = null;
		try {
            startOperation();
            
            Criteria criteria = session.createCriteria(this.inClass);
            criteria.setFirstResult(start);
            criteria.setMaxResults(finish);

            pages = criteria.list();

		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO:listForPagination(Criterion)");
            HibernateFactory.close(session);
        }
		return pages;
    }

    /**
     * List for pagination with a start, a finish and a List of criterias you want
     * @param start
     * @param finish
     * @param criterion
     * @return List<T>
     */
    protected List<T> listForPagination(int start, int finish, ArrayList<Criterion> criterions) {
		log.info(">>GenericSessionDAO.listForPagination(int, int, ArrayList<Criterion>)");
		List<T> pages = null;
		try {
            startOperation();
            
            Criteria criteria = session.createCriteria(this.inClass);
            criteria.setFirstResult(start);
            criteria.setMaxResults(finish);
            
		    for (final Criterion c : criterions) {
		    	criteria.add(c);
		    }
            
            pages = criteria.list();

		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO.listForPagination(int, int, ArrayList<Criterion>)");
            HibernateFactory.close(session);
        }
		return pages;
    }

    /**
     * Execute a SQL provided, and set the query as an entity represented by T class
     * @param strQuery
     * @return
     */    
    protected List<T> runQueryEntity(String strQuery) {
		log.info(">> GenericSessionDAO.runQueryEntity()");
		List<T> list = null;
		try {
            startOperation();
		    Query query = session.createSQLQuery(strQuery).addEntity(this.inClass);
		    list = query.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<< GenericSessionDAO.runQueryEntity()");
            HibernateFactory.close(session);
        }
		return list;
    }
    
    /**
     * Handle all exceptions on this API
     * @param e the exceptio
     * @throws DataAccessLayerException
     */
    protected void handleException(HibernateException e) throws DataAccessLayerException {
        HibernateFactory.rollback(tx);
        throw new DataAccessLayerException(e);
    }

    /**
     * Well, the place all starts to work...
     * @throws HibernateException
     */
    protected void startOperation() throws HibernateException {
        log.debug(">> GenericSessionDAO.startOperation()");
        session = HibernateFactory.openSession();
        tx = session.beginTransaction();
        log.debug("<< GenericSessionDAO.startOperation()");
    }

	public Session getSession() {
		return session;
	}
	
    protected List<T> listForPaginationA(int start, int finish, ArrayList<Criterion> criterions) {
		log.info(">>IwAcaoDAO.listForPaginationA(int, int, ArrayList<Criterion>)");
		List<T> pages = null;
		try {
            startOperation();
            
            Criteria criteria = getSession().createCriteria(this.inClass);
            criteria.setFirstResult(start);
            criteria.setMaxResults(finish);
            
            criteria = criteriaDao(criteria);
            
//            if (orderList != null){
//            	for (final Order order : orderList) {
//	            	criteria.addOrder(order);
//				}
//            }
            
		    for (final Criterion c : criterions) {
		    	criteria.add(c);
		    }
            
            pages = criteria.list();

		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<IwAcaoDAO.listForPagination(int, int, ArrayList<Criterion>)");
            HibernateFactory.close(getSession());
        }
		return pages;
    }
    
    protected Criteria criteriaDao(Criteria criteria){
    	log.info(">>ANCESTRAL IwAcaoDAO.criteriaOrder");
		criteria.addOrder(Order.desc("codAcao"));
		log.info("<<IwAcaoDAO.criteriaOrder");
		return criteria;
	}
}