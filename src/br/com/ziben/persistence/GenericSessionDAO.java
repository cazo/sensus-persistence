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
import java.util.Date;
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
	
	private boolean executingTransaction = false;
	private SessionDAOCtrl sessionDAOCtrl = null;

	/**
	 * Get the class that extends me, well...
	 */
	public GenericSessionDAO() {
		this.inClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        HibernateFactory.buildIfNeeded();
    }
	
	public GenericSessionDAO(SessionDAOCtrl sessionParam) {
		this.sessionDAOCtrl = sessionParam;
		// TODO: quando for SessionDAOCtrl preencho????
		this.session= sessionParam.getSession();
		this.tx = sessionParam.getSession().getTransaction();
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
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            session.saveOrUpdate(obj);
	            tx.commit();
        	} else {
        		log.debug("Session sessionDAOCtrl");
        		sessionDAOCtrl.getSession().saveOrUpdate(obj);
        	}
        } catch (HibernateException e) {
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
        		startOperation();
        		session.save(obj);
        		tx.commit();
        	} else {
        		log.debug("Session sessionDAOCtrl");
        		sessionDAOCtrl.getSession().save(obj);        		
        	}
        } catch (HibernateException e) {
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(this.session);
        	}
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
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            session.update(obj);
	            tx.commit();
        	} else {
        		log.debug("Session sessionDAOCtrl");
        		sessionDAOCtrl.getSession().update(obj);
        	}
        } catch (HibernateException e) {
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            session.delete(obj);
	            tx.commit();
        	} else {
        		log.debug("Session sessionDAOCtrl");
        		sessionDAOCtrl.getSession().delete(obj);
        	}
        } catch (HibernateException e) {
//        	tx.rollback(); // Já tratado no handleException()
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
        	Query query = null;
        	String hql = String.format("delete from %s", this.inClass.getName());
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            query = session.createQuery(hql); 
	            query.executeUpdate();
	            tx.commit();
        	} else {
        		log.debug("Session sessionDAOCtrl");
	            query = sessionDAOCtrl.getSession().createQuery(hql); 
	            query.executeUpdate();
        	}
        } catch (HibernateException e) {
//        	tx.rollback();
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            obj = session.load(this.inClass, id);
	            session.flush();
        	} else {
        		log.debug("Session sessionDAOCtrl");
        		obj = sessionDAOCtrl.getSession().load(this.inClass, id);
        		sessionDAOCtrl.getSession().flush();
        	}
        } catch (HibernateException e) {
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
        	Query query = null;
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		log.debug("Session object");
	            startOperation();
	            query = session.createQuery("from " + this.inClass.getName());
        	} else {
        		log.debug("Session sessionDAOCtrl");
	            query = sessionDAOCtrl.getSession().createQuery("from " + this.inClass.getName());
        	}
        	objects = query.list();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
        	if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
        		HibernateFactory.close(session);
        	}
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
			Criteria crit = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
	            startOperation();
	            crit = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
	            crit = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
			
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<< GenericSessionDAO.findByCriteria()");
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
			Criteria crit = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				crit = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				crit = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
            log.info("<< GenericSessionDAO.findByCriteria(ArrayList<Criterion>)");
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
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
	            startOperation();
			    crit = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				crit = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}

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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
            log.info("<< GenericSessionDAO.findByCriteria()");
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
			Criteria criteriaCount = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				criteriaCount = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				criteriaCount = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<<GenericSessionDAO:countForPagination(criterions)");
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
			Criteria criteriaCount = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				criteriaCount = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				criteriaCount = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<<GenericSessionDAO.rowsCountCriteria()");
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
			Criteria criteria = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				criteria = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				criteria = sessionDAOCtrl.getSession().createCriteria(this.inClass); 
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<<GenericSessionDAO.listForPagination()");
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
			Criteria criteria = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				criteria = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				criteria = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<<GenericSessionDAO:listForPagination(Criterion)");
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
			Criteria criteria = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				criteria = session.createCriteria(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				criteria = sessionDAOCtrl.getSession().createCriteria(this.inClass);
			}
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
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<<GenericSessionDAO.listForPagination(int, int, ArrayList<Criterion>)");
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
			Query query = null;
			if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
				log.debug("Session object");
				startOperation();
				query = session.createSQLQuery(strQuery).addEntity(this.inClass);
			} else {
				log.debug("Session sessionDAOCtrl");
				query = sessionDAOCtrl.getSession().createSQLQuery(strQuery).addEntity(this.inClass);
			}
		    list = query.list();
		} catch (HibernateException e) {
            handleException(e);
        } finally {
    		if (sessionDAOCtrl == null || !sessionDAOCtrl.isExecutingTransaction()){
    			HibernateFactory.close(session);
    		}
    		log.info("<< GenericSessionDAO.runQueryEntity()");
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
	 * Set the criteria Restrictions.between 
	 * @param field
	 * @param minorDate
	 * @param majorDate
	 */
	public void setBetween(String field, Date minorDate, Date majorDate){
	   	log.info(">> GenericSessionDAO.setBetween()");
	   	if(field != null && minorDate != null && majorDate != null) {
			Criterion criterio = Restrictions.between(field, minorDate, majorDate);
			criterionList.add(criterio);
	   	}
	   	log.info("<< GenericSessionDAO.setBetween()");
	}

    /**
     * Handle all exceptions on this API
     * @param e the exception
     * @throws DataAccessLayerException
     */
    protected void handleException(HibernateException e) throws DataAccessLayerException {
        HibernateFactory.rollback(tx);
        executingTransaction = false;
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
        executingTransaction = false; // just in case........
        log.debug("<< GenericSessionDAO.startOperation()");
    }
    
    // TODO: usando o sessionDAOCtrl precisa disso? Lembre-se que quem está "mandando é esse cara e ele já contém a sessão
    protected void openSession() throws HibernateException {
        log.debug(">> GenericSessionDAO.openSession()");
        session = HibernateFactory.openSession();
        log.debug("<< GenericSessionDAO.openSession()");
    }

    // TODO: o mesmo comentário que o anterior.
    protected void closeSession(Session sessionParam) throws HibernateException {
        log.debug(">> GenericSessionDAO.closeSession()");
        HibernateFactory.close(sessionParam);
        executingTransaction = false;
        log.debug("<< GenericSessionDAO.closeSession()");
    }
    
    protected void beginTransaction() throws HibernateException {
        log.debug(">> GenericSessionDAO.beginTransaction()");
        session.beginTransaction();
        executingTransaction = true;
        log.debug("<< GenericSessionDAO.beginTransaction()");
    }
    
    protected void commitTransaction() throws HibernateException {
        log.debug(">> GenericSessionDAO.commitTransaction()");
        session.getTransaction().commit();
        executingTransaction = false;
        log.debug("<< GenericSessionDAO.commitTransaction()");
    }

    // TODO: fecho a sessão?????
    protected void rollbackTransaction() throws HibernateException {
        log.debug(">> GenericSessionDAO.rollbackTransaction()");
        session.getTransaction().rollback();
        executingTransaction = false;
        HibernateFactory.close(session);
        log.debug("<< GenericSessionDAO.rollbackTransaction()");
    }

    /**
     * just return the current session
     * @return
     */
	protected Session getSession() {
		return session;
	}

	public boolean isExecutingTransaction() {
		return executingTransaction;
	}
//
//	public void setExecutingTransaction(boolean executingTransaction) {
//		this.executingTransaction = executingTransaction;
//	}
}