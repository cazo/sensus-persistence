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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

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
	private ArrayList<Criterion> criterionList = new ArrayList<Criterion>();
	private List<Order> orderList = new ArrayList<Order>();

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
	 * Save a record represented by T class
	 * @param obj
	 */
    protected void save(T obj) {
        log.debug(">> GenericSessionDAO.save(): " + this.inClass.toString());
        try {
            startOperation();
            session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.save(): " + this.inClass.toString());
        }
    }
    
	/**
	 * Update a record represented by T class
	 * @param obj
	 */
    protected void update(T obj) {
        log.debug(">> GenericSessionDAO.update(): " + this.inClass.toString());
        try {
            startOperation();
            session.update(obj);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.update(): " + this.inClass.toString());
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
        	tx.rollback();
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.delete(): " + this.inClass.toString());
        }
    }

    /**
     * Clean a Table represented by T class
     * @param obj
     */
    protected void clean() {
        log.debug(">> GenericSessionDAO.clean(): " + this.inClass.toString());
        try {
            startOperation();
            
            String hql = String.format("delete from %s", this.inClass.getName());
            Query query = session.createQuery(hql); 
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
        	tx.rollback();
            handleException(e);
        } finally {
            HibernateFactory.close(session);
            log.debug("<< GenericSessionDAO.clean(): " + this.inClass.toString());
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
    protected List<T> findAll() {
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
     * @deprecated
     */
    protected List<T> findByCriteria(Criterion... criterion) {
		log.info(">> GenericSessionDAO.findByCriteria()");
		List<T> list = null;
		try {
            startOperation();
            
            Criteria crit = session.createCriteria(this.inClass);
            // verify if criteria exists
            if(criterion != null) {
			    for (final Criterion c : criterion) {
			    	crit.add(c);
			    }
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
    
    /**
     * find by criteria usinf a array of criterions
     * @param criterions
     * @return
     * @deprecated
     */
    protected List<T> findByCriteria(ArrayList<Criterion> criterions) {
		log.info(">> GenericSessionDAO.findByCriteria(ArrayList<Criterion>)");
		List<T> list = null;
		try {
            startOperation();
            
            Criteria crit = session.createCriteria(this.inClass);
            // verify if criteria exists
            if(criterions != null) {
			    for (final Criterion c : criterions) {
			    	crit.add(c);
			    }
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
     * find by criteria based on criterion list and Order list setted first
     * @return
     */
    protected List<T> findByCriteria() {
		log.info(">> GenericSessionDAO.findByCriteria()");
		Criteria crit = null;
		List<T> list = null;
		try {
            startOperation();
            
		    crit = session.createCriteria(this.inClass);
		    // verify is exists some criterias to apply
		    if(criterionList != null) {
			    for (final Criterion c : criterionList) {
			    	crit.add(c);
			    }
		    }
		    // verify if exists order to apply
		    if (orderList != null){
            	for (final Order order : orderList) {
            		crit.addOrder(order);
				}
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
    
    /**
     * table rows count giving a criterion list
     * @param criterions
     * @return
     * @deprecated use rowsCountCriteria() setting the criterias or ordering
     */
    protected Long rowsCount(ArrayList<Criterion> criterions) {
		log.info(">>GenericSessionDAO:countForPagination(criterions)");

		Long count = 0L;
		
		try {
            startOperation();
    		Criteria criteriaCount = session.createCriteria(this.inClass);
    		criteriaCount.setProjection(Projections.rowCount());
    		
    		// apply criterions, if exist
    		if(criterions != null) {
			    for (final Criterion c : criterions) {
			    	criteriaCount.add(c);
			    }
    		}
		    // verify if existe order to apply
		    if (orderList != null){
            	for (final Order order : orderList) {
            		criteriaCount.addOrder(order);
				}
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
     * table rows count
     * @return
     */
    protected Long rowsCount() {
    	return rowsCountCriteria();
    }
    
    /**
     * table rowns count with criterias and ordering if you want
     * @return numer of regsters
     */
    protected Long rowsCountCriteria() {
		log.info(">>GenericSessionDAO.rowsCountCriteria()");

		Long count = 0L;
		
		try {
            startOperation();
    		Criteria criteriaCount = session.createCriteria(this.inClass);
    		criteriaCount.setProjection(Projections.rowCount());
            // verify if exists criterions to apply
            if(criterionList != null) {
    		    for (final Criterion c : criterionList) {
    		    	criteriaCount.add(c);
    		    }
            }
		    // verify if existe order to apply
		    if (orderList != null){
            	for (Order order : orderList) {
            		criteriaCount.addOrder(order);
				}
            }

			count = (Long) criteriaCount.uniqueResult();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		log.info("<<GenericSessionDAO.rowsCountCriteria()");
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
            
            // verify if exists criterions to apply
            if(criterionList != null) {
    		    for (final Criterion c : criterionList) {
    		    	criteria.add(c);
    		    }
            }

		    // verify if existe order to apply
		    if (orderList != null){
            	for (final Order order : orderList) {
	            	criteria.addOrder(order);
				}
            }
		    
            pages = criteria.list();
    		log.info(">> GenericSessionDAO.listForPagination() pages size: " + pages.size());

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
            
            // verify if exists criterions to apply
            if(criterionList != null) {
    		    for (final Criterion c : criterionList) {
    		    	criteria.add(c);
    		    }
            }
            // verify if existe order to apply
		    if (orderList != null){
            	for (final Order order : orderList) {
	            	criteria.addOrder(order);
				}
            }

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
            // setting the range
            criteria.setFirstResult(start);
            criteria.setMaxResults(finish);
            
            // verify if exists criterias
            if(criterions != null) {
			    for (final Criterion c : criterions) {
			    	criteria.add(c);
			    }
            }
            // verify if existe order to apply
		    if (orderList != null){
            	for (final Order order : orderList) {
	            	criteria.addOrder(order);
				}
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
	 * Set the criteria Restrictions.eq
	 * @param field
	 * @param value
	 */
	public void setEq(String field, String value){
	   	log.info(">> GenericSessionDAO.setEq");
	   	if(field != null && value != null) {
			Criterion criterio = Restrictions.eq(field, value);
			criterionList.add(criterio);
	   	}
	   	log.info("<< GenericSessionDAO.setEq");
	}

	/**
	 * Set the criteria Restrictions.eq
	 * @param field
	 * @param value
	 */
	public void setEq(String field, Serializable value){
	   	log.info(">> GenericSessionDAO.setEq");
	   	if(field != null && value != null) {
			Criterion criterio = Restrictions.eq(field, value);
			criterionList.add(criterio);
	   	}
	   	log.info("<< GenericSessionDAO.setEq");
	}

	/**
     * Define ascending ordering
     * @return
     */
    public void setOrderAsc(String nameToOrder){
    	log.info(">>GenericSessionDAO.setOrderAsc");
    	if(nameToOrder != null) {
    		orderList.add(Order.asc(nameToOrder));
    	}
		log.info("<<GenericSessionDAO.setOrderAsc");

	}

	/**
     * Define descendent ordering
     * @return
     */
    public void setOrderDesc(String nameToOrder){
    	log.info(">>GenericSessionDAO.setOrderDesc");
    	if(nameToOrder != null) {
    		orderList.add(Order.desc(nameToOrder));
    	}
		log.info("<<GenericSessionDAO.setOrderDesc");

	}

	/**
	 * Set the criteria Restrictions.like
	 * @param field
	 * @param value
	 * @param matchMode
	 */
	public void setLike(String field, String value, MatchMode matchMode){
    	log.info(">>GenericSessionDAO.setLike(String, String, MatchMode)");
    	if(field != null && value != null) {
			Criterion criterio = Restrictions.ilike(field, value, matchMode );
			criterionList.add(criterio);
    	}
    	log.info(">>GenericSessionDAO.setLike(String, String, MatchMode)");
	}

	/**
	 * Set the criteria Restrictions.like with MatchMode.ANYWHERE
	 * @param campo
	 * @param valor
	 */
	public void setLike(String field, String value){
    	log.info(">>GenericSessionDAO.setLike(String, String)");
    	if(field != null && value != null) {
    		Criterion criterio = Restrictions.ilike(field, value, MatchMode.ANYWHERE );
			criterionList.add(criterio);
    	}
    	log.info("<<GenericSessionDAO.setLike(String, String)");

	}

    /**
     * Handle all exceptions on this API
     * @param e the exception
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

    /**
     * just return the current session
     * @return
     */
	public Session getSession() {
		return session;
	}
}